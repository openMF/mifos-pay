#!/usr/bin/env bash
# scripts/docs/tree-scaffold.sh — a docs page for EVERY top-level area of the project, shaped like the tree.
#
# WHY THIS EXISTS
# `scaffold.sh` generates a page per Kotlin module under `core/` and `core-base/` — 911 of the repo's
# ~3,200 tracked files. Measured 2026-09-27, everything else had nothing:
#
#     feature/        656 files   0 pages   ← the largest area in the repo
#     scripts/        426 files   0 pages
#     app-profile/    161 files   0 pages   ← the fork-owned source of truth
#     build-logic/     41 files   0 pages
#     deployment/     174 files   4 pages
#
# A reader arriving at `docs/` could learn how `core/store` works and nothing about how the app is
# built, signed, released or configured. So this covers the tree, not a subset of it.
#
# ENUMERATED FROM DISK, NEVER FROM A LIST
# The areas come from `git ls-files`, so a new top-level directory gets a page on the next run. A
# hardcoded roster is how coverage silently stops matching the repo — the same defect that had detekt's
# test excludes naming five source sets this tree does not have while missing the two it does.
#
# GENERATED FACTS + AUTHORED SIGNIFICANCE
# Everything above `<!-- tree-scaffold:end -->` is regenerated: counts, subdirectories, entry points,
# ownership. Below it is prose a person writes — what the area is FOR, what breaks without it, what a
# fork touches — and regeneration never touches it. That split is the point: the facts cannot go stale,
# and the significance cannot be derived.
#
# Usage: scripts/docs/tree-scaffold.sh [--write]
# Env:   TEMPLATE_PATH
# Exit:  0 ok · 1 would change (without --write) · 2 cannot run
set -uo pipefail
ROOT="${TEMPLATE_PATH:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"
cd "$ROOT" || exit 2
WRITE=0; for a in "$@"; do [ "$a" = "--write" ] && WRITE=1; done

python3 - "$ROOT" "$WRITE" <<'PYEOF'
import io, os, re, subprocess, sys, collections

ROOT, WRITE = sys.argv[1], sys.argv[2] == "1"
OUT = "docs/architecture/tree"
MARK = "<!-- tree-scaffold:end -->"

def git(*a):
    return subprocess.run(["git", *a], cwd=ROOT, capture_output=True, text=True).stdout.splitlines()

# The tree AS THE NEXT COMMIT WILL HOLD IT: tracked + untracked-not-ignored files that exist on disk.
# Plain `git ls-files` lists the INDEX, so a refresh run before committing a new file (or after
# deleting one) wrote pages that went stale the moment the commit landed, and CI then failed the
# docs check on a tree that had been "refreshed". On a clean checkout this is exactly `git ls-files`.
FILES = sorted({f for f in git("ls-files", "-co", "--exclude-standard")
                if os.path.lexists(os.path.join(ROOT, f))})

# Areas with no page, each with the REASON — published on PROJECT_TREE.md rather than left implicit. An
# area silently missing from the map is indistinguishable from one nobody has considered, which is the
# same failure as a doc that omits a script: the reader cannot tell absence from oversight.
SKIP = {
    "docs":            "this documentation tree",
    ".swiftpm-locks":  "generated SwiftPM resolution locks",
    "kotlin-js-store": "generated yarn.lock for Kotlin/JS",
    "META-INF":        "one service-registration file",
    ".bundle":         "bundler config (two lines)",
    ".run":            "IDE run configurations",
    "config":          "the detekt config — documented where detekt is configured",
    "spotless":        "the license-header template applied by Spotless",
}

by_area = collections.defaultdict(list)
for f in FILES:
    parts = f.split("/")
    if len(parts) > 1:
        by_area[parts[0]].append(f)

areas = sorted(a for a in by_area if a not in SKIP)

# ── classification: what KIND of area is this, so the page leads with the right facts ──────────────
def classify(area, files):
    exts = {f.rsplit(".", 1)[-1] for f in files if "." in f}
    if any(f.startswith(f"{area}/") and "/src/common" in f for f in files):
        return "kmp-modules" if len({f.split("/")[1] for f in files if len(f.split("/")) > 2}) > 1 else "kmp-module"
    if "kts" in exts and area == "build-logic":            return "build-logic"
    if area == ".github":                                   return "ci"
    if area == "deployment" or area == "fastlane":           return "deploy"
    if area == "app-profile":                               return "config-sot"
    if area == "scripts":                                   return "automation"
    if area == "secrets":                                   return "secrets"
    if area == "tools":                                     return "codegen"
    if area == "tests":                                     return "fixtures"
    if area == "legal":                                     return "legal"
    if area == "gradle":                                    return "build-config"
    return "other"

