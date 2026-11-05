# Consumer App Migration Guide — Room 3 + Store 5 + Security

> **Target Audience:** Developers migrating an existing KMP consumer app onto the latest brand-neutral `kmp-project-template` infrastructure.
>
> **Template Version:** Room 3.0.0-alpha03 | Store 5.1.0-alpha08 | Security (AES-256 field encryption)
>
> **Platforms:** Android | iOS | Desktop (Windows/macOS/Linux) | Web (JS/WasmJS)

---

## Canonical white-label loop

This is the one authoritative loop for standing up (or migrating) a fork — every other doc links
here rather than re-deriving it:

> **TL;DR:** fork → **`syncForkConfig`** → declare **`store_archetype`** → codegen → build → **`/kmp-project-template-sync`**.

1. **fork** the template into your repo.
2. Fill `app-profile/app.yaml` (+ `platforms/**`) and run **`syncForkConfig`** — brand flows from
   `app-profile/` (the SoT) to every tokenized surface: `gradle/fork.properties`, bundle id + display
   name, Firebase config, `mac-app-store.entitlements`, `fastlane/Appfile`, icons.
3. Author a feature and **declare** its `feature_profile.store_archetype` (one of the 8 archetypes).
4. Run codegen (`/kmp-implement` → `kmp-store-gen`) — it reads `store_archetype` and emits the matching
   `core/store` factory (`createStore` / `createMemoryStore` / `createOfflineStore` /
   `createMutableStore`) + `FetchPolicy`, annotated `@StoreProvider`. The unified
   `BaseMutationViewModel` + `MutationMode` (InSession / Draft) drives every write screen.
5. **Build / run**, then periodically run **`/kmp-project-template-sync`** to pull future white-label
   improvements without losing fork work (the home/profile/settings starters are yours and never synced).

---

## Table of Contents

