// Karma runs the browser web tests through Mocha, whose default per-test timeout is 2000ms.
// The web invalidation tests wait in REAL time for a Room re-emission that may never arrive, so
// 2s is far too tight: a missing emission surfaces as "Timeout of 2000ms exceeded", which reads
// like a hung browser rather than the dropped invalidation signal it actually is.
//
// Set on the Karma CLIENT (not via `useMocha {}` in Gradle — that swaps the Mocha runner in for
// Karma entirely, which removes the browser and makes `Worker` undefined).
config.set({
    client: {
        mocha: {
            timeout: 30000,
        },
    },
});
