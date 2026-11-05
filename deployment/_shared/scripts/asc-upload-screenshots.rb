#!/usr/bin/env ruby
# asc-upload-screenshots.rb — RELIABLE App Store screenshot sync via the direct ASC API
# (reserve → upload → commit → poll-COMPLETE), bypassing fastlane `deliver`'s finicky
# screenshot handling (device-folder mapping gaps, config-path errors, upload races that
# leave sets half-full — e.g. a display type ending up with 1 of 10 screenshots). Idempotent
# per display type: clears the set, then uploads the full
# local mockup set in filename order, and waits until every image is COMPLETE.
#
# Usage:
#   asc-upload-screenshots.rb --bundle-id <id> --key-id <k> --issuer <i> --p8 <path> \
#     --screenshots-dir deployment/ios/appstore/metadata/screenshots/en-US [--locale en-US] \
#     [--only APP_IPHONE_67,APP_IPHONE_65]   # default: every mapped device folder present
#
# Local device folder → Apple screenshotDisplayType map (extend as Apple adds sizes):
#   iphone-6.9→APP_IPHONE_67  iphone-6.7→APP_IPHONE_67  iphone-6.5→APP_IPHONE_65
#   iphone-6.3→APP_IPHONE_63  iphone-6.1→APP_IPHONE_61  iphone-5.5→APP_IPHONE_55
#   ipad-13→APP_IPAD_PRO_3GEN_129  ipad-11→APP_IPAD_PRO_3GEN_11

require 'spaceship'; require 'net/http'; require 'json'; require 'digest'; require 'optparse'

opts = { locale: 'en-US' }
OptionParser.new do |o|
  o.on('--bundle-id V') { |v| opts[:bundle_id] = v }
  o.on('--key-id V') { |v| opts[:key_id] = v }
  o.on('--issuer V') { |v| opts[:issuer] = v }
  o.on('--p8 V') { |v| opts[:p8] = v }
  o.on('--screenshots-dir V') { |v| opts[:dir] = v }
  o.on('--locale V') { |v| opts[:locale] = v }
  o.on('--only V') { |v| opts[:only] = v.split(',') }
end.parse!
%i[bundle_id key_id issuer p8 dir].each { |k| abort "❌ --#{k.to_s.tr('_','-')} required" unless opts[k] }

# NOTE: no 'iphone-6.3' — APP_IPHONE_63 is NOT a valid ASC screenshotDisplayType (ASC 409s on it);
# the iPhone 6.3" (16 Pro) shares the 6.1" bucket (APP_IPHONE_61), already covered by 'iphone-6.1'.
MAP = { 'iphone-6.9' => 'APP_IPHONE_67', 'iphone-6.7' => 'APP_IPHONE_67', 'iphone-6.5' => 'APP_IPHONE_65',
        'iphone-6.1' => 'APP_IPHONE_61', 'iphone-5.5' => 'APP_IPHONE_55',
        'ipad-13' => 'APP_IPAD_PRO_3GEN_129', 'ipad-11' => 'APP_IPAD_PRO_3GEN_11' }

TOK = Spaceship::ConnectAPI::Token.create(key_id: opts[:key_id], issuer_id: opts[:issuer], key: File.read(opts[:p8]))
Spaceship::ConnectAPI.token = TOK
app = Spaceship::ConnectAPI::App.find(opts[:bundle_id]) or abort "❌ app not found"
V = app.get_edit_app_store_version(platform: Spaceship::ConnectAPI::Platform::IOS) or abort "❌ no editable iOS version"
BASE = 'https://api.appstoreconnect.apple.com'

# Issue one App Store Connect API request with the bearer token attached.
def api(m, path, body = nil)
  uri = URI("#{BASE}#{path}"); h = Net::HTTP.new(uri.host, 443); h.use_ssl = true
  r = { get: Net::HTTP::Get, post: Net::HTTP::Post, patch: Net::HTTP::Patch, delete: Net::HTTP::Delete }[m].new(uri)
  r['Authorization'] = "Bearer #{TOK.text}"; r['Content-Type'] = 'application/json'; r.body = body.to_json unless body.nil?
  res = h.request(r); [res.code.to_i, (res.body.to_s.empty? ? {} : (JSON.parse(res.body) rescue res.body))]
end

# PUT the image bytes to the pre-signed upload URL Apple returned.
# The headers come from Apple's own `uploadOperations` response and must be sent verbatim —
# substituting a Content-Type here makes the asset fail validation after upload.
def raw_upload(op, bytes)
  uri = URI(op['url']); h = Net::HTTP.new(uri.host, uri.port); h.use_ssl = (uri.scheme == 'https')
  klass = { 'PUT' => Net::HTTP::Put, 'POST' => Net::HTTP::Post, 'PATCH' => Net::HTTP::Patch }[op['method']] || Net::HTTP::Put
  r = klass.new(uri); (op['requestHeaders'] || []).each { |rh| r[rh['name']] = rh['value'] }
  r.body = bytes[op['offset'], op['length']]
  h.request(r).code.to_i
