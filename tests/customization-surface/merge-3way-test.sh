#!/usr/bin/env bash
# Fixture test for cs_merge_3way — proves the AndroidManifest permission-loss class
# is closed: a fork's added <uses-permission> survives a template manifest update.
set -uo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
READER="$HERE/../../scripts/customization-surface.sh"
# shellcheck source=/dev/null
source "$READER"

WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
fail=0; pass() { echo "  ✅ $1"; }; bad() { echo "  ❌ $1"; fail=1; }

manifest() { # $1 = extra permission lines
  cat <<XML
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
$1    <application android:label="@string/app_name" />
</manifest>
XML
}

echo "── Case A: additive union (fork perms + template perms) ──"
manifest '' > "$WORK/base.xml"
# ours = fork added RECORD_AUDIO + FOREGROUND_SERVICE_MICROPHONE
manifest '    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />
' > "$WORK/ours.xml"
# theirs = template added POST_NOTIFICATIONS
manifest '    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
' > "$WORK/theirs.xml"

# The loss class: a BLIND copy of theirs would drop the fork's RECORD_AUDIO.
if grep -q RECORD_AUDIO "$WORK/theirs.xml"; then
  bad "precondition: theirs unexpectedly already has RECORD_AUDIO"
else
  pass "precondition: blind-copy(theirs) would DROP fork's RECORD_AUDIO (the loss class)"
fi

# manifest-union strategy (semantic union, not textual — additions at the same
# insertion point must NOT spurious-conflict).
cs_merge manifest-union "$WORK/ours.xml" "$WORK/base.xml" "$WORK/theirs.xml" "$WORK/out.xml"
rc=$?
[ "$rc" -eq 0 ] && pass "merge returned clean (rc=0)" || bad "merge rc=$rc (expected 0)"
grep -q RECORD_AUDIO "$WORK/out.xml"                  && pass "fork RECORD_AUDIO preserved"            || bad "RECORD_AUDIO lost"
grep -q FOREGROUND_SERVICE_MICROPHONE "$WORK/out.xml" && pass "fork FOREGROUND_SERVICE_MICROPHONE preserved" || bad "FOREGROUND_SERVICE_MICROPHONE lost"
grep -q POST_NOTIFICATIONS "$WORK/out.xml"            && pass "template POST_NOTIFICATIONS picked up"  || bad "template POST_NOTIFICATIONS missing"
grep -q '<<<<<<<' "$WORK/out.xml"                     && bad "unexpected conflict markers in clean merge" || pass "no conflict markers"

echo "── Case B: true conflict surfaces (same line changed both sides) ──"
printf '<application android:label="BASE" />\n'   > "$WORK/b2.xml"
printf '<application android:label="FORK" />\n'   > "$WORK/o2.xml"
printf '<application android:label="UPSTREAM" />\n'> "$WORK/t2.xml"
cs_merge_3way "$WORK/o2.xml" "$WORK/b2.xml" "$WORK/t2.xml" "$WORK/out2.xml"
rc=$?
[ "$rc" -eq 1 ] && pass "conflict returned rc=1 (surfaced, not silently shipped)" || bad "conflict rc=$rc (expected 1)"
grep -q '<<<<<<<' "$WORK/out2.xml" && pass "conflict markers present for review" || bad "conflict markers missing"

echo "── Case D: manifest-union keeps fork additions that are NOT permissions ──"
# The permission-only union used to drop every <queries>, <meta-data> and intent filter the fork added.
cat > "$WORK/b4.xml" <<'XML'
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <application android:label="@string/app_name">
        <activity android:name=".MainActivity" />
    </application>
</manifest>
XML
sed 's#    <application#    <queries><package android:name="com.whatsapp" /></queries>\n    <application#; s#        <activity android:name=".MainActivity" />#        <meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" android:value="x" />\n        <activity android:name=".MainActivity" />#' "$WORK/b4.xml" > "$WORK/o4.xml"
sed 's#    <uses-permission android:name="android.permission.INTERNET" />#&\n    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />#' "$WORK/b4.xml" > "$WORK/t4.xml"
cs_merge manifest-union "$WORK/o4.xml" "$WORK/b4.xml" "$WORK/t4.xml" "$WORK/out4.xml"; rc=$?
[ "$rc" -eq 0 ] && pass "clean merge (rc 0)" || bad "rc=$rc"
grep -q 'com.whatsapp' "$WORK/out4.xml"           && pass "fork <queries> kept"            || bad "fork <queries> dropped"
grep -q 'ads.APPLICATION_ID' "$WORK/out4.xml"     && pass "fork <meta-data> kept"          || bad "fork <meta-data> dropped"
grep -q 'POST_NOTIFICATIONS' "$WORK/out4.xml"     && pass "template permission picked up"  || bad "template permission missing"

