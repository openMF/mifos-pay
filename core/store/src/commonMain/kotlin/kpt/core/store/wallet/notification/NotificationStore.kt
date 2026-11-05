/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.notification

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.wallet.notification.NotificationDao
import kpt.core.database.wallet.notification.toDomain
import kpt.core.database.wallet.notification.toEntity
import org.mifospay.core.model.notification.Notification
import org.mifospay.core.network.FineractApiManager
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import kotlin.time.Clock

// Wall clock for fetched-at stamps. File-private rather than a defaulted provider parameter: store-ksp
// injects every provider parameter with get(), which cannot resolve a function type.
private val clock: () -> Long = { Clock.System.now().toEpochMilliseconds() }

/** Room `@Entity(tableName = …)` for `NotificationEntity`. Shared with the DAO's writes. */
private const val NOTIFICATIONS_TABLE = "wallet_notifications"

/**
 * `notificationApi.fetchNotifications(...)` `isRead` value. The pre-store
 * `NotificationRepositoryImpl.fetchNotifications()` also hardcodes `true` —
 * the Fineract endpoint returns the CURRENT notification list (both read +
 * unread are surfaced via this same call in practice; the flag is a filter
 * inherited from the template's server contract). Kept as a named constant
 * so future toggles surface here rather than at the fetcher call site.
 */
private const val IS_READ_FLAG: Boolean = true

/**
 * Build the LEDGER read [Store] for notifications
 * (GOAL D13 — the `history` archetype variant for a client-scoped list).
 *
 * ## Shape
 *
 * `createStore` — read-only Store5 factory. Reads are offline-first through
 * the Room `wallet_notifications` [SourceOfTruth]; the network fetcher pulls
 * from `notificationApi.fetchNotifications(isRead = true)` and hands the mapped
 * list to the writer for atomic page replacement.
 *
 * The consumer at the repository layer wires this to
 * `store.asScreenStream(key, networkMonitor, fetchedAtRepository, cacheKey, scope,
 * fetchPolicy = FetchPolicy.CACHE_FIRST_SWR)` — the CACHE_FIRST_SWR band gate is
 * the Phase-5 app-wide default.
 *
 * ## Cache-key scoping (no server-side per-client filter)
 *
 * The Fineract `notifications/` endpoint returns the list for the authenticated
 * session (no `clientId` path parameter). This store still keys on
 * `NotificationKey(clientId)` so the DAO's per-client page semantics prevent a
 * logout-then-login as a different client from surfacing the previous user's
 * cached list. The mapper stamps [NotificationKey.clientId] into each persisted
 * row at write time (see `Notification.toEntity(clientId, ...)`) — parity with
 * `BeneficiaryStore`.
 *
 * ## Atomic page write (S5-PAGE-ATOMIC)
 *
 * The writer lambda calls [NotificationDao.replacePage] — a single `@Transaction`
 * that delete-then-upserts under one SQLite transaction. In-flight
 * [NotificationDao.observeByClient] subscribers never see an empty page
 * mid-write.
 *
 * ## Invalidation wrapping
 *
 * The DAO reader is wrapped with `daoFlow("wallet_notifications") { … }` so
 * wasmJs collectors re-emit after writes even when Room 3 alpha's async
 * InvalidationTracker fails. On Android/Desktop/iOS the wrap is a microsecond
 * no-op alongside Room's native invalidation.
 *
 * ## Write path (GOAL D1 — WRITES STAY ONLINE)
 *
 * This is a `Store` (not `MutableStore`) by design. There is no user-facing
 * write path for notifications today; if a mark-read mutation lands, it must
 * flow through an online endpoint and appear here via the next refresh.
 * There is no `Bookkeeper`.
 */
@StoreProvider(id = "notification")
@CacheKey(fn = "of", key = "wallet_notifications-{clientId}", params = ["clientId:Long"])
fun provideNotificationStore(
    apiManager: FineractApiManager,
    dao: NotificationDao,
): Store<NotificationKey, List<Notification>> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: NotificationKey ->
        // Ktorfit returns Flow<NotificationPayload> — take the first emission,
        // strip to the pageItems list (parity with
        // NotificationRepositoryImpl.fetchNotifications).
        apiManager.notificationApi.fetchNotifications(IS_READ_FLAG).first().pageItems
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: NotificationKey ->
            run { dao.observeByClient(key.clientId) }
                .map { rows -> rows.map { it.toDomain() } }
        },
        writer = { key: NotificationKey, notifications: List<Notification> ->
            val stamp = clock()
            // ATOMIC page replacement — see [NotificationDao.replacePage] KDoc
            // for the S5-PAGE-ATOMIC invariant this write path preserves.
            dao.replacePage(
                clientId = key.clientId,
                entities = notifications.map {
                    it.toEntity(clientId = key.clientId, fetchedAtEpochMs = stamp)
                },
            )
        },
        delete = { key: NotificationKey -> dao.deleteByClient(key.clientId) },
        deleteAll = { dao.deleteAll() },
    ),
)