end

# locale localization id
_, locs = api(:get, "/v1/appStoreVersions/#{V.id}/appStoreVersionLocalizations?limit=50")
loc = (locs['data'] || []).find { |l| l.dig('attributes', 'locale') == opts[:locale] } or abort "❌ no localization #{opts[:locale]}"
LOCID = loc['id']

MAP.each do |device, dt|
  next if opts[:only] && !opts[:only].include?(dt)
  # Support BOTH screenshot layouts (media-not-synced fix, 2026-08-27): flat device-PREFIXED files
  # (`ipad-13-01.png` — the fastlane deliver / syncForkConfig convention under deployment/**/metadata)
  # AND device SUBDIRECTORIES (`ipad-13/01.png` — the app-profile media convention). The prior loop
  # only globbed subdirs, so pointing it at the flat deck silently uploaded NOTHING (empty iteration,
  # "sync complete") → required display types ended up missing. Flat first, then subdir fallback.
  files = Dir.glob(File.join(opts[:dir], "#{device}-*.{png,jpg,jpeg}")).sort
  files = Dir.glob(File.join(opts[:dir], device, '*.{png,jpg,jpeg}')).sort if files.empty?
  next if files.empty?
  files = files.first(10) # Apple caps at 10 per size
  puts "▸ #{device} → #{dt}: #{files.size} local"
  # find or create the screenshot set
  _, sets = api(:get, "/v1/appStoreVersionLocalizations/#{LOCID}/appScreenshotSets?limit=50")
  set = (sets['data'] || []).find { |s| s.dig('attributes', 'screenshotDisplayType') == dt }
  unless set
    c, r = api(:post, '/v1/appScreenshotSets', { 'data' => { 'type' => 'appScreenshotSets',
      'attributes' => { 'screenshotDisplayType' => dt },
      'relationships' => { 'appStoreVersionLocalization' => { 'data' => { 'type' => 'appStoreVersionLocalizations', 'id' => LOCID } } } } })
    (puts "  ⚠ create set #{c}: #{r.to_json[0,160]}"; next) unless c.between?(200, 299)
    set = r['data']
  end
  setid = set['id']
  # clear existing (delete all) so the full ordered set is uploaded cleanly
  _, cur = api(:get, "/v1/appScreenshotSets/#{setid}/appScreenshots?limit=50")
  (cur['data'] || []).each { |s| api(:delete, "/v1/appScreenshots/#{s['id']}") }
  ok = 0
  files.each do |f|
    bytes = File.binread(f); md5 = Digest::MD5.hexdigest(bytes)
    c, r = api(:post, '/v1/appScreenshots', { 'data' => { 'type' => 'appScreenshots',
      'attributes' => { 'fileName' => File.basename(f), 'fileSize' => bytes.bytesize },
      'relationships' => { 'appScreenshotSet' => { 'data' => { 'type' => 'appScreenshotSets', 'id' => setid } } } } })
    (puts "  ⚠ reserve #{File.basename(f)} #{c}"; next) unless c.between?(200, 299)
    sid = r['data']['id']
    (r.dig('data', 'attributes', 'uploadOperations') || []).each { |op| raw_upload(op, bytes) }
    pc, = api(:patch, "/v1/appScreenshots/#{sid}", { 'data' => { 'type' => 'appScreenshots', 'id' => sid,
      'attributes' => { 'uploaded' => true, 'sourceFileChecksum' => md5 } } })
    ok += 1 if pc.between?(200, 299)
  end
  puts "  ✅ uploaded #{ok}/#{files.size} to #{dt}"
end

# poll all COMPLETE
puts '⏳ waiting for screenshot processing…'
deadline = Time.now + 600
loop do
  _, sets = api(:get, "/v1/appStoreVersionLocalizations/#{LOCID}/appScreenshotSets?limit=50")
  pending = 0
  (sets['data'] || []).each do |set|
    _, shots = api(:get, "/v1/appScreenshotSets/#{set['id']}/appScreenshots?limit=50&fields%5BappScreenshots%5D=assetDeliveryState")
    (shots['data'] || []).each do |s|
      st = s.dig('attributes', 'assetDeliveryState', 'state')
      if st == 'AWAITING_UPLOAD' then api(:delete, "/v1/appScreenshots/#{s['id']}")
      elsif st != 'COMPLETE' then pending += 1 end
    end
  end
  break if pending.zero? || Time.now > deadline
  puts "  #{pending} still processing…"; sleep 12
end
puts '✅ screenshot sync complete.'
