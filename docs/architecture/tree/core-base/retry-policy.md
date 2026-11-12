# RetryPolicy + outbox retry-backoff (Phase 05 deferred work)

**Status:** Partial — `RetryPolicy` data class shipped + 6 unit tests; full
wire-in to `OfflineSubmitSyncer.retryAll()` deferred pending Room schema
migration. This document captures what ships today, what's deferred, and the
upgrade path.

---

## What shipped today

- **`template.core.base.store.submit.RetryPolicy`** — pure-data exponential-
  backoff-with-jitter policy. Computes `delayFor(attempt: Int, random: () -> Double)`
  in milliseconds. Defaults: 3 attempts, 1s/2s/4s schedule, ±25% jitter,
  60s cap. All constructor invariants validated via `require(...)`. Six unit
  tests in `RetryPolicyTest.kt` cover monotonic increase, max-delay cap,
  jitter boundaries (deterministic via fixed `random`), constructor
  validation, `delayFor(0) == 0`, and the default schedule snapshot.
- **`OfflineSubmitSyncer(... retryPolicy: RetryPolicy = RetryPolicy())`** —
  the syncer accepts the policy in its primary constructor and the matching
  `CoroutineScope.offlineSubmitSyncer(...)` extension. The field is held but
  **NOT yet applied** to `retryAll()`. See "What's deferred" below.

## What's deferred

To apply `retryPolicy` between retry attempts the syncer needs:

1. **Per-entry attempt counter** — `SubmitOutboxEntry` (Room) currently has
   `state: SubmitState`, but no `attemptCount: Int`. Without this we can't
   tell which side of the backoff curve a given entry sits on.
2. **Per-entry next-retry-at timestamp** — `nextRetryAt: Long?` (or `Instant?`)
   so that the syncer can skip entries whose backoff window hasn't elapsed,
   instead of looping the whole batch on every connectivity edge.
3. **Room migration v9→v10** — non-trivial because the existing migration
   chain is already entangled with the original `SubmitOutbox` schema
   (`feature/loans`, `feature/bills`, plus the legacy crypto archive). The
   migration needs to:
   - Add two columns to `submit_outbox_entries` with safe defaults
     (`attemptCount = 0`, `nextRetryAt = null`).
   - Mirror the same shape on any fork-side outbox tables.
   - Backfill `attemptCount = 0` for all existing rows so the first
     post-migration retry uses the full backoff curve.
4. **`SubmitOutbox` write path** — `markFailed(id, message)` becomes
   `markFailed(id, message, nextRetryAt)`, and a new `incrementAttempt(id)`
   call replaces today's implicit "retry-on-reconnect" behavior. Every
   `SubmitOutbox` impl (Room-backed, fake, in-memory) must implement both.
5. **Retry loop** — `retryAll()` filters PENDING entries to those whose
   `nextRetryAt` is null or in the past, then per-entry calls
   `delay(retryPolicy.delayFor(entry.attemptCount + 1))` before re-attempting,
   and on failure calls `markFailed(id, msg, computeNextRetryAt(...))`.

## Why deferred

The Room migration touches every downstream module that builds a
`SubmitOutbox` (currently 2 production features + 1 archived feature group).
Phase 05's friction-reduction scope was the API surface (RetryPolicy +
OptimisticSubmit + ConflictStrategy + FetchPolicy.PERIODIC); the schema
migration belongs to the offline-resilience phase. Shipping the API in
Phase 05 means forks can declare their retry policy at Store construction
today and pick up the wired behavior the moment the migration lands —
no API break.

## Behavior today

`retryAll()` continues to fire every PENDING entry exactly once per
connectivity-edge transition, with no inter-entry delay. This is the same
behavior as before Phase 05 — the parameter is purely additive.

## Upgrade trigger

When the migration lands, this document will be revised to point at the
follow-up PLAN reference and the migration's commit hash. Until then, the
`@Suppress("unused")` annotation on `OfflineSubmitSyncer.retryPolicy` is the
breadcrumb signalling the deferred wire-in.

---

## Phase 06 follow-up: idempotency keys (Room v10→v11)

**Status:** Partial — `IdempotencyKey.generate()` ships + `BatchSubmitHandler`
threads the key through `submitBlock`; durable persistence to
`framework_submit_drafts` deferred.

### What ships today (Phase 06)

- **`template.core.base.store.submit.IdempotencyKey`** — `object` with a single
  `generate(): String` returning a Kotlin `Uuid.random().toString()`. KMP-safe
  (currently `@OptIn(ExperimentalUuidApi::class)`).
- **`BatchSubmitHandler.submitBatch(idempotencyKey: String = IdempotencyKey.generate(), ...)`**
  — the handler threads the key into the per-payload `submitBlock(index, payload, key)`
  signature so callers can attach it to an `Idempotency-Key` header (or equivalent).
- **`SubmitOutboxEntry.idempotencyKey: String? = null`** — new optional field on the
  common-main data class. **Default null. Currently not persisted** by any Room-
  backed `SubmitOutbox` — every `RoomSubmitOutbox.toEntity()` ignores the field
  and reconstructs entries with `idempotencyKey = null` on read.

### What's deferred (Room v10→v11)

To make the field durable across process restarts we need a Room migration that:

1. **Adds an `idempotency_key TEXT` column** to `framework_submit_drafts` with
   `DEFAULT NULL`.
2. **Mirrors the same shape** on any fork-side outbox tables.
3. **Backfills `idempotency_key = NULL`** for existing rows — pre-existing drafts
   never had a key and shouldn't suddenly be deduplicated server-side.
4. **Plumbs read/write through every concrete `SubmitOutbox` impl** — the
   `save` / `saveByUniqueKey` overloads gain an optional `idempotencyKey` parameter
   (defaulting to `null` for backwards-compat), and `getPending` / `observePending`
   surface the persisted value.

### Why deferred

Identical reasoning to Phase 05's retry-policy deferral: the migration touches
every downstream module that builds a `SubmitOutbox` (currently 2 production
features + 1 archived feature group), and Phase 06's scope was the in-memory
API surface (PushStoreAdapter / N-way combine / BatchSubmitHandler /
IdempotencyKey). Shipping the API now lets forks adopt the generate-and-thread
contract today; the persistence wire-in is a separate, additive PR with no API
break.

### Behaviour today

- **In-batch retries within the same process**: `BatchSubmitHandler` reuses
  the same key for every payload in the batch, so server-side dedup works for
  intra-batch retries already.
- **Cross-process retries via the outbox**: do **not** dedupe. After the
  v10→v11 migration lands, `OfflineSubmitSyncer.retryAll()` will read the
  persisted key off each `SubmitOutboxEntry` and attach it to the retry call.
