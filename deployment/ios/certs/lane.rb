# deployment/ios/certs/lane.rb
# Certificate renewal lanes — imported by fastlane/Fastfile via Dir[].
#
# WHY THIS EXISTS
# Apple Distribution certificates expire after exactly 1 year. Fastlane Match's
# built-in `force` parameter only re-downloads profiles, not certificates — it does
# NOT bypass the "cert not valid" error on an expired cert. The only reliable fix is
# to purge the expired cert files from the Match git repo so Match's next run finds
# an empty certs/distribution/ directory and creates a fresh certificate via the
# App Store Connect API.
#
# LANES
#   renewCerts   — purge expired certs from Match repo, then create fresh adhoc +
#                  appstore certs. Run manually or via the monthly CI schedule.
#
# WHEN TO RUN
#   • Immediately when any iOS lane (e.g. `beta` / `release`) fails with:
#       "Your certificate '<ID>.cer' is not valid, please check end date"
#   • Proactively via monthly GitHub Actions schedule (cert-renewal.yml (openMF/ios-provisioning-profile))
#
# SIGNING PASSWORDS (resolved in priority order)
#   KEYCHAIN_PASSWORD     — unlocks macOS login keychain (local) / creates CI keychain
#   MATCH_PASSWORD        — decrypts git-stored certs in the Match repo
#   CERTIFICATES_PASSWORD — p12 import password (Match uses MATCH_PASSWORD by default)