# Pages that describe an area but live OUTSIDE the tree because they genuinely span modules —
# `architecture/cross-cutting/` is their declared home. Everything else that described one area has been
# MOVED into that area's own directory, where `sibling_guides()` links it automatically; a hand-listed
# map of those would be one more thing to forget when a page moves.
DEEP_GUIDES = {
    "core-base": [("architecture/cross-cutting/store-architecture.md", "Store architecture"),
                  ("architecture/cross-cutting/store-data-api.md", "Store + data API reference"),
                  ("architecture/cross-cutting/source-set-hierarchy.md", "Source-set hierarchy")],
    "core":      [("architecture/cross-cutting/customization-surface.md", "Customization surface"),
                  ("architecture/cross-cutting/style-guide.md", "Style guide")],
    "feature":   [("architecture/cross-cutting/demo-showcase.md", "Demo showcase — feature ↔ archetype pairing")],
    "build-logic": [("architecture/cross-cutting/flavors-extension.md", "Flavor extension"),
                    ("architecture/cross-cutting/consumer-app-migration-guide.md", "Consumer app migration")],
}

# ── SUBSECTIONS ────────────────────────────────────────────────────────────────────────────────────
# A child directory gets its own page when it is a UNIT — something with its own contract: a deploy
# platform, a script group, a feature module, a KSP processor. Derived, not listed: a unit CARRIES ITS
# OWN build file, executable script or declared config.
#
# That rule was checked against the tree and it agrees with the source of truth. `deployment/linux` and
# `deployment/windows` hold metadata but no lane, and DEPLOYMENT_MANIFEST.yaml declares exactly four
# platforms (android, ios, desktop, web) — so they are desktop VARIANTS and belong inside the desktop
# page, which is what the rule concludes. It also rejects `cmp-ios/iosApp.xcodeproj` and
# `KotlinMultiplatformLinkedPackage`: Xcode artifacts, not units a reader navigates to.
OWN_MARKERS = (".gradle.kts", ".sh", ".rb", ".yaml", ".yml")
# Fastlane's own files carry NO extension, so an extension-only test missed `deployment/fastlane/` —
# the directory holding the canonical Fastfile, which is as much a unit as any lane directory.
OWN_NAMES = ("Fastfile", "Appfile", "Pluginfile", "Gemfile", "Package.swift")
NOT_A_UNIT  = {"src", "icons", "resources", "assets", "build"}
MODULE_PAGED = {"core", "core-base"}     # per-module guides, generated by scaffold.sh into tree/<layer>/

def subsections(area, files):
    if area in MODULE_PAGED:
        return []
    kids = collections.defaultdict(list)
    for f in files:
        p = f.split("/")
        if len(p) > 2:
            kids[p[1]].append(f)
    out = []
    for c, fs in kids.items():
        if c in NOT_A_UNIT or len(fs) < 3:
            continue
        # A GENERATED ObjC shim package — `Package.swift` plus only `.m`/`.h`/`.modulemap` stubs and
        # mangled `subpackages/` — is linking scaffolding, not a unit. Excluded by its SHAPE rather than
        # its name: `cmp-ios/KotlinMultiplatformLinkedPackage` is machine-written, and a page about it
        # would describe the build system's bookkeeping.
        real = [f for f in fs if not f.endswith((".m", ".h", ".modulemap", "Package.swift"))]
        if not real:
            continue
        if any(f.endswith(OWN_MARKERS) or f.split("/")[-1] in OWN_NAMES for f in fs):
            out.append((c, fs))
    return sorted(out)

