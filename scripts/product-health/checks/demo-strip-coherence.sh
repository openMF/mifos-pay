#!/usr/bin/env bash
# checks/demo-strip-coherence.sh — the POST-`--clean` tree must be coherent, not just the template's.
#
# Every other gate in this repo audits the tree as the TEMPLATE ships it. None of them audits the
# tree a fork actually gets, which is the tree AFTER `scripts/remove-demo.sh --apply` (fork-init.sh
# strips the demo BY DEFAULT — "forking = starting clean"). That blind spot is not theoretical: the
# E1/C7 relocation moved AppDatabase's demo entities/DAOs/migrations out of `demo:`-fenced blocks and
# into app-profile-driven `gen-*` codegen regions, which left a cleaned fork holding entities that
# referenced `kpt.core.database.demo.*` classes the strip had just deleted. Seven framework gates and
# three product-health checks were green over that, because none of them ran the strip.
#
# So this check RUNS the strip in a throwaway copy of the tracked tree and audits the result:
#
#   DSC-1  the strip completes (non-zero exit is itself the failure)
#   DSC-2  no surviving source references a deleted `**/demo/**` package
#   DSC-3  no surviving source references a deleted demo FEATURE module
#   DSC-4  no `demo:begin` fence survives outside test fixtures (the strip missed a file)
#   DSC-6  no surviving import names a `kpt.core.network.<pkg>` package that no longer exists —
#          endpoint code lives in a package per ACCESS POINT now, and the strip deletes the packages
#          of `owner: template` points. DSC-2 only knows the `demo` naming, so it is blind to this:
#          a Supabase test sitting in `config/` imported the deleted `project` endpoint's facade and
#          would have broken :core:network:commonTest on every cleaned fork.
#   DSC-5  every syncForkConfig-generated surface agrees with the STRIPPED app-profile — the strip
#          removes DECLARATIONS, and the generated output is committed source that must be re-derived
#          from what survives. fork-init.sh runs syncForkConfig BEFORE remove-demo (wrong side), so a
#          cleaned fork shipped AppAccessPoints/AppUrlTypes listing 5 endpoints app.yaml no longer had.
#
# Static only — no Gradle. A dangling reference to a deleted class is an unresolved-reference compile
# break, so grepping for one is a faithful proxy and costs seconds instead of a full KMP build.
#
# exit 0 PASS / 1 FAIL.
set -uo pipefail
: "${HEALTH_ROOT:?demo-strip-coherence: HEALTH_ROOT not set (run via product-health.sh)}"
STRIP="${DSC_STRIP:-$HEALTH_ROOT/scripts/remove-demo.sh}"
rc=0
ok()  { echo "   ✅ $1"; }
bad() { echo "   ❌ $1"; rc=1; }

[ -f "$STRIP" ] || { echo "no scripts/remove-demo.sh — nothing to audit (ok)"; exit 0; }
command -v git >/dev/null 2>&1 || { echo "git unavailable — cannot sandbox the tree"; exit 1; }

SB="$(mktemp -d)"; trap 'rm -rf "$SB"' EXIT
# Tracked files at their WORKING-TREE content — not `git archive HEAD`.
#
# HEAD is the wrong subject: this gate exists to catch a strip that leaves the tree incoherent, and
# the change that breaks it is the one you are about to commit. Sandboxing HEAD audits the last
# commit while the working tree is what actually ships — it silently passed a real dangling import
# (a Supabase test outside its endpoint package) because the whole restructure was uncommitted.
# build/ output and untracked scratch are still excluded: a fork receives tracked files.
( cd "$HEALTH_ROOT" && git ls-files -z 2>/dev/null | tar --null -T - -cf - 2>/dev/null ) | tar -x -C "$SB" 2>/dev/null
[ -f "$SB/scripts/remove-demo.sh" ] || { echo "⚠️  could not sandbox tracked tree (shallow/empty repo?) — skipped"; exit 0; }
# Audit the WORKING-TREE strip script, not HEAD's — that is what the next fork will run.
cp "$STRIP" "$SB/scripts/remove-demo.sh"

# Demo feature names come from the settings fence, before it is stripped.
DEMO_FEATURES="$(awk '/demo:begin/{s=1;next} /demo:end/{s=0} s' "$SB/settings.gradle.kts" 2>/dev/null \
  | grep -oE 'feature:[A-Za-z0-9_-]+' | sed 's/feature://' || true)"

echo "── post-strip coherence (remove-demo.sh --apply) ──"
# --no-regen keeps this check gradle-free and seconds-long; DSC-5 then asserts the coherence the
# regen is responsible for, so skipping it here cannot hide the defect it exists to catch.
if ( cd "$SB" && bash scripts/remove-demo.sh --apply --all --no-format --no-regen ) >/dev/null 2>&1; then
  ok "DSC-1 strip completed"
else
  bad "DSC-1 strip FAILED — a cleaned fork cannot even be produced"; exit 1
fi

