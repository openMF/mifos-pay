# `jsonplaceholder` — access point package

SCAFFOLDED by `./gradlew syncForkConfig` from the `jsonplaceholder` access point in
`app-profile/app.yaml#network.access_points`. One package per endpoint.

- `api/` — the Ktorfit interface for this endpoint. Annotate it
  `@ApiBinding("jsonplaceholder")` and its Koin binding is GENERATED into
  `di/GeneratedApiBindings.kt`; there is no wiring step.
- `dto/` — the wire types this endpoint returns.

`type: rest` — the binding is `restApi("jsonplaceholder") { it.create${simple}Api() }`, so Ktorfit
generates the `create…Api()` factory from the interface.

Delete this package by removing its access point from app-profile.