KIND_LABEL = {
    "kmp-modules": "Kotlin Multiplatform module group",
    "kmp-module":  "Kotlin Multiplatform module",
    "build-logic": "Gradle convention plugins (a separate build)",
    "ci":          "GitHub Actions",
    "deploy":      "Release + deployment automation",
    "config-sot":  "Fork-owned configuration source of truth",
    "automation":  "Shell + Ruby automation",
    "secrets":     "Secret material (gitignored live values)",
    "codegen":     "KSP symbol processors",
    "fixtures":    "Test fixtures",
    "legal":       "Generated legal documents",
    "build-config":"Gradle wrapper + version catalog",
    "other":       "Project area",
}

def ext_summary(files, top=5):
    c = collections.Counter(f.rsplit(".", 1)[-1] for f in files if "." in f.split("/")[-1])
    return ", ".join(f"{n}× `.{e}`" for e, n in c.most_common(top))

def subdirs(area, files):
    """Immediate children with their file counts — the area's own shape."""
    c = collections.Counter()
    for f in files:
        p = f.split("/")
        if len(p) > 2:
            c[p[1]] = c[p[1]] + 1
        else:
            c["(files at the root)"] = c["(files at the root)"] + 1
    return c.most_common()

def first_header_comment(path, limit=6):
    """The one-line purpose a file already states in its own header — reuse it, never restate it."""
    full = os.path.join(ROOT, path)
    if not os.path.isfile(full):
        return ""
    try:
        with open(full, encoding="utf-8", errors="replace") as fh:
            for i, line in enumerate(fh):
                if i > limit:
                    break
                t = line.strip()
                if t.startswith("#!") or t in ("#", ""):
                    continue
                if t.startswith("#"):
                    t = t.lstrip("# ").strip()
                    # `name.sh — purpose` is this tree's convention; keep the purpose half.
                    return t.split("—", 1)[1].strip() if "—" in t else t
                break
    except OSError:
        return ""
    return ""

def entry_points(area, kind, files):
    rows = []
    if kind in ("kmp-modules", "kmp-module"):
        mods = sorted({f.split("/")[1] for f in files if len(f.split("/")) > 2 and "/src/" in f})
        for m in mods:
            kt = sum(1 for f in files if f.startswith(f"{area}/{m}/") and f.endswith(".kt"))
            # A module's GUIDE is a page in this node's directory, written by scaffold.sh with a
            # generated API reference. Linked from the same row as its file count so the table is the
            # one place a reader goes, rather than a count here and a link three sections down.
            guide = os.path.join(ROOT, OUT, area.lstrip("."), f"{m}.md")
            cell = f"{kt} Kotlin files"
            if os.path.isfile(guide):
                cell += f" — [guide]({area.lstrip('.')}/{m}.md)"
            rows.append((f"`{area}:{m}`", cell))
    elif kind == "automation":
        for d in sorted({f.split("/")[1] for f in files if len(f.split("/")) > 2}):
            n = sum(1 for f in files if f.startswith(f"{area}/{d}/") and f.endswith(".sh"))
            rows.append((f"`{area}/{d}/`", f"{n} shell scripts" if n else "no shell scripts"))
    elif kind == "deploy":
        # Pair each lane with its `desc` — fastlane's OWN documentation mechanism, which `fastlane list`
        # already renders. Reusing it means the page cannot describe a lane differently from the CLI.
        lanes = {}
        for f in files:
            if f.endswith(".rb") or f.endswith("Fastfile"):
                try:
                    with open(os.path.join(ROOT, f), encoding="utf-8", errors="replace") as fh:
                        src = fh.read()
                except OSError:
                    continue
                pending = ""
                for line in src.splitlines():
                    t = line.strip()
                    m = re.match(r"""^desc\s+["'](.+)["']\s*$""", t)
                    if m:
                        pending = m.group(1); continue
                    m = re.match(r"^(?:private_)?lane :([A-Za-z0-9_]+)", t)
                    if m:
                        lanes.setdefault(m.group(1), pending); pending = ""
                        continue
                    if t and not t.startswith("#"):
                        pending = ""
        for l in sorted(lanes):
            rows.append((f"`fastlane … {l}`", lanes[l]))
    elif kind == "ci":
        for f in sorted(f for f in files if "/workflows/" in f and f.endswith((".yml", ".yaml"))):
            name = ""
            try:
                with open(os.path.join(ROOT, f), encoding="utf-8", errors="replace") as fh:
                    m = re.search(r"^name:\s*(.+)$", fh.read(), re.M)
                    name = m.group(1).strip().strip("'\"") if m else ""
            except OSError:
                pass
            rows.append((f"`{f.split('/')[-1]}`", name))
    elif kind == "codegen":
        for d in sorted({f.split("/")[1] for f in files if len(f.split("/")) > 2}):
            rows.append((f"`{area}/{d}`", first_header_comment(f"{area}/{d}/README.md")))
    elif kind == "config-sot":
        for f in sorted(f for f in files if f.endswith(".yaml"))[:14]:
            rows.append((f"`{f}`", first_header_comment(f)))
    return rows[:24]

