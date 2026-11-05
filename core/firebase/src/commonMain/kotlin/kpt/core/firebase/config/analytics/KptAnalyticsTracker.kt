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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.AnalyticsEvent
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.AnalyticsHelper
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.EventTypes
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.Param
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.ParamKeys
import io.github.mobilebytelabs.kmptoolkit.firebase.compose.rememberAnalyticsHelper

/**
 * CROSS-CUTTING analytics tracker — TEMPLATE-OWNED, full-copied by every sync.
 *
 * Wraps the kmptoolkit [AnalyticsHelper] with the events every app on this template raises, whatever
 * it ships: session, navigation, network, sync, performance. It deliberately knows about NO feature.
 *
 * ## Tracking a feature — do it in the feature's own package
 * A feature gets `kpt/core/firebase/<feature>/` — OUTSIDE `config/` entirely, not a child of it —
 * holding that feature's analytics and crash vocabulary under its own prefix; see `loans/`.
 * Keeping it out of `config/` is what lets `remove-demo.sh` strip a feature as one plain directory
 * and leaves the whole template surface free of demo code.
 *
 * A fork's own feature package is undeclared, which resolves `fork`, so a sync never rewrites it;
 * this file is template-owned, so a sync always does. Putting `trackLoanApproved` here loses it on
 * the next sync AND ships it to forks with no loans. (`loans/` is the template's own showcase of the
 * layout, so it is demo-showcase and `--clean` deletes it.)
 *
 * Both halves take the same injected [AnalyticsHelper], so a feature tracker composes with this one
 * rather than replacing it.
 */
class KptAnalyticsTracker(
    private val analyticsHelper: AnalyticsHelper,
) {

    /** Authentication outcome. `method` is free-text (`password`, `biometric`, `sso`, …). */
    fun trackLogin(method: String, success: Boolean, errorCode: String? = null) {
        val eventType = if (success) EventTypes.LOGIN_SUCCESS else EventTypes.LOGIN_FAILURE
        val params = mutableListOf(
            Param(KptParamKeys.LOGIN_METHOD, method),
            Param(ParamKeys.SUCCESS, success.toString()),
        )
        errorCode?.let { params.add(Param(ParamKeys.ERROR_CODE, it)) }

        analyticsHelper.logEvent(AnalyticsEvent(eventType, params))
    }

    /** Session lifecycle. Pass [durationMs] on end so session length is queryable without a join. */
    fun trackSession(started: Boolean, durationMs: Long? = null) {
        val params = mutableListOf<Param>()
        durationMs?.let { params.add(Param(KptParamKeys.SESSION_DURATION_MS, it.toString())) }

        analyticsHelper.logEvent(
            AnalyticsEvent(
                if (started) KptEventTypes.SESSION_START else KptEventTypes.SESSION_END,
                params,
            ),
        )
    }

    /**
     * A sync pass. Offline-first means sync health IS app health, so this is template-level rather
     * than per-feature even though individual features trigger it.
     */
    fun trackSync(
        syncType: String,
        success: Boolean,
        recordsSynced: Int = 0,
        durationMs: Long? = null,
        errorCode: String? = null,
    ) {
        val eventType = if (success) KptEventTypes.SYNC_COMPLETED else KptEventTypes.SYNC_FAILED
        val params = mutableListOf(
            Param(KptParamKeys.SYNC_TYPE, syncType),
            Param(KptParamKeys.RECORDS_SYNCED, recordsSynced.toString()),
            Param(ParamKeys.SUCCESS, success.toString()),
        )
        durationMs?.let { params.add(Param(KptParamKeys.DURATION_MS, it.toString())) }
        errorCode?.let { params.add(Param(ParamKeys.ERROR_CODE, it)) }

        analyticsHelper.logEvent(AnalyticsEvent(eventType, params))
    }

    /**
     * An operation performed while offline, or replayed once connectivity returned.
     * [entityType] is the caller's own word for what was queued — this tracker does not enumerate it.
     */
    fun trackOfflineOperation(entityType: String, queued: Boolean) {
        analyticsHelper.logEvent(
            AnalyticsEvent(
                if (queued) {
                    KptEventTypes.OFFLINE_OPERATION_QUEUED
                } else {
                    KptEventTypes.OFFLINE_OPERATION_REPLAYED
                },
                listOf(Param(ParamKeys.CONTENT_TYPE, entityType)),
            ),
        )
    }

    /** Screen render/load timing. */
    fun trackPerformance(screenName: String, durationMs: Long) {
        analyticsHelper.logEvent(
            AnalyticsEvent(
                KptEventTypes.SCREEN_LOAD_TIME,
                listOf(
                    Param(KptParamKeys.SCREEN_NAME, screenName),
                    Param(KptParamKeys.DURATION_MS, durationMs.toString()),
                ),
            ),
        )
    }
}

/**
 * Composition-scoped [KptAnalyticsTracker], remembered against the ambient [AnalyticsHelper].
 *
 * A feature tracker gets its own `remember…` in its own package; they share this helper instance.
 */
@Composable
fun rememberKptAnalyticsTracker(): KptAnalyticsTracker {
    val analyticsHelper = rememberAnalyticsHelper()
    return remember(analyticsHelper) { KptAnalyticsTracker(analyticsHelper) }
}
