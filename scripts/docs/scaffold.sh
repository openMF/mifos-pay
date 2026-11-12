#!/usr/bin/env bash
# scripts/docs/scaffold.sh — create/refresh the docs/architecture tree FROM DISK.
#
# Coverage is complete by construction: add a module, re-run, and its guide exists. Never overwrites
# authored prose — a guide that already carries content below its `<!-- scaffold:end -->` marker
# keeps it.
#
# Each guide is seeded with MEASURED facts (public API, tests, annotations defined and consumed,
# corpus surface, dependents) rather than placeholder text, because a stub that says "TODO: describe
# this module" is worse than no file: it looks like coverage and teaches nothing.
#
# THIS SHIPS IN THE TEMPLATE so the docs Action can create a page for a NEW module with no framework
# checkout. Without it the pipeline had a hole: `api-docs-gen.sh` injects INTO an existing page, so a
# newly added module had nowhere for its API block to go and the gate failed with "module has sources
# but NO architecture page" — a failure CI could report and not fix.
#
# Usage: scripts/docs/scaffold.sh [<template-root>] [--write]
# Env:   CORPUS_PATH  framework training-corpus dir, when one is reachable (see below)
set -uo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"; cd "$ROOT" || exit 2
TPL="${1:-$ROOT}"
[ "${1:-}" = "--write" ] && { TPL="$ROOT"; WRITE=1; } || WRITE=0
[ "${2:-}" = "--write" ] && WRITE=1
[ -d "$TPL/core" ] || { echo "no template at $TPL" >&2; exit 2; }

# The corpus lives in the FRAMEWORK, which a fork or a CI runner does not have. Passing an empty path
# makes the generator state the expected surface NAME without asserting its presence — see the
# `corpus_known` handling below. Claiming "⚠ missing" for a surface we simply cannot see would be a
# lie printed on every page in every fork.
CORPUS_PATH="${CORPUS_PATH:-}"

python3 - "$TPL" "$WRITE" "$CORPUS_PATH" <<'PYEOF'
import io, os, re, sys
tpl, write, corpus_path = sys.argv[1], sys.argv[2] == "1", sys.argv[3]
ARCH = os.path.join(tpl, "docs", "architecture")
# Empty when no framework is reachable (fork / CI). `corpus_known` gates every claim
# about whether a surface EXISTS; the surface NAME is derived from the module and is
# always safe to state.
CORPUS = corpus_path
corpus_known = bool(corpus_path) and os.path.isdir(corpus_path)
MARK = "<!-- scaffold:end -->"

def read(p):
    try: return io.open(p, encoding="utf-8", errors="replace").read()
    except Exception: return ""

# ── modules from disk ────────────────────────────────────────────────────────
mods = []
for base in ("core-base", "core"):
    d = os.path.join(tpl, base)
    if os.path.isdir(d):
        for m in sorted(os.listdir(d)):
            if os.path.isdir(os.path.join(d, m)) and not m.startswith("."):
                mods.append((base, m))

contract = read(os.path.join(ARCH, "CONTRACT.yaml"))

def surface_name(base, m):
    tok = re.sub(r"[^A-Z0-9]", "_", m.upper())
    return ("CORE_BASE_%s.md" % tok) if base == "core-base" else ("CORE_%s.md" % tok)

def facts(base, m):
    """Measured facts only. Anything not measurable is left out, not guessed."""
    root = os.path.join(tpl, base, m)
    kt, tests, pub, defines, consumes = 0, 0, [], set(), set()
    for r, _, fs in os.walk(root):
        if "/build/" in r: continue
        for fn in fs:
            if not fn.endswith(".kt"): continue
            kt += 1
            if "commonTest" in r or fn.endswith("Test.kt"): tests += 1
            b = read(os.path.join(r, fn))
            defines |= set(re.findall(r"annotation\s+class\s+(\w+)", b))
            consumes |= set(re.findall(r"^\s*@(\w+)", b, re.M))
            # Principal types come from MAIN source sets only. Including tests made the list
            # read "WebSecureCryptoTest" for core-base/crypto — the one type a reader must not
            # take as the module's public surface.
            is_test = ("Test" in r) or fn.endswith("Test.kt") or "/test" in r.lower()
            if is_test: continue
            # Full KMP modifier set. The first cut knew only `public/abstract/sealed/open`, so it
            # missed `expect class` / `actual class` — the dominant shape in this template —
            # and reported core-base/crypto as having no principal types at all while it has 22
            # files. expect/actual pairs collapse to one name by the set() below.
            for mm in re.finditer(
                    r"^(?:public\s+|internal\s+|private\s+)?(?:expect\s+|actual\s+)?"
                    r"(?:abstract\s+|sealed\s+|open\s+|data\s+|value\s+|enum\s+|annotation\s+)*"
                    r"(?:class|interface|object|fun\s+interface)\s+(\w+)", b, re.M):
                pub.append(mm.group(1))
    return {"kt": kt, "tests": tests, "pub": sorted(set(pub)), "defines": sorted(defines),
            "consumes": sorted(c for c in consumes if c in ALL_ANN)}

