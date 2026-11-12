#!/usr/bin/env bash
# scripts/docs/doc-refs.sh — does the AUTHORED prose still name files that exist?
#
# WHY THIS EXISTS
# The generated half of the docs cannot go stale: `refresh.sh --check` re-derives it and diffs. The
# AUTHORED half has no such protection, and it is the half that names things — a script to run, a file
# to edit, a class to look at. When one of those is renamed or deleted, the prose keeps pointing at it
# and a reader goes looking for something that was removed on purpose.
#
# That is not hypothetical here. `scripts/CLAUDE.md` was rewritten in 2026-09 after an audit found it
# describing 8 scripts that no longer existed, 22 that were never mentioned, and every path written flat
# after the tree had moved into subdirectories. A doc that names a missing script costs more than no doc.
#
# WHAT COUNTS AS RESOLVED — and why the classes matter
# A flat "does this path exist" check reports ~38% of references dead and is therefore ignored. Five
# resolutions are legitimate and only the sixth is a defect:
#
#   exact        the reference is a tracked path
#   suffix       a tracked path ends with it (`libs.versions.toml` → `gradle/libs.versions.toml`)
#   gitignored   DELIBERATELY absent — `local.properties`, `GoogleService-Info.plist`, a keystore.
#                Telling a reader to create one of these is the correct instruction.
#   generated    produced by a build (`BuildKonfig.kt`) — absent in a clean checkout, present after.
#   framework    lives in the framework repo, not this template (`framework-verify-*.sh`).
#   DEAD         nothing explains it. This is the one the gate fails on.
#
# Usage:
#   scripts/docs/doc-refs.sh              # summary + the dead list
#   scripts/docs/doc-refs.sh --strict     # exit 1 when anything is DEAD
# Env: TEMPLATE_PATH
set -uo pipefail
ROOT="${TEMPLATE_PATH:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"
cd "$ROOT" || exit 2
STRICT=0; for a in "$@"; do [ "$a" = "--strict" ] && STRICT=1; done

python3 - "$ROOT" "$STRICT" <<'PYEOF'
import io, os, re, subprocess, sys, collections
ROOT, STRICT = sys.argv[1], sys.argv[2] == "1"
os.chdir(ROOT)

# The tree AS THE NEXT COMMIT WILL HOLD IT: tracked + untracked-not-ignored files that exist on disk.
# Plain `git ls-files` lists the INDEX, so a refresh run before committing a new file (or after
# deleting one) wrote pages that went stale the moment the commit landed, and CI then failed the
# docs check on a tree that had been "refreshed". On a clean checkout this is exactly `git ls-files`.
TRACKED = sorted({f for f in subprocess.run(["git","ls-files","-co","--exclude-standard"],capture_output=True,
                    text=True).stdout.splitlines() if os.path.lexists(f)})
EXACT = set(TRACKED)
SUFFIX = collections.defaultdict(set)
for t in TRACKED:
    parts = t.split("/")
    for i in range(len(parts)):
        SUFFIX["/".join(parts[i:])].add(t)

# Deliberately absent from the repo: something a fork CREATES or a build PRODUCES, so prose naming it is
# an instruction rather than a stale reference. `git check-ignore` is ASKED rather than guessed — a
# hand-written pattern list is the same defect as the enumerated detekt excludes that named five source
# sets this tree does not have. One batched call, because per-reference invocation is ~350 processes.
def ignored_set(refs):
    if not refs:
        return set()
    r = subprocess.run(["git", "check-ignore", "--stdin"], input="\n".join(sorted(refs)),
                       capture_output=True, text=True)
    return set(r.stdout.splitlines())
IGNORED = set()

# Basenames the template ships a SAMPLE of — `secrets/sample/**` mirrors `secrets/live/**` 1:1, so a
# sample is the template saying "a fork provides this file, here is its shape".
SAMPLES = {os.path.basename(t) for t in TRACKED if t.startswith("secrets/sample/")}

# Basenames named by EXECUTABLE source. If doctor.sh or a lane reads a path, the file is expected to
# exist at runtime even when the template does not ship it.
SOURCE_EXT = (".sh", ".rb", ".kt", ".kts", ".yaml", ".yml", ".properties", ".xcconfig")
def _named_by_source():
    srcs = [t for t in TRACKED if t.endswith(SOURCE_EXT)]
    if not srcs:
        return set()
    hay = []
    for t in srcs:
        try:
            hay.append(io.open(t, encoding="utf-8", errors="replace").read())
        except OSError:
            pass
    blob = "\n".join(hay)
    return {b for b in {os.path.basename(x) for x in ()} } or blob
NAMED_BLOB = _named_by_source()

class _NamedBy:
    """Membership test against the concatenated source, computed once."""
    def __contains__(self, base):
        return isinstance(NAMED_BLOB, str) and base in NAMED_BLOB
NAMED_BY_SOURCE = _NamedBy()
GENERATED = re.compile(r"(BuildKonfig|BuildConfig|Generated[A-Za-z]*|version\.properties)\.kt$|"
                       r"^build/|/build/")
# The framework drives this template from outside it; those paths are real, just not here.
FRAMEWORK = re.compile(r"^(core/scripts/|framework-verify-|\.claude-runtime/|layers/|capsules/)")

def strip_rel(ref):
    """Remove a leading `./` — NOT `lstrip("./")`, which strips character-wise and turned
    `.github/workflows/x.yaml` into `github/workflows/x.yaml`, reporting every dotfile path as dead."""
    return ref[2:] if ref.startswith("./") else ref

