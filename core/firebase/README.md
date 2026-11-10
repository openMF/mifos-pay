# :core:firebase module

## Fork-customisation seam (start here)

The toolkit ships **without analytics** — every event call becomes a no-op via
`coreFirebaseModule` (the `AnalyticsHelper` is bound to the `cmp-firebase` NoOp helper).
The fork-customisation seam is a single Koin binding. To wire real analytics,
override the binding in your app module:

### 1. No-op (toolkit default — privacy-respecting open-source build)

```kotlin
import kpt.core.firebase.di.coreFirebaseModule

startKoin {
    modules(coreFirebaseModule, /* ... */)  // NoOp AnalyticsHelper installed
}
```

### 2. Console stub (development — print events to logcat / stdout)

```kotlin
val devAnalyticsModule = module {
    single<AnalyticsHelper> { StubAnalyticsHelper() }
}
startKoin { modules(devAnalyticsModule, /* ... */) }
```

### 3. Firebase Analytics (`core-base/firebase`)

```kotlin
import kpt.core.base.firebase.di.firebaseModule
startKoin {
    modules(firebaseModule, /* ... */)  // Firebase-backed AnalyticsHelper; collection starts off until consent
}
```

Requires `google-services.json` (Android) / `GoogleService-Info.plist` (iOS).

### 4. Mixpanel / Amplitude / Segment (custom provider)

```kotlin
class MixpanelAnalyticsHelper(token: String) : AnalyticsHelper {
    private val mixpanel = MixpanelAPI.getInstance(applicationContext, token)
    override fun logEvent(event: AnalyticsEvent) {
        mixpanel.track(event.type, JSONObject(event.params.associate { it.key to it.value }))
    }
    override fun setUserId(userId: String) = mixpanel.identify(userId)
    override fun setUserProperty(name: String, value: String) = mixpanel.people.set(name, value)
}

val mixpanelModule = module {
    single<AnalyticsHelper> { MixpanelAnalyticsHelper(BuildConfig.MIXPANEL_TOKEN) }
}
startKoin { modules(mixpanelModule, /* ... */) }  // Overrides coreFirebaseModule
```

Koin's last-binding-wins resolution means later modules override earlier
bindings — your fork never edits framework files.

### Related seams

- **CrashReporter** (`core-base:observability.observabilityModule`) — separate
  seam for non-fatal errors + caught exceptions. Don't conflate with analytics.
- **AuthProvider** (`core:auth.authModule`) — sign-in / session state. Default
  is `NoOpAuthProvider` (toolkit ships without a login wall).

---

## Overview

This module provides project-specific analytics tracking functionality built on top of the
`cmp-firebase` library's `AnalyticsHelper` (bound via `AnalyticsModule`, NoOp mode by default). It
offers domain-specific tracking methods, extension functions, and Compose utilities tailored for
microfinance applications.

## Enhanced Features

### 🚀 Base Analytics Enhancements (`cmp-firebase`)

#### Enhanced AnalyticsEvent

- **Builder Pattern Support**: Added `withParam()` and `withParams()` methods for fluent event
  creation
- **Parameter Validation**: Automatic validation of parameter keys (≤40 chars) and values (≤100
  chars)
- **Comprehensive Event Types**: Extended from 1 to 25+ predefined event types including:
    - Navigation events (screen_view, screen_transition)
    - User interactions (button_click, search_performed, filter_applied)
    - Form events (form_started, form_completed, form_abandoned)
    - Error tracking (error_occurred, api_error, network_error)
    - Performance metrics (app_launch, loading_time)
    - Authentication events (login_attempt, login_success, logout)

#### Enhanced AnalyticsHelper Interface

- **Convenience Methods**: Added overloaded `logEvent()` methods for simplified usage
- **Built-in Helpers**: Pre-defined methods for common events:
    - `logScreenView()`
    - `logButtonClick()`
    - `logError()`
    - `logFeatureUsed()`
- **User Management**: Support for `setUserProperty()` and `setUserId()`

#### New Extension Functions (`AnalyticsExtensions.kt`)

