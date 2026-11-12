#!/usr/bin/env ruby
# frozen_string_literal: true
#
# merge-plist.rb — key-level 3-way union for Info.plist / *.entitlements.
#
# WHY NOT git merge-file
# These files are a flat top-level <dict> where template and fork each add their OWN keys. The
# additions land at the same place in the text — just inside <dict> — so a line merge can report a
# conflict for changes that do not overlap in meaning.
#
# Measured on mbs/cappy ⋈ template 2026-09-10, against each file's REAL ancestor:
#
#   iosApp.entitlements  ADD/ADD — absent at the fork's base b97eb73d (the template added its own
#                        in 99083ed2, the fork added its own separately). No ancestor, so a line
#                        merge CONFLICTS, and the result does not even parse:
#                        `plutil: Encountered unexpected character = on line 15`. The build breaks.
#                        This file is why the strategy exists.
#   Info.plist           has a real ancestor, and a line merge is currently CLEAN — 0 conflicts,
#                        CFBundleURLTypes preserved, plutil OK. It is routed here anyway because it
#                        is the same flat-dict shape and one template key added near the fork's
#                        block turns it into the entitlements case. Correct today, robust tomorrow —
#                        not a fix for a present breakage, and this comment should not imply it is.
#
# (An earlier draft of this header claimed BOTH files were add/add with 2 conflicts each. That came
# from a broken measurement — the base was extracted with `"$SHA:cmp-ios/…"` in zsh, where `:c` is
# eaten as a modifier, so every `git show` silently returned nothing and every file looked add/add.
# Quote it `"${SHA}:cmp-ios/…"`.)
#
# The keys involved are genuinely disjoint and both are required:
#   template  keychain-access-groups          core-base/datastore's KeychainSettings needs it, or
#                                             iOS fails errSecMissingEntitlement (-34018) at launch
#   fork      com.apple.security.application-groups   the WidgetKit shared container
#   fork      CFBundleURLTypes                the OAuth redirect scheme — delete it and sign-in
#                                             silently never returns to the app
# Neither side may win. The merge is a UNION.
#
# WHY TEXT-LEVEL RATHER THAN parse-and-rewrite
# Round-tripping through a plist parser is easier and loses every comment. That is not cosmetic
# here: the template's entitlements carries 14 lines explaining WHY the keychain group is required
# and what happens without it — exactly the knowledge a fork needs when it later edits the file.
# So this merges at the text level, treating each key and its value element (plus the comments
# attached above it) as an opaque block. Comments survive because they are never re-serialized.
#
#   usage: merge-plist.rb --ours <f> --base <f|-> --theirs <f> --out <f> [--report]
#          --base -   there is no common ancestor (add/add); union both sides
#   exit 0 merged · 1 undecidable difference (nothing written) · 2 usage/IO error

def die(msg, code = 2)
  warn "❌ merge-plist: #{msg}"
  exit code
end

opts = { report: false }
args = ARGV.dup
until args.empty?
  case (a = args.shift)
  when '--ours' then opts[:ours] = args.shift
  when '--base' then opts[:base] = args.shift
  when '--theirs' then opts[:theirs] = args.shift
  when '--out' then opts[:out] = args.shift
  when '--report' then opts[:report] = true
  when '-h', '--help'
    puts 'usage: merge-plist.rb --ours <f> --base <f|-> --theirs <f> --out <f> [--report]'; exit 0
  else die("unknown arg: #{a}")
  end
end
%i[ours base theirs out].each { |k| opts[k] || die("missing --#{k}") }

# ── split a plist into: header (through the opening top-level <dict>), ordered key blocks, footer ──
# A block is [key_name, text] where text includes any comments/blank lines immediately above the
# <key> line — those comments document that key and must travel with it.
def split_plist(src)
  lines = src.lines
  dict_at = lines.index { |l| l.strip == '<dict>' }
  return [nil, nil, nil] unless dict_at

  depth = 0; close_at = nil
  lines.each_with_index do |l, i|
    next if i < dict_at
    s = l.strip
    depth += 1 if s == '<dict>'
    if s == '</dict>'
      depth -= 1
      if depth.zero? then close_at = i; break end
    end
  end
  return [nil, nil, nil] unless close_at

  header = lines[0..dict_at].join
  footer = lines[close_at..].join
  body   = lines[(dict_at + 1)...close_at]

  # Net element-depth change contributed by one line, ignoring comments, declarations and
  # self-closing tags. Used to find where a multi-line value element ends.
  def_depth = lambda do |line|
    text = line.gsub(/<!--.*?-->/m, '')
    d = 0
    text.scan(/<[^>]*>/).each do |tag|
      next if tag.start_with?('<?', '<!')
      if tag.start_with?('</') then d -= 1
      elsif tag.end_with?('/>') then d += 0
      else d += 1
      end
    end
    d
  end

  blocks = []
  pending = []          # comments / blank lines waiting to attach to the next key
  in_comment = false
  i = 0
  while i < body.length
    line = body[i]

    # ── comment state ────────────────────────────────────────────────────
    # A <key> inside a comment is DOCUMENTATION, not a key. The template's entitlements shows
    # `<key>com.apple.developer.applesignin</key>` inside its explanatory block as an example for
    # forks adding Sign in with Apple; parsing that as a real key invented an entitlement the file
    # does not grant and left the real one's <array> orphaned.
    if in_comment
      pending << line
      in_comment = false if line.include?('-->')
      i += 1
      next
    end
    if line =~ /<!--/ && !line.include?('-->')
      pending << line; in_comment = true; i += 1; next
    end
    if line.strip.start_with?('<!--') || line.strip.empty?
      pending << line; i += 1; next
    end

    # ── a real key ───────────────────────────────────────────────────────
    if (m = line.match(%r{<key>(.*?)</key>}))
      key = m[1]
      chunk = pending.dup + [line]
      pending = []
      # The value element may finish on this line or span several. Track element depth from just
      # after </key>; the value is complete when depth returns to zero having seen a tag.
      after = line.split('</key>', 2)[1].to_s
      d = def_depth.call(after)
      saw = after.match?(/<[^>]*>/)
      while (d > 0 || !saw) && (i + 1) < body.length
        i += 1
        nxt = body[i]
        chunk << nxt
        d += def_depth.call(nxt)
        saw ||= nxt.match?(/<[^>]*>/)
      end
      blocks << [key, chunk.join]
      i += 1
      next
    end

    blocks << [nil, (pending.dup + [line]).join]
    pending = []
    i += 1
  end
  blocks << [nil, pending.join] unless pending.empty?
  [header, blocks, footer]