def sub_facts(area, child, files):
    """The facts a reader of THIS unit needs — chosen per area, because a deploy platform and a feature
    module are not described by the same table."""
    L = []
    rel = f"{area}/{child}"
    if area == "deployment":
        lanes = {}
        for f in files:
            if f.endswith((".rb",)) or f.endswith("Fastfile"):
                try: src = io.open(os.path.join(ROOT, f), encoding="utf-8", errors="replace").read()
                except OSError: continue
                pending = ""
                for line in src.splitlines():
                    t = line.strip()
                    m = re.match(r"""^desc\s+["'](.+)["']\s*$""", t)
                    if m: pending = m.group(1); continue
                    m = re.match(r"^(?:private_)?lane :([A-Za-z0-9_]+)", t)
                    if m: lanes.setdefault(m.group(1), pending); pending = ""
        if lanes:
            L += ["### Lanes", "", "| Lane | Purpose |", "|---|---|"]
            for k in sorted(lanes): L.append(f"| `{k}` | {lanes[k]} |")
            L.append("")
        meta = sorted({f.split("/")[2] for f in files if len(f.split("/")) > 3})
        if meta:
            L += ["### Contents", "", ", ".join(f"`{m}/`" for m in meta), ""]
    elif area == "scripts":
        rows = [(f, first_header_comment(f)) for f in sorted(files) if f.endswith(".sh")]
        if rows:
            L += ["### Scripts", "", "| Script | Purpose |", "|---|---|"]
            for f, p in rows[:30]:
                L.append(f"| `{f.split('/')[-1]}` | {p} |")
            L.append("")
            if len(rows) > 30: L.append(f"_…and {len(rows)-30} more._\n")
    elif area == "feature":
        kt = [f for f in files if f.endswith(".kt")]
        screens = sorted({f.split("/")[-1][:-3] for f in kt if f.endswith("Screen.kt")})
        vms     = sorted({f.split("/")[-1][:-3] for f in kt if f.endswith("ViewModel.kt")})
        L += [f"**{len(kt)} Kotlin files** — {len(screens)} screen(s), {len(vms)} ViewModel(s).", ""]
        if screens: L += ["Screens: " + ", ".join(f"`{x}`" for x in screens), ""]
        if vms:     L += ["ViewModels: " + ", ".join(f"`{x}`" for x in vms), ""]
        arch, doms = archetype_for(child, files)
        if doms:
            L += ["**Consumes:** " + ", ".join(f"`core/store/{d}`" for d in doms), ""]
        if arch:
            L += ["**Store5 archetype(s) exercised:** " + ", ".join(f"`{a}`" for a in arch) +
                  " — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build "
                  "against when an archetype loses its last showcase.", ""]
    elif area == "tools":
        ann = None
        for f in files:
            if f.endswith(".kt"):
                try: src = io.open(os.path.join(ROOT, f), encoding="utf-8", errors="replace").read()
                except OSError: continue
                m = re.search(r'ANN[A-Z_]*\s*=\s*"([^"]+)"', src)
                if m: ann = m.group(1); break
        if ann: L += [f"**Processes:** `@{ann.split('.')[-1]}` (`{ann}`)", ""]
    if not L:
        L += [f"{len(files)} tracked files.", ""]
    return L

def _archetype_domains():
    """domain → {archetypes}. STORE_ARCHETYPES.yaml declares showcases as `core/store/…/<domain>/impl/…`
    paths, so the archetype is keyed by the store DOMAIN, never by a feature name."""
    reg = os.path.join(ROOT, "core/store/STORE_ARCHETYPES.yaml")
    out = collections.defaultdict(set)
    if not os.path.isfile(reg):
        return out
    try:
        text = io.open(reg, encoding="utf-8", errors="replace").read()
    except OSError:
        return out
    cur = None
    for line in text.splitlines():
        m = re.match(r"^\s{2}([A-Z][A-Z0-9_]*):\s*$", line)
        if m:
            cur = m.group(1); continue
        if cur:
            for d in re.findall(r"core/store/src/commonMain/kotlin/kpt/core/store/([a-z0-9]+)/", line):
                out[d].add(cur)
    return out

