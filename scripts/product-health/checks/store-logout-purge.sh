#!/usr/bin/env bash
# store-logout-purge.sh — every store's rows are reachable by the logout purge.
#
# WHY THIS EXISTS
# `@StoreProvider(logout = true)` (the default) makes store-ksp emit BOTH the Koin binding and the
# `StoreCacheManager.register(...)` line that wipes the store on sign-out. One field producing both
# is what stops them disagreeing — a store cannot be bound but left unregistered.
#
# `logout = false` is the deliberate exception, and it exists for a mechanical reason rather than a
# policy one: Store5 5.1 does not make `MutableStore` a subtype of `Store`, so `register` cannot
# accept one. Every `MutableStore` in the template therefore sets it.
#
# That leaves a hole the annotation cannot close by itself. A `MutableStore` opted out of the purge
# is only safe because a PAIRED read store — same DAO, same table — IS purged, and wiping that table
# takes the mutable store's rows with it. Nothing verified the pairing. A fork adding
# `@StoreProvider(id = "receiptsMutable", logout = false)` with no `receipts` read store gets a
# clean build, green tests, and the previous user's receipts on the next sign-in on a shared device.
#
# The failure is invisible in exactly the way that matters: nothing throws, nothing logs, and the
# only symptom is data belonging to someone else.
#
#   LP-1 every `logout = false` store has a paired store whose id is the same minus the `Mutable`
#        suffix — the convention every shipped pair already follows
#   LP-2 the paired store is actually purged (it must NOT also set `logout = false`)
#   LP-3 both halves take the same DAO parameter type, so one table backs both and the purge
#        genuinely reaches the mutable store's rows
#   LP-4 a `logout = false` store whose id does NOT end in `Mutable` must say why, with a
#        `// logout-exempt: <reason>` comment on the line above — no silent opt-out
#
# Exit 0 = PASS · 1 = FAIL (blocks) · 2 = WARN. Pure bash + grep/sed — no Kotlin reflection.
set -uo pipefail

ROOT="${1:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)}"
STORE_DIR="$ROOT/core/store"

# Print a passing assertion.
pass() { printf '  ✓ %s\n' "$1"; }
# Print a failing assertion and set FAILED, so the check reports every problem in one run
# rather than stopping at the first.
fail() { printf '  ✗ %s\n' "$1"; FAILED=1; }
FAILED=0

if [ ! -d "$STORE_DIR" ]; then
    echo "  – no core/store in this fork — nothing to check"
    exit 0
fi

# Every @StoreProvider in the tree: "id<TAB>logout<TAB>file<TAB>line".
# `-A1` because the annotation and its function signature are on adjacent lines; the DAO type comes
# from the signature, so the two are read together.
ALL="$(mktemp)"; PURGED="$(mktemp)"; OPTOUT="$(mktemp)"
trap 'rm -f "$ALL" "$PURGED" "$OPTOUT"' EXIT

while IFS= read -r hit; do
    [ -z "$hit" ] && continue
    f="${hit%%:*}"; rest="${hit#*:}"; ln="${rest%%:*}"; body="${rest#*:}"
    id="$(printf '%s' "$body" | sed -n 's/.*id = "\([A-Za-z0-9_]*\)".*/\1/p')"
    [ -z "$id" ] && continue
    if printf '%s' "$body" | grep -q 'logout = false'; then lo=false; else lo=true; fi
    printf '%s\t%s\t%s\t%s\n' "$id" "$lo" "$f" "$ln" >> "$ALL"
done < <(grep -rn '@StoreProvider(' "$STORE_DIR" --include='*.kt' 2>/dev/null || true)

if [ ! -s "$ALL" ]; then
    echo "  ✗ LP-0 found 0 @StoreProvider declarations under core/store — refusing a vacuous pass"
    exit 1
fi

awk -F'\t' '$2=="true"  {print $1}' "$ALL" | sort -u > "$PURGED"
awk -F'\t' '$2=="false" {print $0}'  "$ALL" | sort -u > "$OPTOUT"

n_optout="$(wc -l < "$OPTOUT" | tr -d ' ')"
n_all="$(wc -l < "$ALL" | tr -d ' ')"

# The DAO parameter type of the provider function declaring a given id.
dao_of() {
    local want="$1" f l
    f="$(awk -F'\t' -v i="$want" '$1==i {print $3; exit}' "$ALL")"
    l="$(awk -F'\t' -v i="$want" '$1==i {print $4; exit}' "$ALL")"
    [ -n "$f" ] && [ -n "$l" ] || return 0
    # Scan the few lines after the annotation for the fn signature and pull the first param type.
    sed -n "${l},$((l + 6))p" "$f" 2>/dev/null \
        | grep -m1 -oE '\([a-zA-Z0-9_]+: [A-Za-z0-9_]+' | sed 's/.*: //'
}

while IFS=$'\t' read -r id lo file line; do
    [ -z "$id" ] && continue
    rel="${file#"$ROOT"/}"

    case "$id" in
        *Mutable)
            base="${id%Mutable}"
            if ! grep -qx "$base" "$PURGED"; then
                if awk -F'\t' -v b="$base" '$1==b' "$ALL" | grep -q .; then
                    fail "LP-2 '$id' opts out of the logout purge, but its pair '$base' ALSO sets logout = false — neither wipes the table, so both survive sign-out ($rel:$line)"
                else
                    fail "LP-1 '$id' opts out of the logout purge and there is NO paired '$base' read store to wipe its table — its rows survive sign-out ($rel:$line)"
                fi
                continue
            fi
            md="$(dao_of "$id")"; bd="$(dao_of "$base")"
            if [ -n "$md" ] && [ -n "$bd" ] && [ "$md" != "$bd" ]; then
                fail "LP-3 '$id' takes $md but its purged pair '$base' takes $bd — different DAOs mean different tables, so wiping '$base' does not reach '$id' rows ($rel:$line)"
            fi
            ;;
        *)
            # Not a Mutable pair — the convention cannot explain the opt-out, so it must be stated.
            if ! sed -n "$((line - 1))p" "$file" 2>/dev/null | grep -q 'logout-exempt:'; then
                fail "LP-4 '$id' sets logout = false but its id does not end in 'Mutable', so no paired store explains the opt-out. Add a '// logout-exempt: <reason>' comment above the annotation, or set logout = true ($rel:$line)"
            fi
            ;;
    esac
done < "$OPTOUT"

if [ "$FAILED" -eq 0 ]; then
    pass "LP-1..LP-4 $n_all store(s), $n_optout opted out of the purge — every one reachable via a paired purged store"
fi

exit "$FAILED"
