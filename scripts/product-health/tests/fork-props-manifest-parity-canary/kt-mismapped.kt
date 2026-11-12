// Fixture: someone "fixed" the asymmetry by inventing an app-profile home for project.name, whose
// real SoT is the tracked version catalog. Mapping it gives derive.rb a second resolution order.
        private val APP_PROFILE_MAP: Map<String, String> = mapOf(
            "app.id" to "identity.app_id",
            "log.tag" to "network.log_tag",
            "project.name" to "identity.project_name",
        )
