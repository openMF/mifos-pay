#!/usr/bin/env bash
# =============================================================================
# ci-prepush.sh — tiered + parallel pre-push runner (KMP)
# =============================================================================
#
# Modes:
#   (no flag)   = FULL  — all 5 platforms (Android all variants + Desktop +
#                         Web + iOS framework link + K/N tests), ~15-25 min.
#                         Safest pre-push gate. Catches iOS/Web/Desktop
#                         regressions locally before GitHub Actions does.
#   --lite                — Android prod-release + shared JVM tests, ~5-7 min.
#                         Opt-in for routine Android-only changes.
#   --auto-fix            — re-runs failed tasks with their canonical healer
#                         (e.g. `detekt --auto-correct`,
#                         `:cmp-android:updateProdReleaseBadging`).
#                         Used by `/ci [X <n>]` matrix action.
#
# Parallelism:
#   - One Gradle invocation with --parallel + --max-workers=N
#   - N auto-detected from system RAM (2/4/8/12/16 tier)
#   - Override via CI_PREPUSH_MAX_WORKERS env var
#
# Memory tiers:
#   ≤ 8 GB  → 2 workers   (8 GB MacBook Air — safe, no OOM)
#   ≤ 16 GB → 4 workers   (16 GB devs)
#   ≤ 32 GB → 8 workers
#   ≤ 64 GB → 12 workers
#   > 64 GB → 16 workers
#
# Why default = FULL not lite:
#   - Pre-push is "the last gate" before GitHub Actions CI sees the code.
#   - Building locally catches iOS/Web/Desktop regressions ~5-10 min earlier
#     than CI would (CI runs on push and takes time to spin up runners).
#   - Devs opt into --lite when iterating on Android-only changes.
#
# Usage:
#   ./ci-prepush.sh                   # FULL (default, ~15-25 min)
#   ./ci-prepush.sh --lite            # LITE (~5-7 min)
#   ./ci-prepush.sh --auto-fix        # heal failed tasks
#   ./ci-prepush.sh --lite --auto-fix # combo
# =============================================================================

set -uo pipefail

if [ ! -f "./gradlew" ]; then
    echo "❌ gradlew not found in $(pwd)" >&2
    exit 1
fi

echo "Starting CI pre-push checks…"

# --- arg parsing -----------------------------------------------------------
AUTO_FIX_MODE=0
LITE_MODE=0
while [ $# -gt 0 ]; do
    case "$1" in
        --lite)         LITE_MODE=1 ;;
        --auto-fix)     AUTO_FIX_MODE=1 ;;
        --all|--full)   ;;   # legacy aliases — default already runs full
        -h|--help)      sed -n '1,40p' "${BASH_SOURCE[0]}"; exit 0 ;;
        *)              echo "Unknown flag: $1" >&2; exit 2 ;;
    esac
    shift
done

if [ "$LITE_MODE" = "1" ]; then
    echo "→ Mode: LITE (~5-7 min, Android prod-release + shared JVM tests)"
else
    echo "→ Mode: FULL (~15-25 min, all 5 platforms)"
fi

# --- auto-detect --max-workers from system RAM -----------------------------
TOTAL_RAM_GB=16   # safe default
if command -v sysctl >/dev/null 2>&1 && sysctl -n hw.memsize >/dev/null 2>&1; then
    TOTAL_RAM_GB=$(( $(sysctl -n hw.memsize) / 1024 / 1024 / 1024 ))
elif [ -f /proc/meminfo ]; then
    TOTAL_RAM_GB=$(awk '/MemTotal/ {print int($2/1024/1024)}' /proc/meminfo)
fi

if   [ "$TOTAL_RAM_GB" -le 8 ];  then DETECTED_WORKERS=2
elif [ "$TOTAL_RAM_GB" -le 16 ]; then DETECTED_WORKERS=4
elif [ "$TOTAL_RAM_GB" -le 32 ]; then DETECTED_WORKERS=8
elif [ "$TOTAL_RAM_GB" -le 64 ]; then DETECTED_WORKERS=12
else                                  DETECTED_WORKERS=16
fi

