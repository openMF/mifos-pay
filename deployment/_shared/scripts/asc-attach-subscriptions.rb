#!/usr/bin/env ruby
# frozen_string_literal: true
#
# asc-attach-subscriptions.rb — put the app's subscriptions INTO the App Store review submission.
#
# THE GAP THIS CLOSES
# A subscription reaching READY_TO_SUBMIT is not in the review. It has to be ATTACHED, and until it
# is, its version sits at PREPARE_FOR_SUBMISSION and the app ships reviewed WITHOUT its in-app
# purchases. Observed on a live app (2026-09-18): the app version was submitted alone, two
# subscriptions sat in a separate UNSUBMITTED draft, and a third was attached to nothing at all.
# Nothing reported a problem — the version submission succeeded, because from its point of view it
# had everything it needed.
#
# THE MECHANISM (verified against the live API, not inferred)
#   · `reviewSubmissionItems` does NOT accept a `subscription` relationship — Apple answers
#     409 ENTITY_ERROR.RELATIONSHIP.UNKNOWN. Several plausible names (inAppPurchaseV2, subscriptionV2,
#     iap, inAppPurchase) are all rejected the same way.
#   · The dedicated resource is `POST /v1/subscriptionSubmissions` with a `subscription`
#     relationship. It creates the pending-version submission, which joins the app's open review
#     submission as an item.
#   · The ACT OF ATTACHING is what moves the subscriptionVersion PREPARE_FOR_SUBMISSION →
#     READY_FOR_REVIEW. The state is a consequence of attachment, not a precondition for it.
#
# So "is this subscription in the review?" is answered by its VERSION state, not by the subscription
# state: a subscription reads READY_TO_SUBMIT whether or not it is attached.
#
# Usage:
#   asc-attach-subscriptions.rb --bundle-id <id> --key-id <k> --issuer <i> --p8 <path.p8>
#                               [--dry-run] [--require-subscriptions]
#
# This step ATTACHES an existing catalogue; it never CREATES one. Creation belongs to whatever owns
# the product catalogue (for a PayCraft-backed app, the dashboard's product sync, which already
# provisions group / subscription / availability / prices / localizations / review screenshot).
# Growing a second catalogue source here would just be drift with two writers.
#
# Apple's refusal detail is vague ("please check associated errors") but the reasons ARE in the
# payload, nested under `errors[].meta.associatedErrors` keyed by resource path. The most common one
# is not a defect at all: FIRST_SUBSCRIPTION_MUST_BE_SUBMITTED_ON_VERSION — an app's first
# subscription must be submitted TOGETHER WITH an app version, which is exactly why the submit script
# adds the version item first and attaches before flipping `submitted`.
#
# There is no direct state write: `subscriptionVersions` allows only CREATE and GET_INSTANCE, so a
# PATCH to set READY_FOR_REVIEW is refused 403. Submission is the only transition.
#
# Exit: 0 = every sellable subscription is attached (or already in review / live)
#       4 = at least one could NOT be attached — Apple's nested reasons are printed
#       1 = hard error (auth, app not found)
require 'json'
require 'net/http'
require 'uri'
require 'base64'
require 'openssl'
require 'optparse'

opts = { dry_run: false }
OptionParser.new do |o|
  o.on('--bundle-id ID') { |v| opts[:bundle_id] = v }
  o.on('--key-id ID')    { |v| opts[:key_id] = v }
  o.on('--issuer ID')    { |v| opts[:issuer] = v }
  o.on('--p8 PATH')      { |v| opts[:p8] = v }
  o.on('--dry-run')      { opts[:dry_run] = true }
  o.on('--require-subscriptions') { opts[:require] = true }
end.parse!
%i[bundle_id key_id issuer p8].each { |k| abort "❌ missing --#{k.to_s.tr('_', '-')}" unless opts[k] }

# Mint a short-lived ES256 JWT for App Store Connect.
# 20-minute expiry — Apple rejects anything longer, so this cannot be cached across a long run.
def jwt(key_id, issuer, p8_path)
  header  = { alg: 'ES256', kid: key_id, typ: 'JWT' }
  payload = { iss: issuer, iat: Time.now.to_i, exp: Time.now.to_i + 1200, aud: 'appstoreconnect-v1' }
  b64 = ->(h) { Base64.urlsafe_encode64(JSON.dump(h)).delete('=') }
  signing_input = "#{b64.call(header)}.#{b64.call(payload)}"
  key = OpenSSL::PKey::EC.new(File.read(p8_path))
  der = key.sign(OpenSSL::Digest.new('SHA256'), signing_input)
  asn1 = OpenSSL::ASN1.decode(der)
  r = asn1.value[0].value.to_s(2).rjust(32, "\x00")
  s = asn1.value[1].value.to_s(2).rjust(32, "\x00")
  "#{signing_input}.#{Base64.urlsafe_encode64(r + s).delete('=')}"