ALL_ANN = set(re.findall(r"^\s*-\s*annotation:\s*(\w+)", contract, re.M))

def contract_rows(m_path):
    rows = []
    for chunk in re.split(r"\n\s*-\s*annotation:\s*", contract)[1:]:
        name = chunk.split("\n", 1)[0].strip()
        own = re.search(r"^\s*owns_module:\s*(\S+)", chunk, re.M)
        if own and own.group(1) == m_path:
            proc = re.search(r"^\s*processor:\s*(\S+)", chunk, re.M)
            gen = re.search(r"^\s*generates:\s*\[([^\]]*)\]", chunk, re.M)
            # Line-walk instead of a regex group: the `(?:[ ]{6,}.*\n)+` form silently stopped
            # after two of three lines even though each matched in isolation, and a note that
            # loses its last sentence loses the failure mode ("throws at first" / "…first query").
            ntxt = ""
            _ls = chunk.split("\n")
            for _i, _l in enumerate(_ls):
                if _l.strip().startswith("notes:"):
                    _acc = []
                    for _n in _ls[_i + 1:]:
                        if _n.strip() and (len(_n) - len(_n.lstrip(" "))) >= 6:
                            _acc.append(_n.strip())
                        else:
                            break
                    ntxt = " ".join(_acc)
                    break
            rows.append((name, proc.group(1) if proc else "-", gen.group(1) if gen else "", ntxt))
    return rows

made, kept = 0, 0
for base, m in mods:
    # The module pages live UNDER THE TREE: docs/architecture/tree/<layer>/<module>.md.
    # One hierarchy, so a reader navigating `core/` from the project tree lands on the module
    # guides rather than being sent to a parallel `modules/` directory.
    d = os.path.join(ARCH, "tree", base)
    path = os.path.join(d, "%s.md" % m)
    f = facts(base, m)
    mp = "%s/%s" % (base, m)
    rows = contract_rows(mp)
    surf = surface_name(base, m)
    has_surface = os.path.isfile(os.path.join(CORPUS, surf)) if corpus_known else None

    head = ["# `%s`" % mp, ""]
    head.append("> **Layer:** %s — %s" % (base,
        "framework-shared; generators CONSUME, never write" if base == "core-base"
        else "fork-owned; a codegen target"))
    # None = "not checked" and prints nothing. Only a real, observed absence warns.
    head.append("> **Corpus surface:** `%s`%s" % (
        surf, "" if has_surface is not False else "  ⚠ missing"))
    head.append("> **Measured:** %d Kotlin files, %d test files" % (f["kt"], f["tests"]))
    head.append("")
    if rows:
        head += ["## Codegen contracts owned here", "",
                 "| annotation | processor | generates |", "|---|---|---|"]
        for n, p, g, _nt in rows:
            head.append("| `@%s` | `%s` | %s |" % (n, p, g))
        head += ["", "Declared in [`../../CONTRACT.yaml`](../../CONTRACT.yaml); that file is the"
                     " machine-verified SoT and this table is its human projection.", ""]
    if f["defines"]:
        head += ["**Defines annotations:** " + ", ".join("`@%s`" % a for a in f["defines"]), ""]
    if f["consumes"] and not rows:
        head += ["**Consumes contracts:** " + ", ".join("`@%s`" % a for a in f["consumes"]), ""]
    if f["pub"]:
        shown = f["pub"][:14]
        head += ["## Principal types", "",
                 ", ".join("`%s`" % t for t in shown) + ("  …and %d more" % (len(f["pub"]) - 14) if len(f["pub"]) > 14 else ""),
                 ""]
    head += [MARK, ""]
    seeded = "\n".join(head)

    existing = read(path)
    if existing and MARK in existing:
        authored = existing.split(MARK, 1)[1]
        out = seeded.rstrip("\n") + authored
        kept += 1
    else:
        body = ["## Contract", ""]
        if rows:
            body += ["This module is a **codegen target**: the annotations above are the whole",
                     "declaration, and their aggregates are build artifacts that must never be",
                     "authored in source.", ""]
            for n, _p, _g, nt in rows:
                body += ["### `@%s`" % n, ""]
                body += [nt if nt else "_See `%s` for the emission idiom._" % surf, ""]
        else:
            body += ["**No codegen contract.** Nothing in this module is declared by annotation, so",
                     "there is no aggregate to generate and no propagation target. The emission idiom",
                     "lives in the corpus surface `%s`, which is the authoritative instruction for" % surf,
                     "generators writing here.", ""]
        body += ["## Instruction surface", "",
                 "`%s` in `training-layer/instructions/stream-first/latest/` is the generator-facing" % surf,
                 "instruction for this module. This guide is the architecture SoT; that surface is how",
                 "it reaches codegen. They are kept in step by `/kmp-project-template-retrain`",
                 "(STEP CONTRACT-SCAN).", ""]
        out = seeded + "\n".join(body)
        made += 1
    if write:
        os.makedirs(d, exist_ok=True)
        io.open(path, "w", encoding="utf-8").write(out)

