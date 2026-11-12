# deployment/desktop/dmg-notarized/lane.rb
# Imported by deployment/fastlane/Fastfile via the per-target lane.rb glob.
#
# Tier-2 Apple-notarized macOS DMG → GitHub Releases. Replaces the legacy
# `script.sh` (deleted) with the same Fastlane discipline used by
# deployment/desktop/mac-app-store/lane.rb and deployment/ios/appstore/lane.rb.
#
# The four pipeline steps that used to be hand-rolled `xcrun` / `security` /
# `gh` shell calls now go through native Fastlane actions:
#
#   custom bash (script.sh)              →  native Fastlane
#   ───────────────────────────────────────────────────────────────────
#   security create-keychain + import    →  match(type: "developer_id", platform: "macos")
#   xcrun notarytool submit --wait       →  notarize(package: <dmg>, ...)
#   xcrun stapler staple                 →  notarize(skip_stapling: false)  (default)
#   gh release upload                    →  sh("gh release upload ...")     (deliberate; see below)
#
# Why some `sh()` survives by design:
#   1. Gradle build — Compose Desktop has no Fastlane build action; same pattern
#      as mac-app-store/lane.rb#build_mac_pkg.
#   2. codesign — `sh("codesign ...")` matches mac-app-store/lane.rb#build_mac_pkg
#      step 4; the Fastlane codesign action is just a wrapper around the CLI.
#   3. hdiutil — no Fastlane equivalent; one-line invocation.
#   4. gh release upload — `set_github_release` CREATES a release; we want to
#      attach an asset to one the caller's tag-release workflow already created.
#
# Cert lifecycle:
#   Match-managed Developer ID Application cert. Lives in the same Match repo as
#   the iOS / mac-app-store certs (different cert TYPE, same store). On first
#   bootstrap, a maintainer seeds the repo once:
#     bundle exec fastlane match developer_id --platform macos
#   Subsequent runs are `readonly: true` — CI never mutates the cert. To rotate,
#   run with `match_readonly: false` from a maintainer machine.
#
# Secrets (resolved via _shared/config.rb#_secret — ENV first, secrets/ fallback):
#   secrets/live/apple/match/match_ci_key             — SSH key for Match git repo access
#   secrets/live/apple/match/.match_password          — Match encryption password
#   secrets/live/apple/appstore/AuthKey.p8            — ASC API key (Match cert issuance + notarize)
#   secrets/live/apple/appstore/key_id                — ASC key ID
#   secrets/live/apple/appstore/issuer_id             — ASC issuer ID
#
# Env vars (optional overrides):
#   MAC_APP_IDENTIFIER  — macOS bundle ID (defaults to ForkIdentity::APP_ID)
#   GIT_TAG             — GitHub Release tag (else: `git describe --tags --abbrev=0`)
#   FLAVOR              — Gradle flavor dimension (default: prod)
#   STAGE               — Promotion stage: prerelease | beta | stable (default: stable). Controls
#                         the GitHub Release `prerelease` + `latest` flags so the same lane spans
#                         Stage 1 (RC), Stage 2 (Beta), Stage 3 (Production) of the direct-distro ladder.
#   GITHUB_REPOSITORY   — owner/repo for `gh release upload` (GHA sets this; auto-derived locally)
#   AD_HOC_SIGNING=1    — skip Match + notarize for fast local dev (per config.yaml#dev_fallback)

require_relative "../../_shared/lib/appstore_helpers"

DMG_NOTARIZED_ENTITLEMENTS = File.join(
  DEPLOYMENT_REPO_ROOT, "deployment/desktop/dmg-notarized/Entitlements.plist"
).freeze