end

TOKEN = jwt(opts[:key_id], opts[:issuer], opts[:p8])

# Issue one App Store Connect API request and parse the JSON response.
def api(method, path, body = nil)
  uri = URI("https://api.appstoreconnect.apple.com/v1/#{path}")
  klass = { get: Net::HTTP::Get, post: Net::HTTP::Post, patch: Net::HTTP::Patch }[method]
  req = klass.new(uri)
  req['Authorization'] = "Bearer #{TOKEN}"
  req['Content-Type'] = 'application/json'
  req.body = JSON.dump(body) if body
  http = Net::HTTP.new(uri.host, 443); http.use_ssl = true
  res = http.request(req)
  [res.code.to_i, (JSON.parse(res.body) rescue res.body)]
end

# Apple's top-level `detail` for a refused attach is deliberately vague — "please check associated
# errors" — and the errors it means are nested in `meta.associatedErrors`, keyed by resource path.
# Reading only `detail` turns a precisely-diagnosed refusal into an unexplained one.
def detail(body)
  errs = Array(body.is_a?(Hash) ? body['errors'] : nil)
  top = errs.first
  return (body.is_a?(Hash) ? body.to_json[0, 240] : body.to_s[0, 240]) unless top

  assoc = top.dig('meta', 'associatedErrors') || {}
  nested = assoc.values.flatten.compact.map { |e| e['title'] || e['detail'] || e['code'] }.uniq
  return top['detail'].to_s if nested.empty?

  "#{top['detail']} → #{nested.join(' | ')}"
end

# The refusal that is NOT a per-subscription defect: Apple requires an app's FIRST subscription to
# ride along with an app version submission. Nothing about the subscription is wrong — it simply
# cannot go on its own, so the fix is ordering, not metadata.
def first_subscription_gate?(body)
  Array(body.is_a?(Hash) ? body['errors'] : nil).any? do |e|
    (e.dig('meta', 'associatedErrors') || {}).values.flatten.compact.any? do |n|
      n['code'].to_s.include?('FIRST_SUBSCRIPTION_MUST_BE_SUBMITTED_ON_VERSION')
    end
  end
end

# A version state that means "already in, or past, review" — nothing to do.
IN_REVIEW_OR_BEYOND = %w[READY_FOR_REVIEW WAITING_FOR_REVIEW IN_REVIEW PENDING_DEVELOPER_RELEASE
                         APPROVED DEVELOPER_ACTION_NEEDED REPLACED_WITH_NEW_VERSION].freeze

# ── resolve app + every subscription ──────────────────────────────────────────────────────────────
c, b = api(:get, "apps?filter[bundleId]=#{opts[:bundle_id]}&limit=1")
abort "❌ apps lookup #{c}: #{detail(b)}" unless c.between?(200, 299)
app_id = b.dig('data', 0, 'id') or abort "❌ no app for bundle id #{opts[:bundle_id]}"

c, b = api(:get, "apps/#{app_id}/subscriptionGroups?limit=50")
abort "❌ subscriptionGroups #{c}: #{detail(b)}" unless c.between?(200, 299)
groups = b['data'] || []

subs = []
groups.each do |g|
  gc, gb = api(:get, "subscriptionGroups/#{g['id']}/subscriptions?limit=200")
  next unless gc.between?(200, 299)
  subs.concat(gb['data'] || [])
end

if subs.empty?
  # "Nothing to attach" is the right answer for an app that sells nothing, and a DANGEROUS one for an
  # app whose paywall ships with it: the deploy would submit, report success, and the app would reach
  # users with a paywall that can sell nothing. The caller knows which it is (the app declares its
  # monetization), so it passes --require-subscriptions and this becomes a halt rather than a shrug.
  if opts[:require]
    warn '❌ this app declares in-app purchases but App Store Connect has NONE.'
    warn '   Nothing would be attached, and the release would ship a paywall with no products behind it.'
    warn '   Create/sync the products first (for a PayCraft-backed app: the dashboard product sync),'
    warn '   then re-run. This step ATTACHES existing subscriptions; it never invents a catalogue.'
    exit 4
  end
  puts '→ no subscriptions on this app — nothing to attach'
  exit 0
end

attached = []
already  = []
blocked  = []
needs_version = []

