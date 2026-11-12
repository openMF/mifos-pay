# deployment/_shared/config.rb
#
# Single source of truth for all deployment configuration.
# Imported by deployment/Fastfile BEFORE all lane.rb imports.
#
# Fork identity source of truth: gradle/libs.versions.toml (6 keys).
# Secrets: read from secrets/ filesystem first, ENV fallback for CI.
#
# Usage:
#   Local/manual  → cp secrets/sample/* secrets/live/, fill values, run fastlane
#   GitHub Actions → secrets injected as ENV vars by workflow-snippet.yml
#   Framework     → /secrets pull materializes vault to secrets/, run fastlane

require_relative "lib/appstore_helpers"
require_relative "lib/firebase_helpers"
require_relative "lib/version_helpers"
require_relative "lib/build_secrets"   # secrets path/value resolver (secrets/LAYOUT.yaml is SoT)
require_relative "variant_resolver"    # (flavor, buildType) → gradle_task / artifact / ios_scheme
                                       # by convention (Phase 2 of deploy-gha-product-flavors epic;
                                       # delegates all secret paths to BuildSecrets.for(flavor:))

require "yaml"

# ── AppProfile — fork-owned white-label SoT (app-profile/) ────────────────────
# Lazily loads app-profile/app.yaml + platforms/**/*.yaml, deep-merges them into
# ONE nested hash, and resolves the SAME flat dotted gradle/fork.properties
# namespace the lanes already use via AppProfile.get(dotted_key). `_fork_prop`
# tries this FIRST (non-nil / non-empty), then falls back to gradle/fork.properties.
# Fail-soft: if app-profile/ is absent (or a file is malformed), get() returns nil
# for everything and `_fork_prop` behaves EXACTLY as before. Lanes are unchanged.
module AppProfile
  module_function

  # Walk up from this file's dir to the first ancestor containing app-profile/.
  def _root
    return @_root if defined?(@_root)
    # APP_PROFILE_ROOT: an explicit app-profile/ dir — set by `derive.rb --fork <dir>` so a fork that
    # has no white-label machinery yet can verify its app-profile with the template's resolver.
    override = ENV["APP_PROFILE_ROOT"]
    return @_root = override if override && File.directory?(override)
    @_root = nil
    dir = __dir__
    while dir && dir != "/"
      cand = File.join(dir, "app-profile")
      if File.directory?(cand)
        @_root = cand
        break
      end
      parent = File.dirname(dir)
      break if parent == dir
      dir = parent
    end
    @_root
  end

  # Recursively merge hash b over a. A non-hash on either side means b wins outright, which
  # is what lets a platform YAML override a scalar the shared one set.
  def _deep_merge(a, b)
    return b unless a.is_a?(Hash) && b.is_a?(Hash)
    out = a.dup
    b.each do |k, v|
      out[k] = (out[k].is_a?(Hash) && v.is_a?(Hash)) ? _deep_merge(out[k], v) : v
    end
    out
  end

  # Merged hash of app.yaml + every platforms/**/*.yaml (string keys, deep-merged).
  def _data
    return @_data if defined?(@_data)
    @_data = {}
    root = _root
    return @_data unless root
    files = []
    app = File.join(root, "app.yaml")
    files << app if File.exist?(app)
    # Merge ONLY the <platform>.yaml CONFIG files — never the app-content/ store DECLARATIONS
    # (questionnaire answers read by their own consumers, not identity/store config keys).
    files.concat(Dir.glob(File.join(root, "platforms", "**", "*.yaml")).reject { |f| f.include?("/app-content/") }.sort)
    files.each do |f|
      begin
        y = YAML.respond_to?(:unsafe_load_file) ? YAML.unsafe_load_file(f) : YAML.load_file(f)
        @_data = _deep_merge(@_data, y) if y.is_a?(Hash)
      rescue StandardError
        # fail-soft: a malformed profile file must never crash a deploy.
      end
    end
    @_data
  rescue StandardError
    @_data = {}
  end

  # Flat gradle/fork.properties key → dotted path in the merged app-profile hash.
  # Every non-legacy fork.properties key resolves deterministically; unmapped → nil
  # (so `_fork_prop` falls through to gradle/fork.properties for legacy keys like
  # `apple.tf.groups`).
  MAP = {
    # ── identity / app ──
    "app.id"                             => "identity.app_id",
    # identity.app_name is authored in app-profile and documented (CLAUDE.md) as flowing to
    # fork.properties#app.display.name → BuildKonfig.APP_DISPLAY_NAME, but had no MAP entry, so a
    # properly-DERIVED bridge never carried it: feature/settings + core-base/ui silently fell back
    # to "App" / "App Toolkit". Only a hand-authored fork.properties ever had the key.
    "app.display.name"                   => "identity.app_name",
    "app.description"                    => "store.app_description",

    # Per-flavor network endpoints + demo credentials + log tag. app.yaml documents these as flowing
    # to gradle/fork.properties, and KMPFlavorsConventionPlugin reads them into per-flavor
    # BuildConfig (BASE_URL / DEMO_USERNAME / DEMO_PASSWORD / LOG_TAG). They were mapped in Kotlin's
    # APP_PROFILE_MAP but NOT here, so the pure-Ruby derive.rb path (used by CI and doctor's fast
    # lane) produced a bridge missing them while syncForkConfig's produced one with them — two
    # writers of one file with different coverage. Paths taken verbatim from the Kotlin map, which
    # agreed with this one on all 113 shared keys.
    "network.base.url.demo"              => "network.demo_base_url",
    "network.base.url.prod"              => "network.prod_base_url",
    "demo.username"                      => "network.demo_username",
    "demo.password"                      => "network.demo_password",
    "log.tag"                            => "network.log_tag",
    # ── org ──
    "org.name"                           => "org.name",
    "org.email"                          => "org.email",
    "org.first.name"                     => "org.first_name",
    "org.secrets.namespace"              => "org.secrets_namespace",
    "org.last.name"                      => "org.last_name",
    "org.phone"                          => "org.phone",
    "org.copyright"                      => "org.copyright",
    "org.marketing.url"                  => "org.marketing_url",
    "org.privacy.url"                    => "org.privacy_url",
    "org.support.url"                    => "org.support_url",
    # ── legal ──
    "legal.company.name"                 => "legal.company_name",
    "legal.jurisdiction"                 => "legal.jurisdiction",
    "legal.effective.date"               => "legal.effective_date",
    "legal.contact.email"                => "legal.contact_email",
    # ── keystore DN ──
    "keystore.dn.org_unit"               => "keystore_dn.org_unit",
    "keystore.dn.city"                   => "keystore_dn.city",
    "keystore.dn.state"                  => "keystore_dn.state",
    "keystore.dn.country"                => "keystore_dn.country",
    # ── store (common: iOS + macOS + Android) ──
    "store.primary.locale"               => "store.primary_locale",
    "store.title"                        => "store.title",
    "store.subtitle"                     => "store.subtitle",
    "store.promotional.text"             => "store.promotional_text",
    "store.release.notes"                => "store.release_notes",
    "store.description"                  => "store.description",
    "store.copyright"                    => "store.copyright",
    "store.ios.age.rating"               => "store.age_rating",
    "store.review.notes"                 => "store.review.notes",
    "store.review.demo.user"             => "store.review.demo_user",
    "store.review.demo.password"         => "store.review.demo_password",
    # ── android ──
    "store.android.category"             => "android.category",
    "store.android.short.description"    => "android.short_description",
    "store.android.changelog"            => "android.changelog",
    "store.android.video.url"            => "android.video_url",
    "android.play.tracks.by_flavor"      => "android.play_tracks_by_flavor",
    "firebase.android.prod.app.id"       => "android.firebase.app_id_prod",
    "firebase.android.demo.app.id"       => "android.firebase.app_id_demo",
    "firebase.groups"                    => "android.firebase.groups",
    "play.testers.internal.googlegroup"  => "android.play_testers.internal_googlegroup",
    "play.testers.closed.googlegroup"    => "android.play_testers.closed_googlegroup",
    # ── apple (shared iOS + macOS) ──
    "apple.team.id"                      => "apple.team_id",
    "apple.app.store.id"                 => "apple.app_store_id",
    "apple.match.git.url"                => "apple.match.git.url",
    "apple.match.git.branch"             => "apple.match.git.branch",
    "firebase.ios.prod.app.id"           => "apple.firebase.ios_app_id_prod",
    "firebase.ios.demo.app.id"           => "apple.firebase.ios_app_id_demo",
    "firebase.ios.app.id"                => "apple.firebase.ios_app_id",
    "apple.testers.internal.group"       => "apple.testers.internal_group",
    "apple.testers.external.group"       => "apple.testers.external_group",
    "apple.testers.external.public.link" => "apple.testers.external_public_link",
    "store.ios.keywords"                 => "apple.keywords",
    "store.ios.category"                 => "apple.category",
    # ── apple / iOS-only ──
    "store.ios.secondary.category"       => "apple.ios.secondary_category",
    "store.ios.apple.tv.privacy.url"     => "apple.ios.apple_tv_privacy_url",
    # ── apple / macOS-only ──
    "store.macos.keywords"               => "apple.macos.keywords",
    "store.macos.category"               => "apple.macos.category",
    "store.macos.secondary.category"     => "apple.macos.secondary_category",
    "mac.app.category"                   => "apple.macos.app_category",
    # ── web ──
    "web.cloudflare.project"             => "web.cloudflare_project",
    # ── Windows / Microsoft Store (platforms/windows/windows.yaml, top-level `windows:`) ──
    "store.windows.ms.app.id"            => "windows.ms_app_id",
    "store.windows.ms.publish.mode"      => "windows.ms_publish_mode",
    "store.windows.ms.visibility"        => "windows.ms_visibility",
    "windows.store.id"                   => "windows.store_id",
    "windows.msix.identity.name"         => "windows.msix_identity_name",
    "windows.msix.identity.publisher"    => "windows.msix_publisher",
    "windows.msix.publisher.display.name" => "windows.msix_publisher_display_name",
    "windows.partner.center.tenant.id"   => "windows.partner_center.tenant_id",
    "windows.partner.center.client.id"   => "windows.partner_center.client_id",
    # ── Ubuntu / Linux (platforms/ubuntu/ubuntu.yaml, top-level `ubuntu:`) ──
    "ubuntu.snap.name"                   => "ubuntu.snap.name",
    "ubuntu.snap.grade"                  => "ubuntu.snap.grade",
    "ubuntu.snap.confinement"            => "ubuntu.snap.confinement",
    "ubuntu.snap.channel"                => "ubuntu.snap.channel",
    "ubuntu.flathub.app.id"              => "ubuntu.flathub.app_id",
    "ubuntu.deb.package"                 => "ubuntu.deb.package",
    "ubuntu.deb.section"                 => "ubuntu.deb.section",
    "ubuntu.deb.maintainer"              => "ubuntu.deb.maintainer",
    "ubuntu.deb.homepage"                => "ubuntu.deb.homepage",
    # ── apple / trade representative contact information ──
    "store.apple.trade.first.name"       => "apple.trade_representative.first_name",
    "store.apple.trade.last.name"        => "apple.trade_representative.last_name",
    "store.apple.trade.address.line1"    => "apple.trade_representative.address_line1",
    "store.apple.trade.address.line2"    => "apple.trade_representative.address_line2",
    "store.apple.trade.address.line3"    => "apple.trade_representative.address_line3",
    "store.apple.trade.city"             => "apple.trade_representative.city_name",
    "store.apple.trade.state"            => "apple.trade_representative.state",
    "store.apple.trade.country"          => "apple.trade_representative.country",
    "store.apple.trade.postal.code"      => "apple.trade_representative.postal_code",
    "store.apple.trade.phone"            => "apple.trade_representative.phone_number",
    "store.apple.trade.email"            => "apple.trade_representative.email_address",
    "store.apple.trade.displayed"        => "apple.trade_representative.is_displayed_on_app_store",
    # ── deploy-surface completeness (MS Store / winget / snap / flathub / aur / homebrew / ios-subcats / web) ──
    "win.store.name"                     => "windows.store.name",
    "win.store.short.description"        => "windows.store.short_description",
    "win.store.description"              => "windows.store.description",
    "win.store.category"                 => "windows.store.category",
    "win.store.privacy.url"              => "windows.store.privacy_url",
    "win.winget.package.id"              => "windows.winget.package_id",
    "win.winget.publisher"               => "windows.winget.publisher",
    "win.winget.name"                    => "windows.winget.name",
    "win.winget.moniker"                 => "windows.winget.moniker",
    "ubuntu.snap.summary"                => "ubuntu.snap.summary",
    "ubuntu.snap.description"            => "ubuntu.snap.description",
    "ubuntu.flathub.developer"           => "ubuntu.flathub.developer",
    "ubuntu.flathub.name"                => "ubuntu.flathub.name",
    "ubuntu.flathub.summary"             => "ubuntu.flathub.summary",
    "ubuntu.flathub.description"         => "ubuntu.flathub.description",
    "ubuntu.aur.package.name"            => "ubuntu.aur.package_name",
    "ubuntu.aur.source.type"             => "ubuntu.aur.source_type",
    "mac.homebrew.cask.name"             => "apple.macos.homebrew.cask_name",
    "mac.homebrew.tagline"               => "apple.macos.homebrew.tagline",
    "store.ios.primary.first.subcategory" => "apple.ios.primary_first_sub_category",
    "store.ios.primary.second.subcategory" => "apple.ios.primary_second_sub_category",
    "store.ios.secondary.first.subcategory" => "apple.ios.secondary_first_sub_category",
    "store.ios.secondary.second.subcategory" => "apple.ios.secondary_second_sub_category",
    "web.custom.domain"                  => "web.custom_domain",
  }.freeze

  # Resolve a flat fork.properties key against the merged app-profile hash.
  # Returns nil for an unmapped key or a missing path; scalar leaves are stringified
  # to mirror fork.properties' string return shape.
  def get(key)
    path = MAP[key]
    return nil unless path
    node = _data
    path.split(".").each do |seg|
      return nil unless node.is_a?(Hash) && node.key?(seg)
      node = node[seg]
    end
    return nil if node.is_a?(Hash) || node.is_a?(Array)
    node.nil? ? nil : node.to_s
  end
