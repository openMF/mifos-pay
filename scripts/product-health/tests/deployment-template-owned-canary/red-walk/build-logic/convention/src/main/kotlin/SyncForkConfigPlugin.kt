// Canary fixture — writes only inside generated deployment/ areas.
class Fixture {
    fun run(root: java.io.File) {
        writeLocalized("deployment/ios/appstore/metadata", "name.txt", "x")
        copyIfExists("app-profile/a.csv", "deployment/android/app-content/data-safety.csv")
        // a comment naming deployment/web/cloudflare-pages/wrangler.toml must not count
        val resolver = java.io.File(root, "deployment/_shared/lib/build_secrets.rb")
    }
}
class Walk { fun w(root: java.io.File) { java.io.File(root, "deployment").walkTopDown().forEach { } } }
