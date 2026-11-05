#!/usr/bin/env bash
# scripts/docs/scanners/workflow.sh — GitHub Actions workflows and their dispatch inputs.
#
# Actions has a native doc surface and the GitHub UI renders it: a workflow's `name:`, and a
# `description:` on every `workflow_dispatch` input. An input without one shows as a bare field in the
# Run-workflow dialog, so the gap is user-visible, not cosmetic.
#
# POSIX character classes ONLY — `\S`/`\s`/`\d` are GNU extensions. macOS awk treats `\S` as a
# literal `S`, so a `\S` guard silently never matches there while gawk in CI matches: the two
# disagree with no error. This cost 30 falsely-undocumented workflow inputs.
#
# Pure grep/awk on purpose — no YAML parser in the template's toolchain, and the two keys this needs are
# unambiguous at fixed indentation depths inside `on.workflow_dispatch.inputs`.
#
# Emits: path <TAB> line <TAB> public <TAB> symbol_kind <TAB> name <TAB> doc_state <TAB> doc_words
set -uo pipefail
for f in "$@"; do
  # file-level: `name:` at column 0
  if n=$(grep -nE '^name:[[:space:]]*[^[:space:]]' "$f" | head -1 | cut -d: -f1); then :; else n=""; fi
  if [ -n "$n" ]; then
    w=$(sed -n "${n}p" "$f" | sed 's/^name:[[:space:]]*//' | wc -w | tr -d ' ')
    printf '%s\t%s\tpublic\tfile\t%s\tdoc\t%s\n' "$f" "$n" "${f##*/}" "$w"
  else
    printf '%s\t1\tpublic\tfile\t%s\tnone\t0\n' "$f" "${f##*/}"
  fi
  # inputs: an `inputs:` block, then each key one indent deeper, then its `description:`
  awk -v F="$f" '
    /^[[:space:]]*inputs:[[:space:]]*$/ { in_inputs=1; ind=match($0,/[^ ]/); next }
    in_inputs {
      cur=match($0,/[^ ]/)
      if ($0 ~ /^[[:space:]]*$/) next
      if (cur <= ind) { in_inputs=0; if (name!="") { printf "%s\t%d\tpublic\tinput\t%s\t%s\t%d\n", F, ln, name, (d?"doc":"none"), dw; name="" } next }
      if ($0 ~ /^[[:space:]]*[A-Za-z_][A-Za-z0-9_-]*:[[:space:]]*$/ && cur == ind+2) {
        if (name != "") printf "%s\t%d\tpublic\tinput\t%s\t%s\t%d\n", F, ln, name, (d?"doc":"none"), dw
        name=$0; sub(/^[[:space:]]*/,"",name); sub(/:.*$/,"",name); ln=FNR; d=0; dw=0; next
      }
      if ($0 ~ /description:[[:space:]]*[^[:space:]]/) { d=1; t=$0; sub(/.*description:[[:space:]]*/,"",t); dw=split(t,_a,/[[:space:]]+/) }
    }
    END { if (name != "") printf "%s\t%d\tpublic\tinput\t%s\t%s\t%d\n", F, ln, name, (d?"doc":"none"), dw }
  ' "$f"
done
