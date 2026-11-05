#!/usr/bin/env bash
# supabase-client-security.sh — a client app carries only client-safe Supabase credentials.
#
# WHY THIS EXISTS
# Everything compiled into an app binary (BuildKonfig) can be extracted by anyone who has the app.
# For Supabase that is fine for the project URL and the publishable/anon key — Row Level Security is
# the boundary — and catastrophic for the service_role / secret key (bypasses RLS), the DB password or
# URL, a management access token or the JWT secret. The framework's supabase onboarding pulls BOTH
# kinds of key into the vault, so nothing but this check stands between a mis-wired alias and a
# privileged key shipping in a release. And supabase-kt's default `install(Auth)` keeps the signed-in
# session (refresh token included) in PLAIN settings; installSecureAuth keeps it encrypted + PKCE.
#
#   SC-1 no app-compiled key (app.yaml publishable_key_env / api_key_env / network.build_config_fields) has a
#        privileged name — same rule syncForkConfig enforces
#   SC-2 the secrets/LAYOUT.yaml row behind each one is not a privileged vault alias
#   SC-3 the value in secrets/live/ (or secrets/sample/) is not privileged by SHAPE: `sb_secret_…`, or a
#        JWT whose role is service_role / supabase_admin. Only the path is ever printed, never a value.
#   SC-4 the secrets/sample/ file behind each one is empty (a sample never carries a real key)
#   SC-5 Supabase Auth / ComposeAuth are installed only by kmp-supabase-auth
#        (io.github.mobilebytelabs:cmp-supabase-auth) — never by a hand-written install(...) in app source.
#        The library owns the session handling every app needs (callback activity, both callback
#        shapes, cancellation, id-preserving guest upgrade); a hand-rolled install drops that, and a
#        second install silently REPLACES the first (supabase-kt keys plugins in a map).
#
# Exit 0 = PASS · 1 = FAIL (blocks).
set -uo pipefail
ROOT="$(cd "${1:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)}" 2>/dev/null && pwd)" || { echo "  ✗ SC-0 cannot enter ${1:-}"; exit 2; }
[ -f "$ROOT/app-profile/app.yaml" ] || { echo "  ✓ SC-1 no app-profile/app.yaml in this tree — nothing to check"; exit 0; }
command -v ruby >/dev/null 2>&1 || { echo "  ✗ SC-0 ruby unavailable"; exit 1; }
ruby -ryaml -rbase64 -rjson - "$ROOT" <<'RUBY'
root = ARGV[0]
PRIV_NAME  = /SERVICE_ROLE|SERVICE_KEY|SECRET|PASSWORD|DB_URL|DATABASE_URL|ACCESS_TOKEN|JWT|PRIVATE_KEY/
PRIV_ALIAS = /service-role|service_role|secret-key|sb-secret|db-password|db-url|database-url|access-token|jwt-secret|private-key/
app = YAML.load_file(File.join(root, "app-profile", "app.yaml")) || {}
net = app["network"] || {}
envs = []
(net["access_points"] || []).each { |p| %w[publishable_key_env api_key_env].each { |k| envs << p[k].to_s.strip unless p[k].to_s.strip.empty? } }
(net["build_config_fields"] || []).each { |r| e = (r["from_env"] || r["name"]).to_s.strip; envs << e unless e.empty? }
envs.uniq!
fail = false
bad = envs.grep(PRIV_NAME)
if bad.empty?
  puts "  ✓ SC-1 #{envs.size} app-compiled key(s), none named as a privileged credential"
else
  puts "  ✗ SC-1 app-compiled key name marks a privileged credential: #{bad.join(", ")} — keep it in the vault for CI / Edge Functions"
  fail = true
end
layout_p = File.join(root, "secrets", "LAYOUT.yaml")
layout = File.file?(layout_p) ? (YAML.load_file(layout_p) || {}) : {}
roots = layout["roots"] || { "live" => "secrets/live", "sample" => "secrets/sample" }
rows = (layout["secrets"] || {}).select { |_, s| s.is_a?(Hash) && s["kind"] == "value" && envs.include?(s["source_env"].to_s) && s["rel"] }
alias_bad = rows.select { |_, s| s["vault_alias"].to_s =~ PRIV_ALIAS }.keys
if alias_bad.empty?
  puts "  ✓ SC-2 #{rows.size} LAYOUT row(s) behind them, none a privileged vault alias"
else
  puts "  ✗ SC-2 LAYOUT row(s) compiled into the app point at a privileged vault alias: #{alias_bad.join(", ")}"
  fail = true
end
privileged_value = lambda do |v|
  return true if v.start_with?("sb_secret_")
  parts = v.split(".")
  return false unless parts.size == 3 && parts[0].start_with?("eyJ")
  payload = begin JSON.parse(Base64.urlsafe_decode64(parts[1] + "=" * ((4 - parts[1].size % 4) % 4))) rescue {} end
  %w[service_role supabase_admin].include?(payload["role"].to_s)
end
shape_bad = []
sample_bad = []
rows.each do |key, s|
  %w[live sample].each do |r|
    f = File.join(root, roots[r], s["rel"])
    next unless File.file?(f)
    v = File.read(f).strip
    shape_bad << "#{roots[r]}/#{s["rel"]} (#{key})" if !v.empty? && privileged_value.call(v)
    sample_bad << "#{roots[r]}/#{s["rel"]} (#{key})" if r == "sample" && !v.empty?
  end
end
if shape_bad.empty?
  puts "  ✓ SC-3 no app-compiled value has the shape of a privileged key"
else
  puts "  ✗ SC-3 privileged key material where an app-compiled key is read (value not shown): #{shape_bad.join(", ")}"
  puts "        → rotate that key, and put the publishable key there instead"
  fail = true
end
if sample_bad.empty?
  puts "  ✓ SC-4 every sample behind an app-compiled key is empty"
else
  puts "  ✗ SC-4 a committed sample carries a value: #{sample_bad.join(", ")} — samples are placeholders, keep them empty"
  fail = true
end
auth_bad = []
Dir.glob(File.join(root, "**", "src", "**", "*.kt")).each do |f|
  next if f.include?("/build/") || f.include?("/product-health/tests/")
  t = File.read(f)
  next unless t.include?("io.github.jan.supabase.auth.Auth") || t.include?("io.github.jan.supabase.compose.auth.ComposeAuth")
  t.each_line.with_index(1) do |l, i|
    next if l.strip.start_with?("//", "*")
    auth_bad << "#{f.sub(root + "/", "")}:#{i}" if l =~ /\binstall\(\s*(?:Compose)?Auth\b/
  end
end
if auth_bad.empty?
  puts "  ✓ SC-5 no hand-written Supabase Auth install — sign-in comes from kmp-supabase-auth"
else
  puts "  ✗ SC-5 hand-written install(Auth/ComposeAuth) — use kmp-supabase-auth's kmpSupabaseAuthInstall(config) in SupabaseExtrasProvider:"
  auth_bad.each { |x| puts "      #{x}" }
  fail = true
end
exit(fail ? 1 : 0)
RUBY
