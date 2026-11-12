source "https://rubygems.org"

ruby '~> 3.3'

# Add compatibility gems for Ruby 3.3+
gem "abbrev"
gem "base64"
gem "mutex_m"
gem "bigdecimal"

gem "fastlane", "~> 2.239.0"
# No iOS package-manager gem: iOS links the Kotlin ComposeApp XCFramework via SwiftPM
# (cmp-ios/Package.swift + the Embed-and-Sign Run-Script phase). E6 removed the
# pod toolchain from every fork — re-adding it here re-drags the whole pod
# dependency tree into every `bundle install`. Enforced by G-IOS-SWIFTPM (IOS-5).

# x86_64-linux added to lockfile PLATFORMS for GHA ubuntu-latest runners.

plugins_path = File.join(File.dirname(__FILE__), 'fastlane', 'Pluginfile')
eval_gemfile(plugins_path) if File.exist?(plugins_path)
