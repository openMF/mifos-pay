# deployment/ios/sync-listing/lane.rb
# Syncs App Store metadata (name, subtitle, keywords, description, screenshots)
# WITHOUT uploading an IPA or submitting for review. Safe to run at any time.
# Invocation: (cd deployment && bundle exec fastlane ios syncListing)
require_relative "../../_shared/lib/appstore_helpers"

platform :ios do
  desc "OVERRIDE App Store listing TEXT (name/subtitle/keywords/description) from the promoted deck — no build, no submission. Screenshots are overridden separately by the deploy runtime via asc-upload-screenshots.rb (reliable clear-and-replace)."
  lane :syncListing do |options|
    options     = sanitize_options(options)
    ios_config  = FastlaneConfig::IosConfig::BUILD_CONFIG
    appstore_config = FastlaneConfig::IosConfig::APPSTORE_CONFIG

    load_api_key(options)

    UI.message("📋 Overriding App Store listing text (metadata)")
    UI.message("   Metadata:     #{ios_config[:metadata_path]}")

    deliver(
      api_key:                              Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      metadata_path:                        ios_config[:metadata_path],
      skip_binary_upload:                   true,
      # SCREENSHOTS: NOT via deliver. This lane is the TEXT half of the iOS store OVERRIDE. The deploy
      # runtime's [SC-I]/release step replaces screenshots via asc-upload-screenshots.rb — it CLEARS
      # each display set then re-uploads the full ordered set (a true, reliable replace). deliver's
      # screenshot upload leaves display sets half-filled, so it cannot guarantee a full override.
      skip_screenshots:                     true,
      # NO app_rating_config_path — a listing sync must not set the Age Rating (one-time app-level
      # declaration; deliver's PATCH also rejects the config's versioned `v1_0` shape on the current
      # ASC API). deliver has NO `skip_submission` (a pilot param) — submit_for_review:false leaves it
      # unsubmitted.
      submit_for_review:                    false,
      skip_app_version_update:              true,
      ignore_language_directory_validation: true,
      run_precheck_before_submit:           false,
      force:                                true,   # skip HTML verification prompt
    )

    # Record the pushed hash so a subsequent deploy lane's drift-checked sync correctly skips.
    record_store_listing_synced("ios", ios_config[:metadata_path])

    UI.success("✅ App Store listing text overridden (no binary, not submitted). Screenshots via asc-upload-screenshots.rb.")
  end
end
