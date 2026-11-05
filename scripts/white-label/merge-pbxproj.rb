#!/usr/bin/env ruby
# frozen_string_literal: true
#
# merge-pbxproj.rb — a 3-way merge for Xcode project files that produces NO conflict markers.
#
# WHY A DEDICATED MERGER
# `project.pbxproj` is the one file in the sync surface where a line-based 3-way is structurally
# wrong. It is not a document, it is a SERIALIZED OBJECT GRAPH: ~50 objects keyed by stable 24-hex
# UUIDs, where "add a target" rewrites a UUID list three levels away from the object it adds. git
# merge-file sees adjacent lines in a container array and calls it a conflict; worse, a merge marker
# inside a pbxproj makes the project UNOPENABLE, so a human cannot even use Xcode to resolve it.
#
# WHY IT IS TRACTABLE ANYWAY
# The UUIDs are stable ACROSS THE FORK BOUNDARY. Measured template ⋈ mbs/cappy 2026-09-10:
# 36 and 52 objects, **33 UUIDs shared** — a fork's project descends from the template's, so the
# same object carries the same key on both sides. That turns an intractable text merge into an
# ordinary keyed 3-way, and every real difference falls into exactly three mechanical classes:
#
#   1. UUID-LIST additions        PBXProject.targets / .packageReferences, PBXGroup.children,
#                                 PBXSourcesBuildPhase.files, PBXNativeTarget.buildPhases …
#                                 Template added XCLocalSwiftPackageReference (the SwiftPM
#                                 migration); the fork added a CappyWidgets target. Different
#                                 elements of the same array → UNION. No judgement needed.
#   2. buildSettings KEY additions  the fork added CODE_SIGN_ENTITLEMENTS; the template adds its
#                                 own keys over time → per-key union.
#   3. buildSettings VALUE conflict  exactly one in the measured pair:
#                                 DEVELOPMENT_TEAM "$(TEAM_ID)" (template placeholder) vs
#                                 L432S2FZP5 (the fork's real team). Fork identity → fork wins.
#
# So this is not heuristic reconstruction — it is a keyed 3-way with a union rule for arrays and a
# declared precedence for the identity keys. Anything it CANNOT decide is reported and the merge
# FAILS loudly, because a silently-wrong pbxproj costs more than a stopped sync.
#
# The `xcodeproj` gem does the parsing and (critically) the WRITING — it is already resolved in
# Gemfile.lock as a fastlane transitive (1.28.1), so this adds no dependency. Writing through the
# gem is what keeps Xcode's canonical formatting and the `/* comment */` annotations intact.
#
#   usage: merge-pbxproj.rb --ours <f> --base <f> --theirs <f> --out <f> [--report]
#   exit 0 merged · 1 undecidable difference (nothing written) · 2 usage/IO error

require 'set'
begin
  require 'xcodeproj'
rescue LoadError
  warn "❌ merge-pbxproj: the `xcodeproj` gem is not available to #{RUBY_VERSION} at #{RbConfig.ruby}."
  warn "   It ships as a fastlane transitive, so it is usually already present. Either:"
  warn "     gem install xcodeproj          # for the pinned interpreter (.ruby-version)"
  warn "     bundle install && bundle exec ruby scripts/white-label/merge-pbxproj.rb …"
  warn "   Exit code 2 specifically means GEM MISSING — sync-dirs.sh retries under bundler on it."
  exit 2
end