platform :ios do
  desc "Renew expired iOS Distribution certificate: revoke from Apple portal + create fresh adhoc + appstore certs"
  lane :renewCerts do |options|
    options = sanitize_options(options)

    cfg     = FastlaneConfig::IosConfig::BUILD_CONFIG
    ssh_key = File.join(DEPLOYMENT_REPO_ROOT, cfg[:match_ssh_key_path])

    # SSH agent + keychain unlock + ASC API key must come before Match/match_nuke.
    with_ios_preamble(options)
    setup_ios_keychain(keychain_password: options[:keychain_password] || ENV["KEYCHAIN_PASSWORD"])
    load_api_key(options)

    # Step 1a: Revoke expired certs from Apple portal via ASC API (no Apple ID needed).
    # min_free_slots: 2 — the legacy `cert` action (Spaceship portal API) consistently
    # overcounts by 1 vs ConnectAPI, so we need ConnectAPI at ≤1/3 (2 free) for `cert`
    # to see 2 free slots and allow a new create.
    # revoke_within_months: 14 — uses 14×30=420-day window so a cert expiring in ~364
    # days (just beyond the 12-month guard) is included as a revoke candidate.
    UI.header("Step 1a — Revoking expired Distribution certs from Apple portal")
    revoke_expired_distribution_certs(min_free_slots: 2, revoke_within_months: 14)

    # Step 1b: Delete cert files from the Match git repo AND delete any stale/duplicate
    # provisioning profiles from the Apple portal. Previous runs can leave orphan profiles
    # on the portal that trigger a 409 "Multiple profiles found" when Match recreates them.
    UI.header("Step 1b — Purging cert files from Match repo + stale profiles from Apple portal")
    purge_match_distribution_certs
    purge_apple_portal_profiles

    UI.header("Step 2 — Creating fresh AdHoc certificate (Firebase App Distribution)")
    fetch_certificates_with_match(match_type: "adhoc")

    UI.header("Step 3 — Creating fresh App Store certificate (TestFlight + App Store)")
    fetch_certificates_with_match(match_type: "appstore")

    UI.success("Certificate renewal complete. Run the iOS TestFlight lane (`ios beta`) to continue.")
  end

  desc <<~DESC
    Full cross-platform cert renewal — all 4 cert types across all managed bundle IDs.

    Covers:
      • appstore (iOS)                    — TestFlight + App Store (all app IDs)
      • adhoc (iOS)                       — Firebase App Distribution (all app IDs)
      • appstore (macOS)                  — Mac .app codesign (primary app ID)
      • mac_installer_distribution (macOS) — Mac .pkg signing (primary app ID)

    Options:
      force:true          — renew even if certs are not near expiry (default false)
      type:TYPE           — all | appstore | adhoc | mac_installer_distribution (default all)

    Run locally:
      cd deployment && bundle exec fastlane ios renewAllCerts
      cd deployment && bundle exec fastlane ios renewAllCerts force:true
      cd deployment && bundle exec fastlane ios renewAllCerts type:mac_installer_distribution

    This is the local equivalent of openMF/ios-provisioning-profile cron (cert-renewal.yml).
    See openMF/ios-provisioning-profile/cert-renewal.sh for a standalone bash runner
    that also works outside of this Fastlane context (e.g. from ios-provisioning-profile).
  DESC
  # Renew every certificate and provisioning profile through Match, for all platforms.
  lane :renewAllCerts do |options|
    options = sanitize_options(options)

    cfg            = FastlaneConfig::IosConfig::BUILD_CONFIG
    ssh_key        = File.join(DEPLOYMENT_REPO_ROOT, cfg[:match_ssh_key_path])
    git_url        = options[:match_git_url]    || cfg[:match_git_url]    || ""
    git_branch     = options[:match_git_branch] || cfg[:match_git_branch] || "master"
    git_ssh_key    = File.exist?(ssh_key) ? ssh_key : nil
    cert_type      = options.fetch(:type, "all").to_s

    # Bundle IDs to provision in the Match repo. Defaults to THIS fork's own app id —
    # never a foreign one, or a fork would try to provision an App ID it does not own.
    # Set APP_IDENTIFIERS (space-separated) to add sibling apps / app-extension ids.
    all_app_ids    = (ENV["APP_IDENTIFIERS"] || ForkIdentity::APP_ID.to_s).split
    primary_app_id = ENV["PRIMARY_APP_ID"] || ForkIdentity::APP_ID

    load_api_key(options)
    with_ios_preamble(options)
    setup_ios_keychain(keychain_password: options[:keychain_password] || ENV["KEYCHAIN_PASSWORD"])

    match_base = {
      git_url:                    git_url,
      git_branch:                 git_branch,
      git_private_key:            git_ssh_key,
      readonly:                   false,
      force_for_new_certificates: true,
      force_for_new_devices:      false,
    }

    # ── Step 1: Revoke expired Apple Distribution certs from portal ─────────────
    # Runs regardless of cert_type — expired certs are always safe to clear first.
    UI.header("Step 1 — Revoking expired Distribution certs from Apple portal")
    revoke_expired_distribution_certs(min_free_slots: 2, revoke_within_months: 14)

    # ── Step 2: Purge stale cert files from Match repo + orphan portal profiles ─
    UI.header("Step 2 — Purging Match repo cert files + orphan Apple portal profiles")
    purge_match_distribution_certs
    purge_apple_portal_profiles

    # ── Step 3: iOS appstore — TestFlight + App Store (all bundle IDs) ─────────
    if %w[all appstore].include?(cert_type)
      UI.header("Step 3 — iOS appstore certs + profiles")
      all_app_ids.each do |app_id|
        UI.message("  → #{app_id}")
        match(**match_base.merge(type: "appstore", platform: "ios", app_identifier: app_id))
      end
    end

    # ── Step 4: iOS adhoc — Firebase App Distribution (all bundle IDs) ─────────
    if %w[all adhoc].include?(cert_type)
      UI.header("Step 4 — iOS adhoc profiles (uses same Apple Distribution cert)")
      all_app_ids.each do |app_id|
        UI.message("  → #{app_id}")
        match(**match_base.merge(type: "adhoc", platform: "ios", app_identifier: app_id))
      end
    end

    # ── Step 5: macOS appstore — Mac App Store .app codesign (primary ID) ──────
    if %w[all appstore].include?(cert_type)
      UI.header("Step 5 — macOS appstore cert + profile")
      match(**match_base.merge(type: "appstore", platform: "macos", app_identifier: primary_app_id))
    end

    # ── Step 6: mac_installer_distribution — .pkg signing (primary ID) ─────────
    if %w[all mac_installer_distribution].include?(cert_type)
      UI.header("Step 6 — macOS mac_installer_distribution cert + profile")
      match(**match_base.merge(type: "mac_installer_distribution", platform: "macos", app_identifier: primary_app_id))
    end

    UI.success("🎉 renewAllCerts complete — ios-provisioning-profile is fully stocked.")
    UI.message("   Run 'bundle exec fastlane mac desktop_testflight' to build + upload to TestFlight.")
  end
end
