# `project` — access point package

SCAFFOLDED by `./gradlew syncForkConfig` from the `project` access point in
`app-profile/app.yaml#network.access_points`. One package per endpoint.

- `api/` — the Ktorfit interface for this endpoint. Annotate it
  `@ApiBinding("project")` and its Koin binding is GENERATED into
  `di/GeneratedApiBindings.kt`; there is no wiring step.
- `dto/` — the wire types this endpoint returns.

`type: supabase` — the binding is `supabaseApi("project") { ${simple}Api(it) }`, so the
interface takes a single `SupabaseConfigClient` constructor argument.

Delete this package by removing its access point from app-profile.
