# deployment/_shared/before_all.rb — regenerate the derived store listing before ANY lane.
#
# app-profile/ (app.yaml + platforms/<p>/*.yaml + platforms/<p>/media) is the SINGLE source of truth
# for all store-listing text + media. deployment/**/metadata is DERIVED (customization-surface
# owner:generated, gitignored) — fastlane's `metadata_path` reads that dir, so it MUST be materialized
# from app-profile via `./gradlew syncForkConfig` BEFORE deliver/supply run, or a fresh clone / CI
# deploy would upload EMPTY listings.
#
# Imported by BOTH deployment/Fastfile (CI entry) and deployment/fastlane/Fastfile (local) so it runs
# whichever Fastfile fastlane loads. Runs once per invocation (SYNC_FORK_DONE latch — /idea-deploy
# STEP 1.7.0 already sets it when it pre-syncs). Fail-soft: a gradle hiccup never blocks a deploy.
# Files fastlane's match / gym / version steps write back into the TRACKED source tree during a build:
# the resolved signing identity into cmp-ios project.pbxproj (PRODUCT_BUNDLE_IDENTIFIER +
# PROVISIONING_PROFILE_SPECIFIER = "match AppStore <id>") and the computed version into Info.plist.
# Committing those RE-HARDCODES the template bundle id into every fork's Xcode project
# (breaking white-label) and trips scripts/product-health/checks/ios-pbxproj-identity.sh. A deploy must
# NEVER dirty tracked source, so restore this allow-list to its committed state after every lane
# (success AND error) — the deploy already built/uploaded from the mutated files; we just clean the
# tree so `git add -A` (git-session-commit) can't sweep the writeback in. (2026-08-09.)
DEPLOY_MUTATED_SOURCES = [
  "cmp-ios/iosApp.xcodeproj/project.pbxproj",
  "cmp-ios/iosApp/Info.plist",
].freeze

# Walk up from the working directory to the repo root, recognised by app-profile/app.yaml.
# Bounded at 20 levels so a lane run from an unexpected directory fails fast instead of
# walking to /.
def _deploy_repo_root
  root = Dir.pwd
  20.times do
    break if File.exist?(File.join(root, "app-profile", "app.yaml"))
    parent = File.dirname(root)
    break if parent == root
    root = parent
  end
  root
end

# Restore every source file a lane rewrote in place (version strings, Info.plist entries).
# Run unconditionally after a lane so a local build does not leave the tree dirty — the
# same files on CI are throwaway, but on a developer machine they are the working copy.
def restore_deploy_mutated_sources
  root = _deploy_repo_root
  DEPLOY_MUTATED_SOURCES.each do |rel|
    next unless File.exist?(File.join(root, rel))
    # tracked-only, restore to HEAD: discards the deploy's signing/version writeback. No-op if unchanged.
    next unless system("git", "-C", root, "ls-files", "--error-unmatch", "--", rel, out: File::NULL, err: File::NULL)
    system("git", "-C", root, "checkout", "--", rel, out: File::NULL, err: File::NULL)
  end
end