- **Event Builders**: Factory methods for creating common events
- **Timing Utilities**:
    - `startTiming()` and `timeExecution()` for performance tracking
    - `TimedEvent` class for manual timing control
- **Batch Processing**: `AnalyticsBatch` for efficient multiple event logging
- **Safe Parameter Creation**: Validation helpers for robust parameter handling

#### Enhanced UI Integration (`UiHelpers.kt`)

- **Compose Integration**:
    - `TrackScreenView()` composable for automatic screen tracking
    - `Modifier.trackClick()` for button click tracking
    - `TrackComposableLifecycle()` for component lifecycle tracking
    - `rememberAnalytics()` for easy analytics access

### Domain analytics tracker (`core:firebase`)

#### KptAnalyticsTracker

A specialized tracker providing domain-specific methods for microfinance operations. Construct it
from an `AnalyticsHelper` via the `kptTracker()` extension:

```kotlin
val tracker = analyticsHelper.kptTracker()

// Track a login
tracker.trackLogin(method = "password", success = true)

// Track client operations
tracker.trackClientOperation("create", clientId = "12345", success = true)

// Track loan operations
tracker.trackLoanOperation("apply", loanType = "personal", amount = "5000")

// Track savings operations
tracker.trackSavingsOperation("deposit", accountId = "67890", amount = "1000")

// Track performance metrics
tracker.trackPerformance(
    "api_call",
    duration = 250,
    additionalMetrics = mapOf("endpoint" to "/clients")
)
```

Other tracker methods: `trackGroupOperation`, `trackCenterOperation`, `trackSurveyOperation`,
`trackReportGeneration`, `trackSync`, `trackOfflineOperation`.

#### Extension functions (`KptAnalyticsExtensions.kt`)

Convenient `AnalyticsHelper` extension methods for common workflows:

```kotlin
// Track client creation flow
analyticsHelper.trackClientCreationFlow("step_2")

// Track loan application flow
analyticsHelper.trackLoanApplicationFlow("submit")

// Track API performance
analyticsHelper.trackApiCall("/api/clients", "GET", responseTime = 150, statusCode = 200)

// Track navigation patterns
analyticsHelper.trackNavigation("ClientList", "ClientDetails", "item_click")

// Track form validation
analyticsHelper.trackValidationError("ClientForm", "phoneNumber", "invalid_format")
```

More extensions: `trackDataSync`, `trackPreferenceChange`, `trackTutorial`,
`trackDocumentOperation`, `trackBiometricAuth`, `trackLocationUsage`,
`trackNotificationInteraction`, `trackAccessibilityUsage`, `trackBackupRestore`,
`trackMicrofinanceEvent`. The `kptTracker()` extension returns a `KptAnalyticsTracker`.

#### Compose utilities

Seamless integration with Jetpack Compose via `rememberKptAnalyticsTracker()`:

```kotlin
@Composable
fun ClientDetailsScreen(clientId: String) {
    // Obtain a KptAnalyticsTracker bound to the current AnalyticsHelper
    val tracker = rememberKptAnalyticsTracker()

    Button(
        onClick = {
            tracker.trackClientOperation("view_documents", clientId = clientId)
        }
    ) {
        Text("View Documents")
    }
}
```

#### Event definitions (`KptAnalyticsEvents.kt`)

`object KptEventTypes` defines predefined event-name constants specific to microfinance:

- **Client Management**: `CLIENT_CREATED`, `CLIENT_PROFILE_VIEWED`, etc.
- **Loan Operations**: `LOAN_APPLICATION_STARTED`, `LOAN_DISBURSED`, `LOAN_REPAYMENT_MADE`
- **Savings Management**: `SAVINGS_DEPOSIT_MADE`, `SAVINGS_WITHDRAWAL_MADE`
- **Group Operations**: group meeting / collection events
- **Reporting**: report + dashboard events
- **Sync Operations**: data-sync + offline-transaction events

## Usage Examples

### Basic Event Logging

