# scripts/docs/scanners/shell.awk — file header + function docs for bash.
#
# Shell has no KDoc, but this tree already has a convention and follows it: every one of the 114 tracked
# scripts opens with a `#` header block. So the job is to EXTRACT and GATE what is already written, not
# to invent a doc format.
#
# Same discipline as the Kotlin scanner: a `#` inside a single- or double-quoted string is not a comment,
# and `$(...)`/heredoc bodies are not code. Rather than parse bash, this tracks quote state per line and
# refuses to read a `#` that opens inside an unterminated quote.
#
# Emits: path <TAB> line <TAB> public <TAB> symbol_kind <TAB> name <TAB> doc_state <TAB> doc_words
#   symbol_kind: file | function
#   doc_state:   doc | none

function comment_body(raw,   i, c, n, q) {
  n = length(raw); q = ""
  for (i = 1; i <= n; i++) {
    c = substr(raw, i, 1)
    if (q != "") { if (c == q) q = ""; continue }
    if (c == "'" || c == "\"") { q = c; continue }
    if (c == "\\") { i++; continue }
    if (c == "#") { c = substr(raw, i + 1); sub(/^[[:space:]#!]+/, "", c); return c }
  }
  return ""
}

# Track single-quote spans ACROSS lines. This tree embeds awk programs as `awk '\''…'\''`, and their
# `function keyof(s, t){ … }` declarations are not shell functions. Reading inside them reported 13 awk
# helpers in customization-surface.sh alone as undocumented bash — the same "a string is not code"
# lesson the Kotlin scanner needed, in a different language.
function squote_delta(raw,   i, n, c, d) {
  SQ = sprintf("%c", 39)      # a single quote, built by code — writing it literally here is a quoting trap
  n = length(raw); d = 0
  for (i = 1; i <= n; i++) {
    c = substr(raw, i, 1)
    if (c == "\\") { i++; continue }
    if (c == SQ) d++
  }
  return d % 2
}

FNR == 1 {
  hdr_words = 0; hdr = 0; inprog = 0
  # The shebang is not documentation; the block under it is.
  getline probe
  if (probe ~ /^#!/) { } else { rewound = probe }
}

{
  raw = (FNR == 2 && rewound != "") ? rewound : $0
  if (FNR == 2 && rewound != "") rewound = ""
  was_in = inprog
  if (squote_delta(raw)) inprog = 1 - inprog
  if (was_in) next                        # inside an embedded program body — not shell

  body = comment_body(raw)
  stripped = raw; sub(/^[[:space:]]+/, "", stripped)

  # A COMMENT LINE is any line starting with `#`, including a bare `#` separator. Requiring a non-empty
  # body here made the blank `#` under the shebang read as CODE, which ended header detection on line 2
  # and reported 11 well-documented scripts as headerless.
  if (stripped ~ /^#/) {
    if (body != "") {
      pend_words += split(body, _w, /[[:space:]]+/)
      pend = 1
      if (!seen_code) { hdr_words += split(body, _h, /[[:space:]]+/); hdr = 1 }
    }
    next
  }
  if (stripped == "" ) { next }                         # a blank line keeps a pending block alive

  # `name() {` or `function name {` — the two forms this tree uses.
  if (match(stripped, /^([a-zA-Z_][a-zA-Z0-9_:-]*)[[:space:]]*\(\)[[:space:]]*\{/) ||
      match(stripped, /^function[[:space:]]+([a-zA-Z_][a-zA-Z0-9_:-]*)/)) {
    # bash's `function name` form declares NO parameters. `function keyof(s, t)` is awk, so a non-empty
    # parameter list means this is not a shell function at all.
    if (stripped ~ /^function[[:space:]]+[A-Za-z_][A-Za-z0-9_:-]*[[:space:]]*\([[:space:]]*[^)[:space:]]/) {
      seen_code = 1; pend = 0; pend_words = 0; next
    }
    nm = stripped
    sub(/^function[[:space:]]+/, "", nm); sub(/[[:space:]]*\(.*$/, "", nm); sub(/[[:space:]].*$/, "", nm)
    printf "%s\t%d\tpublic\tfunction\t%s\t%s\t%d\n", FILENAME, FNR, nm, (pend ? "doc" : "none"), pend_words
  }
  seen_code = 1; pend = 0; pend_words = 0
}

END {
  if (FILENAME != "") printf "%s\t1\tpublic\tfile\t%s\t%s\t%d\n", FILENAME, FILENAME, (hdr ? "doc" : "none"), hdr_words
}