MAX_WORKERS="${CI_PREPUSH_MAX_WORKERS:-$DETECTED_WORKERS}"
echo "→ System: ${TOTAL_RAM_GB} GB RAM → --max-workers=${MAX_WORKERS} (override: CI_PREPUSH_MAX_WORKERS)"

# --- OS detection (iOS K/N targets are macOS-only) -------------------------
ON_MAC=0
[ "$(uname -s)" = "Darwin" ] && ON_MAC=1

# --- task DAG (pipe syntax: cmd|heal_cmd) ----------------------------------
declare -a CORE_TASKS=(
    "spotlessApply|"
    "detekt|detekt --auto-correct"
)

declare -a ANDROID_LITE_TASKS=(
    ":cmp-android:assembleProdRelease|"
    ":cmp-android:lintProdRelease|"
    ":cmp-android:testProdReleaseUnitTest|"
    ":cmp-android:checkProdReleaseBadging|:cmp-android:updateProdReleaseBadging"
)

declare -a ANDROID_FULL_TASKS=(
    ":cmp-android:build|"
    ":cmp-android:checkProdReleaseBadging|:cmp-android:updateProdReleaseBadging"
)

declare -a DESKTOP_TASKS=(
    ":cmp-desktop:packageReleaseDistributionForCurrentOS|"
)

declare -a WEB_TASKS=(
    ":cmp-web:jsBrowserDistribution|"
)

declare -a IOS_TASKS=(
    ":cmp-shared:linkPodReleaseFrameworkIosSimulatorArm64|"
    ":cmp-shared:linkPodReleaseFrameworkIosArm64|"
    ":cmp-shared:iosSimulatorArm64Test|"
)

declare -a SHARED_TASKS=(
    ":cmp-shared:jvmTest|"
)

declare -a TRAILING_TASKS=(
    "dependencyGuardBaseline|"
)

GRADLE_TASKS=( "${CORE_TASKS[@]}" )

if [ "$LITE_MODE" = "1" ]; then
    GRADLE_TASKS+=( "${ANDROID_LITE_TASKS[@]}" "${SHARED_TASKS[@]}" "${TRAILING_TASKS[@]}" )
else
    GRADLE_TASKS+=( "${ANDROID_FULL_TASKS[@]}" "${DESKTOP_TASKS[@]}" "${WEB_TASKS[@]}" "${SHARED_TASKS[@]}" )
    if [ "$ON_MAC" = "1" ]; then
        GRADLE_TASKS+=( "${IOS_TASKS[@]}" )
    else
        echo "⚠  iOS framework link + simulator tests skipped (non-macOS system)"
    fi
    GRADLE_TASKS+=( "${TRAILING_TASKS[@]}" )
fi

# --- dependency-guard CI-compat baseline -----------------------------------
DEPGUARD_PROPS="lib-integrate.properties"
DEPGUARD_RESTORE=0
if [ -f "$DEPGUARD_PROPS" ] && grep -q "\.local=true" "$DEPGUARD_PROPS"; then
    echo "→ Flipping $DEPGUARD_PROPS .local=true → .local=false for CI-compatible baseline"
    cp "$DEPGUARD_PROPS" "${DEPGUARD_PROPS}.prepush.bak"
    sed -i.sed-bak 's/\.local=true/\.local=false/g' "$DEPGUARD_PROPS"
    rm -f "${DEPGUARD_PROPS}.sed-bak"
    DEPGUARD_RESTORE=1
