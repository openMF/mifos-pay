# Version Handling - Understanding Version Formats

**For:** Understanding version formats
**Time:** 15 minutes
**Last Updated:** 2026-02-13

---

## 📖 Overview

This guide explains how versions are generated and why they differ across platforms.

**Key insight:** Different platforms have different version requirements, so a single semantic version is transformed appropriately for each platform.

---

## 📋 Table of Contents

1. [Version Generation Overview](#version-generation-overview)
2. [Gradle Version Generation](#gradle-version-generation)
3. [Firebase Version Requirements](#firebase-version-requirements)
4. [App Store Version Sanitization](#app-store-version-sanitization)
5. [Version Code Calculation](#version-code-calculation)
6. [Platform Comparison](#platform-comparison)
7. [Troubleshooting](#troubleshooting)

---

## Version Generation Overview

### The Challenge

**Goal:** One source of truth for version

**Problem:** Each platform has different requirements:

| Platform | Format | Example | Constraints |
|----------|--------|---------|-------------|
| **Gradle** | Semantic Versioning | `2026.1.1-beta.0.9+abc123` | Full semver supported |
| **Firebase** | Semantic (relaxed) | `2026.1.1-beta.0.9` | Pre-release allowed, no build metadata |
| **App Store** | 3-part version | `2026.1.9` | `MAJOR.MINOR.PATCH` only (max 3 integers) |
| **Play Store** | Any string | `2026.1.1-beta.0.9` | No strict format |
| **Desktop** | Semantic Versioning | `2026.1.1-beta.0.9` | Full semver supported |
| **Web** | Semantic Versioning | `2026.1.1-beta.0.9` | Full semver supported |

**Solution:** Generate full semantic version, then transform per platform

---

### Version Flow

```
┌─────────────────────────────────────┐
│   Gradle (source of truth)          │
│   versionFile.gradle.kts             │
│                                      │
│   Generates:                         │
│   2026.1.1-beta.0.9+abc123          │
│                                      │
│   Format:                            │
│   YYYY.M.D-<prerelease>.<count>+<sha>│
└──────────────┬──────────────────────┘
               │
       ┌───────┼────────┐
       │       │        │
       ▼       ▼        ▼
┌──────────┐ ┌──────────┐ ┌──────────┐
│ Firebase │ │ Play St. │ │ App Store│
│          │ │          │ │          │
│ Uses     │ │ Uses     │ │ Sanitize │
│ as-is    │ │ as-is    │ │ to 3-part│
│          │ │          │ │          │
│ 2026.1.1 │ │ 2026.1.1 │ │ 2026.1.9 │
│ -beta.0.9│ │ -beta.0.9│ │          │
└──────────┘ └──────────┘ └──────────┘
```

---

## Gradle Version Generation

### File Location

**Location:** `build.gradle.kts` (root)

**Task:** `versionFile`

### Version Format

**Full semantic version:**
```
YYYY.M.D-<pre-release>.<commit-count>+<git-sha>
```

**Examples:**
```
2026.1.1                           # No pre-release, first commit of day
2026.1.1-beta.0.5                  # Beta, 5th commit of day
2026.1.1-beta.0.5+abc1234          # With Git SHA
2026.2.15-alpha.1.23+def5678       # Alpha, 15th day, 23rd commit
```

### Components Explained

**1. Year:** `YYYY`
```kotlin
val year = LocalDate.now().year  // 2026
```

**2. Month:** `M`
```kotlin
val month = LocalDate.now().monthValue  // 1 (January)
```

**3. Day:** `D`
```kotlin
val day = LocalDate.now().dayOfMonth  // 1
```

**4. Pre-release:** `<pre-release>.<commit-count>` (optional)
```kotlin
val branch = getCurrentBranch()  // e.g., "dev", "feature/auth"

val preRelease = when {
    branch == "main" || branch == "master" -> null  // No pre-release for production
    branch.startsWith("release/") -> null
    branch == "dev" -> "beta.0"
    branch.startsWith("feature/") -> "beta.0"
    else -> "alpha.0"
}
```

**5. Commit count:** Number of commits today
```kotlin
val commitCount = getCommitCountToday()  // e.g., 5
// Final: "beta.0.5"
```

**6. Git SHA:** Short commit hash (optional)
```kotlin
val gitSha = getGitSha()  // e.g., "abc1234"
// Final: "+abc1234"
```

---

### Task Implementation

**File:** `build.gradle.kts`

```kotlin
// Line 56-60
tasks.register("versionFile") {
    doLast {
        val version = generateVersion()
        val versionCode = calculateVersionCode(version)

        println("Version: $version")
        println("Version code: $versionCode")

        // Write to version.properties
        file("version.properties").writeText(
            """
            version=$version
            versionCode=$versionCode
            """.trimIndent()
        )
    }
}

fun generateVersion(): String {
    val year = LocalDate.now().year
    val month = LocalDate.now().monthValue
    val day = LocalDate.now().dayOfMonth

    val branch = getCurrentBranch()
    val preRelease = getPreRelease(branch)
    val commitCount = getCommitCountToday()
    val gitSha = getGitSha()

    return buildString {
        append("$year.$month.$day")
        if (preRelease != null) {
            append("-$preRelease.$commitCount")
        }
        if (gitSha != null) {
            append("+$gitSha")
        }
    }
}
```

---

### Running Version Generation

```bash
# Generate version
./gradlew versionFile

# Output:
# Version: 2026.1.1-beta.0.9+abc1234
# Version code: 20260109
```

**Output file:** `version.properties`

```properties
version=2026.1.1-beta.0.9+abc1234
versionCode=20260109
```

---

## Firebase Version Requirements

### Firebase App Distribution Format

**Accepts:** Semantic versioning with pre-release

**Format:**
```
MAJOR.MINOR.PATCH[-pre-release]
```

**Examples:**
```
✅ 2026.1.1
✅ 2026.1.1-beta
✅ 2026.1.1-beta.0
✅ 2026.1.1-beta.0.9
❌ 2026.1.1-beta.0.9+abc1234  # Build metadata NOT supported
```

### How We Handle It

**Fastlane automatically removes build metadata:**

**File:** `fastlane/Fastfile`

```ruby
# Line 157-159 (Android)
firebase_app_distribution(
  app: get_app_id,
  release_notes: changelog,
  groups: options[:groups] || ENV['FIREBASE_GROUPS'] || 'internal-testers',
  firebase_cli_path: '/usr/local/bin/firebase',
  service_credentials_file: 'secrets/android/firebaseAppDistributionServiceCredentialsFile.json',
  apk_path: apk_path,
  # Version from Gradle used as-is (build metadata removed)
)

# Line 230-235 (iOS)
firebase_app_distribution(
  app: get_app_id,
  release_notes: changelog,
  groups: options[:groups] || ENV['FIREBASE_GROUPS'] || 'internal-testers',
  firebase_cli_path: '/usr/local/bin/firebase',
  service_credentials_file: '../../secrets/android/firebaseAppDistributionServiceCredentialsFile.json',
  ipa_path: lane_context[SharedValues::IPA_OUTPUT_PATH],
  # Version from Gradle used as-is (build metadata removed)
)
```

**Transformation:**
```
Gradle:   2026.1.1-beta.0.9+abc1234
          ↓ (remove build metadata)
Firebase: 2026.1.1-beta.0.9
```

**Why:** Firebase rejects build metadata (`+abc1234`), but accepts pre-release (`-beta.0.9`)

---

## App Store Version Sanitization

### App Store Connect Requirements

**Strict format:** `MAJOR.MINOR.PATCH`

**Rules:**
- Exactly 3 integers
- No pre-release identifiers
- No build metadata
- Max 3 periods

**Examples:**
```
✅ 2026.1.9
✅ 1.0.0
✅ 2.1.3
❌ 2026.1.1-beta.0.9      # Pre-release not allowed
❌ 2026.1.1-beta.0.9+abc  # Build metadata not allowed
❌ 2026.1                 # Only 2 parts (needs 3)
❌ 2026.1.1.9             # Too many parts (max 3)
```

---

### Sanitization Algorithm

**File:** `fastlane/Fastfile`

```ruby
# Line 251-262
def sanitize_version_for_testflight(version)
  # Extract base version (remove pre-release and build metadata)
  base_version = version.split('-').first.split('+').first

  # Split into parts
  parts = base_version.split('.')

  # App Store requires exactly 3 parts: MAJOR.MINOR.PATCH
  # We use: YYYY.M.CommitCount
  if parts.length >= 3
    year = parts[0]
    month = parts[1]
    commit_count = parts[2..-1].join('')  # Combine remaining parts as commit count
    "#{year}.#{month}.#{commit_count}"
  else
    base_version  # Return as-is if already 3 or fewer parts
  end
end
```

**Examples:**

| Gradle Version | Sanitized for App Store | Explanation |
|---------------|------------------------|-------------|
| `2026.1.1-beta.0.9+abc` | `2026.1.9` | Year.Month.CommitCount |
| `2026.2.15-alpha.1.23` | `2026.2.23` | Year.Month.CommitCount |
| `2026.1.1` | `2026.1.1` | Already valid |
| `1.0.0` | `1.0.0` | Already valid |

**Key insight:** Day is dropped, commit count becomes PATCH

---

### Why This Works

**Year.Month format:**
- Year: Always increasing (2026, 2027, 2028...)
- Month: 1-12
- CommitCount: Monotonically increasing within month

**Example progression:**
```
January:
2026.1.1  (1st commit)
2026.1.2  (2nd commit)
2026.1.3  (3rd commit)
...
2026.1.50 (50th commit)

February:
2026.2.1  (1st commit of February)
2026.2.2  (2nd commit of February)
...

Next year:
2027.1.1  (1st commit of new year)
```

**Benefits:**
- Always increasing (required by App Store)
- Monotonic within month
- Unique per build
- Human-readable (can see year/month)

---

### Where Sanitization Happens

**Fastlane lanes that use sanitization:**

1. **iOS TestFlight** (`fastlane/Fastfile` line 208-214)
```ruby
lane :beta do
  version = get_version_from_gradle
  sanitized_version = sanitize_version_for_testflight(version)

  increment_version_number(version_number: sanitized_version)
  # ...
end
```

2. **iOS App Store** (`fastlane/Fastfile` line 220-228)
```ruby
lane :release do
  version = get_version_from_gradle
  sanitized_version = sanitize_version_for_testflight(version)

  increment_version_number(version_number: sanitized_version)
  # ...
end
```

3. **macOS TestFlight** (similar)
4. **macOS App Store** (similar)

**Firebase iOS:** No sanitization needed (accepts pre-release)

---

## Version Code Calculation

### What is Version Code?

**Version code** (Android only): Monotonically increasing integer

**Used for:**
- Determining which version is newer
- Play Store release ordering
- In-app update logic

---

### Calculation Algorithm

**File:** `build.gradle.kts`

```kotlin
fun calculateVersionCode(version: String): Int {
    // Extract base version (remove pre-release and build metadata)
    val baseVersion = version.split('-').first().split('+').first()

    // Split into parts
    val parts = baseVersion.split('.')

    return when (parts.size) {
        // YYYY.M.D or YYYY.M.CommitCount
        3 -> {
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val dayOrCount = parts[2].toInt()

            // Format: YYYYMMDD or YYYYMMCC
            (year * 10000) + (month * 100) + dayOrCount
        }
        // YYYY.M
        2 -> {
            val year = parts[0].toInt()
            val month = parts[1].toInt()

            (year * 10000) + (month * 100)
        }
        // YYYY
        1 -> {
            parts[0].toInt() * 10000
        }
        else -> 1  // Fallback
    }
}
```

**Examples:**

| Version | Calculation | Version Code |
|---------|-------------|--------------|
| `2026.1.1` | (2026 × 10000) + (1 × 100) + 1 | `20260101` |
| `2026.1.9` | (2026 × 10000) + (1 × 100) + 9 | `20260109` |
| `2026.2.15` | (2026 × 10000) + (2 × 100) + 15 | `20260215` |
| `2026.12.31` | (2026 × 10000) + (12 × 100) + 31 | `20261231` |

---

### Version Code Properties

**Monotonically increasing:** ✅

```
20260101  (2026-01-01)
20260102  (2026-01-02)
...
20260109  (2026-01-09)
...
20260201  (2026-02-01)  # Always > previous month
...
20270101  (2027-01-01)  # Always > previous year
```

**Max value:** `99999999` (year 9999, month 99, day 99)

**Android max:** `2,147,483,647` (32-bit signed integer)

**Safe:** ✅ (`99999999` < `2,147,483,647`)

---

## Platform Comparison

### Example: January 9, 2026 (9th commit of the day)

**Gradle generates:**
```
Version: 2026.1.1-beta.0.9+abc1234
Version code: 20260109
```

**Android:**
```yaml
Play Store:
  versionName: "2026.1.1-beta.0.9"  # Full version
  versionCode: 20260109             # Integer code

Firebase:
  releaseNotes: "Version 2026.1.1-beta.0.9"  # Pre-release OK
```

**iOS:**
```yaml
Firebase:
  version: "2026.1.1-beta.0.9"      # Pre-release OK
  buildNumber: "9"                  # Commit count

TestFlight:
  version: "2026.1.9"                # Sanitized (YYYY.M.CommitCount)
  buildNumber: "9"                   # Commit count

App Store:
  version: "2026.1.9"                # Sanitized (YYYY.M.CommitCount)
  buildNumber: "9"                   # Commit count
```

**Desktop:**
```yaml
GitHub Release:
  tag: "v2026.1.1-beta.0.9"         # Full version
  filename: "app-2026.1.1-beta.0.9-windows.exe"
```

**Web:**
```yaml
GitHub Pages:
  version: "2026.1.1-beta.0.9"      # Full version
  meta: '<meta name="version" content="2026.1.1-beta.0.9">'
```

---

### Summary Table

| Platform | Version Format | Example | Sanitization? |
|----------|---------------|---------|---------------|
| **Gradle (source)** | Full semver | `2026.1.1-beta.0.9+abc` | N/A (source) |
| **Android Play** | Full semver | `2026.1.1-beta.0.9` | None |
| **Android Firebase** | Semver (no build metadata) | `2026.1.1-beta.0.9` | Remove `+abc` |
| **iOS Firebase** | Semver (no build metadata) | `2026.1.1-beta.0.9` | Remove `+abc` |
| **iOS TestFlight** | 3-part version | `2026.1.9` | ✅ Sanitized |
| **iOS App Store** | 3-part version | `2026.1.9` | ✅ Sanitized |
| **macOS TestFlight** | 3-part version | `2026.1.9` | ✅ Sanitized |
| **macOS App Store** | 3-part version | `2026.1.9` | ✅ Sanitized |
| **Desktop** | Full semver | `2026.1.1-beta.0.9` | None |
| **Web** | Full semver | `2026.1.1-beta.0.9` | None |

---

## Troubleshooting

### Issue 1: Version Mismatch Between Platforms

**Symptom:** iOS version different from Android

**This is expected!**

```
Android: 2026.1.1-beta.0.9  (full version)
iOS App Store: 2026.1.9     (sanitized)
```

**Why:** App Store requirement (3-part version only)

**Solution:** This is by design, not an error

---

### Issue 2: Version Code Conflict

**Symptom:**
```
Version code 20260109 has already been used
```

**Cause:** Trying to upload same version twice

**Solutions:**

```bash
# 1. Make a new commit to increment version
git commit -m "chore: bump version"

# 2. Or manually increment version code in build.gradle.kts
android {
    defaultConfig {
        versionCode = 20260110  # Increment manually
    }
}

# 3. Re-generate version
./gradlew versionFile
```

---

### Issue 3: Invalid Version for App Store

**Symptom:**
```
Invalid version format: 2026.1.1-beta.0.9
```

**Cause:** Trying to use unsanitized version for App Store

**Solution:** Fastlane should auto-sanitize. If not:

```bash
# Check Fastlane version
bundle exec fastlane --version

# Update Fastlane
bundle update fastlane

# Verify sanitization function exists
grep -A 10 "sanitize_version_for_testflight" fastlane/Fastfile
```

---

### Issue 4: Build Number Conflict

**Symptom (iOS):**
```
Build number 9 has already been used for version 2026.1.9
```

**Cause:** Uploaded same build twice in one day

**Solution:**

```bash
# Make a new commit to increment build number
git commit -m "chore: bump build"

# Re-deploy
bundle exec fastlane ios beta
```

---

### Verify Version Generation

```bash
# Check current version
./gradlew versionFile

# Output:
# Version: 2026.1.1-beta.0.9+abc1234
# Version code: 20260109

# View version file
cat version.properties
# Output:
# version=2026.1.1-beta.0.9+abc1234
# versionCode=20260109

# Check iOS sanitization
bundle exec fastlane ios beta --verbose
# Look for: "Setting version to 2026.1.9"
```

---

## Additional Resources

- **Semantic Versioning:** https://semver.org/
- **Android Versioning:** https://developer.android.com/studio/publish/versioning
- **iOS Version Numbers:** https://developer.apple.com/documentation/bundleresources/information_property_list/cfbundleshortversionstring
- **Firebase Versioning:** https://firebase.google.com/docs/app-distribution/android/distribute-console

---

**Last Updated:** 2026-02-13
**Maintainer:** See CLAUDE.md
