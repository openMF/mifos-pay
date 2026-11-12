#!/usr/bin/env bash
# customization-surface.sh — reader + validator for customization-surface.yaml
# ─────────────────────────────────────────────────────────────────────────────
# The single consumer-facing API for the fork-ownership contract. Pure bash + awk
# (no yq/python/jq dependency, so it runs on any fork out of the box).
#
# Library use (source it):
#     source scripts/customization-surface.sh
#     owner=$(cs_resolve_owner "cmp-android/src/main/AndroidManifest.xml")   # -> merge
#     strat=$(cs_resolve_strategy "cmp-android/src/main/AndroidManifest.xml") # -> manifest-union
#
# CLI use:
#     scripts/customization-surface.sh resolve <path>   # print owner (+ strategy)
#     scripts/customization-surface.sh report           # owner of every tracked path
#     scripts/customization-surface.sh verify            # CI: fail if any tracked
#                                                        # path only matches the default
#     scripts/customization-surface.sh --verify          # CI: fail if any ownership glob
#                                                        # matches ZERO real paths (dead-glob
#                                                        # / contract-rot guard; E0/T2)
# ─────────────────────────────────────────────────────────────────────────────
# NOTE: shell options are set only on direct execution (bottom), NOT on source —
# sourcing must not leak `set -u`/`pipefail` into a caller like scripts/white-label/sync-dirs.sh.

CS_SELF="${BASH_SOURCE[0]}"
CS_ROOT="$(cd "$(dirname "$CS_SELF")/.." && pwd)"
CS_CONTRACT="${CS_CONTRACT:-$CS_ROOT/customization-surface.yaml}"

# Parse the YAML into "glob<TAB>owner<TAB>strategy<TAB>default" rows, in file order.
# Handles both block form (- glob:\n  owner:) and inline form (- { glob: x, owner: y }).
cs_parse_rules() {
  awk '
    function emit() {
      if (glob != "") printf "%s|%s|%s|%s\n", glob, owner, strat, def
      glob=""; owner=""; strat=""; def="0"
    }
    # inline form:  - { glob: "x", owner: y, strategy: z }
    /^[[:space:]]*-[[:space:]]*\{/ {
      emit()               # flush any pending block-form rule first
      line=$0
      g=line; sub(/.*glob:[[:space:]]*"?/,"",g); sub(/"?[[:space:]]*,.*/,"",g); sub(/"?[[:space:]]*}.*/,"",g)
      o=line; sub(/.*owner:[[:space:]]*/,"",o); sub(/[[:space:]]*,.*/,"",o); sub(/[[:space:]]*}.*/,"",o)
      glob=g; owner=o; strat=""; def="0"
      if (line ~ /default:[[:space:]]*true/) def="1"
      emit(); next
    }
    # package-manifest directive — expanded by cs_expand_packages at this position
    /^[[:space:]]*-[[:space:]]*packages:/ { emit(); print "@packages|"; next }
    # block form start — extract the QUOTED value (drops any trailing # comment)
    /^[[:space:]]*-[[:space:]]*glob:/ {
      emit()
      g=$0; sub(/.*glob:[[:space:]]*"/,"",g); sub(/".*/,"",g)
      glob=g; next
    }
    # owner/strategy are single tokens — take the first word (drops trailing comments)
    /^[[:space:]]*owner:/    { o=$0; sub(/.*owner:[[:space:]]*/,"",o);    sub(/[[:space:]].*/,"",o); owner=o; next }
    /^[[:space:]]*strategy:/ { s=$0; sub(/.*strategy:[[:space:]]*/,"",s); sub(/[[:space:]].*/,"",s); strat=s; next }
    /^[[:space:]]*default:[[:space:]]*true/ { def="1"; next }
    END { emit() }
  ' "$CS_CONTRACT"
}

# Convert a contract glob to an anchored ERE.
#   **/x -> (.*/)?x   |   ** -> .*   |   * -> [^/]*   |   . escaped
cs_glob_to_regex() {
  local g="$1"
  local s1=$'\x01' s2=$'\x02'   # sentinels — never occur in a path glob
  g="${g//./\\.}"
  # Placeholder the multi-segment tokens FIRST so the single-`*` pass below can't
  # re-clobber the `*` inside their `.*` expansions (globstar → sentinel → restore).
  g="${g//\*\*\//$s1}"   # **/  (match zero+ segments)
  g="${g//\*\*/$s2}"     # **   (match anything)
  g="${g//\*/[^/]*}"     # *    (match within one segment)
  g="${g//$s1/(.*/)?}"
  g="${g//$s2/.*}"
  printf '^%s$' "$g"
}

# Load + compile the rules ONCE into parallel arrays (parse is expensive; matching
# is hot). Idempotent — safe to call before every resolve.
CS_LOADED=0
cs_load_rules() {
  [ "$CS_LOADED" = 1 ] && return
  CS_RX=(); CS_OWNER=(); CS_STRAT=(); CS_DEF=(); CS_GLOB=()
  local glob owner strat def
  while IFS='|' read -r glob owner strat def; do
    [ -z "$glob" ] && continue
    CS_RX+=("$(cs_glob_to_regex "$glob")")
    CS_OWNER+=("$owner"); CS_STRAT+=("$strat"); CS_DEF+=("$def"); CS_GLOB+=("$glob")
  done < <(cs_parse_rules | cs_expand_packages)
  CS_LOADED=1
}

# Package-level ownership lives BESIDE each module, in its module-packages.yaml — the contract does
# not restate it. A `- packages: module-packages` entry in the contract marks WHERE (in first-match
# order) those rows apply; it expands into one `demo-showcase` rule per `demo:` id of each
# core/<module>/module-packages.yaml → core/<module>/**/kpt/core/<module>/<id>/**.
# (feature/module-packages.yaml lists whole demo MODULES, which are fork territory and never synced;
# `feature/**` already owns them.) Manifests are found next to the contract being evaluated, so a
# fork, a fixture and a scratch copy (ownership audit, surface report) each read their own.
cs_expand_packages() {
  local base line f mod id
  base="$(dirname "$CS_CONTRACT")"
  while IFS= read -r line; do
    case "$line" in
      "@packages|"*)
        for f in "$base"/core/*/module-packages.yaml; do
          [ -f "$f" ] || continue
          mod="$(basename "$(dirname "$f")")"
          while IFS= read -r id; do
            [ -n "$id" ] && printf 'core/%s/**/kpt/core/%s/%s/**|demo-showcase||0\n' "$mod" "$mod" "$id"
          done < <(awk '/^demo:[[:space:]]*$/{s=1; next} /^[a-z_]+:/{s=0}
                        s && /id:/ {v=$0; sub(/.*id:[[:space:]]*/,"",v); sub(/[,}[:space:]].*/,"",v); print v}' "$f")
        done ;;
      *) printf '%s\n' "$line" ;;
    esac
  done
}