ARCH_BY_DOMAIN = _archetype_domains()

def archetype_for(feature, files):
    """Which Store5 archetype(s) this feature exercises.

    Derived by two hops rather than a hardcoded table: the feature's IMPORTS name the `core.store` /
    `core.data` domain it consumes, and STORE_ARCHETYPES.yaml keys archetypes by that domain. A feature
    renamed or re-pointed at a different store therefore reports correctly with no edit here."""
    doms = set()
    for f in files:
        if not f.endswith(".kt"):
            continue
        try:
            src = io.open(os.path.join(ROOT, f), encoding="utf-8", errors="replace").read()
        except OSError:
            continue
        doms |= set(re.findall(r"import\s+kpt\.core\.(?:store|data)\.([a-z0-9]+)\.", src))
    arch = sorted({a for d in doms for a in ARCH_BY_DOMAIN.get(d, ())})
    return arch, sorted(doms)

def in_tree_docs(prefix, files):
    """Markdown living BESIDE the code under `prefix` — 216 such files exist in this repo (12,440 lines),
    and nothing in docs/ linked any of them.

    They are surfaced, not moved: a module's `README.md` and `CONSUMPTION.md` belong next to the module,
    where a consumer of that module reads them directly. Listed as PATHS rather than links because the
    site is served from docs/ — a relative link out of that root 404s in docsify while working on GitHub,
    so a path is the one form that is honest in both."""
    out = []
    for f in sorted(files):
        if not f.endswith(".md"):
            continue
        depth = f[len(prefix):].strip("/").count("/")
        if depth > 1:                      # deep in a source set — not a module-level doc
            continue
        title = ""
        try:
            for line in io.open(os.path.join(ROOT, f), encoding="utf-8", errors="replace"):
                if line.startswith("# "):
                    title = line[2:].strip(); break
        except OSError:
            pass
        out.append((f, title))
    return out

def sibling_guides(node_dir, exclude=()):
    """Authored pages living in this node's own directory. Listed by the generator so a guide dropped
    beside a page appears in it automatically — no index to remember to update."""
    d = os.path.join(ROOT, OUT, node_dir)
    if not os.path.isdir(d):
        return []
    out = []
    for f in sorted(os.listdir(d)):
        if f.endswith(".md") and f[:-3] not in exclude:
            full = os.path.join(d, f)
            title = f[:-3].replace("-", " ").replace("_", " ")
            try:
                for line in io.open(full, encoding="utf-8", errors="replace"):
                    if line.startswith("# "):
                        title = line[2:].strip().lstrip("`").rstrip("`"); break
            except OSError:
                pass
            out.append((f, title))
    return out

def render_sub(area, child, files):
    L = [f"# `{area}/{child}/`\n",
         f"> Part of [`{area}/`](../{area.lstrip('.')}.md)  ",
         f"> **Measured:** {len(files)} tracked files — {ext_summary(files)}\n"]
    L += sub_facts(area, child, files)
    intree = in_tree_docs(f"{area}/{child}", files)
    if intree:
        L.append("### Docs in the tree\n")
        L.append("| Path | |")
        L.append("|---|---|")
        for f, t in intree:
            L.append(f"| `{f}` | {t} |")
        L.append("")
    g = sibling_guides(os.path.join(area.lstrip("."), child))
    if g:
        L.append("### Guides\n")
        for f, t in g:
            L.append(f"- [{t}]({child}/{f})")
        L.append("")
    L.append(MARK)
    L.append("")
    return "\n".join(L)

