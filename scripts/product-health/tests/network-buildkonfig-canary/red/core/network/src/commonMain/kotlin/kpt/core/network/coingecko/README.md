# `coingecko` — access point package

SCAFFOLDED by `./gradlew syncForkConfig` from the `coingecko` access point in
`app-profile/app.yaml#network.access_points`. One package per endpoint.

- `api/` — the Ktorfit interface for this endpoint. Annotate it
  `@ApiBinding("coingecko")` and its Koin binding is GENERATED into
  `di/GeneratedApiBindings.kt`; there is no wiring step.
- `dto/` — the wire types this endpoint returns.

`type: rest` — the binding is `restApi("coingecko") { it.create${simple}Api() }`, so Ktorfit
generates the `create…Api()` factory from the interface.

Delete this package by removing its access point from app-profile.
