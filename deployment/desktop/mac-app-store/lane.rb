# deployment/desktop/mac-app-store/lane.rb
#
# macOS PKG → TestFlight / App Store via Fastlane + Gradle.
#
# Signing approach — bypass Compose Desktop's internal signing entirely:
#
#   Compose Desktop 1.11.0's ExternalToolRunner calls /usr/bin/security find-certificate
#   via execOperations.exec() using an ABSOLUTE path. On macOS 15 Sequoia this subprocess
#   returns 0 bytes (stdout + stderr), causing "Could not find certificate" regardless of
#   which keychain is targeted (login.keychain-db, custom, default search list — all fail).
#   ProcessBuilder from Gradle doLast{} and Fastlane sh() both work fine (1770 bytes).
#   The absolute /usr/bin/security path also makes a PATH-shim impossible.
#
#   Solution: run createReleaseDistributable (unsigned app bundle, ProGuard applied),
#   then sign manually in Fastlane sh() context where security works:
#     1. createReleaseDistributable  — unsigned .app, MAC_SIGNING_IDENTITY unset so
#                                      Gradle sees macSigningId = null → sign.set(false)
#     2. embed provisioning profile  — copy .provisionprofile → app/Contents/
#     3. codesign                    — sign .app with Apple Distribution cert
#     4. productbuild --sign         — wrap .app into signed .pkg with installer cert
#
#   Local:  Match (readonly) installs Apple Distribution cert into login.keychain-db.
#           MAC_KEYCHAIN_PATH = login.keychain-db is passed to codesign --keychain.
#   CI:     Composite action (mifos-x-actionhub-publish-macos-on-appstore-testflight-kmp)
#           creates build.keychain, imports both signing + installer certs, runs
#           set-key-partition-list, then calls this lane.  Lane reads the keychain path
#           from ~/Library/Keychains/build.keychain-db and resolves identities from it.
#
# Secrets (all read via config.rb _secret helper — ENV first, secrets/ fallback):
#   secrets/live/apple/appstore/AuthKey.p8          — ASC API key
#   secrets/live/apple/appstore/key_id              — ASC key ID
#   secrets/live/apple/appstore/issuer_id           — ASC issuer ID
#   secrets/live/apple/match/match_ci_key           — SSH key for Match git repo (local only)
#   secrets/live/apple/match/.match_password        — Match encryption password (local only)
#
# Env vars (optional overrides):
#   MAC_APP_IDENTIFIER             — macOS bundle ID (defaults to ForkIdentity::APP_ID)
#   MAC_SIGNING_IDENTITY           — Apple Distribution identity for codesign
#   MAC_KEYCHAIN_PATH              — keychain file path for codesign --keychain
#   MAC_PROVISIONING_PROFILE_PATH  — .provisionprofile path embedded into app bundle
#   MAC_APP_STORE_CONNECT_APP_ID   — Numeric ASC app ID for the macOS app

require_relative "../../_shared/lib/appstore_helpers"

MAC_APP_STORE_METADATA_PATH    = File.join(DEPLOYMENT_REPO_ROOT, "deployment/desktop/mac-app-store/metadata").freeze
MAC_APP_STORE_SCREENSHOTS_PATH = File.join(DEPLOYMENT_REPO_ROOT, "deployment/desktop/mac-app-store/metadata/screenshots").freeze
MAC_APP_STORE_PRIMARY_LOCALE   = "en-GB".freeze