# ── --verify zero-match-rule guard (pure-white-label-store5-network E0/T2) ────────
# Every NON-default ownership glob MUST match >=1 real path in the tree — a rule that
# resolves to zero paths silently mis-owns whatever the author MEANT to cover (exactly
# how the 4 dead globs `core/store/{economic,banking}/**`, `appStoreModule*.kt`,
# `core/**/src/**/local/**` survived). This turns contract rot into a hard failure.
#
# "Real path" universe = git-tracked files UNION files present on disk (so gitignored-
# but-materialized fork paths — local.properties, gradle/fork.properties, secrets/live/**,
# secrets/.sync-meta.json — count as matches, never false-flagged). Heavy build output
# (build/, .gradle/, .kotlin/, node_modules/, .git/) is pruned.
#
# EXEMPT: `owner: generated` rules describe REGENERABLE artifacts (store og-images /
# screenshots / metadata) that are legitimately absent from a freshly-synced / bare
# template tree — flagging them would be a false positive by design (a fork regenerates
# them from app-profile/). The catch-all default `**` rule is skipped too.
#
# Returns 0 iff every enforced rule matches >=1 path; 1 (listing the offenders) otherwise.
cs_build_path_universe() {
  {
    git -C "$CS_ROOT" ls-files 2>/dev/null
    ( cd "$CS_ROOT" 2>/dev/null && \
      find . \( -path './.git' -o -path '*/build' -o -path '*/.gradle' \
                -o -path '*/.kotlin' -o -path '*/node_modules' \) -prune \
             -o -type f -print 2>/dev/null | sed 's#^\./##' )
  } | sort -u
}

cs_verify_zero_match() {
  cs_load_rules
  local universe rc=0 i n
  universe="$(mktemp)"
  cs_build_path_universe > "$universe"
  if [ ! -s "$universe" ]; then
    rm -f "$universe"
    echo "❌ --verify: could not enumerate any real paths under $CS_ROOT" >&2
    return 1
  fi
  for i in "${!CS_RX[@]}"; do
    [ "${CS_DEF[$i]}" = "1" ] && continue            # catch-all default — matches everything by design
    [ "${CS_OWNER[$i]}" = "generated" ] && continue  # regenerable artifacts, legitimately absent
    n="$(grep -cE "${CS_RX[$i]}" "$universe" 2>/dev/null || true)"
    if [ "${n:-0}" -eq 0 ]; then
      echo "❌ ZERO-MATCH RULE (owner=${CS_OWNER[$i]}, matches no real path): ${CS_GLOB[$i]}" >&2
      rc=1
    fi
  done
  rm -f "$universe"
  if [ "$rc" -eq 0 ]; then
    echo "✅ --verify: every ownership rule matches >=1 real path (generated carve-outs exempt)"
  else
    echo "❌ --verify: one or more ownership rules match ZERO real paths — dead/mis-declared contract rot" >&2
  fi
  return "$rc"
}

# First matching rule → set globals CS_M_OWNER / CS_M_STRAT / CS_M_DEFAULT.
# No subshell / no print (hot path — called once per tracked file in `verify`).
# cs_load_roots — the paths the template SHIPS: SYNC_DIRS + SYNC_FILES from the engine itself
# (single list, never duplicated). Empty when the engine is absent (bare checkouts / fixtures), in
# which case the root check is skipped and the default stays template — the pre-2026-10-31 behaviour.
CS_ROOTS_LOADED=0; CS_ROOTS=()
cs_load_roots() {
  [ "$CS_ROOTS_LOADED" = 1 ] && return
  # The engine BESIDE the contract being evaluated — not CS_ROOT, which is always this reader's own
  # repo. In a fork both are the fork's synced copies; in a fixture both are the fixture's.
  local eng; eng="$(dirname "$CS_CONTRACT")/scripts/white-label/sync-dirs.sh"; local r
  CS_ROOTS=()
  if [ -f "$eng" ]; then
    while IFS= read -r r; do [ -n "$r" ] && CS_ROOTS+=("$r"); done < <(
      awk '/^SYNC_(DIRS|FILES)=\(/{on=1; next} on&&/^\)/{on=0} on{ if (match($0,/"[^"]+"/)) print substr($0,RSTART+1,RLENGTH-2) }' "$eng")
  fi
  CS_ROOTS_LOADED=1
}

# cs_under_root <path> — 0 iff <path> is a sync root or sits under one.
cs_under_root() {
  local p="$1" r
  for r in ${CS_ROOTS[@]+"${CS_ROOTS[@]}"}; do   # bash-3.2-safe empty-array expansion (SP-4)
    [ "$p" = "$r" ] || [[ "$p" == "$r/"* ]] && return 0
  done
  return 1
}

# cs_match_g <path> — first-match-wins ownership lookup. Sets CS_M_OWNER / CS_M_STRAT / CS_M_DEFAULT;
# a default-rule match outside every sync root resolves `fork`.
cs_match_g() {
  cs_load_rules
  local path="$1" i
  for i in "${!CS_RX[@]}"; do
    if [[ "$path" =~ ${CS_RX[$i]} ]]; then
      CS_M_OWNER="${CS_OWNER[$i]}"; CS_M_STRAT="${CS_STRAT[$i]}"; CS_M_DEFAULT="${CS_DEF[$i]}"
      # The default rule claims only what the template SHIPS. Outside every sync root the path is
      # fork territory (see the `**` rule's SYNC ROOTS note in customization-surface.yaml).
      if [ "$CS_M_DEFAULT" = "1" ]; then
        cs_load_roots
        if [ "${#CS_ROOTS[@]}" -gt 0 ] && ! cs_under_root "$path"; then CS_M_OWNER="fork"; fi
      fi
      return 0
    fi
  done
  # TEMPLATE-FIRST fallback. This repo IS the template: the default answer to "who owns this?" is
  # the template, and FORK territory is declared explicitly (`core/**`, `feature/**`, `app-profile/**`,
  # the branding carve-outs) rather than inferred from silence.
  #
  # It used to be `fork` — "never clobber unknown" — which is the safe default for a CONSUMER but the
  # wrong one for a template: a directory the template ADDS and nobody writes a rule for silently
  # becomes fork-owned and never syncs to anyone. `tools/**` (the KSP processors driving every
  # @StoreProvider / @DbEntity / @ApiBinding) and `.bundle/**` both sat in exactly that state.
  #
  # Flipping is safe BECAUSE fork territory is explicitly claimed: `core/store/.../quests/X.kt` and
  # `feature/quests/X.kt` resolve `fork` through their module catch-alls, not through this line.
  # Verified against the tree at the time of the change — all 2712 tracked files matched an explicit
  # rule, so ZERO existing paths change owner. Only genuinely-unclaimed NEW paths move, and by the
  # template-first principle those are the template's.
  CS_M_OWNER="template"; CS_M_STRAT=""; CS_M_DEFAULT="1"
}

