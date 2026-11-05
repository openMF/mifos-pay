# deployment/_shared/lib/version_helpers.rb
# Shared version-generation helpers extracted from legacy fastlane/Fastfile.
# Consumed by every lane.rb that needs to compute VERSION / VERSION_CODE.

module VersionHelpers
  module_function

  # Read version.txt — the version SOURCE OF TRUTH (written by `/idea-deploy [N]` + `./gradlew
  # versionFile`). Resolve it ROBUSTLY: DEPLOYMENT_REPO_ROOT, then walk up from THIS file
  # (deployment/_shared/lib) to the repo root (the dir with settings.gradle.kts), then CWD-relative.
  # The prior relative-only lookup silently yielded "1.0.0" whenever the fastlane CWD wasn't the repo
  # parent — which shipped a stale versionName even though version.txt was correct (2026-06-22 partial
  # fix; full walk-up fix 2026-08-31).
  def gradle_version
    roots = []
    if defined?(DEPLOYMENT_REPO_ROOT) && DEPLOYMENT_REPO_ROOT && !DEPLOYMENT_REPO_ROOT.to_s.empty?
      roots << DEPLOYMENT_REPO_ROOT.to_s
    end
    dir = File.expand_path("../..", __dir__) # deployment/
    8.times do
      roots << dir
      break if File.exist?(File.join(dir, "settings.gradle.kts")) || File.exist?(File.join(dir, "version.txt"))
      parent = File.expand_path("..", dir)
      break if parent == dir
      dir = parent
    end
    candidates = roots.map { |r| File.join(r, "version.txt") } + ["../version.txt", "version.txt"]
    path = candidates.uniq.find { |p| File.exist?(p) && !File.read(p).strip.empty? }
    path ? File.read(path).strip : "1.0.0"
  rescue StandardError
    "1.0.0"
  end

  # Sanitize a gradle semver (e.g. "2026.1.1-beta.0.9+abc123") to App-Store
  # compatible MAJOR.MINOR.PATCH form (e.g. "2026.1.9"). The final integer is
  # the commit-count extracted from the pre-release suffix.
  def appstore_sanitize(full_version)
    base = full_version.split("-")[0].split("+")[0]
    parts = base.split(".")
    commit_count = "0"
    if full_version.include?("-")
      pre = full_version.split("-")[1].split("+")[0]
      commit_count = pre.split(".").last || "0"
    end
    return full_version unless parts.length >= 2
    "#{parts[0]}.#{parts[1]}.#{commit_count}"
  end
end
