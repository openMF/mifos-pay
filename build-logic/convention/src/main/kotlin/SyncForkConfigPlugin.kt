/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import org.convention.LicenseHeader
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import org.gradle.work.DisableCachingByDefault
import org.yaml.snakeyaml.Yaml
import java.io.File

/**
 * `app.yaml#security.screen_capture_allowed_by_default` → `AppSecurityConfig.SCREEN_CAPTURE_ALLOWED_BY_DEFAULT`.
 * Absent or unparseable keeps the secure default, `false`: only an explicit `true` allows capture.
 */
internal fun screenCaptureAllowedByDefault(security: Map<*, *>?): Boolean =
    security?.get("screen_capture_allowed_by_default")?.toString()?.trim()?.lowercase() == "true"

/**
 * `app.yaml#security.signed_in_by_default` → `AppSecurityConfig.SIGNED_IN_BY_DEFAULT`.
 *
 * The OPPOSITE missing-key rule from [screenCaptureAllowedByDefault], on purpose: absent means signed
 * in. The template has no login, so `true` is its behaviour, and a fork whose `app.yaml` predates the
 * key must keep it — reading absent as `false` would land a no-login app on a login screen. Only an
 * explicit `false` opts a fork with real auth into a signed-out first launch.
 */
internal fun signedInByDefault(security: Map<*, *>?): Boolean =
    security?.get("signed_in_by_default")?.toString()?.trim()?.lowercase() != "false"

/**
 * `app.yaml#security.first_run_onboarding` → `AppSecurityConfig.FIRST_RUN_ONBOARDING`, the starting value of
 * `UserData.firstTimeUser`. Absent means `false`: onboarding (feature/onboarding) is opt-in, so a fresh
 * install is not routed to it unless app.yaml asks. With `true`, `RootNavViewModel` emits `ShowOnboarding`
 * once, until the onboarding screen calls `setFirstTimeState(false)`.
 */
internal fun firstRunOnboarding(security: Map<*, *>?): Boolean =
    security?.get("first_run_onboarding")?.toString()?.trim()?.lowercase() == "true"

/**
 * Makes [target]'s parent dir exist when its platform's `metadata/` root does, and reports whether it
 * may be written. Every `deployment/.../metadata` tree is gitignored and the template tracks only some of its
 * sub-dirs, so a fresh clone has no `review_information/` or `changelogs/` — skipping on a missing
 * PARENT silently dropped the App Review contact and the Play changelog on every CI run. A missing
 * metadata ROOT still skips: that platform is not shipped by this fork.
 */
internal fun ensureMetadataParent(target: File): Boolean {
    val parent = target.parentFile ?: return false
    if (parent.isDirectory) return true
    var root: File? = parent
    while (root != null && root.name != "metadata") root = root.parentFile
    return root != null && root.isDirectory && parent.mkdirs()
}

/**
 * Registers the `syncForkConfig` task on whichever project applies this plugin.
 *
 * Single source of truth:
 *   gradle/fork.properties    — deployment identity + store metadata (gitignored)
 *   gradle/libs.versions.toml — 5 build-time Gradle values (appId, appDisplayName,
 *                               desktopAppName, projectName, iosTeamId). Module namespaces are a
 *                               fixed framework label (org.convention.BASE_MODULE_NAMESPACE), not here.
 *
 * Propagates ALL fork identity to:
 *   - cmp-ios/Configuration/Config.xcconfig
 *   - local.properties          (Fastlane bridge — gitignored)
 *   - gradle.properties
 *   - All store metadata .txt files (App Store iOS, App Store macOS, Play Store)
 *   - Platform app icons (from app-profile/icons/)
 *
 * Apply from root build.gradle.kts:
 *   plugins { id("org.convention.fork.sync-config") }
 *
 * Then run:
 *   ./gradlew syncForkConfig
 */
class SyncForkConfigPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.register<SyncForkConfigTask>("syncForkConfig") {
            group = "fork"
            description = "Sync fork identity from fork.properties + libs.versions.toml to all platform config files."
            // Always re-run — reads gitignored fork.properties, not tracked by Gradle.
            outputs.upToDateWhen { false }
            projectRootDir.set(target.layout.projectDirectory)
            iconSourceDir.set(target.layout.projectDirectory.dir("app-profile/icons"))
            iosAppIconDir.set(target.layout.projectDirectory.dir("cmp-ios/iosApp/Assets.xcassets/AppIcon.appiconset"))
            jsResourcesDir.set(target.layout.projectDirectory.dir("cmp-web/src/jsMain/resources"))
            wasmJsResourcesDir.set(target.layout.projectDirectory.dir("cmp-web/src/wasmJsMain/resources"))
            desktopIconsDir.set(target.layout.projectDirectory.dir("cmp-desktop/icons"))
            androidResDir.set(target.layout.projectDirectory.dir("cmp-android/src/main/res"))
        }
    }
}

/**
 * Projects `app-profile/` onto every generated surface: `gradle/fork.properties`, the version catalog,
 * `Config.xcconfig`, per-module BuildKonfig, icons and store metadata.
 *
 * Caching is disabled on purpose — it reads a gitignored `fork.properties` and writes outside the build directory, so
 * a cached result would be wrong on a machine whose fork config differs.
 */
@DisableCachingByDefault(because = "Reads gitignored fork.properties; writes local.properties and metadata files")
abstract class SyncForkConfigTask : DefaultTask() {

    /**
     * The repository root. Every other path is resolved from it, so the task has no dependency on the invoking
     * project's location.
     */
    @get:Internal abstract val projectRootDir: DirectoryProperty

    /** `app-profile/icons/` — the fork-owned icon source. Missing or empty means the template's own icons are kept. */
    @get:Internal abstract val iconSourceDir: DirectoryProperty

    /** Destination for the iOS `AppIcon.appiconset`. */
    @get:Internal abstract val iosAppIconDir: DirectoryProperty

    /** Destination for the Kotlin/JS favicon. */
    @get:Internal abstract val jsResourcesDir: DirectoryProperty

    /** Destination for the Kotlin/Wasm favicon. */
    @get:Internal abstract val wasmJsResourcesDir: DirectoryProperty

    /** Destination for the desktop `.icns` / `.ico` / `.png` icons. */
    @get:Internal abstract val desktopIconsDir: DirectoryProperty

    /** Destination for a pre-built Android res tree, for the adaptive icons Image Asset Studio must generate. */
    @get:Internal abstract val androidResDir: DirectoryProperty