# ── ARCHITECTURE.md: refresh the generated TOC block in place ────────────────
# The TOC is GENERATED so it cannot drift from the tree; everything outside the markers is
# authored prose and is preserved byte-for-byte.
TOC_BEGIN = "<!-- toc:begin — generated by architecture-docs-scaffold.sh; do not hand-edit -->"
TOC_END = "<!-- toc:end -->"
lines = [TOC_BEGIN, "", "## Contents", "",
         "This directory is the **single source of truth for this template's architecture**, for",
         "humans and for AI. `CONTRACT.yaml` is its machine-verified low-level half; the guides below",
         "are the instruction surface.", "",
         "### Modules", "",
         "One guide per module, named 1:1 with its training-corpus surface.", ""]

# The project tree section, generated so the TOC cannot drift from the pages that exist. It is inserted
# BEFORE "### Modules" because a reader who does not know the repo needs its shape before a per-module
# API reference — `feature/` is the largest area here and had no page at all until this existed.
_tree_dir = os.path.join(tpl, "docs/architecture/tree")
if os.path.isdir(_tree_dir):
    _t = ["### Project tree", "",
          "Every top-level area of the repository — measured facts, plus authored significance.", "",
          "- [All areas](PROJECT_TREE.md)"]
    for _f in sorted(os.listdir(_tree_dir)):
        if _f.endswith(".md"):
            _t.append("- [`%s/`](tree/%s)" % (_f[:-3], _f))
    _t.append("")
    _at = lines.index("### Modules")
    lines[_at:_at] = _t
for base in ("core-base", "core"):
    group = [(b, m) for b, m in mods if b == base]
    if not group: continue
    lines.append("**`%s/`** — %s" % (base,
        "framework-shared primitives; generators consume, never write" if base == "core-base"
        else "fork-owned implementation; the codegen target"))
    lines.append("")
    for b, m in group:
        rows = contract_rows("%s/%s" % (b, m))
        tag = "  ·  contracts: " + ", ".join("`@%s`" % r[0] for r in rows) if rows else ""
        lines.append("- [`%s/%s`](tree/%s/%s.md)%s" % (b, m, b, m, tag))
    lines.append("")
lines += ["### Cross-cutting", "",
          "Concerns that span modules and are anchored by content digest rather than a module tree.", ""]
cc = sorted(os.listdir(os.path.join(ARCH, "cross-cutting"))) if os.path.isdir(os.path.join(ARCH, "cross-cutting")) else []
lines += ["- [`%s`](cross-cutting/%s)" % (f[:-3], f) for f in cc if f.endswith(".md")] or ["_(none yet)_"]
lines += ["", "### Patterns", "",
          "Named recipes spanning two or more modules.", ""]
pt = sorted(os.listdir(os.path.join(ARCH, "patterns"))) if os.path.isdir(os.path.join(ARCH, "patterns")) else []
lines += ["- [`%s`](patterns/%s)" % (f[:-3], f) for f in pt if f.endswith(".md")] or ["_(none yet)_"]
lines += ["", "### Machine-readable", "",
          "- [`CONTRACT.yaml`](CONTRACT.yaml) — every module, annotation, processor, generated",
          "  aggregate and seam. Verified equal to disk in both directions by",
          "  `framework-verify-architecture-contract.sh`; write-gated by `architecture-contract-guard`.",
          "", TOC_END]
toc_block = "\n".join(lines)

ap = os.path.join(ARCH, "ARCHITECTURE.md")
cur = read(ap)
if cur:
    if TOC_BEGIN in cur and TOC_END in cur:
        pre, rest = cur.split(TOC_BEGIN, 1)
        _, post = rest.split(TOC_END, 1)
        new_arch = pre + toc_block + post
    else:
        # insert after the intro paragraph, before the first ## section
        i = cur.find("\n## ")
        new_arch = (cur[:i] + "\n\n" + toc_block + "\n" + cur[i:]) if i > 0 else cur + "\n\n" + toc_block
    if write:
        io.open(ap, "w", encoding="utf-8").write(new_arch)

for extra in ("cross-cutting", "patterns"):
    if write: os.makedirs(os.path.join(ARCH, extra), exist_ok=True)

print("modules=%d  new=%d  refreshed-preserving-prose=%d  write=%s" % (len(mods), made, kept, write))
PYEOF