# DSC-2 / DSC-3 — a reference in surviving source to a deleted demo package or feature module.
#
# Comments are STRIPPED before scanning. Several template-owned files legitimately NAME demo symbols
# in their KDoc ("the demo block relocated to [kpt.core.data.demo.di.DemoRepositoryModule]") — that
# documents the seam and is exactly what we want them to say. A raw grep flags all of them and the
# gate becomes noise nobody trusts; only a reference in CODE is an unresolved-reference compile
# break. Same reason NAP-7 strips comments before hunting hand-wired bindings.
command -v ruby >/dev/null 2>&1 || { echo "ruby unavailable — cannot scan comment-stripped source"; exit 1; }
scan_out="$(DSC_SB="$SB" DSC_FEATS="$DEMO_FEATURES" ruby -e '
  sb = ENV["DSC_SB"]
  feats = ENV["DSC_FEATS"].to_s.split(/\s+/).reject(&:empty?).map { |f| f.delete("-") }
  pkg_re  = /kpt\.[a-z]+(?:\.[a-z]+)?\.demo\./
  feat_re = feats.empty? ? nil : /kpt\.feature\.(#{feats.join("|")})[.]/
  pkg, feat = [], []
  Dir.glob(File.join(sb, "**/*.{kt,kts}")).sort.each do |f|
    next if f.include?("/build/") || f.include?("/product-health/tests/")
    src = File.read(f) rescue next
    src = src.gsub(%r{/\*.*?\*/}m, "").gsub(%r{//[^\n]*}, "")
    rel = f.sub(sb + "/", "")
    pkg  << rel if src =~ pkg_re
    feat << rel if feat_re && src =~ feat_re
  end
  puts "PKG:#{pkg.join(",")}"
  puts "FEAT:#{feat.join(",")}"
')"
dangling="$(echo "$scan_out" | sed -n 's/^PKG://p' | tr ',' '\n' | grep -v '^$' || true)"
featrefs="$(echo "$scan_out" | sed -n 's/^FEAT://p' | tr ',' '\n' | grep -v '^$' || true)"

if [ -z "$dangling" ]; then
  ok "DSC-2 no surviving CODE references a deleted demo package"
else
  bad "DSC-2 surviving code references deleted demo package(s) — unresolved reference at compile:"
  echo "$dangling" | sed 's#^#        #'
fi

if [ -n "$DEMO_FEATURES" ]; then
  if [ -z "$featrefs" ]; then
    ok "DSC-3 no surviving CODE references a deleted demo feature module"
  else
    bad "DSC-3 surviving code references deleted demo feature module(s):"
    echo "$featrefs" | sed 's#^#        #'
  fi
fi

# DSC-4 — a surviving fence means the strip skipped a file it was supposed to process.
fences="$(grep -rl 'demo:begin' "$SB" --include='*.kt' --include='*.kts' --include='*.yaml' 2>/dev/null \
  | grep -v '/build/' | grep -v '/product-health/tests/' | grep -v '/module-packages\.yaml$' | sed "s#^$SB/##" || true)"
if [ -z "$fences" ]; then
  ok "DSC-4 no demo fence survives outside test fixtures"
else
  bad "DSC-4 demo fence(s) survive — the strip skipped these files:"
  echo "$fences" | sed 's#^#        #'
fi

# DSC-6 — every referenced kpt.core.network.<pkg> exists on disk after the strip.
net_root="$SB/core/network/src/commonMain/kotlin/kpt/core/network"
if [ -d "$net_root" ]; then
  existing="$(find "$net_root" -mindepth 1 -maxdepth 1 -type d -exec basename {} \; 2>/dev/null | sort -u)"
  dangling_pkg="$(DSC_SB="$SB" DSC_PKGS="$existing" ruby -e '
    sb = ENV["DSC_SB"]; have = ENV["DSC_PKGS"].to_s.split(/\s+/).to_set rescue nil
    require "set"; have = ENV["DSC_PKGS"].to_s.split(/\s+/).to_set
    out = []
    Dir.glob(File.join(sb, "**/*.kt")).sort.each do |f|
      next if f.include?("/build/") || f.include?("/product-health/tests/")
      src = File.read(f) rescue next
      src = src.gsub(%r{/\*.*?\*/}m, "").gsub(%r{//[^\n]*}, "")
      src.scan(/kpt\.core\.network\.([a-z0-9]+)\./).flatten.uniq.each do |pkg|
        next if have.include?(pkg)
        out << "#{f.sub(sb + "/", "")} -> kpt.core.network.#{pkg}"
      end
    end
    puts out.uniq
  ' 2>/dev/null || true)"
  if [ -z "$dangling_pkg" ]; then
    ok "DSC-6 every referenced kpt.core.network.<pkg> still exists"
  else
    bad "DSC-6 surviving code imports a DELETED endpoint package:"
    echo "$dangling_pkg" | sed 's#^#        #'
  fi
fi

# DSC-5 — the strip must OWN post-strip regeneration; without it the generated surfaces keep the
# demo entries app-profile no longer declares (NAP-1/NAP-3 red on every cleaned fork).
if grep -q 'syncForkConfig' "$SB/scripts/remove-demo.sh"; then
  ok "DSC-5 the strip re-derives generated surfaces from the stripped app-profile"
else
  bad "DSC-5 the strip never re-derives generated surfaces — AppAccessPoints/AppUrlTypes will keep"
  echo "        demo endpoints app.yaml no longer declares (fork-init.sh runs syncForkConfig BEFORE"
  echo "        the strip, which is the wrong side of it)"
fi

[ "$rc" = "0" ] || { echo "     → a fork gets THIS tree; fix remove-demo.sh (or the codegen it must undo)."; exit 1; }
echo "post-strip coherence: cleaned fork has no dangling demo reference and no surviving fence"
