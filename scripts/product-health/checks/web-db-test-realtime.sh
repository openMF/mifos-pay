#!/usr/bin/env bash
# web-db-test-realtime.sh — a web database test must wait in REAL time, never on virtual time.
#
# WHY THIS EXISTS
# On js/wasmJs the SQLite driver talks to a browser Worker, so a DAO Flow emission can ONLY arrive
# via a real async round trip. `runTest` runs on VIRTUAL time; turbine's `awaitItem()` waiting there
# for such a value spins the single JS thread, the browser stops answering Karma's ping, and the run
# dies with:
#     Disconnected (0 times) reconnect failed before timeout of 2000ms (ping timeout)
#
# That is indistinguishable from "Room lost the invalidation" — and is not. It is the harness
# blocking the very event loop the driver needs.
#
# This is not hypothetical. On 2026-09-17 exactly that harness produced a measured-looking verdict
# that Room 3.1.0-alpha01 drops invalidations on web. A rewrite of the SAME scenario on real time
# showed Room re-emitting correctly (single write, two rapid writes, and a 20-write burst under
# event-loop contention, on js AND wasmJs). The false verdict was written into a README as evidence
# and nearly justified keeping a whole invalidation bridge that the measurement then disproved.
#
# CONTRACT
#   WDT-1 a *Test.kt that COMPILES FOR WEB (commonTest / jsTest / wasmJsTest / webTest) and touches a
#         real Room database must not use turbine (`awaitItem(` / `awaitComplete(` / `.test {`).
#         Wait in real time instead: `withContext(Dispatchers.Default)` + `delay(...)` polling, and
#         collect with a plain `launch` into a list.
#
# Deliberately NOT flagged:
#   · turbine in a test that only uses FAKES (no real database) — no worker, no real async, no trap.
#     That is most of core-base/store's suite and it is correct.
#   · turbine under nonWebTest/desktopTest/iosTest/androidHostTest — those never compile for web, and
#     a real thread makes the wait genuinely concurrent.
#   A rule that fired on those would hit dozens of correct tests and get muted.
#
# ESCAPE HATCH (per file, must say why):
#     // web-db-realtime-ok: <reason>
#
# Exit 0 = PASS · 1 = FAIL (blocks).
set -uo pipefail

ROOT="${1:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)}"
FAILED=0

# Source sets whose tests are COMPILED FOR A WEB TARGET. nonWebTest is excluded by construction —
# that source set exists precisely because web cannot open a database there.
web_compiled() {
    case "$1" in
        */src/commonTest/*|*/src/jsTest/*|*/src/wasmJsTest/*|*/src/webTest/*) return 0 ;;
        *) return 1 ;;
    esac
}

# "Touches a real database" — a fake DAO does not, and must not be flagged.
touches_db() {
    grep -qE '(^|[^A-Za-z0-9_])(AppDatabase|RoomDatabase|testPlatformModule|Room\.(inMemory)?[Dd]atabaseBuilder)' "$1"
}

while IFS= read -r f; do
    [ -f "$ROOT/$f" ] || continue
    web_compiled "/$f" || continue
    touches_db "$ROOT/$f" || continue
    grep -q 'web-db-realtime-ok:' "$ROOT/$f" && continue

    # Comment lines are skipped: a KDoc explaining this very rule (as the probe test's does) is not
    # a turbine call. Without this the documentation of the trap would trip the gate describing it.
    hits="$(grep -nE '(awaitItem\(|awaitComplete\(|\.test *\{|app\.cash\.turbine)' "$ROOT/$f" \
            | grep -vE '^[0-9]+: *(\*|//|/\*)' || true)"
    [ -z "$hits" ] && continue

    while IFS= read -r h; do
        [ -z "$h" ] && continue
        printf '  ✗ WDT-1 %s:%s turbine/virtual-time wait in a web-compiled database test\n' \
            "$f" "${h%%:*}"
        printf '        %s\n' "$(printf '%s' "$h" | cut -d: -f2- | sed 's/^[[:space:]]*//' | cut -c1-96)"
        FAILED=1
    done <<< "$hits"
# product-health/tests/** is excluded for the same reason kdoc-comment-balance.sh excludes it: a RED
# canary fixture is DELIBERATELY broken, and it only proves the gate can fail if the gate still fails
# on it. Scanning the fixtures from the repo-wide walk would fail the repo the moment the canary was
# committed — which is exactly what happened here. The canary stages each fixture in its own throwaway
# git repo (run.sh), so the fixture paths it scans are never under this prefix.
done < <(cd "$ROOT" && git ls-files '*Test.kt' 2>/dev/null | grep -v '^scripts/product-health/tests/')

if [ "$FAILED" = "0" ]; then
    echo "  ✓ web-compiled database tests wait in real time (no turbine/virtual-time waits)"
    exit 0
fi
cat <<'EOF'
        → On js/wasmJs a DAO emission arrives only via a real Worker round trip. Waiting for it on
          `runTest` virtual time spins the single JS thread and the browser is dropped on a ping
          timeout, which reads as "Room lost the invalidation" but is the harness blocking the loop.
        → Collect with a plain `launch(Dispatchers.Default)` into a list and poll with real `delay`.
          See core/database/src/jsTest/.../WebInvalidationProbeTest.kt for the shape.
        → Deliberate exception: // web-db-realtime-ok: <reason>
EOF
exit 1