def classify(ref):
    r = strip_rel(ref)
    if r in EXACT:                       return "exact"
    if r in SUFFIX:                      return "suffix"
    # An ELIDED path — `core-base/store/.../screen/ScreenDataStream.kt` — is this tree's shorthand for a
    # long package path. Held to existence like any other, by matching head and tail against a real path.
    # A LEADING elision (`.../di/ForkNetworkModule.kt`) claims only a tail — resolve it as a suffix.
    if r.startswith((".../", "…/")):
        tail = re.sub(r"^(?:\.\.\.|…)/", "", r)
        return "elided" if tail in SUFFIX else "DEAD"
    if "/.../" in r or "/…/" in r:
        head, tail = re.split(r"/(?:\.\.\.|…)/", r, maxsplit=1)
        return "elided" if any(t.startswith(head + "/") and t.endswith("/" + tail) for t in EXACT) else "DEAD"
    # A BARE filename with no path component is too weak a claim to hold to existence: a style guide's
    # `UserRepository.kt` is an example of a NAME, not a pointer to a file. Reported separately so the
    # count stays visible without turning illustrative prose into a build failure.
    if "/" not in r:                      return "weak"
    if r in IGNORED:                     return "gitignored"
    if GENERATED.search(r):              return "generated"
    if FRAMEWORK.search(r):              return "framework"
    if os.path.exists(r):                return "untracked"   # created, not yet committed
    # FORK-SUPPLIED: the template ships the LOCATION but not the content, so prose naming the path is an
    # instruction ("put your GoogleService-Info.plist here") rather than a stale pointer.
    #
    # The FIRST attempt at this rule was "the parent directory is tracked" — and it was too loose to be
    # worth having: `scripts/` is tracked, so an invented `scripts/deleted-on-purpose.sh` passed, and the
    # gate reported IN STEP on a reference I had planted to prove it would fail. Two tighter signals,
    # both derived from the repo rather than from a list:
    #
    #   · a SAMPLE of the same basename ships under secrets/sample/ — the template declares the shape
    #   · EXECUTABLE source names the basename (a .sh/.rb/.kt/.kts/.ya?ml file, never markdown)
    #
    # Markdown is excluded on purpose: prose citing prose proves nothing, and it is exactly how a stale
    # reference keeps itself alive across a dozen pages.
    base = os.path.basename(r)
    if base in SAMPLES:
        return "fork-supplied"
    if base in NAMED_BY_SOURCE:
        return "fork-supplied"
    return "DEAD"

PAT = re.compile(r'`(\.{0,2}/?[A-Za-z0-9_./-]+\.(?:kt|kts|sh|rb|ya?ml|json|toml|properties|plist|xcconfig))`')
# A path inside a link to ANOTHER repository belongs to that repository. `ADOPTION_KMP_PRODUCT_FLAVORS.md`
# documents the upstream kmp-product-flavors library, so its `AppFlavor.kt` and `pr-check.yml` are the
# LIBRARY's files — real, just not here. Held to existence they would be permanent false failures.
EXTERNAL = re.compile(r"\[`([^`]+)`\]\(https?://")

def authored_only(text):
    """Strip every generated block — their content is re-derived and not a human's claim."""
    for mark in ("<!-- tree-scaffold:end -->", "<!-- scaffold:end -->"):
        if mark in text:
            text = text.split(mark, 1)[1]
    return re.sub(r"<!-- api-docs:begin.*?<!-- api-docs:end -->", "", text, flags=re.S)

# Pass 1 — collect every reference, so `git check-ignore` is called ONCE.
_all = set()
for d, _, fs in os.walk("docs"):
    for f in fs:
        if not f.endswith(".md"):
            continue
        b = authored_only(io.open(os.path.join(d, f), encoding="utf-8", errors="replace").read())
        b = re.sub(r"```.*?```", "", b, flags=re.S)
        for m in PAT.finditer(b):
            ref = m.group(1)
            if not ref.startswith("http") and "openMF/" not in ref:
                _all.add(strip_rel(ref))
IGNORED = ignored_set(_all)

tally = collections.Counter()
dead  = collections.defaultdict(set)
for d, _, fs in os.walk("docs"):
    for f in fs:
        if not f.endswith(".md"):
            continue
        p = os.path.join(d, f)
        body = authored_only(io.open(p, encoding="utf-8", errors="replace").read())
        # A reference inside a FENCED BLOCK is usually illustrative — a style guide's `UserRepository.kt`
        # is an example, not a file. Only prose references are held to existing.
        body = re.sub(r"```.*?```", "", body, flags=re.S)
        ext = set(EXTERNAL.findall(body))
        # A doc ABOUT another repo (its own name appears in its external links) describes that repo's
        # files throughout, not only inside the links — so the whole page is exempted once any of its
        # references is externally linked, rather than reference by reference.
        page_external = bool(ext)
        for m in PAT.finditer(body):
            ref = m.group(1)
            if ref.startswith("http") or "openMF/" in ref:
                continue
            c = classify(ref)
            if c == "DEAD" and (ref in ext or (page_external and strip_rel(ref) not in EXACT)):
                c = "external"
            tally[c] += 1
            if c == "DEAD":
                dead[p].add(ref)

total = sum(tally.values())
print(f"authored file references: {total}")
for k, v in tally.most_common():
    print(f"  {k:<12} {v:4d}")
n = sum(len(v) for v in dead.values())
if n:
    print(f"\nDEAD — {n} reference(s) across {len(dead)} page(s):")
    for p, rs in sorted(dead.items()):
        print(f"  {p}")
        for r in sorted(rs):
            print(f"      `{r}`")
else:
    print("\nno dead references")
sys.exit(1 if (STRICT and n) else 0)
PYEOF