platform :mac do
  desc "Tier-2 Apple-notarized macOS DMG → GitHub Releases (Developer ID via Match + notarytool via Fastlane notarize)"
  lane :buildNotarizedMacDmg do |options|
    options       = sanitize_options(options)
    mac_bundle_id = ENV["MAC_APP_IDENTIFIER"] || ENV["MAC_BUNDLE_ID"] || ForkIdentity::APP_ID
    repo_root     = DEPLOYMENT_REPO_ROOT
    gradlew       = File.join(repo_root, "gradlew")
    flavor        = options[:flavor] || ENV["FLAVOR"] || "prod"
    stage         = (options[:stage] || ENV["STAGE"] || "stable").to_s.downcase
    UI.user_error!("Invalid STAGE: #{stage} (expected: prerelease | beta | stable)") unless %w[prerelease beta stable].include?(stage)
    ad_hoc        = options[:ad_hoc] == true || ENV["AD_HOC_SIGNING"] == "1"

    setup_ci_if_needed
    with_ios_preamble(options)

    if ad_hoc
      UI.important("⚠️  AD_HOC mode — skipping Match + notarize + GH upload (trusted-device dev build).")
      app_path = _build_unsigned_mac_app(repo_root, gradlew, flavor)
      sh("codesign --force --deep --sign - #{app_path.shellescape}")
      dmg_path = _wrap_mac_app_in_dmg(repo_root, app_path)
      UI.success("✅ Ad-hoc-signed DMG: #{dmg_path}")
      next
    end

    tag = options[:tag] || ENV["GIT_TAG"] || _resolve_git_tag(repo_root)
    UI.user_error!("Unable to resolve a GitHub Release tag (set GIT_TAG env or pass tag: option)") if tag.to_s.strip.empty?

    load_api_key(options)
    _fetch_developer_id_cert(options, mac_bundle_id)

    app_path = _build_unsigned_mac_app(repo_root, gradlew, flavor)
    identity = _resolve_developer_id_identity
    _sign_mac_app_with_developer_id(app_path, identity)
    dmg_path = _wrap_mac_app_in_dmg(repo_root, app_path)

    UI.message("📤 Submitting #{File.basename(dmg_path)} to notarytool (5-15 min wait)...")
    notarize(
      package:   dmg_path,
      bundle_id: mac_bundle_id,
      api_key:   Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      verbose:   true,
      # skip_stapling defaults to false — notarize action staples the DMG after
      # notarytool returns "Accepted" so the ticket is embedded for offline verify.
    )
    UI.success("✅ Notarized + stapled")

    _upload_dmg_to_github_release(tag, dmg_path)
    _set_release_stage_flags(tag, stage)
    UI.success("🚀 Released #{tag} (stage=#{stage}) ← #{File.basename(dmg_path)}")
  end

  # ── Helpers ────────────────────────────────────────────────────────────────

  # Match: install Developer ID Application cert + private key into the active
  # keychain. On CI, setup_ci has already created build.keychain; locally Match
  # imports into login.keychain-db. readonly: true by default — CI never rotates.
  def _fetch_developer_id_cert(options, mac_bundle_id)
    cfg     = FastlaneConfig::IosConfig::BUILD_CONFIG
    ssh_key = File.join(DEPLOYMENT_REPO_ROOT, cfg[:match_ssh_key_path])

    match_pass = options[:match_password] || ENV["MATCH_PASSWORD"] || cfg[:match_password]
    ENV["MATCH_PASSWORD"] = match_pass.to_s if match_pass && ENV["MATCH_PASSWORD"].to_s.empty?

    match(
      type:            "developer_id",
      platform:        "macos",
      app_identifier:  mac_bundle_id,
      git_url:         options[:match_git_url]    || cfg[:match_git_url],
      git_branch:      options[:match_git_branch] || cfg[:match_git_branch],
      git_private_key: File.exist?(ssh_key) ? ssh_key : nil,
      api_key:         Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      readonly:        options.key?(:match_readonly) ? options[:match_readonly] : true,
    )
  end

  # Build the unsigned .app bundle. Compose Desktop 1.11.0's internal signing
  # path is broken on macOS 15 Sequoia (see mac-app-store/lane.rb#L7-L13) —
  # build unsigned, sign manually in sh() context where security/codesign work.
  def _build_unsigned_mac_app(repo_root, gradlew, flavor)
    saved_env = {}
    %w[MAC_SIGNING_IDENTITY MAC_KEYCHAIN_PATH MAC_PROVISIONING_PROFILE_PATH].each do |k|
      saved_env[k] = ENV.delete(k)
    end

    begin
      UI.message("Building unsigned .app bundle (createReleaseDistributable, flavor=#{flavor})...")
      sh(gradlew, "-p", repo_root, ":cmp-desktop:createReleaseDistributable",
         "-PflavorDimension=#{flavor}",
         "--no-daemon", "--no-configuration-cache")
    ensure
      saved_env.each { |k, v| ENV[k] = v if v }
    end

    app_dir  = File.join(repo_root, "cmp-desktop/build/compose/binaries/main-release/app")
    app_path = Dir[File.join(app_dir, "*.app")].first
    UI.user_error!("No .app bundle produced at #{app_dir}") unless app_path && File.exist?(app_path)
    UI.message("App bundle: #{app_path}")

    # arm64-only deployment target must be ≥ 12.0 (matches mac-app-store discipline).
    info_plist = File.join(app_path, "Contents", "Info.plist")
    if File.exist?(info_plist)
      sh("/usr/libexec/PlistBuddy -c 'Set :LSMinimumSystemVersion 12.0' #{info_plist.shellescape}",
         log: false)
      UI.message("📋 Set LSMinimumSystemVersion → 12.0")
    end

    app_path
  end

  # Prefer SHA1 over display name to avoid "ambiguous" codesign errors when
  # multiple Developer ID certs exist (mirrors mac-app-store/lane.rb#L266-L274).
  def _resolve_developer_id_identity
    sha1 = sh(
      "security find-certificate -c 'Developer ID Application' -Z 2>/dev/null" \
      " | grep 'SHA-1 hash:' | tail -1 | awk '{print $3}'",
      log: false,
    ).strip
    UI.user_error!(
      "No 'Developer ID Application' cert in keychain — Match install must have failed. " \
      "Did you seed the Match repo? `bundle exec fastlane match developer_id --platform macos`"
    ) if sha1.empty?
    UI.message("🔏 Developer ID SHA1: #{sha1}")
    sha1
  end

  # Codesign the .app with Developer ID, hardened runtime and the entitlements file.
  # Hardened runtime is a notarisation requirement, so a DMG build cannot skip it.
  def _sign_mac_app_with_developer_id(app_path, identity)
    UI.message("Signing .app with Developer ID + hardened runtime + entitlements...")
    sh(
      "codesign --force --deep --options runtime " \
      "--entitlements #{DMG_NOTARIZED_ENTITLEMENTS.shellescape} " \
      "--sign #{identity.shellescape} #{app_path.shellescape}",
    )
    sh("codesign --verify --deep --strict --verbose=2 #{app_path.shellescape}")
    UI.success("✅ .app signed + verified")
  end

  # hdiutil — UDZO is the standard compressed read-only DMG format. -ov overwrites
  # any prior artifact. The .app must be FULLY signed BEFORE wrapping; notarytool
  # inspects the contents and rejects DMGs containing an unsigned/unhardened binary.
  def _wrap_mac_app_in_dmg(repo_root, app_path)
    dmg_dir  = File.join(repo_root, "cmp-desktop/build/compose/binaries/main-release/dmg")
    FileUtils.mkdir_p(dmg_dir)
    app_name = File.basename(app_path, ".app")
    dmg_path = File.join(dmg_dir, "#{app_name}.dmg")
    FileUtils.rm_f(dmg_path)

    sh(
      "hdiutil create -volname #{app_name.shellescape} " \
      "-srcfolder #{app_path.shellescape} -ov -format UDZO #{dmg_path.shellescape}",
    )
    UI.user_error!("DMG not produced at #{dmg_path}") unless File.exist?(dmg_path)
    UI.success("✅ DMG built: #{dmg_path}")
    dmg_path
  end

  # Attach the built DMG to the GitHub release for the current tag.
  # Falls back to the origin remote when GITHUB_REPOSITORY is unset, so the lane works locally too.
  def _upload_dmg_to_github_release(tag, dmg_path)
    repo = ENV["GITHUB_REPOSITORY"].to_s.strip
    repo = _resolve_origin_repo(DEPLOYMENT_REPO_ROOT) if repo.empty?
    UI.user_error!("Cannot resolve GitHub repo (set GITHUB_REPOSITORY or origin remote)") if repo.empty?

    sh("gh release upload #{tag.shellescape} #{dmg_path.shellescape} --clobber --repo #{repo.shellescape}")
    UI.message("📤 Uploaded #{File.basename(dmg_path)} → #{repo} release #{tag}")
  end

  # Apply GitHub Release `prerelease` + `latest` flags per the direct-distribution
  # promotion ladder. The asset upload (gh release upload) doesn't touch these —
  # they're release-level properties, set via `gh release edit`.
  #   stage=prerelease → prerelease: true,  latest: false  (Stage 1 RC)
  #   stage=beta       → prerelease: false, latest: false  (Stage 2 public beta)
  #   stage=stable     → prerelease: false, latest: true   (Stage 3 production)
  def _set_release_stage_flags(tag, stage)
    repo = ENV["GITHUB_REPOSITORY"].to_s.strip
    repo = _resolve_origin_repo(DEPLOYMENT_REPO_ROOT) if repo.empty?
    return if repo.empty?

    prerelease, latest = case stage
                        when "prerelease" then [true,  false]
                        when "beta"       then [false, false]
                        when "stable"     then [false, true]
                        end

    sh(
      "gh release edit #{tag.shellescape} " \
      "--prerelease=#{prerelease} --latest=#{latest} " \
      "--repo #{repo.shellescape}",
      log: false,
    )
    UI.message("🏷  Release #{tag} → prerelease=#{prerelease}, latest=#{latest}")
  end

  # The nearest annotated tag, or an empty string outside a tagged checkout.
  # Empty rather than raising: a DMG can legitimately be built from an untagged commit for testing.
  def _resolve_git_tag(repo_root)
    sh("git -C #{repo_root.shellescape} describe --tags --abbrev=0 2>/dev/null", log: false).strip
  rescue StandardError
    ""
  end

  # Accepts SSH (git@github.com:owner/repo.git) and HTTPS (https://github.com/owner/repo.git).
  def _resolve_origin_repo(repo_root)
    url = sh("git -C #{repo_root.shellescape} remote get-url origin 2>/dev/null", log: false).strip
    return "" if url.empty?
    url.sub(%r{^git@github\.com:}, "").sub(%r{^https?://github\.com/}, "").sub(/\.git$/, "")
  rescue StandardError
    ""
  end
end
