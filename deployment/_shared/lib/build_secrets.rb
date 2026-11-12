# deployment/_shared/lib/build_secrets.rb
#
# The ONE implementation of the secrets resolver. Reads secrets/LAYOUT.yaml (the
# single source of truth for the secrets layout) and answers, for any secret key:
#   - path(key)          → where the file is / will be (live wins, else sample)
#   - value(key)         → the string value (env-first, file fallback)
#   - exists?(key)       → is it materialized?
#   - materialize!(key)  → decode the env/literal into its on-disk path
#   - application_id     → flavor-aware applicationId (libs.versions.toml#appId + suffix)
#
# Exposed two ways from this one file: a Ruby API (BuildSecrets.for(...)) used by
# config.rb + the Fastlane lanes, and a CLI (the `if __FILE__ == $0` block) used by
# the GitHub Actions pre_fastlane_script via deployment/scripts/build-secrets.
#
# Stdlib-only (yaml/json/fileutils/base64) so it runs on the runner's SYSTEM Ruby,
# BEFORE Fastlane's Bundler is set up (pre_fastlane_script timing).
require "yaml"
require "fileutils"
require "base64"

module BuildSecrets
  # LAYOUT lives at <repo>/secrets/LAYOUT.yaml. This file is at
  # <repo>/deployment/_shared/lib/build_secrets.rb → three dirs up to the repo root.
  REPO_ROOT = File.expand_path("../../..", __dir__)
  LAYOUT    = File.join(REPO_ROOT, "secrets", "LAYOUT.yaml")
  # The fork's own secret rows (its client keys, its extra service files) — FORK-owned, so a sync never
  # touches it. LAYOUT.yaml is template-owned and replaced on every sync, which deleted any row a fork
  # added there. Rows here are ADDED to the template's; a key the template already declares is ignored
  # (with a warning) so a fork cannot silently redirect a template secret.
  FORK_LAYOUT = File.join(REPO_ROOT, "secrets", "fork-layout.yaml")

  # LAYOUT.yaml with the fork's rows from fork-layout.yaml added.
  def self.load_layout
    doc = YAML.load_file(LAYOUT)
    return doc unless File.exist?(FORK_LAYOUT)
    fork_rows = (YAML.load_file(FORK_LAYOUT) || {})["secrets"] || {}
    fork_rows.each do |key, row|
      if doc["secrets"].key?(key)
        warn "build_secrets: secrets/fork-layout.yaml '#{key}' ignored — LAYOUT.yaml already declares it"
      else
        doc["secrets"][key] = row
      end
    end
    doc
  end

  # Product-flavor → applicationId suffix. Mirrors org.convention.AppFlavor.
  FLAVOR_SUFFIX = { "prod" => "", "demo" => ".demo" }.freeze

  # An accessor bound to one flavor/variant pair, so every lookup resolves the right
  # `{flavor}` / `{variant}` placeholder without the caller repeating them.
  def self.for(flavor: :prod, variant: :release)
    Accessor.new(flavor.to_s, variant.to_s)
  end

  class Accessor
    # Bind this accessor to a flavor and variant and load secrets/LAYOUT.yaml once.
    def initialize(flavor, variant)
      @flavor  = flavor
      @variant = variant
      @doc     = BuildSecrets.load_layout
    end

    # Spec for a secret, with any `by_flavor[<flavor>]` override merged on top.
    def spec(key)
      base = @doc.fetch("secrets").fetch(key.to_s) { raise "unknown secret '#{key}'" }
      ov   = base["by_flavor"] && base["by_flavor"][@flavor]
      ov ? base.merge(ov) : base
    end

    # Interpolate {flavor}/{variant} into a relative path.
    def rel(key)
      (spec(key)["rel"] || "").gsub("{flavor}", @flavor).gsub("{variant}", @variant)
    end

    # Candidate on-disk locations, in precedence order. `consume_at` (out-of-tree,
    # e.g. cmp-android/google-services.json) short-circuits the root search.
    def candidate_paths(key)
      s = spec(key)
      if s["consume_at"]
        [s["consume_at"].gsub("{flavor}", @flavor).gsub("{variant}", @variant)]
      elsif s["kind"] == "env_var"
        # env-line secrets live at <root>/_env/<VAR> (matches materialize_dest)
        @doc.fetch("precedence").map { |root| File.join(@doc["roots"].fetch(root), "_env", s.fetch("source_env")) }
      else
        @doc.fetch("precedence").map { |root| File.join(@doc["roots"].fetch(root), rel(key)) }
      end
    end

    # candidate_paths returns REPO-ROOT-RELATIVE strings (roots are `secrets/live`
    # etc.). Existence/read MUST be anchored on REPO_ROOT — NOT the process cwd —
    # or resolution silently changes with the caller's directory: pre_fastlane_script
    # runs at repo root (live found) while config.rb runs from deployment/ (live
    # missed → wrong fall-through to the committed sample). Anchor here; still
    # RETURN the relative string (consumers File.join it with DEPLOYMENT_REPO_ROOT).
    def abs(p)
      File.join(REPO_ROOT, p)
    end

    # Resolved read path: first existing candidate, else the highest-precedence one.
    def path(key)
      candidate_paths(key).find { |p| File.exist?(abs(p)) } || candidate_paths(key).last
    end

    # True when any candidate path for key is present on disk.
    def exists?(key)
      candidate_paths(key).any? { |p| File.exist?(abs(p)) }
    end

    # String value: literal constant → its value; else env var first (CI), then
    # the on-disk file (local fallback, REPO_ROOT-anchored like path()).
    def value(key)
      # An UNKNOWN key resolves to nil — consistent with a missing on-disk file → nil below. `.value`
      # is for OPTIONAL secret VALUES (ENV-first, file-fallback); a key that isn't registered for this
      # project must NOT hard-raise and abort config.rb load. It was doing exactly that: config.rb's
      # `module IosConfig` eagerly resolves `:appstore_key_id` at LOAD, so an Android deploy (which never
      # touches iOS) aborted with `unknown secret 'appstore_key_id'` (2026-08-08). `.path` still raises —
      # a structural path is required. The real consumer (an iOS lane) validates a nil at deploy time.
      s = begin; spec(key); rescue; nil; end
      return nil if s.nil?
      return s["value"] if s["kind"] == "literal"
      env = ENV[s["source_env"].to_s]
      return env unless env.to_s.empty?
      a = abs(path(key))
      File.exist?(a) ? File.read(a).strip : nil
    end

    # Flavor-aware applicationId — feeds the FB-PRE-3 app-id↔package preflight.
    def application_id
      toml = File.join(REPO_ROOT, "gradle", "libs.versions.toml")
      base = File.read(toml)[/^appId\s*=\s*"([^"]+)"/, 1]
      raise "appId not found in #{toml}" if base.nil? || base.empty?
      "#{base}#{FLAVOR_SUFFIX.fetch(@flavor, '')}"
    end

    # Write a secret to its on-disk path. Returns the path, or nil if the source is empty.
    def materialize!(key, from_env: nil)
      s    = spec(key)
      kind = s.fetch("kind")
      dest = materialize_dest(key, s)
      body = materialize_body(key, s, kind, from_env)
      return nil if body.nil? || body.empty?

      new_body = (kind == "file" ? body : "#{body.chomp}\n")

      # NEVER clobber a git-TRACKED destination. A `consume_at` that points at a committed reference
      # (e.g. cmp-android/google-services.json ships a COMPLETE per-flavor reference so the template
      # builds out-of-box) is the build's source of truth. Overwriting it with the vault copy — which
      # may be a SUBSET (missing a flavored applicationId) — silently breaks the flavored build at
      # compile time, far from the `/secrets pull` that caused it (2026-08-09: the vault
      # google-services carried 2 of 6 variants → demoDebug link failure). Materialize only to
      # untracked/gitignored destinations (secrets/live/**, _env/**, local.properties, …). A fork
      # that wants a vault-driven file must `git rm --cached` it first (make it untracked).
      if git_tracked?(dest)
        return dest if File.exist?(abs(dest)) && File.read(abs(dest)) == new_body  # identical — no-op
        warn "  ↳ skip #{key}: #{dest} is git-tracked — keeping the committed reference " \
             "(vault copy differs; not clobbering the build source-of-truth)"
        return nil
      end

      FileUtils.mkdir_p(File.dirname(dest))
      # Files keep their bytes verbatim; line-oriented values get a trailing newline.
      File.write(dest, new_body)
      File.chmod(s["mode"].to_i(8), dest) if s["mode"]
      dest
    end

    # Is `dest` (repo-root-relative) a git-TRACKED file? A committed file is authoritative — the
    # `/secrets pull` materialize must not overwrite it. Fails SAFE to false (allow materialize)
    # when git is unavailable. system(out:/err:) redirection is Ruby 2.6-compatible.
    def git_tracked?(dest)
      rel = dest.sub(%r{\A#{Regexp.escape(REPO_ROOT)}/}, "")
      system("git", "-C", REPO_ROOT, "ls-files", "--error-unmatch", "--", rel,
             out: File::NULL, err: File::NULL)
    end

    # Materialize every secret whose source is present (used by `materialize-all`).
    # `map{}.compact` (not `filter_map`) so this runs on macOS system Ruby 2.6 — the
    # local `/secrets pull` delegates here, and filter_map is Ruby 2.7+.
    def materialize_all!
      @doc.fetch("secrets").keys.map { |k| materialize!(k) }.compact
    end

    # Emit the vault-materialization plan: one row per LAYOUT secret carrying a
    # `vault_alias`, as `vault_alias<TAB>source_env<TAB>b64` where b64=1 for `file`
    # kinds (feed base64 → decode_file reproduces exact bytes incl. PEM trailing
    # newline) and 0 for value/env_var (fed raw). `literal` secrets have no vault
    # source so they never appear. `by_flavor` secrets emit one row per flavor
    # override (each carries its own source_env + vault_alias). This is the ONLY
    # place the framework's `/secrets pull` needs to consult to know what to decrypt
    # and how to hand it back — build_secrets.rb stays the LAYOUT's single SoT.
    def vault_plan
      @doc.fetch("secrets").flat_map do |_key, s|
        kind = s["kind"] || "value"
        b64  = kind == "file" ? 1 : 0
        if kind == "properties"
          # Each assembled key that is vault-backed needs its value handed back as an env var
          # (materialize_body reads ENV[source_env]); `literal` keys carry no vault source.
          (s["keys"] || []).map { |k| k["vault_alias"] && [k["vault_alias"], k["source_env"], 0] }.compact
        elsif (fl = s["by_flavor"])
          fl.values.map { |fv| fv["vault_alias"] && [fv["vault_alias"], fv["source_env"] || s["source_env"], b64] }.compact
        elsif (va = s["vault_alias"])
          [[va, s["source_env"], b64]]
        else
          []
        end
      end
    end

    # Provider format: emit `export VAR='value'` lines for every env-consumed secret (kind env_var /
    # value), each value read via the env-first/file-fallback resolver. A deploy step runs
    # `eval "$(build-secrets export-env)"` instead of sourcing secrets/live/_env/* — so the vault-backed
    # files under secrets/live/ are the single source of truth and env is just one on-demand format.
    # (map/compact, not filter_map, for system Ruby 2.6 — same as materialize_all!.)
    def export_env
      @doc.fetch("secrets").map do |key, s|
        kind = s["kind"]
        next nil unless kind == "env_var" || kind == "value"
        senv = s["source_env"]
        next nil if senv.nil? || senv.empty?
        v = value(key).to_s
        next nil if v.empty?
        "export #{senv}='#{v.gsub("'", "'\\''")}'"
      end.compact
    end

    # JVM/Gradle consumption surface. Emit `key=<repo-relative-path>` for every secret that resolves to
    # an on-disk FILE (file/properties/literal kinds + anything with rel/consume_at) via the SAME
    # `path()` algorithm — so the map is a faithful projection of the resolver, no parallel logic. Gradle
    # reads this map (a generated `gradle/secrets-paths.properties`) at configuration time instead of
    # shelling out to this resolver — a config-time subprocess is Windows-fragile + config-cache-hostile
    # + runs on every build. env_var (env-line) secrets are skipped: Gradle never needs those paths.
    # Paths are repo-root-relative with forward slashes (Java .properties-safe; the consumer File.joins
    # them with the Gradle rootProject dir).
    def export_paths
      @doc.fetch("secrets").keys.map do |key|
        s = spec(key)
        next nil if s["kind"] == "env_var"
        next nil unless s["rel"] || s["consume_at"]
        "#{key}=#{path(key)}"
      end.compact
    end

    # Persist the JVM path map to gradle/secrets-paths.properties (repo-root-anchored, gitignored).
    # Called after a materialize (CI / `/secrets pull`) so a subsequent `./gradlew` release build reads
    # real live paths WITHOUT any subprocess. Returns the written destination.
    def write_paths_map!
      dest = File.join(REPO_ROOT, "gradle", "secrets-paths.properties")
      FileUtils.mkdir_p(File.dirname(dest))
      File.write(dest, export_paths.join("\n") + "\n")
      dest
    end

    private

    # Where a secret should be written for this flavor/variant, from its LAYOUT entry.
    # An env-var secret lands under `_env/`; everything else uses its declared consume_at.
    def materialize_dest(key, s)
      live = @doc["roots"].fetch("live")
      return s["consume_at"].gsub("{flavor}", @flavor).gsub("{variant}", @variant) if s["consume_at"]
      return File.join(live, "_env", s.fetch("source_env")) if s["kind"] == "env_var"
      File.join(live, rel(key))
    end

    # The bytes to write for a secret, by kind — a literal, an env-var value, or a file copy.
    def materialize_body(_key, s, kind, from_env)
      case kind
      when "literal"
        s["value"]
      when "value", "env_var"
        ENV[s["source_env"].to_s]
      when "file"
        raw = ENV[(from_env || s["source_env"]).to_s]
        return nil if raw.to_s.empty?
        decode_file(raw, s["encoding"])
      when "properties"
        # Assemble a KEY=VALUE .properties file at the platform path (single source of truth) from
        # vault-backed keys (value read from ENV[source_env]) + `literal` keys. Materialized to
        # `rel:` (NOT _env), e.g. android/keystores/upload_keystore.properties — the exact file
        # config.rb#get_android_signing_config reads. Heals the "signing creds only in _env" gap.
        (s["keys"] || []).map { |k|
          next nil unless k["name"]
          v = k.key?("literal") ? k["literal"] : ENV[k["source_env"].to_s]
          "#{k['name']}=#{v}"
        }.compact.join("\n")
      else
        raise "unknown kind '#{kind}'"
      end
    end

    # base64 → decode; pem-or-base64 → raw PEM passthrough else decode; plain → verbatim.
    def decode_file(raw, encoding)
      case encoding
      when "pem-or-base64"
        raw.lstrip.start_with?("-----BEGIN") ? raw : Base64.decode64(raw)
      when "base64"
        Base64.decode64(raw)
      else
        raw
      end
    end
  end

  # ── deployment/**/secrets-needs.yaml → expansion + the root secrets-manifest.yaml ──────────────
  # Each target AUTHORS only which LAYOUT secrets it needs (`needs: [{secret:, required_for:}]`) — LAYOUT
  # keys, no alias, no prefix, no fork identity — so a target file is BYTE-IDENTICAL in the template and
  # every fork, and deployment/ stays purely template-owned (no consumer writes it). Every per-secret fact
  # (vault alias, path, CI secret, how to obtain it) is LAYOUT's. The expansion is computed on demand:
  #   · `needs-generate` writes the ONE generated file, <repo>/secrets-manifest.yaml (./gradlew
  #     syncForkConfig runs it; the framework reads it through a workspace symlink);
  #   · `needs-expand` serves scripts/secrets/* (setup/status) one target's rows at run time.
  # Before this, both LAYOUT and the target files declared the same facts by hand and had drifted in 27
  # places — 14 aliases LAYOUT did not know, 13 CI names no workflow reads (2026-10-29).
  # Ruby 2.6-compatible stdlib only, like the rest of this file.
  module NeedsGenerator
    # The block earlier revisions stored inside each target file. Its presence now means a stale file.
    MARKER = "# >>> GENERATED by ./gradlew syncForkConfig from needs[] above + secrets/LAYOUT.yaml — " \
             "do not hand-edit below this line"
    MANIFEST = "secrets-manifest.yaml"
    MANIFEST_HEADER = "# secrets-manifest.yaml — GENERATED by ./gradlew syncForkConfig (deployment/_shared/lib/build_secrets.rb\n" \
                      "# needs-generate) from deployment/**/secrets-needs.yaml + secrets/LAYOUT.yaml — do not hand-edit.\n" \
                      "# The framework reads it through workspaces/<ws>/<project>/secrets-manifest.yaml, a symlink to this file."

    module_function

    # Every deployment/**/secrets-needs.yaml under +root+, sorted.
    def files(root = REPO_ROOT)
      Dir.glob(File.join(root, "deployment", "**", "secrets-needs.yaml")).sort
    end

    # +str+ as a double-quoted YAML scalar.
    def q(str)
      "\"" + str.to_s.gsub("\\") { "\\\\" }.gsub("\"") { "\\\"" } + "\""
    end

    # LAYOUT roots.live — where a materialized secret lands.
    def live(doc)
      doc["roots"].fetch("live")
    end

    # LAYOUT roots.sample — the committed fake tree.
    def sample(doc)
      doc["roots"].fetch("sample")
    end

    # One vault row per alias the secret resolves to. Raises on a LAYOUT entry the target cannot use.
    def rows_for(doc, key, s, where)
      need_alias = lambda do |a, what|
        raise "#{where}: needs '#{key}', but secrets/LAYOUT.yaml#secrets.#{key}#{what} has no vault_alias" if a.to_s.empty?
        a
      end
      file_mat = lambda do |mode|
        mode ? "{ format: \"file\", via: \"copy\", mode: #{q(mode)} }" : "{ format: \"file\", via: \"copy\" }"
      end
      env_mat = "{ format: \"env\", via: \"export\" }"
      case s["kind"]
      when "file"
        [[need_alias.call(s["vault_alias"], ""), q(File.join(live(doc), s.fetch("rel"))), file_mat.call(s["mode"])]]
      when "value"
        if s["by_flavor"]
          s["by_flavor"].map do |fl, f|
            [need_alias.call(f["vault_alias"], ".by_flavor.#{fl}"), q(File.join(live(doc), f.fetch("rel"))), file_mat.call(nil)]
          end
        else
          [[need_alias.call(s["vault_alias"], ""), q(File.join(live(doc), s.fetch("rel"))), file_mat.call(nil)]]
        end
      when "properties"
        seen = {}
        rows = []
        (s["keys"] || []).each do |k|
          next if k["vault_alias"].to_s.empty? || seen[k["vault_alias"]]
          seen[k["vault_alias"]] = true
          rows << [k["vault_alias"], q(File.join(live(doc), s.fetch("rel"))), env_mat]
        end
        raise "#{where}: needs '#{key}', whose keys carry no vault_alias" if rows.empty?
        rows
      when "env_var"
        [[need_alias.call(s["vault_alias"], ""), s["needs_canonical"] || "env:#{s.fetch("source_env")}", env_mat]]
      else
        raise "#{where}: needs '#{key}' (kind #{s["kind"]}), which has no vault value to need"
      end
    end

    # The manual-mode (no vault) inputs: one per file/value/env the operator can hand-drop.
    # `properties` is assembled from its keys' own secrets, and a needs_canonical secret is written
    # by /secrets pull, so neither gets one.
    def manual_for(doc, s)
      ph = lambda { |rel, env| s["placeholder"] || (rel ? File.join(sample(doc), rel) : "<#{env.downcase.tr("_", "-")}>") }
      case s["kind"]
      when "file", "value"
        list = s["by_flavor"] ? s["by_flavor"].values : [s]
        list.map do |f|
          [q(File.join(live(doc), f.fetch("rel"))), s["hint"], ph.call(s["kind"] == "file" ? f["rel"] : nil, f.fetch("source_env")),
           f["gha_secret"] || s["gha_secret"] || f.fetch("source_env")]
        end
      when "env_var"
        s["needs_canonical"] ? [] : [["env:#{s.fetch("source_env")}", s["hint"], ph.call(nil, s.fetch("source_env")), s["gha_secret"] || s.fetch("source_env")]]
      else
        []
      end
    end

    # One target's expansion: [vault rows, manual rows]. vault row = [alias, canonical(YAML), required_for, materialize];
    # manual row = [canonical(YAML), hint, placeholder, gha_secret].
    def expand(doc, needs, where)
      rows = []
      manual = []
      seen = {}
      mseen = {}
      Array(needs).each do |n|
        key = n.is_a?(Hash) ? n["secret"].to_s : ""
        raise "#{where}: a needs[] entry has no `secret:`" if key.empty?
        s = doc["secrets"][key] or raise "#{where}: needs '#{key}', which secrets/LAYOUT.yaml does not declare"
        rf = Array(n["required_for"]).map(&:to_s)
        rows_for(doc, key, s, where).each do |a, canon, mat|
          next if seen[a]
          seen[a] = true
          rows << [a, canon, rf, mat]
        end
        manual_for(doc, s).each do |m|
          next if mseen[m[0]]
          mseen[m[0]] = true
          manual << m
        end
      end
      [rows, manual]
    end

    # A target file's authored YAML, or nil for a legacy v1 file (no needs:). Raises on a file that
    # still stores derived rows — that is exactly the per-fork churn this layout removes.
    def authored(path, root)
      text = File.read(path)
      rel = path.sub("#{root}/", "")
      raise "#{rel}: still carries the old generated block — delete everything from `#{MARKER[0, 20]}…` down" if text.include?(MARKER)
      y = YAML.safe_load(text) || {}
      return nil unless y.key?("needs")
      %w[vault_aliases manual_inputs].each do |k|
        raise "#{rel}: `#{k}` is derived from secrets/LAYOUT.yaml — author only `needs:`" if y.key?(k)
      end
      y
    end

    # <ws>/<project> for the manifest header, read from ONE place both a write and a --check see:
    # gradle/fork.properties (org.secrets.namespace + project.name — the identity bridge
    # syncForkConfig derives from app-profile). Falls back to LAYOUT's alias_namespaces.
    def project_id(doc, root)
      props = {}
      fp = File.join(root, "gradle", "fork.properties")
      if File.file?(fp)
        File.foreach(fp) do |l|
          k, v = l.strip.split("=", 2)
          props[k] = v.to_s.strip if v && !k.start_with?("#")
        end
      end
      ns = doc["alias_namespaces"] || {}
      org = props["org.secrets.namespace"].to_s.empty? ? ns["org"] : props["org.secrets.namespace"]
      proj = props["project.name"].to_s.empty? ? ns["project"] : props["project.name"]
      "#{org}/#{proj}"
    end

    # The vault manifest: the union of every target's vault aliases (first declaration wins), in
    # sorted-file order so the bytes are identical on every machine — the framework's old `find`-ordered
    # union differed between APFS and ext4. v2 targets expand from LAYOUT; a v1 (legacy) file's stored
    # vault_aliases are unioned as they are, exactly as the framework collector did.
    def manifest(doc, root, legacy)
      seen = {}
      rows = []
      files(root).each do |f|
        rel = f.sub("#{root}/", "")
        a = authored(f, root)
        list = if a
                 expand(doc, a["needs"], rel)[0].map { |al, canon, _rf, mat| [al, canon, mat] }
               else
                 legacy << rel
                 (YAML.load_file(f) || {})["vault_aliases"].to_a.map do |v|
                   c = v["canonical"].to_s
                   m = v["materialize"].is_a?(Hash) ? "{ " + v["materialize"].map { |k, x| "#{k}: #{q(x)}" }.join(", ") + " }" : nil
                   [v["alias"].to_s, c.empty? ? nil : (c.start_with?("env:") || c == "local.properties" ? c : q(c)), m]
                 end
               end
        # A PURE alias list (framework schema v3.0, core/schemas/secrets-manifest.schema.json): `canonical` and
        # `materialize` are rejected there — the framework resolves every destination from the alias via
        # secrets-resolve-path.sh and never reads `.canonical` (secrets-pull.sh says so). Emitting them failed
        # `/secrets verify` SV10 and, with it, every command that preflights the vault.
        list.each do |al, _canon, _mat|
          next if al.to_s.empty? || seen[al]
          seen[al] = true
          rows << "  - alias: #{al}"
        end
      end
      return nil if rows.empty?
      pid = project_id(doc, root)
      # default_vault is the ORG namespace (one vault per org). /secrets-sync refuses a manifest without it
      # ("no default_vault"), which left every /secrets handoff stranded in the vault, never materialized.
      [MANIFEST_HEADER, "schema_version: \"3.0\"", "project: #{pid}", "default_vault: #{pid.split('/').first}",
       "default_account_email: null", "", "needs:", *rows].join("\n") + "\n"
    end

    # Validate every target and write — or with check: true, only compare — the root manifest. Returns
    # the repo-relative paths that are out of date; legacy (v1) targets are collected into +legacy+.
    def run(check: false, root: REPO_ROOT, legacy: [])
      doc = YAML.load_file(File.join(root, "secrets", "LAYOUT.yaml"))
      want = manifest(doc, root, legacy)
      path = File.join(root, MANIFEST)
      return [] if want.nil? || (File.exist?(path) && File.read(path) == want)
      File.write(path, want) unless check
      [MANIFEST]
    end

    # TSV for scripts/secrets/_lib.sh — one target's manual inputs (canonical, hint, gha, placeholder) or
    # vault rows (alias, canonical): the columns its awk parsers emitted from stored blocks. nil = v1 file.
    def expand_tsv(path, kind, root = REPO_ROOT)
      doc = YAML.load_file(File.join(root, "secrets", "LAYOUT.yaml"))
      a = authored(path, root) or return nil
      rows, manual = expand(doc, a["needs"], path.sub("#{root}/", ""))
      un = lambda { |v| v.to_s.sub(/\A"(.*)"\z/m, '\1') }
      if kind == "manual"
        manual.map { |canon, hint, ph, gha| [un.call(canon), hint.to_s, gha.to_s, ph.to_s].join("\t") }
      else
        rows.map { |al, canon, _rf, _m| [al, un.call(canon)].join("\t") }
      end
    end
  end
end

# ── CLI ─────────────────────────────────────────────────────────────────────
if __FILE__ == $PROGRAM_NAME
  cmd, *rest = ARGV
  # keyless commands (application-id, materialize-all) have no positional <key>;
  # a leading "--flag" means the key was omitted.
  key = (rest.first && !rest.first.start_with?("--")) ? rest.shift : nil
  opts = {}
  rest.each_slice(2) { |flag, val| opts[flag.sub(/\A--/, "")] = val }
  acc = BuildSecrets.for(flavor: (opts["flavor"] || "prod"), variant: (opts["variant"] || "release"))

  case cmd
  when "path"            then puts acc.path(key)
  when "value"           then puts acc.value(key)
  when "application-id"  then puts acc.application_id
  when "exists"          then exit(acc.exists?(key) ? 0 : 1)
  when "materialize"
    dest = acc.materialize!(key, from_env: opts["from-env"])
    warn(dest ? "✓ materialized #{key} → #{dest}" : "· skip #{key} (source empty)")
  when "materialize-all"
    acc.materialize_all!.each { |d| warn "✓ #{d}" }
    warn "✓ #{acc.write_paths_map!} (gradle path map)"
  when "vault-plan"
    acc.vault_plan.each { |a, e, b| puts "#{a}\t#{e}\t#{b}" }
  when "export-env"
    puts acc.export_env
  when "export-paths"
    puts acc.export_paths
  when "needs-expand"
    # needs-expand --file <secrets-needs.yaml> --kind manual|vault → TSV (exit 3 = legacy v1 file).
    fx = ARGV.index("--file")
    kx = ARGV.index("--kind")
    begin
      out = BuildSecrets::NeedsGenerator.expand_tsv(File.expand_path(ARGV.fetch(fx + 1)), kx ? ARGV.fetch(kx + 1) : "manual")
    rescue RuntimeError => e
      abort "✗ secrets-needs: #{e.message}"
    end
    exit 3 if out.nil?
    puts out unless out.empty?
  when "needs-generate"
    # Validate every deployment/**/secrets-needs.yaml#needs[] against LAYOUT and write the root
    # secrets-manifest.yaml. --check writes nothing and exits 1 when the manifest is out of date.
    # Own flag parsing: the shared each_slice(2) pairing above reads `--check --root X` as
    # check=--root and silently checks the wrong tree.
    check = ARGV.include?("--check")
    ri = ARGV.index("--root")
    begin
      root = File.expand_path(ri ? ARGV.fetch(ri + 1) : BuildSecrets::REPO_ROOT)
      legacy = []
      stale = BuildSecrets::NeedsGenerator.run(check: check, root: root, legacy: legacy)
    rescue RuntimeError => e
      abort "✗ secrets-needs: #{e.message}"
    end
    legacy.each { |f| warn "⚠ legacy v1 (hand-authored, not generated): #{f} — migrate to `needs:` (see deployment/_shared/secrets-needs-schema.yaml)" }
    if stale.empty?
      warn "✓ secrets-needs: #{BuildSecrets::NeedsGenerator.files(root).size - legacy.size} targets valid · secrets-manifest.yaml current with secrets/LAYOUT.yaml"
    elsif check
      warn "✗ out of date with deployment/**/secrets-needs.yaml + secrets/LAYOUT.yaml — run ./gradlew syncForkConfig:"
      stale.each { |f| warn "    #{f}" }
      exit 1
    else
      stale.each { |f| warn "✓ regenerated #{f}" }
    end
  else
    abort "usage: build-secrets {path|value|application-id|exists|materialize|materialize-all|vault-plan|export-env|export-paths|needs-generate|needs-expand} " \
          "[key] [--flavor f] [--variant v] [--from-env VAR]"
  end
end