before_all do
  # ── INTERPRETER GUARD — the misleading-failure catcher ──────────────────────────────────────────
  # rbenv works by PATH interception. A shell without `eval "$(rbenv init -)"` never reaches the
  # pinned ruby, so `bundle exec fastlane …` runs under macOS's system 2.6.10 while every gem was
  # installed for .ruby-version. The result is a crash inside rubygems' `activate_bin_path` that
  # reads as a GEM problem — the gems are fine, the interpreter is wrong.
  #
  # Shell scripts avoid this by going through scripts/ruby-exec.sh (RT-9 enforces it). But the READMEs
  # and each target's config.yaml `local:` command DOCUMENT a bare `bundle exec fastlane …` for a
  # human to copy — correct guidance for a correctly-configured shell, and nothing a lint can fix.
  # This is the one place every lane passes through, so it is where that human gets told the truth
  # instead of a gem error.
  #
  # WARN, never fail: a mismatched interpreter that still has working gems can deploy fine, and
  # blocking a release on a shell-config nit would be its own defect.
  begin
    _root = Dir.pwd
    20.times do
      break if File.exist?(File.join(_root, ".ruby-version"))
      _parent = File.dirname(_root)
      break if _parent == _root
      _root = _parent
    end
    _vf = File.join(_root, ".ruby-version")
    if File.exist?(_vf)
      _want = File.read(_vf).strip
      if !_want.empty? && _want != RUBY_VERSION
        UI.important("⚠️  ruby #{RUBY_VERSION} is running, but .ruby-version pins #{_want}.")
        UI.important("    If this run dies inside rubygems' activate_bin_path, THAT is the cause —")
        UI.important("    the gems are fine, the interpreter is wrong. Fix the shell:")
        UI.important("      eval \"$(rbenv init -)\"     (or: rbenv install #{_want})")
      end
    end
  rescue StandardError
    nil # a guard that breaks a deploy is worse than the mismatch it reports
  end

  # START-CLEAN: remove any fastlane_tmp keychain left DEFAULT by a prior run that died before
  # delete_keychain (build error / hard kill), and restore the developer's login keychain as default —
  # otherwise unrelated apps keep prompting for fastlane_tmp. Idempotent, no-op on CI / Android.
  cleanup_ci_keychain rescue nil

  # Locate repo root (dir holding app-profile/app.yaml) by walking up from cwd — robust to whatever
  # cwd fastlane sets for the loaded Fastfile (deployment/ for CI, repo root for local).
  repo_root = Dir.pwd
  20.times do
    break if File.exist?(File.join(repo_root, "app-profile", "app.yaml"))
    parent = File.dirname(repo_root)
    break if parent == repo_root
    repo_root = parent
  end

  # iOS: the "[KMP] Embed and Sign ComposeApp XCFramework" Run Script phase declares
  # cmp-ios/kotlin-sources.xcfilelist as an input. Xcode resolves inputFileListPaths at
  # GRAPH-CONSTRUCTION time, before any phase runs, so the list cannot self-heal during a build —
  # a MISSING list is a hard "Unable to load contents of file list" failure, and a STALE one lets
  # Xcode skip the phase after a Kotlin edit and ship stale code. Ensure it before gym/xcodebuild.
  # The generator is write-only-if-changed, so an in-sync run costs nothing and does NOT invalidate
  # the phase (rewriting it unconditionally would re-trigger the ~8-11 min Gradle path every build).
  filelist_gen = File.join(repo_root, "cmp-ios", "scripts", "gen-kotlin-filelist.sh")
  if File.exist?(filelist_gen)
    begin
      Dir.chdir(repo_root) { sh("bash", filelist_gen) }
    rescue => e
      UI.important("gen-kotlin-filelist.sh failed (#{e.message}) — iOS build may re-run Gradle or use a stale list")
    end
  end

  if ENV["SYNC_FORK_DONE"].nil? && File.exist?(File.join(repo_root, "app-profile", "app.yaml"))
    UI.message("app-profile → deployment/**/metadata: materializing store listing via ./gradlew syncForkConfig…")
    begin
      Dir.chdir(repo_root) { sh("./gradlew", "-q", "syncForkConfig") }
    rescue => e
      UI.important("syncForkConfig failed (#{e.message}) — deployment/metadata may be stale; using existing files")
    end
    ENV["SYNC_FORK_DONE"] = "1"
  end
end

# END-CLEAN: guaranteed fastlane_tmp keychain teardown after EVERY lane — on success (after_all) AND on
# failure/exception (error). Pairs with the setup_ci create_keychain so a local run never leaves
# fastlane_tmp as the macOS default keychain. (A hard SIGKILL still can't run these — the before_all
# start-clean above catches that leftover on the next run.) Idempotent, no-op on CI / Android.
after_all do |_lane, _options|
  cleanup_ci_keychain rescue nil
  restore_deploy_mutated_sources rescue nil
end

error do |_lane, _exception, _options|
  cleanup_ci_keychain rescue nil
  restore_deploy_mutated_sources rescue nil
end