# ── Identity build settings: PREFER THE INDIRECTION, never a literal ────────
# The first version of this list said "fork wins" for these keys, on the reasoning that they are
# per-fork deployment identity. That is right about the VALUE and wrong about the LOCATION, and
# picking the fork's literal would have entrenched the exact defect the template just finished
# removing.
#
# None of these is authored in the pbxproj at all. Each is an INDIRECTION resolved from
# Config.xcconfig, which syncForkConfig generates from the app-profile SoT:
#
#   app-profile/platforms/apple/apple.yaml#apple.team_id
#     → gradle/fork.properties#apple.team.id        (derive.rb, AppProfile::MAP)
#     → Config.xcconfig#TEAM_ID                     (SyncForkConfigPlugin)
#     → $(TEAM_ID)                                  ← what the pbxproj must contain
#
# So a LITERAL here is a defect no matter which side wrote it — syncForkConfig never touches
# project.pbxproj, so nothing downstream can correct one. This template shipped
# `DEVELOPMENT_TEAM = L432S2FZP5` to every fork through two commits and it survived until
# 2026-09-10; `PBX-4` in product-health/checks/ios-pbxproj-identity.sh now blocks the recurrence.
#
# The merge rule follows from that: whichever side holds the `$(…)` indirection wins. It is
# direction-agnostic — it repairs a fork that hardcoded a value AND declines to import a literal
# the template leaked — and it needs no notion of "who owns this", because the answer is always
# "app-profile does".
XCCONFIG_ROUTED_KEYS = %w[
  DEVELOPMENT_TEAM
  PRODUCT_BUNDLE_IDENTIFIER
  PROVISIONING_PROFILE_SPECIFIER
  CODE_SIGN_ENTITLEMENTS
  CODE_SIGN_IDENTITY
].freeze

# An xcconfig indirection: "$(TEAM_ID)", "$(inherited)", "$(APP_BUNDLE_ID).widgets", …
def indirection?(v)
  v.is_a?(String) && v.include?('$(')
end

# Attributes whose value is an ORDERED LIST OF UUIDs (or of plain strings, for a few settings).
# A change on either side is an insertion into a shared array, never a replacement.
LIST_ATTRS = %w[
  children files targets buildPhases buildRules dependencies
  packageReferences packageProductDependencies buildConfigurations
  projectReferences knownRegions
].freeze

# Print a message and exit with the given code.
def die(msg, code = 2)
  warn "❌ merge-pbxproj: #{msg}"
  exit code
end

opts = { report: false }
args = ARGV.dup
until args.empty?
  case (a = args.shift)
  when '--ours'   then opts[:ours]   = args.shift
  when '--base'   then opts[:base]   = args.shift
  when '--theirs' then opts[:theirs] = args.shift
  when '--out'    then opts[:out]    = args.shift
  when '--report' then opts[:report] = true
  when '-h', '--help'
    puts 'usage: merge-pbxproj.rb --ours <f> --base <f> --theirs <f> --out <f> [--report]'
    exit 0
  else die("unknown arg: #{a}")
  end
end
%i[ours base theirs out].each { |k| opts[k] || die("missing --#{k}") }
%i[ours base theirs].each { |k| File.file?(opts[k]) || die("no such file: #{opts[k]}") }

# Read a pbxproj and return its plist plus the `objects` dict.
# Fails loudly when `objects` is absent — that means the file is not a pbxproj, and merging
# it blind would produce a project Xcode cannot open.
def read_objects(path)
  plist = Xcodeproj::Plist.read_from_path(path)
  die("#{path} has no `objects` dict — is it really a pbxproj?") unless plist.is_a?(Hash) && plist['objects']
  [plist, plist['objects']]
end

ours_plist,   ours   = read_objects(opts[:ours])
base_plist,   base   = read_objects(opts[:base])
theirs_plist, theirs = read_objects(opts[:theirs])

conflicts = []
notes     = []

# ── merge one ordered UUID list ─────────────────────────────────────────────
# Union, in a deterministic order: the fork's order first (its file is the one being updated, so
# its layout is the one a human recognises), then whatever the template added. An element both
# sides DELETED stays deleted; one deleted by only the template but kept by the fork survives,
# because the fork may have re-parented it.
def merge_list(o, b, t)
  o = Array(o); b = Array(b); t = Array(t)
  ob = Set.new(o); bb = Set.new(b)
  deleted_by_ours = bb - ob
  result = o.dup
  # anything the template has that the fork does not, and the fork did not delete on purpose
  t.each { |e| result << e unless result.include?(e) || deleted_by_ours.include?(e) }
  result
end