def render(area, kind, files):
    L = []
    L.append(f"# `{area}/`\n")
    L.append(f"> **Kind:** {KIND_LABEL[kind]}  ")
    L.append(f"> **Measured:** {len(files)} tracked files — {ext_summary(files)}\n")
    subs = subdirs(area, files)
    if subs:
        L.append("## Shape\n")
        L.append("| Path | Files |")
        L.append("|---|---:|")
        for d, n in subs:
            label = d if d.startswith("(") else f"`{area}/{d}/`"
            L.append(f"| {label} | {n} |")
        L.append("")
    eps = entry_points(area, kind, files)
    if eps:
        head = {"kmp-modules": "Modules", "kmp-module": "Modules", "automation": "Script groups",
                "deploy": "Fastlane lanes", "ci": "Workflows", "codegen": "Processors",
                "config-sot": "Declared configuration"}.get(kind, "Entry points")
        L.append(f"## {head}\n")
        L.append("| | |")
        L.append("|---|---|")
        for a, b in eps:
            L.append(f"| {a} | {b} |")
        L.append("")
    subs = subsections(area, files)
    if subs:
        L.append("## Subsections\n")
        L.append("| Unit | Files | Page |")
        L.append("|---|---:|---|")
        for c, fs in subs:
            L.append(f"| `{area}/{c}/` | {len(fs)} | [{c}]({area.lstrip('.')}/{c}.md) |")
        L.append("")
    intree = in_tree_docs(area, files)
    if intree:
        L.append("## Docs in the tree\n")
        L.append("_Authored beside the code, where the module's own consumers read them._\n")
        L.append("| Path | |")
        L.append("|---|---|")
        for f, t in intree:
            L.append(f"| `{f}` | {t} |")
        L.append("")
    module_names = {x.split("/")[1] for x in files if len(x.split("/")) > 2} if area in MODULE_PAGED else set()
    g = sibling_guides(area.lstrip("."), exclude={c for c, _ in subs} | module_names)
    if g:
        L.append("## Guides\n")
        L.append("_Authored in depth, living in this area's own directory._\n")
        for f, t in g:
            L.append(f"- [{t}]({area.lstrip('.')}/{f})")
        L.append("")
    deep = DEEP_GUIDES.get(area, [])
    if deep:
        L.append("## Deeper reading\n")
        L.append("_This page is the map; these are the depth. Both are authored, and linking them is what")
        L.append("stops two descriptions of one thing from drifting apart._\n")
        for rel, title in deep:
            # `rel` is docs-root-relative; a tree page sits at docs/architecture/tree/, so ../../ reaches docs/.
            L.append(f"- [{title}](../../{rel})")
        L.append("")
    L.append(MARK)
    L.append("")
    return "\n".join(L)

changed = []
produced = set()   # every page this run writes — see the PROJECT_TREE count below
os.makedirs(os.path.join(ROOT, OUT), exist_ok=True)
for area in areas:
    files = by_area[area]
    kind = classify(area, files)
    for child, cfiles in subsections(area, files):
        spage = os.path.join(OUT, area.lstrip("."), f"{child}.md")
        produced.add(spage)
        sfull = os.path.join(ROOT, spage)
        os.makedirs(os.path.dirname(sfull), exist_ok=True)
        sgen = render_sub(area, child, cfiles)
        stail = ""
        if os.path.isfile(sfull):
            ex = io.open(sfull, encoding="utf-8").read()
            if MARK in ex:
                stail = ex.split(MARK, 1)[1]
        else:
            stail = ("\n\n## Significance\n\n"
                     "_Authored below this marker; regeneration never touches it._\n")
        snew = sgen + (stail.lstrip("\n") if stail.startswith("\n") else stail)
        sold = io.open(sfull, encoding="utf-8").read() if os.path.isfile(sfull) else None
        if sold != snew:
            changed.append(spage)
            if WRITE:
                io.open(sfull, "w", encoding="utf-8").write(snew)
    page = os.path.join(OUT, f"{area.lstrip('.')}.md")
    produced.add(page)
    full = os.path.join(ROOT, page)
    generated = render(area, kind, files)
    tail = ""
    if os.path.isfile(full):
        with open(full, encoding="utf-8") as fh:
            existing = fh.read()
        if MARK in existing:
            tail = existing.split(MARK, 1)[1]
        if existing == generated + tail.lstrip("\n") if False else False:
            pass
    else:
        # A NEW page gets the authored section as an explicit prompt, not filler prose. A stub reading
        # "TODO: describe this" is worse than no file — it looks like coverage and teaches nothing.
        tail = ("\n\n## Significance\n\n"
                "_Authored below this marker; regeneration never touches it. Say what this area is FOR,\n"
                "what breaks without it, and what a fork touches._\n")
    new = generated + tail.lstrip("\n") if tail.startswith("\n") else generated + tail
    old = open(full, encoding="utf-8").read() if os.path.isfile(full) else None
    if old != new:
        changed.append(page)
        if WRITE:
            with open(full, "w", encoding="utf-8") as fh:
                fh.write(new)

