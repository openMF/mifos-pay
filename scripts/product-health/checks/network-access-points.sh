#!/usr/bin/env bash
# checks/network-access-points.sh — app-profile is the ONLY place a network endpoint is declared,
# and every derived surface is a faithful projection of it.
#
# `app-profile/app.yaml#network.access_points` is the SoT. `./gradlew syncForkConfig` projects it onto
# four committed Kotlin surfaces:
#
#   AppAccessPoints.kt        the registry list          (NAP-1 ids · NAP-2 fields)
#   AppUrlTypes.kt            the UrlType vocabulary     (NAP-3)
#   di/GeneratedApiBindings.kt the Koin bindings         (NAP-4)
#   AppAccessPoints.kt `supabase =`  each Supabase point's publishable key (NAP-5 spec · NAP-6 no committed key)
#   core/network/build.gradle.kts  the BuildKonfig fields (NAP-8 every referenced field is declared)
#   core/network/<id>/             the per-endpoint package  (NAP-9 exists · NAP-10 supabase id=ref)
#
# WHY a gate and not just codegen: the generated files are COMMITTED (the build must work on a fresh
# clone with no Gradle run), so nothing forces them to still match app.yaml. Whoever edits app.yaml
# and forgets `syncForkConfig` gets a repo that compiles and is wrong. That is not hypothetical —
# AppUrlTypes was hand-kept and had drifted to 3 constants against 8 declared points, and the drift
# was SILENT AND WRONG-ANSWERING: AppMultiUrlConfigProvider.getBaseUrl falls back to UrlType.MAIN for
# an unrecognised type, so a lookup for an undeclared endpoint returned the MAIN base URL rather than
# failing. NAP-3 is that bug's gate.
#
# NAP-7 closes the loop: wiring must go THROUGH the generated file. A hand-added restApi(...) line
# re-creates the drift the codegen removes (a binding with no declared endpoint, or an endpoint whose
# binding nobody regenerates), so it is refused even though it compiles.
#
# exit 0 PASS / 1 FAIL. Ruby (like fork-props-manifest-parity.sh) — YAML + Kotlin in one pass.
set -uo pipefail
: "${HEALTH_ROOT:?network-access-points: HEALTH_ROOT not set (run via product-health.sh)}"

YAML="${NAP_YAML:-$HEALTH_ROOT/app-profile/app.yaml}"
NET="${NAP_NET_DIR:-$HEALTH_ROOT/core/network/src/commonMain/kotlin/kpt/core/network}"

# A fork that stripped the white-label machinery has nothing to compare. PASS.
[ -f "$YAML" ] || { echo "no app-profile/app.yaml — nothing to compare (ok)"; exit 0; }
command -v ruby >/dev/null 2>&1 || { echo "ruby not available — cannot audit access points"; exit 1; }

ruby - "$YAML" "$NET" <<'RUBY'
require 'yaml'
yaml_path, net_dir = ARGV
profile = begin
  YAML.respond_to?(:unsafe_load) ? YAML.unsafe_load(File.read(yaml_path)) : YAML.load(File.read(yaml_path))
rescue StandardError
  nil
end
unless profile.is_a?(Hash)
  puts "❌ could not parse #{yaml_path} as YAML"; exit 1
end
points = profile.dig("network", "access_points")
if points.nil?
  puts "no network.access_points declared — nothing to project (ok)"; exit 0
end
unless points.is_a?(Array)
  puts "❌ network.access_points is not a list"; exit 1
end
points = points.select { |p| p.is_a?(Hash) && p["id"].to_s.strip != "" }

def read(path)
  File.exist?(path) ? File.read(path) : nil
end

ap_src    = read(File.join(net_dir, "config/AppAccessPoints.kt"))
ut_src    = read(File.join(net_dir, "config/AppUrlTypes.kt"))
# GENERATED into build/ from `@ApiBinding` — no longer committed source. Derived from the module
# root rather than by counting `..` levels off net_dir, which is easy to get wrong by one.
module_root = net_dir.sub(%r{/src/commonMain/kotlin/kpt/core/network\z}, "")
bind_src  = read(File.join(module_root, "build/generated/ksp/metadata/commonMain/kotlin/kpt/core/network/di/GeneratedApiBindings.kt"))
# Fall back to the committed location. Two readers need it: a fork that has not rebuilt since
# adopting @ApiBinding still has the old file, and the canary fixtures are plain trees with no
# build/ directory. If neither exists we take the "not built" path below rather than failing.
bind_src ||= read(File.join(net_dir, "di/GeneratedApiBindings.kt"))

