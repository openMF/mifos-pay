# `scripts/docs/`

> Part of [`scripts/`](../scripts.md)  
> **Measured:** 18 tracked files — 12× `.sh`, 4× `.awk`, 1× `.kt`, 1× `.tsv`

### Scripts

| Script | Purpose |
|---|---|
| `_kdoc-lib.sh` | the ONE upward walk from a declaration to the line that closes its KDoc. |
| `api-docs-gen.sh` | scan-bounded: pure bash + grep/find/sed over one template module tree (RULE-CI-001). Never idea-layer. |
| `api-index-gen.sh` | scan-bounded: reads only the GENERATED api-docs blocks under docs/ (RULE-CI-001). Never idea-layer. |
| `doc-audit.sh` | is the documentation actually complete? One command, seven checks. |
| `doc-refs.sh` | does the AUTHORED prose still name files that exist? |
| `doc-scan.sh` | one full-project documentation scan, every language. |
| `kdoc-coverage.sh` | who is missing KDoc, and where. |
| `module-hash.sh` | scan-bounded: pure bash + find/sha256 over one module tree (RULE-CI-001). Never idea-layer. |
| `refresh.sh` | ONE entry point that brings the whole docs tree back in step with source. |
| `scaffold.sh` | create/refresh the docs/architecture tree FROM DISK. |
| `workflow.sh` | GitHub Actions workflows and their dispatch inputs. |
| `tree-scaffold.sh` | a docs page for EVERY top-level area of the project, shaped like the tree. |

<!-- tree-scaffold:end -->
## Significance

The documentation pipeline — the scripts that make this page, and every other generated page, derive
from source.

`refresh.sh` is the one entry point and runs five producers in dependency order; `--check` is what a PR
gate runs. `doc-scan.sh` measures documentation coverage across five languages; `doc-refs.sh` asks
whether the authored prose still names files that exist. `_kdoc-lib.sh` exists because three copies of
the same "walk up to a declaration's KDoc" had diverged, and all three stopped at the `)` closing a
multi-line annotation — so four documented factories printed "no KDoc at source" in the published
reference.

`tests/` holds deliberately-undocumented Kotlin: the scanner has to be provable, and a fixture that
exists to be found cannot also be documented.