    /**
     * Runs the whole projection. Idempotent: re-running with an unchanged `app-profile/` rewrites identical content,
     * which is what lets a template sync be followed by an unconditional `syncForkConfig`.
     */
    @TaskAction
    fun sync() {
        val root = projectRootDir.get().asFile

        // ── 1. Load gradle/fork.properties ──────────────────────────────────
        val fork = java.util.Properties()
        val forkFile = File(root, "gradle/fork.properties")
        if (forkFile.exists()) {
            forkFile.reader(Charsets.UTF_8).use(fork::load)
        } else {
            logger.warn(
                "syncForkConfig: gradle/fork.properties not found.\n" +
                    "  Copy: cp gradle/fork.properties.template gradle/fork.properties\n" +
                    "  Fill in your values, then re-run ./gradlew syncForkConfig",
            )
        }

        // ── 2. Load 6 build-time fields from gradle/libs.versions.toml ──────
        val toml = parseTomlVersions(File(root, "gradle/libs.versions.toml"))

        // ── 2a. Load app-profile/ — the fork-owned white-label SoT ──────────
        // Deep-merges app.yaml + platforms/**/*.yaml into one nested map and resolves the SAME
        // flat dotted fork.properties keys the Ruby side (deployment/_shared/config.rb AppProfile)
        // resolves — one contract, two consumers. Fail-soft: absent app-profile/ → empty map, so
        // the plugin behaves EXACTLY as before (off fork.properties + TOML).
        val appProfile = loadAppProfile(root)
        val hasAppProfile = appProfile.isNotEmpty()

        // Priority: ENV > app-profile > fork.properties > TOML > ""
        fun get(forkKey: String, envKey: String? = null, tomlKey: String? = null): String {
            envKey?.let { System.getenv(it)?.takeIf(String::isNotBlank) }?.let { return it }
            appProfileGet(appProfile, forkKey)?.takeIf(String::isNotBlank)?.let { return it }
            (fork.getProperty(forkKey))?.takeIf(String::isNotBlank)?.let { return it }
            tomlKey?.let { toml[it]?.takeIf(String::isNotBlank) }?.let { return it }
            return ""
        }

        // Build-time fields. app.id is AUTHORED in fork.properties (its single source of truth);
        // appDisplayName/projectName still read fork.properties-first then fall back to TOML.
        val appId = get("app.id", "APP_BUNDLE_ID", "appId")
        val appDisplayName = get("app.display.name", "APP_DISPLAY_NAME", "appDisplayName")
        // projectName's SoT is the TRACKED catalog (libs.versions.toml#projectName — see
        // fork-props-manifest-parity.sh NOT_APP_PROFILE), so it must be read BEFORE the gitignored
        // fork.properties bridge. The generic get() order put the bridge first, and this task then
        // WROTE the value back into the catalog (patchTomlVersion below) — so a stale local bridge
        // silently overwrote the tracked SoT with `kmp-project-template` (field-officer, 2026-10-30).
        // ENV still wins for CI; the bridge is only the last-resort fallback.
        val projectName = System.getenv("PROJECT_NAME")?.takeIf(String::isNotBlank)
            ?: toml["projectName"]?.takeIf(String::isNotBlank)
            ?: fork.getProperty("project.name")?.takeIf(String::isNotBlank)
            ?: ""

        // Network build-bridge (B4) — SoT is app-profile/app.yaml#network.*; emitted to fork.properties
        // for KMPFlavorsConventionPlugin. Blank → the plugin's baked-in template default.
        val networkDemoUrl = get("network.base.url.demo")
        val networkProdUrl = get("network.base.url.prod")
        val demoUsername = get("demo.username")
        val demoPassword = get("demo.password")
        val logTag = get("log.tag")

        // ── 2b. Write app.id + app display name BACK into gradle/libs.versions.toml ─────────
        // The whole build reads the bundle id via libs.versions.appId AND the app name via
        // libs.versions.appDisplayName (Android resValue app_name / iOS CFBundleDisplayName). Both are
        // resolved app-profile-first (identity.app_id / identity.app_name — priority ENV > app-profile >
        // fork.properties > TOML), so sync BOTH back into the catalog: a fork edits identity in ONE place
        // (app-profile/app.yaml) and the catalog follows. No-op when they already agree (e.g. the upstream
        // template). Without the appDisplayName write the catalog drifts — a fork renamed in app.yaml would
        // still INSTALL under the old name while the store listing showed the new one. appid-consistency
        // guards the appId drift.
        if (appId.isNotBlank()) {
            patchTomlVersion(File(root, "gradle/libs.versions.toml"), "appId", appId)
        }
        if (appDisplayName.isNotBlank()) {
            patchTomlVersion(File(root, "gradle/libs.versions.toml"), "appDisplayName", appDisplayName)
        }
        // desktopAppName (JVM/dock) + projectName (rootProject.name) ALSO live in the catalog and resolve
        // app-profile-first (identity.app_name / project name) per this repo's CLAUDE.md, but a catalog-3way
        // merge during /kmp-project-template-sync reverts them to the template placeholder ("App Toolkit" /
        // "kmp-project-template"). Writing only appId+appDisplayName left them stale (a fork's catalog
        // could carry template identity after a clean sync). Derive + write ALL identity
        // lines so the catalog fully follows the app-profile SoT after any sync.
        if (appDisplayName.isNotBlank()) {
            patchTomlVersion(File(root, "gradle/libs.versions.toml"), "desktopAppName", appDisplayName)
        }
        if (projectName.isNotBlank()) {
            patchTomlVersion(File(root, "gradle/libs.versions.toml"), "projectName", projectName)
        }

        // Apple
        val appleTeamId = get("apple.team.id", "APPLE_TEAM_ID", "iosTeamId")
        if (appleTeamId.isNotBlank() && appleTeamId != "YOUR_TEAM_ID") {
            patchTomlVersion(File(root, "gradle/libs.versions.toml"), "iosTeamId", appleTeamId)
        }
        val matchGitUrl = get("apple.match.git.url", "MATCH_GIT_URL")
        val tfGroups = get("apple.tf.groups", "TESTFLIGHT_GROUPS")

        // Firebase
        val fbAndroidProd = get("firebase.android.prod.app.id", "FIREBASE_ANDROID_PROD_APP_ID")
        val fbAndroidDemo = get("firebase.android.demo.app.id", "FIREBASE_ANDROID_DEMO_APP_ID")
        val fbIos = get("firebase.ios.app.id", "FIREBASE_IOS_APP_ID")
        val fbGroups = get("firebase.groups", "FIREBASE_GROUPS")

        // Org
        val orgName = get("org.name", "ORGANIZATION_NAME")
        val orgEmail = get("org.email", "APPSTORE_REVIEW_EMAIL")
        val orgFirstName = get("org.first.name", "APPSTORE_REVIEW_FIRST_NAME")
        val orgLastName = get("org.last.name", "APPSTORE_REVIEW_LAST_NAME")
        val orgPhone = get("org.phone", "APPSTORE_REVIEW_PHONE")
        // Vault-alias namespaces (RULE-SECRETS-NAMING-CONVENTION-001): `<org>-<suffix>` for org-shared
        // secrets, `<project>-<suffix>` for per-app ones. The SOURCE pair is what the template's committed
        // secret files carry (declared once in the template-owned secrets/LAYOUT.yaml#alias_namespaces);
        // the TARGET pair is this checkout's own org (app-profile org.secrets_namespace) + projectName.
        val templateAliasNs = readTemplateAliasNamespaces(root)
        val orgSecretsNamespace = get("org.secrets.namespace", "ORG_SECRETS_NAMESPACE")
            .ifBlank { templateAliasNs?.org.orEmpty() }
        val marketingUrl = get("org.marketing.url", "APP_MARKETING_URL")
        val privacyUrl = get("org.privacy.url", "APP_PRIVACY_URL")
        val supportUrl = get("org.support.url")

        // Web
        val cloudflareProject = get("web.cloudflare.project", "CLOUDFLARE_PAGES_PROJECT_NAME")

        // Store — shared
        // Primary listing locale (metadata subdir). app-profile store.primary_locale is the SoT;
        // default en-US. Replaces the hardcoded en-GB (iOS/mac) / en-US (android) subdirs.
        val primaryLocale = get("store.primary.locale").ifBlank { "en-US" }
        val storeTitle = get("store.title")
        val storeSubtitle = get("store.subtitle")
        val storeDescription = get("store.description")
        val storePromoText = get("store.promotional.text")
        val storeReleaseNotes = get("store.release.notes")
        val storeCopyright = get("store.copyright")
        val storeReviewNotes = get("store.review.notes")
        val reviewDemoUser = get("store.review.demo.user")
        val reviewDemoPassword = get("store.review.demo.password")
        val iosAgeRating = get("store.ios.age.rating").ifBlank { "4+" }

        // Store — Apple trade representative contact information (App Store Connect requirement)
        val tradeFirstName = get("store.apple.trade.first.name")
        val tradeLastName = get("store.apple.trade.last.name")
        val tradeAddr1 = get("store.apple.trade.address.line1")
        val tradeAddr2 = get("store.apple.trade.address.line2")
        val tradeAddr3 = get("store.apple.trade.address.line3")
        val tradeCity = get("store.apple.trade.city")
        val tradeState = get("store.apple.trade.state")
        val tradeCountry = get("store.apple.trade.country")
        val tradePostal = get("store.apple.trade.postal.code")
        val tradePhone = get("store.apple.trade.phone")
        val tradeEmail = get("store.apple.trade.email")
        val tradeDisplayed = get("store.apple.trade.displayed")

        // Store — Android
        val androidChangelog = get("store.android.changelog")
        val androidShortDesc = get("store.android.short.description")
        val androidCategory = get("store.android.category")
        val androidVideoUrl = get("store.android.video.url")

        // Store — iOS
        val iosKeywords = get("store.ios.keywords")
        val iosCategory = get("store.ios.category")
        val iosSecondaryCategory = get("store.ios.secondary.category")
        val iosAppleTvPrivacyUrl = get("store.ios.apple.tv.privacy.url")

        // Store — macOS
        val macosKeywords = get("store.macos.keywords")
        val macosCategory = get("store.macos.category")
        val macosSecondaryCategory = get("store.macos.secondary.category")

        // Store — Windows / Microsoft Store
        val msAppId = get("store.windows.ms.app.id", "MS_APP_ID")
        val msPublishMode = get("store.windows.ms.publish.mode").ifBlank { "Manual" }
        val msVisibility = get("store.windows.ms.visibility").ifBlank { "Public" }
        // MSIX packaging identity (Package.appxmanifest) — app-profile windows.msix_*.
        val msixIdentityName = get("windows.msix.identity.name")
        val msixIdentityPub = get("windows.msix.identity.publisher")
        val msixPublisherDisplay = get("windows.msix.publisher.display.name")

        // ── 3. iOS xcconfig ───────────────────────────────────────────────────
        val xcconfigFile = File(root, "cmp-ios/Configuration/Config.xcconfig")
        if (xcconfigFile.parentFile.exists()) {
            xcconfigFile.writeText(
                "// Fork identity — generated by ./gradlew syncForkConfig\n" +
                    "// To change: edit gradle/fork.properties (or libs.versions.toml for app.id),\n" +
                    "// then re-run ./gradlew syncForkConfig\n" +
                    "APP_BUNDLE_ID = $appId\n" +
                    "APP_NAME = $appDisplayName\n" +
                    "TEAM_ID = $appleTeamId\n" +
                    "\n" +
                    "// Keychain Sharing entitlement — REQUIRED by core-base/datastore's KeychainSettings\n" +
                    "// (service \"kpt.secure\"). Without it iOS keychain access fails -34018 at launch and\n" +
                    "// crashes the Koin graph (UserPreferencesRepositoryImpl -> AppViewModel). Applies to\n" +
                    "// every flavor (project-level base config). Path relative to cmp-ios/ (SRCROOT).\n" +
                    "CODE_SIGN_ENTITLEMENTS = iosApp/iosApp.entitlements\n",
            )
            logger.lifecycle("syncForkConfig: wrote cmp-ios/Configuration/Config.xcconfig")
        }
        // NOTE: cmp-ios/Configs/{variant}.xcconfig files are generated by the
        // :cmp-shared:generateIosFlavorXcconfigs task (KMPFlavorsConventionPlugin),
        // which reads the live kmpFlavors DSL. They auto-regenerate on every iOS build.
        // syncForkConfig only owns Config.xcconfig (APP_BUNDLE_ID / APP_NAME / TEAM_ID).

        // ── 4. local.properties (Fastlane bridge — gitignored) ───────────────
        // project_config.rb reads these as a fallback when fork.properties is absent.
        val lp = File(root, "local.properties")
        val props = java.util.Properties()
        if (lp.exists()) lp.inputStream().use(props::load)
        // Write every fork.properties key with fork.* prefix
        fork.forEach { k, v -> props["fork.$k"] = v }
        // Ensure the 4 TOML-sourced keys are always present
        if (appId.isNotBlank()) props["fork.app.id"] = appId
        if (appDisplayName.isNotBlank()) props["fork.app.display.name"] = appDisplayName
        if (projectName.isNotBlank()) props["fork.project.name"] = projectName
        if (appleTeamId.isNotBlank()) props["fork.apple.team.id"] = appleTeamId
        lp.outputStream().use { props.store(it, "Generated by syncForkConfig — do not edit manually") }
        logger.lifecycle("syncForkConfig: updated local.properties (${props.size} entries)")

        // ── 5. gradle.properties (settings.gradle.kts bridge) ────────────────
        val gp = File(root, "gradle.properties")
        val gpText = if (gp.exists()) gp.readText() else ""
        gp.writeText(
            if (gpText.contains("fork.project.name=")) {
                gpText.replace(Regex("fork\\.project\\.name=.*"), "fork.project.name=$projectName")
            } else {
                gpText.trimEnd() + "\nfork.project.name=$projectName\n"
            },
        )
        logger.lifecycle("syncForkConfig: updated gradle.properties fork.project.name=$projectName")

        // ── 5b. Regenerate gradle/fork.properties FROM resolved values ────────
        // When app-profile/ is the SoT, fork.properties becomes a DERIVED build-bridge so anything
        // that reads it directly (e.g. AndroidConfig::PRIMARY_LOCALE in config.rb, which parses
        // store.primary.locale inline) stays fed from app-profile. Every key the plugin resolves is
        // written; unknown/legacy fork.properties keys (apple.tf.groups, apple.testers.*, …) are
        // PRESERVED so config.rb's fallback still resolves them. Values are written RAW (not via
        // Properties.store) so config.rb's raw `split("=")` reader sees the exact string — app.yaml
        // uses single-line `\n`-literal scalars, so no value spans multiple lines. Only runs when
        // app-profile/ is present; a fork WITHOUT app-profile keeps its hand-authored file untouched.
        if (hasAppProfile) {
            val resolved = linkedMapOf(
                "app.id" to appId, "app.display.name" to appDisplayName, "project.name" to projectName,
                "network.base.url.demo" to networkDemoUrl, "network.base.url.prod" to networkProdUrl,
                "demo.username" to demoUsername, "demo.password" to demoPassword, "log.tag" to logTag,
                "apple.team.id" to appleTeamId, "apple.match.git.url" to matchGitUrl,
                "apple.tf.groups" to tfGroups,
                "firebase.android.prod.app.id" to fbAndroidProd,
                "firebase.android.demo.app.id" to fbAndroidDemo,
                "firebase.ios.app.id" to fbIos, "firebase.groups" to fbGroups,
                "org.name" to orgName, "org.email" to orgEmail, "org.first.name" to orgFirstName,
                "org.last.name" to orgLastName, "org.phone" to orgPhone,
                "org.secrets.namespace" to orgSecretsNamespace,
                "org.marketing.url" to marketingUrl, "org.privacy.url" to privacyUrl,
                "org.support.url" to supportUrl, "web.cloudflare.project" to cloudflareProject,
                "store.primary.locale" to primaryLocale, "store.title" to storeTitle,
                "store.subtitle" to storeSubtitle, "store.description" to storeDescription,
                "store.promotional.text" to storePromoText, "store.release.notes" to storeReleaseNotes,
                "store.copyright" to storeCopyright, "store.review.notes" to storeReviewNotes,
                "store.review.demo.user" to reviewDemoUser,
                "store.review.demo.password" to reviewDemoPassword,
                "store.ios.age.rating" to iosAgeRating, "store.android.changelog" to androidChangelog,
                "store.android.short.description" to androidShortDesc,
                "store.android.category" to androidCategory, "store.android.video.url" to androidVideoUrl,
                "store.ios.keywords" to iosKeywords, "store.ios.category" to iosCategory,
                "store.ios.secondary.category" to iosSecondaryCategory,
                "store.ios.apple.tv.privacy.url" to iosAppleTvPrivacyUrl,
                "store.macos.keywords" to macosKeywords, "store.macos.category" to macosCategory,
                "store.macos.secondary.category" to macosSecondaryCategory,
                "store.windows.ms.app.id" to msAppId, "store.windows.ms.publish.mode" to msPublishMode,
                "store.windows.ms.visibility" to msVisibility,
                "windows.msix.identity.name" to msixIdentityName,
                "windows.msix.identity.publisher" to msixIdentityPub,
                "windows.msix.publisher.display.name" to msixPublisherDisplay,
            )
            val forkOut = LinkedHashMap<String, String>()
            resolved.forEach { (k, v) -> if (v.isNotBlank()) forkOut[k] = v }
            // Preserve legacy/unknown keys already in fork.properties (single-line values only).
            fork.forEach { k, v ->
                val ks = k.toString()
                val vs = v.toString()
                if (!forkOut.containsKey(ks) && vs.isNotBlank() && !vs.contains('\n')) forkOut[ks] = vs
            }
            val sb = StringBuilder()
                // The "DERIVED from app-profile" phrase is a CONTRACT, not prose: white-label-derived.sh
                // greps head -3 for it to tell a generated bridge from a hand-authored one. derive.rb
                // stamps the same phrase. They disagreed until 2026-09-06 — this writer said "GENERATED
                // from app-profile/app.yaml" — so a syncForkConfig-written bridge was reported
                // hand-authored, and the health verdict depended on which of the two writers ran last.
                .append("# gradle/fork.properties — DERIVED from app-profile by syncForkConfig. DO NOT EDIT.\n")
                .append("# Edit app-profile/app.yaml or app-profile/platforms/**/*.yaml instead;\n")
                .append("# fork.properties is the derived build-bridge (config.rb fallback + inline reads).\n")
            forkOut.forEach { (k, v) -> sb.append(k).append('=').append(v).append('\n') }
            File(root, "gradle/fork.properties").writeText(sb.toString())
            logger.lifecycle("syncForkConfig: regenerated fork.properties from app-profile (${forkOut.size} keys)")

            // B3 — regenerate core/network AccessPointRegistry.points from app-profile#network.access_points.
            // The four passes share one SoT (network.access_points), so a declared endpoint reaches the
            // registry (incl. each Supabase point's publishable key) and the UrlType vocabulary in one
            // run — the reason a fork only writes the API type.
            regenerateAccessPoints(root, appProfile)
            regenerateUrlTypes(root, appProfile)
            regenerateAppReviewConfig(root, appProfile)
            regenerateAppSecurityConfig(root, appProfile)
            regenerateAppPrivacyConfig(root, appProfile)
            regenerateAppOrgLinks(root, appProfile)
            reconcileMigrationLedger(root, appProfile)
            regenerateBuildKonfigFields(root, appProfile)
            scaffoldAccessPointPackages(root, appProfile)
        }

        // ── 6. Store metadata files ───────────────────────────────────────────
        var written = 0

        // app.yaml stores long copy (store.description / release_notes / review notes / changelog)
        // as single-quoted scalars with LITERAL `\n` sequences (kept single-line so config.rb's raw
        // reader + fork.properties round-trip). Metadata .txt files want REAL newlines, so unescape
        // here — the point at which the plugin owns the .txt output (G4). No-op for single-line values.
        fun nl(value: String): String = value.replace("\\r\\n", "\n").replace("\\n", "\n")

        // Write only when value is non-blank (skip optional fields that aren't set)
        fun writeIfPresent(rel: String, value: String) {
            if (value.isBlank()) return
            val f = File(root, rel)
            if (!ensureMetadataParent(f)) return
            f.writeText(nl(value))
            logger.lifecycle("syncForkConfig: wrote $rel")
            written++
        }

        // Always write — clears file when value is blank (used for optional/empty fields
        // that must exist so Fastlane / Deliver don't fall back to stale content)
        fun writeAlways(rel: String, value: String) {
            val f = File(root, rel)
            if (!ensureMetadataParent(f)) return
            f.writeText(nl(value))
            logger.lifecycle("syncForkConfig: wrote $rel")
            written++
        }

        // Locale-scoped metadata writer (G5). Writes `<metaRoot>/<primaryLocale>/<name>` (the fresh,
        // authoritative copy) and ALSO refreshes a pre-existing legacy `<metaRoot>/en-GB/<name>` when
        // it is on disk and differs from the primary locale — back-compat for forks whose Deliver
        // config still points at en-GB. The primary-locale dir is created by ensureMetadataParent
        // (e.g. mac-app-store/metadata ships en-GB but no en-US).
        fun writeLocalized(metaRoot: String, name: String, value: String) {
            writeIfPresent("$metaRoot/$primaryLocale/$name", value)
            if (primaryLocale != "en-GB" && File(root, "$metaRoot/en-GB").isDirectory) {
                writeIfPresent("$metaRoot/en-GB/$name", value)
            }
        }

        // Generate App Store / Mac App Store age-rating JSON from store.ios.age.rating
        fun writeRatingConfig(rel: String) {
            val f = File(root, rel)
            if (!ensureMetadataParent(f)) return
            f.writeText(
                """{
  "v1_0": {
    "ratings": {
      "violenceCartoonOrFantasy": "NONE",
      "violenceRealistic": "NONE",
      "violenceRealisticProlongedGraphicOrSadistic": "NONE",
      "profanityOrCrudeHumor": "NONE",
      "matureOrSuggestiveThemes": "NONE",
      "horrorOrFearThemesForChildren": "NONE",
      "medicalOrTreatmentInformation": "NONE",
      "alcoholTobaccoOrDrugUseOrReferences": "NONE",
      "gamblingAndContests": "NONE",
      "sexualContentOrNudity": "NONE",
      "sexualContentGraphicAndNudity": "NONE"
    },
    "booleans": {
      "ageRatingProcess": false,
      "gamblingAllowed": false,
      "unrestrictedWebAccess": false,
      "kidsAgeBand": false
    }
  }
}""",
            )
            logger.lifecycle("syncForkConfig: wrote $rel (age rating: $iosAgeRating)")
            written++
        }

        // ── iOS App Store ─────────────────────────────────────────────────────
        // Locale-scoped files → primaryLocale dir (was hardcoded en-GB) via writeLocalized (G5).
        writeLocalized("deployment/ios/appstore/metadata", "name.txt", storeTitle)
        writeLocalized("deployment/ios/appstore/metadata", "subtitle.txt", storeSubtitle)
        writeLocalized("deployment/ios/appstore/metadata", "description.txt", storeDescription)
        writeLocalized("deployment/ios/appstore/metadata", "keywords.txt", iosKeywords)
        writeLocalized("deployment/ios/appstore/metadata", "promotional_text.txt", storePromoText)
        writeLocalized("deployment/ios/appstore/metadata", "release_notes.txt", storeReleaseNotes)
        writeLocalized("deployment/ios/appstore/metadata", "marketing_url.txt", marketingUrl)
        writeLocalized("deployment/ios/appstore/metadata", "privacy_url.txt", privacyUrl)
        writeLocalized("deployment/ios/appstore/metadata", "support_url.txt", supportUrl)
        writeIfPresent("deployment/ios/appstore/metadata/copyright.txt", storeCopyright)
        writeIfPresent("deployment/ios/appstore/metadata/primary_category.txt", iosCategory)
        writeAlways("deployment/ios/appstore/metadata/primary_first_sub_category.txt", "")
        writeAlways("deployment/ios/appstore/metadata/primary_second_sub_category.txt", "")
        writeAlways("deployment/ios/appstore/metadata/secondary_category.txt", iosSecondaryCategory)
        writeAlways("deployment/ios/appstore/metadata/secondary_first_sub_category.txt", "")
        writeAlways("deployment/ios/appstore/metadata/secondary_second_sub_category.txt", "")
        writeIfPresent("deployment/ios/appstore/metadata/review_information/email_address.txt", orgEmail)
        writeIfPresent("deployment/ios/appstore/metadata/review_information/first_name.txt", orgFirstName)
        writeIfPresent("deployment/ios/appstore/metadata/review_information/last_name.txt", orgLastName)
        writeIfPresent("deployment/ios/appstore/metadata/review_information/phone_number.txt", orgPhone)
        writeIfPresent("deployment/ios/appstore/metadata/review_information/notes.txt", storeReviewNotes)
        writeAlways("deployment/ios/appstore/metadata/review_information/demo_user.txt", reviewDemoUser)
        writeAlways("deployment/ios/appstore/metadata/review_information/demo_password.txt", reviewDemoPassword)
        writeAlways("deployment/ios/appstore/metadata/$primaryLocale/apple_tv_privacy_policy.txt", iosAppleTvPrivacyUrl)
        writeRatingConfig("deployment/ios/appstore/metadata/app_store_rating_config.json")

        // ── iOS trade representative contact information (App Store Connect) ──
        val tradeIos = "deployment/ios/appstore/metadata/trade_representative_contact_information"
        File(root, tradeIos).mkdirs()
        writeIfPresent("$tradeIos/first_name.txt", tradeFirstName)
        writeIfPresent("$tradeIos/last_name.txt", tradeLastName)
        writeIfPresent("$tradeIos/address_line1.txt", tradeAddr1)
        writeAlways("$tradeIos/address_line2.txt", tradeAddr2)
        writeAlways("$tradeIos/address_line3.txt", tradeAddr3)
        writeIfPresent("$tradeIos/city_name.txt", tradeCity)
        writeIfPresent("$tradeIos/state.txt", tradeState)
        writeIfPresent("$tradeIos/country.txt", tradeCountry)
        writeIfPresent("$tradeIos/postal_code.txt", tradePostal)
        writeIfPresent("$tradeIos/phone_number.txt", tradePhone)
        writeIfPresent("$tradeIos/email_address.txt", tradeEmail)
        writeAlways("$tradeIos/is_displayed_on_app_store.txt", tradeDisplayed)

        // ── macOS App Store ───────────────────────────────────────────────────
        // Locale-scoped files → primaryLocale dir (was hardcoded en-GB) via writeLocalized (G5).
        writeLocalized("deployment/desktop/mac-app-store/metadata", "name.txt", storeTitle)
        writeLocalized("deployment/desktop/mac-app-store/metadata", "subtitle.txt", storeSubtitle)
        writeLocalized("deployment/desktop/mac-app-store/metadata", "description.txt", storeDescription)
        writeLocalized("deployment/desktop/mac-app-store/metadata", "keywords.txt", macosKeywords)
        writeLocalized("deployment/desktop/mac-app-store/metadata", "promotional_text.txt", storePromoText)
        writeLocalized("deployment/desktop/mac-app-store/metadata", "release_notes.txt", storeReleaseNotes)
        writeLocalized("deployment/desktop/mac-app-store/metadata", "marketing_url.txt", marketingUrl)
        writeLocalized("deployment/desktop/mac-app-store/metadata", "privacy_url.txt", privacyUrl)
        writeLocalized("deployment/desktop/mac-app-store/metadata", "support_url.txt", supportUrl)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/copyright.txt", storeCopyright)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/primary_category.txt", macosCategory)
        writeAlways("deployment/desktop/mac-app-store/metadata/primary_first_sub_category.txt", "")
        writeAlways("deployment/desktop/mac-app-store/metadata/primary_second_sub_category.txt", "")
        writeAlways("deployment/desktop/mac-app-store/metadata/secondary_category.txt", macosSecondaryCategory)
        writeAlways("deployment/desktop/mac-app-store/metadata/secondary_first_sub_category.txt", "")
        writeAlways("deployment/desktop/mac-app-store/metadata/secondary_second_sub_category.txt", "")
        writeIfPresent("deployment/desktop/mac-app-store/metadata/review_information/email_address.txt", orgEmail)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/review_information/first_name.txt", orgFirstName)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/review_information/last_name.txt", orgLastName)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/review_information/phone_number.txt", orgPhone)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/review_information/notes.txt", storeReviewNotes)
        writeAlways("deployment/desktop/mac-app-store/metadata/review_information/demo_user.txt", reviewDemoUser)
        writeAlways("deployment/desktop/mac-app-store/metadata/review_information/demo_password.txt", reviewDemoPassword)
        writeRatingConfig("deployment/desktop/mac-app-store/metadata/app_store_rating_config.json")

        // ── macOS trade representative contact information (App Store Connect) ─
        val tradeMac = "deployment/desktop/mac-app-store/metadata/trade_representative_contact_information"
        File(root, tradeMac).mkdirs()
        writeIfPresent("$tradeMac/first_name.txt", tradeFirstName)
        writeIfPresent("$tradeMac/last_name.txt", tradeLastName)
        writeIfPresent("$tradeMac/address_line1.txt", tradeAddr1)
        writeAlways("$tradeMac/address_line2.txt", tradeAddr2)
        writeAlways("$tradeMac/address_line3.txt", tradeAddr3)
        writeIfPresent("$tradeMac/city_name.txt", tradeCity)
        writeIfPresent("$tradeMac/state.txt", tradeState)
        writeIfPresent("$tradeMac/country.txt", tradeCountry)
        writeIfPresent("$tradeMac/postal_code.txt", tradePostal)
        writeIfPresent("$tradeMac/phone_number.txt", tradePhone)
        writeIfPresent("$tradeMac/email_address.txt", tradeEmail)
        writeAlways("$tradeMac/is_displayed_on_app_store.txt", tradeDisplayed)

        // ── Android Play Store ────────────────────────────────────────────────
        // Locale-scoped files → primaryLocale dir (default en-US) — G5. Android uses en-US today,
        // so the default is a no-op; a fork that sets store.primary_locale moves the listing subdir.
        writeIfPresent("deployment/android/metadata/$primaryLocale/title.txt", storeTitle)
        writeIfPresent("deployment/android/metadata/$primaryLocale/short_description.txt", androidShortDesc)
        writeIfPresent("deployment/android/metadata/$primaryLocale/full_description.txt", storeDescription)
        writeIfPresent("deployment/android/metadata/$primaryLocale/changelogs/default.txt", androidChangelog)
        writeAlways("deployment/android/metadata/$primaryLocale/video.txt", androidVideoUrl)

        // Microsoft Store (StoreBroker.config.json, Package.appxmanifest) and the Cloudflare Pages project
        // name are NOT written here any more: deployment/ is template-owned and byte-identical in every
        // fork. deployment/_shared/scripts/fork-identity.sh renders those values from
        // gradle/fork.properties at deploy time, into build/ (removed 2026-10-29: section 6c + StoreBroker).

        logger.lifecycle("syncForkConfig: wrote $written store metadata files")

        // ── 6d. Fork media fan-out (platforms-first, extends the icon pattern) ─
        fun deriveForkMedia() {
            // platforms/<p>/media/ is the fork-owned media SoT → deployment/<p>/metadata images (extends the icon pattern).
            val map = mapOf(
                "android" to "deployment/android/metadata",
                "apple/ios" to "deployment/ios/appstore/metadata",
                "apple/macos" to "deployment/desktop/mac-app-store/metadata",
                "windows" to "deployment/windows/metadata",
                "ubuntu" to "deployment/linux/metadata",
            )
            for ((platRel, metaRel) in map) {
                val src = File(root, "app-profile/platforms/$platRel/media")
                if (!src.isDirectory) continue
                // Skip a media dir with no real assets (only .gitkeep) — don't seed empty deployment metadata dirs.
                if (src.walkTopDown().none { it.isFile && it.name != ".gitkeep" }) continue
                val dst = File(root, metaRel)
                dst.mkdirs()
                // MIRROR, not merge: remove the previously-derived media subtrees (images/ for Play,
                // screenshots/ for iOS/mac/Windows/Linux) before copying, so a prior generation's files
                // (renamed/removed screenshots, an _over8/ overflow) do NOT linger and get uploaded.
                // The .txt store-metadata (written separately by writeLocalized) lives outside these
                // dirs and is untouched. (2026-08-08: derived metadata had stale 01_home.png + v1 01.png
                // + _over8/ mixed → Play "phoneScreenshots - Invalid request".)
                dst.walkTopDown()
                    .filter { it.isDirectory && (it.name == "images" || it.name == "screenshots") }
                    .toList()
                    .forEach { it.deleteRecursively() }
                src.copyRecursively(dst, overwrite = true)
            }
            // Strip STRAY image-type dirs at the metadata ROOT. fastlane supply reads EVERY metadata-root
            // subdir as a LOCALE, so a root-level `phoneScreenshots` becomes language "phoneScreenshots"
            // → get_edit_listing 400 "Invalid request" (2026-08-08). Screenshot dirs belong ONLY under
            // <locale>/images/<type>/. These are stale from an old layout (the mirror above cleans
            // images/screenshots subtrees, not these root-level type dirs). Real locale dirs (en-US) stay.
            listOf(
                "phoneScreenshots",
                "sevenInchScreenshots",
                "tenInchScreenshots",
                "tvScreenshots",
                "wearScreenshots",
                "images",
                "screenshots",
            )
                .forEach { File(root, "deployment/android/metadata/$it").deleteRecursively() }
            // Play caps screenshots at 8 per form-factor per locale — trim deployment/android/metadata; extras → _over8/.
            val androidMeta = File(root, "deployment/android/metadata")
            androidMeta.listFiles()?.filter { it.isDirectory }?.forEach { locale ->
                listOf("phoneScreenshots", "sevenInchScreenshots", "tenInchScreenshots").forEach { form ->
                    val dir = File(locale, "images/$form")
                    if (dir.isDirectory) {
                        val imgs = dir.listFiles { f -> f.isFile && f.name.matches(Regex(".*\\.(png|jpe?g)$", RegexOption.IGNORE_CASE)) }?.sortedBy { it.name } ?: emptyList()
                        if (imgs.size > 8) {
                            val over = File(locale, "images/_over8/$form")
                            over.mkdirs()
                            imgs.drop(8).forEach { it.renameTo(File(over, it.name)) }
                        }
                    }
                }
            }
            // Mac App Store caps screenshots at 10 per DISPLAY TYPE — and mac-1280..mac-2880 are all the
            // SAME display type (APP_DESKTOP), unlike iOS where each iphone/ipad folder is a DISTINCT type.
            // The app-profile SoT renders every size family (per STORE_ASSET_SPECS asc_macos); a verbatim
            // copy would hand deliver 4×7 = 28 APP_DESKTOP shots → "over 10" rejection. Keep ONLY the
            // largest size family present per locale (best quality; ASC downscales within the 16:10 family).
            val macShots = File(root, "deployment/desktop/mac-app-store/metadata/screenshots")
            if (macShots.isDirectory) {
                val macSizeRank = listOf("mac-2880", "mac-2560", "mac-1440", "mac-1280")
                macShots.listFiles()?.filter { it.isDirectory }?.forEach { locale ->
                    val present = macSizeRank.filter { fam ->
                        File(locale, fam).let { d -> d.isDirectory && (d.listFiles { f -> f.isFile && f.name.matches(Regex(".*\\.(png|jpe?g)$", RegexOption.IGNORE_CASE)) }?.isNotEmpty() == true) }
                    }
                    present.drop(1).forEach { File(locale, it).deleteRecursively() }
                }
            }
            // deliver (App Store iOS + macOS) reads screenshots FLAT per locale: it globs
            // "<screenshots_path>/<locale>/*.png" and infers each device by image RESOLUTION — it does NOT
            // recurse into device subfolders (Deliver::Loader.language_folders + LanguageFolder#file_paths).
            // The SoT organises shots in per-device folders for rendering; FLATTEN each
            // <locale>/<device>/<NN>.png → <locale>/<device>-<NN>.png so deliver actually sees them. Without
            // this deliver finds ZERO screenshots and silently "uploads all" (nothing). Supply/Android is
            // exempt — Play's <locale>/images/<type>/ subfolders ARE the documented supply layout.
            // (2026-08-10: iOS + macOS listing media never actually pushed — device subfolders invisible to deliver.)
            fun flattenDeliverScreenshots(shotsRel: String) {
                val shots = File(root, shotsRel)
                if (!shots.isDirectory) return
                shots.listFiles()?.filter { it.isDirectory }?.forEach { localeDir ->
                    localeDir.listFiles()?.filter { it.isDirectory }?.forEach { deviceDir ->
                        deviceDir.listFiles { f -> f.isFile && f.name.matches(Regex(".*\\.(png|jpe?g)$", RegexOption.IGNORE_CASE)) }
                            ?.forEach { img -> img.copyTo(File(localeDir, "${deviceDir.name}-${img.name}"), overwrite = true) }
                        deviceDir.deleteRecursively()
                    }
                }
            }
            flattenDeliverScreenshots("deployment/ios/appstore/metadata/screenshots")
            flattenDeliverScreenshots("deployment/desktop/mac-app-store/metadata/screenshots")

            // deliver uploads screenshots to the version localization matching each screenshots/<locale>/
            // folder. The text metadata is dual-written (writeLocalized: primary locale + legacy en-GB),
            // but the media SoT is single-locale (en-US). If the ASC app's DISPLAYED localization is en-GB
            // (Apple registers many accounts as English U.K.), en-US-only screenshots upload to an en-US
            // localization the store never shows → the listing renders the OLD/empty en-GB set. Mirror the
            // primary-locale screenshots into EVERY other metadata locale that has none, so deliver
            // populates en-GB too. (2026-08-10: iOS showed stale + macOS empty screenshots despite upload.)
            fun mirrorScreenshotLocales(metaRel: String) {
                val meta = File(root, metaRel)
                val shotsRoot = File(meta, "screenshots")
                if (!shotsRoot.isDirectory) return
                val shotLocales = shotsRoot.listFiles()?.filter { it.isDirectory }?.map { it.name } ?: return
                val primary = shotLocales.firstOrNull() ?: return
                val localeRe = Regex("[a-z]{2}-[A-Z]{2}")
                meta.listFiles()
                    ?.filter { it.isDirectory && localeRe.matches(it.name) && it.name !in shotLocales }
                    ?.forEach { locDir -> File(shotsRoot, primary).copyRecursively(File(shotsRoot, locDir.name), overwrite = true) }
            }
            mirrorScreenshotLocales("deployment/ios/appstore/metadata")
            mirrorScreenshotLocales("deployment/desktop/mac-app-store/metadata")
        }
        deriveForkMedia()

        // ── 6e. Copy managed structured docs to deployment ─────────────────────
        fun copyIfExists(srcRel: String, dstRel: String) {
            val s = File(root, srcRel)
            if (!s.isFile) return
            val d = File(root, dstRel)
            d.parentFile?.mkdirs()
            s.copyTo(d, overwrite = true)
        }
        copyIfExists("app-profile/platforms/apple/ios/app-content/app_privacy_details.json", "deployment/ios/appstore/metadata/app_privacy_details.json")
        copyIfExists("app-profile/platforms/apple/macos/app-content/app_privacy_details.json", "deployment/desktop/mac-app-store/metadata/app_privacy_details.json")
        copyIfExists("app-profile/platforms/android/app-content/data-safety.csv", "deployment/android/app-content/data-safety.csv")

        // ── 6b. Fork-unique database filename (core/database/DatabaseConfig.kt) ─
        // Derive a per-fork on-disk DB name from appId (dots → underscores) so two
        // forks never collide on desktop/web storage. Uses appId (NOT baseNamespace).
        if (appId.isNotBlank()) {
            val dbName = appId.lowercase().replace(Regex("[^a-z0-9]"), "_") + ".db"
            // Desktop data-dir name: app_name stripped to alphanumerics (human-readable brand
            // folder), falling back to a PascalCase of the appId segments if the display name is
            // blank. Per-fork + collision-free so two template-derived desktop apps never share a dir.
            val desktopDir = appDisplayName.replace(Regex("[^A-Za-z0-9]"), "")
                .ifBlank { appId.split(".").joinToString("") { seg -> seg.replaceFirstChar { it.uppercase() } } }
            val dbConfig = File(root, "core/database/src/commonMain/kotlin/kpt/core/database/config/DatabaseConfig.kt")
            if (dbConfig.parentFile.exists()) {
                // Header from the one fork-owned source (spotless/license-header.txt + org.name);
                // keep the file's stamped year so a re-run never churns it.
                val header = LicenseHeader.kotlin(root, LicenseHeader.existingYear(dbConfig) ?: "2026")
                dbConfig.writeText(
                    (if (header.isEmpty()) "" else "$header\n") +
                        "package kpt.core.database.config\n\n" +
                        "/** Fork-unique database naming — generated by syncForkConfig from app-profile/app.yaml. */\n" +
                        "object DatabaseConfig {\n" +
                        "    /** On-disk SQLite file name (appId-derived). */\n" +
                        "    const val NAME = \"$dbName\"\n\n" +
                        "    /** Desktop (JVM) data directory under the OS app-data root (app_name-derived). */\n" +
                        "    const val DESKTOP_DIR_NAME = \"$desktopDir\"\n" +
                        "}\n",
                )
                logger.lifecycle("syncForkConfig: wrote core/database/.../DatabaseConfig.kt (NAME=$dbName, DESKTOP_DIR_NAME=$desktopDir)")
            }
        }

        // ── 6f. Rewrite cmp-desktop/mac-app-store.entitlements from template ──
        // The Mac App Store entitlements embed the fully-qualified application id ("$TEAM.$APP_ID")
        // and the team id — both fork identity. Derive them from the resolved app-profile values via
        // token substitution against the committed cmp-desktop/mac-app-store.entitlements.template so
        // the shipped entitlements never carries a hardcoded team/bundle literal. Mirrors the
        // Config.xcconfig / DatabaseConfig writers above (same shape, no new machinery).
        val entitlementsTemplate = File(root, "cmp-desktop/mac-app-store.entitlements.template")
        val entitlementsOut = File(root, "cmp-desktop/mac-app-store.entitlements")
        if (entitlementsTemplate.exists()) {
            val teamId = appleTeamId.ifBlank { "YOUR_TEAM_ID" }
            val bundleId = appId.ifBlank { "com.example.app" }
            val body = entitlementsTemplate.readText(Charsets.UTF_8)
                .replace("{{APPLE_TEAM_ID}}", teamId)
                .replace("{{APPLE_APP_IDENTIFIER}}", "$teamId.$bundleId")
            entitlementsOut.writeText(body, Charsets.UTF_8)
            logger.lifecycle("syncForkConfig: wrote cmp-desktop/mac-app-store.entitlements (team=$teamId, appId=$bundleId)")
        } else {
            logger.warn("syncForkConfig: cmp-desktop/mac-app-store.entitlements.template missing — skipping entitlements emit")
        }

        // ── 6g. Secrets alias-namespace + provisioning-profile URL tokenization (A1) ──
        // secrets-manifest.yaml + secrets/LAYOUT.yaml carry vault-alias PREFIXES + a match git URL that
        // are fork identity. Derive them from app-profile so a vault-mode (Path-B) fork rebrands with
        // ZERO hand-edits, the same way deployment/android/*/secrets-needs.yaml already tokenizes.
        //   (a) match git URL — the LAYOUT.yaml `match_git_url` literal is DERIVED from
        //       app-profile#apple.match.git.url (== fork.properties#apple.match.git.url). The neutral
        //       template value is the YOUR_ORG placeholder; a fork's authored URL flows here. No real
        //       org literal is ever committed. Fail-soft when blank.
        //   (b) alias prefixes — see rewriteAliasPrefixes(): source namespaces from
        //       secrets/LAYOUT.yaml#alias_namespaces, target = org.secrets_namespace + projectName.
        run {
            // secrets/LAYOUT.yaml only: deployment/**/secrets-needs.yaml AND secrets-manifest.yaml are
            // regenerated from LAYOUT at 6h, so they pick these rewrites up without being listed here.
            val secretFiles = listOf("secrets/LAYOUT.yaml")
            val aliasNamespace = projectName
            for (rel in secretFiles) {
                val f = File(root, rel)
                if (!f.isFile) continue
                val orig = f.readText()
                var next = orig
                // (a) provisioning-profile git URL — swap the whole github URL to the app-profile value.
                if (matchGitUrl.isNotBlank()) {
                    next = next.replace(
                        Regex("git@github\\.com:[^\\s\"']+/ios-provisioning-profile\\.git"),
                        matchGitUrl,
                    )
                }
                // (b) vault-alias prefixes — no-op on the template itself (source == target).
                next = rewriteAliasPrefixes(next, templateAliasNs, orgSecretsNamespace, aliasNamespace)
                if (next != orig) {
                    f.writeText(next)
                    written++
                    logger.lifecycle("syncForkConfig: tokenized $rel (alias prefix=$aliasNamespace, match-url derived)")
                }
            }
        }

        // ── 6h. secrets-manifest.yaml — generated from deployment/**/secrets-needs.yaml + LAYOUT ──
        // Each target authors only `needs: [{secret, required_for}]` (LAYOUT keys — nothing fork-specific,
        // so deployment/ is never written here); every per-secret fact (vault alias, path, CI secret,
        // how to obtain it) is LAYOUT's. The ONE generated output is the root secrets-manifest.yaml.
        // Runs AFTER 6g so it sees this fork's rewritten alias prefixes. The logic lives ONCE, in the
        // LAYOUT resolver (build_secrets.rb `needs-generate`); the product-health gate re-runs it with
        // --check rather than a second implementation that could disagree.
        generateSecretsNeeds(root)

        // ── 7. Fork icons ─────────────────────────────────────────────────────
        copyForkIcons(root)
    }

    /**
     * Run `build_secrets.rb needs-generate` (stdlib Ruby — the same interpreter the deployment lanes
     * use). A missing Ruby is a warning, not a failure: syncForkConfig must still work for a fork that
     * only builds, and `scripts/product-health/checks/secrets-needs-generated.sh` fails CI if the
     * files were left stale. A generator ERROR (a target needing a secret LAYOUT does not declare)
     * fails the sync — that is the drift this exists to stop.
     */
    private fun generateSecretsNeeds(root: File) {
        val resolver = File(root, "deployment/_shared/lib/build_secrets.rb")
        if (!resolver.isFile) return
        val proc = try {
            ProcessBuilder("ruby", resolver.path, "needs-generate")
                .directory(root)
                .redirectErrorStream(true)
                .start()
        } catch (e: java.io.IOException) {
            logger.warn("syncForkConfig: ruby not found — deployment/**/secrets-needs.yaml NOT regenerated (${e.message})")
            return
        }
        val out = proc.inputStream.bufferedReader().readText().trim()
        val rc = proc.waitFor()
        out.lines().filter { it.isNotBlank() }.forEach { logger.lifecycle("syncForkConfig: $it") }
        if (rc != 0) {
            throw org.gradle.api.GradleException("syncForkConfig: secrets-needs generation failed (exit $rc) — see above")
        }
    }

    /**
     * Replace `key = "..."` in libs.versions.toml with [value], preserving the leading alignment
     * and any trailing inline `# comment`. No-op when the file is missing, the key isn't found, or
     * the value already matches (so the committed catalog only churns on a real fork identity change).
     */
    private fun patchTomlVersion(toml: File, key: String, value: String) {
        if (!toml.exists()) return
        val lines = toml.readLines()
        // ^(indent + key + spaces + = + spaces)("old")(rest incl. inline comment)$  — key match is exact
        // (\b guards against appId matching a longer key), so appDisplayName is never touched.
        val re = Regex("^(\\s*" + Regex.escape(key) + "\\s*=\\s*)\"[^\"]*\"(.*)$")
        var hit = false
        // A `(PLACEHOLDER — …)` note on the trailing comment describes the OLD unset value. Carrying
        // it through verbatim leaves the catalog asserting something false the moment a fork is
        // branded: `iosTeamId = "L432S2FZP5"   # … (PLACEHOLDER — set in app-profile/…)`. A reader
        // then cannot tell a real value from an unfilled one, which is exactly what the marker is
        // for. Drop the parenthetical when the value written is real; keep the descriptive half.
        val valueIsReal = value.isNotBlank() &&
            !value.startsWith("YOUR_") &&
            !value.contains("example.com") &&
            value != "XXXXXXXXXX"
        val placeholderNote = Regex("\\s*\\((?:PLACEHOLDER|placeholder)\\b[^)]*\\)")
        val out = lines.map { line ->
            val m = re.find(line) ?: return@map line
            hit = true
            val rest = if (valueIsReal) placeholderNote.replace(m.groupValues[2], "") else m.groupValues[2]
            "${m.groupValues[1]}\"$value\"$rest"
        }
        if (hit && out != lines) {
            toml.writeText(out.joinToString("\n") + "\n")
            logger.lifecycle("syncForkConfig: patched gradle/libs.versions.toml $key=\"$value\" (from fork.properties)")
        }
    }

    // ── app-profile/ loader (fork-owned white-label SoT) ──────────────────────
    // Reads app-profile/app.yaml + every platforms/**/*.yaml, deep-merged (app.yaml first, then
    // platform files sorted by path so later overrides earlier) into ONE nested String-keyed map.
    // Mirrors the Ruby AppProfile._data merge order in deployment/_shared/config.rb exactly.
    // Fail-soft: absent dir → empty map; a malformed file is skipped with a warning, never fatal.
    private fun loadAppProfile(root: File): Map<String, Any?> {
        val dir = File(root, "app-profile")
        if (!dir.isDirectory) return emptyMap()
        val files = mutableListOf<File>()
        File(dir, "app.yaml").takeIf { it.isFile }?.let { files += it }
        File(dir, "platforms").takeIf { it.isDirectory }
            ?.walkTopDown()
            // Merge ONLY the <platform>.yaml CONFIG files — never the app-content/ store DECLARATIONS
            // (content-rating/age-rating/privacy/export-compliance are questionnaire answers read by
            //  their own consumers, not identity/store config keys — they must not pollute the map).
            ?.filter { it.isFile && it.extension == "yaml" && !it.path.contains("/app-content/") }
            ?.sortedBy { it.path }
            ?.forEach { files += it }
        var merged = emptyMap<String, Any?>()
        val yaml = Yaml()
        for (f in files) {
            try {
                val loaded = f.inputStream().use { yaml.load<Any?>(it) }
                if (loaded is Map<*, *>) merged = deepMerge(merged, loaded.toStringKeyedMap())
            } catch (e: Exception) {
                logger.warn("syncForkConfig: skipping malformed app-profile file ${f.name}: ${e.message}")
            }
        }
        return merged
    }

    @Suppress("UNCHECKED_CAST")
    private fun Map<*, *>.toStringKeyedMap(): Map<String, Any?> =
        entries.associate { (k, v) -> k.toString() to v }

    // Regenerate core/network AppAccessPoints.points from app-profile#network.access_points (B3).
    // In-place sentinel patch (mirrors the libs.versions.toml#appId patch pattern) so the file stays a
    // committed, compilable Kotlin source while app.yaml remains the SoT. No-op when the section or the
    // sentinels are absent. `type: rest|supabase` → AccessPointKind; AccessPoint.type (UrlType) defaults.
    private fun regenerateAccessPoints(root: File, appProfile: Map<String, Any?>) {
        val network = appProfile["network"] as? Map<*, *> ?: return
        val aps = network["access_points"] as? List<*> ?: return
        val file = File(root, "core/network/src/commonMain/kotlin/kpt/core/network/config/AppAccessPoints.kt")
        if (!file.isFile) return
        val begin = "// syncForkConfig:access-points:begin"
        val end = "// syncForkConfig:access-points:end"
        val text = file.readText()
        val beginIdx = text.indexOf(begin)
        val endIdx = text.indexOf(end)
        if (beginIdx < 0 || endIdx < 0 || endIdx < beginIdx) return
        fun esc(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"")
        val sb = StringBuilder()
        sb.append(begin).append(" — GENERATED from app-profile/app.yaml#network.access_points.\n")
        sb.append("    // Edit access points THERE (the SoT) and run `./gradlew syncForkConfig`; do not hand-edit this block.\n")
        sb.append("    // `type` defaults to UrlType(id.uppercase()) — value-class-equal to the AppUrlTypes.* constants.\n")
        sb.append("    /** This project's declared access points, in app-profile order. */\n")
        sb.append("    val points: List<AccessPoint> = listOf(\n")
        var count = 0
        // `main: true` marks the access point whose clients follow the RUNTIME main network config
        // (core-base NetworkConfigRepository). Two mains is a contradiction the app would only discover
        // at start-up, so it fails the sync instead.
        val mains = aps.mapNotNull { it as? Map<*, *> }.filter { it["main"] == true }.map { it["id"] }
        if (mains.size > 1) {
            throw org.gradle.api.GradleException(
                "syncForkConfig: app-profile network.access_points marks ${mains.size} points main: true $mains — mark exactly one",
            )
        }
        for (ap in aps) {
            val m = ap as? Map<*, *> ?: continue
            val id = m["id"]?.toString()?.takeIf { it.isNotBlank() } ?: continue
            val kind = if (m["type"]?.toString()?.trim()?.lowercase() == "supabase") "SUPABASE" else "REST"
            val baseUrl = m["base_url"]?.toString().orEmpty()
            val basePath = m["base_path"]?.toString()?.trim()?.takeIf { it.isNotBlank() }
            val host = m["loggable_host"]?.toString().orEmpty()
            val proxied = m["proxied_host"]?.toString()?.takeIf { it.isNotBlank() }
            // One field per line so the generated block stays under the detekt/ktlint max line length.
            sb.append("        AccessPoint(\n")
            sb.append("            id = \"${esc(id)}\",\n")
            sb.append("            kind = AccessPointKind.$kind,\n")
            sb.append("            baseUrl = \"${esc(baseUrl)}\",\n")
            if (basePath != null) sb.append("            basePath = \"${esc(basePath)}\",\n")
            sb.append("            loggableHost = \"${esc(host)}\",\n")
            if (proxied != null) sb.append("            proxiedHost = \"${esc(proxied)}\",\n")
            // Declared headers. A `value:` is baked in; a `runtime:` emits the KEY only — the value
            // is written to RuntimeHeaderStore at login and read again on every request, because a
            // credential captured when this singleton client was built could never become a token
            // obtained after sign-in.
            val authRaw = m["auth"]?.toString()?.trim()?.lowercase()
            val authScheme = when (authRaw) {
                "basic" -> "BASIC"
                "bearer" -> "BEARER"
                "oauth" -> "OAUTH"
                else -> "NONE"
            }
            val hdrs = (m["headers"] as? List<*>).orEmpty().mapNotNull { h ->
                val hm = h as? Map<*, *> ?: return@mapNotNull null
                val hname = hm["name"]?.toString()?.trim().orEmpty()
                if (hname.isEmpty()) return@mapNotNull null
                val hvalue = hm["value"]?.toString()
                val hruntime = hm["runtime"]?.toString()?.trim()
                when {
                    hruntime != null && hruntime.isNotEmpty() ->
                        "HeaderSpec(name = \"${esc(hname)}\", runtimeKey = \"${esc(hruntime)}\")"
                    hvalue != null ->
                        "HeaderSpec(name = \"${esc(hname)}\", value = \"${esc(hvalue)}\")"
                    // Neither set is a malformed row: emitting it would fail HeaderSpec's own
                    // require() at construction, i.e. at app start. Skip, and let NAP report it.
                    else -> null
                }
            }
            // A declared `auth:` emits its own Authorization spec, so no one writes that row by hand
            // (and no one gets the `Basic `/`Bearer ` prefix wrong). An EXPLICIT Authorization row
            // still wins — a fork with a non-standard scheme keeps full control.
            val hasExplicitAuthHeader = hdrs.any { it.contains("name = \"Authorization\"") }
            val allHdrs = if (authScheme != "NONE" && !hasExplicitAuthHeader) {
                hdrs + "HeaderSpec(name = \"Authorization\", runtimeKey = \"${esc(id)}.auth\")"
            } else {
                hdrs
            }
            if (authScheme != "NONE") sb.append("            auth = AuthScheme.$authScheme,\n")
            if (m["main"] == true) sb.append("            isMain = true,\n")
            if (kind == "SUPABASE") {
                // The key is a BuildKonfig REFERENCE (build-time env / secrets read, by FQN so the file needs
                // no import), never a value — nothing secret is written here. No env -> "" -> client inert.
                val keyEnv = supabasePublishableKeyEnv(m)
                val keyExpr = keyEnv?.let { "kpt.core.network.BuildKonfig.$it" } ?: "\"\""
                sb.append("            supabase = kpt.core.base.network.SupabaseSpec(\n")
                sb.append("                publishableKey = $keyExpr,\n")
                sb.append("            ),\n")
            }
            if (allHdrs.isNotEmpty()) {
                sb.append("            headers = listOf(\n")
                allHdrs.forEach { sb.append("                ").append(it).append(",\n") }
                sb.append("            ),\n")
            }
            sb.append("        ),\n")
            count++
        }
        sb.append("    )\n")
        appendNetworkPolicy(sb, network)
        sb.append("    ").append(end)
        val endLineEnd = text.indexOf('\n', endIdx).let { if (it < 0) text.length else it }
        file.writeText(text.substring(0, beginIdx) + sb.toString() + text.substring(endLineEnd))
        logger.lifecycle("syncForkConfig: regenerated AppAccessPoints.points from app-profile ($count access points)")
    }

    /**
     * Emit `AppAccessPoints.policy` — the app-wide network privacy policy — from `network.logging` and
     * `network.web_cors_proxy`. Every key is optional and absent means the SECURE default (release logs
     * nothing, no CORS relay), so a fork whose app.yaml predates these keys becomes safer on sync, never
     * louder. A value that would ship personal data fails the sync instead: release `body` logging, or a
     * relay that is not https.
     */
    private fun appendNetworkPolicy(sb: StringBuilder, network: Map<*, *>) {
        val logging = (network["logging"] as? Map<*, *>).orEmpty()
        fun detail(key: String, allowed: List<String>, default: String): String {
            val raw = logging[key]?.toString()?.trim()?.lowercase()?.takeIf { it.isNotEmpty() } ?: return default
            if (raw !in allowed) {
                throw org.gradle.api.GradleException(
                    "syncForkConfig: network.logging.$key '$raw' must be one of $allowed" +
                        if (key == "release" && raw == "body") {
                            " — request/response bodies carry personal data and must never be logged by a release build."
                        } else {
                            "."
                        },
                )
            }
            return raw
        }
        val release = detail("release", listOf("none", "info", "headers"), "none")
        val debug = detail("debug", listOf("none", "info", "headers", "body"), "body")
        val identifier = Regex("^[A-Za-z0-9_.-]+$")
        val redact = (logging["redact_query_params"] as? List<*>).orEmpty().map { it.toString().trim() }
        redact.firstOrNull { !identifier.matches(it) }?.let {
            throw org.gradle.api.GradleException("syncForkConfig: network.logging.redact_query_params entry '$it' is not a query-parameter name.")
        }
        val proxy = network["web_cors_proxy"]?.toString()?.trim()?.takeIf { it.isNotEmpty() && it != "none" }
        // A path ("/cors") is a same-origin relay the site serves itself (cmp-web's _worker.js on
        // Cloudflare Pages), so it is as private as the site; anything else must be https.
        if (proxy != null && !proxy.startsWith("https://") && !Regex("^/[A-Za-z0-9_-]+$").matches(proxy)) {
            throw org.gradle.api.GradleException(
                "syncForkConfig: network.web_cors_proxy '$proxy' must be an https:// URL, a same-origin path like /cors, " +
                    "or omitted for no relay.",
            )
        }
        fun esc(v: String) = v.replace("\\", "\\\\").replace("\"", "\\\"")
        sb.append("\n    /** Network privacy policy — app-profile `network.logging` / `network.web_cors_proxy`. */\n")
        sb.append("    val policy: NetworkPolicy = NetworkPolicy(\n")
        sb.append("        releaseLogging = HttpLogDetail.${release.uppercase()},\n")
        sb.append("        debugLogging = HttpLogDetail.${debug.uppercase()},\n")
        if (redact.isNotEmpty()) {
            sb.append("        redactQueryParams = listOf(").append(redact.joinToString(", ") { "\"${esc(it)}\"" }).append("),\n")
        }
        proxy?.let { sb.append("        webCorsProxy = \"${esc(it)}\",\n") }
        sb.append("    )\n")
    }

    /** The `network.access_points` list, normalized to maps with a non-blank `id`. Empty when absent. */
    private fun accessPoints(appProfile: Map<String, Any?>): List<Map<*, *>> {
        val network = appProfile["network"] as? Map<*, *> ?: return emptyList()
        val aps = network["access_points"] as? List<*> ?: return emptyList()
        return aps.mapNotNull { it as? Map<*, *> }
            .filter { it["id"]?.toString()?.isNotBlank() == true }
    }

    private fun isSupabase(m: Map<*, *>): Boolean =
        m["type"]?.toString()?.trim()?.lowercase() == "supabase"

    /**
     * Replace the text between [begin] and [end] sentinels in [file] with [body].
     *
     * No-op when the file or either sentinel is absent — a fork that stripped the demo wiring (or
     * removed the block) is not an error, it is a fork that opted out. Returns true when it wrote.
     */
    private fun patchSentinel(file: File, begin: String, end: String, body: String): Boolean {
        if (!file.isFile) return false
        val text = file.readText()
        val b = text.indexOf(begin)
        val e = text.indexOf(end)
        if (b < 0 || e < 0 || e < b) return false
        val endLineEnd = text.indexOf('\n', e).let { if (it < 0) text.length else it }
        file.writeText(text.substring(0, b) + body + text.substring(endLineEnd))
        return true
    }

    /**
     * Regenerate `AppUrlTypes` from the declared access points.
     *
     * [AccessPoint.type] defaults to `UrlType(id.uppercase())`, so the vocabulary is a pure projection
     * of the id list — yet it was hand-maintained and had drifted to 3 constants against 8 declared
     * points. That drift is silent AND wrong-answering: `AppMultiUrlConfigProvider.getBaseUrl` falls
     * back to `UrlType.MAIN` for an unknown type, so a lookup for an undeclared id returned the MAIN
     * base URL instead of failing. Generating it removes the class of bug rather than the instance.
     */
    private fun regenerateUrlTypes(root: File, appProfile: Map<String, Any?>) {
        val points = accessPoints(appProfile)
        if (points.isEmpty()) return
        val file = File(root, "core/network/src/commonMain/kotlin/kpt/core/network/config/AppUrlTypes.kt")
        val sb = StringBuilder()
        sb.append("// syncForkConfig:url-types:begin — GENERATED from app-profile/app.yaml#network.access_points.\n")
        sb.append("    // One constant per declared access point (UrlType(id.uppercase()), matching\n")
        sb.append("    // AccessPoint.type's default). Edit the access points THERE; do not hand-edit this block.\n")
        val names = mutableListOf<String>()
        for (m in points) {
            val id = m["id"].toString()
            // The KEY must be exactly `id.uppercase()` — that is what AccessPoint.type defaults to, and
            // UrlType equality is what AccessPointRegistry.restBaseUrl matches on. Only the Kotlin
            // IDENTIFIER is sanitized (an id may contain characters an identifier cannot). Sanitizing
            // the key too would silently break every hyphenated id: `pay-gw` would declare
            // UrlType("PAY_GW") while its access point carries UrlType("PAY-GW"), so restBaseUrl would
            // miss and getBaseUrl would fall back to MAIN's URL — the exact bug this codegen removes.
            val key = id.uppercase()
            val name = key.replace(Regex("[^A-Z0-9]"), "_")
            names += name
            val kindDoc = if (isSupabase(m)) "Supabase" else "REST"
            sb.append("\n    /** `$id` — $kindDoc access point. */\n")
            if (key == "MAIN") {
                sb.append("    val MAIN: UrlType = UrlType.MAIN\n")
            } else {
                sb.append("    val $name: UrlType = UrlType(\"$key\")\n")
            }
        }
        // One entry per line: a fork with many endpoints would otherwise generate a single line past
        // any sane max-line-length, and the formatter cannot reflow generated output for us.
        sb.append("\n    /** Every declared endpoint type, in app-profile order. */\n")
        sb.append("    val all: List<UrlType> = listOf(\n")
        names.forEach { sb.append("        ").append(it).append(",\n") }
        sb.append("    )\n")
        sb.append("    // syncForkConfig:url-types:end")
        if (patchSentinel(file, "// syncForkConfig:url-types:begin", "// syncForkConfig:url-types:end", sb.toString())) {
            logger.lifecycle("syncForkConfig: regenerated AppUrlTypes (${names.size} types)")
        }
    }

    /**
     * A Supabase point's `publishable_key_env:`, or null when undeclared (the client then stays inert).
     *
     * Fails on the retired `anon_key_env:` instead of ignoring it: ignored, a fork's key would silently
     * vanish on the next sync and its client would go inert with no error anywhere.
     */
    private fun supabasePublishableKeyEnv(m: Map<*, *>): String? {
        if (m.containsKey("anon_key_env")) {
            throw org.gradle.api.GradleException(
                "syncForkConfig: access point '${m["id"]}' declares `anon_key_env:` — renamed to `publishable_key_env:` " +
                    "(Supabase's client key is the publishable key; the legacy anon JWT still works as its value).",
            )
        }
        return m["publishable_key_env"]?.toString()?.trim()?.takeIf { it.isNotEmpty() }
    }

    /**
     * Regenerate `AppReviewConfig` — store identity + prompt policy for the in-app review flow.
     *
     * Every id here already exists in app-profile for the DEPLOY side (the Android applicationId, the
     * App Store numeric id the TestFlight lane uploads against, the Partner Center Store ID). The
     * running app could not read any of them, so `AppReviewManagerImpl`'s documented fork step —
     * `AppReview.configure(StoreListing(...))` — had no source to draw from and was never performed.
     * Projecting rather than re-declaring keeps one SoT per id.
     *
     * Absent policy keys fall back to conservative defaults (disabled, no prompting) rather than to
     * an enabled-with-zero-thresholds state, which would prompt on first launch.
     */
    private fun regenerateAppReviewConfig(root: File, appProfile: Map<String, Any?>) {
        if (appProfile.isEmpty()) return
        val file = File(
            root,
            "core/platform/src/commonMain/kotlin/kpt/core/platform/config/AppReviewConfig.kt",
        )

        // PLACEHOLDER ids are treated as absent, matching resolve-deploy-config.sh's `pick`. A
        // literal "YOUR_TEAM_ID"-class value in a StoreListing produces a store URL that 404s —
        // worse than an empty listing, which at least reports canRequestReview == false honestly.
        fun id(key: String): String =
            appProfileGet(appProfile, key)?.trim().orEmpty()
                .takeUnless { it.isEmpty() || it.startsWith("YOUR_") || it.contains("example.com") }
                .orEmpty()

        val review = appProfile["in_app_review"] as? Map<*, *>
        fun policy(k: String): String? = review?.get(k)?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        val enabled = policy("enabled")?.lowercase() == "true"
        val minLaunches = policy("min_launches")?.toIntOrNull() ?: 0
        val minDays = policy("min_days_since_install")?.toIntOrNull() ?: 0
        val cooldown = policy("cooldown_days")?.toIntOrNull() ?: 0

        val play = id("app.id")
        val appStore = id("apple.app.store.id")
        val microsoft = id("windows.store.id")
        val web = id("org.marketing.url")

        val sb = StringBuilder()
        sb.append("// syncForkConfig:app-review:begin — GENERATED from app-profile. Do not hand-edit.\n")
        sb.append("    /** Play Store package — `identity.app_id`. */\n")
        sb.append("    const val PLAY_STORE_PACKAGE: String = \"").append(play).append("\"\n\n")
        sb.append("    /** App Store numeric id — `platforms/apple/apple.yaml#apple.app_store_id`. */\n")
        sb.append("    const val APP_STORE_ID: String = \"").append(appStore).append("\"\n\n")
        sb.append("    /** Microsoft Store product id — `platforms/windows/windows.yaml#windows.store_id`. */\n")
        sb.append("    const val MICROSOFT_STORE_PRODUCT_ID: String = \"").append(microsoft).append("\"\n\n")
        sb.append("    /** Open-web fallback for targets with no store — `org.marketing_url`. */\n")
        sb.append("    const val WEB_URL: String = \"").append(web).append("\"\n\n")
        sb.append("    /** `in_app_review.enabled` — false disables the custom prompt entirely. */\n")
        sb.append("    const val ENABLED: Boolean = ").append(enabled).append("\n\n")
        sb.append("    /** `in_app_review.min_launches`. */\n")
        sb.append("    const val MIN_LAUNCHES: Int = ").append(minLaunches).append("\n\n")
        sb.append("    /** `in_app_review.min_days_since_install`. */\n")
        sb.append("    const val MIN_DAYS_SINCE_INSTALL: Int = ").append(minDays).append("\n\n")
        sb.append("    /** `in_app_review.cooldown_days`. */\n")
        sb.append("    const val COOLDOWN_DAYS: Int = ").append(cooldown).append("\n")
        sb.append("    // syncForkConfig:app-review:end")

        if (patchSentinel(
                file,
                "// syncForkConfig:app-review:begin",
                "// syncForkConfig:app-review:end",
                sb.toString(),
            )
        ) {
            val ids = listOf(play, appStore, microsoft, web).count { it.isNotEmpty() }
            logger.lifecycle(
                "syncForkConfig: regenerated AppReviewConfig ($ids/4 store ids, enabled=$enabled)",
            )
        }
    }

    /**
     * Regenerate `AppSecurityConfig` — the security defaults a fresh install starts with, from
     * `app.yaml#security`. Today: whether screen capture is allowed before the user chooses
     * (`UserData.DEFAULT.enableScreenCapture` reads it). An absent or unparseable value keeps the
     * secure default (`false`) rather than silently opening capture. And whether a fresh install starts
     * signed in (`UserData.DEFAULT.isAuthenticated`), where absent means `true` — see the inline note.
     */
    private fun regenerateAppSecurityConfig(root: File, appProfile: Map<String, Any?>) {
        if (appProfile.isEmpty()) return
        val file = File(root, "core/model/src/commonMain/kotlin/kpt/core/model/config/AppSecurityConfig.kt")
        val security = appProfile["security"] as? Map<*, *>
        val allowCapture = screenCaptureAllowedByDefault(security)
        val signedIn = signedInByDefault(security)
        val onboarding = firstRunOnboarding(security)

        val sb = StringBuilder()
        sb.append("// syncForkConfig:app-security:begin — GENERATED from app-profile/app.yaml#security. Do not hand-edit.\n")
        sb.append("    /** `security.screen_capture_allowed_by_default` — whether a fresh install allows screenshots + recording. */\n")
        sb.append("    const val SCREEN_CAPTURE_ALLOWED_BY_DEFAULT: Boolean = ").append(allowCapture).append("\n\n")
        sb.append("    /** `security.signed_in_by_default` — whether a fresh install starts signed in (false = the app has a login). */\n")
        sb.append("    const val SIGNED_IN_BY_DEFAULT: Boolean = ").append(signedIn).append("\n\n")
        sb.append("    /** `security.first_run_onboarding` — whether a fresh install shows onboarding once (false = skip it). */\n")
        sb.append("    const val FIRST_RUN_ONBOARDING: Boolean = ").append(onboarding).append("\n")
        sb.append("    // syncForkConfig:app-security:end")

        if (patchSentinel(file, "// syncForkConfig:app-security:begin", "// syncForkConfig:app-security:end", sb.toString())) {
            logger.lifecycle("syncForkConfig: regenerated AppSecurityConfig (screenCaptureAllowedByDefault=$allowCapture, signedInByDefault=$signedIn, firstRunOnboarding=$onboarding)")
        }
    }

    /**
     * Regenerate `AppPrivacyConfig` from `app-profile/app.yaml#privacy` — the consent STARTING values for a
     * fresh install. Absent (or anything but `true`) means off: consent-based processing must be opt-in, so
     * a fork whose app.yaml predates the block syncs to the GDPR-safe default, never to collection.
     */
    private fun regenerateAppOrgLinks(root: File, appProfile: Map<String, Any?>) {
        if (appProfile.isEmpty()) return
        val file = File(root, "core/model/src/commonMain/kotlin/kpt/core/model/config/AppOrgLinks.kt")
        val links = ((appProfile["org"] as? Map<*, *>)?.get("links") as? Map<*, *>).orEmpty()
            .mapNotNull { (k, v) ->
                val name = k?.toString()?.trim().orEmpty()
                val url = v?.toString()?.trim().orEmpty()
                // A blank or non-http(s) value is a typo, not a link: skip it rather than ship a dead row.
                if (name.isEmpty() || !(url.startsWith("https://") || url.startsWith("http://"))) {
                    if (name.isNotEmpty()) logger.warn("syncForkConfig: org.links.$name is not an http(s) URL — skipped")
                    null
                } else {
                    name to url
                }
            }
        fun lit(s: String) = "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\\$") + "\""

        val sb = StringBuilder()
        sb.append("// syncForkConfig:org-links:begin — GENERATED from app-profile/app.yaml#org.links. Do not hand-edit.\n")
        if (links.isEmpty()) {
            sb.append("    private val links: Map<String, String> = emptyMap()\n")
        } else {
            sb.append("    private val links: Map<String, String> = linkedMapOf(\n")
            links.forEach { (name, url) -> sb.append("        ").append(lit(name)).append(" to ").append(lit(url)).append(",\n") }
            sb.append("    )\n")
        }
        sb.append("    // syncForkConfig:org-links:end")

        if (patchSentinel(file, "// syncForkConfig:org-links:begin", "// syncForkConfig:org-links:end", sb.toString())) {
            logger.lifecycle("syncForkConfig: regenerated AppOrgLinks (${links.size} link(s))")
        }
    }

    private fun regenerateAppPrivacyConfig(root: File, appProfile: Map<String, Any?>) {
        if (appProfile.isEmpty()) return
        val file = File(root, "core/model/src/commonMain/kotlin/kpt/core/model/config/AppPrivacyConfig.kt")
        val privacy = appProfile["privacy"] as? Map<*, *>
        fun flag(key: String) = privacy?.get(key)?.toString()?.trim()?.lowercase() == "true"
        val analytics = flag("analytics_enabled_by_default")
        val crash = flag("crash_reporting_enabled_by_default")
        // The privacy POLICY lives on the fork's website (org.privacy_url, the same URL the store listings use);
        // Settings → Privacy opens it in the in-app browser. Absent → "" and the link is not shown.
        val policyUrl = ((appProfile["org"] as? Map<*, *>)?.get("privacy_url")?.toString()?.trim().orEmpty())
            .takeIf { it.startsWith("https://") }.orEmpty()

        val sb = StringBuilder()
        sb.append("// syncForkConfig:app-privacy:begin — GENERATED from app-profile/app.yaml#privacy. Do not hand-edit.\n")
        sb.append("    /** `privacy.analytics_enabled_by_default` — whether a fresh install shares usage analytics. */\n")
        sb.append("    const val ANALYTICS_ENABLED_BY_DEFAULT: Boolean = ").append(analytics).append("\n\n")
        sb.append("    /** `privacy.crash_reporting_enabled_by_default` — whether a fresh install sends crash reports. */\n")
        sb.append("    const val CRASH_REPORTING_ENABLED_BY_DEFAULT: Boolean = ").append(crash).append("\n\n")
        sb.append("    /** `org.privacy_url` — the privacy policy page, opened in the in-app browser; empty when not set. */\n")
        sb.append("    const val PRIVACY_POLICY_URL: String =\n        \"").append(policyUrl.replace("\\", "\\\\").replace("\"", "\\\"")).append("\"\n")
        sb.append("    // syncForkConfig:app-privacy:end")

        if (patchSentinel(file, "// syncForkConfig:app-privacy:begin", "// syncForkConfig:app-privacy:end", sb.toString())) {
            logger.lifecycle("syncForkConfig: regenerated AppPrivacyConfig (analytics=$analytics, crashReporting=$crash)")
        }
    }

    private fun declaredStores(appProfile: Map<String, Any?>): List<Map<*, *>> {
        val block = (appProfile["core_store"] as? Map<*, *>).orEmpty()
        return ((block["stores"] as? List<*>) ?: emptyList<Any?>()).mapNotNull { it as? Map<*, *> }
    }

    private fun storeStr(row: Map<*, *>, key: String): String =
        row[key]?.toString()?.trim().orEmpty()

    /** `interestRateSeries` -> `INTEREST_RATE_SERIES`, matching the hand-written Ttl constants. */
    private fun screamingSnake(id: String): String =
        id.replace(Regex("([a-z0-9])([A-Z])"), "$1_$2").uppercase()

    /** `5m` / `1h` / `7d` -> a kotlin.time expression. Anything else is rejected loudly. */
    private fun ttlExpression(raw: String): String {
        val m = Regex("^(\\d+)(m|h|d)$").find(raw.trim())
            ?: error("core_store.stores[].ttl must look like 5m / 1h / 7d, got '$raw'")
        val n = m.groupValues[1]
        return when (m.groupValues[2]) {
            "m" -> "$n.minutes"
            "h" -> "$n.hours"
            else -> "$n.days"
        }
    }

    /**
     * Give every declared access point its OWN package under `core/network`:
     * `kpt/core/network/<id>/{api,dto}`.
     *
     * Endpoint code used to live in DOMAIN packages under `demo/` (`demo/economic` held BOTH the fred
     * and worldbank APIs), which tied it to the demo lifecycle: `remove-demo.sh` deletes every
     * a `demo` package, so a fork's endpoint code could not live beside the template's, and a
     * cleaned fork had nowhere structural to put an API at all. Naming the package for the ACCESS
     * POINT makes the layout a pure projection of app-profile — declare an endpoint, get a package,
     * write the interface in it — and lets the strip delete exactly the endpoints it removed.
     *
     * Scaffolds only; never overwrites. Each new package gets a README so git tracks the directory
     * and the next person knows what belongs there.
     */
    private fun scaffoldAccessPointPackages(root: File, appProfile: Map<String, Any?>) {
        val base = File(root, "core/network/src/commonMain/kotlin/kpt/core/network")
        if (!base.isDirectory) return
        var made = 0
        for (m in accessPoints(appProfile)) {
            val id = m["id"]?.toString()?.trim().orEmpty()
            // Package segments are lowercase alphanumerics: `my_api` -> `myapi`.
            val pkg = id.lowercase().filter { it.isLetterOrDigit() }
            if (pkg.isEmpty() || !pkg.first().isLetter()) continue
            val dir = File(base, pkg)
            val readme = File(dir, "README.md")
            if (readme.isFile) continue
            File(dir, "api").mkdirs()
            File(dir, "dto").mkdirs()
            val type = m["type"]?.toString()?.trim()?.lowercase() ?: "rest"
            val simple = pkg.replaceFirstChar { it.uppercase() }
            readme.writeText(
                buildString {
                    append("# `$id` — access point package\n\n")
                    append("SCAFFOLDED by `./gradlew syncForkConfig` from the `$id` access point in\n")
                    append("`app-profile/app.yaml#network.access_points`. One package per endpoint.\n\n")
                    append("- `api/` — the Ktorfit interface for this endpoint. Annotate it\n")
                    append("  `@ApiBinding(\"$id\")` and its Koin binding is GENERATED into\n")
                    append("  `di/GeneratedApiBindings.kt`; there is no wiring step.\n")
                    append("- `dto/` — the wire types this endpoint returns.\n\n")
                    if (type == "supabase") {
                        append("`type: supabase` — the binding is `supabaseApi(\"$id\") { ${'$'}{simple}Api(it) }`, so the\n")
                        append("interface takes a single `SupabaseConfigClient` constructor argument.\n")
                    } else {
                        append("`type: rest` — the binding is `restApi(\"$id\") { it.create${'$'}{simple}Api() }`, so Ktorfit\n")
                        append("generates the `create…Api()` factory from the interface.\n")
                    }
                    append("\nDelete this package by removing its access point from app-profile.\n")
                },
            )
            made++
        }
        if (made > 0) logger.lifecycle("syncForkConfig: scaffolded $made access-point package(s) under core/network")
    }

    /**
     * Refill the `syncForkConfig:buildkonfig` region of `core/network/build.gradle.kts` — one
     * `buildConfigField` per key a fork declares in app-profile.
     *
     * Closes the half of the endpoint contract that was missing: `syncForkConfig` generated the
     * REFERENCE (`AppAccessPoints` emits `BuildKonfig.<publishable_key_env>`) while the DECLARATION had
     * to be hand-added to this template-owned build file. A fork that declared a key env `X` got
     * `Unresolved reference: X` and no seam to fix it in. Now the declaration is derived from the same
     * SoT as the reference, so the two cannot drift.
     *
     * Sources: every access point's `publishable_key_env:` / `api_key_env:`, plus `network.build_config_fields`
     * for keys not tied to an endpoint. Names already declared OUTSIDE the region (the demo
     * `FRED_API_KEY`) are skipped — a duplicate `buildConfigField` fails the buildkonfig plugin.
     */
    private fun regenerateBuildKonfigFields(root: File, appProfile: Map<String, Any?>) {
        val file = File(root, "core/network/build.gradle.kts")
        if (!file.isFile) return
        val begin = "        // syncForkConfig:buildkonfig:begin"
        val end = "        // syncForkConfig:buildkonfig:end"
        val text = file.readText()
        if (!text.contains(begin) || !text.contains(end)) return

        // A BuildKonfig constant is a Kotlin identifier reached as `BuildKonfig.NAME`; anything else
        // would emit uncompilable source. Skip rather than emit (the api-binding generator did the same,
        // before @ApiBinding replaced it).
        val valid = Regex("^[A-Z][A-Z0-9_]*$")

        // (name -> env). LinkedHashMap keeps declaration order stable so the region does not churn.
        val fields = LinkedHashMap<String, String>()
        for (m in accessPoints(appProfile)) {
            for (key in listOf("publishable_key_env", "api_key_env")) {
                val env = m[key]?.toString()?.trim()?.takeIf { it.isNotEmpty() } ?: continue
                if (valid.matches(env)) fields.putIfAbsent(env, env)
            }
        }
        val network = (appProfile["network"] as? Map<*, *>).orEmpty()
        for (row in (network["build_config_fields"] as? List<*>) ?: emptyList<Any?>()) {
            val m = row as? Map<*, *> ?: continue
            val name = m["name"]?.toString()?.trim()?.takeIf { it.isNotEmpty() } ?: continue
            if (!valid.matches(name)) continue
            val env = m["from_env"]?.toString()?.trim()?.takeIf { it.isNotEmpty() } ?: name
            if (valid.matches(env)) fields.putIfAbsent(name, env)
        }

        // A BuildKonfig field is compiled INTO the app binary, where anyone can extract it. Only
        // client-safe values belong there (a Supabase publishable key, a public API key). A
        // privileged credential — Supabase service_role / secret key, a DB password or URL, a
        // management access token, a JWT secret, a private key — bypasses RLS or grants account
        // access, so a name that says it is one fails the sync instead of shipping. Such keys stay in
        // the vault for CI / Edge Functions (scripts/product-health/checks/supabase-client-security.sh
        // checks the values themselves).
        val privileged = Regex("SERVICE_ROLE|SERVICE_KEY|SECRET|PASSWORD|DB_URL|DATABASE_URL|ACCESS_TOKEN|JWT|PRIVATE_KEY")
        fields.entries.firstOrNull { (name, env) -> privileged.containsMatchIn(name) || privileged.containsMatchIn(env) }
            ?.let { (name, env) ->
                throw org.gradle.api.GradleException(
                    "syncForkConfig: app-profile asks to compile '$name' (env $env) into the app via BuildKonfig, " +
                        "but that name marks a privileged credential. Client apps may carry only client-safe keys " +
                        "(e.g. the Supabase publishable key); keep service-role/secret/DB/admin keys in the vault " +
                        "for CI or a Supabase Edge Function.",
                )
            }

        // env -> secrets/LAYOUT.yaml key, for every single-value `kind: value` row with a `rel:` (what the
        // resolver's export-paths map can resolve). The generated field then reads that file at build time.
        val secretKeys = layoutValueKeysByEnv(root)

        // Anything already declared outside the region wins — re-emitting it would be a duplicate.
        val outside = text.substringBefore(begin) + text.substringAfter(end)
        val existing = Regex("""buildConfigField\(\s*STRING,\s*"([A-Z0-9_]+)"""")
            .findAll(outside).map { it.groupValues[1] }.toSet()

        val body = buildString {
            append(begin).append(" — GENERATED from `app-profile/app.yaml`: one field per\n")
            append("        // access point declaring `publishable_key_env:`/`api_key_env:`, plus every `network.build_config_fields`\n")
            append("        // entry. DO NOT HAND-EDIT — declare the key in app-profile and re-run `./gradlew syncForkConfig`.\n")
            append("        // Values are read at BUILD time — the env var (CI), then the secrets/LAYOUT.yaml `kind: value` row whose\n")
            append("        // source_env matches (secrets/live/ else the secrets/sample/ fake), then local.properties (legacy).\n")
            append("        // No secret is committed.\n")
            for ((name, env) in fields) {
                if (name in existing) continue
                append("        buildConfigField(\n")
                // One argument per line: ktlint (spotlessKotlinGradle) rejects a multi-line call that puts two on one.
                append("            STRING,\n")
                append("            \"").append(name).append("\",\n")
                append("            System.getenv(\"").append(env).append("\")")
                secretKeys[env]?.let { append(" ?: project.readSecretValue(\"").append(it).append("\")") }
                append(" ?: localProps.getProperty(\"").append(env).append("\", \"\"),\n")
                append("        )\n")
            }
            append(end)
        }

        /** The file's content before patching, so the task can report only a real change. */
        val before = file.readText()
        patchSentinel(file, begin, end, body)
        if (file.readText() != before) {
            /**
             * How many fields were newly added, for the log line — distinct from the total, which would report work on
             * every run.
             */
            val emitted = fields.keys.count { it !in existing }
            logger.lifecycle("syncForkConfig: regenerated core/network buildkonfig fields ($emitted field(s))")
        }
    }

    /** Parse `app-profile/migration-ledger.yaml` -> ordered (from, to, specFqn?) rows. */
    private fun readLedgerVersion(root: File): Int? {
        val ledger = File(root, "app-profile/migration-ledger.yaml")
        if (!ledger.isFile) return null
        return ledger.readLines().firstNotNullOfOrNull { raw ->
            Regex("""^version:\s*(\d+)""").find(raw.substringBefore('#'))?.groupValues?.get(1)?.toInt()
        }
    }

    /**
     * Append any template migration unit this fork has not applied yet, at the fork's next free
     * version. APPEND-ONLY — an existing row is never renumbered.
     *
     * Room's version is one monotonic integer and migrations are edges between consecutive values,
     * so template and fork cannot share the counter. The previous `TEMPLATE_BASE_VERSION +
     * VERSION_OFFSET` did not separate them, it relabelled it: a fork at base 13 + offset 1 ships
     * v14, the template bumps base to 14, the fork computes 15, and devices sitting at 14 have no
     * 14 -> 15 edge. Here the fork owns the sequence outright and the template ships units with a
     * stable id and NO version, so the same unit can land at v14 in one fork and v27 in another.
     *
     * Renumbering is the one forbidden move: a shipped from/to edge is a contract with every
     * installed device, and rewriting it strands them.
     */
    private fun reconcileMigrationLedger(root: File, appProfile: Map<String, Any?>) {
        val ledger = File(root, "app-profile/migration-ledger.yaml")
        if (!ledger.isFile) return
        // Units live in their OWN template-owned file, not app.yaml: app.yaml is owner:merge and
        // holds both sides' declarations, so a template edit beside a fork edit is a 3-way merge.
        // migration-units.yaml is template-only and full-copies; the ledger is fork-only and is never
        // copied. Neither side writes the other's file.
        val unitsFile = File(root, "core/database/migration-units.yaml")
        val units = if (unitsFile.isFile) {
            unitsFile.readLines().mapNotNull { raw ->
                val line = raw.substringBefore('#')
                if (!line.trimStart().startsWith("-")) return@mapNotNull null
                Regex("""id:\s*([A-Za-z0-9_-]+)""").find(line)?.groupValues?.get(1)
            }
        } else {
            emptyList()
        }
        if (units.isEmpty()) return

        // Line-oriented read/write: the ledger is fork-authored and comment-heavy, and a YAML
        // round-trip would reformat it and drop every comment explaining why a row exists.
        val lines = ledger.readLines().toMutableList()
        val applied = mutableSetOf<String>()
        var version = 0
        var baselineIdx = -1
        var lastMigrationIdx = -1
        val unitRe = Regex("""unit:\s*([A-Za-z0-9_-]+)""")
        lines.forEachIndexed { i, raw ->
            val line = raw.substringBefore('#')
            Regex("""^version:\s*(\d+)""").find(line)?.let { version = it.groupValues[1].toInt() }
            if (Regex("""^baseline_units:""").containsMatchIn(line)) baselineIdx = i
            if (baselineIdx >= 0 && i == baselineIdx) {
                Regex("""\[(.*)\]""").find(line)?.groupValues?.get(1)
                    ?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }?.forEach { applied += it }
            }
            unitRe.find(line)?.let { applied += it.groupValues[1] }
            if (Regex("""^\s*-\s*\{.*from:""").containsMatchIn(line)) lastMigrationIdx = i
        }
        if (version <= 0 || lastMigrationIdx < 0) return

        val missing = units.filterNot { it in applied }
        if (missing.isEmpty()) return

        val additions = missing.map { id ->
            val from = version
            version += 1
            "  - { from: $from, to: $version, unit: $id }   # appended by syncForkConfig"
        }
        lines.addAll(lastMigrationIdx + 1, additions)
        val vIdx = lines.indexOfFirst { Regex("""^version:\s*\d+""").containsMatchIn(it.substringBefore('#')) }
        if (vIdx >= 0) lines[vIdx] = Regex("""^version:\s*\d+""").replace(lines[vIdx], "version: $version")
        ledger.writeText(lines.joinToString("\n") + "\n")
        logger.lifecycle(
            "syncForkConfig: appended ${missing.size} migration unit(s) to the ledger " +
                "(${missing.joinToString(", ")}) -> version $version",
        )
    }

    private fun deepMerge(a: Map<String, Any?>, b: Map<String, Any?>): Map<String, Any?> {
        val out = LinkedHashMap<String, Any?>(a)
        for ((k, v) in b) {
            val existing = out[k]
            out[k] = if (existing is Map<*, *> && v is Map<*, *>) {
                deepMerge(existing.toStringKeyedMap(), v.toStringKeyedMap())
            } else {
                v
            }
        }
        return out
    }

    // Resolve a flat fork.properties key against the merged app-profile map, via APP_PROFILE_MAP
    // (mirrors the Ruby AppProfile::MAP key-for-key). Returns null for an unmapped key or a missing
    // path; a container (map/list) leaf → null; a scalar leaf → its String form (mirrors Ruby to_s).
    private fun appProfileGet(data: Map<String, Any?>, key: String): String? {
        val path = APP_PROFILE_MAP[key] ?: return null
        var node: Any? = data
        for (seg in path.split(".")) {
            val m = node as? Map<*, *> ?: return null
            if (!m.containsKey(seg)) return null
            node = m[seg]
        }
        if (node is Map<*, *> || node is List<*>) return null
        return node?.toString()
    }

    /** The two vault-alias namespaces committed secret files carry: org-shared + per-app. */
    data class AliasNamespaces(
        /** Prefix of org-shared aliases, e.g. `mifos-x` in `mifos-x-match-password`. */
        val org: String,
        /** Prefix of per-app aliases, e.g. `kmp-project-template` in `kmp-project-template-upload-keystore`. */
        val project: String,
    )

    /**
     * The TEMPLATE's alias namespaces, declared once in the template-owned `secrets/LAYOUT.yaml`:
     *
     * ```yaml
     * alias_namespaces:
     *   org: <org-slug>          # prefix of org-shared aliases in the committed files
     *   project: <project-slug>  # prefix of per-app aliases in the committed files
     * ```
     *
     * Every fork receives that file unchanged on sync, so it always names what the committed aliases
     * carry — the code never hardcodes either value. Null (no block / no file) disables the rewrite.
     */
    /**
     * `source_env -> key` for every `kind: value` secret in `secrets/LAYOUT.yaml` that has a `rel:` and no
     * `by_flavor` — the rows the resolver's `export-paths` map resolves to a single file. Empty when the
     * file is missing or unparsable, so the generated fields fall back to env + local.properties.
     */
    private fun layoutValueKeysByEnv(root: File): Map<String, String> {
        val f = File(root, "secrets/LAYOUT.yaml")
        if (!f.isFile) return emptyMap()
        val doc = runCatching { f.inputStream().use { Yaml().load<Any?>(it) } }.getOrNull() as? Map<*, *>
            ?: return emptyMap()
        val out = LinkedHashMap<String, String>()
        for ((key, spec) in (doc["secrets"] as? Map<*, *>).orEmpty()) {
            val s = spec as? Map<*, *> ?: continue
            if (s["kind"] != "value" || s["rel"] == null || s["by_flavor"] != null) continue
            val env = s["source_env"]?.toString()?.trim()?.takeIf { it.isNotEmpty() } ?: continue
            out.putIfAbsent(env, key.toString())
        }
        return out
    }

    private fun readTemplateAliasNamespaces(root: File): AliasNamespaces? {
        val f = File(root, "secrets/LAYOUT.yaml")
        if (!f.isFile) return null
        var inBlock = false
        var org = ""
        var project = ""
        for (line in f.readLines()) {
            if (line.trimEnd() == "alias_namespaces:") {
                inBlock = true
                continue
            }
            if (!inBlock) continue
            if (line.isNotBlank() && !line.startsWith(" ")) break
            val m = Regex("""^\s+(org|project):\s*([A-Za-z0-9._-]+)""").find(line) ?: continue
            if (m.groupValues[1] == "org") org = m.groupValues[2] else project = m.groupValues[2]
        }
        return if (org.isNotBlank() && project.isNotBlank()) AliasNamespaces(org, project) else null
    }

    /**
     * Rewrite the template's vault-alias prefixes to this checkout's (RULE-SECRETS-NAMING-CONVENTION-001):
     * `<src.org>-…` → `<dstOrg>-…` (org-shared), then `<src.project>-…` → `<dstProject>-…` (per-app).
     * Each swap runs only when source and target differ, so it is a no-op on the template and an
     * org-shared alias keeps its prefix in a fork of the same org. The org swap runs FIRST so a per-app
     * alias produced by the project swap (which may itself begin with the org slug) is never rewritten
     * a second time.
     */
    private fun rewriteAliasPrefixes(text: String, src: AliasNamespaces?, dstOrg: String, dstProject: String): String {
        if (src == null) return text
        var out = text
        if (dstOrg.isNotBlank() && dstOrg != src.org) out = out.replace("${src.org}-", "$dstOrg-")
        if (dstProject.isNotBlank() && dstProject != src.project) out = out.replace("${src.project}-", "$dstProject-")
        return out
    }

    private fun parseTomlVersions(toml: File): Map<String, String> {
        if (!toml.exists()) return emptyMap()
        val result = mutableMapOf<String, String>()
        toml.readLines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("#") || !trimmed.contains("=")) return@forEach
            val eq = trimmed.indexOf('=')
            val key = trimmed.substring(0, eq).trim()
            // Drop inline TOML comment first, then strip quotes
            val raw = trimmed.substring(eq + 1).trim().substringBefore(" #").trim()
            result[key] = raw.removePrefix("\"").removeSuffix("\"").trim()
        }
        return result
    }

    private fun copyForkIcons(root: File) {
        val src = iconSourceDir.get().asFile
        if (!src.exists()) {
            logger.lifecycle("syncForkConfig: app-profile/icons/ not present — skipping icon copy (template defaults preserved)")
            return
        }

        val mappings = listOf(
            Triple("ios.png", iosAppIconDir.get().asFile, "AppIcon.png"),
            Triple("web-favicon.ico", jsResourcesDir.get().asFile, "favicon.ico"),
            Triple("web-favicon.ico", wasmJsResourcesDir.get().asFile, "favicon.ico"),
            Triple("desktop-macos.icns", desktopIconsDir.get().asFile, "ic_launcher.icns"),
            Triple("desktop-windows.ico", desktopIconsDir.get().asFile, "ic_launcher.ico"),
            Triple("desktop-linux.png", desktopIconsDir.get().asFile, "ic_launcher.png"),
        )
        var copied = 0
        for ((srcName, dstDir, dstName) in mappings) {
            val from = File(src, srcName)
            if (!from.exists()) continue
            dstDir.mkdirs()
            val to = File(dstDir, dstName)
            from.copyTo(to, overwrite = true)
            logger.lifecycle("syncForkConfig: copied app-profile/icons/$srcName → ${to.relativeTo(root)}")
            copied++
        }

        // Web link-preview image (og:image / twitter:image → ./og-image.png in index.html). SoT is
        // app-profile/platforms/web/media/og-images/og-image.png → both web resource roots. The >1KB guard
        // skips the .gitkeep-sized placeholder stubs so a fork never ships a broken 0-byte preview; without
        // a real og-image the token falls back to a 404 (no wrong-brand image), which beats the template's
        // hardcoded mobile-wallet github URL. (2026-08-10 web white-label.)
        val ogSrc = File(root, "app-profile/platforms/web/media/og-images/og-image.png")
        if (ogSrc.isFile && ogSrc.length() > 1024L) {
            listOf(jsResourcesDir.get().asFile, wasmJsResourcesDir.get().asFile).forEach { dir ->
                dir.mkdirs()
                ogSrc.copyTo(File(dir, "og-image.png"), overwrite = true)
            }
            logger.lifecycle("syncForkConfig: copied app-profile web og-image → cmp-web/{js,wasmJs}Main/resources/og-image.png")
            copied++
        }

        val androidSrc = File(src, "android/res")
        if (androidSrc.isDirectory && androidSrc.list()?.isNotEmpty() == true) {
            val androidDst = androidResDir.get().asFile
            // MIRROR the launcher icon: app-profile/icons/android/res is the SoT (adaptive-only —
            // ic_launcher_foreground.webp per density + anydpi-v26 XML + values/ic_launcher_background.xml).
            // Strip ALL stale ic_launcher* first so a prior template's legacy square/round webp
            // (ic_launcher.webp / ic_launcher_round.webp) and old drawable vector adaptive
            // (drawable/ic_launcher_foreground.xml / _background.xml) do NOT coexist with the promoted
            // set — otherwise the device shows the wrong icon and res carries duplicate launcher defs.
            // ONLY ic_launcher* files are removed; every other resource (colors/themes/other drawables)
            // is untouched. (2026-08-09)
            androidDst.walkTopDown()
                .filter { it.isFile && it.name.startsWith("ic_launcher") }
                .toList()
                .forEach { it.delete() }
            androidSrc.copyRecursively(androidDst, overwrite = true)
            logger.lifecycle("syncForkConfig: mirrored app-profile/icons/android/res/ → ${androidDst.relativeTo(root)}/ (stale ic_launcher* stripped)")
            copied++
        } else {
            logger.lifecycle("syncForkConfig: app-profile/icons/android/res/ not present — use Android Studio Image Asset Studio (one-time per fork, commit the result).")
        }

        if (copied == 0) {
            logger.lifecycle("syncForkConfig: app-profile/icons/ contained no recognised files — template defaults preserved")
        }
    }

    /**
     * The flat-key → app-profile-path map, mirrored key-for-key with the Ruby side.
     */
    companion object {
        // Flat gradle/fork.properties key → dotted path in the merged app-profile map.
        // MIRRORS deployment/_shared/config.rb `AppProfile::MAP` KEY-FOR-KEY so the Ruby (fastlane)
        // and Kotlin (build) sides resolve the SAME dotted key to the SAME app-profile yaml path —
        // one white-label contract, two consumers. Keep the two in lockstep on any change.
        private val APP_PROFILE_MAP: Map<String, String> = mapOf(
            // ── identity / app ──
            "app.id" to "identity.app_id",
            "app.display.name" to "identity.app_name",
            "app.description" to "store.app_description",
            // ── network (B4): per-flavor endpoints + demo creds + log tag ──
            "network.base.url.demo" to "network.demo_base_url",
            "network.base.url.prod" to "network.prod_base_url",
            "demo.username" to "network.demo_username",
            "demo.password" to "network.demo_password",
            "log.tag" to "network.log_tag",
            // ── org ──
            "org.name" to "org.name",
            "org.email" to "org.email",
            "org.first.name" to "org.first_name",
            "org.secrets.namespace" to "org.secrets_namespace",
            "org.last.name" to "org.last_name",
            "org.phone" to "org.phone",
            "org.copyright" to "org.copyright",
            "org.marketing.url" to "org.marketing_url",
            "org.privacy.url" to "org.privacy_url",
            "org.support.url" to "org.support_url",
            // ── legal ──
            "legal.company.name" to "legal.company_name",
            "legal.jurisdiction" to "legal.jurisdiction",
            "legal.effective.date" to "legal.effective_date",
            "legal.contact.email" to "legal.contact_email",
            // ── keystore DN ──
            "keystore.dn.org_unit" to "keystore_dn.org_unit",
            "keystore.dn.city" to "keystore_dn.city",
            "keystore.dn.state" to "keystore_dn.state",
            "keystore.dn.country" to "keystore_dn.country",
            // ── store (common: iOS + macOS + Android) ──
            "store.primary.locale" to "store.primary_locale",
            "store.title" to "store.title",
            "store.subtitle" to "store.subtitle",
            "store.promotional.text" to "store.promotional_text",
            "store.release.notes" to "store.release_notes",
            "store.description" to "store.description",
            "store.copyright" to "store.copyright",
            "store.ios.age.rating" to "store.age_rating",
            "store.review.notes" to "store.review.notes",
            "store.review.demo.user" to "store.review.demo_user",
            "store.review.demo.password" to "store.review.demo_password",
            // ── android ──
            "store.android.category" to "android.category",
            "store.android.short.description" to "android.short_description",
            "store.android.changelog" to "android.changelog",
            "store.android.video.url" to "android.video_url",
            "android.play.tracks.by_flavor" to "android.play_tracks_by_flavor",
            "firebase.android.prod.app.id" to "android.firebase.app_id_prod",
            "firebase.android.demo.app.id" to "android.firebase.app_id_demo",
            "firebase.groups" to "android.firebase.groups",
            "play.testers.internal.googlegroup" to "android.play_testers.internal_googlegroup",
            "play.testers.closed.googlegroup" to "android.play_testers.closed_googlegroup",
            // ── apple (shared iOS + macOS) ──
            "apple.team.id" to "apple.team_id",
            // The App Store NUMERIC id. Two consumers that previously each had their own copy:
            // the TestFlight/App Store lanes (hardcoded) and AppReviewConfig (which had none, so
            // promptForCustomReview could not reach the listing on iOS).
            "apple.app.store.id" to "apple.app_store_id",
            "apple.match.git.url" to "apple.match.git.url",
            "apple.match.git.branch" to "apple.match.git.branch",
            "firebase.ios.prod.app.id" to "apple.firebase.ios_app_id_prod",
            "firebase.ios.demo.app.id" to "apple.firebase.ios_app_id_demo",
            "firebase.ios.app.id" to "apple.firebase.ios_app_id",
            "apple.testers.internal.group" to "apple.testers.internal_group",
            "apple.testers.external.group" to "apple.testers.external_group",
            "apple.testers.external.public.link" to "apple.testers.external_public_link",
            "store.ios.keywords" to "apple.keywords",
            "store.ios.category" to "apple.category",
            // ── apple / iOS-only ──
            "store.ios.secondary.category" to "apple.ios.secondary_category",
            "store.ios.apple.tv.privacy.url" to "apple.ios.apple_tv_privacy_url",
            // ── apple / macOS-only ──
            "store.macos.keywords" to "apple.macos.keywords",
            "store.macos.category" to "apple.macos.category",
            "store.macos.secondary.category" to "apple.macos.secondary_category",
            "mac.app.category" to "apple.macos.app_category",
            // ── web ──
            "web.cloudflare.project" to "web.cloudflare_project",
            // ── Windows / Microsoft Store (platforms/windows/windows.yaml, top-level `windows:`) ──
            "store.windows.ms.app.id" to "windows.ms_app_id",
            "store.windows.ms.publish.mode" to "windows.ms_publish_mode",
            "store.windows.ms.visibility" to "windows.ms_visibility",
            "windows.store.id" to "windows.store_id",
            "windows.msix.identity.name" to "windows.msix_identity_name",
            "windows.msix.identity.publisher" to "windows.msix_publisher",
            "windows.msix.publisher.display.name" to "windows.msix_publisher_display_name",
            "windows.partner.center.tenant.id" to "windows.partner_center.tenant_id",
            "windows.partner.center.client.id" to "windows.partner_center.client_id",
            // ── Ubuntu / Linux (platforms/ubuntu/ubuntu.yaml, top-level `ubuntu:`) ──
            "ubuntu.snap.name" to "ubuntu.snap.name",
            "ubuntu.snap.grade" to "ubuntu.snap.grade",
            "ubuntu.snap.confinement" to "ubuntu.snap.confinement",
            "ubuntu.snap.channel" to "ubuntu.snap.channel",
            "ubuntu.flathub.app.id" to "ubuntu.flathub.app_id",
            "ubuntu.deb.package" to "ubuntu.deb.package",
            "ubuntu.deb.section" to "ubuntu.deb.section",
            "ubuntu.deb.maintainer" to "ubuntu.deb.maintainer",
            "ubuntu.deb.homepage" to "ubuntu.deb.homepage",
            // ── apple / trade representative contact information ──
            "store.apple.trade.first.name" to "apple.trade_representative.first_name",
            "store.apple.trade.last.name" to "apple.trade_representative.last_name",
            "store.apple.trade.address.line1" to "apple.trade_representative.address_line1",
            "store.apple.trade.address.line2" to "apple.trade_representative.address_line2",
            "store.apple.trade.address.line3" to "apple.trade_representative.address_line3",
            "store.apple.trade.city" to "apple.trade_representative.city_name",
            "store.apple.trade.state" to "apple.trade_representative.state",
            "store.apple.trade.country" to "apple.trade_representative.country",
            "store.apple.trade.postal.code" to "apple.trade_representative.postal_code",
            "store.apple.trade.phone" to "apple.trade_representative.phone_number",
            "store.apple.trade.email" to "apple.trade_representative.email_address",
            "store.apple.trade.displayed" to "apple.trade_representative.is_displayed_on_app_store",
            // ── deploy-surface completeness (MS Store / winget / snap / flathub / aur / homebrew / ios-subcats / web) ──
            "win.store.name" to "windows.store.name",
            "win.store.short.description" to "windows.store.short_description",
            "win.store.description" to "windows.store.description",
            "win.store.category" to "windows.store.category",
            "win.store.privacy.url" to "windows.store.privacy_url",
            "win.winget.package.id" to "windows.winget.package_id",
            "win.winget.publisher" to "windows.winget.publisher",
            "win.winget.name" to "windows.winget.name",
            "win.winget.moniker" to "windows.winget.moniker",
            "ubuntu.snap.summary" to "ubuntu.snap.summary",
            "ubuntu.snap.description" to "ubuntu.snap.description",
            "ubuntu.flathub.developer" to "ubuntu.flathub.developer",
            "ubuntu.flathub.name" to "ubuntu.flathub.name",
            "ubuntu.flathub.summary" to "ubuntu.flathub.summary",
            "ubuntu.flathub.description" to "ubuntu.flathub.description",
            "ubuntu.aur.package.name" to "ubuntu.aur.package_name",
            "ubuntu.aur.source.type" to "ubuntu.aur.source_type",
            "mac.homebrew.cask.name" to "apple.macos.homebrew.cask_name",
            "mac.homebrew.tagline" to "apple.macos.homebrew.tagline",
            "store.ios.primary.first.subcategory" to "apple.ios.primary_first_sub_category",
            "store.ios.primary.second.subcategory" to "apple.ios.primary_second_sub_category",
            "store.ios.secondary.first.subcategory" to "apple.ios.secondary_first_sub_category",
            "store.ios.secondary.second.subcategory" to "apple.ios.secondary_second_sub_category",
            "web.custom.domain" to "web.custom_domain",
        )
    }
}