fail = false
def bad(msg)
  puts(msg)
  true
end

# ── NAP-1 / NAP-2 — the registry list ────────────────────────────────────────
if ap_src.nil?
  fail = bad("❌ NAP-1 AppAccessPoints.kt missing — the registry has no source")
else
  # Parse the generated AccessPoint(...) entries: id + kind + baseUrl + loggableHost (+ proxiedHost).
  entries = ap_src.scan(/AccessPoint\(\s*(.*?)\n\s*\),/m).map do |body,|
    b = body
    {
      "id"    => b[/id\s*=\s*"([^"]*)"/, 1],
      "kind"  => b[/kind\s*=\s*AccessPointKind\.(\w+)/, 1],
      "base"  => b[/baseUrl\s*=\s*"([^"]*)"/, 1],
      "host"  => b[/loggableHost\s*=\s*"([^"]*)"/, 1],
      "proxy" => b[/proxiedHost\s*=\s*"([^"]*)"/, 1],
    }
  end
  declared = points.map { |p| p["id"].to_s }
  generated = entries.map { |e| e["id"] }.compact
  missing = declared - generated
  extra   = generated - declared
  fail = bad("❌ NAP-1 declared in app.yaml but NOT in AppAccessPoints: #{missing.join(', ')}\n     → run `./gradlew syncForkConfig`") if missing.any?
  fail = bad("❌ NAP-1 in AppAccessPoints but NOT declared in app.yaml: #{extra.join(', ')}\n     → remove them there, or declare them in app-profile (the SoT)") if extra.any?

  points.each do |p|
    e = entries.find { |x| x["id"] == p["id"].to_s } or next
    want_kind = p["type"].to_s.strip.downcase == "supabase" ? "SUPABASE" : "REST"
    diffs = []
    diffs << "kind #{e['kind']} != #{want_kind}"                                if e["kind"] != want_kind
    diffs << "baseUrl #{e['base'].inspect} != #{p['base_url'].to_s.inspect}"    if e["base"] != p["base_url"].to_s
    diffs << "loggableHost #{e['host'].inspect} != #{p['loggable_host'].to_s.inspect}" if e["host"] != p["loggable_host"].to_s
    want_proxy = p["proxied_host"].to_s.strip
    have_proxy = e["proxy"].to_s
    diffs << "proxiedHost #{have_proxy.inspect} != #{want_proxy.inspect}"       if have_proxy != want_proxy
    fail = bad("❌ NAP-2 '#{p['id']}' stale in AppAccessPoints: #{diffs.join('; ')}\n     → run `./gradlew syncForkConfig`") if diffs.any?
  end
end

# ── NAP-3 — the UrlType vocabulary ───────────────────────────────────────────
# AccessPoint.type defaults to UrlType(id.uppercase()), so the vocabulary is a pure projection of the
# id list. A missing constant does not fail to compile — it fails at runtime by returning MAIN's URL.
if ut_src.nil?
  fail = bad("❌ NAP-3 AppUrlTypes.kt missing")
else
  want = points.map { |p| p["id"].to_s.upcase.gsub(/[^A-Z0-9]/, "_") }
  # name => UrlType KEY. `UrlType.MAIN` is UrlType("MAIN"); everything else declares its key inline.
  decls = {}
  ut_src.scan(/^\s*val\s+([A-Z][A-Z0-9_]*)\s*:\s*UrlType\s*=\s*(UrlType\.MAIN|UrlType\("([^"]*)"\))/).each do |name, whole, key|
    decls[name] = whole == "UrlType.MAIN" ? "MAIN" : key
  end
  have = decls.keys
  missing = want - have
  extra   = have - want
  fail = bad("❌ NAP-3 access points with no AppUrlTypes constant: #{missing.join(', ')}\n     → run `./gradlew syncForkConfig` (getBaseUrl would silently fall back to MAIN)") if missing.any?
  fail = bad("❌ NAP-3 AppUrlTypes constants with no access point: #{extra.join(', ')}") if extra.any?

  # The constant's KEY must equal id.upcase() exactly — that is AccessPoint.type's default, and UrlType
  # equality is what restBaseUrl matches on. A constant whose NAME was sanitized but whose KEY was too
  # (e.g. id `pay-gw` declaring UrlType("PAY_GW")) compiles, resolves nothing, and falls back to MAIN's
  # URL. Checking existence alone would not catch it; checking the key does.
  points.each do |p|
    id   = p["id"].to_s
    name = id.upcase.gsub(/[^A-Z0-9]/, "_")
    key  = decls[name] or next
    next if key == id.upcase
    fail = bad("❌ NAP-3 '#{id}' declares UrlType(#{key.inspect}) but its access point carries UrlType(#{id.upcase.inspect})\n     → the keys must match exactly, or restBaseUrl misses and getBaseUrl returns MAIN's URL")
  end
  all_list = ut_src[/val\s+all\s*:\s*List<UrlType>\s*=\s*listOf\(([^)]*)\)/m, 1].to_s
  listed = all_list.split(",").map(&:strip).reject(&:empty?)
  fail = bad("❌ NAP-3 AppUrlTypes.all lists #{listed.size} of #{want.size} types — not every declared endpoint is enumerated") if listed.sort != want.sort
