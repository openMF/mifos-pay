# deployment/ios/testflight/lane.rb
# Imported by fastlane/Fastfile via `import` directive (AC64).
# Extracted from legacy `beta` lane (iOS platform block).
#
# Phase 2 of `deploy-gha-product-flavors` epic (D5/D9): TestFlight lanes now
# accept `flavor:` + `build_type:` options and derive the Xcode scheme from
# `VariantResolver.resolve(...).ios_scheme` — the pre-Phase-2 hardcoded
# fallback to `ios_config[:scheme]` ("iosApp") is replaced by the
# convention-derived per-variant scheme (`prodRelease`, `demoStaging`, …).
# All aliases + `uploadTestFlight` / `promoteToExternalBeta` are preserved.
require_relative "../../_shared/lib/appstore_helpers"
require_relative "../../_shared/lib/version_helpers"

platform :ios do
  desc "Upload an already-built IPA to TestFlight (skips build; use after beta build succeeded but pilot upload failed)"
  lane :uploadTestFlight do |options|
    options         = sanitize_options(options)
    ios_config      = FastlaneConfig::IosConfig::BUILD_CONFIG
    testflight_config = FastlaneConfig::IosConfig::TESTFLIGHT_CONFIG

    load_api_key(options)

    ipa_path = options[:ipa] || File.join(DEPLOYMENT_REPO_ROOT, "cmp-ios/build/iosApp.ipa")
    UI.user_error!("IPA not found at #{ipa_path}") unless File.exist?(ipa_path)
    UI.important("📦 Uploading existing IPA: #{ipa_path} (#{File.size(ipa_path) / 1_048_576} MB)")

    releaseNotes = generateReleaseNote()
    locale = ios_config[:primary_locale]
    localized_build_info = {
      "default" => { whats_new: releaseNotes },
      locale    => { whats_new: releaseNotes },
    }

    pilot(
      api_key:                              Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      apple_id:                             ios_config[:apple_id],
      ipa:                                  ipa_path,
      beta_app_review_info:                 testflight_config[:beta_app_review_info].dup,
      beta_app_feedback_email:              testflight_config[:beta_app_feedback_email],
      beta_app_description:                 testflight_config[:beta_app_description],
      demo_account_required:                testflight_config[:demo_account_required],
      distribute_external:                  testflight_config[:distribute_external],
      notify_external_testers:              testflight_config[:notify_external_testers],
      groups:                               testflight_config[:groups],
      skip_submission:                      testflight_config[:skip_submission],
      skip_waiting_for_build_processing:    testflight_config[:skip_waiting_for_build_processing],
      submit_beta_review:                   testflight_config[:submit_beta_review],
      expire_previous_builds:               testflight_config[:expire_previous_builds],
      reject_build_waiting_for_review:      testflight_config[:reject_build_waiting_for_review],
      wait_processing_interval:             testflight_config[:wait_processing_interval],
      wait_processing_timeout_duration:     testflight_config[:wait_processing_timeout_duration],
      uses_non_exempt_encryption:           testflight_config[:uses_non_exempt_encryption],
      changelog:                            releaseNotes,
      localized_app_info:                   testflight_config[:localized_app_info],
      localized_build_info:                 localized_build_info,
    )

    UI.success("✅ Successfully uploaded to TestFlight!")
  end

  desc "Upload beta build to TestFlight (parameterized on flavor + build_type; scheme from resolver)"
  lane :beta do |options|
    options = sanitize_options(options)
    flavor    = (options[:flavor]     || :prod).to_sym
    build_ty  = (options[:build_type] || :release).to_sym

    # Xcode scheme is derived by CONVENTION from the manifest — no more
    # hardcoded "iosApp" default. The resolved scheme name matches the
    # `.xcscheme` filenames shipped under
    # `cmp-ios/iosApp.xcodeproj/xcshareddata/xcschemes/` (six today:
    # {prod,demo}{Debug,Staging,Release}).
    variant           = VariantResolver.resolve(flavor: flavor.to_s, build_type: build_ty.to_s)
    ios_config        = FastlaneConfig::IosConfig::BUILD_CONFIG
    testflight_config = FastlaneConfig::IosConfig::TESTFLIGHT_CONFIG

    with_ios_preamble(options)
    setup_ci_if_needed
    load_api_key(options)

    # E6 — assemble the Kotlin `ComposeApp` XCFramework (SwiftPM/XCFramework) so the
    # `iosApp.xcodeproj`
    # archive links the framework the app's Package.swift binary target + embed
    # Run-Script phase consume. Staging → Release slice.
    assemble_ios_xcframework(build_ty.to_s)

    # Auto-sync + verify App Store Connect store config BEFORE the ~15-min build:
    # fail fast if the app record is missing, and create/update the TestFlight "Test
    # Information" (BetaAppLocalization) from TESTFLIGHT_CONFIG so the external-beta
    # promotion never fails with `betaAppLocalizations not found`. Fully automatic —
    # the TestFlight parallel to the Play Store listing sync that runs with the AAB.
    ensure_testflight_store_config(app_identifier: ios_config[:app_identifier])

    # Ensure the ACCOUNT-LEVEL internal + external tester groups exist and are populated (from the reusable
    # _org/testflight-testers.yaml). Internal groups are has_access_to_all_builds, so this build reaches
    # internal testers automatically once it finishes processing — no per-build assignment needed. External
    # testers are wired here and distributed later by `promoteToExternalBeta` (Apple beta review). Best-effort.
    sync_testflight_testers(app_identifier: ios_config[:app_identifier])

    fetch_certificates_with_match(options.merge(match_type: "appstore"))

    # Switch project from whatever signing state the previous lane left it in
    # (e.g. Manual+AdHoc from a Firebase deploy) to Manual+AppStore so xcodebuild
    # archives with the AppStore profile Match just installed. Target is the
    # resolved per-variant scheme, not a generic "iosApp".
    update_code_signing_settings(
      use_automatic_signing: false,
      path:                  ios_config[:project_path],
      team_id:               ios_config[:team_id],
      code_sign_identity:    "Apple Distribution",
      targets:               ["iosApp"],  # Xcode TARGET name (fixed in KMP template), NOT the scheme (prodRelease/…) — a scheme here matches no target → update is a silent no-op → archive hunts a Development profile
      bundle_identifier:     ios_config[:app_identifier],
      profile_name:          "match AppStore #{ios_config[:app_identifier]}",
    )

    # EVERY app-extension target needs its OWN profile (its bundle id is a distinct App ID).
    # Discovered from the .xcodeproj — extension names are fork-specific, never hardcoded.
    # Without this the archive fails late with
    #   `"<Ext>" requires a provisioning profile with the <capability> feature`.
    ios_extension_targets(
      ios_config[:project_path], ios_config[:app_identifier], build_ty.to_s.capitalize
    ).each do |ext|
      UI.message("🔏 signing app-extension target #{ext[:name]} → #{ext[:bundle_id]}")
      update_code_signing_settings(
        use_automatic_signing: false,
        path:                  ios_config[:project_path],
        team_id:               ios_config[:team_id],
        code_sign_identity:    "Apple Distribution",
        targets:               [ext[:name]],
        bundle_identifier:     ext[:bundle_id],
        profile_name:          "match AppStore #{ext[:bundle_id]}",
      )
    end

    gradle_version = get_version_from_gradle(sanitize_for_appstore: true)

    latest_build_number = latest_tf_build_number_resilient(
      app_identifier: options[:app_identifier] || ios_config[:app_identifier],
      api_key: Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
    )
    latest_version = Actions.lane_context[SharedValues::LATEST_TESTFLIGHT_VERSION]

    version = AppStoreHelpers.bumped_version(
      options[:version_number] || gradle_version,
      latest_version,
    )
    UI.important("📱 Final App Store version: #{version}")

    increment_version_number(xcodeproj: ios_config[:project_path], version_number: version)
    increment_build_number(xcodeproj: ios_config[:project_path], build_number: latest_build_number + 1)

    build_ios_project(
      options.merge(
        scheme:                    variant.ios_scheme,
        configuration:             build_ty.to_s.capitalize,
        provisioning_profile_name: ios_config[:provisioning_profile_appstore],
      ),
    )

    releaseNotes = generateReleaseNote()
    locale = ios_config[:primary_locale]
    localized_build_info = {
      "default" => { whats_new: releaseNotes },
      locale    => { whats_new: releaseNotes },
    }

    # Stage 1 = INTERNAL TestFlight only. External distribution + Apple's beta review
    # are owned by Stage 2 (`promoteToExternalBeta`). Beta review — and therefore the
    # app-level "Test Information" (BetaAppLocalization: beta_app_description /
    # beta_app_feedback_email / localized_app_info) — is required ONLY for external
    # testing. Passing that app-level metadata here forced Spaceship to update a
    # BetaAppLocalization that doesn't exist yet on a fresh app record, raising
    # `betaAppLocalizations not found for this app`. Internal uploads need none of it:
    # this mirrors the macOS `desktop_testflight` lane, which uploads cleanly because
    # it sets zero app-level beta metadata. Only BUILD-level "What to Test"
    # (localized_build_info) is set — it's created together with the new build.
    # Every TestFlight deploy reaches BOTH the internal (team) and external ({org}-mobile-apps)
    # groups — but as TWO SEPARATE, resilient phases, NOT one merged pilot call.
    #
    # WHY SPLIT (the "internal-yes / external-no" defect this heals, user-flagged 2026-08-27):
    # the old lane merged upload + external-distribute + submit_beta_review into ONE pilot call.
    # Internal groups are has_access_to_all_builds, so they receive the binary the instant it
    # finishes processing — the upload alone satisfies internal. External needs group assignment
    # + Apple beta-review submission, which happens LATER in the same call. When that review step
    # hiccups (a transient Spaceship 409/timeout, a not-yet-propagated BetaAppLocalization, an
    # export-compliance race) it RAISES *after* the binary is already up: internal has the build,
    # external assignment + review never land → exactly "added to internal, not external". So the
    # binary upload and the external distribution are now DECOUPLED: a review hiccup can never cost
    # the internal upload, and external is a distinct retryable phase (identical to the proven
    # promoteToExternalBeta Stage-2 path, reused below — one code path, no duplication).
    uploaded_build = latest_build_number + 1

    # PHASE 1 — reliable INTERNAL upload. No external group, no beta-review in THIS call
    # (skip_submission: true). Internal testers auto-receive via all-builds once processing ends.
    pilot(
      api_key:                           Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      distribute_external:               false,
      skip_submission:                   true,   # external distribution + beta review are PHASE 2
      skip_waiting_for_build_processing: false,  # wait so PHASE 2 can submit for review immediately
      wait_processing_interval:          testflight_config[:wait_processing_interval],
      wait_processing_timeout_duration:  testflight_config[:wait_processing_timeout_duration],
      expire_previous_builds:            testflight_config[:expire_previous_builds],
      uses_non_exempt_encryption:        testflight_config[:uses_non_exempt_encryption],
      changelog:                         releaseNotes,
      localized_build_info:              localized_build_info,
    )
    UI.success("✅ PHASE 1 — uploaded build #{uploaded_build} to TestFlight; internal testers auto-receive via all-builds access.")

    # PHASE 2 — ALWAYS distribute to EXTERNAL + submit Apple beta review, reusing the proven
    # promoteToExternalBeta path (assign external group → submit_beta_review → export compliance).
    # Guarantees every I1 reaches BOTH groups; a review hiccup here is isolated from PHASE 1.
    promoteToExternalBeta(options.merge(build_number: uploaded_build.to_s, changelog: releaseNotes))

    UI.success("✅ Uploaded to TestFlight and distributed to BOTH internal + external — external submitted for Apple beta review.")
  end

  desc "Stage 1 → Stage 2 promotion: distribute an already-uploaded TF build to external testers (no rebuild, no re-upload). Triggers Apple's beta review (~24h)."
  lane :promoteToExternalBeta do |options|
    options           = sanitize_options(options)
    ios_config        = FastlaneConfig::IosConfig::BUILD_CONFIG
    testflight_config = FastlaneConfig::IosConfig::TESTFLIGHT_CONFIG

    load_api_key(options)

    # Resolve build: explicit option > latest TF build for this app.
    build_number = options[:build_number]&.to_s || latest_tf_build_number_resilient(
      app_identifier: ios_config[:app_identifier],
      api_key:        Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
    ).to_s

    # External beta review REQUIRES the app-level Test Information — sync it from
    # config first so this promotion never fails for a fresh app record.
    ensure_testflight_store_config(app_identifier: ios_config[:app_identifier])
    # Ensure the external tester groups + testers exist (from the account-level registry) before distributing.
    sync_testflight_testers(app_identifier: ios_config[:app_identifier])

    # External group comes from gradle/fork.properties (apple.testers.external.group), then explicit
    # option, then a sane default.
    fp_external = FastlaneConfig::IosConfig::TESTERS[:ios][:external_group]
    external_groups = options[:groups] ||
                      (fp_external ? [fp_external] : nil) ||
                      testflight_config[:external_groups] ||
                      ["External Beta"]

    # Pin each group NAME to the external group's ID. pilot selects with
    # `BetaGroup#matches_identifiers?` = `identifiers.include?(name) || identifiers.include?(id)` —
    # NAME-ONLY matching with no internal/external awareness. A name shared by an internal AND an
    # external group (supported by ASC, and a common org convention) therefore matches BOTH, and
    # `add_beta_groups` fails the whole call on the internal one with Apple's
    # `Cannot add internal group to a build.` An id selects exactly one group.
    external_groups = external_groups.map do |n|
      (app_for_groups ||= Spaceship::ConnectAPI::App.find(ios_config[:app_identifier]))
        .get_beta_groups(filter: { name: n }).to_a.find { |g| !g.is_internal_group }&.id || n
    end

    UI.important("📦 Promoting TF build #{build_number} → external testers (#{external_groups.join(', ')})")

    # pilot in distribute-only mode — Spaceship updates the build's group
    # assignment + submits for beta review. No IPA upload.
    pilot(
      api_key:                              Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      app_identifier:                       ios_config[:app_identifier],
      app_platform:                         "ios",   # distribute_only has no upload to infer platform from → pilot prompts interactively without this (crashes in non-interactive/CI)
      build_number:                         build_number,
      distribute_only:                      true,
      distribute_external:                  true,
      notify_external_testers:              true,
      groups:                               external_groups,
      submit_beta_review:                   true,
      reject_build_waiting_for_review:      true,
      changelog:                            options[:changelog] || generateReleaseNote(),
      beta_app_review_info:                 testflight_config[:beta_app_review_info]&.dup,
      uses_non_exempt_encryption:           testflight_config[:uses_non_exempt_encryption],
    )

    UI.success("✅ Build #{build_number} submitted for Apple's beta review — external testers will receive it on approval (~24h).")
  end
end
