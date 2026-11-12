# deployment/desktop/sync-listing/lane.rb
# Syncs Desktop store listings WITHOUT uploading binaries.
#
# Sub-targets (pass via options[:target]):
#   "github"   — update current GitHub Release notes/body (no binary needed, no secrets)
#   "mac"      — sync Mac App Store listing via deliver (requires ASC API key)
#   "all"      — github + mac (default when :target not specified)
#
# Invocation examples:
#   (cd deployment && bundle exec fastlane desktop syncListing)
#   (cd deployment && bundle exec fastlane desktop syncListing target:github)
#   (cd deployment && bundle exec fastlane desktop syncListing target:mac)
require_relative "../../_shared/lib/appstore_helpers"
require_relative "../../_shared/lib/version_helpers"

platform :desktop do
  desc "Sync Desktop store listings — no binary upload (GitHub Release + Mac App Store)"
  lane :syncListing do |options|
    options = sanitize_options(options)
    target  = (options[:target] || "all").to_s

    run_github = %w[all github].include?(target)
    run_mac    = %w[all mac].include?(target)

    # ── GitHub Release notes sync ─────────────────────────────────────────────
    if run_github
      UI.message("📋 Syncing GitHub Release notes...")

      git_tag = sh("git describe --tags --abbrev=0 2>/dev/null || git rev-parse --short HEAD",
                   log: false).strip

      # Build release notes from changelog or commit history
      notes = generateReleaseNote()

      sh("gh release edit '#{git_tag}' --notes '#{notes.gsub("'", "'\\''")}' " \
         "--repo #{FastlaneConfig::ProjectConfig.upstream_repo} " \
         "2>&1") do |status, output|
        UI.user_error!("gh release edit failed:\n#{output}") unless status.success?
      end

      UI.success("✅ GitHub Release notes updated for tag #{git_tag}")
    end

    # ── Mac App Store listing sync ─────────────────────────────────────────────
    if run_mac
      UI.message("📋 Syncing Mac App Store listing...")

      mac_metadata_path    = File.join(DEPLOYMENT_REPO_ROOT, "deployment/desktop/mac-app-store/metadata")
      # screenshots live UNDER metadata/ (deliver's screenshots_path convention, parity with iOS) — the
      # bare mac-app-store/screenshots path finds nothing → deliver syncs 0 screenshots. Matches
      # MAC_APP_STORE_SCREENSHOTS_PATH in deployment/desktop/mac-app-store/lane.rb.
      mac_screenshots_path = File.join(DEPLOYMENT_REPO_ROOT, "deployment/desktop/mac-app-store/metadata/screenshots")

      unless File.directory?(mac_metadata_path)
        UI.important("⚠️  Mac App Store metadata not found at #{mac_metadata_path} — skipping Mac sync")
        next if run_github  # already ran github, just skip mac
        UI.user_error!("Mac metadata missing — run [G] to generate store listing first")
      end

      load_api_key(options)

      deliver(
        api_key:                              Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
        metadata_path:                        mac_metadata_path,
        screenshots_path:                     mac_screenshots_path,
        platform:                             "osx",
        skip_binary_upload:                   true,
        # deliver has NO `skip_submission` (that is a pilot param) — passing it aborts with
        # "invalid parameters to deliver". Not submitting == leaving submit_for_review at false.
        submit_for_review:                    false,
        skip_app_version_update:              true,
        overwrite_screenshots:                true,
        ignore_language_directory_validation: true,
        run_precheck_before_submit:           false,
        force:                                true,
      )

      # Record the pushed hash so a subsequent mac deploy lane's drift-checked sync correctly skips.
      record_store_listing_synced("mac", mac_metadata_path)

      UI.success("✅ Mac App Store listing synced (no binary uploaded)")
    end

    UI.success("✅ Desktop listing sync complete (target: #{target})")
  end
end