end

# ── NAP-4 — the generated Koin bindings ──────────────────────────────────────
# The declaration moved onto the class: `@ApiBinding("<id>")`. Scanning the source for it is what
# keeps this gate honest — reading the retired `api:` field would select ZERO points and then agree
# with zero bindings, which is a vacuous pass, not a check (CI-1).
# id -> the annotated type's simple name, so the factory/ctor shape below is checked against the
# REAL class rather than a FQN string someone typed into YAML.
annotated_simple = {}
Dir.glob(File.join(net_dir, "**", "*.kt")).each do |f|
  src = begin
    File.read(f)
  rescue StandardError
    next
  end
  src.scan(/@ApiBinding\(\s*"([^"]+)"\s*\)\s*(?:@[\w.]+(?:\([^\n]*\))?\s*)*(?:public\s+|internal\s+|abstract\s+|open\s+|data\s+)*(?:class|interface|object)\s+(\w+)/m).each do |id, cls|
    annotated_simple[id] = cls
  end
end
annotated = annotated_simple.keys
want_bind = points.select { |p| annotated.include?(p["id"].to_s) }
if annotated.empty?
  fail = bad("❌ NAP-4 no @ApiBinding found in core/network — the gate would pass vacuously")
end
orphan = annotated - points.map { |p| p["id"].to_s }
fail = bad("❌ NAP-4 @ApiBinding names an undeclared access point: #{orphan.join(', ')}\n     → declare it in app-profile, or fix the id") if orphan.any?
if bind_src.nil?
  # NOT a failure. product-health runs as a pure-bash CI job with no Gradle (quality-gate.yml keeps
  # it a ~10s step), so on a fresh checkout the artifact simply has not been produced yet. The
  # invariants that do not need a build — every @ApiBinding names a declared point (above), and
  # nothing is hand-wired (NAP-7) — still ran. The binding-vs-declaration comparison is additionally
  # enforced by the compiler: a binding for a class that does not exist does not build.
  puts "   ℹ️  NAP-4 GeneratedApiBindings not built — annotation/declaration agreement checked, binding shapes deferred to the compiler"
else
  # `<Iface>` is OPTIONAL: a supabase point whose API is split into interface + impl generates
  # `supabaseApi<ServerInstanceApi>("project") { ServerInstanceApiImpl(it) }` so Koin binds the INTERFACE
  # (`single<T>` infers T from the factory lambda, so without the explicit type argument every
  # `get<Interface>()` would miss). Matching only the bare form reported that correct binding as
  # MISSING and then as "does not use supabaseApi(...)" — two failures for working code.
  bound = bind_src.scan(/(?:restApi|supabaseApi)(?:<[^>]+>)?\("([^"]+)"\)/).flatten
  missing = want_bind.map { |p| p["id"].to_s } - bound
  extra   = bound - points.map { |p| p["id"].to_s }
  fail = bad("❌ NAP-4 @ApiBinding declared but no generated binding: #{missing.join(', ')}\n     → rebuild core/network") if missing.any?
  fail = bad("❌ NAP-4 binding for an id that is not a declared access point: #{extra.join(', ')}") if extra.any?
  want_bind.each do |p|
    id     = p["id"].to_s
    simple = annotated_simple[p["id"].to_s]
    dsl    = p["type"].to_s.strip.downcase == "supabase" ? "supabaseApi" : "restApi"
    # Regex, not include? — the DSL call may carry an explicit type argument
    # (`supabaseApi<ServerInstanceApi>("project")`) when the API is split interface + impl.
    unless bind_src =~ /#{Regexp.escape(dsl)}(?:<[^>]+>)?\(#{Regexp.escape(%Q{"#{id}"})}\)/
      fail = bad("❌ NAP-4 '#{id}' is #{p['type']} but its binding does not use #{dsl}(...)")
      next
    end
    factory = dsl == "restApi" ? "it.create#{simple}()" : "#{simple}(it)"
    fail = bad("❌ NAP-4 '#{id}' binding does not construct #{simple} (expected `#{factory}`)") unless bind_src.include?(factory)
  end
