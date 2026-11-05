#!/usr/bin/env bash
# deployment/_shared/scripts/fork-identity.sh — a fork's identity, rendered at RUN time.
#
# deployment/ is template-owned and byte-identical in every fork: nothing (no syncForkConfig pass, no
# consumer) writes fork values into it. Files there that need a fork's identity — the Cloudflare
# Pages project name, the MSIX identity in Package.appxmanifest, the StoreBroker submission config —
# carry neutral placeholders, and the deploy step renders the real values into a BUILD output from
# gradle/fork.properties (the identity bridge derived from app-profile/). Before 2026-10-29
# syncForkConfig regex-rewrote those committed files in every fork, so each template sync overwrote
# them and the next sync rewrote them again.
#
#   fork-identity.sh get <key>                 one fork.properties value (empty when unset)
#   fork-identity.sh appx <template> <out>     Package.appxmanifest with this fork's MSIX identity
#   fork-identity.sh storebroker <out>         StoreBroker.config.json for this fork
#   fork-identity.sh msix-name                 the MSIX file name both of the above agree on
#
# gradle/fork.properties is gitignored; when it is missing (a fresh CI checkout) it is derived first,
# exactly as the quality gate does (ruby scripts/white-label/derive.rb).
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
# shellcheck source=../../../scripts/_shared/fork-props.sh
. "$ROOT/scripts/_shared/fork-props.sh"
if [ -z "${FORK_PROPERTIES:-}" ] && [ ! -f "$ROOT/gradle/fork.properties" ] && [ -f "$ROOT/scripts/white-label/derive.rb" ]; then
  (cd "$ROOT" && ruby scripts/white-label/derive.rb >/dev/null 2>&1) || true
fi
FORK_PROPERTIES="$(fp_file "$ROOT")" || { echo "fork-identity: no gradle/fork.properties (run ./gradlew syncForkConfig)" >&2; exit 1; }
export FORK_PROPERTIES

get() { fp_get "$1" || true; }
msix_name() { local p; p="$(get project.name)"; echo "${p:-app}.msix"; }

# Replace one attribute/element value in an XML stream, only when the new value is non-empty — the
# same targeted substitutions syncForkConfig used to apply to the committed file.
xml_sub() {  # <perl-regex-with-$1-$2-groups> <value>
  [ -n "$2" ] || { cat; return; }
  VAL="$2" perl -0777 -pe "s{$1}{\$1\$ENV{VAL}\$2}g"
}

case "${1:-}" in
  get) get "${2:?key}" ;;
  msix-name) msix_name ;;
  appx)
    in="${2:?template}"; out="${3:?out}"; mkdir -p "$(dirname "$out")"
    xml_sub '(<Identity\b[\s\S]*?\bName=")[^"]*(")' "$(get windows.msix.identity.name)" < "$in" \
      | xml_sub '(<Identity\b[\s\S]*?\bPublisher=")[^"]*(")' "$(get windows.msix.identity.publisher)" \
      | xml_sub '(<PublisherDisplayName>)[^<]*(</PublisherDisplayName>)' "$(get windows.msix.publisher.display.name)" \
      | xml_sub '(\bDescription=")[^"]*(")' "$(get app.display.name)" > "$out" ;;
  storebroker)
    out="${2:?out}"; mkdir -p "$(dirname "$out")"
    app_id="$(get store.windows.ms.app.id)"; app_id="${app_id:-\${ENV:MS_APP_ID\}}"
    mode="$(get store.windows.ms.publish.mode)"; vis="$(get store.windows.ms.visibility)"
    cat > "$out" <<JSON
{
  "_comment": "StoreBroker submission config — rendered by deployment/_shared/scripts/fork-identity.sh from gradle/fork.properties.",
  "appId": "$app_id",
  "flightId": null,
  "packagePath": "cmp-desktop/build/compose/binaries/main-release/msix/$(msix_name)",
  "targetPublishMode": "${mode:-Manual}",
  "visibility": "${vis:-Public}"
}
JSON
    ;;
  *) echo "usage: fork-identity.sh {get <key>|appx <template> <out>|storebroker <out>|msix-name}" >&2; exit 2 ;;
esac