1. [Overview](#1-overview)
2. [Prerequisites](#2-prerequisites)
3. [Migration Path Summary](#3-migration-path-summary)
4. [Phase 0 — sync-dirs (Delivery Mechanism)](#4-phase-0--sync-dirs-delivery-mechanism)
5. [Phase 1 — Gradle Setup](#5-phase-1--gradle-setup)
6. [Phase 2 — Security Module](#6-phase-2--security-module)
7. [Phase 3 — Room 3 Database](#7-phase-3--room-3-database)
8. [Phase 4 — DataStore (Encrypted Preferences)](#8-phase-4--datastore-encrypted-preferences)
9. [Phase 5 — Store 5 (Offline-First Data Layer)](#9-phase-5--store-5-offline-first-data-layer)
10. [Phase 6 — DI Wiring (Koin)](#10-phase-6--di-wiring-koin)
11. [Phase 7 — SecurityGate (UI Integration)](#11-phase-7--securitygate-ui-integration)
12. [Phase 8 — Verification & Testing](#12-phase-8--verification--testing)
13. [Troubleshooting](#13-troubleshooting)
14. [Platform Reference Matrix](#14-platform-reference-matrix)
15. [Automatic vs Manual — What sync-dirs Handles](#15-automatic-vs-manual--what-sync-dirs-handles)

---

## 1. Overview

The `kmp-project-template` provides a layered `core-base` infrastructure that consumer apps adopt:

```
core-base/common     — Dispatchers, logging, base utilities
core-base/security   — AES-256 encryption, biometrics, session management, tamper detection
core-base/database   — Room 3 AppDatabaseFactory (platform-specific path resolution)
core-base/datastore  — Encrypted preferences (multiplatform-settings + platform secure storage)
core-base/store      — Store 5 factory utilities (offline-first caching)
```

Consumer apps build on top:

```
core/database   — AppDatabase, entities, DAOs, encrypted type converters
core/datastore  — UserPreferencesRepository (dual plain/secure store)
core/data       — Repositories using Store 5 + Room + Network
```

**What this guide covers:** How to migrate your existing consumer app from the old stack to the new unified Room 3 + Store 5 + Security architecture.

---

## 2. Prerequisites

### 2.1 Minimum Versions

| Dependency | Required Version | Catalog Key |
|------------|:----------------:|-------------|
| Kotlin | 2.3.20+ | `kotlin` |
| Room | 3.0.0-alpha03 | `room` |
| Store 5 | 5.1.0-alpha08 | `store` |
| SQLite Bundled | 2.6.2 | `sqliteBundled` |
| SQLite Web | 2.6.2 | `sqliteWeb` |
| Koin | 4.1.1 | `koin` |
| Multiplatform Settings | 1.3.0 | `multiplatformSettings` |
| BouncyCastle | 1.78.1 | `bouncycastle` |
| AndroidX Security Crypto | 1.1.0-alpha06 | `androidxSecurityCrypto` |

### 2.2 Sync core-base Modules

Ensure your project includes the latest `core-base` modules from `kmp-project-template`. These are delivered automatically via the `sync-dirs` GitHub Action (see [Phase 0](#4-phase-0--sync-dirs-delivery-mechanism)), or can be manually synced:

```bash
# Manual sync (if not using GitHub Action)
scripts/white-label/sync-dirs.sh --check
```

---

## 3. Migration Path Summary

```
Phase 0: sync-dirs             — Deliver core-base, build-logic, cmp-shared from template (AUTOMATIC)
   ↓
Phase 1: Gradle Setup          — Version catalog + build convention plugins
   ↓
Phase 2: Security Module       — Zero-config, auto-included via DI (no upstream deps)
   ↓
Phase 3: Room 3 Database       — Entities, DAOs, AppDatabase, encrypted converters (depends on Security)
   ↓
Phase 4: DataStore             — Dual-store preferences: plain + secure (depends on Security)
   ↓
Phase 5: Store 5               — Offline-first repositories (depends on Room 3 + DataStore)
   ↓
Phase 6: DI Wiring             — KoinModules.allModules (enforce ordering)
   ↓
Phase 7: SecurityGate          — Root composable wrapper (depends on Security DI)
   ↓
Phase 8: Verification          — Build, test, verify encryption
```

**Dependency chain:** Security → Room 3 → DataStore → Store 5 → DI → UI

**Estimated effort:** 2-4 hours for a typical consumer app (Phase 0 is automated).

---

## 4. Phase 0 — sync-dirs (Delivery Mechanism)

### 4.1 What sync-dirs Does

The `sync-dirs` GitHub Action (`.github/workflows/sync-dirs.yaml`) automatically syncs infrastructure directories from `kmp-project-template` to consumer app forks. It runs weekly (Monday midnight UTC) and can be triggered manually.

**What gets synced (and what it delivers):**

| Synced Directory | What It Delivers to Consumer Apps |
|---|---|
| `core-base/` | Security module, Database base, DataStore base, Store 5 factory, Common utilities |
| `build-logic/` | Room convention plugin (`mifos.kmp.room`), library conventions, KSP configuration |
| `cmp-shared/` | Shared navigation framework, shared DI scaffolding |
| `cmp-desktop/` | Desktop launcher (excludes `icons/`, `build.gradle.kts`) |
| `cmp-web/` | Web launcher |
| `.github/` | CI workflows (including sync-dirs itself) |
| `config/` | Detekt, Spotless, lint configurations |
| `fastlane/` | Deployment lanes |
| `scripts/` | Setup, deploy, verify scripts |

**Files also synced:** `Gemfile`, `Gemfile.lock`, `ci-prepush.bat`, `ci-prepush.sh`

### 4.2 How to Trigger (First-Time Bootstrap)

For consumer apps that haven't synced recently:

```bash
# Option 1: Manual trigger via GitHub UI
# Go to: Actions → "Sync Directories" → Run workflow

# Option 2: Manual trigger via CLI
gh workflow run sync-dirs.yaml --repo yourorg/your-consumer-app
```

The action creates a PR against `dev` branch with a detailed change description. **Review and merge this PR before proceeding to Phase 1.**

### 4.3 Guard: Only Runs on Forks

The sync workflow only runs on forks, not on the template itself:

```yaml
if: github.repository != 'openMF/kmp-project-template'
```

This prevents the template from syncing to itself.

### 4.4 Exclusion System

Some files are excluded per directory to preserve consumer app customizations:

| Directory | Excluded Files |
|---|---|
| `cmp-desktop/` | `icons/`, `build.gradle.kts` (app-specific icons and config) |
| `cmp-android/` | Not synced (app-specific) |
| `core/` | Not synced (app-specific entities, DAOs, repositories) |
| `feature/` | Not synced (app-specific feature modules) |

### 4.5 After Merging the sync-dirs PR

Once merged, the following infrastructure is available in your consumer app:

```
core-base/
├── common/      ✅ Dispatchers, logging, base utilities
├── security/    ✅ FieldEncryptor, SecureKeyProvider, SessionManager, BiometricAuth
├── database/    ✅ AppDatabaseFactory, Room 3 base, migration helpers
├── datastore/   ✅ DatastoreBaseModule, SecureSettingsFactory, dual-store pattern
└── store/       ✅ StoreFactory (createStore, createMutableStore, createMemoryStore)

build-logic/
└── convention/  ✅ KMPRoomConventionPlugin (Room + KSP for all 7 targets)
```

**These modules are dormant until wired up.** Proceed to Phase 1 to activate them.

### 4.6 Subsequent Syncs (Ongoing)

After initial migration, weekly syncs keep `core-base` updated automatically:
- Bug fixes in Security, Room base, or DataStore base arrive via PR
- New features (e.g., new encryption algorithm) land automatically
- Consumer app's `core/` modules (entities, DAOs, repositories) are **never overwritten**
- Always review the sync PR before merging — check the diff for breaking changes

---

## 5. Phase 1 — Gradle Setup

### 5.1 Version Catalog (`gradle/libs.versions.toml`)

Add or update these entries:

```toml
[versions]
room = "3.0.0-alpha03"
store = "5.1.0-alpha08"
sqliteBundled = "2.6.2"
sqliteWeb = "2.6.2"
multiplatformSettings = "1.3.0"
bouncycastle = "1.78.1"
androidxSecurityCrypto = "1.1.0-alpha06"

[libraries]
androidx-room-compiler = { module = "androidx.room3:room3-compiler", version.ref = "room" }
androidx-room-runtime = { module = "androidx.room3:room3-runtime", version.ref = "room" }
androidx-sqlite-bundled = { module = "androidx.sqlite:sqlite-bundled", version.ref = "sqliteBundled" }
androidx-sqlite-web = { module = "androidx.sqlite:sqlite-web", version.ref = "sqliteWeb" }
store5 = { group = "org.mobilenativefoundation.store", name = "store5", version.ref = "store" }
store5-cache = { group = "org.mobilenativefoundation.store", name = "cache5", version.ref = "store" }
multiplatform-settings = { group = "com.russhwolf", name = "multiplatform-settings-no-arg", version.ref = "multiplatformSettings" }
multiplatform-settings-serialization = { group = "com.russhwolf", name = "multiplatform-settings-serialization", version.ref = "multiplatformSettings" }
multiplatform-settings-coroutines = { group = "com.russhwolf", name = "multiplatform-settings-coroutines", version.ref = "multiplatformSettings" }
androidx-security-crypto = { group = "androidx.security", name = "security-crypto", version.ref = "androidxSecurityCrypto" }
bouncycastle = { module = "org.bouncycastle:bcprov-jdk18on", version.ref = "bouncycastle" }

[plugins]
room = { id = "androidx.room3", version.ref = "room" }
```

### 5.2 Room Convention Plugin

The template provides a `KMPRoomConventionPlugin` that handles Room + KSP setup automatically:

```kotlin
// build-logic/convention/src/main/kotlin/KMPRoomConventionPlugin.kt
// Applies: androidx.room3, com.google.devtools.ksp
// Registers KSP compiler for all 7 KMP targets
// Sets schema export directory
```

Apply it in your database module:

```kotlin
// core/database/build.gradle.kts
plugins {
    alias(libs.plugins.kmp.library.convention)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.mifos.kmp.room)  // <-- This handles everything
}
```

---

## 6. Phase 2 — Security Module

### 6.1 What You Get (Zero Config)

The security module is **self-sufficient**. Consumer apps need ZERO wiring code. Include the module dependency and the DI module — everything else is automatic.

| Component | Purpose | Platform Support |
|-----------|---------|:----------------:|
| `FieldEncryptor` | AES-256 field-level encryption | Android (GCM), Desktop (GCM/BC), iOS (CBC/CCCrypt), JS (stub) |
| `SecureKeyProvider` | Hardware-backed key storage | Android (Keystore), Desktop (BC), iOS (Keychain), JS (stub) |
| `SecureRandom` | Cryptographically secure RNG | All platforms |
| `SessionManager` | Inactivity-based session timeout | All platforms |
| `SecurityGate` | Root composable security wrapper | All platforms (Compose) |
| `BiometricAuthenticator` | Biometric re-auth on timeout | Android (BiometricPrompt), iOS (LocalAuth) |
| `TamperDetector` | Device integrity verification | Android (Play Integrity), iOS (jailbreak) |
| `FailedAttemptTracker` | Rate limiting for auth attempts | All platforms |
| `SecureAuthManager` | Auth orchestration | All platforms |
| `SensitiveString` | Clearable string for passwords | All platforms |
| `DeepLinkValidator` | URL/deep link validation | All platforms |
| `SecureNavHandler` | Navigation security | All platforms |

### 6.2 Dependency

```kotlin
// core/database/build.gradle.kts (or wherever you need encryption)
commonMain.dependencies {
    implementation(projects.coreBase.security)
}
```

### 6.3 DI Module (Auto-Included)

The `SecurityModule` is registered in `KoinModules.allModules`. No manual setup needed:

```kotlin
// SecurityModule provides:
// - SecureKeyProvider (platform-specific key storage)
// - FieldEncryptor (AES-256 encryption)
// - SecureRandom (CSPRNG)
// - SessionManager, BiometricAuthenticator, TamperDetector, etc.
```

### 6.4 Using FieldEncryptor Directly (Optional)

If you need to encrypt individual fields outside of Room:

```kotlin
class MyRepository(private val encryptor: FieldEncryptor) {
    fun storeSecret(value: String): String {
        return "ENC:" + encryptor.encrypt(value)
    }
    
    fun readSecret(stored: String): String {
        if (!stored.startsWith("ENC:")) return stored  // Legacy unencrypted
        return encryptor.decrypt(stored.removePrefix("ENC:"))
    }
}
```

---

## 7. Phase 3 — Room 3 Database

### 7.1 Module Dependencies

```kotlin
// core/database/build.gradle.kts
plugins {
    alias(libs.plugins.kmp.library.convention)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.mifos.kmp.room)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.koin.android)
        }

        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kermit.logging)
            api(projects.core.common)
            api(projects.coreBase.database)
            implementation(projects.coreBase.security)
        }
    }
}
```

### 7.2 Define Entities

```kotlin
// commonMain/kotlin/.../entity/ClientEntity.kt
@Entity(tableName = "clients")
@Serializable
data class ClientEntity(
    @PrimaryKey
    val id: Int,
    val displayName: String,
    val accountNo: String,
    val status: String,
    @ColumnInfo(name = "office_id")
    val officeId: Int,
)
```

**KMP-Specific Rules:**
- Use `@kotlin.concurrent.Volatile` (NOT `@Volatile`) for any `commonMain` volatile fields
- Entity classes must be `@Serializable` if used with type converters
- All annotations come from `androidx.room3.*`

### 7.3 Define DAOs

```kotlin
// commonMain/kotlin/.../dao/ClientDao.kt
@Dao
interface ClientDao {
    @Query("SELECT * FROM clients")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id")
    fun getClientById(id: Int): Flow<ClientEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClients(clients: List<ClientEntity>)

    @Delete
    suspend fun deleteClient(client: ClientEntity)
}
```

**Rules:**
- Read operations return `Flow<T>` (observable, reactive)
- Write operations are `suspend` functions
- Use `OnConflictStrategy.REPLACE` for upsert behavior

### 7.4 Type Converters with Encryption

```kotlin
// commonMain/kotlin/.../utils/TypeConverters.kt
class AppTypeConverters {
    companion object {
        @kotlin.concurrent.Volatile  // NOT @Volatile (JVM-only)
        private var encryptor: FieldEncryptor? = null

        fun install(fieldEncryptor: FieldEncryptor) {
            encryptor = fieldEncryptor
        }
    }

    private fun encryptString(value: String): String {
        val enc = encryptor ?: return value
        return try {
            "ENC:" + enc.encrypt(value)
        } catch (e: Exception) {
            Logger.d("TypeConverters") { "Encryption failed: ${e.message}" }
            value
        }
    }

    private fun decryptString(value: String): String {
        if (!value.startsWith("ENC:")) return value  // Legacy unencrypted
        val raw = value.removePrefix("ENC:")
        return encryptor?.runCatching { decrypt(raw) }
            ?.onFailure { Logger.d("TypeConverters") { "Decryption failed: ${it.message}" } }
            ?.getOrDefault(raw)
            ?: raw
    }

    @TypeConverter
    fun fromJsonList(value: String): List<String> {
        return try {
            Json.decodeFromString(decryptString(value))
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun toJsonList(list: List<String>): String {
        return encryptString(Json.encodeToString(list))
    }
}
```

**Key Points:**
- `@kotlin.concurrent.Volatile` — use the common KMP annotation, NOT `@Volatile` (which is `kotlin.jvm.Volatile` and fails on native)
- Encryption is **optional** — if `install()` is never called, data stores unencrypted
- Backward compatible — unencrypted (legacy) values read transparently via `"ENC:"` prefix detection
- Encrypt-on-write migration — existing unencrypted data stays readable, gets encrypted on next write

### 7.5 AppDatabase

```kotlin
// commonMain/kotlin/.../AppDatabase.kt
@Database(
    entities = [
        ClientEntity::class,
        LoanEntity::class,
        SavingsEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(AppTypeConverters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val clientDao: ClientDao
    abstract val loanDao: LoanDao
    abstract val savingsDao: SavingsDao

    companion object {
        const val VERSION = 1
        const val DATABASE_NAME = "mifos_database.db"
    }
}

// KSP generates the actual implementations for each platform
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>
```

### 7.6 Platform DI Modules

Each platform needs its own database builder configuration:

**Android:**
```kotlin
// androidMain/kotlin/.../di/DatabaseModule.android.kt
actual val platformModule: Module = module {
    single {
        AppTypeConverters.install(get<FieldEncryptor>())

        AppDatabaseFactory(androidApplication())
            .createDatabase<AppDatabase>(AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigrationOnDowngrade(false)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}
```

**Desktop:**
```kotlin
// desktopMain/kotlin/.../di/DatabaseModule.desktop.kt
actual val platformModule: Module = module {
    single {
        AppTypeConverters.install(get<FieldEncryptor>())

        AppDatabaseFactory()
            .createDatabase<AppDatabase>(AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigrationOnDowngrade(false)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}
```

**Native (iOS):**
```kotlin
// nativeMain/kotlin/.../di/DatabaseModule.native.kt
actual val platformModule: Module = module {
    single {
        AppTypeConverters.install(get<FieldEncryptor>())

        AppDatabaseFactory()
            .createDatabase<AppDatabase>(AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigrationOnDowngrade(false)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Default)  // No Dispatchers.IO on Native
            .build()
    }
}
```

**JS / WasmJS:**
```kotlin
// jsMain or wasmJsMain
actual val platformModule: Module = module {
    single {
        AppTypeConverters.install(get<FieldEncryptor>())

        AppDatabaseFactory()
            .createDatabase<AppDatabase>(AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigrationOnDowngrade(false)
            .setDriver(SQLiteWebDriver())  // OPFS-backed
            .setQueryCoroutineContext(Dispatchers.Default)
            .build()
    }
}
```

**Critical:** `AppTypeConverters.install(get<FieldEncryptor>())` MUST execute before `build()`. This is enforced by placing it first in the `single { }` block.

### 7.7 Common DI Module

```kotlin
// commonMain/kotlin/.../di/DatabaseModule.kt
expect val platformModule: Module

val DatabaseModule = module {
    includes(platformModule)
    single { get<AppDatabase>().clientDao }
    single { get<AppDatabase>().loanDao }
    single { get<AppDatabase>().savingsDao }
}
```

---

## 8. Phase 4 — DataStore (Encrypted Preferences)

### 8.1 Architecture: Dual-Store Pattern

The template uses **two separate settings stores**:

| Store | Qualifier | Backend | Use For |
|-------|-----------|---------|---------|
| Plain | `named("plain")` | Standard SharedPreferences / Properties file | UI preferences (theme, language, layout) |
| Secure | `named("secure")` | EncryptedSharedPreferences (Android), Keychain (iOS), DPAPI (Desktop) | Credentials, tokens, PII |

### 8.2 Module Dependencies

```kotlin
// core/datastore/build.gradle.kts
commonMain.dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.coreBase.common)
    implementation(projects.coreBase.datastore)  // Provides DatastoreBaseModule

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.multiplatform.settings)
    implementation(libs.multiplatform.settings.serialization)
    implementation(libs.multiplatform.settings.coroutines)
}
```

### 8.3 Repository Interface

```kotlin
interface UserPreferencesRepository {
    val userData: StateFlow<UserData>
    val authToken: String?

    val observeLanguage: Flow<LanguageConfig>
    val observeDarkThemeConfig: Flow<DarkThemeConfig>
    val observeDynamicColorPreference: Flow<Boolean>

    suspend fun setLanguage(language: LanguageConfig)
    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)
    suspend fun setIsAuthenticated(isAuthenticated: Boolean)
    suspend fun setPasscode(passcode: String)
    suspend fun clearUserData()
}
```

### 8.4 Implementation (Dual-Store Split)

```kotlin
class UserPreferencesRepositoryImpl(
    private val plainSettings: Settings,       // UI preferences
    private val secureSettings: Settings,      // Credentials, tokens
    private val dispatcher: DispatcherManager,
) : UserPreferencesRepository {

    // Non-sensitive: plain store
    override suspend fun setLanguage(language: LanguageConfig) {
        plainSettings.putString(KEY_LANGUAGE, language.name)
    }

    // Sensitive: secure store
    override suspend fun setPasscode(passcode: String) {
        secureSettings.putString(KEY_PASSCODE, passcode)
    }
}
```

### 8.5 DI Module

```kotlin
val DatastoreModule = module {
    includes(CommonModule, DatastoreBaseModule)  // From core-base

    single {
        UserPreferencesRepositoryImpl(
            plainSettings = get<Settings>(named("plain")),
            secureSettings = get<Settings>(named("secure")),
            dispatcher = get(),
        )
    } bind UserPreferencesRepository::class
}
```

### 8.6 Migration from Single-Store

If your app previously used a single `Settings` instance:

```kotlin
class UserPreferencesRepositoryImpl(...) {
    init {
        migrateIfNeeded()
    }

    private fun migrateIfNeeded() {
        // Step 1: Check if old single-store has data
        val oldData = plainSettings.getStringOrNull(OLD_USER_DATA_KEY) ?: return

        // Step 2: Write sensitive fields to secure store (write-before-delete)
        val userData: UserData = Json.decodeFromString(oldData)
        secureSettings.putString(KEY_AUTH_TOKEN, userData.authToken)
        secureSettings.putString(KEY_PASSCODE, userData.passcode)
        secureSettings.putBoolean(KEY_IS_AUTHENTICATED, userData.isAuthenticated)

        // Step 3: Keep non-sensitive in plain store
        plainSettings.putString(KEY_LANGUAGE, userData.language.name)
        plainSettings.putString(KEY_THEME, userData.theme.name)

        // Step 4: Remove old combined key (after successful write)
        plainSettings.remove(OLD_USER_DATA_KEY)
    }
}
```

**Pattern: Write-before-delete** — Always write to the new location BEFORE removing from the old location. If the app crashes mid-migration, data is not lost.

### 8.7 Platform Secure Storage Backends

| Platform | Backend | Hardware-Backed? |
|----------|---------|:----------------:|
| Android | `EncryptedSharedPreferences` (AES-256-GCM via AndroidKeyStore) | Yes |
| iOS | `KeychainSettings` (Keychain Services, service: `"${app_id}.secure"` — from `app-profile`) | Yes |
| Desktop | `PropertiesSettings` (file-based, upgrade to OS Keychain planned) | No |
| JS/WasmJS | In-memory (upgrade to IndexedDB CryptoKey planned) | No |

---

## 9. Phase 5 — Store 5 (Offline-First Data Layer)

### 9.1 Architecture

```
ViewModel  →  Repository  →  Store 5  →  { Fetcher (Network), SourceOfTruth (Room) }
```

Store 5 manages the network-to-cache pipeline:
- **Fetcher**: Network API call
- **SourceOfTruth**: Room database (persistent cache)
- **Validator**: TTL-based cache freshness
- **Bookkeeper**: Offline write tracking (for mutable stores)

### 9.2 Read-Only Store (Most Common)

```kotlin
// core/data/repository/ClientRepository.kt
class ClientRepositoryImpl(
    private val clientApi: ClientApi,
    private val clientDao: ClientDao,
) {
    private val store = StoreFactory.createStore(
        fetcher = Fetcher.of { key: Int ->
            clientApi.getClients(key)  // Network call
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key -> clientDao.getAllClients() },       // Room read (Flow)
            writer = { _, clients -> clientDao.insertClients(clients) },  // Room write
        ),
        validator = DefaultValidator(ttlHours = 1),  // Cache valid for 1 hour
    )

    fun observeClients(): Flow<StoreData<List<ClientEntity>>> {
        return store.streamData(key = 0)
    }
}
```

### 9.3 Mutable Store (Read/Write with Sync)

```kotlin
val mutableStore = StoreFactory.createMutableStore(
    fetcher = Fetcher.of { key -> api.getItem(key) },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key -> dao.getItem(key) },
        writer = { _, item -> dao.insertItem(item) },
    ),
    converter = Converter.Builder<NetworkItem, LocalItem, DomainItem>()
        .fromNetworkToLocal { it.toLocalEntity() }
        .fromLocalToOutput { it.toDomainModel() }
        .build(),
    updater = Updater.by(
        post = { key, item -> api.updateItem(key, item) },
    ),
    bookkeeper = RoomBookkeeper(get<AppDatabase>().bookkeeperDao, keySerializer = { it.toString() }),
)
```

### 9.4 Using StoreData in ViewModel

```kotlin
class ClientViewModel(
    private val clientRepository: ClientRepository,
) : ViewModel() {

    val clients = clientRepository.observeClients()
        .map { storeData ->
            when {
                storeData.isLoading -> UiState.Loading
                storeData.hasData -> UiState.Success(storeData.requireData())
                storeData.hasError -> UiState.Error(storeData.error!!)
                else -> UiState.Empty
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)
}
```

For full Store 5 API reference, see [`docs/architecture/cross-cutting/store-data-api.md`](store-data-api.md).

---

## 10. Phase 6 — DI Wiring (Koin)

### 10.1 Module Registration Order

The order matters because of initialization dependencies:

```kotlin
// cmp-navigation/src/commonMain/kotlin/.../di/KoinModules.kt
object KoinModules {
    val allModules = listOf(
        SecurityModule,      // 1. Security first (FieldEncryptor, SecureKeyProvider)
        dataModule,          // 2. Data layer (repositories)
        DatabaseModule,      // 3. Database (calls TypeConverters.install(FieldEncryptor))
        dispatcherModule,    // 4. Dispatchers (CommonModule)
        analyticsModule,     // 5. Analytics
        DatastoreModule,     // 6. DataStore (uses secure Settings from SecurityModule)
        featureModule,       // 7. Feature modules
        AppModule,           // 8. App-level (ViewModels)
    )
}
```

**Critical ordering:**
1. `SecurityModule` MUST come before `DatabaseModule` (provides `FieldEncryptor` for `TypeConverters.install()`)
2. `SecurityModule` MUST come before `DatastoreModule` (provides secure storage backends)
3. `DatabaseModule` registers DAOs as singletons — all repositories can then inject DAOs

### 10.2 Module Hierarchy

```
KoinModules.allModules
├── SecurityModule (core-base/security)
│    └── platformSecurityModule (Android/Desktop/Native/JS)
│         └── SecureKeyProvider, FieldEncryptor, SecureRandom
├── DatabaseModule (core/database)
│    └── platformModule (Android/Desktop/Native/JS/WasmJS)
│         └── AppDatabase (with TypeConverters.install)
├── CommonModule (core-base/common)
│    └── DispatcherManager
├── DatastoreModule (core/datastore)
│    └── DatastoreBaseModule (core-base/datastore)
│         └── datastoreBasePlatformModule
│              └── SecureSettingsFactory → Settings(named("secure"))
└── Feature modules, App module
```

---

## 11. Phase 7 — SecurityGate (UI Integration)

### 11.1 Wrap App Root

The `SecurityGate` composable wraps your entire app and provides automatic:
- Session timeout detection on app resume
- Touch tracking (resets inactivity timer on every gesture)
- Biometric re-auth when session expires (active→inactive transition only, not cold start)
- Tamper detection at startup
- `LocalSecurityState` for downstream composables

```kotlin
// In your root App composable
@Composable
fun App() {
    SecurityGate {
        // Your entire app content here
        AppNavigation()
    }
}
```

### 11.2 Reading Security State

```kotlin
@Composable
fun ProtectedScreen() {
    val securityState = LocalSecurityState.current

    when {
        securityState.isDeviceCompromised -> {
            // Show warning, disable sensitive features
            DeviceCompromisedBanner()
        }
        !securityState.isSessionActive -> {
            // Session expired — SecurityGate handles biometric re-auth
            // This composable can show a lock overlay
            LockScreen()
        }
        else -> {
            // Normal content
            MainContent()
        }
    }
}
```

### 11.3 SecurityPolicy Configuration

```kotlin
// Default policy (can be customized via DI override)
SecurityPolicy(
    sessionTimeoutMinutes = 5,           // Lock after 5 min inactivity
    maxFailedAttempts = 5,               // Lock after 5 failed biometric attempts
    lockoutDurationMinutes = 15,         // Lockout for 15 min after max failures
    requireBiometricOnResume = true,     // Re-auth when app comes to foreground
    enableTamperDetection = true,        // Check device integrity at startup
)
```

---

## 12. Phase 8 — Verification & Testing

### 12.1 Build Verification

```bash
# Run all static analysis checks
./gradlew check spotlessCheck detekt dependencyGuard

# Compile for all platforms
./gradlew compileKotlinAndroid compileKotlinDesktop compileKotlinIosArm64

# Run KSP (Room annotation processing) for all targets
./gradlew kspKotlinAndroid kspKotlinDesktop kspKotlinIosArm64

# Build iOS framework (catches native-specific issues)
./gradlew :cmp-shared:linkDebugFrameworkIosArm64

# Run tests
./gradlew desktopTest
```

### 12.2 Common Build Errors

| Error | Cause | Fix |
|-------|-------|-----|
| `KSP failed with exit code: PROCESSING_ERROR` | Missing type in Room entity/converter | Check imports, use `@kotlin.concurrent.Volatile` (not `@Volatile`) |
| `Unresolved reference: Volatile` | Using `kotlin.jvm.Volatile` on native | Use `@kotlin.concurrent.Volatile` |
| `SecurityException` unresolved on native | Using `java.security.SecurityException` | Use `error()` instead (throws `IllegalStateException`) |
| `String(chars)` deprecated | KMP deprecated constructor | Use `chars.concatToString()` |
| `KeychainSettings` parameter error | API changed in multiplatform-settings 1.3.0 | Use `service` parameter (not `serviceName`) |
| KLIB resolver duplicate warnings | Mixed Compose/AndroidX lifecycle versions | Safe to ignore (warnings only) |

### 12.3 Encryption Verification

```kotlin
// In a test or debug screen
@Test
fun verifyEncryptionRoundTrip() {
    val encryptor: FieldEncryptor = get()  // From Koin

    val original = "sensitive-data-123"
    val encrypted = encryptor.encrypt(original)
    val decrypted = encryptor.decrypt(encrypted)

    assertEquals(original, decrypted)
    assertNotEquals(original, encrypted)  // Must be different
}
```

### 12.4 Database Encryption Verification

```kotlin
@Test
fun verifyTypeConverterEncryption() {
    val encryptor: FieldEncryptor = get()
    AppTypeConverters.install(encryptor)

    val converter = AppTypeConverters()
    val original = listOf("secret1", "secret2")
    val stored = converter.toJsonList(original)

    assertTrue(stored.startsWith("ENC:"))  // Encrypted
    assertEquals(original, converter.fromJsonList(stored))  // Roundtrip works
}
```

---

## 13. Troubleshooting

### 13.1 iOS Build Fails with KSP PROCESSING_ERROR

**Symptom:** `Room processor was unable to process 'AppDatabase' because not all of its dependencies could be resolved`

**Root Cause:** A dependency of your database module has a compilation error on native targets. The most common cause is using `@Volatile` instead of `@kotlin.concurrent.Volatile` in `commonMain` code.

**Fix:**
1. Search your codebase: `grep -r "@Volatile" --include="*.kt" src/commonMain/`
2. Replace all with `@kotlin.concurrent.Volatile`
3. Clean and rebuild: `./gradlew clean :core:database:kspKotlinIosArm64`

### 13.2 FieldEncryptor Not Available at Database Init

**Symptom:** `NullPointerException` or encryption not working

**Root Cause:** `SecurityModule` is loaded after `DatabaseModule` in Koin

**Fix:** Ensure `SecurityModule` appears BEFORE `DatabaseModule` in `KoinModules.allModules`

### 13.3 DataStore Migration Data Loss

**Symptom:** User preferences lost after update

**Root Cause:** Deleting old store before writing to new store

**Fix:** Always use write-before-delete pattern:
1. Write to new location
2. Verify write succeeded
3. THEN delete from old location

### 13.4 Store 5 Cache Not Refreshing

**Symptom:** Stale data shown even with network available

**Fix:** Check `DefaultValidator` TTL configuration. Use `store.fresh(key)` to force network refresh.

### 13.5 Native (iOS) Keychain Errors

**Symptom:** `Failed to store key in Keychain: -25299`

**Root Cause:** Duplicate Keychain entry

**Fix:** `SecureKeyProvider.deleteKey()` before `generateKey()` (the template already handles this)

---

## 14. Platform Reference Matrix

| Feature | Android | Desktop | iOS/macOS | JS | WasmJS |
|---------|:-------:|:-------:|:---------:|:--:|:------:|
| **Room 3 Database** | BundledSQLiteDriver | BundledSQLiteDriver | BundledSQLiteDriver | SQLiteWeb (OPFS) | SQLiteWeb (OPFS) |
| **DB Path** | `Context.getDatabasePath()` | `%APPDATA%` / `~/Library` / `~/.local/share` | `NSDocumentDirectory` | OPFS | OPFS |
| **Field Encryption** | AES-256-GCM (Cipher) | AES-256-GCM (BouncyCastle) | AES-256-CBC (CCCrypt) | Stub (no-op) | Stub (no-op) |
| **Key Storage** | Android Keystore (HW) | BouncyCastle (SW) | Keychain Services (HW) | N/A | N/A |
| **Secure Settings** | EncryptedSharedPreferences | PropertiesSettings (file) | KeychainSettings | In-memory | In-memory |
| **Biometric Auth** | BiometricPrompt | Stub | LocalAuthentication | Stub | Stub |
| **Tamper Detection** | Play Integrity API | Stub | Jailbreak heuristics | N/A | N/A |
| **Coroutine Dispatcher** | `Dispatchers.IO` | `Dispatchers.IO` | `Dispatchers.Default` | `Dispatchers.Default` | `Dispatchers.Default` |
| **KSP Targets** | `kspAndroid` | `kspDesktop` | `kspIosArm64`, `kspIosX64`, `kspIosSimulatorArm64` | `kspJs` | `kspWasmJs` |

---

## Quick Reference: Files to Create/Modify

### New Files (per consumer app)

```
core/database/
├── src/commonMain/kotlin/.../
│   ├── AppDatabase.kt                    # @Database + @ConstructedBy
│   ├── entity/                           # @Entity classes
│   ├── dao/                              # @Dao interfaces
│   ├── utils/AppTypeConverters.kt        # @TypeConverter with encryption
│   └── di/DatabaseModule.kt             # Common DI (expect platformModule)
├── src/androidMain/kotlin/.../di/DatabaseModule.android.kt
├── src/desktopMain/kotlin/.../di/DatabaseModule.desktop.kt
├── src/nativeMain/kotlin/.../di/DatabaseModule.native.kt
├── src/jsMain/kotlin/.../di/DatabaseModule.js.kt
└── src/wasmJsMain/kotlin/.../di/DatabaseModule.wasmJs.kt

core/datastore/
├── src/commonMain/kotlin/.../
│   ├── UserPreferencesRepository.kt      # Interface
│   ├── UserPreferencesRepositoryImpl.kt  # Dual-store implementation
│   └── di/DatastoreModule.kt            # DI module
```

### Modified Files

```
gradle/libs.versions.toml                 # Add Room, Store, Security versions
cmp-navigation/.../di/KoinModules.kt     # Add SecurityModule, DatabaseModule, DatastoreModule
App.kt (root composable)                  # Wrap with SecurityGate { ... }
```

---

## 15. Automatic vs Manual — What sync-dirs Handles

| Component | Delivered by sync-dirs? | Consumer must implement? |
|---|:---:|:---:|
| `core-base/security/` (FieldEncryptor, SecureKeyProvider) | Yes | Wire SecurityModule in DI |
| `core-base/database/` (AppDatabaseFactory) | Yes | Create app-specific entities/DAOs |
| `core-base/datastore/` (DatastoreBaseModule) | Yes | Create app-specific UserPreferencesRepository |
| `core-base/store/` (StoreFactory) | Yes | Wrap repositories with Store 5 |
| `build-logic/` (Room convention plugin) | Yes | Apply `mifos.kmp.room` in build.gradle.kts |
| `cmp-shared/` (navigation, shared DI) | Yes | Register feature modules in navigation |
| App entities/DAOs | No | Migrate manually (Phase 3) |
| App DataStore preferences | No | Split plain/secure (Phase 4) |
| App repositories | No | Add Store 5 wrapping (Phase 5) |
| DI module ordering | No | Reorder manually (Phase 6) |
| SecurityGate UI wrapper | Yes (composable delivered) | Add to App root composable (Phase 7) |

### Key Insight

sync-dirs delivers the **building blocks** (core-base, build-logic). Consumer apps must **wire them up** (DI, entities, repositories). Think of it as:

- **sync-dirs** = delivers the bricks (automatic, weekly)
- **This migration guide** = shows how to build the house (manual, one-time per app)

After initial migration, subsequent sync-dirs PRs bring bug fixes and new features to `core-base` automatically. Consumer app code (`core/`, `feature/`) is never overwritten.

---

## Consumer App Checklist

- [ ] **sync-dirs**: Triggered and merged latest sync PR (Phase 0)
- [ ] **sync-dirs**: Verified `core-base/security/`, `core-base/database/`, `core-base/datastore/`, `core-base/store/` exist
- [ ] **Gradle**: Version catalog updated with Room 3, Store 5, Security deps
- [ ] **Gradle**: `mifos.kmp.room` convention plugin applied to database module
- [ ] **Security**: `SecurityModule` included in `KoinModules.allModules` (BEFORE DatabaseModule)
- [ ] **Database**: Entities defined with `@Entity`, `@Serializable`, `@PrimaryKey`
- [ ] **Database**: DAOs defined with `Flow<T>` reads and `suspend` writes
- [ ] **Database**: `AppDatabase` with `@ConstructedBy(AppDatabaseConstructor::class)`
- [ ] **Database**: Type converters use `@kotlin.concurrent.Volatile` (NOT `@Volatile`)
- [ ] **Database**: `TypeConverters.install(FieldEncryptor)` called in platform DI modules
- [ ] **Database**: Platform DI modules created for all 5 targets
- [ ] **DataStore**: `UserPreferencesRepository` splits sensitive/non-sensitive data
- [ ] **DataStore**: Migration uses write-before-delete pattern
- [ ] **Store 5**: Repositories use `StoreFactory.createStore()` for network+cache
- [ ] **DI**: Module order: Security → Database → DataStore → Features
- [ ] **UI**: Root composable wrapped with `SecurityGate { ... }`
- [ ] **Build**: `./gradlew check spotlessCheck detekt` passes
- [ ] **Build**: iOS KSP compiles: `./gradlew :core:database:kspKotlinIosArm64`
- [ ] **Build**: iOS framework links: `./gradlew :cmp-shared:linkDebugFrameworkIosArm64`
- [ ] **Test**: Encryption roundtrip verified
- [ ] **Test**: `desktopTest` passes