end

# ── NAP-5 / NAP-6 — each Supabase point's generated spec ─────────────────────
# core-base builds a Supabase client from `AccessPoint.supabase` alone (its publishable key), so a point
# whose spec is missing is a client with no key. NAP-5: spec present exactly on Supabase points.
# NAP-6: the key is "" or a BuildKonfig read — never a literal in tracked source.
if ap_src
  blocks = ap_src.split(/^\s*AccessPoint\(\s*$/).drop(1)
  specs = blocks.each_with_object({}) do |blk, h|
    id = blk[/id = "([^"]+)"/, 1] or next
    h[id] = blk
  end
  points.each do |p|
    id = p["id"].to_s
    blk = specs[id] or next # NAP-1 already reports an absent point
    is_supa = p["type"].to_s.strip.downcase == "supabase"
    has_spec = blk.include?("SupabaseSpec(")
    if is_supa && !has_spec
      fail = bad("❌ NAP-5 Supabase point '#{id}' has no generated `supabase = SupabaseSpec(…)`\n     → run `./gradlew syncForkConfig` (the client would have no key)")
      next
    end
    if !is_supa && has_spec
      fail = bad("❌ NAP-5 non-Supabase point '#{id}' carries a SupabaseSpec — run `./gradlew syncForkConfig`")
      next
    end
    next unless is_supa
    key = blk[/publishableKey = (.+?),\s*$/, 1].to_s.strip
    unless key == '""' || key.match?(/\A(?:[\w.]*\.)?BuildKonfig\.[A-Z0-9_]+\z/)
      fail = bad("❌ NAP-6 publishable key for '#{id}' is a literal in tracked source: #{key[0, 12]}…\n     → declare `publishable_key_env: <ENV_KEY>` on the point and re-run syncForkConfig")
    end
  end
end

# ── NAP-8 — every referenced BuildKonfig field is DECLARED ───────────────────
# The codegen emits `BuildKonfig.<publishable_key_env>` into AppAccessPoints, but the FIELD is declared
# in core/network/build.gradle.kts. Those were two unlinked places: a fork declaring
# `publishable_key_env: MY_KEY` got a generated reference to a field nobody created — `Unresolved reference`
# on :core:network, with no fork seam to fix it in (the build file is owner:merge, template-derived).
# Both halves now derive from app-profile; this gate is what keeps them derived.
bk_path = ENV["NAP_BUILD_FILE"].to_s.empty? ?
  File.join(net_dir.sub(%r{/src/commonMain/kotlin/kpt/core/network\z}, ""), "build.gradle.kts") :
  ENV["NAP_BUILD_FILE"]
bk_src = read(bk_path)
if bk_src.nil?
  puts "⚠️  NAP-8 skipped — #{bk_path} not found"