end

# ── Helpers: read libs.versions.toml + secrets/ ──────────────────────────────

DEPLOYMENT_REPO_ROOT = File.expand_path("../..", __dir__).freeze

# Read key's double-quoted value from gradle/libs.versions.toml.
# The catalog is the build's own source for appId and version, so reading it keeps Fastlane
# and Gradle from disagreeing about what is being shipped.
def _toml_value(key)
  toml = File.join(DEPLOYMENT_REPO_ROOT, "gradle", "libs.versions.toml")
  File.readlines(toml).each do |line|
    m = line.match(/^\s*#{Regexp.escape(key)}\s*=\s*"([^"]+)"/)
    return m[1] if m
  end
  nil
rescue Errno::ENOENT
  UI.error("libs.versions.toml not found at #{toml}") if defined?(UI)
  nil
end

# Read and strip a secret from repo-relative rel_path, or nil when it is absent.
# nil rather than an exception: a missing secret is normal for a lane that does not need it.
def _secret_file(rel_path)
  full = File.join(DEPLOYMENT_REPO_ROOT, rel_path)
  File.exist?(full) ? File.read(full).strip : nil
end

# ENV takes precedence (CI mode); falls back to file content (local/vault mode).
def _secret(env_var, file_path = nil)
  ENV[env_var] || (file_path ? _secret_file(file_path) : nil)
end

# Read a key from the fork-owned white-label SoT (app-profile/) FIRST, then fall
# back to gradle/fork.properties (local, gitignored). Returns nil when neither has
# the key (CI uses ENV vars directly).
def _fork_prop(key)
  ap = AppProfile.get(key)
  return ap if ap && !ap.to_s.strip.empty?
  props = File.join(DEPLOYMENT_REPO_ROOT, "gradle", "fork.properties")
  return nil unless File.exist?(props)
  File.readlines(props).each do |line|
    next if line.strip.start_with?("#") || !line.include?("=")
    k, v = line.strip.split("=", 2)
    return v&.strip if k&.strip == key
  end
  nil
end

# ── Fork identity (single read at load time) ──────────────────────────────────
# Uses module_function so _toml_value is callable with self=ForkIdentity
# (plain `def` at eval-binding scope is not accessible inside module bodies).

module ForkIdentity
  module_function

  # Read key from the version catalog. The memoised inner form of _toml_value.
  def _read_toml(key)
    toml = File.join(DEPLOYMENT_REPO_ROOT, "gradle", "libs.versions.toml")
    File.readlines(toml).each do |line|
      m = line.match(/^\s*#{Regexp.escape(key)}\s*=\s*"([^"]+)"/)
      return m[1] if m
    end
    nil
  rescue Errno::ENOENT
    nil
  end

  public

  APP_ID           = _read_toml("appId")          || "org.example.app"
  APP_DISPLAY_NAME = _read_toml("appDisplayName") || "My App"
  BASE_NAMESPACE   = _read_toml("baseNamespace")  || "app"
  DESKTOP_APP_NAME = _read_toml("desktopAppName") || "My App"
  PROJECT_NAME     = _read_toml("projectName")    || "kmp-project-template"
  IOS_TEAM_ID      = _read_toml("iosTeamId")      || ""
end

# ── FastlaneConfig module ─────────────────────────────────────────────────────

module FastlaneConfig
  SECRETS_DIR = "secrets".freeze
  SECRETS_DIR_ABS = File.join(DEPLOYMENT_REPO_ROOT, "secrets").freeze

  # ── Module-level helpers (module_function so callable from nested modules) ──
  # Top-level `def _secret/_secret_file` is accessible only in FastFile's eval
  # binding scope (i.e. inside lane bodies), NOT from nested module bodies at
  # parse time. These module_function copies fix that gap.
  module_function

  # Resolve a secret from ENV first, then from file_path.
  # ENV wins so CI can inject without writing anything to disk.
  def _secret(env_var, file_path = nil)
    ENV[env_var] || (file_path ? _secret_file(file_path) : nil)
  end

  # Read and strip a secret from repo-relative rel_path, or nil when it is absent.
  # nil rather than an exception: a missing secret is normal for a lane that does not need it.
  def _secret_file(rel_path)
    full = File.join(DEPLOYMENT_REPO_ROOT, rel_path)
    File.exist?(full) ? File.read(full).strip : nil
  end

  # Read a fork property, app-profile first and gradle/fork.properties as the fallback.
  # That order matters: app-profile is the source of truth and fork.properties is its
  # generated bridge, so a stale bridge can never win.
  def _fork_prop(key)
    ap = AppProfile.get(key)
    return ap if ap && !ap.to_s.strip.empty?
    props = File.join(DEPLOYMENT_REPO_ROOT, "gradle", "fork.properties")
    return nil unless File.exist?(props)
    File.readlines(props).each do |line|
      next if line.strip.start_with?("#") || !line.include?("=")
      k, v = line.strip.split("=", 2)
      return v&.strip if k&.strip == key
    end
    nil
  end

  public

  # --------------------------------------------------------------------------
  # ProjectConfig — identity + service-account paths consumed by Appfile
  # --------------------------------------------------------------------------
  module ProjectConfig
    # E0/T5 (white-label): no template identity literal — a fork overrides via ENV['ORGANIZATION_NAME']
    # (or fork.properties org.name, read at call sites). Neutral default, never mifos.
    ORGANIZATION_NAME = (ENV["ORGANIZATION_NAME"] || "Your Organization").freeze

    ANDROID = {
      package_name:        ForkIdentity::APP_ID,
      # DRIFT FIX: was "secrets/play/service-account.json" (wrong — lanes/script use
      # secrets/live/android/play/…). Resolver returns the canonical path from LAYOUT.yaml.
      play_store_json_key: BuildSecrets.for.path(:play_service_account),
    }.freeze

    IOS = {
      app_identifier: ForkIdentity::APP_ID,
      team_id:        ForkIdentity::IOS_TEAM_ID,
    }.freeze

    # The Android applicationId, from the version catalog via app-profile.
    def self.android_package_name
      ANDROID[:package_name]
    end
  end

  # --------------------------------------------------------------------------
  # IosConfig — build paths, ASC API, Match, TestFlight, App Store settings
  # --------------------------------------------------------------------------
  module IosConfig
    # Binding-robust self-reference (2026-08-08): fastlane's `import` intermittently re-parses this file
    # in a NON-top-level binding (e.g. inside CredentialsManager::AppfileConfig), where the unqualified
    # `FastlaneConfig` nesting lookup fails with `uninitialized constant …::FastlaneConfig` and aborts
    # the whole config.rb load — breaking even an Android deploy that never touches IosConfig. Resolve the
    # enclosing module by identity via Module.nesting (always correct regardless of the absolute namespace).
    _c = Module.nesting.find { |m| m.name.to_s.split("::").last == "FastlaneConfig" } || FastlaneConfig
    _s = _c::SECRETS_DIR
    _bs = BuildSecrets.for  # canonical resolver: LAYOUT.yaml + secrets/live/ path + ENV-first
    _nz = ->(v) { (v.nil? || v.to_s.strip.empty?) ? nil : v.to_s.strip }  # nil-if-blank

    BUILD_CONFIG = {
      scheme:                        "iosApp",
      # E6 — the app opens as a plain `.xcodeproj` (SwiftPM/XCFramework); there is no
      # generated `.xcworkspace`, so `build_app` archives from `project:` below.
      project_path:                  File.join(DEPLOYMENT_REPO_ROOT, "cmp-ios/iosApp.xcodeproj"),
      app_identifier:                ForkIdentity::APP_ID,
      team_id:                       ForkIdentity::IOS_TEAM_ID,
      # ASC API key — ENV preferred (CI); file fallback (local/vault)
      key_id:                        BuildSecrets.for.value(:appstore_key_id),
      issuer_id:                     BuildSecrets.for.value(:appstore_issuer_id),
      key_filepath:                  File.join(DEPLOYMENT_REPO_ROOT, BuildSecrets.for.path(:appstore_auth_key)),
      # Match certificate repository — resolve via the CANONICAL BuildSecrets resolver (LAYOUT.yaml →
      # secrets/live/apple/match/… + ENV-first), exactly like :match_ssh_key below. The old
      # `_c._secret("…","#{_s}/apple/match/git_branch")` form dropped the `live/` flavor segment (the file is
      # at secrets/live/apple/match/git_branch, NOT secrets/apple/…), so the lookup MISSED and silently fell
      # through to the fork.properties default. When that default named a branch the provisioning repo does
      # not have, Match checked out an empty orphan branch → certs/distribution/ came back empty → "No code
      # signing identity found and cannot create a new one because you enabled readonly", even though the
      # repo carries a valid cert+key+profile. The last-resort `|| "dev"` matches the provisioning repo's
      # default branch so a fresh fork works with zero config; each fork overrides via its own LAYOUT/fork.properties. (fix 2026-07-31)
      match_git_url:    _nz.call(_bs.value(:match_git_url))    || _c._fork_prop("apple.match.git.url"),
      match_git_branch: _nz.call(_bs.value(:match_git_branch)) || _c._fork_prop("apple.match.git.branch") || "dev",
      match_type:                    "adhoc",
      match_ssh_key_path:            BuildSecrets.for.path(:match_ssh_key),
      match_password:                BuildSecrets.for.value(:match_password),
      certificates_password:         _c._secret("CERTIFICATES_PASSWORD",   "#{_s}/apple/match/certificates_password"),
      keychain_password:             _c._secret("KEYCHAIN_PASSWORD",       "#{_s}/apple/match/keychain_password"),
      # Provisioning profiles
      provisioning_profile_adhoc:    "#{ForkIdentity::APP_ID} AdHoc",
      provisioning_profile_appstore: "#{ForkIdentity::APP_ID} AppStore",
      # Metadata paths
      plist_path:                    File.join(DEPLOYMENT_REPO_ROOT, "cmp-ios/iosApp/Info.plist"),
      metadata_path:                 File.join(DEPLOYMENT_REPO_ROOT, "deployment/ios/appstore/metadata"),
      screenshots_path:              File.join(DEPLOYMENT_REPO_ROOT, "deployment/ios/appstore/metadata/screenshots"),
      app_rating_config_path:        File.join(DEPLOYMENT_REPO_ROOT, "deployment/ios/appstore/metadata/app_store_rating_config.json"),
      primary_locale:                "en-US",
    }.freeze

    TESTFLIGHT_CONFIG = {
      beta_app_review_info: {
        contact_email:         _c._secret("TESTFLIGHT_CONTACT_EMAIL") || _c._fork_prop("org.email")      || "", # E0/T5: no mifos fallback — fork supplies org.email
        contact_first_name:    _c._secret("TESTFLIGHT_FIRST_NAME")    || _c._fork_prop("org.first.name") || "App",
        contact_last_name:     _c._secret("TESTFLIGHT_LAST_NAME")     || _c._fork_prop("org.last.name")  || "Team",
        contact_phone:         _c._secret("TESTFLIGHT_PHONE")         || _c._fork_prop("org.phone")      || "", # E0/T5: no mifos fallback — fork supplies org.phone
        demo_account_required: false,
      }.freeze,
      beta_app_feedback_email:           _c._secret("BETA_FEEDBACK_EMAIL") || _c._fork_prop("org.email") || "", # E0/T5: no mifos fallback — fork supplies org.email
      beta_app_description:              "#{ForkIdentity::APP_DISPLAY_NAME} beta build",
      demo_account_required:             false,
      distribute_external:               true,
      notify_external_testers:           true,
      groups:                            (_c._secret("TESTFLIGHT_GROUPS") || _c._fork_prop("apple.tf.groups") || "internal-testers").split(",").map(&:strip),
      skip_submission:                   false,
      skip_waiting_for_build_processing: true,
      submit_beta_review:                true,
      expire_previous_builds:            false, # NEVER auto-expire: a later run that expires the prior build then FAILS to upload a new one leaves the app with zero live builds (all EXPIRED) → no internal/external distribution possible + un-expiry is impossible. Apple keeps builds 90d; testers always get the latest anyway.
      reject_build_waiting_for_review:   true,
      wait_processing_interval:          30,
      wait_processing_timeout_duration:  900,
      uses_non_exempt_encryption:        false,
      localized_app_info:                {},
    }.freeze

    # BETA TESTERS — per-platform tester groups, sourced entirely from gradle/fork.properties
    # (template documents the keys; the fork fills real values). deployment/ auto-syncs every build to
    # these groups. The groups are pre-created on each store; iOS internal testers must be Apple-team
    # members, everyone else is an iOS EXTERNAL tester (Apple review) — the public link lets them self-join.
    TESTERS = {
      ios: {
        internal_group:       _nz.call(_c._fork_prop("apple.testers.internal.group")) || "Internal Testers",
        external_group:       _nz.call(_c._fork_prop("apple.testers.external.group")) || _nz.call(_c._fork_prop("apple.tf.groups")) || "External Beta",
        external_public_link: (_nz.call(_c._fork_prop("apple.testers.external.public.link")) || "true").to_s.downcase == "true",
        # External TestFlight tester roster (emails — PUBLIC identifiers, not secrets). App-profile is the
        # runtime SoT (apple.testers.external_emails), materialized from _org/company.yaml. Comma-separated
        # fork.properties `apple.tf.testers` is the fallback when app-profile is absent (older forks).
        external_emails:      (lambda {
                                f = File.join(DEPLOYMENT_REPO_ROOT, "app-profile/platforms/apple/apple.yaml")
                                raw = nil
                                if File.exist?(f)
                                  y = (YAML.load_file(f) rescue nil) || {}
                                  raw = y.dig("apple", "testers", "external_emails")
                                end
                                raw ||= _nz.call(_c._fork_prop("apple.tf.testers"))
                                list = raw.is_a?(Array) ? raw : raw.to_s.split(",")
                                list.map { |e| e.to_s.strip.downcase }.reject(&:empty?).uniq
                              }).call,
      },
      play: {
        internal_googlegroup: _nz.call(_c._fork_prop("play.testers.internal.googlegroup")),
        closed_googlegroup:   _nz.call(_c._fork_prop("play.testers.closed.googlegroup")),
      },
      firebase: {
        groups: (_nz.call(_c._fork_prop("firebase.groups")) || "").split(",").map(&:strip).reject(&:empty?),
      },
    }.freeze

    APPSTORE_CONFIG = {
      # SUBMIT is done by _shared/scripts/asc-appstore-submit.rb (the reliable direct-API path), NOT by
      # deliver — deliver's submit_for_review races on reviewSubmission state, chokes on stale empty
      # drafts (one open reviewSubmission max), and can't surface the Part XX ITA human gate. The
      # `release` lane builds/uploads/syncs the listing; the deploy runtime runs the submit script after
      # (RULE-DEPLOY-APPSTORE-AUTOSUBMIT-001). automatic_release stays true → auto-release to production
      # on Apple approval.
      submit_for_review:                  false,
      automatic_release:                  true,
      phased_release:                     false,
      skip_app_version_update:            false,
      reject_if_possible:                 true,
      force:                              true,
      precheck_include_in_app_purchases:  false,
      run_precheck_before_submit:         true,
      # export_compliance_uses_encryption:false ⇒ app declares NO non-exempt encryption (HTTPS/TLS only is
      # exempt, Category 5 Part 2 — matches app-profile app-content/export-compliance.yaml
      # itsappusesnonexemptencryption:false). Required to submit a never-submitted version, else deliver
      # aborts "Export compliance is required to submit". add_id_info_uses_idfa:false ⇒ no IDFA.
      submission_information:             { add_id_info_uses_idfa: false, export_compliance_uses_encryption: false }.freeze,
      app_review_information: {
        first_name: _c._secret("APPSTORE_REVIEW_FIRST_NAME") || _c._fork_prop("org.first.name") || "App",
        last_name:  _c._secret("APPSTORE_REVIEW_LAST_NAME")  || _c._fork_prop("org.last.name")  || "Team",
        phone:      _c._secret("APPSTORE_REVIEW_PHONE")      || _c._fork_prop("org.phone")      || "", # E0/T5: no mifos fallback — fork supplies org.phone
        email:      _c._secret("APPSTORE_REVIEW_EMAIL")      || _c._fork_prop("org.email")      || "", # E0/T5: no mifos fallback — fork supplies org.email
      }.freeze,
    }.freeze
  end

  # --------------------------------------------------------------------------
  # AndroidConfig — Play Store metadata path + per-flavor Play track policy.
  #
  # Artifact paths are NO LONGER stored as per-flavor constants — every lane
  # derives its APK/AAB path by convention through
  # `VariantResolver.resolve(flavor:, build_type:).apk_path` / `.aab_path`
  # (single derivation point, D9/AC-3). The former `BUILD_PATHS` symbolic
  # constant has been removed entirely.
  # --------------------------------------------------------------------------
  module AndroidConfig
    METADATA_PATH = "deployment/android/metadata".freeze
    # Primary Play Store listing locale (the metadata subdir under METADATA_PATH).
    # Android-owned — platform-wise config, do NOT borrow IosConfig for an Android lane.
    # Fork-configurable via gradle/fork.properties `store.primary.locale` (falls back to en-US).
    # Replaces the broken `FastlaneConfig::SHARED[:primary_locale]` reference (SHARED was undefined).
    # Read INLINE (not via the top-level `_fork_prop` helper): fastlane imports config.rb into its
    # FastFile class, so top-level defs become FastFile methods, unreachable from inside this nested
    # module at load time (NoMethodError). Self-contained read avoids that. (fix 2026-07-31)
    PRIMARY_LOCALE = begin
      _loc = "en-US"
      _fp = File.join(DEPLOYMENT_REPO_ROOT, "gradle", "fork.properties")
      if File.exist?(_fp)
        File.foreach(_fp) do |line|
          k, v = line.strip.split("=", 2)
          next unless k && v
          if k.strip == "store.primary.locale" && !v.strip.empty?
            _loc = v.strip
            break
          end
        end
      end
      _loc
    end.freeze

    # Per-flavor Play track destination — the mechanism that makes demo's Play
    # publication config, not a Ruby conditional (AC-12). Each flavor maps to
    # a Play track string (`"internal"` / `"beta"` / `"production"`) that
    # `deployInternal` passes to `upload_to_play_store`, or to `nil` when the
    # flavor is Firebase-only and MUST NOT be uploaded to Play. `deployInternal`
    # errors loudly on `nil` — a silent skip would hide a mis-configuration.
    #
    # Override order (highest → lowest):
    #   1. `PLAY_TRACKS_BY_FLAVOR` env var — CSV of `flavor=track` pairs
    #      (blank value ⇒ Firebase-only). Example:
    #        PLAY_TRACKS_BY_FLAVOR="prod=internal,demo="
    #      threads clean through workflow-dispatch env inputs.
    #   2. `android.play.tracks.by_flavor` `fork.properties` key — same CSV
    #      shape as the env var. Local override for a colleague fork.
    #   3. Default hash below: prod → "internal", demo → "internal".
    #
    # Any flavor not in the resolved hash is undefined ⇒ `deployInternal`
    # errors ("no Play track configured"). Add a flavor to `kmpFlavors {}` DSL
    # AND to this map (via env var / fork.properties / editing the default) —
    # the DSL alone is not enough because Play publication is a policy choice.
    DEFAULT_PLAY_TRACKS_BY_FLAVOR = {
      "prod" => "internal",
      "demo" => "internal",
    }.freeze

    # Map of flavor to Play track, parsed from a CSV secret or fork property.
    # Falls back to the shipped default when unset, so a fork with one flavor needs no config.
    def self.play_tracks_by_flavor
      csv = FastlaneConfig._secret("PLAY_TRACKS_BY_FLAVOR") ||
            FastlaneConfig._fork_prop("android.play.tracks.by_flavor")
      return DEFAULT_PLAY_TRACKS_BY_FLAVOR if csv.nil? || csv.strip.empty?
      csv.split(",").each_with_object({}) do |pair, acc|
        k, v = pair.split("=", 2).map { |s| s.to_s.strip }
        next if k.nil? || k.empty?
        acc[k] = (v.nil? || v.empty?) ? nil : v
      end
    end
  end

  # --------------------------------------------------------------------------
  # get_firebase_config — returns config hash for firebase_app_distribution
  # --------------------------------------------------------------------------
  def self.get_firebase_config(platform, flavor = :prod)
    _s     = SECRETS_DIR         # relative — for _secret_file (which prepends DEPLOYMENT_REPO_ROOT)
    _s_abs = SECRETS_DIR_ABS     # absolute — for paths passed directly to fastlane actions
    base = {
      serviceCredsFile: ENV["FIREBASE_SERVICE_ACCOUNT_PATH"] ||
                        File.join(DEPLOYMENT_REPO_ROOT, BuildSecrets.for(flavor: flavor).path(:firebase_service_account)),
      # Priority: ENV (CI) → fork.properties key (local) → nil. Mirrors the iOS app-id resolution
      # below (and the TestFlight groups line) so a fork's tester group (gradle/fork.properties
      # `firebase.groups`) is applied automatically. Without this, `firebase_app_distribution`
      # silently logs "No testers or groups passed in. Skipping this step." on every local deploy —
      # the build uploads but reaches ZERO testers — unless FIREBASE_GROUPS is exported per-deploy
      # (the long-standing "Firebase groups parameter ignored" known issue).
      groups: ENV["FIREBASE_GROUPS"] || _fork_prop("firebase.groups") || nil,
    }
    case platform
    when :ios
      # Priority: ENV (CI) → fork.properties key (local) → secrets/ file (vault) → ""
      app_id = if flavor == :demo
                 ENV["FIREBASE_IOS_DEMO_APP_ID"] ||
                   _fork_prop("firebase.ios.demo.app.id") ||
                   _secret_file("#{_s}/apple/firebase/ios_demo_app_id") || ""
               else
                 ENV["FIREBASE_IOS_PROD_APP_ID"] ||
                   _fork_prop("firebase.ios.prod.app.id") ||
                   ENV["FIREBASE_IOS_APP_ID"] ||
                   _fork_prop("firebase.ios.app.id") ||
                   _secret_file("#{_s}/apple/firebase/ios_prod_app_id") ||
                   _secret_file("#{_s}/apple/firebase/ios_app_id") || ""
               end
      base.merge(appId: app_id)
    when :android
      # by_flavor in LAYOUT.yaml maps prod→FIREBASE_ANDROID_APP_ID, demo→FIREBASE_ANDROID_DEMO_APP_ID
      # (env-first, file fallback) — collapses the old per-flavor if/else.
      app_id = BuildSecrets.for(flavor: flavor).value(:firebase_android_app_id) || ""
      base.merge(appId: app_id)
    else
      base
    end
  end

  # --------------------------------------------------------------------------
  # get_android_signing_config — returns signing config hash for buildAndSignApp
  # --------------------------------------------------------------------------
  # Reads UPLOAD keystore credentials (Play App Signing model — Play Console
  # verifies upload signature, then re-signs with the Google-held app signing key).
  def self.get_android_signing_config(options = {})
    # Resolve the keystore + its .properties through the LAYOUT resolver
    # (live-wins-else-sample) — NOT the raw SECRETS_DIR base, which misses the
    # `live/` segment after the secrets/{live,sample} split and yields a
    # non-existent `secrets/android/keystores/…` path (breaks local signed
    # builds). Matches the same resolver buildAndSignApp uses for its fallback
    # (BuildSecrets.for.path). (fix 2026-07-31)
    ks_rel     = BuildSecrets.for.path(:upload_keystore)
    ks_dir     = File.dirname(ks_rel)
    props_path = "#{ks_dir}/upload_keystore.properties"
    props = {}
    if File.exist?(File.join(DEPLOYMENT_REPO_ROOT, props_path))
      File.readlines(File.join(DEPLOYMENT_REPO_ROOT, props_path)).each do |line|
        k, v = line.strip.split("=", 2)
        props[k&.strip] = v&.strip if k && v
      end
    end

    # Read storeFile dynamically so forks can rename the keystore without
    # touching config.rb (storeFile key in upload_keystore.properties is canonical).
    # `fetch` only falls back when the key is ABSENT. secrets/LAYOUT.yaml can emit a `storeFile`
    # key whose vault alias is unresolvable ("not resolvable from vault → skip"), leaving the key
    # PRESENT but EMPTY — fetch then returns "" and this collapses to the bare directory:
    #   Keystore file '.../secrets/live/android/keystores' not found for signing config 'release'
    # which surfaces only after a full release build. Treat empty as absent (2026-09-04).
    store_file_name = props["storeFile"].to_s.strip
    store_file_name = "upload_keystore.keystore" if store_file_name.empty?
    default_jks = "#{ks_dir}/#{store_file_name}"

    # Resolved once, because the key password falls back to it (see below).
    store_password = options[:keystore_password] ||
                     ENV["KEYSTORE_PASSWORD"]    ||
                     props["storePassword"]      || ""

    {
      keystore_path:     options[:keystore_path]     ||
                         ENV["KEYSTORE_PATH"]        ||
                         default_jks,
      keystore_password: store_password,
      # Honor BOTH env conventions: KEY_ALIAS (CI pre_fastlane_script) AND KEYSTORE_ALIAS
      # (build.gradle.kts + buildAndSignApp / vault materialization). props (upload_keystore.properties)
      # is the file source. The literal "release" is a LAST-RESORT default — it is WRONG for keystores
      # whose alias differs (e.g. mifos-kmp-release); honoring the env prevents a silent wrong-alias
      # signing failure when the .properties file is absent (RULE-DEPLOY-VAULT-SIGNING-001).
      key_alias:         options[:key_alias]         ||
                         ENV["KEY_ALIAS"]            ||
                         ENV["KEYSTORE_ALIAS"]       ||
                         props["keyAlias"]           || "release",
      # Falls back to the STORE password, never to "". A PKCS12 keystore has no separate
      # key password — keytool discards `-keypass` at creation — so the store password IS
      # the key password. Measured with jarsigner against a real PKCS12 keystore: signing
      # succeeds when the key password is absent or equal to the store password, and FAILS
      # ("key associated with <alias> not a private key") when it is empty or different.
      # An empty default is therefore not a graceful degradation, it is one of the two
      # failure values — and it fails only in CI, after the local build has gone green.
      key_password:      options[:key_password]      ||
                         ENV["KEY_PASSWORD"]         ||
                         ENV["KEYSTORE_ALIAS_PASSWORD"] ||
                         props["keyPassword"]        || store_password,
    }
  end
end

# ─────────────────────────────────────────────────────────────────────────────
# Global Fastlane helpers — available in all platform lanes after import.
# ─────────────────────────────────────────────────────────────────────────────

# Normalize options hash: ensure symbol keys, return empty hash on nil.
def sanitize_options(options)
  return {} if options.nil?
  options.transform_keys(&:to_sym)
rescue
  options || {}
end

# Run Fastlane's setup_ci action when running on CI.
def setup_ci_if_needed
  # ALWAYS run fastlane's setup_ci — it creates a throwaway keychain and wires Match to it, so the
  # shared Distribution cert imports there, NOT the developer's login keychain. Makes the
  # ios-provisioning-profile Match repo self-sufficient headless/interactive/CI, zero per-member setup,
  # no keychain password. Gating on ENV["CI"] left local runs on the (locked) login keychain → Match
  # found no identity and tried to mint a new cert → Apple cert cap. (fix 2026-07-31)
  setup_ci(force: true)
end

# Delete any lingering fastlane_tmp keychain and RESTORE the developer's login keychain as the macOS
# default + search list. setup_ci creates fastlane_tmp, makes it DEFAULT, and unlocks it; if a LOCAL run
# dies before delete_keychain (a build error, or a hard kill), fastlane_tmp stays DEFAULT forever →
# unrelated apps (Bitwarden, Xcode) get endless "wants to use fastlane_tmp_keychain" password prompts.
# We keep setup_ci (local iOS Match needs the throwaway keychain — CI-gating it broke Match, fix
# 2026-07-31), and make cleanup GUARANTEED instead: called start-of-run (before_all, clears leftovers
# from a prior killed run) AND end-of-run (Fastfile after_all + error). Idempotent, failure-safe.
# GUARD on RUNNER_ENVIRONMENT, NOT ENV["CI"]: a SELF-HOSTED GitHub Actions runner (this repo runs one
# on a dev's Mac — `actions.runner.…local-mac.plist`) sets CI=true but is a PERSISTENT machine, so
# skipping cleanup there is exactly what leaves fastlane_tmp default → endless prompts. Skip ONLY a
# truly-ephemeral github-hosted runner (torn down anyway). (2026-08-08)
def cleanup_ci_keychain
  return if ENV["RUNNER_ENVIRONMENT"] == "github-hosted"  # ephemeral GH-hosted runner — nothing to protect
  home  = ENV["HOME"].to_s
  login = "#{home}/Library/Keychains/login.keychain-db"
  tmp   = "#{home}/Library/Keychains/fastlane_tmp_keychain-db"
  # SURGICAL — touch ONLY the keychain we created (fastlane_tmp). NEVER rewrite the whole search list
  # (`list-keychains -s login` would DROP the user's other keychains — system/custom) and NEVER force
  # the default unless fastlane_tmp actually holds it.
  # (1) Restore login as default ONLY IF fastlane_tmp is the CURRENT default (that's the prompt cause).
  if File.exist?(login) && `security default-keychain -d user 2>/dev/null`.to_s.include?("fastlane_tmp_keychain")
    system("security", "default-keychain", "-d", "user", "-s", login, out: File::NULL, err: File::NULL)
  end
  # (2) delete-keychain removes ONLY fastlane_tmp from the search list AND deletes its file (the rest of
  # the search list is left intact) — same as fastlane's own delete_keychain. rm is a last-resort fallback.
  if File.exist?(tmp)
    system("security", "delete-keychain", tmp, out: File::NULL, err: File::NULL) || (File.delete(tmp) rescue nil)
  end
rescue
  nil
end

# Load App Store Connect API key into lane context.
def load_api_key(options = {})
  cfg = FastlaneConfig::IosConfig::BUILD_CONFIG
  app_store_connect_api_key(
    key_id:       options[:appstore_key_id]    || cfg[:key_id],
    issuer_id:    options[:appstore_issuer_id] || cfg[:issuer_id],
    key_filepath: options[:key_filepath]       || cfg[:key_filepath],
    duration:     1200,
    in_house:     false,
  )
end

# Setup SSH agent + write match_ci_key to expected path for Match.
def with_ios_preamble(options = {})
  cfg = FastlaneConfig::IosConfig::BUILD_CONFIG
  ssh_key_path = options[:match_ssh_key_path] || cfg[:match_ssh_key_path]
  full_key_path = File.join(DEPLOYMENT_REPO_ROOT, ssh_key_path)

  if File.exist?(full_key_path)
    sh("chmod 600 '#{full_key_path}'") rescue nil
    # Write ~/.ssh/config entry so git can reach the Match repo
    ssh_config = File.expand_path("~/.ssh/config")
    entry = "\nHost github.com\n  IdentityFile #{full_key_path}\n  StrictHostKeyChecking no\n"
    unless File.exist?(ssh_config) && File.read(ssh_config).include?(full_key_path)
      FileUtils.mkdir_p(File.dirname(ssh_config))
      File.open(ssh_config, "a") { |f| f.write(entry) }
      File.chmod(0600, ssh_config) rescue nil
    end
  end

  # E6 — SwiftPM/XCFramework. The iOS app links the Kotlin `ComposeApp`
  # framework as an XCFramework (cmp-ios/Package.swift binary target) that the Xcode
  # `[KMP] Embed and Sign ComposeApp XCFramework` Run-Script build phase assembles +
  # signs + embeds on every archive (cmp-ios/scripts/embed-xcframework.sh). There is no
  # separate iOS dependency-install step — the per-variant
  # lanes assemble the XCFramework explicitly (see `assemble_ios_xcframework`) before
  # `build_app`, so a cold CI runner produces the framework the archive consumes.
end

# Assemble the Kotlin `ComposeApp` XCFramework for the given Xcode build type.
# Staging maps to the Release XCFramework slice (only *Debug is debuggable, matching
# the flavor-aware mapping in cmp-ios/scripts/embed-xcframework.sh).
# Registered by the `XCFramework("ComposeApp")` DSL in
# cmp-shared/build.gradle.kts (assembleComposeApp{Debug,Release}XCFramework).
def assemble_ios_xcframework(build_type = "release")
  xcf_type = build_type.to_s.downcase == "debug" ? "Debug" : "Release"
  sh("cd #{DEPLOYMENT_REPO_ROOT} && ./gradlew :cmp-shared:assembleComposeApp#{xcf_type}XCFramework")
end

# Prepare the keychain for Match — delegates to fastlane's standard ephemeral `setup_ci` keychain.
# NEVER the developer's login keychain and NO custom keychain: Match imports the shared cert from the
# fastlane-match provisioning repo into fastlane's throwaway `fastlane_tmp_keychain`, which is
# auto-created, isolated, and cleaned up — identical headless/interactive/CI, zero per-member setup.
# (2026-07-31)
def setup_ios_keychain(options = {})
  setup_ci(force: true)
end

# DSA-7 (RULE-DEPLOY-SIGNING-AUTO-001) — make Match signing fully NON-INTERACTIVE: no
# "<app> wants to use the fastlane_tmp_keychain-db keychain / enter the keychain password"
# dialog. After `match(readonly:true)` imports the shared Distribution cert + key into
# `setup_ci`'s throwaway keychain, add codesign (and the apple tooling) to the key's ACL
# partition list so `codesign`/`xcodebuild` use the key WITHOUT a per-signature prompt.
# `setup_ci` exports MATCH_KEYCHAIN_NAME/PASSWORD; the tmp keychain's password is empty by
# default. Fail-soft: a missing keychain (e.g. non-macOS/CI variance) just logs and continues —
# the signing itself is already validated by the match import above.
def suppress_codesign_keychain_prompt
  kc_name = ENV["MATCH_KEYCHAIN_NAME"].to_s.empty? ? "fastlane_tmp_keychain" : ENV["MATCH_KEYCHAIN_NAME"]
  kc_pass = ENV["MATCH_KEYCHAIN_PASSWORD"].to_s
  kc_path = kc_name.include?("/") ? kc_name : File.expand_path("~/Library/Keychains/#{kc_name}-db")
  kc_path = File.expand_path("~/Library/Keychains/#{kc_name}-db") unless File.exist?(kc_path)
  unless File.exist?(kc_path)
    UI.important("set-key-partition-list: keychain #{kc_name} not found — skipping (signing already imported).")
    return
  end
  sh(%(security set-key-partition-list -S apple-tool:,apple:,codesign: -s -k "#{kc_pass}" "#{kc_path}" >/dev/null 2>&1)) rescue nil
  UI.message("🔓 codesign key-partition-list set on #{kc_name} — signing is prompt-free.")
end

# Run Fastlane Match to fetch/refresh certificates and provisioning profiles.
# ── App-extension signing discovery (RULE-IOS-SIGNING-001; added 2026-09-01) ──
# A fork may ship app extensions (widgets, share, notification-service). EVERY such target
# needs its OWN provisioning profile — the extension bundle id is a DISTINCT App ID, so a
# profile for the app alone does NOT cover it. Xcode fails the archive with
# `"<Ext>" requires a provisioning profile with the <capability> feature` when the extension
# target is left unsigned — a failure that lands AFTER the full Kotlin/Native link (~40 min).
#
# Extension target names are FORK-SPECIFIC (CappyWidgets, FooShareExtension, …) and therefore
# can NOT be hardcoded here — they are DISCOVERED from the .xcodeproj, and their bundle ids
# resolved by substituting the `$(APP_BUNDLE_ID)` xcconfig var the template already owns.
# Returns [{name:, bundle_id:}]; [] (with a warning) if discovery fails — never raises.
def ios_extension_targets(project_path, app_identifier, configuration = "Release")
  require "xcodeproj"
  Xcodeproj::Project.open(project_path).native_targets.select { |t|
    t.product_type.to_s.include?("app-extension")
  }.map { |t|
    cfg = t.build_configurations.find { |c| c.name == configuration } || t.build_configurations.first
    raw = cfg&.build_settings&.dig("PRODUCT_BUNDLE_IDENTIFIER").to_s
    bid = raw.gsub("$(APP_BUNDLE_ID)", app_identifier.to_s)
    bid = "" if bid.include?("$(")            # unresolved var → fall back below
    bid = "#{app_identifier}.#{t.name.downcase}" if bid.empty?
    { name: t.name, bundle_id: bid }
  }
rescue StandardError => e
  UI.important("⚠️ app-extension discovery failed (#{e.message}) — signing the app target only")
  []
end

# Every bundle id needing a provisioning profile: the app + each app extension.
def ios_signing_identifiers(app_identifier, configuration = "Release")
  cfg = FastlaneConfig::IosConfig::BUILD_CONFIG
  exts = ios_extension_targets(cfg[:project_path], app_identifier, configuration)
  [app_identifier] + exts.map { |e| e[:bundle_id] }
end

# Fetch signing certificates and profiles via Fastlane Match over the provisioning repo.
# Match is the only sanctioned path — a hand-imported certificate is what breaks a
# keychain, per the no-ad-hoc-keychain rule.
def fetch_certificates_with_match(options = {})
  cfg = FastlaneConfig::IosConfig::BUILD_CONFIG
  ssh_key = File.join(DEPLOYMENT_REPO_ROOT, cfg[:match_ssh_key_path])

  # Match reads MATCH_PASSWORD automatically to decrypt the git-stored certs.
  # Priority: call option → ENV → BUILD_CONFIG (file-backed via secrets/live/apple/match/).
  match_pass = options[:match_password] || ENV["MATCH_PASSWORD"] || cfg[:match_password]
  ENV["MATCH_PASSWORD"] = match_pass.to_s if match_pass && ENV["MATCH_PASSWORD"].to_s.empty?

  # CERTIFICATES_PASSWORD — p12 import password (same as MATCH_PASSWORD in standard Match setups).
  certs_pass = options[:certificates_password] || ENV["CERTIFICATES_PASSWORD"] || cfg[:certificates_password]
  ENV["CERTIFICATES_PASSWORD"] = certs_pass.to_s if certs_pass && ENV["CERTIFICATES_PASSWORD"].to_s.empty?

  base = {
    type:                     options[:match_type]       || cfg[:match_type],
    # App + EVERY app-extension bundle id — match's app_identifier accepts an Array. Fetching
    # only the app leaves the extension profile uninstalled → archive hunts and fails.
    app_identifier:           ios_signing_identifiers(options[:app_identifier] || cfg[:app_identifier]),
    git_url:                  options[:match_git_url]    || cfg[:match_git_url],
    git_branch:               options[:match_git_branch] || cfg[:match_git_branch],
    git_private_key:          File.exist?(ssh_key) ? ssh_key : nil,
    include_all_certificates: true,
  }

  # Explicit override — a caller can pin readonly (e.g. bootstrap passes readonly:false to mint).
  if options.key?(:readonly)
    ro = options[:readonly]
    match(**base, readonly: ro, force: options.fetch(:force, !ro))
    return
  end

  # SINGLE SOURCE OF TRUTH — readonly ONLY, NO fallback to minting/renewal. All signing material comes
  # STRICTLY from the fastlane-match provisioning repo. A deploy lane NEVER creates or renews a
  # cert/profile on the Apple Dev Portal — that fallback is what tries to mint a new Distribution cert
  # and hits Apple's cert cap. If the repo lacks a valid, installable cert+key/profile for this app,
  # FAIL LOUDLY: fix the provisioning repo (its cert-renewal.sh — the ONLY sanctioned place a cert is
  # minted), not the deploy. Deliberate minting stays available via the explicit `readonly: false`
  # override above (cert-renewal / bootstrap only), never as an automatic deploy-time fallback. (2026-07-31)
  begin
    match(**base, readonly: true, force: false)
    suppress_codesign_keychain_prompt
    UI.success("✅ Signing pulled readonly from the fastlane-match provisioning repo (no Dev Portal, no mint).")
  rescue StandardError => e
    UI.user_error!(
      "Match (readonly) could not install a valid signing identity for #{base[:app_identifier]} " \
      "(#{e.message.to_s.lines.first&.strip}). The provisioning repo must carry a valid cert (with its " \
      "private key) + profile for this app. Fix it with the provisioning repo's cert-renewal.sh — NO " \
      "cert is minted from a deploy lane (no fallback)."
    )
  end
end

# Drives the auto-renew decision WITHOUT a Dev Portal call: returns true ONLY when there is no
# valid Apple Distribution signing identity available AND no unexpired AppStore profile on disk.
# `security find-identity -v` lists ONLY non-expired codesigning identities, so a hit means the
# cert is valid → stay readonly (no portal/PLA). Checks BOTH the legacy and the Xcode-16+/26
# provisioning-profile directories. Defaults to NOT-expired on any uncertainty, so a valid cert
# is never needlessly renewed (which would hit the PLA). (2026-06-22)
def match_assets_expired?(cfg, options = {}, min_days = 7)
  # 1) Valid (non-expired) Distribution cert in the keychain → certs are good.
  ids = `security find-identity -v -p codesigning 2>/dev/null`
  return false if ids =~ /Apple Distribution|iPhone Distribution|3rd Party Mac/

  # 2) No cert hit → fall back to an unexpired AppStore profile on disk (either profile dir).
  require "time"
  app_id = options[:app_identifier] || cfg[:app_identifier]
  dirs = [
    File.expand_path("~/Library/Developer/Xcode/UserData/Provisioning Profiles"),
    File.expand_path("~/Library/MobileDevice/Provisioning Profiles"),
  ]
  dirs.each do |dir|
    Dir.glob(File.join(dir, "*.mobileprovision")).each do |p|
      xml = `security cms -D -i "#{p}" 2>/dev/null`
      next unless xml.include?(app_id)
      next if xml =~ %r{<key>get-task-allow</key>\s*<true}  # skip Development profiles
      if xml =~ %r{<key>ExpirationDate</key>\s*<date>([^<]+)</date>}
        return false if Time.parse(Regexp.last_match(1)) > Time.now + (min_days * 86_400)
      end
    end
  end
  UI.important("No valid Apple Distribution identity or unexpired AppStore profile found — renewal needed.")
  true
rescue StandardError => e
  UI.important("match validity check failed (#{e.message}); assuming VALID (readonly) to avoid an unneeded Dev Portal/PLA call.")
  false
end

# Apple's App Store Connect API intermittently throws "A required agreement is missing or
# has expired" on TestFlight reads (latest_testflight_build_number / pilot) even when ALL
# agreements are current — a transient Apple-side bug seen across CI platforms and Xcode
# versions (fastlane#29860, surfaced ~2026-06). It clears on retry. Wrap the read so a flake
# RETRIES instead of failing the release. Versioning is unchanged: on success the real build
# number (and the LATEST_TESTFLIGHT_VERSION side-effect) is returned exactly as before; only
# after max_attempts genuine failures does it re-raise. NEVER falls back to a fabricated
# build number (that could be LOWER than the live TF build → Apple rejects the upload). (2026-06-23)
def latest_tf_build_number_resilient(max_attempts: 3, sleep_seconds: 20, **opts)
  attempts = 0
  begin
    latest_testflight_build_number(**opts)
  rescue StandardError => e
    attempts += 1
    raise if attempts >= max_attempts
    UI.important("⚠️ latest_testflight_build_number flaked (#{e.message.to_s.lines.first&.strip}); " \
                 "retry #{attempts}/#{max_attempts - 1} in #{sleep_seconds}s (Apple ASC transient, fastlane#29860)…")
    sleep(sleep_seconds)
    retry
  end
end

# ─────────────────────────────────────────────────────────────────────────────
# ensure_testflight_store_config — auto-sync + verify App Store Connect store
# config from fastlane config BEFORE any TestFlight upload/promotion.
# ─────────────────────────────────────────────────────────────────────────────
# Makes the whole TestFlight release fully automatic: no manual App Store Connect
# setup is ever required. Runs as a preflight in every TF lane (internal upload +
# external promotion, iOS + macOS):
#   1. VERIFIES the app record exists on ASC (fail-fast with a clear message instead
#      of dying deep in `pilot`/`deliver` — the "app not found" class).
#   2. SYNCS the app-level "Test Information" (BetaAppLocalization: description,
#      feedback email, marketing / privacy URLs) from TESTFLIGHT_CONFIG — creating it
#      when the app record has none (the `betaAppLocalizations not found` class) and
#      updating it to match config otherwise. External beta review REQUIRES this, and
#      pre-creating it means an external promotion never fails for a fresh app.
# Requires load_api_key to have run first (populates Spaceship::ConnectAPI.token).
# Idempotent + config-driven; opt out per-run with SKIP_TESTFLIGHT_STORE_SYNC=1.
def ensure_testflight_store_config(app_identifier:, config: nil, locale: nil)
  return UI.important("⏭  TestFlight store-config sync skipped (SKIP_TESTFLIGHT_STORE_SYNC=1).") \
    if ENV["SKIP_TESTFLIGHT_STORE_SYNC"].to_s == "1"

  require "spaceship"
  config ||= FastlaneConfig::IosConfig::TESTFLIGHT_CONFIG
  locale ||= (FastlaneConfig::IosConfig::BUILD_CONFIG[:primary_locale] rescue nil) || "en-US"
  UI.user_error!("No ASC API token — call load_api_key before ensure_testflight_store_config") unless Spaceship::ConnectAPI.token

  # ── 1. Verify the app record exists (fail-fast, clear guidance) ──
  app = begin
    Spaceship::ConnectAPI::App.find(app_identifier)
  rescue => e
    UI.important("⚠️  ASC app lookup flaked: #{e.message.to_s.lines.first&.strip}")
    nil
  end
  UI.user_error!(
    "App '#{app_identifier}' not found on App Store Connect. Create the app record " \
    "(same bundle id) in App Store Connect → My Apps → + → New App, or fix the " \
    "bundle id in gradle/libs.versions.toml, then re-run.",
  ) unless app

  # ── 2. Sync app-level Test Information (BetaAppLocalization) from config ──
  li        = (config[:localized_app_info] || {})[locale] || {}
  attrs     = {}
  desc      = (config[:beta_app_description]   || li[:description]).to_s
  email     = (config[:beta_app_feedback_email] || li[:feedback_email]).to_s
  marketing = (li[:marketing_url]      || li[:marketingUrl]).to_s
  privacy   = (li[:privacy_policy_url] || li[:privacyPolicyUrl]).to_s
  attrs[:description]      = desc      unless desc.empty?
  attrs[:feedbackEmail]    = email     unless email.empty?
  attrs[:marketingUrl]     = marketing unless marketing.empty?
  attrs[:privacyPolicyUrl] = privacy   unless privacy.empty?

  if attrs.empty?
    return UI.message("ℹ️  No TestFlight Test Information in config to sync for #{app_identifier}.")
  end

  begin
    existing = (app.get_beta_app_localizations || []).find { |l| l.locale == locale }
    if existing
      # Already present → external beta review is unblocked. Best-effort sync to config;
      # some Spaceship model versions expose no instance #update, so fall back to leaving
      # the existing (still-valid) values in place rather than failing the release.
      begin
        # Spaceship's ConnectAPI tunes client exposes the PLURAL client-level methods
        # (post_beta_app_localizations / patch_beta_app_localizations) — NOT a singular
        # #patch_beta_app_localization nor a model-level BetaAppLocalization.create (those
        # raise NoMethodError on spaceship 2.235, which was silently swallowing Test-Information
        # sync and blocking external beta review with "Beta App Description is missing").
        Spaceship::ConnectAPI.patch_beta_app_localizations(localization_id: existing.id, attributes: attrs)
        UI.success("🔄 Synced TestFlight Test Information (#{locale}) for #{app_identifier}.")
      rescue NoMethodError, NameError, ArgumentError
        UI.success("✅ TestFlight Test Information already present (#{locale}) for #{app_identifier} — external beta review unblocked.")
      end
    else
      Spaceship::ConnectAPI.post_beta_app_localizations(app_id: app.id, attributes: attrs.merge(locale: locale))
      UI.success("🆕 Created TestFlight Test Information (#{locale}) for #{app_identifier} — external beta review is now unblocked.")
    end
  rescue => e
    # Never let a Test-Information sync hiccup block an internal upload; surface loudly.
    UI.important("⚠️  Could not sync TestFlight Test Information for #{app_identifier}: " \
                 "#{e.message.to_s.lines.first&.strip}. Internal upload continues; " \
                 "external promotion may need it set on App Store Connect.")
  end

  # ── 3. Sync the app-level BetaAppReviewDetail (contact info) from config ──
  # External beta review REQUIRES this SEPARATE app-level object (contact name/email/phone +
  # demo account) — it is DISTINCT from the BetaAppLocalization synced above. pilot passes
  # beta_app_review_info only at submit time, so on an expired/failed build the review detail
  # is never created and external review stays blocked with no obvious cause. Create/patch it
  # here, independent of any build, so external promotion always has it. The betaAppReviewDetail
  # resource id EQUALS the app id (1:1), so patch(app_id: app.id) is the create-or-update. Phone
  # must be non-empty — Apple rejects an empty contact_phone on external submission. Best-effort.
  begin
    ri = (config[:beta_app_review_info] || {})
    brd_attrs = {
      contactFirstName:    ri[:contact_first_name].to_s,
      contactLastName:     ri[:contact_last_name].to_s,
      contactEmail:        ri[:contact_email].to_s,
      contactPhone:        ri[:contact_phone].to_s,
      demoAccountRequired: !!ri[:demo_account_required],
    }
    unless brd_attrs[:contactEmail].empty? || brd_attrs[:contactPhone].empty?
      Spaceship::ConnectAPI.patch_beta_app_review_detail(app_id: app.id, attributes: brd_attrs)
      UI.success("🔄 Synced TestFlight Beta App Review contact info for #{app_identifier} — external beta review unblocked.")
    else
      UI.important("⚠️  Skipped Beta App Review contact sync for #{app_identifier}: contact_email/contact_phone empty in TESTFLIGHT_CONFIG[:beta_app_review_info] (org.email / org.phone). External review will reject an empty contact.")
    end
  rescue => e
    UI.important("⚠️  Could not sync Beta App Review contact info for #{app_identifier}: " \
                 "#{e.message.to_s.lines.first&.strip}. External promotion may need it set on App Store Connect.")
  end
end

# Ensure the App Store Connect TestFlight tester GROUPS exist (from gradle/fork.properties `apple.testers.*`)
# so an uploaded build reaches testers instead of sitting undistributed. Fully config-driven — NO tester
# email lists live here: iOS INTERNAL testers are Apple-team members (managed in ASC → Users & Access; the
# internal group has has_access_to_all_builds=true so they get EVERY build instantly, no review), and iOS
# EXTERNAL testers self-join via the group's PUBLIC LINK (one Apple beta review, then every build). The
# public link is the ASC analog of the Play/Firebase tester group. Idempotent, best-effort (rescued — a
# hiccup NEVER blocks the upload). Requires load_api_key first. Opt out: SKIP_TESTFLIGHT_TESTER_SYNC=1.
# Shared by iOS + macOS TestFlight lanes.
def sync_testflight_testers(app_identifier:, config: nil)
  return UI.important("⏭  TestFlight tester sync skipped (SKIP_TESTFLIGHT_TESTER_SYNC=1).") \
    if ENV["SKIP_TESTFLIGHT_TESTER_SYNC"].to_s == "1"

  require "spaceship"
  cfg = config || FastlaneConfig::IosConfig::TESTERS[:ios]
  UI.user_error!("No ASC API token — call load_api_key before sync_testflight_testers") unless Spaceship::ConnectAPI.token

  app = begin
    Spaceship::ConnectAPI::App.find(app_identifier)
  rescue => e
    return UI.important("⚠️  ASC app lookup flaked for tester sync: #{e.message.to_s.lines.first&.strip}")
  end
  return UI.important("⚠️  App '#{app_identifier}' not found on ASC — skipping tester sync.") unless app

  # internal group (team-only, auto every build) + external group (self-join public link).
  specs = []
  specs << { name: cfg[:internal_group], internal: true,  public: false } if cfg[:internal_group].to_s != ""
  specs << { name: cfg[:external_group], internal: false, public: cfg[:external_public_link] } if cfg[:external_group].to_s != ""
  return UI.message("ℹ️  No apple.testers.* groups in fork.properties — skipping ASC tester sync.") if specs.empty?

  specs.each do |gs|
    begin
      # App Store Connect scopes beta-group name uniqueness WITHIN a group type, not across the
      # app: an INTERNAL and an EXTERNAL group may legitimately share one name (verified live —
      # `sensei-app-testers` exists under both Internal Testing and External Testing on a shipped
      # fork). So the lookup MUST key on (name, is_internal_group). Filtering by name alone and
      # taking .first returns whichever type ASC lists first (the internal one), which made the
      # external spec resolve to the INTERNAL group, skip creation via `grp ||=`, and then log
      # itself as "external". The external group never existed, and promoteToExternalBeta later
      # died on Apple's `Cannot add internal group to a build.`
      grp = (app.get_beta_groups(filter: { name: gs[:name] }) || [])
              .find { |g| !!g.is_internal_group == !!gs[:internal] }
      grp ||= app.create_beta_group(
        group_name:                gs[:name],
        is_internal_group:         gs[:internal],
        has_access_to_all_builds:  gs[:internal] ? true : nil,
        public_link_enabled:       gs[:public] ? true : nil,
        public_link_limit_enabled: gs[:public] ? true : nil,
        public_link_limit:         gs[:public] ? 10_000 : nil,
      )
      kind = gs[:internal] ? "internal (team, every build)" : "external (public-link self-join, after review)"
      UI.success("👥 ASC beta group ensured: '#{gs[:name]}' — #{kind}.")
      if !gs[:internal] && gs[:public]
        link = (grp.respond_to?(:public_link) && grp.public_link) || nil
        UI.success("🔗 Public TestFlight join link for '#{gs[:name]}': #{link}") if link
      end

      # Add the org-shared external tester roster (emails) to the EXTERNAL group so named testers
      # receive an invite, not only public-link self-joiners. Idempotent + best-effort per email —
      # a tester that already exists (globally or in the group) is a no-op, never an error. Roster
      # SoT: app-profile apple.testers.external_emails ← _org/company.yaml. Internal group is team-
      # only (members must be added by Apple-ID in App Store Connect), so we never push emails there.
      if !gs[:internal]
        roster = (cfg[:external_emails] || [])
        unless roster.empty?
          begin
            # `Spaceship::ConnectAPI.create_beta_tester` DOES NOT EXIST (verified against fastlane
            # 2.239.0 — it raises `undefined method 'create_beta_tester'` for every email while the
            # surrounding rescue downgrades it to a warning, so the lane reports success having added
            # nobody). `POST /v1/bulkBetaTesterAssignments` is ALSO retired by Apple ("The resource
            # 'v1/bulkBetaTesterAssignments' does not exist") even though spaceship still ships the
            # helper — and that message contains "exist", so an /already|exist/ rescue silently
            # swallows it too. The live endpoint is `POST /v1/betaTesters` with the betaGroups
            # relationship. A tester that already exists ACCOUNT-WIDE (an org-shared roster is
            # already on sibling apps) 409s there — look that tester up and attach the EXISTING id.
            testers = roster.map do |em|
              fn = em.split("@").first.to_s.gsub(/[^A-Za-z0-9]/, " ").strip[0, 30]
              fn = "Tester" if fn.empty?
              # last_name is a required ASC field but carries no product meaning — keep it brand-neutral
              # (never a fork's initials) so a synced fork doesn't stamp its testers with another org's name.
              { email: em, firstName: fn, lastName: "Tester" }
            end
            testers.each do |t|
              begin
                Spaceship::ConnectAPI.post_beta_tester_assignment(
                  beta_group_ids: [grp.id],
                  attributes: { email: t[:email], firstName: t[:firstName], lastName: t[:lastName] },
                )
              rescue => e
                existing = (Spaceship::ConnectAPI.get_beta_testers(filter: { email: t[:email] })
                                                 .to_models.first rescue nil)
                if existing
                  grp.add_beta_testers(beta_tester_ids: [existing.id])
                else
                  UI.important("   ⚠️  #{t[:email]}: #{e.message.to_s.lines.first&.strip}")
                end
              end
            end
            # VERIFY, never assume: read the group's membership back and report which roster emails
            # are actually present. Both broken APIs above reported success while adding nobody.
            present = begin
              Spaceship::ConnectAPI.get_beta_testers(filter: { betaGroups: grp.id }, limit: 200)
                                   .to_models.map { |t| t.email.to_s.downcase }
            rescue => e
              UI.important("   ⚠️  could not read back '#{gs[:name]}' membership: #{e.message.to_s.lines.first&.strip}")
              nil
            end
            if present
              hit  = roster.select { |em| present.include?(em.downcase) }
              miss = roster - hit
              UI.success("   ➕ external testers on '#{gs[:name]}': #{hit.size}/#{roster.size} present")
              UI.important("   ⚠️  NOT on '#{gs[:name]}': #{miss.join(', ')}") unless miss.empty?
            end
          rescue => e
            UI.important("   ⚠️  could not assign external tester roster to '#{gs[:name]}': #{e.message.to_s.lines.first&.strip}")
          end
        end
      end
    rescue => e
      UI.important("⚠️  ASC group '#{gs[:name]}' sync hiccuped: #{e.message.to_s.lines.first&.strip}. " \
                   "Upload continues — verify in App Store Connect → TestFlight if needed.")
    end
  end
end

# Attach the configured Google Group (fork.properties play.testers.<track>.googlegroup) to a Play testing
# track, so every upload to that track reaches the group's members. UNLIKE ASC, this binding PERSISTS on the
# track — a one-time (idempotent) setup, re-asserted here so a fresh app/track is wired with zero Console
# clicks. Manage membership in the Google Group itself. Best-effort (rescued — never blocks the AAB upload);
# no-op when the fork leaves the group blank. `track` = internal|closed. Opt out: SKIP_PLAY_TESTER_SYNC=1.
def sync_play_testers(package_name:, track:, json_key: nil, google_group: nil)
  return if ENV["SKIP_PLAY_TESTER_SYNC"].to_s == "1"
  key = track.to_s == "closed" ? :closed_googlegroup : :internal_googlegroup
  group = google_group || FastlaneConfig::IosConfig::TESTERS[:play][key]
  return UI.message("ℹ️  No play.testers.#{track}.googlegroup in fork.properties — skipping Play tester sync.") \
    if group.nil? || group.to_s.strip.empty?
  json_key ||= FastlaneConfig::ProjectConfig::ANDROID[:play_store_json_key]
  json_key = File.join(DEPLOYMENT_REPO_ROOT, json_key) unless json_key.to_s.start_with?("/")
  return UI.important("⚠️  Play service-account json not found (#{json_key}) — skipping Play tester sync.") \
    unless File.exist?(json_key.to_s)

  begin
    require "google/apis/androidpublisher_v3"
    require "googleauth"
    svc = Google::Apis::AndroidpublisherV3::AndroidPublisherService.new
    svc.authorization = Google::Auth::ServiceAccountCredentials.make_creds(
      json_key_io: File.open(json_key),
      scope: "https://www.googleapis.com/auth/androidpublisher",
    )
    edit = svc.insert_edit(package_name)
    testers = Google::Apis::AndroidpublisherV3::Testers.new(google_groups: [group])
    svc.update_edit_tester(package_name, edit.id, track.to_s, testers)
    svc.commit_edit(package_name, edit.id)
    UI.success("👥 Play '#{track}' track testers → Google Group '#{group}' (persists for every future upload).")
  rescue => e
    UI.important("⚠️  Play tester sync (#{track} → #{group}) hiccuped: #{e.message.to_s.lines.first&.strip}. " \
                 "Upload continues — set the group once in Play Console → Testing → #{track} → Testers if needed.")
  end
end

# Revoke iOS Distribution certificates from Apple Developer Portal to free slots for
# a fresh cert. Strategy (in order):
#   1. Revoke all truly expired certs first (safe, they're already unusable).
#   2. Ensure at least `min_free_slots` (default 2) slots are available. If we have
#      fewer free slots than required, revoke the soonest-expiring cert within
#      `revoke_within_months` months (default 12). This handles two edge cases:
#       a. At the 3-cert maximum (0 free slots) — always triggers.
#       b. At 2/3 (1 free slot) — triggers when min_free_slots >= 2. Needed because
#          the legacy `cert` action can disagree with ConnectAPI on the cert count
#          (different Apple backends) and fail with "maximum" even at 2/3.
#
# Requires load_api_key to have run first (populates Spaceship::ConnectAPI.token).
def revoke_expired_distribution_certs(revoke_within_months: 12, min_free_slots: 1)
  require "spaceship"
  UI.user_error!("No ASC API token — call load_api_key before revoke_expired_distribution_certs") unless Spaceship::ConnectAPI.token

  max_dist_certs = 3

  dist_certs = Spaceship::ConnectAPI::Certificate.all.select do |c|
    c.certificate_type == "DISTRIBUTION" || c.certificate_type == "IOS_DISTRIBUTION"
  end

  # Step 1 — revoke already-expired certs.
  now = Time.now
  expired, valid = dist_certs.partition do |c|
    c.expiration_date.to_s.empty? ? false : Time.parse(c.expiration_date) < now
  end

  expired.each do |cert|
    UI.important("Revoking expired Distribution cert #{cert.id} (expired #{cert.expiration_date})")
    cert.delete!
    UI.success("Revoked #{cert.id}")
  end

  remaining = valid.size
  free_slots = max_dist_certs - remaining
  UI.message("Distribution certs after expired-revoke: #{remaining}/#{max_dist_certs} (#{free_slots} free)")
  return if free_slots >= min_free_slots

  # Step 2 — need more free slots; revoke the soonest-expiring cert within the window.
  cutoff = now + (revoke_within_months * 30 * 24 * 3600)
  candidate = valid.min_by { |c| Time.parse(c.expiration_date) }

  if candidate && Time.parse(candidate.expiration_date) < cutoff
    UI.important("Need #{min_free_slots} free slot(s), have #{free_slots} — revoking soonest-expiring cert " \
                 "#{candidate.id} (expires #{candidate.expiration_date}) to make room")
    candidate.delete!
    UI.success("Revoked #{candidate.id}")
  else
    UI.user_error!(
      "Need #{min_free_slots} free Distribution cert slot(s) but all #{remaining} remaining certs " \
      "expire beyond #{revoke_within_months} months. Revoke one manually at " \
      "https://developer.apple.com/account/resources/certificates/list then re-run renewCerts."
    )
  end
end

# Clone (or update) the Match git repo, delete all distribution cert/key files and any
# provisioning profiles tied to our app identifier, then push. After this,
# fetch_certificates_with_match sees an empty cert directory and creates a fresh
# Distribution certificate via the ASC API.
#
# PERSISTENT CLONE STRATEGY
#   The clone lives at secrets/live/apple/match/ios-provisioning-profile/ (gitignored).
#   • Local / colleagues: clone is reused across runs (git fetch + reset — fast).
#   • GitHub Actions: starts clean each run, falls back to git clone --depth 1.
#
#   Colleague first-time setup:
#     GIT_SSH_COMMAND="ssh -i secrets/live/apple/match/match_ci_key -o StrictHostKeyChecking=no" \
#       git clone --depth 1 git@github.com:openMF/ios-provisioning-profile.git \
#       secrets/live/apple/match/ios-provisioning-profile
#   Subsequent runs: the lane updates the clone automatically.
#
# Safe: does NOT revoke from Apple Developer Portal (expired certs are already unusable).
# Idempotent: no-op if the cert directory is already empty.
def purge_match_distribution_certs
  cfg        = FastlaneConfig::IosConfig::BUILD_CONFIG
  ssh_key    = File.join(DEPLOYMENT_REPO_ROOT, cfg[:match_ssh_key_path])
  git_url    = cfg[:match_git_url]    || ""
  git_branch = cfg[:match_git_branch] || "master"
  bundle_id  = cfg[:app_identifier]  || ""

  git_env = {"GIT_SSH_COMMAND" => "ssh -i #{ssh_key} -o StrictHostKeyChecking=no"}

  # Persistent clone — reused locally, cloned fresh on CI (secrets/ is gitignored).
  match_local = File.join(DEPLOYMENT_REPO_ROOT, "secrets", "apple", "match", "ios-provisioning-profile")

  if Dir.exist?(File.join(match_local, ".git"))
    UI.message("Updating existing Match repo clone at #{match_local}...")
    sh(git_env, "git -C #{match_local} fetch --depth 1 origin #{git_branch}")
    sh(git_env, "git -C #{match_local} reset --hard origin/#{git_branch}")
  else
    UI.message("Cloning Match repo (shallow) into #{match_local}...")
    FileUtils.mkdir_p(File.dirname(match_local))
    sh(git_env, "git clone --depth 1 --single-branch --branch #{git_branch} #{git_url} #{match_local}")
  end

  sh("git -C #{match_local} config user.email 'match@fastlane.tools'")
  sh("git -C #{match_local} config user.name 'fastlane match renewal'")

  deleted = []

  # Remove every distribution cert + private key (they're tied to the revoked cert).
  Dir[File.join(match_local, "certs", "distribution", "*")].sort.each do |abs|
    rel = abs.delete_prefix("#{match_local}/")
    sh("git -C #{match_local} rm -f -- #{rel.shellescape}")
    deleted << File.basename(abs)
  end

  # Remove provisioning profiles for our app (they reference the revoked cert).
  Dir[File.join(match_local, "profiles", "**", "*")].select { |f| File.file?(f) }.each do |abs|
    next unless File.basename(abs).include?(bundle_id.tr(".", "_").tr("-", "_")) ||
                File.basename(abs).downcase.include?(bundle_id.downcase)
    rel = abs.delete_prefix("#{match_local}/")
    sh("git -C #{match_local} rm -f -- #{rel.shellescape}")
    deleted << File.basename(abs)
  end

  if deleted.any?
    sh("git -C #{match_local} commit -m 'chore(certs): purge expired Distribution cert + stale profiles'")
    sh(git_env, "git -C #{match_local} push origin #{git_branch}")
    UI.success("Purged from Match repo: #{deleted.join(', ')}")
  else
    UI.important("Match repo already clean — nothing to purge")
  end
end

# Delete ALL provisioning profiles for our bundle ID from Apple's Developer Portal.
# Required when previous Match runs leave duplicate "match AdHoc …" / "match AppStore …"
# profiles that trigger a 409 ENTITY_ERROR on the next profile-create call.
def purge_apple_portal_profiles
  require "spaceship"
  UI.user_error!("No ASC API token — call load_api_key before purge_apple_portal_profiles") unless Spaceship::ConnectAPI.token

  bundle_id = FastlaneConfig::IosConfig::BUILD_CONFIG[:app_identifier] || ""

  %w[adhoc appstore].each do |type|
    profile_name = "match #{type == 'adhoc' ? 'AdHoc' : 'AppStore'} #{bundle_id}"
    profiles = Spaceship::ConnectAPI::Profile.all.select { |p| p.name == profile_name }
    if profiles.empty?
      UI.message("No portal profiles found with name '#{profile_name}'")
      next
    end
    UI.important("Found #{profiles.size} portal profile(s) named '#{profile_name}' — deleting for clean recreation")
    profiles.each do |profile|
      profile.delete!
      UI.success("Deleted portal profile #{profile.id} (#{profile.name})")
    end
  end
end

# Get app version from gradle-generated version.txt, optionally sanitized for App Store.
def get_version_from_gradle(sanitize_for_appstore: false)
  sh("./gradlew versionFile 2>/dev/null || true") rescue nil
  version = VersionHelpers.gradle_version
  sanitize_for_appstore ? VersionHelpers.appstore_sanitize(version) : version
end

# Generate release notes from recent git merge commits.
# Play Store enforces a 500-char cap per locale; truncate with ellipsis if needed.
def generateReleaseNote
  notes = changelog_from_git_commits(
    commits_count:          15,
    pretty:                 "- %s",
    merge_commit_filtering: "only_include_merges",
    quiet:                  true,
  )
  notes.length <= 500 ? notes : notes[0, 497] + "..."
rescue
  "#{ForkIdentity::APP_DISPLAY_NAME} update"
end

# Build iOS project using build_app (gym). Supports ad-hoc and app-store exports.
# `configuration:` was hardcoded pre-Phase-2 — parameterized so lanes can pass
# the resolved build-type (e.g. "Staging" → the Staging xcconfig) that
# `VariantResolver.resolve(...).ios_scheme` was built against; defaults to
# "Release" to preserve pre-Phase-2 caller behavior.
def build_ios_project(options = {})
  cfg = FastlaneConfig::IosConfig::BUILD_CONFIG
  build_app(
    scheme:           options[:scheme]        || cfg[:scheme],
    # E6 — archive from the `.xcodeproj` (SwiftPM/XCFramework), not a generated
    # `.xcworkspace`. The `[KMP] Embed and Sign ComposeApp XCFramework`
    # Run-Script phase builds + embeds the Kotlin framework during this archive.
    project:          options[:project]       || cfg[:project_path],
    configuration:    options[:configuration] || "Release",
    output_name:      "iosApp.ipa",
    output_directory: File.join(DEPLOYMENT_REPO_ROOT, "cmp-ios/build"),
    export_method:    options[:export_method] || "app-store",
    include_bitcode:  false,
    include_symbols:  true,
  )
end

# Build signed iOS IPA — wrapper around build_ios_project with export method resolved.
def build_signed_ios(options = {})
  export = options[:match_type] == "adhoc" ? "ad-hoc" : "app-store"
  build_ios_project(options.merge(export_method: export))
end

# Increment iOS build number by fetching the latest release from Firebase App Distribution.
# firebase_app_id: pass the flavor-specific app ID; falls back to env vars.
def increment_version(serviceCredsFile:, firebase_app_id: nil)
  cfg = FastlaneConfig::IosConfig::BUILD_CONFIG
  app_id = firebase_app_id ||
           ENV["FIREBASE_IOS_PROD_APP_ID"] ||
           ENV["FIREBASE_IOS_APP_ID"] || ""
  if app_id.empty?
    UI.important("⚠️ Firebase iOS app ID not set — skipping build number increment from Firebase")
    return
  end
  latest = firebase_app_distribution_get_latest_release(
    app:                      app_id,
    service_credentials_file: serviceCredsFile,
  )
  build_num = (latest&.dig(:buildVersion) || 0).to_i + 1
  increment_build_number(xcodeproj: cfg[:project_path], build_number: build_num)
rescue => e
  UI.important("⚠️ Firebase latest release fetch failed: #{e.message}. Using current build number.")
end

# Compute and export VERSION_NAME / VERSION_CODE for Android builds.
# Must be called before buildAndSignApp so Gradle picks up the injected values.
def generateVersion(platform: "firebase", **config)
  # Give reckon the full tag history. The CI checkout is shallow, so without this reckon can't
  # see the release tags and versionFile falls back to 0.0.1 / 1.0.0 inconsistently. Best-effort:
  # a fresh clone may already be complete (--unshallow then errors → tolerated). (2026-06-22 fix)
  sh("cd #{DEPLOYMENT_REPO_ROOT} && git fetch --tags --force --quiet 2>/dev/null; git fetch --unshallow --quiet 2>/dev/null || true") rescue nil
  # Run versionFile and SURFACE failures (was `2>/dev/null || true`, which hid reckon errors and
  # silently produced a 1.0.0 build). version.txt is written to the repo root by the task.
  begin
    # --no-configuration-cache: config-cache is ON for this project, so a plain `versionFile`
    # gets served a stale cached project.version (1.0.0) and overwrites the committed version.txt.
    # Disabling it forces reckon to re-evaluate fresh against the just-fetched tags. (2026-06-22)
    sh("cd #{DEPLOYMENT_REPO_ROOT} && ./gradlew versionFile --no-configuration-cache --console=plain")
  rescue => e
    UI.important("⚠️ ./gradlew versionFile failed: #{e.message.to_s.lines.first&.strip}")
  end
  version_name = VersionHelpers.gradle_version
  UI.important("⚠️ versionName fell back to '#{version_name}' — reckon/versionFile did not produce version.txt; check git tag history in CI.") if %w[1.0.0 0.0.0].include?(version_name)
  # VERSION_CODE_OVERRIDE: pre-set ENV (e.g. by act --env-file) wins over
  # git-rev-list. Lets iteration runs bump past the last successful upload
  # without a real git commit on the source repo each time.
  override = ENV["VERSION_CODE_OVERRIDE"].to_s.strip
  version_code = if !override.empty?
                   override.to_i
                 else
                   sh("git rev-list --count HEAD 2>/dev/null || echo 1").strip.to_i rescue 1
                 end

  ENV["VERSION_NAME"] = version_name
  ENV["VERSION_CODE"] = version_code.to_s

  UI.message("📦 Version: #{version_name} (#{version_code})")
  { version_name: version_name, version_code: version_code }
end

# Build and sign an Android APK/AAB via Gradle with signing config injected.
# Uses sh() + absolute gradlew path because the multi-module repo layout has
# gradlew at root, not inside cmp-android/ — incompatible with gradle() action's
# project_dir expectation.
def buildAndSignApp(taskName:, buildType: "Release", **signing_config)
  # DRIFT FIX: was hardcoded "secrets/live/android/keystores/upload_keystore.keystore"
  # (flat pre-restructure path — broke after secrets/{live,sample} split). CI passes
  # keystore_path from `build-secrets path upload_keystore`; the local-run fallback
  # must resolve through the SAME LAYOUT resolver (live-wins-else-sample), never hardcode.
  keystore = signing_config[:keystore_path] || BuildSecrets.for.path(:upload_keystore)
  # Resolve to absolute WITHOUT doubling: File.join(repo_root, <absolute>) concatenates into a
  # doubled, non-existent path — only prepend the repo root when `keystore` is relative.
  keystore_abs = keystore.start_with?("/") ? keystore : File.expand_path(File.join(DEPLOYMENT_REPO_ROOT, keystore))
  gradlew    = File.join(DEPLOYMENT_REPO_ROOT, "gradlew")
  full_task  = ":cmp-android:#{taskName}#{buildType}"

  # Signing goes through the ENVIRONMENT, never `-P` CLI args (RULE-DEPLOY-SIGNING-NO-CLI-LEAK-001).
  # Gradle CLI args are echoed by fastlane's `sh` AND visible in `ps` — passing
  # `-Pandroid.injected.signing.store.password=…` leaked the store password to a build log on
  # 2026-08-08. cmp-android/build.gradle.kts's release signingConfig reads exactly these env vars via
  # System.getenv; the gradle child inherits the env, but it is NOT printed in the command line.
  # Only pre-existing env values are honored as a fallback (e.g. CI's pre_fastlane_script exports).
  ENV["KEYSTORE_PATH"]           = keystore_abs
  ENV["KEYSTORE_PASSWORD"]       = signing_config[:keystore_password] || ENV["KEYSTORE_PASSWORD"] || ""
  ENV["KEYSTORE_ALIAS"]          = signing_config[:key_alias]         || ENV["KEYSTORE_ALIAS"] || "release"
  # Never "" — an empty key password is a measured signing FAILURE, not an unset value.
  # PKCS12 has no separate key password, so the store password is the correct last resort.
  ENV["KEYSTORE_ALIAS_PASSWORD"] = signing_config[:key_password]      || ENV["KEYSTORE_ALIAS_PASSWORD"] || ENV["KEYSTORE_PASSWORD"]

  # -p tells Gradle to use repo root as project dir, overriding whatever cwd
  # Fastlane sets (deployment/fastlane/) when running the lane. NO signing on the command line.
  sh(
    gradlew, "-p", DEPLOYMENT_REPO_ROOT, full_task,
    "-PVERSION_NAME=#{ENV['VERSION_NAME'] || '1.0.0'}",
    "-PVERSION_CODE=#{ENV['VERSION_CODE'] || '1'}",
  )
end
