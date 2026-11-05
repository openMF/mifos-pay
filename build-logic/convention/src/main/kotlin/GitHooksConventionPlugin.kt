/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import java.util.Locale

/**
 * Plugin that installs the pre-commit git hooks from the scripts directory.
 */
class GitHooksConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Define a function to check if the OS is Linux or MacOS
        fun isLinuxOrMacOs(): Boolean {
            val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
            return osName.contains("linux") || osName.contains("mac os") || osName.contains("macos")
        }

        // Resolve the real git hooks directory. When the fork is checked out as a git SUBMODULE (or a
        // linked worktree), `.git` is a FILE ("gitdir: <path>") pointing at the module's real git dir
        // (e.g. <super>/.git/modules/<sub>), NOT a `.git/hooks` directory — so hardcoding
        // "${rootDir}/.git/hooks" fails config validation ("Expected .git to be a directory but it's a
        // file"). Resolve both layouts; fall back to the plain path when there is no `.git` yet.
        val gitEntry = project.rootDir.resolve(".git")
        val gitHooksDir = when {
            gitEntry.isDirectory -> gitEntry.resolve("hooks")
            gitEntry.isFile -> {
                val gitDir = gitEntry.readText().lineSequence()
                    .firstOrNull { it.startsWith("gitdir:") }
                    ?.substringAfter("gitdir:")?.trim()
                if (gitDir != null) {
                    project.rootDir.resolve(gitDir).normalize().resolve("hooks")
                } else {
                    gitEntry.resolve("hooks")
                }
            }
            else -> gitEntry.resolve("hooks")
        }

        // Define the copyGitHooks task
        project.tasks.register<Copy>("copyGitHooks") {
            description = "Copies the git hooks from /scripts to the git hooks folder."
            // No usable git hooks dir (e.g. a source export with no .git at all) → skip cleanly.
            onlyIf { gitHooksDir.parentFile?.exists() == true }
            // Exactly the two hooks, at the hooks ROOT — git runs nothing else. Copying `scripts/**/*.sh`
            // kept the subdirectories, so after the hooks moved to scripts/ci/ they landed in hooks/ci/
            // where git never looks, and every repo kept running a stale whole-repo hook from before.
            from("${project.rootDir}/scripts/ci/") {
                include("pre-commit.sh", "pre-push.sh")
                rename { it.removeSuffix(".sh") }
            }
            into(gitHooksDir)
        }

        // Define the installGitHooks task
        project.tasks.register<Exec>("installGitHooks") {
            description = "Installs the pre-commit git hooks from the scripts directory."
            group = "git hooks"
            workingDir = project.rootDir
            // Resolve the same real hooks dir as copyGitHooks (submodule/worktree safe), and skip cleanly
            // when there is none rather than chmod-ing a path that doesn't exist under rootDir.
            onlyIf { gitHooksDir.exists() }

            if (isLinuxOrMacOs()) {
                commandLine("chmod", "-R", "+x", gitHooksDir.absolutePath)
            } else {
                commandLine("cmd", "/c", "attrib", "-R", "+X", "${gitHooksDir.absolutePath}\\*.*")
            }
            dependsOn(project.tasks.named("copyGitHooks"))

            doLast {
                println("Git hooks installed successfully.")
            }
        }

        // Configure task dependencies after evaluation
        project.afterEvaluate {
            project.tasks.matching {
                it.name in listOf("preBuild", "build", "assembleDebug", "assembleRelease", "installDebug", "installRelease", "clean")
            }.configureEach {
                dependsOn(project.tasks.named("installGitHooks"))
            }
        }
    }
}