fi
# Put the dependency-guard properties file back after the push check.
# Registered on a trap, so an interrupted run does not leave the repo with a
# modified gradle.properties that a later build would silently pick up.
restore_depguard() {
    if [ "$DEPGUARD_RESTORE" = "1" ] && [ -f "${DEPGUARD_PROPS}.prepush.bak" ]; then
        mv "${DEPGUARD_PROPS}.prepush.bak" "$DEPGUARD_PROPS"
        echo "→ Restored $DEPGUARD_PROPS"
    fi
}
trap restore_depguard EXIT

# --- Phase 1: build-logic check (daemon warm-up) ---------------------------
successful_tasks=()
failed_tasks=()
healed_tasks=()

echo ""
echo "→ Phase 1: build-logic check (daemon warm-up)"
if ./gradlew check -p build-logic --build-cache --configuration-cache; then
    successful_tasks+=("check -p build-logic")
else
    failed_tasks+=("check -p build-logic")
fi

# --- Phase 2: single parallel Gradle invocation ----------------------------
GRADLE_TASK_ARGS=()
for entry in "${GRADLE_TASKS[@]}"; do
    GRADLE_TASK_ARGS+=("${entry%%|*}")
done

GRADLE_FLAGS=(
    --parallel
    --build-cache
    --configuration-cache
    "--max-workers=${MAX_WORKERS}"
    --continue
)

echo ""
echo "→ Phase 2: parallel build (${#GRADLE_TASK_ARGS[@]} tasks)"
printf '    %s\n' ${GRADLE_TASK_ARGS[@]+"${GRADLE_TASK_ARGS[@]}"}

if ./gradlew ${GRADLE_TASK_ARGS[@]+"${GRADLE_TASK_ARGS[@]}"} "${GRADLE_FLAGS[@]}"; then
    successful_tasks+=(${GRADLE_TASK_ARGS[@]+"${GRADLE_TASK_ARGS[@]}"})
else
    failed_tasks+=(${GRADLE_TASK_ARGS[@]+"${GRADLE_TASK_ARGS[@]}"})

    if [ "$AUTO_FIX_MODE" = "1" ]; then
        echo ""
        echo "→ --auto-fix mode: re-running with per-task healers"
        for entry in "${GRADLE_TASKS[@]}"; do
            task="${entry%%|*}"
            heal="${entry#*|}"
            if [ -n "$heal" ]; then
                echo ""
                echo "→ healing $task via $heal"
                if ./gradlew $heal --build-cache --configuration-cache; then
                    if ./gradlew $task --build-cache --configuration-cache; then
                        healed_tasks+=("$task <- $heal")
                    fi
                fi
            fi
        done
    fi
fi

restore_depguard

# --- summary ----------------------------------------------------------------
echo ""
echo "──────────────────────────────────────────────"
if [ "$LITE_MODE" = "1" ]; then
    echo "Mode: LITE   ·   ${TOTAL_RAM_GB} GB RAM   ·   --max-workers=${MAX_WORKERS}"
else
    echo "Mode: FULL   ·   ${TOTAL_RAM_GB} GB RAM   ·   --max-workers=${MAX_WORKERS}"
    [ "$ON_MAC" = "0" ] && echo "(iOS framework link skipped — non-macOS)"
fi
echo "──────────────────────────────────────────────"
echo "Successful tasks: ${#successful_tasks[@]}"
for t in ${successful_tasks[@]+"${successful_tasks[@]}"}; do echo "  ✅ $t"; done

if [ "${#healed_tasks[@]}" -gt 0 ]; then
    echo ""
    echo "Healed tasks: ${#healed_tasks[@]}"
    for t in "${healed_tasks[@]}"; do echo "  ⚠️  $t"; done
fi

if [ "${#failed_tasks[@]}" -gt 0 ]; then
    echo ""
    echo "Failed tasks: ${#failed_tasks[@]}"
    for t in "${failed_tasks[@]}"; do echo "  ❌ $t"; done
    echo ""
    echo "Please review the output above for failure details."
    exit 1
fi

echo ""
echo "All checks passed."
exit 0
