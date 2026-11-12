plugins { id("x") }
project.file("module-deps.gradle.kts").takeIf { it.exists() }?.let { apply(from = it) }