echo "── Case E: a conflict that would lose a fork line is surfaced, never shipped ──"
sed 's#android:label="@string/app_name"#android:label="FORK"#' "$WORK/o4.xml" > "$WORK/o5.xml"
sed 's#android:label="@string/app_name"#android:label="UPSTREAM"#' "$WORK/t4.xml" > "$WORK/t5.xml"
cs_merge manifest-union "$WORK/o5.xml" "$WORK/b4.xml" "$WORK/t5.xml" "$WORK/out5.xml"; rc=$?
[ "$rc" -eq 1 ] && pass "rc=1 — the conflict is reported" || bad "rc=$rc (a fork line was silently dropped)"
grep -q '<<<<<<<' "$WORK/out5.xml" && grep -q 'com.whatsapp' "$WORK/out5.xml" \
  && pass "conflict markers + fork lines present for review" || bad "fork lines lost or no markers"

echo "── Case F: the engine refuses a 'clean' merge that drops a line the fork added ──"
printf 'a\nb\n'            > "$WORK/b6.txt"
printf 'a\nFORK\nb\n'      > "$WORK/o6.txt"
printf 'a\nb\nTPL\n'       > "$WORK/kept6.txt";  printf 'a\nFORK\nb\nTPL\n' >> "$WORK/kept6.txt"
printf 'a\nb\nTPL\n'       > "$WORK/lost6.txt"
cs_fork_lines_kept "$WORK/o6.txt" "$WORK/b6.txt" "$WORK/kept6.txt" && pass "fork line present → invariant holds" || bad "false alarm"
cs_fork_lines_kept "$WORK/o6.txt" "$WORK/b6.txt" "$WORK/lost6.txt" && bad "dropped fork line not detected" || pass "dropped fork line detected"
ENGINE="$HERE/../../scripts/white-label/sync-dirs.sh"
awk '/cs_match_g "\$_mf"/{p=1} p{print} p && /THREE outcomes, not two/{exit}' "$ENGINE" | grep -q 'cs_fork_lines_kept "\$_o" "\$_b" "\$_mf"' \
  && pass "engine checks the fork-line invariant after every per-file line merge" \
  || bad "engine ships line merges without the fork-line invariant"

echo "── Case C: lib-integrate.properties keeps the fork's local links (properties-3way) ──"
# /lib-integrate flips `<lib>.local=true` and adds the fork's own libraries; the template adds rows.
printf 'a.local=false\na.path=../a\n'                    > "$WORK/b3.properties"
printf 'a.local=true\na.path=../a\nmine.local=true\n'    > "$WORK/o3.properties"
printf 'a.local=false\na.path=../a\nnewlib.local=false\n' > "$WORK/t3.properties"
cs_merge_properties "$WORK/o3.properties" "$WORK/b3.properties" "$WORK/t3.properties" "$WORK/out3.properties"
grep -qx 'a.local=true' "$WORK/out3.properties"       && pass "fork's local-source link kept"       || bad "fork's a.local=true reset"
grep -qx 'mine.local=true' "$WORK/out3.properties"    && pass "fork-added library kept"             || bad "fork-added library dropped"
grep -qx 'newlib.local=false' "$WORK/out3.properties" && pass "template's new library row added"    || bad "template row missing"
[ "$( (cd "$HERE/../.." && bash "$READER" resolve lib-integrate.properties) | cut -f2)" = merge ] \
  && pass "contract: lib-integrate.properties is merge-owned" || bad "contract: lib-integrate.properties not merge-owned"

echo
if [ "$fail" -eq 0 ]; then echo "✅ merge-3way: all assertions passed"; exit 0
else echo "❌ merge-3way: failures above"; exit 1; fi