# Print the owner (template | fork | merge) for path $1.
cs_resolve_owner()    { cs_match_g "$1"; printf '%s' "$CS_M_OWNER"; }
# Print the merge strategy for path $1, meaningful only when the owner is `merge`.
cs_resolve_strategy() { cs_match_g "$1"; printf '%s' "$CS_M_STRAT"; }
# True when path $1 matched only the catch-all `**` rule — i.e. no rule claims it
# explicitly, so it falls to the template-first default.
cs_is_default()       { cs_match_g "$1"; [ "$CS_M_DEFAULT" = "1" ]; }

# 3-way merge driver for a `merge`-owned file. This is the general merge strategy
# behind manifest-union / catalog-3way / include-union / kotlin-3way / strings-union:
# git merge-file cleanly unions non-overlapping additions (e.g. a fork's extra
# <uses-permission> lines) and only emits conflict markers on a true overlap.
#
#   cs_merge_3way <ours> <base> <theirs> [<out>]
#     ours   = the fork's current file        base = common ancestor
#     theirs = the upstream/template file      out  = result (default: ours, in place)
#   returns  0 clean · 1 merged WITH conflict markers (needs review) · 2 error
cs_merge_3way() {
  local ours="$1" base="$2" theirs="$3" out="${4:-$1}" rc tmp
  [ -f "$ours" ] && [ -f "$base" ] && [ -f "$theirs" ] || {
    echo "cs_merge_3way: need existing <ours> <base> <theirs>" >&2; return 2; }
  tmp="$(mktemp)"
  # -p prints the merged result (does not mutate inputs); exit = conflict count.
  # Labelled markers: unlabelled, git names each side after its TEMP file (/var/folders/…/tmp.X), so
  # whoever resolves a sync conflict cannot tell which side is the fork's and which the template's.
  git merge-file -p -L "fork (ours)" -L "last-synced template (base)" -L "template (theirs)" \
    "$ours" "$base" "$theirs" > "$tmp"; rc=$?
  if [ "$rc" -ge 128 ]; then rm -f "$tmp"; echo "cs_merge_3way: git merge-file error ($rc)" >&2; return 2; fi
  mv "$tmp" "$out"
  [ "$rc" -eq 0 ] && return 0 || return 1
}

# AndroidManifest merge — the `manifest-union` strategy.
#   1. A real line 3-way first: with a base it keeps EVERY fork addition (activities, intent filters,
#      <queries>, <meta-data>, providers) and takes the template's changes.
#   2. Only when that conflicts — typically both sides inserting permission lines at the same spot —
#      fall back to the permission union below: THEIRS plus every <uses-permission>/<uses-feature> of
#      OURS whose android:name theirs lacks.
#   3. Either way, every line the FORK added (in ours, not in base) must survive in the result, or the
#      merge returns 1 with the 3-way's conflict markers. The union alone used to drop everything that
#      was not a permission and still report success: mbs/reels-downloader lost 50 manifest entries
#      (AdMob APPLICATION_ID, share/VIEW intent filters, PayCraft deep link, package queries).
#
#   cs_merge_manifest <ours> <base> <theirs> [<out>]
cs_merge_manifest() {
  local ours="$1" base="$2" theirs="$3" out="${4:-$1}" tmp add tw
  [ -f "$ours" ] && [ -f "$theirs" ] || { echo "cs_merge_manifest: need <ours> <base> <theirs>" >&2; return 2; }
  [ -f "$base" ] || base=/dev/null
  tw="$(mktemp)"
  if git merge-file -p "$ours" "$base" "$theirs" > "$tw" 2>/dev/null; then
    mv "$tw" "$out"; return 0
  fi
  # Every conflict is two INSERTIONS at the same spot (empty base section) — the usual manifest case:
  # both sides added elements after the same line. Keeping both is exactly right, so union them.
  # (a file, not a pipe: under a caller's `pipefail` the pipe returned merge-file's conflict status)
  git merge-file -p --diff3 "$ours" "$base" "$theirs" > "$tw" 2>/dev/null
  if awk '/^[|]{7} /{b=1; next} /^=======$/{b=0; next} b{bad=1} END{exit bad}' "$tw"; then
    git merge-file -p --union "$ours" "$base" "$theirs" > "$tw" 2>/dev/null
    mv "$tw" "$out"; return 0
  fi
  cs_merge_manifest_union "$ours" "$theirs" "$out"
  if ! cs_fork_lines_kept "$ours" "$base" "$out"; then
    git merge-file -p -L "fork (ours)" -L "last-synced template (base)" -L "template (theirs)" \
      "$ours" "$base" "$theirs" > "$tw" 2>/dev/null
    mv "$tw" "$out"; return 1
  fi
  rm -f "$tw"; return 0
}

# cs_fork_lines_kept <ours> <base> <out> — every non-blank line the fork added survives in <out>.
cs_fork_lines_kept() {
  local l
  while IFS= read -r l; do
    [ -n "${l//[[:space:]]/}" ] || continue
    grep -qxF -- "$l" "$2" 2>/dev/null && continue
    grep -qF -- "$(printf '%s' "$l" | sed 's/^[[:space:]]*//')" "$3" || return 1
  done < "$1"
  return 0
}

