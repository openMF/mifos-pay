/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.firebase.config.analytics

/**
 * CROSS-CUTTING analytics event keys — TEMPLATE-OWNED, full-copied by every sync.
 *
 * Everything here is true of ANY app built on this template: a session begins, a screen is shown, a
 * request succeeds or fails, data syncs, a permission is granted. None of it names a feature.
 *
 * ## Where feature events go — NOT here
 * A feature's events live in its own package, `kpt/core/firebase/<feature>/` (e.g. `loans/`),
 * outside `config/`. In a fork that file is fork-owned; a sync never rewrites it, so its own
 * events survive every template upgrade. Adding a feature constant to THIS file has two costs:
 * the next sync overwrites it, and every unrelated fork inherits a key for a feature it does not ship.
 *
 * This file previously carried CLIENT / GROUP / CENTER / SURVEY / SAVINGS / MEETING / ATTENDANCE /
 * COLLECTION / REPAYMENT keys — a specific lender's domain, in a brand-neutral template. Those moved
 * out: a fork that ships those features declares them in its own feature packages.
 */
object KptEventTypes {
    // Session + authentication — every app has a session, whether or not it has accounts.
    /** Session + authentication: login success. Emitted as `login_success`. */
    const val LOGIN_SUCCESS = "login_success"

    /** Session + authentication: login failure. Emitted as `login_failure`. */
    const val LOGIN_FAILURE = "login_failure"

    /** Session + authentication: logout. Emitted as `logout`. */
    const val LOGOUT = "logout"

    /** Session + authentication: session start. Emitted as `session_start`. */
    const val SESSION_START = "session_start"

    /** Session + authentication: session end. Emitted as `session_end`. */
    const val SESSION_END = "session_end"

    /** Session + authentication: session timeout. Emitted as `session_timeout`. */
    const val SESSION_TIMEOUT = "session_timeout"

    /** Session + authentication: biometric auth success. Emitted as `biometric_auth_success`. */
    const val BIOMETRIC_AUTH_SUCCESS = "biometric_auth_success"

    /** Session + authentication: biometric auth failure. Emitted as `biometric_auth_failure`. */
    const val BIOMETRIC_AUTH_FAILURE = "biometric_auth_failure"

    // Navigation — screen-to-screen movement, independent of which screens exist.
    /** Navigation: screen view. Emitted as `screen_view`. */
    const val SCREEN_VIEW = "screen_view"

    /**
     * Event logged on every destination change. One event with from/to params rather than a per-screen event, so a
     * funnel can be built without knowing the screen set in advance.
     */
    const val NAVIGATION = "navigation"

    /** Navigation: deep link opened. Emitted as `deep_link_opened`. */
    const val DEEP_LINK_OPENED = "deep_link_opened"

    /** Navigation: back pressed. Emitted as `back_pressed`. */
    const val BACK_PRESSED = "back_pressed"

    // Network + API — the transport layer, shared by every feature that calls anything.
    /** Network + API: api call success. Emitted as `api_call_success`. */
    const val API_CALL_SUCCESS = "api_call_success"

    /** Network + API: api call failure. Emitted as `api_call_failure`. */
    const val API_CALL_FAILURE = "api_call_failure"

    /** Network + API: network unavailable. Emitted as `network_unavailable`. */
    const val NETWORK_UNAVAILABLE = "network_unavailable"

    /** Network + API: network restored. Emitted as `network_restored`. */
    const val NETWORK_RESTORED = "network_restored"

    // Sync + offline — the offline-first contract in core-base/store applies to every feature.
    /** Sync + offline: sync started. Emitted as `sync_started`. */
    const val SYNC_STARTED = "sync_started"

    /** Sync + offline: sync completed. Emitted as `sync_completed`. */
    const val SYNC_COMPLETED = "sync_completed"

    /** Sync + offline: sync failed. Emitted as `sync_failed`. */
    const val SYNC_FAILED = "sync_failed"

    /** Sync + offline: sync conflict. Emitted as `sync_conflict`. */
    const val SYNC_CONFLICT = "sync_conflict"

    /** Sync + offline: offline operation queued. Emitted as `offline_operation_queued`. */
    const val OFFLINE_OPERATION_QUEUED = "offline_operation_queued"

    /** Sync + offline: offline operation replayed. Emitted as `offline_operation_replayed`. */
    const val OFFLINE_OPERATION_REPLAYED = "offline_operation_replayed"

    // Settings + preferences — the shell, not a feature.
    /** Settings + preferences: theme changed. Emitted as `theme_changed`. */
    const val THEME_CHANGED = "theme_changed"

    /** Settings + preferences: language changed. Emitted as `language_changed`. */
    const val LANGUAGE_CHANGED = "language_changed"

    /** Settings + preferences: preference changed. Emitted as `preference_changed`. */
    const val PREFERENCE_CHANGED = "preference_changed"

    /** Settings + preferences: permission granted. Emitted as `permission_granted`. */
    const val PERMISSION_GRANTED = "permission_granted"

    /** Settings + preferences: permission denied. Emitted as `permission_denied`. */
    const val PERMISSION_DENIED = "permission_denied"

    // Errors + performance — reported the same way whatever raised them.
    /** Errors + performance: validation error. Emitted as `validation_error`. */
    const val VALIDATION_ERROR = "validation_error"

    /** Errors + performance: unhandled error. Emitted as `unhandled_error`. */
    const val UNHANDLED_ERROR = "unhandled_error"

