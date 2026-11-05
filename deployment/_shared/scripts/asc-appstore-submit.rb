#!/usr/bin/env ruby
# frozen_string_literal: true
#
# asc-appstore-submit.rb — reliably submit the editable App Store version for review via the ASC API.
# This is the piece fastlane `deliver`'s submit_for_review does UNRELIABLY: it races on reviewSubmission
# state, chokes on stale empty drafts (only ONE open reviewSubmission is allowed per app/platform), and
# reports a version "not in valid state" without saying why. This script does it deterministically:
#   1. resolve the editable App Store version (explicit --version or the newest non-live one) + its build
#   2. REUSE an existing READY_FOR_REVIEW draft (never create a duplicate) or create one
#   3. add the version as a reviewSubmissionItem
#   3b. ATTACH every subscription (asc-attach-subscriptions.rb) so the app and its in-app purchases
#       go to review in ONE submission — a version submitted alone is reviewed without them
#   4. flip the reviewSubmission to submitted:true  →  WAITING_FOR_REVIEW
#
# The ONE thing it CANNOT do is the Part XX Income Tax Act (ITA) / "regulated personal services"
# app-level declaration — Apple exposes NO API for it (verified: not on the app resource, not a
# relationship, not any v1 resource). On THAT specific block it prints the exact human-gate steps and
# exits 3, so the deploy runtime HALTs cleanly (production-affecting → asc-ita-declaration-human-gate)
# and auto-resumes this script the moment the human saves the declaration. Every OTHER precondition is
# automated by asc-appstore-prereqs.rb + the store-listing sync; this closes the loop.
#
# Usage:
#   asc-appstore-submit.rb --bundle-id <id> --key-id <k> --issuer <i> --p8 <path.p8> [--version <v>]
# Exit: 0 = submitted (WAITING_FOR_REVIEW)  ·  3 = ITA human gate  ·  4 = subscriptions unattached
#       (human gate; submitting would exclude them)  ·  1 = other error.

require 'net/http'
require 'json'
require 'optparse'
require 'rbconfig'
require 'openssl'
require 'base64'

opts = {}
OptionParser.new do |o|
  o.on('--bundle-id V') { |v| opts[:bundle_id] = v }
  o.on('--key-id V')    { |v| opts[:key_id] = v }
  o.on('--issuer V')    { |v| opts[:issuer] = v }
  o.on('--p8 V')        { |v| opts[:p8] = v }
  o.on('--require-subscriptions') { opts[:require_subs] = true }
  o.on('--version V')   { |v| opts[:version] = v }
  # PROACTIVE "reply": App Review notes given to the reviewer UP FRONT (demo/guest access, how to test)
  # so they never have to ask — the API can set this even though replying to a review MESSAGE cannot
  # (Resolution Center is UI-only, probe-verified 404). Default path is the fastlane review_information note.
  o.on('--review-notes-file V') { |v| opts[:review_notes_file] = v }
end.parse!
%i[bundle_id key_id issuer p8].each { |k| abort "❌ --#{k.to_s.tr('_', '-')} required" unless opts[k] }

# ── ES256 JWT for the App Store Connect API (no gem dependency) ────────────────────────────────────
def jwt(key_id, issuer, p8_path)
  header  = { alg: 'ES256', kid: key_id, typ: 'JWT' }
  payload = { iss: issuer, iat: Time.now.to_i, exp: Time.now.to_i + 1200, aud: 'appstoreconnect-v1' }
  b64 = ->(h) { Base64.urlsafe_encode64(JSON.dump(h)).delete('=') }
  signing_input = "#{b64.call(header)}.#{b64.call(payload)}"
  key = OpenSSL::PKey::EC.new(File.read(p8_path))
  der = key.sign(OpenSSL::Digest.new('SHA256'), signing_input)
  # DER → JOSE (r||s), 64 bytes
  asn1 = OpenSSL::ASN1.decode(der)
  r = asn1.value[0].value.to_s(2).rjust(32, "\x00")
  s = asn1.value[1].value.to_s(2).rjust(32, "\x00")
  "#{signing_input}.#{Base64.urlsafe_encode64(r + s).delete('=')}"
end

TOKEN = jwt(opts[:key_id], opts[:issuer], opts[:p8])

