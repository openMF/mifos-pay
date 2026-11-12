#!/usr/bin/env bash
# core-package-ownership.sh — declared-package modules stay consistent with the contract.
#
# Covers every core module that classifies by DECLARATION rather than by path — all six with a
# `module-packages.yaml`: model, data, domain, common, store, database. It replaced the per-module
# store-package-ownership.sh, which audited exactly one of them against a now-deleted app.yaml block. Both are audited by the same four checks; only their per-module facts differ.
#
# WHY THIS EXISTS
# core/model classifies the OTHER way round from its sibling modules: the TEMPLATE's packages are
# enumerated (app-profile#model.packages[] + a `demo-showcase` rule each) and everything else under
# `kpt/core/model/` is the FORK's, resolved by the module catch-all. That is what makes the strip
# clean — one list to delete, no declaration needed for a fork's own models.
#
# It is only safe while the template's half is actually declared. A template package with no
# app-profile row is never stripped (a "clean" fork keeps showcase models it did not ask for); a
# template package with no ownership rule falls to the fork catch-all and silently stops syncing.
# Neither shows up as a build failure — the tree compiles perfectly in both cases.
#
#   MP-1 every `owner: template` package in <module>.packages[] resolves `demo-showcase`
#   MP-2 every declared package exists on disk in at least one source set
#   MP-3 no `demo/` directory remains — ONLY where the module has fully left that convention.
#        core/data keeps `demo/di/` on purpose: DemoRepositoryModule's lifecycle is governed by the
#        DI-seam contract (WLS-2/WLS-4) instead, which needs it under demo/ AND inside
#        ForkFeatureRegistry's demo fence. Deleting the package without unregistering it breaks the graph.
#   MP-4 an UNDECLARED package resolves `fork` — the catch-all direction the design depends on
#   MP-6 app.yaml#module_packages lists exactly the module-packages.yaml files that exist — the
#        connection cannot drift: a module file added without the row is invisible to a reader, and
#        a row without its file points at nothing.
#   MP-7 every `status:` matches what is actually on disk — it is derived, so a stale one is a lie
#        that `/kmp-project-template-sync` would act on
#   MP-8 feature/module-packages.yaml — ONE registry for the whole directory (a feature module is
#        wholly fork-owned, so lifecycle is a module property) — matches the directories on disk
#   MP-5 every package ON DISK is declared — an undeclared directory is invisible to the sync and,
#        if it is the template's, is never reached by the strip either
#
# A fork's own package is deliberately NOT required to be declared. That asymmetry is the point.
#
# exit 0 PASS / 1 FAIL.
set -uo pipefail
: "${HEALTH_ROOT:?model-package-ownership: HEALTH_ROOT not set (run via product-health.sh)}"
CS="$HEALTH_ROOT/scripts/customization-surface.sh"
APP_YAML="${MP_YAML:-$HEALTH_ROOT/app-profile/app.yaml}"   # kept for compatibility; rows now live per-module
fails=0
# Print an informational line that does NOT fail the check.
# Separate from the failure path so a skipped module reads as a skip, not a pass.
note() { echo "  $1"; fails=$((fails + 1)); }

# <yaml key>:<module dir>:<demo-dir-must-be-gone>
# Every core module with a module-packages.yaml is audited. `demo_gone` is per-module because
# core/data keeps `demo/di/` (WLS governs it) and core/database keeps a demo test tree.
CORE_MODULES="${MP_MODULES:-model:model:yes data:data:no domain:domain:yes common:common:yes store:store:yes database:database:no network:network:yes platform:platform:yes datastore:datastore:yes designsystem:designsystem:yes firebase:firebase:yes ui:ui:yes}"
summary=""

[ -f "$APP_YAML" ] || { echo "no app-profile/app.yaml — nothing to audit (ok)"; exit 0; }

# The FORK's package ledger (app-profile/package-ledger.yaml, owner: fork): what the fork did to the
# template's declared packages. The module-packages.yaml files are template-owned and full-copied, so
# their `status:` describes the TEMPLATE's tree; a fork that stripped a demo package records it here
# instead of editing a file the next sync overwrites. "module<TAB>id<TAB>status" per row.
LEDGER="${MP_LEDGER:-$HEALTH_ROOT/app-profile/package-ledger.yaml}"
LEDGER_ROWS=""
if [ -f "$LEDGER" ]; then
  LEDGER_ROWS="$(awk '/^[[:space:]]*-[[:space:]]*\{/ {
      m=$0; sub(/.*module:[[:space:]]*/,"",m); sub(/[,}[:space:]].*$/,"",m)
      i=$0; sub(/.*id:[[:space:]]*/,"",i);     sub(/[,}[:space:]].*$/,"",i)
      s=$0; sub(/.*status:[[:space:]]*/,"",s); sub(/[,}[:space:]].*$/,"",s)
      print m "\t" i "\t" s }' "$LEDGER")"
