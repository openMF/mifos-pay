# scripts/docs/scanners/ruby.awk — Fastlane lanes + Ruby methods.
#
# Fastlane has its OWN doc mechanism: `desc "…"` immediately above a lane. `fastlane list` renders it,
# so a documented lane is already self-describing at the CLI. This extracts that, plus a `#` block above
# a plain `def`, so the deployment layer is measured the same way as everything else.
#
# Emits: path <TAB> line <TAB> public <TAB> symbol_kind <TAB> name <TAB> doc_state <TAB> doc_words
#   symbol_kind: file | lane | private_lane | method
FNR == 1 { hdr = 0; hdr_words = 0; seen_code = 0; pend = 0; pend_words = 0; desc_words = 0 }

{
  raw = $0; stripped = raw; sub(/^[[:space:]]+/, "", stripped)
  if (stripped ~ /^#/) {
    body = stripped; sub(/^#+[[:space:]]*/, "", body)
    if (body != "") {
      pend = 1; pend_words += split(body, _w, /[[:space:]]+/)
      if (!seen_code) { hdr = 1; hdr_words += split(body, _h, /[[:space:]]+/) }
    }
    next
  }
  if (stripped == "") next

  # `desc "…"` is fastlane's native lane documentation — treat it exactly like a doc comment.
  if (stripped ~ /^desc[[:space:]]+["']/) {
    body = stripped; sub(/^desc[[:space:]]+["']/, "", body); sub(/["'][[:space:]]*$/, "", body)
    pend = 1; desc_words = split(body, _d, /[[:space:]]+/); pend_words += desc_words
    next
  }

  if (match(stripped, /^(private_lane|lane)[[:space:]]+:[A-Za-z_][A-Za-z0-9_]*/)) {
    seg = substr(stripped, RSTART, RLENGTH)
    kindname = seg; sub(/[[:space:]].*$/, "", kindname)
    nm = seg; sub(/^[a-z_]+[[:space:]]+:/, "", nm)
    printf "%s\t%d\tpublic\t%s\t%s\t%s\t%d\n", FILENAME, FNR, kindname, nm, (pend ? "doc" : "none"), pend_words
  } else if (match(stripped, /^def[[:space:]]+[A-Za-z_][A-Za-z0-9_?!.]*/)) {
    nm = substr(stripped, RSTART, RLENGTH); sub(/^def[[:space:]]+/, "", nm)
    printf "%s\t%d\tpublic\tmethod\t%s\t%s\t%d\n", FILENAME, FNR, nm, (pend ? "doc" : "none"), pend_words
  }
  seen_code = 1; pend = 0; pend_words = 0
}
END { if (FILENAME != "") printf "%s\t1\tpublic\tfile\t%s\t%s\t%d\n", FILENAME, FILENAME, (hdr ? "doc" : "none"), hdr_words }
