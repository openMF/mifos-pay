# scripts/docs/scanners/yamlcfg.awk — top-level keys of a config schema and their `#` doc.
#
# `app-profile/app.yaml` and `deployment/DEPLOYMENT_MANIFEST.yaml` are SCHEMAS a fork edits by hand, so
# an undocumented key is a fork author guessing. Only TOP-LEVEL keys are measured: nesting in these files
# goes many levels deep and a per-leaf requirement would be noise, while the top-level block is the unit
# a person actually reads before editing.
#
# POSIX classes only — no `\S`.
#
# Emits: path <TAB> line <TAB> public <TAB> symbol_kind <TAB> name <TAB> doc_state <TAB> doc_words
FNR == 1 { pend = 0; pend_words = 0; hdr = 0; hdr_words = 0; seen_key = 0 }
{
  stripped = $0; sub(/^[[:space:]]+/, "", stripped)
  if (stripped ~ /^#/) {
    body = stripped; sub(/^#+[[:space:]]*/, "", body)
    if (body != "") {
      pend = 1; pend_words += split(body, _w, /[[:space:]]+/)
      if (!seen_key) { hdr = 1; hdr_words += split(body, _h, /[[:space:]]+/) }
    }
    next
  }
  if (stripped == "") next
  if ($0 ~ /^[A-Za-z_][A-Za-z0-9_]*:/) {
    nm = $0; sub(/:.*$/, "", nm)
    printf "%s\t%d\tpublic\tconfig-key\t%s\t%s\t%d\n", FILENAME, FNR, nm, (pend ? "doc" : "none"), pend_words
    seen_key = 1
  }
  pend = 0; pend_words = 0
}
END { if (FILENAME != "") printf "%s\t1\tpublic\tfile\t%s\t%s\t%d\n", FILENAME, FILENAME, (hdr ? "doc" : "none"), hdr_words }
