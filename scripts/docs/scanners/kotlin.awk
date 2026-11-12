# scripts/docs/scanners/kotlin.awk — declaration + doc extractor for Kotlin.
#
# Emits one TSV row per API declaration:
#   path <TAB> line <TAB> visibility <TAB> symbol_kind <TAB> name <TAB> doc_state <TAB> doc_words
#
# doc_state: kdoc | property-tag | tag-only | none
#   property-tag = covered by an `@property <name>` on the enclosing type's KDoc, which is how Kotlin
#                  documents an inline constructor property and what detekt accepts.
#   tag-only     = a KDoc with only @param/@return and no prose. detekt counts it; the generated
#                  reference prints `_No KDoc at source._` for it, so it is NOT documented.
#
# FOUR THINGS THIS HAS TO GET RIGHT, each one measured wrong first:
#
# 1. STRINGS AND COMMENTS, not a line regex. `val p = "see /** not a doc */"` reads as both a
#    declaration and a KDoc under a naive scan. Declarations are matched against a BLANKED copy of the
#    line (string bodies and comments replaced by spaces, length preserved so columns stay true).
#    Kotlin block comments NEST, so comment state is a DEPTH.
#
# 2. FNR, NOT NR, and a per-file reset. NR counts records across every file awk was handed, so batched
#    input reported line 9528 of a 60-line file. An unbalanced brace also leaks into the next file.
#
# 3. LOCALS ARE NOT API. A scope STACK, not a depth guess: `{` pushes the kind the line declared, `}`
#    pops. Anything declared with a "fun" anywhere on the stack is a local. Tracking only the brace
#    depth of a `fun` line missed every multi-line signature, where `{` lands on a later line.
#
# 4. AN OVERRIDE INHERITS ITS CONTRACT. detekt does not ask for a doc, and a second description of one
#    behaviour is a thing that drifts.

function blank_span(s, from, to) { return substr(s, 1, from - 1) sprintf("%*s", to - from + 1, "") substr(s, to + 1) }

function blank_line(raw,   i, c, n, out) {
  out = raw; n = length(raw); i = 1
  while (i <= n) {
    c = substr(out, i, 1)
    if (RAW)      { if (substr(out,i,3)=="\"\"\"") { RAW=0; i+=3; continue } out=blank_span(out,i,i); i++; continue }
    if (DEPTH>0)  { if (substr(out,i,2)=="/*") { DEPTH++; out=blank_span(out,i,i+1); i+=2; continue }
                    if (substr(out,i,2)=="*/") { DEPTH--; out=blank_span(out,i,i+1); i+=2; continue }
                    out=blank_span(out,i,i); i++; continue }
    if (STR)      { if (c=="\\") { out=blank_span(out,i,i+1); i+=2; continue }
                    if (c=="\"") { STR=0; out=blank_span(out,i,i); i++; continue }
                    out=blank_span(out,i,i); i++; continue }
    if (substr(out,i,3)=="\"\"\"") { RAW=1; out=blank_span(out,i,i+2); i+=3; continue }
    if (c=="\"")                   { STR=1; out=blank_span(out,i,i);   i++;   continue }
    if (substr(out,i,2)=="//")     { return blank_span(out,i,n) }
    if (substr(out,i,2)=="/*")     { DEPTH=1; out=blank_span(out,i,i+1); i+=2; continue }
    i++
  }
  return out
}

function reset_doc() { doc_end = -1; doc_words = 0; doc_state = "none"; doc_props = "" }
# A Kotlin MEMBER lives directly in a type body, or at file top level. Anything nested in a function,
# an init block, a lambda or an anonymous object is a local — including
# `val results = s.scenarios.map { … }` inside a property initialiser's lambda, and
# `val reviewManager = koinInject<…>()` inside a composable. Testing only for "fun" on the stack missed
# every one of those, because a lambda's brace pushes a plain block.
function not_member(   i) { for (i = 1; i <= sp; i++) if (stack[i] != "type") return 1; return 0 }
function enclosing_type_props(   i) { for (i = sp; i >= 1; i--) if (stack[i] == "type") return props[i]; return "" }

