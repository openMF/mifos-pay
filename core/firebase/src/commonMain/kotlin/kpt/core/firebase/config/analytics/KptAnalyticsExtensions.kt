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

import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.AnalyticsEvent
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.AnalyticsHelper
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.Param
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.ParamKeys

/**
 * CROSS-CUTTING [AnalyticsHelper] extensions — TEMPLATE-OWNED, full-copied by every sync.
 *
 * Extension functions rather than tracker methods because these are called from places that already
 * hold an [AnalyticsHelper] and should not have to construct a tracker: a Ktor plugin, a navigation
 * observer, a validation helper.
 *
 * ## Feature flows go in the feature's package
 * `kpt/core/firebase/<feature>/` — outside `config/`, not a child of it — holds that feature's
 * flows; in a fork it is fork-owned, and the shipped `loans/` one is the template's showcase.
 * This file is template-owned and rewritten by every sync, so a feature extension added here is lost
 * on the next one — and shipped to forks that do not have the feature in the meantime.
 */

/** A screen-to-screen transition. [trigger] is one of the `KptParamValues.TRIGGER_*` values. */
fun AnalyticsHelper.trackNavigation(
    from: String,
    to: String,
    trigger: String = KptParamValues.TRIGGER_USER_ACTION,
) {
    logEvent(
        AnalyticsEvent(
            KptEventTypes.NAVIGATION,
            listOf(
                Param(KptParamKeys.FROM_SCREEN, from),
                Param(KptParamKeys.TO_SCREEN, to),
                Param(KptParamKeys.TRIGGER, trigger),
            ),
        ),
    )
}

/** A screen becoming visible. Separate from [trackNavigation] so first-render is countable alone. */
fun AnalyticsHelper.trackScreenView(screenName: String) {
    logEvent(
        AnalyticsEvent(
            KptEventTypes.SCREEN_VIEW,
            listOf(Param(KptParamKeys.SCREEN_NAME, screenName)),
        ),
    )
}

/**
 * One outbound API call. Intended for a single Ktor plugin rather than per-call sites — endpoint
 * strings should already be templated (`/loans/{id}`), never interpolated with real ids.
 */
fun AnalyticsHelper.trackApiCall(
    endpoint: String,
    method: String,
    statusCode: Int,
    durationMs: Long,
) {
    val success = statusCode in 200..299
    logEvent(
        AnalyticsEvent(
            if (success) KptEventTypes.API_CALL_SUCCESS else KptEventTypes.API_CALL_FAILURE,
            listOf(
                Param(KptParamKeys.ENDPOINT, endpoint),
                Param(KptParamKeys.HTTP_METHOD, method),
                Param(KptParamKeys.STATUS_CODE, statusCode.toString()),
                Param(KptParamKeys.DURATION_MS, durationMs.toString()),
            ),
        ),
    )
}

/** A field-level validation failure. Never pass the rejected VALUE — only the field and the reason. */
fun AnalyticsHelper.trackValidationError(
    screenName: String,
    fieldName: String,
    errorType: String,
) {
    logEvent(
        AnalyticsEvent(
            KptEventTypes.VALIDATION_ERROR,
            listOf(
                Param(KptParamKeys.SCREEN_NAME, screenName),
                Param(KptParamKeys.FIELD_NAME, fieldName),
                Param(KptParamKeys.ERROR_TYPE, errorType),
            ),
        ),
    )
}

/** A settings/preference change. [oldValue] and [newValue] must be non-PII. */
fun AnalyticsHelper.trackPreferenceChange(
    preferenceName: String,
    oldValue: String?,
    newValue: String,
) {
    val params = mutableListOf(
        Param(KptParamKeys.PREFERENCE_NAME, preferenceName),
        Param(KptParamKeys.NEW_VALUE, newValue),
    )
    oldValue?.let { params.add(Param(KptParamKeys.OLD_VALUE, it)) }

    logEvent(AnalyticsEvent(KptEventTypes.PREFERENCE_CHANGED, params))
}

/** A runtime permission decision. */
fun AnalyticsHelper.trackPermission(permissionName: String, granted: Boolean) {
    logEvent(
        AnalyticsEvent(
            if (granted) KptEventTypes.PERMISSION_GRANTED else KptEventTypes.PERMISSION_DENIED,
            listOf(Param(KptParamKeys.PERMISSION_NAME, permissionName)),
        ),
    )
}

/** Onboarding progress. [action] is `started` / `step_completed` / `skipped` / `completed`. */
fun AnalyticsHelper.trackTutorial(action: String, step: Int, tutorialName: String) {
    val eventType = when (action) {
        "started" -> KptEventTypes.TUTORIAL_STARTED
        "skipped" -> KptEventTypes.TUTORIAL_SKIPPED
        "completed" -> KptEventTypes.TUTORIAL_COMPLETED
        else -> KptEventTypes.TUTORIAL_STEP_COMPLETED
    }
    logEvent(
        AnalyticsEvent(
            eventType,
            listOf(
                Param(KptParamKeys.TUTORIAL_NAME, tutorialName),
                Param(KptParamKeys.STEP_INDEX, step.toString()),
                Param(ParamKeys.ACTION_TYPE, action),
            ),
        ),
    )
}