    /** Errors + performance: screen load time. Emitted as `screen_load_time`. */
    const val SCREEN_LOAD_TIME = "screen_load_time"

    /** Errors + performance: slow frame. Emitted as `slow_frame`. */
    const val SLOW_FRAME = "slow_frame"

    // Onboarding — first-run flows exist before any feature does.
    /** Onboarding: tutorial started. Emitted as `tutorial_started`. */
    const val TUTORIAL_STARTED = "tutorial_started"

    /** Onboarding: tutorial step completed. Emitted as `tutorial_step_completed`. */
    const val TUTORIAL_STEP_COMPLETED = "tutorial_step_completed"

    /** Onboarding: tutorial skipped. Emitted as `tutorial_skipped`. */
    const val TUTORIAL_SKIPPED = "tutorial_skipped"

    /** Onboarding: tutorial completed. Emitted as `tutorial_completed`. */
    const val TUTORIAL_COMPLETED = "tutorial_completed"
}

/**
 * CROSS-CUTTING parameter keys — TEMPLATE-OWNED, full-copied by every sync.
 *
 * Feature-specific parameters (a loan product id, a watchlist symbol) belong in that feature's
 * the feature's own `kpt/core/firebase/<feature>/` package, for the same reason as the event types above.
 */
object KptParamKeys {
    // Session
    /** Session: login method. Emitted as `login_method`. */
    const val LOGIN_METHOD = "login_method"

    /** Session: session duration ms. Emitted as `session_duration_ms`. */
    const val SESSION_DURATION_MS = "session_duration_ms"

    // Navigation
    /** Navigation: screen name. Emitted as `screen_name`. */
    const val SCREEN_NAME = "screen_name"

    /**
     * Route navigated FROM. Empty on the first destination of a session, which is how a cold start is told from an in-
     * app move.
     */
    const val FROM_SCREEN = "from_screen"

    /** Route navigated TO. */
    const val TO_SCREEN = "to_screen"

    /**
     * What caused the navigation — a tap, a back press, a deep link. Separates user intent from a programmatic
     * redirect, which otherwise look identical in the funnel.
     */
    const val TRIGGER = "trigger"

    // Network / API
    /** Network / API: endpoint. Emitted as `endpoint`. */
    const val ENDPOINT = "endpoint"

    /** Network / API: http method. Emitted as `http_method`. */
    const val HTTP_METHOD = "http_method"

    /** Network / API: status code. Emitted as `status_code`. */
    const val STATUS_CODE = "status_code"

    /** Network / API: duration ms. Emitted as `duration_ms`. */
    const val DURATION_MS = "duration_ms"

    // Sync
    /** Sync: sync type. Emitted as `sync_type`. */
    const val SYNC_TYPE = "sync_type"

    /** Sync: records synced. Emitted as `records_synced`. */
    const val RECORDS_SYNCED = "records_synced"

    /** Sync: conflict strategy. Emitted as `conflict_strategy`. */
    const val CONFLICT_STRATEGY = "conflict_strategy"

    // Errors
    /** Errors: error type. Emitted as `error_type`. */
    const val ERROR_TYPE = "error_type"

    /** Errors: field name. Emitted as `field_name`. */
    const val FIELD_NAME = "field_name"

    // Settings
    /** Settings: preference name. Emitted as `preference_name`. */
    const val PREFERENCE_NAME = "preference_name"

    /** Settings: old value. Emitted as `old_value`. */
    const val OLD_VALUE = "old_value"

    /** Settings: new value. Emitted as `new_value`. */
    const val NEW_VALUE = "new_value"

    /** Settings: permission name. Emitted as `permission_name`. */
    const val PERMISSION_NAME = "permission_name"

    // Onboarding
    /** Onboarding: tutorial name. Emitted as `tutorial_name`. */
    const val TUTORIAL_NAME = "tutorial_name"

    /** Onboarding: step index. Emitted as `step_index`. */
    const val STEP_INDEX = "step_index"
}

/**
 * CROSS-CUTTING parameter values — TEMPLATE-OWNED, full-copied by every sync.
 *
 * Only values whose meaning is independent of any feature.
 */
object KptParamValues {
    // Trigger sources
    /** Trigger sources: trigger user action. Emitted as `user_action`. */
    const val TRIGGER_USER_ACTION = "user_action"

    /** Trigger sources: trigger deep link. Emitted as `deep_link`. */
    const val TRIGGER_DEEP_LINK = "deep_link"

    /** Trigger sources: trigger notification. Emitted as `notification`. */
    const val TRIGGER_NOTIFICATION = "notification"

    /** Trigger sources: trigger system. Emitted as `system`. */
    const val TRIGGER_SYSTEM = "system"

    // Sync kinds
    /** Sync kinds: sync full. Emitted as `full`. */
    const val SYNC_FULL = "full"

    /** Sync kinds: sync incremental. Emitted as `incremental`. */
    const val SYNC_INCREMENTAL = "incremental"

    /** Sync kinds: sync manual. Emitted as `manual`. */
    const val SYNC_MANUAL = "manual"

    // Outcomes
    /** Outcomes: result success. Emitted as `success`. */
    const val RESULT_SUCCESS = "success"

    /** Outcomes: result failure. Emitted as `failure`. */
    const val RESULT_FAILURE = "failure"

    /** Outcomes: result cancelled. Emitted as `cancelled`. */
    const val RESULT_CANCELLED = "cancelled"
}