# Drop every list element that names an object the merge did not keep.
#
# THIS IS NOT DEFENCE-IN-DEPTH, IT IS THE ONLY CORRECT PLACE FOR THE DECISION.
# Whether a template-side deletion propagates is decided per OBJECT, using base/ours/theirs. A list
# cannot re-derive that from its own elements — they are bare UUID strings with no history — so an
# earlier attempt to filter inside merge_list wrote a guard that could never fire (it tested whether
# an element of `ours` was in `ours`). The result: `Configs/iOSApp.xcconfig`, deleted upstream and
# untouched by the fork, had its object dropped while the PBXGroup child reference survived — a
# DANGLING REFERENCE. The gem discards those on load with a warning, so the merge "succeeded" while
# silently losing whatever the reference pointed at.
#
# Pruning against the merged object set makes the graph consistent BY CONSTRUCTION: exactly one
# place decides what exists, and lists follow.
def prune_dangling!(merged, list_attrs, notes)
  live = Set.new(merged.keys)
  pruned = 0
  merged.each do |uuid, obj|
    next unless obj.is_a?(Hash)
    list_attrs.each do |k|
      v = obj[k]
      next unless v.is_a?(Array)
      kept = v.reject { |e| e.is_a?(String) && e =~ /\A[0-9A-F]{24}\z/ && !live.include?(e) }
      next if kept.size == v.size
      pruned += v.size - kept.size
      obj[k] = kept
      notes << "pruned #{v.size - kept.size} dangling ref(s) from #{obj['isa']} #{uuid}.#{k}"
    end
  end
  pruned
end

# ── merge a buildSettings-style hash, per key ───────────────────────────────
def merge_settings(o, b, t, ctx, conflicts)
  o ||= {}; b ||= {}; t ||= {}
  return t unless o.is_a?(Hash) && t.is_a?(Hash)
  out = {}
  (o.keys | t.keys).each do |k|
    ov, bv, tv = o[k], b[k], t[k]
    # ── xcconfig-routed keys are decided FIRST, ahead of the generic 3-way ──
    # Ordering is load-bearing. The ordinary rules ask WHO CHANGED IT; for these keys that is the
    # wrong question, because a literal is a defect regardless of who wrote it. Placed after
    # `tv == bv → take ours`, a fork that hardcoded its own team id kept the literal on every sync
    # (the template's side had not moved, so "only the fork moved" fired and won) — measured: 14
    # literals survived. Deciding on the VALUE's shape rather than on authorship repairs it.
    if o.key?(k) && t.key?(k) && XCCONFIG_ROUTED_KEYS.include?(k) &&
       indirection?(ov) != indirection?(tv)
      out[k] = indirection?(ov) ? ov : tv
      next
    end
    if !o.key?(k)            then out[k] = tv          # template added it
    elsif !t.key?(k)
      # Template removed it. Keep it only if the fork changed it (deliberate fork setting);
      # otherwise honour the template's removal.
      out[k] = ov if ov != bv
    elsif ov == tv           then out[k] = ov
    elsif ov == bv           then out[k] = tv          # only the template moved
    elsif tv == bv           then out[k] = ov          # only the fork moved
    else
      conflicts << "#{ctx}: buildSettings[#{k}] fork=#{ov.inspect} template=#{tv.inspect} (base=#{bv.inspect})"
      out[k] = ov
    end
  end
  out
end

# 3-way merge one pbxproj object. A key present in theirs and absent from base is an upstream
# addition and is taken; a key both sides changed is reported as a conflict rather than guessed.
def merge_object(uuid, o, b, t, conflicts)
  b ||= {}
  isa = t['isa'] || o['isa']
  ctx = "#{isa} #{uuid}"
  out = {}
  (o.keys | t.keys).each do |k|
    ov, bv, tv = o[k], b[k], t[k]
    if LIST_ATTRS.include?(k) && (ov.is_a?(Array) || tv.is_a?(Array))
      out[k] = merge_list(ov, bv, tv)
    elsif k == 'buildSettings'
      out[k] = merge_settings(ov, bv, tv, ctx, conflicts)
    elsif !o.key?(k)  then out[k] = tv
    elsif !t.key?(k)  then out[k] = ov if ov != bv
    elsif ov == tv    then out[k] = ov
    elsif ov == bv    then out[k] = tv
    elsif tv == bv    then out[k] = ov
    else
      conflicts << "#{ctx}: #{k} fork=#{ov.inspect} template=#{tv.inspect} (base=#{bv.inspect})"
      out[k] = ov
    end
  end
  out.compact
end

merged = {}
all = ours.keys | theirs.keys | base.keys
added_t = added_o = deleted = merged_both = 0