else
  declared = bk_src.scan(/buildConfigField\(\s*STRING,\s*"([A-Z0-9_]+)"/).flatten.uniq
  referenced = []
  [bind_src, ap_src, ut_src].compact.each do |src|
    clean = src.gsub(%r{/\*.*?\*/}m, "").gsub(%r{//[^\n]*}, "")
    referenced.concat(clean.scan(/BuildKonfig\.([A-Z0-9_]+)/).flatten)
  end
  missing = referenced.uniq - declared
  if missing.any?
    fail = bad("❌ NAP-8 generated code references BuildKonfig field(s) that are not declared: #{missing.join(', ')}\n" \
               "     → declare the key in app-profile (`publishable_key_env:`/`api_key_env:` on the point, or " \
               "network.build_config_fields) and re-run `./gradlew syncForkConfig`")
  end
end

# ── NAP-9 — every access point owns a package; no package is orphaned ────────
# Endpoint code lives in `kpt/core/network/<id>/` (id lowercased, non-alphanumerics dropped), NOT in
# a domain package under `demo/` — the old layout tied endpoint code to the demo lifecycle and could
# not express two endpoints in one domain (`demo/economic` held both fred and worldbank). The layout
# is a projection of app-profile, so it is checked like one: declared => package exists, and a
# package with no declaration is dead code the strip will never reap.
pkg_root = File.join(net_dir)
INFRA_PKGS = %w[config di]
declared_pkgs = points.map { |p| p["id"].to_s.downcase.gsub(/[^a-z0-9]/, "") }.reject(&:empty?)
if Dir.exist?(pkg_root)
  on_disk = Dir.children(pkg_root).select { |d| File.directory?(File.join(pkg_root, d)) } - INFRA_PKGS
  missing = declared_pkgs - on_disk
  orphan  = on_disk - declared_pkgs
  fail = bad("❌ NAP-9 declared access point(s) with no package: #{missing.join(', ')}\n" \
             "     → run `./gradlew syncForkConfig` (it scaffolds <id>/api + <id>/dto)") if missing.any?
  fail = bad("❌ NAP-9 package(s) with no declared access point: #{orphan.join(', ')}\n" \
             "     → declare the endpoint in app-profile, or delete the package") if orphan.any?
end

# ── NAP-10 — a Supabase id IS its project ref ────────────────────────────────
# `https://<ref>.supabase.co` — the ref is the project's identity, so the access point takes its
# name (and therefore its package) from it. That keeps a fork with several Supabase projects
# unambiguous, and makes the id derivable from the URL rather than a label someone picked.
points.select { |p| p["type"].to_s.strip.downcase == "supabase" }.each do |p|
  host = p["base_url"].to_s.sub(%r{\Ahttps?://}, "").split("/").first.to_s
  next unless host.end_with?(".supabase.co")
  ref = host.split(".").first.to_s
  next if ref.empty? || p["id"].to_s == ref
  fail = bad("❌ NAP-10 Supabase point '#{p['id']}' does not match its project ref '#{ref}' " \
             "(#{host})\n     → rename the id (and its package) to '#{ref}'")
end

# ── NAP-12 — every access point STATES its auth scheme ───────────────────────
# `auth:` defaults to none, which is the safe default but a silent one: an endpoint that needs a
# credential and forgot to say so just gets 401s at runtime, and an endpoint that is genuinely public
# looks identical to one nobody thought about. Requiring the field turns "no auth" into a decision
# somebody made.
missing_auth = points.reject { |p| p.key?("auth") }.map { |p| p["id"] }
if missing_auth.any?
  fail = bad("❌ NAP-12 access point(s) do not declare `auth:`: #{missing_auth.join(', ')}\n" \
             "     → add `auth: none|basic|bearer|oauth`; none is valid but must be stated")
end
bad_auth = points.select { |p| p.key?("auth") && !%w[none basic bearer oauth].include?(p["auth"].to_s.downcase) }
if bad_auth.any?
  fail = bad("❌ NAP-12 unknown auth scheme: #{bad_auth.map { |p| "#{p['id']}=#{p['auth']}" }.join(', ')}\n" \
             "     → one of none | basic | bearer | oauth (an unknown value silently parses to NONE)")
end

# ── NAP-13 — the DEFAULT transport headers are NOT declared ──────────────────
# `Accept` belongs to ContentNegotiation, which `setupDefaultHttpClient` installs with
# `json(jsonConfig)`; a typed body's `Content-Type: application/json` comes from core-base's
# JsonBodyContentType. (ContentNegotiation alone does NOT name a type for a typed `@Body` object — it
# serializes only once one is named, so before JsonBodyContentType a fork had to declare it or every
# typed POST threw "Fail to prepare request body". The probe below covered String/text bodies only.)
#
# MEASURED, not assumed (probe, 2026-09-08) — declared vs not:
#   declared      GET  Content-Type: application/json   <- on a request with NO body
#   not declared  GET  Content-Type: absent             <- correct per RFC 9110
#   either way    POST Content-Type follows the BODY (application/json / text/plain)
#   either way    Accept: application/json, single value, never duplicated
#
# So a declaration does NOT break non-JSON bodies — Ktor derives Content-Type from the body and
# that wins — and it does NOT duplicate Accept. (Both were claimed when this check was written;
# the probe disproved them.) What it DOES do is put a Content-Type on bodyless requests,
# describing a body that is not there, which some proxies and WAFs reject or flag. It is therefore
# strictly worse than not declaring: nothing gained, one malformed header added.
# Other codebases carry these as constants (mifos-pay's BaseURL.HEADER_CONTENT_TYPE / HEADER_ACCEPT)
# because they hand-build requests. Here the client negotiates, so the right number of declarations
# is zero — and this check exists so that pattern is not ported in by habit.
#
# What IS allowed: a Content-Type that is NOT the default. A server that speaks another type
# (`application/vnd.api+json`, `application/hal+json`) declares it, and core-base's JsonBodyContentType
# steps aside for a request that already names one. Only a declaration that restates the default
# (Accept, or Content-Type: application/json) is redundant — and it is the one that breaks bodyless GETs.
redundant = points.flat_map do |p|
  (p["headers"] || []).select do |h|
    name = h["name"].to_s.downcase
    value = h["value"].to_s.downcase.split(";").first.to_s.strip
    name == "accept" || (name == "content-type" && (value.empty? || value == "application/json"))
  end.map { |h| "#{p['id']}:#{h['name']}" }
end
if redundant.any?
  fail = bad("❌ NAP-13 default transport header(s) declared: #{redundant.join(', ')}\n" \
             "     → remove them; the shared client already sends Accept and names JSON for a typed body\n" \
             "       (core-base JsonBodyContentType). Declaring them only puts a Content-Type on bodyless requests.\n" \
             "       A server that needs a DIFFERENT Content-Type may declare it — that is an override, not noise.")
end

# ── NAP-11 — the default-header seam stays wired ─────────────────────────────
# `ktorfitFor` is the ONE place every generated REST client is built, so a fork's DefaultHeaderProvider
# reaches the wire only if that function passes it to setupDefaultHttpClient. Delete the argument and
# NOTHING fails: the template sends no headers, so every test still passes and the loss shows up only
# in a fork whose API key silently stops being sent.
#
# It cannot be covered behaviourally: ktorfitFor builds its client through the `httpClient` platform
# `expect`, so a test cannot inject a MockEngine to observe the request. Structural, for the same
# reason store-fork-seam-wiring FS-1/FS-2 are.
dsl = read(File.join(net_dir, "..", "..", "..", "..", "..", "..", "..",
                     "core-base/network/src/commonMain/kotlin/kpt/core/base/network/NetworkDsl.kt"))
dsl ||= read(File.expand_path("core-base/network/src/commonMain/kotlin/kpt/core/base/network/NetworkDsl.kt", Dir.pwd))
if dsl.nil?
  puts "   ℹ️  NAP-11 NetworkDsl.kt not readable from here — header-seam wiring unchecked"
elsif dsl !~ /defaultHeaders\s*=\s*headerProvider\.headersFor\(/
  fail = bad("❌ NAP-11 ktorfitFor no longer passes the fork's DefaultHeaderProvider to setupDefaultHttpClient\n" \
             "     → a fork's default headers would silently stop being sent")
elsif dsl !~ /getOrNull<DefaultHeaderProvider>\(\)/
  fail = bad("❌ NAP-11 restApi no longer resolves DefaultHeaderProvider from Koin\n" \
             "     → the seam exists but nothing supplies it")
end

# ── NAP-7 — no hand-rolled wiring ────────────────────────────────────────────
# The generated file is the ONLY place a binding may live; a hand-added line re-opens the drift the
# codegen closes. Comments and KDoc are stripped so the files that DOCUMENT the DSL don't trip it.
Dir.glob(File.join(net_dir, "**/*.kt")).sort.each do |f|
  next if f.end_with?("GeneratedApiBindings.kt")
  src = File.read(f)
  src = src.gsub(%r{/\*.*?\*/}m, "").gsub(%r{//[^\n]*}, "")
  hits = src.scan(/^\s*(restApi|supabaseApi)(?:<[^>]+>)?\("([^"]+)"\)/)
  hits.each do |dsl, id|
    fail = bad("❌ NAP-7 hand-wired #{dsl}(\"#{id}\") in #{f.sub(net_dir + '/', '')}\n     → annotate the API type `@ApiBinding(\"#{id}\")`; the binding is generated")
  end
end

if fail
  puts "     → SoT is app-profile/app.yaml#network.access_points; regenerate with `./gradlew syncForkConfig`."
  exit 1
end
rest = points.count { |p| p["type"].to_s.strip.downcase != "supabase" }
puts "access points: #{points.size} declared (#{rest} REST, #{points.size - rest} Supabase), #{want_bind.size} bound — registry, url-types, bindings and Supabase specs all match app-profile"
RUBY
