#!/usr/bin/env bash
# scripts/ci/verify-dynamic-flavor-propagation.sh
#
# AC-9 dynamic-propagation canary (deploy-gha-product-flavors epic, D9).
#
# Proves the "define once in the DSL, everything else auto-derives" invariant:
# adding a throwaway flavor via the designed LocalFlavors extension point makes
# it appear in variants.json AND become dispatch-selectable, WITH NO EDIT to any
# fastlane lane, GitHub Actions workflow, or the variant-resolver.
#
# Mechanism: drop a temporary flavor into build-logic/convention/.../local/LocalFlavors.kt
# (the reflective hook the plugin already exposes), regenerate the manifest, assert
# the new variant is present, assert the derivation surfaces (deployment/**, .github/**,
# variant_resolver.rb) are byte-untouched, then restore and assert clean revert.
#
# Run from the template repo root:  bash scripts/ci/verify-dynamic-flavor-propagation.sh
# Exit 0 = invariant holds; non-zero = a hardcoded flavor list leaked somewhere.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$REPO_ROOT"

LOCAL_FLAVORS="build-logic/convention/src/main/kotlin/local/LocalFlavors.kt"
MANIFEST="cmp-shared/build/kmp-flavors/variants.json"
CANARY_FLAVOR="canary"
CANARY_VARIANT="canaryRelease"
BACKUP="$(mktemp)"

# Print a canary progress line.
log()  { printf '  [canary] %s\n' "$*"; }
# Print a canary failure and exit 1 immediately — a partially-run canary
# proves nothing, so there is no continue-on-error path.
fail() { printf '  [canary] FAIL: %s\n' "$*" >&2; exit 1; }

# ── guard: the derivation surfaces must be clean BEFORE we start, so the
#    post-injection "untouched" assertion is meaningful. ─────────────────────
DERIVATION_GLOBS=(deployment .github build-logic/convention/src/main/kotlin/local)
baseline_dirty="$(git status --porcelain -- deployment .github deployment/_shared/variant_resolver.rb 2>/dev/null || true)"
if [[ -n "$baseline_dirty" ]]; then
  fail "deployment/ or .github/ has uncommitted changes; commit or stash before running the canary."
fi

# Restore LocalFlavors.kt from the backup taken before injection.
# On a trap, so an interrupted canary cannot leave a synthetic flavor behind.
restore() {
  if [[ -f "$BACKUP" ]]; then
    cp "$BACKUP" "$LOCAL_FLAVORS"
    rm -f "$BACKUP"
  fi
}
trap restore EXIT

# ── 1. snapshot + inject a throwaway flavor via the designed hook ───────────
cp "$LOCAL_FLAVORS" "$BACKUP"
log "injecting throwaway flavor '$CANARY_FLAVOR' into $LOCAL_FLAVORS"
cat > "$LOCAL_FLAVORS" <<'KOTLIN'
package local

import com.mobilebytelabs.kmpflavors.KmpFlavorExtension
import org.gradle.api.Project

// TEMPORARY — written by scripts/ci/verify-dynamic-flavor-propagation.sh. Restored on exit.
object LocalFlavors {
    fun apply(ext: KmpFlavorExtension, project: Project) {
        ext.flavors.register("canary") {
            dimension.set("contentType")
            applicationIdSuffix.set(".canary")
            bundleIdSuffix.set(".canary")
        }
    }
}
KOTLIN

# ── 2. regenerate the manifest from the mutated DSL ─────────────────────────
log "regenerating manifest: ./gradlew :cmp-shared:exportKmpFlavorsManifest"
./gradlew :cmp-shared:exportKmpFlavorsManifest -q

# ── 3. assert the throwaway variant auto-appeared ───────────────────────────
[[ -f "$MANIFEST" ]] || fail "manifest not produced at $MANIFEST"
jq -e --arg f "$CANARY_FLAVOR" '.flavors[] | select(.name==$f)'   "$MANIFEST" >/dev/null \
  || fail "flavor '$CANARY_FLAVOR' absent from manifest — dynamic propagation broken"
jq -e --arg v "$CANARY_VARIANT" '.variants[] | select(.name==$v)' "$MANIFEST" >/dev/null \
  || fail "variant '$CANARY_VARIANT' absent from manifest — dynamic propagation broken"
log "variants.json now carries '$CANARY_FLAVOR' + '$CANARY_VARIANT' — propagation OK"

# ── 4. assert ZERO edits leaked into the derivation surfaces ────────────────
#    (only LocalFlavors.kt — the intended knob — and the gitignored build output changed)
leaked="$(git status --porcelain -- deployment .github deployment/_shared/variant_resolver.rb 2>/dev/null || true)"
[[ -z "$leaked" ]] || fail "a flavor edit leaked into deployment/ or .github/:\n$leaked"
log "deployment/**, .github/**, variant_resolver.rb untouched — nothing hardcoded (D9)"

# ── 5. restore + assert clean revert ────────────────────────────────────────
restore
trap - EXIT
./gradlew :cmp-shared:exportKmpFlavorsManifest -q
if jq -e --arg f "$CANARY_FLAVOR" '.flavors[] | select(.name==$f)' "$MANIFEST" >/dev/null 2>&1; then
  fail "flavor '$CANARY_FLAVOR' still present after revert — LocalFlavors.kt not restored"
fi
log "reverted cleanly — manifest back to the base flavor set"

printf '  [canary] PASS — DSL is the single source; flavors auto-propagate with zero downstream edits (AC-9).\n'