# Issue one App Store Connect API request and parse the JSON response.
def api(method, path, body = nil)
  uri = URI("https://api.appstoreconnect.apple.com/v1/#{path}")
  klass = { get: Net::HTTP::Get, post: Net::HTTP::Post, patch: Net::HTTP::Patch, delete: Net::HTTP::Delete }[method]
  req = klass.new(uri)
  req['Authorization'] = "Bearer #{TOKEN}"
  req['Content-Type'] = 'application/json'
  req.body = JSON.dump(body) if body
  http = Net::HTTP.new(uri.host, 443); http.use_ssl = true
  res = http.request(req)
  [res.code.to_i, (JSON.parse(res.body) rescue res.body)]
end

# True when the app needs an ITAR/encryption declaration before it can be submitted.
# Apple blocks the submission rather than warning, so this is checked up front instead of
# letting the upload fail at the end of a long build.
def ita_gate?(errors)
  Array(errors).any? { |e| e.to_json.include?('REGULATED_PERSONAL_SERVICE') || e.to_json.include?('ITA_CANNOT_SUBMIT') } ||
    Array(errors).any? { |e| e.dig('meta', 'associatedErrors')&.to_json.to_s.include?('REGULATED_PERSONAL_SERVICE') }
end

# Print what the encryption declaration requires and how to satisfy it.
def print_ita_gate
  warn <<~GATE
    ⛔ HUMAN GATE — Part XX Income Tax Act (ITA) declaration (Apple exposes NO API for this).
       App Store Connect → your app → App Information (or Business → Agreements) → the yellow
       "Part XX of the Income Tax Act (ITA)" banner → Add Information →
       "Do any of your apps provide personal services?" → for a self-use / non-marketplace app: No →
       click DONE (the confirm is what saves it). Wait ~30-60s, then re-run this script — it auto-submits.
  GATE
end

# ── resolve app + editable version + build ────────────────────────────────────────────────────────
c, b = api(:get, "apps?filter[bundleId]=#{opts[:bundle_id]}")
app = (b['data'] || []).first or abort "❌ app #{opts[:bundle_id]} not found (#{c})"
app_id = app['id']

_, vb = api(:get, "apps/#{app_id}/appStoreVersions?filter[platform]=IOS&limit=10&include=build")
versions = vb['data'] || []
editable = %w[PREPARE_FOR_SUBMISSION DEVELOPER_REJECTED REJECTED METADATA_REJECTED INVALID_BINARY]
ver = if opts[:version]
        versions.find { |v| v.dig('attributes', 'versionString') == opts[:version] }
      else
        versions.find { |v| editable.include?(v.dig('attributes', 'appStoreState')) }
      end
abort "❌ no editable iOS App Store version found (states: #{versions.map { |v| v.dig('attributes', 'versionString') + ':' + v.dig('attributes', 'appStoreState').to_s }.join(', ')})" unless ver
vid = ver['id']
puts "→ version #{ver.dig('attributes', 'versionString')} (#{ver.dig('attributes', 'appStoreState')}) id=#{vid}"

# ── PROACTIVE review notes — hand the reviewer demo/guest access instructions up front so they never
#    have to ask (the ONLY reliable "auto-reply": posting a Resolution Center message has NO API). ────
notes_file = opts[:review_notes_file]
notes_file ||= 'ios/appstore/metadata/review_information/notes.txt' if File.exist?('ios/appstore/metadata/review_information/notes.txt')
if notes_file && File.exist?(notes_file)
  notes = File.read(notes_file).strip
  unless notes.empty?
    dc, db = api(:get, "appStoreVersions/#{vid}/appStoreReviewDetail")
    if dc == 200 && db.dig('data', 'id')
      rid = db['data']['id']
      api(:patch, "appStoreReviewDetails/#{rid}", { 'data' => { 'type' => 'appStoreReviewDetails', 'id' => rid,
        'attributes' => { 'notes' => notes } } })
    else
      api(:post, 'appStoreReviewDetails', { 'data' => { 'type' => 'appStoreReviewDetails',
        'attributes' => { 'notes' => notes },
        'relationships' => { 'appStoreVersion' => { 'data' => { 'type' => 'appStoreVersions', 'id' => vid } } } } })
    end
    puts "→ proactive review notes set (reviewer sees demo/guest access up front)"
  end