end

# Read one side of the merge, returning header, blocks and footer.
# `-` or a missing path yields an empty side, which is how an added-on-one-side file merges.
def load_side(path)
  return [nil, [], nil] if path == '-' || path.nil? || !File.file?(path)
  src = File.read(path)
  h, b, f = split_plist(src)
  die("#{path}: could not locate a top-level <dict> — not a plist?") if b.nil?
  [h, b, f]
end

o_head, o_blocks, o_foot = load_side(opts[:ours])
_b_head, b_blocks, _b_foot = load_side(opts[:base])
t_head, t_blocks, t_foot = load_side(opts[:theirs])

die("--ours is not a readable plist: #{opts[:ours]}") if o_head.nil?
die("--theirs is not a readable plist: #{opts[:theirs]}") if t_head.nil?

# Index plist blocks by key for comparison across the three sides.
def to_map(blocks)
  blocks.each_with_object({}) { |(k, txt), h| h[k] = txt if k }
end
o_map, b_map, t_map = to_map(o_blocks), to_map(b_blocks), to_map(t_blocks)

# Normalise a block's text before comparing, so whitespace-only differences are not conflicts.
def norm(t) = t.to_s.gsub(%r{<!--.*?-->}m, '').gsub(/\s+/, ' ').strip

conflicts = []
merged = []
seen = {}

# Fork order first: its file is the one being updated, so its layout is what a human recognises.
o_blocks.each do |k, txt|
  next unless k
  seen[k] = true
  if t_map.key?(k)
    if norm(txt) == norm(t_map[k])
      merged << t_map[k]                                   # identical — prefer the template's
                                                           # copy so its comments come along
    elsif b_map.key?(k) && norm(txt) == norm(b_map[k])
      merged << t_map[k]                                   # only the template moved
    elsif b_map.key?(k) && norm(t_map[k]) == norm(b_map[k])
      merged << txt                                        # only the fork moved
    else
      conflicts << "key `#{k}` differs on both sides#{b_map.key?(k) ? '' : ' (add/add, no ancestor)'}"
      merged << txt
    end
  else
    # Not upstream. Template deleted it and the fork never touched it → honour the deletion.
    if b_map.key?(k) && norm(txt) == norm(b_map[k]) then nil else merged << txt end
  end
end

t_blocks.each do |k, txt|
  next unless k
  next if seen[k]
  # Present upstream, absent in the fork: template added it (or the fork deleted it deliberately).
  merged << txt unless b_map.key?(k) && !o_map.key?(k)
end

if conflicts.any?
  warn "❌ merge-plist: #{conflicts.size} key(s) this merger will not decide:"
  conflicts.each { |c| warn "     #{c}" }
  warn "   Nothing was written. Both sides changed the same key to different values — decide which"
  warn "   belongs in the template and which is fork identity, then re-run."
  exit 1
end

# Template's header/footer: it owns the file format (DOCTYPE, plist version).
File.write(opts[:out], t_head + merged.join + t_foot)

# Validate. An unparseable plist fails the BUILD, so never ship one from a merge.
if system('which plutil > /dev/null 2>&1')
  unless system("plutil -lint #{opts[:out].inspect} > /dev/null 2>&1")
    warn "❌ merge-plist: the merged plist does not parse (plutil -lint failed) — refusing it."
    File.delete(opts[:out]) if File.file?(opts[:out])
    exit 1
  end
end

if opts[:report]
  kept = merged.length
  warn "  plist union: #{kept} key block(s) — #{o_map.keys.length} from fork, #{t_map.keys.length} from template"
end
exit 0