# Visibility is INHERITED: a member of an `internal sealed class` is not public API, however the member
# line itself is written. Without this, every subclass of an internal sealed action hierarchy was
# reported as an undocumented public declaration.
function effective_vis(own,   i, v) {
  v = own
  for (i = sp; i >= 1; i--) {
    if (stack[i] != "type") continue
    if (tvis[i] == "private")   return "private"
    if (tvis[i] == "internal")  v = "internal"
    else if (tvis[i] == "protected" && v == "public") v = "protected"
  }
  return v
}
function enclosing_is_enum(   i) { for (i = sp; i >= 1; i--) if (stack[i] == "type") return isenum[i]; return 0 }

# Emit every `val`/`var` in a primary-constructor parameter list. They can share the class's own line
# — `data class Tagged(val a: Int, val b: Int)` — where a one-declaration-per-line match sees only the
# class and the properties vanish. That is the shape most entities and DTOs in this tree use.
function count_props(seg,   rest, n) {
  rest = seg; n = 0
  while (match(rest, /(^|[^A-Za-z0-9_])(val|var)[[:space:]]+[A-Za-z_][A-Za-z0-9_]*/)) {
    n++; rest = substr(rest, RSTART + RLENGTH)
  }
  return n
}

# `line_doc` is the doc state of a block that closed directly ABOVE this line, and it only applies when
# the line declares ONE property — the common multi-line entity shape:
#
#     /** jsonplaceholder's todo id; also the Store key. */
#     val id: Int,
#
# Crediting only the class's @param/@property pool reported every such property as undocumented, even
# with its own doc right above it. Two ways to document a constructor property, both legitimate.
# A constructor property carries its OWN modifier: `private val codeGenerator: CodeGenerator`. Taking
# only the class's visibility reported every private KSP dependency as undocumented public API.
function prop_vis(seg, nm,   pre, i) {
  i = index(seg, nm)
  if (i == 0) return "public"
  pre = substr(seg, 1, i - 1)
  # look back to the start of THIS parameter, not the whole line
  if (match(pre, /[(,][^(,]*$/)) pre = substr(pre, RSTART)
  if (pre ~ /(^|[^A-Za-z0-9_])private[[:space:]]/)   return "private"
  if (pre ~ /(^|[^A-Za-z0-9_])internal[[:space:]]/)  return "internal"
  if (pre ~ /(^|[^A-Za-z0-9_])protected[[:space:]]/) return "protected"
  return "public"
}

function emit_ctor_props(seg, ln, line_doc, line_words,   rest, nm, v, st, w, single) {
  # An `override val` in a primary constructor inherits its contract, exactly like an override method.
  # `KptColorSchemeImpl` restates 50 Material roles as `override val`; asking each to repeat the
  # interface's doc would create 50 second descriptions to drift. The docs belong on the interface —
  # which is where this scan then correctly points, at KptComponent.kt.
  if (seg ~ /(^|[^A-Za-z0-9_])override[[:space:]]/) return
  rest = seg; single = (count_props(seg) == 1)
  while (match(rest, /(^|[^A-Za-z0-9_])(val|var)[[:space:]]+[A-Za-z_][A-Za-z0-9_]*/)) {
    nm = substr(rest, RSTART, RLENGTH)
    rest = substr(rest, RSTART + RLENGTH)
    sub(/.*[[:space:]]/, "", nm)
    own = prop_vis(seg, nm)
    v = effective_vis(own == "public" ? ctor_vis : own)
    st = "none"; w = 0
    if (single && line_doc != "none") { st = line_doc; w = line_words }
    else if (ctor_props ~ ("[[:space:]]" nm "[[:space:]]")) { st = "property-tag"; w = 1 }
    printf "%s\t%d\t%s\tproperty\t%s\t%s\t%d\n", FILENAME, ln, v, nm, st, w
  }
}

BEGIN { FS = "\n" }

FNR == 1 {
  DEPTH=0; STR=0; RAW=0; in_kdoc=0; sp=0
  reset_doc(); pend_anno=0; anno_paren=0
  pend_type=""; pend_props=""; pend_enum=0; ctor_paren=0; ctor_props=""; sig_paren=0; sig_init=0; pend_vis="public"; ctor_vis="public"; ctor_more=0; ctor_init=0; ctor_line_doc="none"; ctor_line_words=0; single_enum=""
  SKIP = (FILENAME ~ /\/src\/[A-Za-z]*Test\//) || (FILENAME ~ /\/test\//)
}
SKIP { next }

{
  raw = $0
  entering_kdoc = (DEPTH==0 && STR==0 && RAW==0 && match(raw, /^[[:space:]]*\/\*\*/))
  depth_before = DEPTH
  L = blank_line(raw)
  stripped = raw; sub(/^[[:space:]]+/,"",stripped); sub(/[[:space:]]+$/,"",stripped)

  if (entering_kdoc) { in_kdoc=1; kd_words=0; kd_prose=0; kd_props="" }
  if (in_kdoc) {
    body = stripped; sub(/^\/\*\*/,"",body); sub(/\*\//,"",body); sub(/^\*+/,"",body)
    gsub(/^[[:space:]]+|[[:space:]]+$/,"",body)
    # BOTH tags credit a constructor property. detekt accepts either, and the tree uses both —
    # `DraftEntity` documents all eight of its columns with @param, and crediting only @property
    # reported every one of them as undocumented while detekt reported none.
    if (body ~ /^@(property|param)[[:space:]]+[A-Za-z_]/) {
      t = body; sub(/^@(property|param)[[:space:]]+/,"",t); sub(/[^A-Za-z0-9_].*$/,"",t)
      kd_props = kd_props " " t " "
    } else if (body != "") {
      if (body !~ /^@/) { kd_prose = 1; kd_words += split(body, _w, /[[:space:]]+/) }
    }
    if (DEPTH == 0) {
      in_kdoc=0; doc_end=FNR; doc_words=kd_words; doc_props=kd_props
      doc_state = kd_prose ? "kdoc" : "tag-only"
    }
    next
  }
  if (depth_before > 0 || DEPTH > 0) next
  if (L ~ /^[[:space:]]*$/) next

  if (anno_paren > 0) { anno_paren += gsub(/\(/,"(",L) - gsub(/\)/,")",L); pend_anno=1; next }
  # `\b` is a BACKSPACE in awk, not a word boundary. With `\b` here the test never matched, so every
  # `@PrimaryKey val id: Long` line was swallowed as a bare annotation and its property never emitted.
  # POSIX classes only — see the same lesson in workflow.sh.
  if (L ~ /^[[:space:]]*@/ && L !~ /(^|[^A-Za-z0-9_])(val|var|fun|class|object|interface|typealias)[[:space:]]+[A-Za-z_]/) {
    anno_paren = gsub(/\(/,"(",L) - gsub(/\)/,")",L); if (anno_paren < 0) anno_paren = 0
    pend_anno=1; next
  }

  is_override = (L ~ /(^|[[:space:]])override[[:space:]]/)
  vis = "public"
  if (L ~ /(^|[[:space:]])private[[:space:]]/)        vis = "private"
  else if (L ~ /(^|[[:space:]])internal[[:space:]]/)  vis = "internal"
  else if (L ~ /(^|[[:space:]])protected[[:space:]]/) vis = "protected"

  kind=""; name=""
  # A `companion object` has NO name after the keyword, so a name-requiring match skips it entirely —
  # detekt reports it as UndocumentedPublicClass and the scanner said nothing.
  if (L ~ /(^|[[:space:]])companion[[:space:]]+object([[:space:]]*\{|[[:space:]]*$)/) {
    kind = "object"; name = "Companion"; pend_type = "type"; pend_props = doc_props; pend_vis = vis
  } else if (match(L, /(^|[[:space:]])(class|interface|object)[[:space:]]+[A-Za-z_][A-Za-z0-9_]*/)) {
    seg=substr(L,RSTART,RLENGTH); split(seg,p,/[[:space:]]+/); kind=p[length(p)-1]; name=p[length(p)]
    if (L ~ /enum[[:space:]]+class/)       kind="enum"
    if (kind == "enum" && L ~ /\{[^}]*\}/) single_enum = L
    if (L ~ /annotation[[:space:]]+class/) kind="annotation"
    pend_type=kind; pend_props=doc_props; pend_enum=(kind=="enum"); pend_vis=vis
    ctor_vis = vis
    # A single-line constructor balances its own parens, so ctor_paren is already 0 by the time the
    # emit runs — the line still has to be scanned. Flag it explicitly rather than inferring from depth.
    if (L ~ /\(/) { ctor_more = 1; ctor_line_doc = "none"; ctor_line_words = 0 }
    ctor_init = 1     # seed below, ONCE — seeding here too double-counted this line's parens
    ctor_props = doc_props
  } else if (match(L, /(^|[[:space:]])fun[[:space:]]+[A-Za-z_<][^({]*/)) {
    seg=substr(L,RSTART,RLENGTH); sub(/^[[:space:]]*/,"",seg); sub(/^fun[[:space:]]+/,"",seg)
    sub(/^<[^>]*>[[:space:]]*/,"",seg); sub(/[[:space:]]*$/,"",seg)
    n2=split(seg,q,/\./); name=q[n2]; kind="fun"; pend_type="fun"
    # A lambda DEFAULT inside the signature — `content: @Composable () -> Unit = {}` — opens and
    # closes a brace before the body does. Consuming `pend_type` there made the real `) {` push a
    # plain block, so every local in the function was reported as public API. Track the signature's
    # paren balance: while it is open, braces are signature lambdas, not the body.
    sig_init = 1
  } else if (ctor_paren > 0) {
    ctor_more = 1                                  # handled by emit_ctor_props below, not here
    # A doc can sit above the property's own ANNOTATION:
    #     /** Client-generated UUID. Primary key. */
    #     @PrimaryKey
    #     val id: String,
    # Annotation lines `next` without clearing the pending doc, so the test is "is one pending", not
    # "is it on the previous line" — which reported every annotated entity column as undocumented.
    ctor_line_doc = (doc_end > 0) ? doc_state : "none"
    ctor_line_words = (doc_end > 0) ? doc_words : 0
  } else if (L ~ /(^|[[:space:]])(get|set)[[:space:]]*\(/) {
    # A property ACCESSOR body is a function body: `get(): String { val fromFile = … }` declares a
    # local, not API. Without this the accessor's braces pushed a plain block and every local inside a
    # custom getter was reported as a public property.
    pend_type = "fun"; sig_init = 1; kind = ""
  } else if (match(L, /(^|[[:space:]])(val|var)[[:space:]]+[A-Za-z_][A-Za-z0-9_]*/)) {
    # An EXTENSION property names its receiver first: `val Project.dynamicVersion`. Take the segment
    # after the last dot, or the gap list reports the receiver type as the symbol.
    # `^.*` is greedy and swallowed past the keyword, leaving "val" as the name. Match the declaration
    # itself, then strip the keyword off the front of THAT match.
    match(L, /(val|var)[[:space:]]+[A-Za-z_][A-Za-z0-9_.]*/)
    seg=substr(L,RSTART,RLENGTH); sub(/^(val|var)[[:space:]]+/,"",seg)
    np=split(seg,pp,/\./); name=pp[np]; kind="property"
  } else if (enclosing_is_enum() && match(L, /^[[:space:]]*[A-Z][A-Za-z0-9_]*[[:space:]]*[,(;{]/)) {
    # Entries are Capitalised, not necessarily SCREAMING: `Left`, `Xs`, `FOLLOW_SYSTEM` all occur here.
    # An all-caps-only pattern silently skipped every mixed-case one.
    seg=substr(L,RSTART,RLENGTH); gsub(/[^A-Za-z0-9_]/,"",seg); name=seg; kind="enum-entry"
  }

  if (kind != "") {
    local = not_member()
    if (local || is_override) { reset_doc(); pend_anno=0; anno_paren=0; kind="" }
  }
  if (kind != "") {
    vis = effective_vis(vis)
    state = (doc_end > 0) ? doc_state : "none"
    words = (doc_end > 0) ? doc_words : 0
    # An inline constructor property, or a member of a type whose KDoc tags it, is documented by that tag.
    if (state == "none" && (kind == "property" || kind == "enum-entry")) {
      pool = (ctor_paren > 0) ? ctor_props : enclosing_type_props()
      if (pool ~ ("[[:space:]]" name "[[:space:]]")) { state = "property-tag"; words = 1 }
    }
    printf "%s\t%d\t%s\t%s\t%s\t%s\t%d\n", FILENAME, FNR, vis, kind, name, state, words
    reset_doc(); pend_anno=0; anno_paren=0
  } else if (!pend_anno) { reset_doc() }
  pend_anno=0

  # Entries of a single-line enum, emitted from the brace span. They share the class's line, so each is
  # undocumented unless the class KDoc tags it — which is what detekt concludes too.
  if (single_enum != "") {
    body = single_enum
    sub(/^[^{]*\{/, "", body); sub(/\}.*$/, "", body)
    ecount = split(body, ents, ",")
    for (ei = 1; ei <= ecount; ei++) {
      enm = ents[ei]
      gsub(/^[[:space:]]+|[[:space:]]+$/, "", enm)
      sub(/[(=].*$/, "", enm)
      if (enm ~ /^[A-Za-z_][A-Za-z0-9_]*$/) {
        est = "none"; ew = 0
        if (doc_props ~ ("[[:space:]]" enm "[[:space:]]")) { est = "property-tag"; ew = 1 }
        printf "%s\t%d\t%s\tenum-entry\t%s\t%s\t%d\n", FILENAME, FNR, vis, enm, est, ew
      }
    }
    single_enum = ""
  }
  if (ctor_paren > 0 || ctor_more) {
    emit_ctor_props(L, FNR, ctor_line_doc, ctor_line_words)
    ctor_more = 0; ctor_line_doc = "none"; ctor_line_words = 0
    reset_doc()            # consumed — a later sibling must not reuse this property's doc
  }
  # Count ONCE, like sig_paren. Seeding on the class line and adding the same line's parens here left
  # ctor_paren permanently above zero, so every later top-level property was routed through the
  # constructor path and lost its own KDoc.
  cdelta = gsub(/\(/,"(",L) - gsub(/\)/,")",L)
  if (ctor_init)           { ctor_paren = cdelta; ctor_init = 0 }
  else if (ctor_paren > 0) { ctor_paren += cdelta }
  if (ctor_paren < 0) ctor_paren = 0
  # Update FIRST, then judge: on the `) {` line the signature's paren CLOSES, so the brace that
  # follows is the body. Judging before the update treated that brace as a lambda default and pushed a
  # plain block — which is worse than the bug it replaced, and the probe said so immediately.
  # Count the parens ONCE. Seeding sig_paren on the `fun` line AND adding the same line's parens here
  # double-counted them, so the signature never balanced and the body brace stayed misclassified —
  # the probe kept reporting a local as public API through two "fixes".
  pdelta = gsub(/\(/,"(",L) - gsub(/\)/,")",L)
  if (sig_init)           { sig_paren = pdelta; sig_init = 0 }
  else if (sig_paren > 0) { sig_paren += pdelta }
  if (sig_paren < 0) sig_paren = 0
  in_sig = (sig_paren > 0)
  nb = gsub(/\{/,"{",L); nc = gsub(/\}/,"}",L)
  while (nb-- > 0) {
    sp++
    if (in_sig)                    stack[sp] = "block"   # a lambda default, not the body
    else if (pend_type == "fun")   stack[sp] = "fun"
    else if (pend_type != "")      stack[sp] = "type"
    else                           stack[sp] = "block"
    props[sp] = pend_props; isenum[sp] = pend_enum; tvis[sp] = pend_vis
    if (!in_sig) { pend_type=""; pend_props=""; pend_enum=0; pend_vis="public" }
  }
  while (nc-- > 0 && sp > 0) sp--
}