all.each do |uuid|
  o, b, t = ours[uuid], base[uuid], theirs[uuid]
  if o && t
    if o == t then merged[uuid] = o
    else merged[uuid] = merge_object(uuid, o, b, t, conflicts); merged_both += 1
    end
  elsif t && !o
    # Present upstream, absent in the fork: template ADDED it (not in base) → take it.
    # In base and gone from the fork → the fork deleted it deliberately → stay deleted.
    if b.nil? then merged[uuid] = t; added_t += 1 else deleted += 1 end
  elsif o && !t
    # Present in the fork, absent upstream: fork ADDED it → keep.
    # In base and gone upstream → template deleted it; honour that only if the fork did not touch it.
    if b.nil? then merged[uuid] = o; added_o += 1
    elsif o == b then deleted += 1
    else merged[uuid] = o
         notes << "kept #{o['isa']} #{uuid} — template deleted it but the fork had modified it"
    end
  end
end

pruned = prune_dangling!(merged, LIST_ATTRS, notes)

# ── INVARIANT: no dangling references may reach the writer ──────────────────
# This is not redundant with prune_dangling!, it is what makes the pruning TRUSTWORTHY. The gem's
# writer silently DISCARDS a reference to an unknown UUID (it prints an `attempted to initialize an
# object with an unknown UUID … being discarded` warning to stderr and carries on), so a merge that
# leaves one still produces a well-formed file that re-opens, validates, and passes every downstream
# check — while having quietly dropped whatever the reference pointed at. Post-hoc inspection of the
# OUTPUT can therefore never detect this class; only asserting before the write can.
dangling = []
live = Set.new(merged.keys)
merged.each do |uuid, obj|
  next unless obj.is_a?(Hash)
  LIST_ATTRS.each do |k|
    Array(obj[k]).each do |e|
      next unless e.is_a?(String) && e =~ /\A[0-9A-F]{24}\z/
      dangling << "#{obj['isa']} #{uuid}.#{k} → #{e}" unless live.include?(e)
    end
  end
end
unless dangling.empty?
  warn "❌ merge-pbxproj: #{dangling.size} dangling reference(s) survived pruning — refusing to write."
  dangling.first(10).each { |d| warn "     #{d}" }
  warn "   The gem would DISCARD these on load and the file would still look valid; that silent"
  warn "   loss is the bug this invariant exists to stop. prune_dangling! is not doing its job."
  exit 1
end

if conflicts.any?
  warn "❌ merge-pbxproj: #{conflicts.size} difference(s) this merger will not decide:"
  conflicts.each { |c| warn "     #{c}" }
  warn "   Nothing was written. Resolve in app-profile / the contract, or extend FORK_IDENTITY_KEYS"
  warn "   if this is a per-fork identity setting the template can only placeholder."
  exit 1
end

# Root keys (archiveVersion / objectVersion / rootObject / classes): template wins — they are the
# file-format envelope and Xcode version marker, never fork content.
out_plist = theirs_plist.dup
out_plist['objects'] = merged

Xcodeproj::Plist.write_to_path(out_plist, opts[:out])

# Round-trip through the full object model. This is the real validation: the gem refuses to open a
# graph with a dangling UUID reference, so a union that produced an inconsistent project fails HERE
# rather than in Xcode on someone's machine. It also rewrites the file in Xcode's canonical form.
begin
  dir = File.dirname(opts[:out])
  if File.basename(dir).end_with?('.xcodeproj')
    proj = Xcodeproj::Project.open(dir)
    proj.save
    notes << "validated: #{proj.objects.count} objects, targets #{proj.targets.map(&:name).join(', ')}"
  end
rescue StandardError => e
  warn "❌ merge-pbxproj: merged graph failed to re-open (#{e.class}: #{e.message})"
  warn "   The output is inconsistent — treat this as a FAILED merge, do not commit it."
  exit 1
end

if opts[:report]
  warn "  pbxproj 3-way: +#{added_t} from template · +#{added_o} kept from fork · " \
       "#{merged_both} objects merged · #{deleted} deletions propagated · #{pruned} refs pruned"
  notes.each { |n| warn "     #{n}" }
end
exit 0