fi

# ledger_status <module label> <id> → the ledger row mapped to this check vocabulary
# (exists|removed|missing), or "" when the ledger has no row for it.
ledger_status() {
  local s
  s="$(printf '%s\n' "$LEDGER_ROWS" | awk -F'\t' -v m="$1" -v i="$2" '$1==m && $2==i {print $3; exit}')"
  case "$s" in removed) echo removed ;; kept) echo exists ;; absent) echo missing ;; *) echo "" ;; esac
}
# shellcheck disable=SC1090
. "$CS" 2>/dev/null || { echo "❌ cannot source customization-surface.sh"; exit 2; }

for spec in $CORE_MODULES; do
  key="${spec%%:*}"; rest="${spec#*:}"; mod="${rest%%:*}"; demo_gone="${rest##*:}"
  case "$mod" in
    feature/*)
      SRC="$HEALTH_ROOT/$mod/src"; MOD_LABEL="$mod"
      # DISCOVER the kotlin package dir — it is not the module name: `feature/emi-calculator` holds
      # `kpt/feature/emicalculator`. Deriving it produced a path that never matched, so every probe
      # reported the package missing.
      pkgdir="$(find "$SRC" -type d -path '*/kotlin/kpt/feature/*' -depth 5 2>/dev/null | head -1)"
      PKG_REL="kotlin/kpt/feature/$(basename "${pkgdir:-x}")"
      ;;
    *)         SRC="$HEALTH_ROOT/core/$mod/src"; PKG_REL="kotlin/kpt/core/$mod"; MOD_LABEL="core/$mod" ;;
  esac
  [ -d "$SRC" ] || continue

  # Declared rows now live in the MODULE: core/<mod>/module-packages.yaml, template-only and
  # full-copying, so a template package addition is never a merge against a fork's edit.
  PKG_FILE="$HEALTH_ROOT/$MOD_LABEL/module-packages.yaml"
  if [ ! -f "$PKG_FILE" ]; then
    note "❌ MP-0 $MOD_LABEL has no module-packages.yaml — its lifecycle is undeclared"
    continue
  fi
  declared="$(awk '
    /^(demo|template|fork):[[:space:]]*$/ { sec=$0; sub(/:.*/,"",sec); next }
    /^[a-z_]+:/                           { sec="" }
    sec != "" && /^[[:space:]]*-[[:space:]]*\{/ {
      id=$0; sub(/.*id:[[:space:]]*/,"",id); sub(/[,}[:space:]].*$/,"",id)
      st=$0; sub(/.*status:[[:space:]]*/,"",st); sub(/[[:space:]}].*$/,"",st)
      print id "\t" sec "\t" st
    }
  ' "$PKG_FILE")"

  if [ -z "$declared" ]; then
    note "❌ MP-0 $MOD_LABEL/module-packages.yaml declares nothing — this audit would pass vacuously"
    continue
  fi

  while IFS=$'\t' read -r id owner status; do
    [ -z "${id:-}" ] && continue
    # A demo row the strip removed is KEPT on purpose (the header's `demo:` contract) — its absence is
    # the expected state, and MP-7 below still proves `removed` is true. Only a live row must exist.
    [ "$owner" = "demo" ] && [ "$status" = "removed" ] && continue
    # Likewise a package the FORK's ledger records as removed / absent: its absence is the fork's
    # declared intent, and MP-7 still holds the disk to that record.
    case "$(ledger_status "$MOD_LABEL" "$id")" in removed|missing) continue ;; esac
    # MP-2 — a declared package that does not exist is a row nobody maintains.
    if ! compgen -G "$SRC/*/$PKG_REL/$id" >/dev/null 2>&1; then
      note "❌ MP-2 declared $MOD_LABEL package '$id' has no directory under $PKG_REL/"
      continue
    fi
    # MP-1 — the STRIP owner and the SYNC owner must agree:
    #   demo     -> demo-showcase  (shipped, updated on sync, deleted by --clean)
    #   template -> template       (framework, full-copied, kept by --clean)
    # A `demo` row with no demo-showcase rule falls to the fork catch-all and silently stops syncing.
    # MP-1 is a CORE-module invariant. A feature module is `owner: fork` on the sync axis by design
    # (a fork owns its features), and its `demo:` rows mean "this module is include()d inside the
    # settings.gradle.kts demo fence", not "demo-showcase surface class". Asserting the core rule
    # there would demand a classification the design deliberately does not use.
    case "$MOD_LABEL" in feature/*) continue ;; esac
    got="$(cs_resolve_owner "$MOD_LABEL/src/commonMain/$PKG_REL/$id/Probe.kt")"
    case "$owner" in
      demo)
        [ "$got" = "demo-showcase" ] \
          || note "❌ MP-1 $MOD_LABEL package '$id' is owner:demo but resolves '$got' — expected demo-showcase (add its rule to customization-surface.yaml, or it stops syncing to forks)" ;;
      template)
        # TEMPLATE-authored code must keep SYNCING. `template` (full-copy), `merge` (3-way) and
        # `generated` all deliver upstream fixes; `fork` does not, and `demo-showcase` would strip it.
        #
        # `fork` is the dangerous verdict, and the reason this is no longer a blanket "anything but
        # demo-showcase": adding a module catch-all `core/<m>/** -> fork` silently reclassified every
        # framework package that had no more-specific rule, so chart / component / bottombar / input
        # stopped receiving upstream fixes and NOTHING failed. Declare framework packages explicitly
        # ABOVE the catch-all.
        case "$got" in
          template|merge|generated) : ;;
          *) note "❌ MP-1 $MOD_LABEL package '$id' is owner:template but resolves '$got' — template code must keep syncing; add an explicit rule above the module catch-all" ;;
        esac ;;
      fork)
        # The fork's own: must not be full-copied over, and must not be strippable.
        case "$got" in
          fork|merge) : ;;
          *) note "❌ MP-1 $MOD_LABEL package '$id' is owner:fork but resolves '$got' — a sync would overwrite the fork's work" ;;
        esac ;;
      *)
        note "❌ MP-1 $MOD_LABEL package '$id' has owner '$owner' — expected demo | template | fork" ;;
    esac
  done <<< "$declared"

  # MP-5 — every package ON DISK is declared. This is what makes the file a usable input for
  # `/kmp-project-template-sync`: an undeclared directory is one the sync cannot reason about, and
  # (for a template package) one the strip will never reach, so it survives --clean by accident.
  # A package the contract resolves `fork` needs no row — that is the header's asymmetry ("a fork's
  # own package is deliberately NOT required to be declared"), and MP-4 already proves the catch-all
  # points that way. MP-5 used to fail every fork package, which pushed forks into editing this
  # template-owned file and losing the edit on the next sync.
  for d in "$SRC"/*/"$PKG_REL"/*/; do
    [ -d "$d" ] || continue
    pkg="$(basename "$d")"
    printf '%s\n' "$declared" | cut -f1 | grep -qx "$pkg" && continue
    src_set="$(basename "$(dirname "$(dirname "${d%/}")")")"
    [ "$(cs_resolve_owner "$MOD_LABEL/src/$src_set/$PKG_REL/$pkg/X.kt" 2>/dev/null)" = "fork" ] && continue
    note "❌ MP-5 $MOD_LABEL package '$pkg' exists on disk but is NOT declared in $MOD_LABEL/module-packages.yaml"
  done

  # MP-7 — `status:` is DERIVED, so a stale value is a lie the sync would act on. Recompute and
  # compare rather than trust the file.
  while IFS=$'\t' read -r id owner status; do
    [ -z "${id:-}" ] && continue
    on_disk=0
    for d in "$SRC"/*/"$PKG_REL"/"$id"; do [ -d "$d" ] && on_disk=1 && break; done
    # The fork's ledger, when it records this package, is the status to hold the disk to — and a
    # package it records `removed` is a deliberate strip, not a missing one, whatever its owner.
    led="$(ledger_status "$MOD_LABEL" "$id")"; [ -n "$led" ] && status="$led"
    if [ "$on_disk" -eq 1 ]; then want="exists"
    elif [ "$owner" = "demo" ] || [ "$led" = "removed" ]; then want="removed"
    else want="missing"; fi
    [ "$status" = "$want" ] \
      || note "❌ MP-7 $MOD_LABEL package '$id' says status:$status but disk says $want — run remove-demo.sh --apply, or record it in app-profile/package-ledger.yaml"
  done <<< "$declared"

  # MP-3 — only for modules that fully left the path convention.
  if [ "$demo_gone" = "yes" ] && find "$SRC" -type d -name demo -not -path '*/build/*' 2>/dev/null | grep -q .; then
    note "❌ MP-3 a demo/ directory still exists under $MOD_LABEL — ownership is declared now, not path-inferred"
  fi

  # MP-4 — the catch-all direction. A fork's own package must NOT be claimed by the template blanket.
  # MP-4 is a CORE invariant: it asserts the module catch-all beats the `core/**` blanket. A backbone
  # feature module (home/profile/settings) legitimately resolves `template` — the sync full-copies it.
  case "$MOD_LABEL" in feature/*) continue ;; esac
  undeclared="$(cs_resolve_owner "$MOD_LABEL/src/commonMain/$PKG_REL/zz-undeclared-fork-pkg/X.kt")"
  [ "$undeclared" = "fork" ] \
    || note "❌ MP-4 an undeclared $MOD_LABEL package resolves '$undeclared', expected fork — a fork's own code would be claimed by the core/** blanket"

  summary="$summary $MOD_LABEL=$(printf '%s\n' "$declared" | grep -c .)"
done

# ── MP-8 — the feature/ registry matches the directories on disk ────────────────────────────────
# One registry for the whole directory: a feature module is wholly fork-owned, so its lifecycle is a
# property of the MODULE. `status:` still has to be true, and an unregistered feature directory is
# exactly what `/kmp-project-template-sync` cannot reason about.
FEAT_REG="$HEALTH_ROOT/feature/module-packages.yaml"
if [ ! -f "$FEAT_REG" ]; then
  note "❌ MP-8 feature/module-packages.yaml is missing — feature modules would be unregistered"
else
  feat_declared="$(awk '
    /^(demo|template|fork):[[:space:]]*$/ { sec=$0; sub(/:.*/,"",sec); next }
    /^[a-z_]+:/                           { sec="" }
    sec != "" && /^[[:space:]]*-[[:space:]]*\{/ {
      id=$0; sub(/.*id:[[:space:]]*/,"",id); sub(/[,}[:space:]].*$/,"",id)
      st=$0; sub(/.*status:[[:space:]]*/,"",st); sub(/[[:space:]}].*$/,"",st)
      print id "\t" sec "\t" st
    }' "$FEAT_REG")"
  [ -n "$feat_declared" ] || note "❌ MP-8 feature/module-packages.yaml declares nothing — vacuous"
  while IFS=$'\t' read -r id sec st; do
    [ -z "${id:-}" ] && continue
    led="$(ledger_status "feature" "$id")"; [ -n "$led" ] && st="$led"
    if [ -d "$HEALTH_ROOT/feature/$id" ]; then want="exists"
    elif [ "$sec" = "demo" ] || [ "$led" = "removed" ]; then want="removed"
    else want="missing"; fi
    [ "$st" = "$want" ] \
      || note "❌ MP-8 feature/$id says status:$st but disk says $want — or record it in app-profile/package-ledger.yaml"
  done <<< "$feat_declared"
  for d in "$HEALTH_ROOT"/feature/*/; do
    fm="$(basename "$d")"
    printf '%s\n' "$feat_declared" | cut -f1 | grep -qx "$fm" && continue
    # A fork's own feature module needs no row in the template's registry (feature/** is fork-owned).
    [ "$(cs_resolve_owner "feature/$fm/build.gradle.kts" 2>/dev/null)" = "fork" ] && continue
    note "❌ MP-8 feature/$fm exists on disk but is NOT declared in feature/module-packages.yaml"
  done
fi

# ── MP-6 — the app.yaml connection matches the files on disk ────────────────────────────────────
listed="$(awk '/^module_packages:/{f=1;next} /^[a-z_]+:/{f=0} f && /^[[:space:]]*-[[:space:]]*/ {
    l=$0; sub(/^[[:space:]]*-[[:space:]]*/,"",l); sub(/[[:space:]]*#.*$/,"",l); print l }' "$APP_YAML" | sort)"
onbox="$( { ls core/*/module-packages.yaml; ls feature/module-packages.yaml; } 2>/dev/null | sort)"
if [ "$listed" != "$onbox" ]; then
  note "❌ MP-6 app.yaml#module_packages does not match the files on disk"
  diff <(printf '%s\n' "$listed") <(printf '%s\n' "$onbox") 2>/dev/null | sed 's/^/       /' || true
fi

[ "$fails" -eq 0 ] && { echo "✅ core-package-ownership: MP-1..MP-8 pass (declared:$summary)"; exit 0; }
echo "❌ core-package-ownership: $fails failure(s)"; exit 1