# ── PROJECT_TREE.md — the map, grouped so the repo reads as a shape rather than an alphabet ────────
# Generated for the same reason as the area pages: a hand-made index of 22 directories is stale the
# first time one is added. The GROUPING is authored (it encodes what belongs with what); the rows,
# counts and links are measured.
GROUPS = [
    ("Application shells", ["cmp-android", "cmp-ios", "cmp-desktop", "cmp-web", "cmp-shared", "cmp-navigation"]),
    ("Features",           ["feature"]),
    ("Core layers",        ["core", "core-base"]),
    ("Codegen",            ["tools", "build-logic"]),
    ("Configuration",      ["app-profile", "gradle", "secrets"]),
    ("Build, release, CI", ["deployment", ".github", "fastlane", "fastlane-config", "legal"]),
    ("Automation",         ["scripts", "sync", "tests"]),
]

def tree_page():
    have = {a.lstrip(".") for a in areas}
    L = ["# Project tree", "",
         "> Generated by `scripts/docs/tree-scaffold.sh` from `git ls-files`. Every top-level area of the",
         "> repository has a page: measured facts above the marker, authored significance below it.", ""]
    placed = set()
    for group, names in GROUPS:
        rows = [(a, len(by_area[a])) for a in names if a in by_area]
        if not rows:
            continue
        L += [f"## {group}", "", "| Area | Files | Page |", "|---|---:|---|"]
        for a, n in rows:
            placed.add(a)
            slug = a.lstrip(".")
            link = f"[`{a}/`](tree/{slug}.md)" if slug in have else "—"
            L.append(f"| `{a}/` | {n} | {link} |")
        L.append("")
    # Anything the authored grouping did not place still appears — a new directory must never be
    # invisible just because nobody has classified it yet.
    rest = sorted(set(by_area) - placed - set(SKIP))
    if rest:
        L += ["## Other", "", "| Area | Files | Page |", "|---|---:|---|"]
        for a in rest:
            slug = a.lstrip(".")
            link = f"[`{a}/`](tree/{slug}.md)" if slug in have else "_generated / vendored_"
            L.append(f"| `{a}/` | {len(by_area[a])} | {link} |")
        L.append("")
    skipped = sorted(a for a in SKIP if a in by_area)
    if skipped:
        L += ["## No page, and why", "",
              "Deliberately not documented as an area — listed so ABSENCE is distinguishable from",
              "oversight. A directory missing from a map reads the same as one nobody looked at.",
              "", "| Area | Files | Reason |", "|---|---:|---|"]
        for a in skipped:
            L.append(f"| `{a}/` | {len(by_area[a])} | {SKIP[a]} |")
        L.append("")
    return "\n".join(L)

tp = "docs/architecture/PROJECT_TREE.md"
# The `docs/` row counts this script's OWN output, so count the pages this run produces whether or not
# they existed when FILES was listed. Otherwise the count depends on what was on disk beforehand: right
# after a sync's strict mirror removed the fork's generated pages, the first refresh wrote 131 where the
# committed tree holds 150, and only a second run converged — a non-idempotent generator.
produced.add(tp)
by_area["docs"] = sorted(set(by_area.get("docs", [])) | produced)
tfull = os.path.join(ROOT, tp)
new_tree = tree_page()
old_tree = open(tfull, encoding="utf-8").read() if os.path.isfile(tfull) else None
if old_tree != new_tree:
    changed.append(tp)
    if WRITE:
        with open(tfull, "w", encoding="utf-8") as fh:
            fh.write(new_tree)

for p in changed:
    print(("  ✎ " if WRITE else "  Δ ") + p)
print(f"{'wrote' if WRITE else 'would change'} {len(changed)} page(s) across {len(areas)} areas")
sys.exit(0 if (WRITE or not changed) else 1)
PYEOF