```kotlin
// Simple event
analyticsHelper.logEvent("button_clicked", "button_name" to "save_client")

// Event with builder pattern
val event = AnalyticsEvent.buttonClick("save_client", "ClientForm")
    .withParam("client_type", "individual")
    .withParam("form_step", "3")
analyticsHelper.logEvent(event)
```

### Performance Tracking

```kotlin
// Time a code block
val result = analyticsHelper.timeExecution("database_query", "table" to "clients") {
    clientRepository.getAllClients()
}

// Manual timing
val timer = analyticsHelper.startTiming("file_upload", "file_type" to "pdf")
uploadFile(file)
timer.complete(mapOf("file_size" to file.size.toString()))
```

### Batch Processing

```kotlin
analyticsHelper.batch()
    .add("client_created", "client_id" to "123")
    .add("document_uploaded", "doc_type" to "photo")
    .add("form_completed", "form_name" to "client_registration")
    .flush()
```

### Error Tracking

```kotlin
// Automatic error tracking with context
try {
    loanService.approveLoan(loanId)
} catch (e: ApiException) {
    analyticsHelper.logError(e.message, e.code, "LoanApproval")
}

// Custom error tracking
analyticsHelper.trackApiCall(
    endpoint = "/loans/approve",
    method = "POST",
    responseTime = 500,
    statusCode = 400,
    success = false
)
```

## Integration

### Dependencies

The core firebase module depends on the `cmp-firebase` library's `AnalyticsHelper`:

```kotlin
// In your module's build.gradle.kts
dependencies {
    implementation(projects.core.firebase)
}
```

### DI Setup

The template binds `AnalyticsHelper` via `AnalyticsModule` — `coreFirebaseModule` installs the
NoOp mode by default:

```kotlin
// core/firebase/.../di/AnalyticsModule.kt
val coreFirebaseModule: Module = module {
    single<AnalyticsHelper> { AnalyticsModule.analyticsHelper(AnalyticsModule.Mode.NoOp) }
    // Switch to AnalyticsModule.Mode.Firebase / Mode.Stub, or bind your own provider, to override.
}
```

Obtain the domain tracker wherever an `AnalyticsHelper` is available:

```kotlin
val tracker: KptAnalyticsTracker = analyticsHelper.kptTracker()
// or, if you prefer explicit construction:
val tracker = KptAnalyticsTracker(analyticsHelper)
```

### Compose Setup

```kotlin
@Composable
fun App() {
    CompositionLocalProvider(
        LocalAnalyticsHelper provides analyticsHelper
    ) {
        // Your app content
    }
}
```

## Best Practices

1. **Use Appropriate Granularity**: Track meaningful user actions, not every UI interaction
2. **Include Business Context**: Add relevant IDs (client_id, loan_id) to events
3. **Handle Errors Gracefully**: Use safe parameter creation for dynamic values
4. **Batch Related Events**: Use batch processing for multiple related events
5. **Respect Privacy**: Avoid logging sensitive personal or financial data
6. **Performance Conscious**: Use timing utilities to track performance bottlenecks

## Migration from Basic Analytics

If you're migrating from basic analytics usage:

```kotlin
// Before
analyticsHelper.logEvent(AnalyticsEvent("client_created", listOf(Param("client_id", "123"))))

// After - multiple options
analyticsHelper.logEvent("client_created", "client_id" to "123")
analyticsHelper.trackClientOperation("create", clientId = "123")
tracker.trackClientOperation("create", clientId = "123")
```

## Platform Support

- ✅ **Android**: Full Firebase Analytics support
- ✅ **Desktop**: Stub implementation for development
- ✅ **iOS**: Firebase Analytics support (via nonJsCommonMain)
- ✅ **Web**: Stub implementation
- ✅ **Native**: Firebase Analytics support

## Contributing

When adding new analytics events:

1. Add event-name constants to `KptEventTypes` (`KptAnalyticsEvents.kt`)
2. Add convenience methods to `KptAnalyticsTracker` if the event needs domain framing
3. Add `AnalyticsHelper` extension functions to `KptAnalyticsExtensions.kt` for common flows
4. Add Compose utilities (e.g. via `rememberKptAnalyticsTracker()`) if UI-related
5. Update this documentation
