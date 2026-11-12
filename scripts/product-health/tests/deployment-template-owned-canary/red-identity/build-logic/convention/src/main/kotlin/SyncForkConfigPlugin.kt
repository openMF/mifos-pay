// Canary fixture — the pre-2026-10-29 SyncForkConfigPlugin writes into template-owned deployment/ (verbatim lines).
class Fixture {
    fun run(root: java.io.File) {
        val storeBrokerFile = File(root, "deployment/desktop/microsoft-store/StoreBroker.config.json")
            val wrangler = File(root, "deployment/web/cloudflare-pages/wrangler.toml")
                "deployment/web/cloudflare-pages/config.yaml",
                "deployment/web/cloudflare-pages/workflow-snippet.yml",
        val appx = File(root, "deployment/desktop/microsoft-store/Package.appxmanifest")
    }
}
