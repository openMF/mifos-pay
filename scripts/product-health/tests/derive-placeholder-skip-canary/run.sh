#!/usr/bin/env bash
# run.sh — locks derive.rb's PLACEHOLDER filter.
#
# derive.rb OMITS any app-profile value that is still a placeholder (derive.rb:61), so an unauthored
# key is ABSENT from gradle/fork.properties and every reader falls back to its own default. That is
# deliberate and load-bearing: app-profile ships `demo_base_url: https://demo.example.com`, so
# weakening this filter would make forks emit that placeholder as a REAL API endpoint into per-flavor
# BuildConfig (BASE_URL) instead of falling back to the plugin default.
#
# The failure mode this guards is a well-meaning "fix": someone notices network.base.url.demo missing
# from the bridge, decides that is a bug, and relaxes the filter. It is not a bug — it is the filter
# working. derive.rb hardcodes REPO_ROOT, so the regex is asserted directly rather than by running it
# against a fixture tree.
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DERIVE="$(cd "$HERE/../../../white-label" && pwd)/derive.rb"
rc_ok=0

[ -f "$DERIVE" ] || { echo "   ❌ derive.rb not found at $DERIVE"; exit 1; }

ruby - "$DERIVE" <<'RUBY'
src = File.read(ARGV[0])
line = src[/^PLACEHOLDER\s*=\s*(\/.*\/[a-z]*)\s*$/, 1]
unless line
  puts "   ❌ PLACEHOLDER constant not found — derive.rb reshaped; update this canary rather than dropping it"
  exit 1
end
re = eval(line) # rubocop:disable Security/Eval — reading our own committed constant
ok = true
must_skip = {
  "https://demo.example.com" => "app-profile's shipped demo endpoint placeholder",
  "https://api.example.com"  => "app-profile's shipped prod endpoint placeholder",
  "YOUR_TEAM_ID"             => "signing placeholder",
  "com.yourcompany.yourapp"  => "bundle-id placeholder",
  "1:000000000000:ios:x"     => "firebase app-id placeholder",
  ""                         => "unset value",
}
must_keep = {
  "https://api.mifos.org"    => "a real authored endpoint",
  "Money Toolkit"            => "the reference app name",
  "Mifos Initiative"         => "the reference org name",
  "MoneyToolkit"             => "the reference log tag",
}
must_skip.each do |v, why|
  hit = v.strip.empty? || v.match?(re)
  puts(hit ? "   ✅ skips  #{v.inspect.ljust(28)} (#{why})" : "   ❌ KEEPS  #{v.inspect} — would ship a placeholder as a real value (#{why})")
  ok &&= hit
end
must_keep.each do |v, why|
  hit = !v.strip.empty? && !v.match?(re)
  puts(hit ? "   ✅ keeps  #{v.inspect.ljust(28)} (#{why})" : "   ❌ SKIPS  #{v.inspect} — a real authored value would be dropped (#{why})")
  ok &&= hit
end
exit(ok ? 0 : 1)
RUBY
rc_ok=$?
exit "$rc_ok"
