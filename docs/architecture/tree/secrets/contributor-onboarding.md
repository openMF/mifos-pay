# Contributor Secrets Bootstrap

> For external contributors to the mifos-x fork. Maintainers manage the canonical secrets in a private SOPS+age vault you do NOT have access to. This guide walks you through getting the credentials you need to run CI on YOUR fork.

## TL;DR

1. Install `rclone` + `age` (one-time, 2 min)
2. Generate a personal age identity key (one-time, 10 sec)
3. Send your PUBLIC key to a maintainer; wait for them to add you as a recipient (next bundle push)
4. Configure rclone for the shared secrets Drive (one-time, 5 min)
5. Run `bash scripts/fetch-secrets-from-drive.sh`
6. Run `bash scripts/secrets/sync-secrets-to-github.sh` to push into YOUR fork's GitHub Actions

Total time: ~15 min one-time, then ~30 sec per refresh.

## Prerequisites

### macOS

```bash
brew install rclone age
```

### Linux (Debian/Ubuntu)

```bash
sudo apt install rclone age   # Ubuntu 22.04+; older = use https://age-encryption.org/ binaries
```

### Windows

Use WSL2 + the Linux instructions, or:
- rclone: https://rclone.org/install/#windows
- age: https://github.com/FiloSottile/age/releases

## Step 1 — Generate your age identity key

```bash
age-keygen -o ~/.config/mifos-secrets.key
```

This creates ONE file with TWO things:
- **Private identity** (rest of the file) — KEEP this secret. Never commit. Never share.
- **Public recipient** (line starting `# public key:`) — share this freely.

Find the public key:

```bash
grep '^# public key:' ~/.config/mifos-secrets.key | cut -d: -f2 | tr -d ' '
# → age1abc123...xyz789
```

## Step 2 — Send the PUBLIC key to a maintainer

Email / Slack / GitHub DM the `age1...` line to one of the maintainers. They will:

1. Add it to the vault's `drive_remote.age_recipients[]`
2. Re-upload the next bundle (or wait until next routine sync)
3. Confirm to you when done

⚠️ If you ever lose `~/.config/mifos-secrets.key`, you'll need to generate a new pair and ask to be re-added — the old bundles cannot be decrypted without that file.

## Step 3 — Configure rclone for the shared secrets Drive

```bash
rclone config
```

Interactive prompts:

1. `n` → New remote
2. Name: `mifos-drive`
3. Storage: `drive` (Google Drive)
4. `client_id` / `client_secret`: blank (uses rclone defaults)
5. Scope: `2` (`drive.file` — read-only-of-its-own-files; minimal scope)
6. `service_account_file`: blank (you use OAuth, not SA-JSON like maintainers)
7. `Edit advanced config?`: `n`
8. `Use auto config?`: `y` → browser opens → log in with the Google account the maintainer shared the folder with
9. `Configure as team drive?`: `n`
10. Confirm: `y`

Verify:

```bash
rclone about mifos-drive:
# Prints quota + the authenticated email — confirm it matches what the maintainer expected
```

## Step 4 — First sync

```bash
cd /path/to/your/kmp-project-template-fork
bash scripts/fetch-secrets-from-drive.sh
```

Output ends with:

```
✅ Secrets extracted to ./secrets/
Next step: push to your fork's GitHub Actions secrets:
   bash scripts/secrets/sync-secrets-to-github.sh --repo {your-user}/kmp-project-template
```

## Step 5 — Push to YOUR fork's GitHub Actions

```bash
bash scripts/secrets/sync-secrets-to-github.sh --repo {your-github-username}/kmp-project-template
```

This uses `gh secret set` against your fork's repo — you need `gh auth login` first.

## Troubleshooting

| Symptom | Fix |
|---|---|
| `❌ age decrypt failed` | Your public key isn't on the recipient list. Ask maintainer to re-add + re-upload. |
| `❌ rclone download failed` | Drive ACL doesn't grant access to your Google account. Ask maintainer to add you to the Drive folder ACL (separate from age recipients). |
| `gh secret set` fails | `gh auth login` first; need a token with `repo` scope. |
| "Bundle not found" | Maintainer hasn't pushed yet OR you're looking at the wrong folder. Confirm `--folder` matches what the maintainer set up. |

## Routine refresh

When the maintainer rotates a secret, they push a new bundle. You sync with:

```bash
bash scripts/fetch-secrets-from-drive.sh
bash scripts/secrets/sync-secrets-to-github.sh
```

30 seconds total. No re-config required.

## Security notes

- **`~/.config/mifos-secrets.key` MUST stay private.** If your laptop is compromised, generate a new pair and ask to be re-added; old bundles still on Drive can be decrypted by anyone with the old key.
- **`secrets/` directory is gitignored.** Confirm `.gitignore` carries `secrets/` before any commit.
- **Don't share your rclone OAuth token.** It's in `~/.config/rclone/rclone.conf` — treat it like a password.
- **age has no revocation.** Removing your name from the recipient list on the next bundle push is the only "revocation" — you still have access to previously-uploaded bundles if you saved them.

---

## What gets put where (kmp-app layout)

After `bash scripts/fetch-secrets-from-drive.sh`, the layout-aware materialization (since 2026-06-17 per PLAN-secrets-layout-standardization) drops files at these canonical paths:

| File | What it is |
|---|---|
| `local.properties` | Env vars (STRIPE_SECRET_KEY, SENTRY_DSN, etc.) |
| `secrets/android/keystores/release.jks` | Android signing keystore |
| `secrets/android/keystores/keystore.properties` | Signing config |
| `cmp-android/google-services.json` | Firebase Android |
| `cmp-ios/iosApp/GoogleService-Info.plist` | Firebase iOS |
| `secrets/fastlane/play-store-sa.json` | Play Store deploy SA |
| `secrets/fastlane/AuthKey_<KEY_ID>.p8` | App Store Connect cert |

`./gradlew assembleDebug` Just Works™ — no further config needed; Gradle reads `local.properties` natively and the Google services plugin picks up the canonical paths.

For the full layout map per project type, see (maintainer-side) `docs/guides/framework/SECRETS_LAYOUT_GUIDE.md`.