end

# ── reuse an existing draft reviewSubmission or create one ─────────────────────────────────────────
_, rb = api(:get, "reviewSubmissions?filter[app]=#{app_id}&limit=20")
open = (rb['data'] || []).find { |s| s.dig('attributes', 'state') == 'READY_FOR_REVIEW' && !s.dig('attributes', 'submitted') }
if open
  sub_id = open['id']; puts "→ reusing open reviewSubmission #{sub_id}"
else
  cc, cbody = api(:post, 'reviewSubmissions', { 'data' => { 'type' => 'reviewSubmissions',
    'attributes' => { 'platform' => 'IOS' },
    'relationships' => { 'app' => { 'data' => { 'type' => 'apps', 'id' => app_id } } } } })
  abort "❌ create reviewSubmission #{cc}: #{cbody.to_json[0, 300]}" unless cc.between?(200, 299)
  sub_id = cbody['data']['id']; puts "→ created reviewSubmission #{sub_id}"
end

# ── add the version as a review item (idempotent) ─────────────────────────────────────────────────
ic, ibody = api(:post, 'reviewSubmissionItems', { 'data' => { 'type' => 'reviewSubmissionItems',
  'relationships' => { 'reviewSubmission' => { 'data' => { 'type' => 'reviewSubmissions', 'id' => sub_id } },
                       'appStoreVersion' => { 'data' => { 'type' => 'appStoreVersions', 'id' => vid } } } } })
unless ic.between?(200, 299)
  if ita_gate?(ibody['errors'])
    print_ita_gate
    exit 3
  end
  # already-added is fine; anything else is a real error
  already = Array(ibody['errors']).any? { |e| e.to_json =~ /already|duplicate/i }
  abort "❌ add review item #{ic}: #{ibody['errors']&.first&.dig('detail') || ibody.to_json[0, 300]}" unless already
  puts "→ version already an item"
else
  puts "→ version added as review item"
end

# ── attach subscriptions BEFORE submitting ────────────────────────────────────────────────────────
# A subscription reaching READY_TO_SUBMIT is not in the review. Until it is attached its version sits
# at PREPARE_FOR_SUBMISSION, and flipping `submitted` here would send the app to review WITHOUT its
# in-app purchases — which is exactly what happened on 2026-09-18: the version went alone and the
# subscriptions were left in a separate, unsubmitted draft. Attaching first puts both in ONE
# submission. Delegated to the dedicated script so there is one implementation of the mechanism.
attach_script = File.join(__dir__, 'asc-attach-subscriptions.rb')
if File.exist?(attach_script)
  attach_args = ['--bundle-id', opts[:bundle_id], '--key-id', opts[:key_id],
                 '--issuer', opts[:issuer], '--p8', opts[:p8]]
  # Forwarded, not re-derived: the caller knows whether this app sells anything.
  attach_args << '--require-subscriptions' if opts[:require_subs]
  ok = system(RbConfig.ruby, attach_script, *attach_args)
  unless ok
    # Exit 4 = at least one subscription could not be attached. Submitting anyway would ship a review
    # that silently excludes them, so this HALTs instead — the operator resolves it in App Store
    # Connect (Apple exposes no API for the underlying reason) and re-runs.
    warn ''
    warn '❌ HALT: subscriptions are not attached to the review submission.'
    warn '   Submitting now would send the app to review WITHOUT its in-app purchases.'
    warn '   Resolve the flagged subscriptions in App Store Connect, then re-run this script.'
    exit 4
  end
else
  warn "⚠ #{attach_script} missing — subscriptions NOT verified; the review may exclude them"
end

# ── submit ────────────────────────────────────────────────────────────────────────────────────────
pc, pbody = api(:patch, "reviewSubmissions/#{sub_id}",
  { 'data' => { 'type' => 'reviewSubmissions', 'id' => sub_id, 'attributes' => { 'submitted' => true } } })
if pc.between?(200, 299)
  puts "✅ SUBMITTED FOR REVIEW — state=#{pbody.dig('data', 'attributes', 'state')}"
  exit 0
elsif ita_gate?(pbody['errors'])
  print_ita_gate
  exit 3
else
  abort "❌ submit #{pc}: #{pbody['errors']&.first&.dig('detail') || pbody.to_json[0, 300]}"
end