platform :mac do
  desc "Build and upload macOS desktop build to TestFlight (Mac App Store track)"
  lane :desktop_testflight do |options|
    options       = sanitize_options(options)
    mac_bundle_id = ENV["MAC_APP_IDENTIFIER"] || ENV["MAC_BUNDLE_ID"] || ForkIdentity::APP_ID

    load_api_key(options)

    # Auto-sync + verify ASC store config before the build (see config.rb) — fail fast
    # on a missing app record + create/update TestFlight Test Information so a later
    # Mac external-beta promotion never hits `betaAppLocalizations not found`.
    ensure_testflight_store_config(app_identifier: mac_bundle_id)
    # Same account-level TestFlight groups as iOS (from fork.properties apple.testers.*): internal (team,
    # every build) + external (public-link self-join). macOS shares the ASC app + tester model with iOS.
    sync_testflight_testers(app_identifier: mac_bundle_id)

    with_ios_preamble(options)
    setup_mac_signing_keychain(options, mac_bundle_id)

    pkg_path = build_mac_pkg(mac_bundle_id, options)

    upload_to_testflight(
      pkg:                               pkg_path,
      api_key:                           Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      skip_waiting_for_build_processing: true,
      apple_id:                          ENV["MAC_APP_STORE_CONNECT_APP_ID"] || options[:apple_id],
    )

    UI.success("✅ macOS PKG uploaded to TestFlight — processing in progress.")
  ensure
    # Local: we use login.keychain-db directly (no custom keychain created), so no cleanup needed.
    # CI:    composite action owns its own keychain lifecycle.
  end

  desc "One-shot: create proper Mac Installer Distribution cert in Apple Developer Portal + push to Match repo."
  lane :create_mac_installer_cert do |options|
    options = sanitize_options(options)
    mac_bundle_id = ENV["MAC_APP_IDENTIFIER"] || ENV["MAC_BUNDLE_ID"] || ForkIdentity::APP_ID
    load_api_key(options)
    cfg     = FastlaneConfig::IosConfig::BUILD_CONFIG
    ssh_key = File.join(DEPLOYMENT_REPO_ROOT, cfg[:match_ssh_key_path])
    match_pass = options[:match_password] || ENV["MATCH_PASSWORD"] || cfg[:match_password]
    ENV["MATCH_PASSWORD"] = match_pass.to_s if match_pass && ENV["MATCH_PASSWORD"].to_s.empty?
    match(
      type:            "mac_installer_distribution",
      platform:        "macos",
      app_identifier:  mac_bundle_id,
      git_url:         options[:match_git_url]    || cfg[:match_git_url],
      git_branch:      options[:match_git_branch] || cfg[:match_git_branch],
      git_private_key: File.exist?(ssh_key) ? ssh_key : nil,
      api_key:         Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      readonly:        false,
    )
    UI.success("✅ Mac Installer Distribution cert created and pushed to Match repo.")
  end

  desc "Stage 1 → Stage 2 promotion: distribute an already-uploaded Mac TF build to external testers (no rebuild). Triggers Apple's beta review (~24h)."
  lane :promoteMacToExternalBeta do |options|
    options       = sanitize_options(options)
    mac_bundle_id = ENV["MAC_APP_IDENTIFIER"] || ENV["MAC_BUNDLE_ID"] || ForkIdentity::APP_ID

    load_api_key(options)

    # External beta review requires app-level Test Information — sync from config first.
    ensure_testflight_store_config(app_identifier: mac_bundle_id)

    build_number = options[:build_number]&.to_s || latest_tf_build_number_resilient(
      app_identifier: mac_bundle_id,
      api_key:        Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      platform:       "osx",
    ).to_s

    external_groups = options[:groups] || ["External Beta"]

    UI.important("📦 Promoting Mac TF build #{build_number} → external testers (#{external_groups.join(', ')})")

    pilot(
      api_key:                              Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      app_platform:                         "osx",
      app_identifier:                       mac_bundle_id,
      build_number:                         build_number,
      distribute_only:                      true,
      distribute_external:                  true,
      notify_external_testers:              true,
      groups:                               external_groups,
      submit_beta_review:                   true,
      reject_build_waiting_for_review:      true,
      changelog:                            options[:changelog] || generateReleaseNote(),
    )

    UI.success("✅ Mac build #{build_number} submitted for beta review — external testers receive it on approval (~24h).")
  end

  desc "Sync Mac App Store listing (metadata + screenshots) — no binary upload, no submission (parity with ios upload_ios_screenshots)."
  lane :syncMacListing do |options|
    options       = sanitize_options(options)
    mac_bundle_id = ENV["MAC_APP_IDENTIFIER"] || ENV["MAC_BUNDLE_ID"] || ForkIdentity::APP_ID
    load_api_key(options)

    # deliver only runs under an officially-supported platform (:ios/:mac/:android) — this lane is under
    # platform :mac (the desktop syncListing lane's :desktop context makes deliver abort "not supported").
    # A LISTING sync must NOT push app-level identity (name/subtitle) — App Store names are globally
    # unique + set once; re-pushing a taken name aborts "app name already used on a different account"
    # (same class as the iOS promoteToAppStore heal). Hide name/subtitle so only version-level content
    # (description / keywords / screenshots) syncs.
    AppStoreHelpers.without_app_identity_metadata(MAC_APP_STORE_METADATA_PATH) do
      deliver(
        platform:                             "osx",
        api_key:                              Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
        app_identifier:                       mac_bundle_id,
        metadata_path:                        MAC_APP_STORE_METADATA_PATH,
        screenshots_path:                     MAC_APP_STORE_SCREENSHOTS_PATH,
        skip_binary_upload:                   true,
        # NOTE: deliver has no `skip_submission` (that is a pilot param). Not submitting == leaving
        # submit_for_review at its false default. Passing skip_submission aborts "invalid parameters".
        submit_for_review:                    false,
        skip_metadata:                        false,
        skip_screenshots:                     false,
        overwrite_screenshots:                true,
        skip_app_version_update:              true,
        ignore_language_directory_validation: true,
        run_precheck_before_submit:           false,
        force:                                true,
      )
    end

    record_store_listing_synced("mac", MAC_APP_STORE_METADATA_PATH)
    UI.success("✅ Mac App Store listing synced (metadata + screenshots — no binary, no submission)")
  end

  desc "Promote an existing Mac TestFlight build to Mac App Store review — no rebuild, no re-upload."
  lane :promoteMacToAppStore do |options|
    options       = sanitize_options(options)
    mac_bundle_id = ENV["MAC_APP_IDENTIFIER"] || ENV["MAC_BUNDLE_ID"] || ForkIdentity::APP_ID

    load_api_key(options)

    if options[:build_number]
      build_number = options[:build_number].to_s
      app_version  = options[:app_version]
      UI.important("📦 Using provided build #{build_number}")
    else
      build_number = latest_tf_build_number_resilient(
        app_identifier: mac_bundle_id,
        api_key:        Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
        platform:       "osx",
      ).to_s
      app_version = Actions.lane_context[SharedValues::LATEST_TESTFLIGHT_VERSION]
      UI.important("📦 Latest Mac TestFlight build: #{build_number}  (#{app_version})")
    end

    UI.message("🚀 Submitting Mac build #{build_number} for App Store review...")
    UI.message("   automatic_release: true  (goes live on approval — no manual step)")

    # Drift-checked listing sync (parity with Android/iOS): re-upload metadata + screenshots only when
    # the app-profile-derived Mac listing changed since the last push. (RULE-DEPLOY-LISTING-SYNC-ALL-STATES-001)
    mac_listing_changed = store_listing_needs_sync?("mac", MAC_APP_STORE_METADATA_PATH)
    UI.message(mac_listing_changed ? "🔄 Mac App Store listing changed — will upload metadata + screenshots" : "✓ Mac App Store listing unchanged — skipping metadata re-upload")

    # A PROMOTE must never push app-level identity (name/subtitle) — the globally-unique app name aborts
    # deliver ("already used on a different account"). Hide it so only version-level listing syncs (parity
    # with the iOS promoteToAppStore lane + syncMacListing). (2026-08-10)
    AppStoreHelpers.without_app_identity_metadata(MAC_APP_STORE_METADATA_PATH) do
      deliver(
        platform:                             "osx",
        api_key:                              Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
        app_identifier:                       mac_bundle_id,
        app_version:                          app_version,
        build_number:                         build_number,
        skip_binary_upload:                   true,
        skip_metadata:                        !mac_listing_changed,
        skip_screenshots:                     !mac_listing_changed,
        metadata_path:                        MAC_APP_STORE_METADATA_PATH,
        screenshots_path:                     MAC_APP_STORE_SCREENSHOTS_PATH,
        overwrite_screenshots:                true,
        ignore_language_directory_validation: true,
        skip_app_version_update:              true,
        submit_for_review:                    true,
        # Export-compliance + IDFA answers — required to submit a never-submitted Mac version, else deliver
        # aborts "Export compliance is required to submit". Shared with iOS (one ASC app record).
        submission_information:               FastlaneConfig::IosConfig::APPSTORE_CONFIG[:submission_information],
        automatic_release:                    true,
        phased_release:                       false,
        reject_if_possible:                   true,
        run_precheck_before_submit:           false,
        force:                                true,
      )
    end

    record_store_listing_synced("mac", MAC_APP_STORE_METADATA_PATH) if mac_listing_changed

    UI.success("✅ Mac build #{build_number} submitted for App Store review — will auto-release on approval.")
  end

  desc "Full Mac App Store release — build PKG from source + deliver (use promoteMacToAppStore when a TestFlight build already exists)"
  lane :desktop_release do |options|
    options       = sanitize_options(options)
    mac_bundle_id = ENV["MAC_APP_IDENTIFIER"] || ENV["MAC_BUNDLE_ID"] || ForkIdentity::APP_ID

    load_api_key(options)

    # Auto-sync + verify ASC store config before the build (see config.rb) — fail fast
    # on a missing app record + create/update TestFlight Test Information so a later
    # Mac external-beta promotion never hits `betaAppLocalizations not found`.
    ensure_testflight_store_config(app_identifier: mac_bundle_id)
    # Same account-level TestFlight groups as iOS (from fork.properties apple.testers.*): internal (team,
    # every build) + external (public-link self-join). macOS shares the ASC app + tester model with iOS.
    sync_testflight_testers(app_identifier: mac_bundle_id)

    with_ios_preamble(options)
    setup_mac_signing_keychain(options, mac_bundle_id)

    pkg_path = build_mac_pkg(mac_bundle_id, options)

    deliver(
      pkg:               pkg_path,
      api_key:           Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      submit_for_review: options.fetch(:submit_for_review, false),
      automatic_release: options.fetch(:automatic_release, false),
      force:             true,
      skip_screenshots:  true,
      skip_metadata:     true,
    )
  ensure
    # Local: login.keychain-db used directly — no custom keychain to delete.
    # CI:    composite action owns cleanup.
  end

  # ── Helpers ────────────────────────────────────────────────────────────────

  # Set up signing context and populate:
  #   ENV["MAC_SIGNING_IDENTITY"]          — Apple Distribution identity for codesign
  #   ENV["MAC_KEYCHAIN_PATH"]             — keychain path passed to codesign --keychain
  #   ENV["MAC_PROVISIONING_PROFILE_PATH"] — .provisionprofile embedded into the app bundle
  #
  # Local: Match installs cert into login.keychain-db; MAC_KEYCHAIN_PATH points there.
  # CI:    composite action owns the keychain; lane reads build.keychain path + identities.
  def setup_mac_signing_keychain(options, mac_bundle_id)
    if ENV["CI"].to_s != ""
      _setup_mac_signing_keychain_ci(options, mac_bundle_id)
    else
      _setup_mac_signing_keychain_local(options, mac_bundle_id)
    end
  end

  # Build a throwaway keychain on CI from Match-managed certificates.
  # macOS certs live in the same Match repo as iOS, so CI needs only MATCH_PASSWORD and the SSH
  # key — never a .p12 pasted into a repository secret.
  def _setup_mac_signing_keychain_ci(options, mac_bundle_id)
    # macOS certs are managed by Fastlane Match (openMF/ios-provisioning-profile,
    # OpenSSL-encrypted) — exactly like iOS. The tier-3 composite action only
    # provides MATCH_PASSWORD + MATCH_GIT_PRIVATE_KEY (+ the ASC API key) and runs
    # this lane; there is NO manual .p12 import (the MAC_*_CERTIFICATE_B64 secrets
    # were not plain p12s — they are Match-encrypted, which `security import` rejects
    # as "Unknown format").
    #
    # CI MUST use a DEDICATED keychain with a KNOWN password — not login.keychain-db.
    # Match (readonly) imports the private key but can only run
    # `security set-key-partition-list` (which grants codesign non-interactive access)
    # when it owns the keychain password. Without that grant, codesign blocks FOREVER
    # on a headless UI ACL prompt → the 6-hour timeout observed in run 29113565899
    # (build + Match + provisioning all succeeded; codesign hung on "replacing existing
    # signature"). A dedicated keychain closes that.
    kc_name     = "signing_temp.keychain-db"
    kc_password = ENV["MAC_KEYCHAIN_TEMP_PASSWORD"].to_s.empty? ? "mbl-ci-mac-signing" : ENV["MAC_KEYCHAIN_TEMP_PASSWORD"]
    kc_path     = File.expand_path("~/Library/Keychains/#{kc_name}")

    create_keychain(
      name:             kc_name,
      password:         kc_password,
      default_keychain: true,
      unlock:           true,
      timeout:          21_600,   # 6h — outlive the whole build; never auto-lock mid-codesign
      lock_when_sleeps: false,
      add_to_search_list: true,
    )
    UI.message("🔐 Created dedicated CI signing keychain: #{kc_path}")

    _setup_mac_signing_keychain_local(
      options, mac_bundle_id,
      keychain_path:     kc_path,
      keychain_name:     kc_name,
      keychain_password: kc_password,
    )

    # Belt-and-braces: Match already grants `apple-tool:,apple:` when it owns the
    # keychain password; explicitly add `codesign:` + `productsign:` so BOTH the .app
    # codesign and the installer productsign are guaranteed non-interactive (a missing
    # grant is what hung run 29113565899 for 6h). Idempotent; never fails the lane.
    sh(
      "security set-key-partition-list -S apple-tool:,apple:,codesign:,productsign: " \
      "-s -k #{kc_password.shellescape} #{kc_path.shellescape} >/dev/null 2>&1 || true",
      log: false,
    )
    UI.message("🔓 Granted codesign/productsign partition access on #{kc_name}")
  end

  # Fetch Match certificates into a TEMPORARY keychain for a local build.
  # The login keychain is never touched, which is what stops a failed build from leaving the
  # developer's default keychain altered.
  def _setup_mac_signing_keychain_local(options, mac_bundle_id, keychain_path: nil, keychain_name: nil, keychain_password: nil)
    cfg        = FastlaneConfig::IosConfig::BUILD_CONFIG
    ssh_key    = File.join(DEPLOYMENT_REPO_ROOT, cfg[:match_ssh_key_path])
    match_pass = options[:match_password] || ENV["MATCH_PASSWORD"] || cfg[:match_password]

    ENV["MATCH_PASSWORD"] = match_pass.to_s if match_pass && ENV["MATCH_PASSWORD"].to_s.empty?

    # CI passes a dedicated keychain (with a known password so Match sets the
    # key-partition-list); local dev falls back to the unlocked login.keychain-db.
    login_kc = keychain_path || File.expand_path("~/Library/Keychains/login.keychain-db")
    UI.user_error!("signing keychain not found at #{login_kc}") unless File.exist?(login_kc)

    # ── Step 0: Remove duplicate Apple Distribution certs ──────────────────────────
    # Match re-installs its cert on every run, causing "ambiguous" errors when a
    # stale copy of the same-named cert remains. Wipe all copies before Match so
    # exactly one cert exists after the install.
    _remove_duplicate_distribution_certs(login_kc)

    match_base = {
      app_identifier:  mac_bundle_id,
      git_url:         options[:match_git_url]    || cfg[:match_git_url],
      git_branch:      options[:match_git_branch] || cfg[:match_git_branch],
      git_private_key: File.exist?(ssh_key) ? ssh_key : nil,
      api_key:         Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      readonly:        true,
    }

    # When a dedicated keychain is supplied (CI), install the cert THERE and let Match
    # run `security set-key-partition-list` (it can, because it owns the password) so
    # codesign later signs the .app non-interactively. Omitted for local dev → Match
    # installs into the already-unlocked login.keychain-db.
    if keychain_name
      match_base[:keychain_name]     = keychain_name
      match_base[:keychain_password] = keychain_password
    end

    # ── Step 1a: Match appstore — installs Apple Distribution cert + provisioning profile ─
    match(**match_base.merge(type: "appstore", platform: "macos"))

    # ── Step 1a.5: resolve the keychain that ACTUALLY holds the identity ─────────────
    # `setup_ci` (via with_ios_preamble/setup_ios_keychain) can install Match's cert into
    # `fastlane_tmp_keychain-db` instead of login.keychain-db — then a login-keychain-only lookup
    # returns EMPTY even though the cert is in the search list (2026-08-06 M1 false-abort). Walk the
    # keychain search list and use whichever one holds a VALID Apple Distribution identity for BOTH the
    # SHA-1 lookup and codesign's --keychain. setup_ci already unlocked + set key-partition-list there.
    search_kcs = [login_kc, *(`security list-keychains 2>/dev/null`.scan(/"([^"]+)"/).flatten)].uniq
    signing_kc = search_kcs.find do |kc|
      File.exist?(kc) &&
        !`security find-identity -v -p codesigning #{kc.shellescape} 2>/dev/null`
          .scan(/Apple Distribution|3rd Party Mac Developer Application/).empty?
    end
    if signing_kc && signing_kc != login_kc
      UI.important("🔎 Apple Distribution identity is in #{File.basename(signing_kc)} (not login.keychain) — using it for signing")
      login_kc = signing_kc
    end

    # ── Step 2: Set MAC_KEYCHAIN_PATH immediately after appstore Match ────────────
    ENV["MAC_KEYCHAIN_PATH"] = login_kc
    UI.message("🔐 Keychain: #{login_kc}")

    # Resolve provisioning profile now (while MATCH_PROVISIONING_PROFILE_MAPPING
    # reflects only the appstore Match result, not the installer cert Match below).
    _resolve_mac_provisioning_profile(mac_bundle_id)

    # ── Step 3: Capture app-signing SHA1 before installer Match muddies keychain ──
    # Use SHA1 over display name to avoid "ambiguous" codesign errors.
    # SHA1 via `find-identity -v -p codesigning` (lists ONLY valid, non-expired signing identities as
    # `N) <SHA1> "name"`). Robust against the cert+p12 double-import that leaves duplicate keychain entries:
    # the old `find-certificate | grep 'SHA-1 hash:' | tail -1 | awk` returned EMPTY on that duplicate.
    app_sha1 = sh(
      "security find-identity -v -p codesigning #{login_kc.shellescape} 2>/dev/null" \
      " | grep -E 'Apple Distribution|3rd Party Mac Developer Application' | head -1 | awk '{print $2}'",
      log: false,
    ).strip
    if app_sha1.empty?
      app_sha1 = sh(
        "security find-certificate -c 'Apple Distribution' -a -Z #{login_kc.shellescape} 2>/dev/null" \
        " | awk '/SHA-1 hash:/{print $3; exit}'",
        log: false,
      ).strip
    end
    UI.user_error!("No Apple Distribution signing identity found in the keychain search list (checked find-identity + find-certificate across #{search_kcs.map { |k| File.basename(k) }.join(', ')})") if app_sha1.empty?
    ENV["MAC_SIGNING_IDENTITY"] = app_sha1
    UI.message("🔏 App signing SHA1: #{app_sha1}")

    # Capture the Apple Team ID from the app-signing identity name ("… (TEAMID)"). A machine that builds
    # multiple orgs carries several teams' installer certs; the installer cert MUST match THIS app's team.
    team_id = `security find-identity -v -p codesigning 2>/dev/null`.lines
              .grep(/#{Regexp.escape(app_sha1)}/).first.to_s[/\(([A-Z0-9]{10})\)/, 1]
    UI.message("🏷  App signing team: #{team_id || 'unknown'}")

    # ── Step 1b: Match mac_installer_distribution — installs installer cert ────────
    # Snapshot SHA1s before so we can diff to find the exact installer cert SHA1,
    # regardless of whether it is named "3rd Party Mac Developer Installer" or
    # "Apple Distribution" in the keychain.
    sha1s_before_installer = _list_identity_sha1s(login_kc)
    # skip_provisioning_profiles: installer certs sign the .pkg via `productbuild` and need NO provisioning
    # profile (ios-provisioning-profile CLAUDE.md gotcha #4). Without this, Match tries to fetch a
    # non-existent `Unknown_<bundle>.provisionprofile` and (readonly) aborts "No matching provisioning
    # profiles found … cannot create because readonly" (2026-08-06 M1 Step-1b).
    match(**match_base.merge(type: "mac_installer_distribution", platform: "macos", skip_provisioning_profiles: true))
    sha1s_after_installer  = _list_identity_sha1s(login_kc)
    new_installer_sha1s    = sha1s_after_installer - sha1s_before_installer

    # ── Step 4: Store installer identity — TEAM-MATCHED across the full search list ────────────────
    # A machine that builds multiple orgs carries several teams' installer certs; picking the wrong-team
    # one → Apple rejects the PKG (90237). Match the app's team; search the whole list (the cert may be in
    # fastlane_tmp_keychain or login.keychain). Installer certs are `-p basic`, NOT `-p codesigning`.
    installer_lines = search_kcs.flat_map do |kc|
      `security find-identity -v -p basic #{kc.shellescape} 2>/dev/null`.lines.grep(/Installer/i)
    end
    chosen_installer = installer_lines.find { |l| team_id && l.include?("(#{team_id})") }&.[](/"([^"]+)"/, 1)
    if chosen_installer
      ENV["MAC_INSTALLER_IDENTITY"] = chosen_installer
      UI.message("📦 Installer (team #{team_id}): #{chosen_installer}")
    elsif new_installer_sha1s.any?
      ENV["MAC_INSTALLER_IDENTITY"] = new_installer_sha1s.first
      UI.message("📦 Installer SHA1 (newly installed): #{new_installer_sha1s.first}")
    else
      first = installer_lines.first&.[](/"([^"]+)"/, 1)
      if first
        UI.important("⚠️  No team-#{team_id} installer cert found; falling back to #{first}")
        ENV["MAC_INSTALLER_IDENTITY"] = first
      else
        UI.user_error!("No '3rd Party Mac Developer Installer' cert for team #{team_id} in the keychain search list — the PKG cannot be signed. Fix the provisioning repo (cert-renewal.sh).")
      end
    end
  end

  # The SHA-1 of every signing identity in the given keychain.
  # Used to assert the expected identity actually landed, rather than trusting the import's exit code.
  def _list_identity_sha1s(keychain_path)
    sh(
      "security find-identity -v -p basic #{keychain_path.shellescape} 2>/dev/null" \
      " | grep -oE '[0-9A-F]{40}'",
      log: false,
    ).strip.split
  end

  # Remove all copies of "Apple Distribution" certs from the given keychain.
  # Prevents "ambiguous" codesign errors when Match re-installs the cert on
  # subsequent runs. Safe: Match will immediately reinstall from the repo.
  def _remove_duplicate_distribution_certs(keychain_path)
    sha1s = sh(
      "security find-certificate -c 'Apple Distribution' -a -Z #{keychain_path.shellescape} 2>/dev/null" \
      " | grep 'SHA-1 hash:' | awk '{print $3}'",
      log: false,
    ).strip.split
    return if sha1s.empty?

    UI.message("🧹 Removing #{sha1s.size} Apple Distribution cert(s) before Match install...")
    sha1s.each do |sha1|
      sh(
        "security delete-certificate -Z #{sha1} #{keychain_path.shellescape} 2>/dev/null || true",
        log: false,
      )
    end
  end

  # Resolve the provisioning profile that Match installed and set MAC_PROVISIONING_PROFILE_PATH.
  # macOS .provisionprofile files land in ~/Library/Developer/Xcode/UserData/Provisioning Profiles/
  # (not ~/Library/MobileDevice/Provisioning Profiles/ which is iOS-only).
  def _resolve_mac_provisioning_profile(mac_bundle_id)
    return unless ENV["MAC_PROVISIONING_PROFILE_PATH"].to_s.strip.empty?

    xcode_profiles_dir  = File.expand_path("~/Library/Developer/Xcode/UserData/Provisioning Profiles")
    mobile_profiles_dir = File.expand_path("~/Library/MobileDevice/Provisioning Profiles")

    # 1. From Match's lane context (most reliable — UUID direct lookup).
    profile_mapping = Actions.lane_context[SharedValues::MATCH_PROVISIONING_PROFILE_MAPPING]
    if profile_mapping&.any?
      profile_uuid = profile_mapping.values.first
      [xcode_profiles_dir, mobile_profiles_dir].each do |dir|
        candidate = File.join(dir, "#{profile_uuid}.provisionprofile")
        if File.exist?(candidate)
          ENV["MAC_PROVISIONING_PROFILE_PATH"] = candidate
          UI.message("🗂  Profile (Match context): #{candidate}")
          return
        end
      end
    end

    # 2. Newest .provisionprofile across both macOS profile directories.
    all_profiles = Dir[File.join(xcode_profiles_dir, "*.provisionprofile")] +
                   Dir[File.join(mobile_profiles_dir, "*.provisionprofile")]
    if all_profiles.any?
      latest = all_profiles.max_by { |f| File.mtime(f) }
      ENV["MAC_PROVISIONING_PROFILE_PATH"] = latest
      UI.message("🗂  Profile (newest): #{latest}")
      return
    end

    UI.important("⚠️  No .provisionprofile found — Mac App Store upload will fail without one.")
  end

  # Build the signed .pkg for Mac App Store submission via the Gradle packaging task.
  def build_mac_pkg(mac_bundle_id, options = {})
    repo_root    = DEPLOYMENT_REPO_ROOT
    gradlew      = File.join(repo_root, "gradlew")
    identity     = ENV["MAC_SIGNING_IDENTITY"].to_s
    keychain     = ENV["MAC_KEYCHAIN_PATH"].to_s
    profile_path = ENV["MAC_PROVISIONING_PROFILE_PATH"].to_s
    entitlements = File.join(repo_root, "cmp-desktop/mac-app-store.entitlements")

    UI.user_error!("MAC_SIGNING_IDENTITY is not set") if identity.strip.empty?
    UI.message("🔏 Identity:  #{identity}")
    UI.message("🔐 Keychain:  #{keychain.empty? ? '(default search list)' : keychain}")
    UI.message("🗂  Profile:   #{profile_path.empty? ? '(none)' : profile_path}")

    # ── Step 1: Build unsigned .app bundle ────────────────────────────────────────
    # Bypass Compose Desktop's internal signing: ExternalToolRunner calls
    # /usr/bin/security find-certificate via execOperations.exec() with an absolute
    # path — returns 0 bytes on macOS 15 Sequoia regardless of keychain target.
    # Fastlane sh() works fine; we sign manually below.
    #
    # Temporarily unset signing env vars so Gradle sees macSigningId = null
    # → sign.set(false) in build.gradle.kts — no certificate lookup attempted.
    saved_env = {}
    %w[MAC_SIGNING_IDENTITY MAC_KEYCHAIN_PATH MAC_PROVISIONING_PROFILE_PATH].each { |k| saved_env[k] = ENV.delete(k) }

    # Thread the active flavor into the Compose Desktop build. cmp-desktop/build.gradle.kts
    # resolves `findProperty("kmpFlavor")` → falls back to the DSL default (demo) when
    # absent. Without -PkmpFlavor a prod-requested release silently builds the DEMO
    # variant (bundle org.mifos.kmp.template.demo) → upload_to_testflight can't find the
    # app on App Store Connect AND it misaligns with the prod Match provisioning profile.
    flavor = (options[:flavor] || ENV["FLAVOR"]).to_s.strip

    # CFBundleVersion must STRICTLY increase per TestFlight/App Store upload. Compose Desktop leaves it
    # unset → build.gradle.kts falls back to the static packageVersion ("1.0.0") UNLESS -PmacBuildVersion
    # is passed. Only CI set it (via GITHUB_RUN_NUMBER); a LOCAL /idea-deploy set neither, so every mac
    # upload 409'd ("bundle version must be higher than … '1.0.0'"). Derive a monotonic build number from
    # the git commit count (same basis as Android's versionCode) so local uploads are unique + increasing.
    # Override with MAC_BUILD_VERSION when a specific value is needed. (2026-08-10)
    require "shellwords"
    mac_build_version = ENV["MAC_BUILD_VERSION"].to_s.strip
    if mac_build_version.empty?
      count = `git -C #{repo_root.shellescape} rev-list --count HEAD 2>/dev/null`.strip
      mac_build_version = "1.0.#{count}" unless count.empty?
    end

    gradle_args = [gradlew, "-p", repo_root, ":cmp-desktop:createReleaseDistributable",
                   "--no-daemon", "--no-configuration-cache"]
    gradle_args << "-PkmpFlavor=#{flavor}" unless flavor.empty?
    unless mac_build_version.to_s.empty?
      gradle_args << "-PmacBuildVersion=#{mac_build_version}"
      UI.message("🔢 macOS CFBundleVersion → #{mac_build_version} (monotonic, from git commit count) — avoids the '1.0.0' upload 409")
    end

    begin
      UI.message("Building unsigned .app bundle (createReleaseDistributable, flavor=#{flavor.empty? ? '(default)' : flavor})...")
      sh(*gradle_args)
    ensure
      saved_env.each { |k, v| ENV[k] = v if v }
    end

    # ── Step 2: Locate .app bundle ────────────────────────────────────────────────
    app_dir  = File.join(repo_root, "cmp-desktop/build/compose/binaries/main-release/app")
    app_path = Dir[File.join(app_dir, "*.app")].first
    UI.user_error!("No .app bundle found in #{app_dir}") unless app_path && File.exist?(app_path)
    UI.message("App bundle: #{app_path}")

    # ── Step 2.5: Patch LSMinimumSystemVersion for arm64-only bundles ─────────────
    # Apple rejects arm64-only apps whose deployment target is < 12.0.
    # The bundled JVM runtime sets this to 10.13 by default; override to 12.0.
    info_plist = File.join(app_path, "Contents", "Info.plist")
    if File.exist?(info_plist)
      sh("/usr/libexec/PlistBuddy -c 'Set :LSMinimumSystemVersion 12.0' #{info_plist.shellescape}",
         log: false)
      UI.message("📋 Set LSMinimumSystemVersion → 12.0")
    end

    # ── Step 3: Embed provisioning profile ────────────────────────────────────────
    unless profile_path.strip.empty?
      embedded = File.join(app_path, "Contents", "embedded.provisionprofile")
      FileUtils.cp(profile_path, embedded)
      UI.message("Embedded provisioning profile → #{embedded}")
    else
      UI.important("⚠️  No provisioning profile — Mac App Store upload will fail.")
    end

    # ── Step 4: INSIDE-OUT codesign (App Store distribution) ─────────────────────
    # `codesign --deep` does NOT reliably re-sign the bundled JVM runtime + skiko
    # dylibs with the submitting team's identity — they keep their upstream
    # (JetBrains/JDK) signature, so App Store processing rejects the build with
    # ITMS-90238 "code failed to satisfy specified code requirement(s)" on every
    # nested *.dylib. Apple requires each nested Mach-O to be signed individually,
    # DEEPEST-FIRST, then the container, so each seal includes its children.
    keychain_arg = keychain.strip.empty? ? "" : "--keychain #{keychain.shellescape}"
    jvm_helper_entitlements = File.join(repo_root, "cmp-desktop/jvm-helper.entitlements")
    ent_for_helpers = File.exist?(jvm_helper_entitlements) ? "--entitlements #{jvm_helper_entitlements.shellescape}" : ""

    # 4a. Every nested Mach-O LIBRARY (.dylib/.so/.jnilib) — libraries carry no
    #     entitlements. `--timestamp` is required for App Store distribution.
    libs = Dir.glob("#{app_path}/Contents/**/*.{dylib,so,jnilib}").select { |f| File.file?(f) }
    # 4b. JVM runtime EXECUTABLES (java, jspawnhelper, keytool …) — Mach-O binaries
    #     with the executable bit that are not libraries; sign with inherit+sandbox.
    jvm_runtime = File.join(app_path, "Contents", "runtime")
    execs = Dir.glob("#{jvm_runtime}/**/*").select do |f|
      File.file?(f) && File.executable?(f) && !f.end_with?(".dylib", ".so", ".jnilib")
    end

    # Deepest-first so a parent's seal is computed over already-signed children.
    libs.sort_by { |f| -f.count("/") }.each do |lib|
      sh("codesign --force --timestamp --sign #{identity.shellescape} #{keychain_arg} #{lib.shellescape}", log: false)
    end
    execs.sort_by { |f| -f.count("/") }.each do |helper|
      sh("codesign --force --timestamp --sign #{identity.shellescape} #{keychain_arg} #{ent_for_helpers} #{helper.shellescape}", log: false)
    end
    UI.message("🔏 Inside-out signed #{libs.size} nested libraries + #{execs.size} runtime executables")

    # 4c. Sign the main app bundle LAST — App Store entitlements + embedded profile.
    #     No --deep: the nested code is already signed above.
    sh(
      "codesign --force --timestamp --sign #{identity.shellescape} #{keychain_arg} " \
      "--entitlements #{entitlements.shellescape} #{app_path.shellescape}",
    )

    # Verify no nested binary is still signed by a foreign identity.
    sh("codesign --verify --deep --strict --verbose=2 #{app_path.shellescape}")
    UI.success("✅ App bundle signed (inside-out, App Store distribution)")

    # ── Step 5: Create PKG with productbuild ─────────────────────────────────────
    # productbuild uses the system keychain search list (no --keychain option).
    # Look for "Mac Installer Distribution" / "3rd Party Mac Developer Installer" cert.
    # If none found locally (Match only installs the App Distribution cert; the
    # installer cert comes from secrets on CI), build an unsigned PKG with a warning.
    # On CI the composite action imports both signing + installer certs so this always
    # finds an installer identity there.
    keychain_find_arg = keychain.strip.empty? ? "" : keychain.shellescape
    # Prefer MAC_INSTALLER_IDENTITY set by _setup_mac_signing_keychain_local (SHA1 diff)
    installer_identity = ENV["MAC_INSTALLER_IDENTITY"].to_s.strip
    if installer_identity.empty?
      installer_identity = sh(
        "security find-identity -v -p basic #{keychain_find_arg} 2>/dev/null" \
        " | grep -iE 'installer|3rd party mac' | head -1 | sed 's/.*\"\\(.*\\)\".*/\\1/'",
        log: false,
      ).strip
    end

    pkg_dir  = File.join(repo_root, "cmp-desktop/build/compose/binaries/main-release/pkg")
    FileUtils.mkdir_p(pkg_dir)
    app_name = File.basename(app_path, ".app")
    pkg_path = File.join(pkg_dir, "#{app_name}.pkg")

    if installer_identity.empty?
      UI.important(
        "⚠️  No Mac Installer Distribution cert found in keychain.\n" \
        "     Creating UNSIGNED PKG (local dev only — cannot be uploaded to TestFlight).\n" \
        "     On CI, the composite action provides the installer cert via secrets.",
      )
      sh(
        "productbuild --component #{app_path.shellescape} /Applications #{pkg_path.shellescape}",
      )
    else
      UI.message("🔐 Installer identity: #{installer_identity}")
      sh(
        "productbuild --sign #{installer_identity.shellescape} " \
        "--component #{app_path.shellescape} /Applications #{pkg_path.shellescape}",
      )
    end

    UI.user_error!("PKG not found at #{pkg_path} after productbuild") unless File.exist?(pkg_path)
    UI.success("Built PKG: #{pkg_path}")
    pkg_path
  end
end