# Permission union (the conflict fallback of cs_merge_manifest).
#   cs_merge_manifest_union <ours> <theirs> <out>
cs_merge_manifest_union() {
  local ours="$1" theirs="$2" out="$3" tmp add
  local theirs_names; theirs_names="$(grep -oE 'android:name="[^"]*"' "$theirs" 2>/dev/null | sort -u)"
  # Extract each uses-permission/uses-feature ELEMENT (robust to one-per-line AND
  # multiple-per-line manifests); add the ones whose android:name theirs lacks.
  add="$(grep -oE '<uses-(permission|feature)[^>]*/?>' "$ours" 2>/dev/null | while IFS= read -r el; do
        nm="$(printf '%s' "$el" | grep -oE 'android:name="[^"]*"' | head -1)"
        [ -n "$nm" ] || continue
        printf '%s\n' "$theirs_names" | grep -qxF "$nm" || printf '    %s\n' "$el"
      done)"
  if [ -z "$add" ]; then cp "$theirs" "$out"; return 0; fi
  local addf; addf="$(mktemp)"; printf '%s\n' "$add" > "$addf"
  tmp="$(mktemp)"
  # Read the injected lines from a file (awk -v cannot carry newlines).
  awk -v addf="$addf" '
    function dumpadd(   l) { while ((getline l < addf) > 0) print l; close(addf) }
    /<application/ && !ins { dumpadd(); ins=1 }
    /<\/manifest>/ && !ins { dumpadd(); ins=1 }
    { print }
  ' "$theirs" > "$tmp"
  mv "$tmp" "$out"; rm -f "$addf"
  return 0
}

# ── strings-union (localized composeResources strings.xml) ───────────────────
# Declared on 4 contract rules since the merge class was introduced, and until now it had NO
# implementation: `cs_merge` had no branch for it, so it fell through to `cs_merge_3way` — a plain
# `git merge-file`. The contract promised a union and the engine performed a line merge.
#
# That gap is invisible while a fork and the template share an ancestor for the file. It stops being
# invisible the moment they do not: locale files are typically ADD/ADD (a fork seeds its own via
# /idea-locale while the template ships its own), and with no base a line merge conflicts on the
# WHOLE FILE. Measured on a real mbs/cappy full sync against merged dev: 90 of 98 conflicts were
# strings-union, every one a whole-file marker starting at line 2.
#
# A resource file is a KEYED SET, not prose — `<string name="x">` entries are addressed by name and
# their order carries no meaning, so a union is both possible and obviously right:
#
#   key only in theirs        → take it (the template added a string; the fork needs it)
#   key only in ours          → keep it (the fork's own string, or its translation)
#   key in both, same         → one copy
#   key in both, different    → THE FORK WINS
#
# The fork winning is the whole point on this surface. The template ships SOURCE text — `app_name`
# is "Money Toolkit" upstream and "Cappy" downstream — so taking the template's value would rename
# the fork's app on every sync and silently discard real translations. A fork that wants the
# template's new wording deletes its own key.
#
# Entry-level, not line-level: `<string-array>` and `<plurals>` span lines, so each element is read
# from its opening tag to its matching close and carried whole.
cs_merge_strings() {
  local ours="$1" base="$2" theirs="$3" out="${4:-$1}"
  [ -f "$ours" ] || { [ -f "$theirs" ] && cp "$theirs" "$out"; return 0; }
  [ -f "$theirs" ] || return 0

  local tmp; tmp="$(mktemp)"
  awk -v oursf="$ours" '
    # Emit "name\x01<entry text>" for every top-level resource element in FILE.
    function slurp(file, arr,    line, name, buf, depth, tag) {
      while ((getline line < file) > 0) {
        if (line ~ /<(string|string-array|plurals|bool|integer|color|dimen)[ >]/) {
          name = line; sub(/.*name="/, "", name); sub(/".*/, "", name)
          buf = line
          # self-closed or closed on the same line → done
          if (line ~ /\/>/ || line ~ /<\/(string|string-array|plurals|bool|integer|color|dimen)>/) {
            if (!(name in arr)) order[++n_order] = name
            arr[name] = buf; continue
          }
          depth = 1
          while (depth > 0 && (getline line < file) > 0) {
            buf = buf "\n" line
            if (line ~ /<\/(string-array|plurals)>/) depth--
            else if (line ~ /<(string-array|plurals)[ >]/) depth++
            else if (line ~ /<\/string>/) depth--
          }
          if (!(name in arr)) order[++n_order] = name
          arr[name] = buf
        }
      }
      close(file)
    }
    BEGIN { slurp(oursf, ourset) }
    # Walk THEIRS, replacing any entry the fork also defines with the fork version.
    {
      if ($0 ~ /<(string|string-array|plurals|bool|integer|color|dimen)[ >]/) {
        nm = $0; sub(/.*name="/, "", nm); sub(/".*/, "", nm)
        # consume the whole element from theirs
        buf = $0
        if (!($0 ~ /\/>/ || $0 ~ /<\/(string|string-array|plurals|bool|integer|color|dimen)>/)) {
          d = 1
          while (d > 0 && (getline nxt) > 0) {
            buf = buf "\n" nxt
            if (nxt ~ /<\/(string-array|plurals)>/) d--
            else if (nxt ~ /<(string-array|plurals)[ >]/) d++
            else if (nxt ~ /<\/string>/) d--
          }
        }
        if (nm in ourset) { print ourset[nm]; seen[nm] = 1 }
        else              { print buf }
        next
      }
      # Before the closing </resources>, append every fork-only entry in the order the fork wrote them.
      # Iterating the array itself is hash order: each sync re-shuffled the fork strings, so a file
      # nobody edited changed on every run.
      if ($0 ~ /<\/resources>/) {
        for (i = 1; i <= n_order; i++) if (!(order[i] in seen)) print ourset[order[i]]
      }
      print
    }
  ' "$theirs" > "$tmp"

  # Never ship an empty or truncated resource file: if the union lost the root element, keep ours.
  if ! grep -q "</resources>" "$tmp"; then
    rm -f "$tmp"
    return 1
  fi
  mv "$tmp" "$out"
  return 0
}

# ── yaml-schema-merge (app-profile deep-merge) ───────────────────────────────
# A fork scalar still carrying a placeholder (a `# PLACEHOLDER` marker or a template-default value) is
# not the fork's choice, so the template's value replaces it even without a base.
CS_PLACEHOLDER_RE='(^|[[:space:]])#[[:space:]]*PLACEHOLDER|com\.example\.app|App Toolkit|https?://(demo|api|staging)\.example\.com|YOUR_|Your Organization|you@example\.com'

# yaml-schema-merge — TRUE 3-way merge of an app-profile YAML, built on the FORK's file.
#
# The fork's text is the output's base, so everything the fork wrote survives byte-for-byte: its values
# (multi-line `|`/`>` blocks included), its comments, and keys the template does not have. The
# template contributes only what CHANGED on its side since the last sync (`base`, the template at
# .template-version#template_sha):
#
# WHO OWNS A KEY comes from the template's schema for the file, `<file>.schema.yaml` (e.g.
# app-profile/app.schema.yaml), passed as $CS_YAML_SCHEMA. Its `keys:` map dotted paths to an owner;
# the longest matching prefix wins, and `default:` covers the rest:
#   template        the template's value always wins; a fork that deleted or edited it gets it back
#   fork            the fork's value stays (rules below); the template only supplies what is missing
#   union:<field>   a list merged by row identity (`union:name|fn` for several identity fields)
# No schema → every key is `fork`, and the five built-in union lists below still union.
#
# Rules for a `fork` key:
#   new key         template has it, base does not      → inserted after its nearest sibling, with the
#                                                           comment lines directly above it
#   deleted by fork base has it, fork does not           → stays deleted
#   new default     fork == base, template != base      → the template's value replaces the fork's
#   fork customized fork != base                         → the fork's value stays
# Any owner:
#   retired         base has it (the template shipped it), the template dropped it
#                                                         → removed from the fork, even when the fork
#                                                           changed its value — the template owned the
#                                                           KEY. Each removal is reported on stderr with
#                                                           the fork's value, so it is never silent.
#   fork extension  neither base nor template has it    → left alone; it is the fork's own key
#   union list      network.access_points/id · core_store.stores/id · core_store.packages/id ·
#                   core_store.cache_keys/name|fn · database.packages/id (built in), plus any
#                   `union:` the schema declares       → the fork's rows stay; a template row is
#                                                           appended when its identity is new (in
#                                                           neither the fork nor the base); a row the
#                                                           template marks `owner: template` replaces the
#                                                           fork's copy of that row
# Values compare as whole subtrees with comments and trailing whitespace removed, so a block scalar or
# a list is one value. An empty base (never synced, or a file added on both sides) means no change can
# be attributed to the template: the fork's values all stay, template keys the fork lacks are added,
# and placeholder values follow the template.
#
# This replaced an overlay that printed the TEMPLATE's file and copied back only the fork's one-line
# scalars. Measured on a fork sync 2026-11-01: it handed the fork the template's demo store description,
# release notes and review notes (all `|-`/`>-` blocks), emptied the fork's `org.links` map (the
# template ships `links: {}`), dropped `security.signed_in_by_default: false` (a key the template only
# documents) and every fork comment, all while reporting a clean merge.
#   cs_merge_yaml_schema <ours=fork> <base> <theirs=template> [<out>]
#   returns 0 merged-clean · 2 error
cs_merge_yaml_schema() {
  local ours="$1" base="$2" theirs="$3" out="${4:-$1}"
  [ -f "$ours" ] && [ -f "$theirs" ] || {
    echo "cs_merge_yaml_schema: need <ours=fork> <base> <theirs=template>" >&2; return 2; }
  local basef="$base"; [ -s "$base" ] || basef=/dev/null
  local tmp; tmp="$(mktemp)"
  local schemaf="${CS_YAML_SCHEMA:-}"; [ -s "$schemaf" ] || schemaf=/dev/null
  awk -v ph="$CS_PLACEHOLDER_RE" -v schemaf="$schemaf" -v hasbase="$([ -s "$base" ] && echo 1 || echo 0)" '
    function lead(s,   n){ n=0; while (substr(s,n+1,1)==" ") n++; return n }
    function keyof(s,   t){ t=s; sub(/^[ ]+/,"",t); sub(/[ ]*:.*/,"",t); return t }
    function restof(s,   t){ t=s; sub(/^[ ]*[^:]+:/,"",t); sub(/^[ ]+/,"",t); return stripc(t) }
    function stripc(v,   t){ t=v; sub(/[ \t]+#.*$/,"",t); sub(/[ \t]+$/,"",t); return t }
    function spaces(n,   s){ s=""; while (n-->0) s=s" "; return s }
    function noise(s){ return (s ~ /^[ ]*#/ || s ~ /^[ ]*$/) }
    function parentof(p,   q){ q=p; if (q !~ /\./) return ""; sub(/\.[^.]*$/,"",q); return q }
    # Owner of a dotted path from the schema: the longest declared prefix, else `default:`, else fork.
    function owner(p,   q){ q=p; while (q!="") { if (q in OWN) return OWN[q]; q=parentof(q) } return (DEF!="") ? DEF : "fork" }
    function union_spec(p,   o){
      o=owner(p); if (o ~ /^union:/) return substr(o,7)
      if (p=="network.access_points") return "id"
      if (p=="core_store.stores")     return "id"
      if (p=="core_store.packages")   return "id"
      if (p=="core_store.cache_keys") return "name|fn"
      if (p=="database.packages")     return "id"
      return ""
    }
    # Row identity for block rows (`- id: main`) and inline maps (`- { id: x, owner: y }`).
    function rowid(c, spec,   n, arr, i, f, v){
      n=split(spec, arr, "|")
      for (i=1;i<=n;i++) {
        f=arr[i]
        if (match(c, "[{,][ ]*" f "[ ]*:[ ]*[^,}]+")) { v=substr(c,RSTART,RLENGTH); sub("^[{,][ ]*" f "[ ]*:[ ]*","",v); return stripc(v) }
        if (match(c, "^-?[ ]*" f "[ ]*:")) { v=c; sub("^-?[ ]*" f "[ ]*:[ ]*","",v); return stripc(v) }
      }
      return ""
    }
    # ── parse: every mapping key → its line range, indent, inline value and child order ──
    # Lists and block scalars are opaque (one value); keys inside list rows are not paths.
    {
      f = (FILENAME==ARGV[1]) ? "o" : (FILENAME==ARGV[2]) ? "b" : "t"
      if (FNR==1) { sp=0; opq=-1 }
      n[f]=FNR; L[f,FNR]=$0
      if (noise($0)) next
      ind=lead($0); c=$0; sub(/^[ ]+/,"",c)
      if (opq>=0 && (ind>opq || (ind==opq && c ~ /^- /))) { for (i=1;i<=sp;i++) E[f,sk[i]]=FNR; next }
      opq=-1
      if (c ~ /^- / || c ~ /^-$/) {                  # a list directly under the key above
        if (sp>0) { K[f,sk[sp]]="list"; opq=si[sp]; for (i=1;i<=sp;i++) E[f,sk[i]]=FNR }
        next
      }
      if (c !~ /:/) { for (i=1;i<=sp;i++) E[f,sk[i]]=FNR; next }
      while (sp>0 && si[sp]>=ind) sp--
      k=keyof($0); p=(sp>0) ? sk[sp] "." k : k
      sp++; sk[sp]=p; si[sp]=ind
      S[f,p]=FNR; E[f,p]=FNR; I[f,p]=ind; R[f,p]=restof($0); has[f,p]=1
      K[f,p] = (R[f,p] ~ /^[|>]/) ? "block" : (R[f,p]=="" ? "map" : "inline")
      if (K[f,p]=="block") opq=ind
      for (i=1;i<sp;i++) E[f,sk[i]]=FNR
      par=(sp>1) ? sk[sp-1] : ""; nc[f,par]++; ch[f,par,nc[f,par]]=p
      if (f=="t") { nt++; tord[nt]=p }
      if (f=="o") { no++; oord[no]=p }
    }
    BEGIN {
      while ((getline sl < schemaf) > 0) {
        if (sl ~ /^[ ]*#/ || sl ~ /^[ ]*$/) continue
        if (sl ~ /^keys:/) { inkeys=1; continue }
        if (sl ~ /^default:/) { DEF=sl; sub(/^default:[ ]*/,"",DEF); sub(/[ ]+#.*$/,"",DEF); inkeys=0; continue }
        if (sl !~ /^ /) { inkeys=0; continue }
        if (inkeys) { k=sl; sub(/^[ ]+/,"",k); v=k; sub(/[ ]*:.*/,"",k); sub(/^[^:]*:[ ]*/,"",v); sub(/[ ]+#.*$/,"",v); OWN[k]=v }
      }
    }
    # A subtree with comments, blank lines and trailing whitespace removed, relative to its own indent.
    function norm(f,p,   j,s,t){
      s=""; for (j=S[f,p]; j<=E[f,p]; j++) { if (noise(L[f,j])) continue; t=stripc(L[f,j]); s=s substr(t, I[f,p]+1) "\n" }
      return s
    }
    # The comment lines directly above a key (no blank line between) — they document it.
    function cstart(f,p,   j){ j=S[f,p]; while (j>1 && L[f,j-1] ~ /^[ ]*#/) j--; return j }
    function emit_range(f,a,z,delta,   j,s){
      s=""; for (j=a;j<=z;j++) {
        if (L[f,j] ~ /^[ ]*$/) { s=s "\n"; continue }
        if (delta>=0) s=s spaces(delta) L[f,j] "\n"; else s=s substr(L[f,j], 1-delta) "\n"
      }
      return s
    }
    function under_done(p,   q){ q=p; while (q!="") { if (q in done) return 1; q=parentof(q) } return 0 }
    END {
      # 1. keys the template retired (first, so no insertion anchors on one)
      if (hasbase) for (x=1;x<=no;x++) {
        p=oord[x]; par=parentof(p)
        if (has["t",p] || !has["b",p]) continue
        if (par!="" && (!has["t",par] || K["t",par]!="map")) continue
        if (norm("o",p)!=norm("b",p)) { v=L["o",S["o",p]]; sub(/^[ ]+/,"",v); printf "cs_merge_yaml_schema: retired key %s removed (fork had: %s)\n", p, v > "/dev/stderr" }
        rep[cstart("o",p)]=E["o",p]; repl[cstart("o",p)]=""; del[p]=1
      }
      # 2. template paths, top-down
      for (x=1;x<=nt;x++) {
        p=tord[x]; if (under_done(p)) continue
        par=parentof(p)
        if (!has["o",p]) {
          if (par!="" && !has["o",par]) continue
          if (hasbase && has["b",p] && owner(p)!="template") { done[p]=1; continue }   # the fork deleted it
          # anchor: the nearest preceding template sibling the fork has
          anchor=0
          for (i=1;i<=nc["t",par];i++) { q=ch["t",par,i]; if (q==p) break; if (has["o",q] && !(q in del)) anchor=E["o",q] }
          if (anchor==0) {
            if (par!="") anchor=S["o",par]
            else anchor=(no>0) ? cstart("o", oord[1])-1 : n["o"]
          }
          newind = (par!="") ? I["o",par] + (I["t",p]-I["t",par]) : I["t",p]
          ins[anchor]=ins[anchor] emit_range("t", cstart("t",p), E["t",p], newind-I["t",p])
          done[p]=1; continue
        }
        spec=union_spec(p)
        if (spec!="" && K["o",p]=="list" && K["t",p]=="list") {     # union by identity
          rind=-1
          # index the fork rows: identity → line range, so a template-owned row can be replaced whole
          cur=""; frs=0; lastj=0
          for (j=S["o",p]+1;j<=E["o",p];j++) {
            c=L["o",j]; if (noise(c)) continue; ci=lead(c); c2=c; sub(/^[ ]+/,"",c2)
            if (c2 ~ /^- / && (rind<0 || ci==rind)) { if (cur!="") { fs[p,cur]=frs; fe[p,cur]=lastj } rind=ci; frs=j; cur="" }
            v=rowid(c2,spec); if (v!="") { fid[p,v]=1; if (cur=="") cur=v }
            lastj=j
          }
          if (cur!="") { fs[p,cur]=frs; fe[p,cur]=lastj }
          if (hasbase && has["b",p]) for (j=S["b",p]+1;j<=E["b",p];j++) { c=L["b",j]; if (noise(c)) continue; c2=c; sub(/^[ ]+/,"",c2); v=rowid(c2,spec); if (v!="") bid[p,v]=1 }
          j=S["t",p]+1
          while (j<=E["t",p]) {
            c=L["t",j]; if (noise(c)) { j++; continue }
            c2=c; sub(/^[ ]+/,"",c2)
            if (c2 !~ /^- /) { j++; continue }
            rs=j; tri=lead(c); id=""; j++
            v=rowid(c2,spec); if (v!="") id=v
            while (j<=E["t",p]) { c=L["t",j]; if (!noise(c)) { c3=c; sub(/^[ ]+/,"",c3); if (lead(c)<=tri) break; v=rowid(c3,spec); if (id=="" && v!="") id=v } j++ }
            # A row the template OWNS (`owner: template`) follows the template, like a template-owned key.
            tow=0; for (y=rs;y<j;y++) if (L["t",y] ~ /(^|[{,][ ]*|^[ ]*-?[ ]*)owner:[ ]*template([ ,}]|$)/) tow=1
            if (tow && id!="" && ((p,id) in fs)) {
              rep[fs[p,id]]=fe[p,id]; repl[fs[p,id]]=emit_range("t", rs, j-1, rind-tri); continue
            }
            if (id!="" && !((p,id) in fid) && !((p,id) in bid))
              ins[E["o",p]]=ins[E["o",p]] emit_range("t", rs, j-1, (rind<0 ? I["o",p]+2 : rind)-tri)
          }
          done[p]=1; continue
        }
        if (owner(p)=="template") {                                   # template-owned: the template value, always
          if (norm("o",p)!=norm("t",p)) { rep[S["o",p]]=E["o",p]; repl[S["o",p]]=emit_range("t", S["t",p], E["t",p], I["o",p]-I["t",p]) }
          done[p]=1; continue
        }
        if (K["o",p]=="map" && K["t",p]=="map") continue              # descend: children decide
        no_=norm("o",p); nt_=norm("t",p)
        take=0
        if (hasbase && has["b",p]) { nb_=norm("b",p); if (no_==nb_ && nt_!=nb_) take=1 }
        else if (K["o",p]=="inline" && L["o",S["o",p]] ~ ph && no_!=nt_) take=1
        if (take) { rep[S["o",p]]=E["o",p]; repl[S["o",p]]=emit_range("t", S["t",p], E["t",p], I["o",p]-I["t",p]) }
        done[p]=1
      }
      # 3. emit the fork, applying replacements and insertions
      if (0 in ins) printf "%s", ins[0]
      j=1
      while (j<=n["o"]) {
        if (j in rep) { printf "%s", repl[j]; z=rep[j]; for (y=j; y<=z; y++) if (y in ins) printf "%s", ins[y]; j=z+1; continue }
        print L["o",j]
        if (j in ins) printf "%s", ins[j]
        j++
      }
    }
  ' "$ours" "$basef" "$theirs" > "$tmp" || { rm -f "$tmp"; echo "cs_merge_yaml_schema: merge failed" >&2; return 2; }
  mv "$tmp" "$out"
  return 0
}

# Semantic settings.gradle.kts merge — the `include-union` strategy. A textual 3-way unions the
# fork's + template's `include(":module")` lines, but the template also declares its OWN demo/app
# feature modules (feature/showcase, feature/loans, feature/rates, …) that a downstream fork does
# NOT have and that the sync does NOT copy in (they are not template-shared modules). The blind
# union therefore references non-existent module dirs and breaks Gradle configuration
# ("Configuring project ':feature:showcase' without an existing directory"). After the union, DROP
# every `include(":a:b")` whose resolved dir `a/b/` is absent on disk — keeping the fork's real
# modules + the shared modules the sync actually materialized, never a template-only phantom.
#   cs_merge_include_union <ours> <base> <theirs> [<out>]
cs_merge_include_union() {
  local ours="$1" base="$2" theirs="$3" out="${4:-$1}" rc
  cs_merge_3way "$ours" "$base" "$theirs" "$out"; rc=$?
  local root; root="$(cd "$(dirname "$out")" 2>/dev/null && pwd)"; [ -n "$root" ] || root="$(pwd)"
  local tmp coord; tmp="$(mktemp)"
  while IFS= read -r line || [ -n "$line" ]; do
    coord="$(printf '%s' "$line" | sed -nE 's/^[[:space:]]*include\("?:([A-Za-z0-9:_.-]+)"?\).*/\1/p')"
    if [ -n "$coord" ] && [ ! -d "$root/${coord//://}" ]; then
      continue   # drop a phantom include — module dir does not exist in this fork
    fi
    printf '%s\n' "$line"
  done < "$out" > "$tmp"
  mv "$tmp" "$out"
  return "$rc"
}

# Strategy dispatcher used by scripts/white-label/sync-dirs.sh for a `merge`-owned file.
#   cs_merge <strategy> <ours> <base> <theirs> [<out>]
# cs_merge_properties <ours> <base> <theirs> [<out>] — KEY-LEVEL 3-way for flat `key=value` files
# (gradle.properties, *.properties). A line-based diff3 spuriously conflicts when a fork-changed line
# sits next to a template-changed line even though the KEYS are independent; this merges per key:
#   emit THEIRS (template) as structure/order/comments; per key — fork changed it (OURS != BASE, or no
#   base) → keep the FORK's value; else follow the template (its default / its change / its new key).
#   fork-only keys (absent from the template) are appended (preserved); template-removed keys the fork
#   left untouched drop out (not re-added). Base absent → fork's keys overlay the template (2-way).
cs_merge_properties() {
  local ours="$1" base="$2" theirs="$3" out="${4:-$1}"
  [ -f "$ours" ] && [ -f "$theirs" ] || { echo "cs_merge_properties: need <ours> <base> <theirs>" >&2; return 2; }
  local tmp; tmp="$(mktemp)"
  awk -v ourf="$ours" -v basef="$base" '
    function kof(s,   t){ t=s; sub(/[ \t]*=.*/,"",t); gsub(/^[ \t]+|[ \t]+$/,"",t); return t }
    function vof(s,   t){ t=s; sub(/^[^=]*=/,"",t); return t }
    BEGIN{
      while((getline l < basef)>0){ if(l ~ /^[ \t]*[#!]|^[ \t]*$/) continue; k=kof(l); if(k!=""){ bv[k]=vof(l); bseen[k]=1 } }
      while((getline l < ourf)>0){  if(l ~ /^[ \t]*[#!]|^[ \t]*$/) continue; k=kof(l); if(k!=""){ ov[k]=vof(l); oseen[k]=1 } }
    }
    /^[ \t]*[#!]|^[ \t]*$/ { print; next }        # template comments/blanks preserved (structure)
    {
      k=kof($0); if(k==""){ print; next }
      tseen[k]=1
      if(k in oseen){
        if( (!(k in bseen)) || ov[k]!=bv[k] ){ print k "=" ov[k]; next }   # fork changed it → keep fork
      }
      print                                       # fork unchanged / fork-absent → template value
    }
    END{ for(k in oseen) if(!(k in tseen)) print k "=" ov[k] }             # fork-only keys preserved
  ' "$theirs" > "$tmp" && mv "$tmp" "$out"
  return 0
}

# Merge one `merge`-owned file by its contract strategy (customization-surface.yaml#strategy); an
# undeclared strategy falls back to the line-based 3-way.
#   cs_merge <strategy> <ours> <base> <theirs> [<out>]
cs_merge() {
  local strat="$1" ours="$2" base="$3" theirs="$4" out="${5:-$2}"
  case "$strat" in
    manifest-union)    cs_merge_manifest      "$ours" "$base" "$theirs" "$out" ;;
    include-union)     cs_merge_include_union "$ours" "$base" "$theirs" "$out" ;;
    yaml-schema-merge) cs_merge_yaml_schema   "$ours" "$base" "$theirs" "$out" ;;
    properties-3way)   cs_merge_properties    "$ours" "$base" "$theirs" "$out" ;;
    strings-union)     cs_merge_strings       "$ours" "$base" "$theirs" "$out" ;;
    *)                 cs_merge_3way          "$ours" "$base" "$theirs" "$out" ;;
  esac
}

# ── CLI ──
# white-label-template-completion E0/T1 — the closed set of ownership rows the boundary flip depends on.
# Asserts each via cs_resolve_owner (no brittle count literal). Returns 0 iff all hold.
cs_require_flip_preconditions() {
  local bad=0
  # Flag one ownership row that does not resolve to the owner the boundary flip needs.
  _cs_expect() { # <path> <expected-owner>
    local got; got="$(cs_resolve_owner "$1")"
    [ "$got" = "$2" ] || { echo "❌ flip-precondition: $1 owner=$got, expected $2"; bad=1; }
  }
  # og-images are DERIVED store media (glob `deployment/**/og-images/** → generated`): regenerated from
  # app-profile, never hand-edited, so sync IGNORES them. Assert `generated` — this both reflects the
  # reclassification AND proves the generated-class row is present in the contract.
  _cs_expect "deployment/web/og-images/01_home.png"                generated
  _cs_expect "gradle.properties"                                   merge   # mixed fork-values + template-infra → key-level properties-3way (2026-08-20)
  _cs_expect "secrets-manifest.yaml"                               generated
  _cs_expect "secrets/live/keystore.jks"                           fork
  _cs_expect "tests/anything.sh"                                   template
  # AppStoreRegistry.kt is GONE — stores are declared with @StoreProvider, and the generated
  # AppStoreRegistry/AppCacheKeys/GeneratedStoreBindings are KSP build artifacts under
  # build/generated, so there is no committed file to own.
  # The surviving core/store fork seams are these two.
  # core/model's three-way split. `invoice/` stands for ANY undeclared package — a fork's own model,
  # which must resolve fork. Before the module catch-all existed it resolved `template` off the
  # `core/**` blanket, and a sync would have believed it could overwrite it.
  _cs_expect "core/model/src/commonMain/kotlin/kpt/core/model/invoice/Invoice.kt" fork
  _cs_expect "core/model/src/commonMain/kotlin/kpt/core/model/user/UserData.kt" template
  _cs_expect "core/model/src/commonMain/kotlin/kpt/core/model/banking/Loan.kt" demo-showcase
  _cs_expect "core/store/src/commonMain/kotlin/kpt/core/store/config/ForkErrorMapper.kt" fork
  _cs_expect "core/store/src/commonMain/kotlin/kpt/core/store/config/ForkScreenStateDefaults.kt" fork
  # REAL paths, not short synthetic ones. These two asserted `template` only because the short form
  # missed every `**/kpt/core/store/**` rule and fell through to the `core/**` blanket — the fixture
  # was testing a path that does not exist. The real files are showcase stores, so demo-showcase.
  _cs_expect "core/store/src/commonMain/kotlin/kpt/core/store/economic/impl/ExchangeRatesStore.kt" demo-showcase
  _cs_expect "core/store/src/commonMain/kotlin/kpt/core/store/economic/impl/InterestRateSeriesStore.kt" demo-showcase
  # The module catch-all: a fork's own store package must not be claimed by the core/** blanket.
  _cs_expect "core/store/src/commonMain/kotlin/kpt/core/store/invoice/impl/InvoiceStore.kt" fork
  _cs_expect "core/store/src/commonMain/kotlin/kpt/core/store/prefs/impl/UserDataStore.kt" template
  [ "$bad" -eq 0 ] && echo "✅ T1 flip preconditions hold (og-images generated · secrets-manifest/keystore + core/store Fork* seams fork · core/model undeclared=fork · gradle.properties merge/properties-3way · tests/core-store-impl template)"
  return "$bad"
}

# CLI entry point: dispatch --verify / resolve / merge / self-test.
cs_main() {
  local cmd="${1:-}"; shift || true
  case "$cmd" in
    --verify|verify-zero-match)
      # E0/T2: zero-match-rule guard — exit 1 if ANY enforced glob matches zero real paths.
      cs_verify_zero_match
      ;;
    resolve)
      local p="${1:?usage: resolve <path>}"
      cs_match_g "$p"
      printf '%s\t%s%s\n' "$p" "$CS_M_OWNER" "${CS_M_STRAT:+  (strategy: $CS_M_STRAT)}"
      ;;
    resolve-owner)
      # Print ONLY the resolved owner token (template | fork | merge | generated |
      # demo-showcase) with no path/strategy decoration — the machine-readable twin of
      # `resolve`, so a gate can `[ "$(… resolve-owner <p>)" = demo-showcase ]` without
      # parsing the tab-delimited `resolve` line. (WS01/AC8 — the distinct demo-showcase
      # ownership class is a first-class owner value the contract now emits.)
      local p="${1:?usage: resolve-owner <path>}"
      cs_match_g "$p"
      printf '%s\n' "$CS_M_OWNER"
      ;;
    report)
      # Owner->count tally as index-matched parallel arrays; bash 3.2 (macOS /bin/bash) has no
      # associative arrays, and would silently collapse every owner onto index 0.
      local f; local _owners=(); local _counts=()
      while IFS= read -r f; do
        cs_match_g "$f"
        local _oi=0; local _found=0
        while [ "$_oi" -lt "${#_owners[@]}" ]; do
          if [ "${_owners[$_oi]}" = "$CS_M_OWNER" ]; then
            _counts[$_oi]=$(( ${_counts[$_oi]} + 1 )); _found=1; break
          fi
          _oi=$(( _oi + 1 ))
        done
        [ "$_found" = "0" ] && { _owners+=("$CS_M_OWNER"); _counts+=(1); }
        printf '%s\t%s\n' "$CS_M_OWNER" "$f"
      done < <(git -C "$CS_ROOT" ls-files) | sort
      local _ki=0
      while [ "$_ki" -lt "${#_owners[@]}" ]; do
        printf '# %-9s %d files\n' "${_owners[$_ki]}" "${_counts[$_ki]}" >&2; _ki=$(( _ki + 1 ))
      done
      ;;
    verify)
      local unclassified=0 total=0 f
      while IFS= read -r f; do
        total=$((total+1))
        cs_match_g "$f"
        if [ "$CS_M_DEFAULT" = "1" ]; then
          echo "UNCLASSIFIED (matches only the default rule): $f"
          unclassified=$((unclassified+1))
        fi
      done < <(git -C "$CS_ROOT" ls-files)
      echo "── customization-surface coverage: $((total-unclassified))/$total classified ──"
      if [ "$unclassified" -gt 0 ]; then
        echo "❌ $unclassified path(s) unclassified — add an explicit rule to customization-surface.yaml"
        return 1
      fi
      # white-label-template-completion E0/T1 flip-precondition assertions (closed-set, not a count literal)
      if ! cs_require_flip_preconditions; then return 1; fi
      echo "✅ every tracked path has an explicit owner + T1 flip preconditions hold"
      ;;
    require-flip-preconditions)
      # E0/T1 (FIX-01-R2-ATOMIC): exit 0 iff every T1 ownership row is present + correct — the atomic
      # self-guard T3's scripts/white-label/sync-dirs.sh flip calls before preserving, so a consumer that pulled the
      # mechanism flip WITHOUT the ownership fix HALTs (no clobber).
      cs_require_flip_preconditions
      ;;
    list-template-owners)
      # WS4/T7 (pure-white-label-100 phase 5): print every glob whose owner is `template`,
      # one per line, in contract order. The single SoT the sync-reachability gate
      # (framework-verify-sync-reachability.sh) consumes so it never hand-rolls YAML parsing.
      cs_load_rules
      local i
      for i in "${!CS_GLOB[@]}"; do
        [ "${CS_OWNER[$i]}" = "template" ] && printf '%s\n' "${CS_GLOB[$i]}"
      done
      ;;
    merge)
      cs_merge "$@"   # <strategy> <ours> <base> <theirs> [<out>]
      ;;
    *)
      echo "usage: customization-surface.sh {resolve <path>|resolve-owner <path>|report|verify|--verify|list-template-owners|merge <strategy> <ours> <base> <theirs> [out]}" >&2
      return 2
      ;;
  esac
}

# Run CLI only when executed directly (not when sourced as a library).
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
  set -uo pipefail
  cs_main "$@"
fi