subs.each do |s|
  pid = s.dig('attributes', 'productId')
  sub_state = s.dig('attributes', 'state')

  # A subscription with no metadata cannot be attached; that is the store-readiness gate's job, and
  # saying so here is more useful than Apple's generic refusal.
  if sub_state == 'MISSING_METADATA'
    blocked << [pid, 'MISSING_METADATA — complete its metadata first (price coverage, localization, review screenshot)']
    next
  end

  vc, vb = api(:get, "subscriptions/#{s['id']}/versions?limit=5")
  unless vc.between?(200, 299)
    blocked << [pid, "could not read versions (#{vc}): #{detail(vb)}"]
    next
  end
  version = (vb['data'] || []).first
  vstate = version&.dig('attributes', 'state')

  if vstate.nil?
    # NO PENDING VERSION = nothing to attach. This is the steady state of a live product: once a
    # subscription is approved and nobody edits it, there is no pending version and no submission to
    # make. Apple says as much if you try anyway ("has no pending version for submission").
    #
    # Treating it as a blocker would HALT every release after the first approval — the deploy would
    # refuse to ship because a healthy, live, already-approved subscription had nothing to submit.
    # That is the wrong failure direction: it fails closed on success. A genuinely incomplete
    # subscription is caught above by the MISSING_METADATA check, which is the real signal.
    already << [pid, 'no pending version — approved/unchanged, nothing to submit']
    next
  end
  if IN_REVIEW_OR_BEYOND.include?(vstate)
    already << [pid, vstate]
    next
  end

  if opts[:dry_run]
    blocked << [pid, "#{vstate} — would attach (dry run)"]
    next
  end

  ac, ab = api(:post, 'subscriptionSubmissions',
               { 'data' => { 'type' => 'subscriptionSubmissions',
                             'relationships' => { 'subscription' => { 'data' => { 'type' => 'subscriptions', 'id' => s['id'] } } } } })
  if ac.between?(200, 299)
    # Confirm the transition rather than assuming the 2xx meant it happened.
    _, nb = api(:get, "subscriptions/#{s['id']}/versions?limit=5")
    now = (nb['data'] || []).first&.dig('attributes', 'state')
    attached << [pid, "#{vstate} → #{now}"]
  elsif first_subscription_gate?(ab)
    needs_version << pid
  else
    blocked << [pid, "#{vstate} — #{detail(ab)}"]
  end
end

# The GROUP has its own pending version too, and App Store Connect says so in the UI ("Your
# auto-renewable subscription must be submitted with its subscription group"). It is NOT separately
# submittable — POST /v1/subscriptionGroupSubmissions answers 409
# ENTITY_ERROR.SUBSCRIPTION_GROUP_SUBMISSION_NOT_ALLOWED (probe-verified). It rides along with the
# subscriptions in the same review submission, so this reports its state rather than acting on it:
# a PREPARE_FOR_SUBMISSION group with attached subscriptions is the expected, healthy state.
group_states = groups.map do |g|
  gv, gb = api(:get, "subscriptionGroups/#{g['id']}/versions?limit=5")
  state = gv.between?(200, 299) ? (gb['data'] || []).first&.dig('attributes', 'state') : nil
  [g.dig('attributes', 'referenceName') || g['id'], state]
end

puts "── App Store subscription attachment (#{subs.length} subscription(s)) ──"
group_states.each do |name, st|
  next if st.nil?
  puts "  group #{name}: version #{st} (submits with its subscriptions — not separately submittable)"
end
already.each      { |p, st| puts "  = #{p}  already in review (#{st})" }
attached.each     { |p, tr| puts "  ✅ #{p}  #{tr}" }
needs_version.each { |p| puts "  ⏸ #{p}  waiting for an app version to be submitted alongside it" }
blocked.each      { |p, why| puts "  ❌ #{p}  #{why}" }

if blocked.empty? && needs_version.empty?
  puts '✅ every subscription is attached to the review submission'
  exit 0
end

unless needs_version.empty?
  puts
  puts "⏸ Apple requires an app's FIRST subscription to be submitted TOGETHER WITH an app version"
  puts '  (STATE_ERROR.FIRST_SUBSCRIPTION_MUST_BE_SUBMITTED_ON_VERSION). Nothing is wrong with these'
  puts '  subscriptions — they cannot be submitted on their own.'
  puts
  puts '  This is not a defect to fix; it is an ORDERING requirement, and it is why the submit script'
  puts '  attaches subscriptions to the SAME reviewSubmission that carries the app version, before'
  puts '  flipping submitted:true. Run this via asc-appstore-submit.rb (which adds the version item'
  puts '  first) rather than standalone, and they go to review together.'
  puts
  puts '  If the app version has ALREADY been submitted on its own, there is no version left for them'
  puts '  to ride along with — they will attach on the NEXT version submission.'
end

unless blocked.empty?
  puts
  puts 'These subscriptions are NOT in the review submission. The app would be reviewed without them.'
  puts 'Resolve what Apple reports above, then re-run.'
end
exit 4
