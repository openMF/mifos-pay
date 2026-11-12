# `project` — Supabase access-point package

SCAFFOLDED by `./gradlew syncForkConfig` from the `project` access point in
`app-profile/app.yaml#network.access_points`. One package per endpoint.

## Layout: `{supabase-project}/{table}/api` + `api/impl`

```
lwmswhoxvvoagzkqxiyd/           ← the access-point id == the Supabase project ref
  appserverconfig/             ← one package per TABLE (app_server_config)
    api/
      ServerInstanceApi.kt      ← interface — the contract, no Supabase types
      impl/
        ServerInstanceApiImpl.kt ← @ApiBinding(...) — the postgrest facade
    dto/
      RemoteServerInstanceDto.kt ← the wire types for THIS table
```

**Why the interface and the implementation are separated into two packages**, rather than sharing a
file the way a small API tempts you to:

- **Tests.** A consumer injects `ServerInstanceApi` and a test fakes it with a plain object — no
  `SupabaseConfigClient`, no network, no Koin. That is only possible while the contract is a type in
  its own right.
- **File-naming conflicts.** Detekt's `MatchingDeclarationName` (and ktlint's `standard:filename`)
  require a file with one top-level classlike to be named after it. Put `ServerInstanceApi` and
  `ServerInstanceApiImpl` in one file and the file can only be named for one of them — the other trips the
  rule. Two files in two packages sidesteps it instead of suppressing it.
- **The binding names the interface.** `network-ksp` resolves the impl's single supertype and emits
  `supabaseApi<ServerInstanceApi>("<id>") { ServerInstanceApiImpl(it) }`, so `get<ServerInstanceApi>()` resolves and
  nothing outside `api/impl/` ever names the implementation.

**`api/impl/` holds HAND-WRITTEN implementations only.** That is the whole rule, and it is why the REST
access points in this module (`coingecko`, `fineract`, `frankfurter`, `fred`, `jsonplaceholder`,
`worldbank`) have an `api/` with no `impl/` beside it: their API type is a Ktorfit interface and
Ktorfit GENERATES the implementation into `build/generated`, which the binding reaches as
`restApi("<id>") { it.create<Simple>() }`. There is no hand-written file to place, the interface is
already alone in its file so the naming rule is satisfied, and a test already fakes the interface — so
a delegating wrapper would add a layer to maintain and buy none of the three reasons above.

Supabase is the asymmetric case: supabase-kt has no interface-generation step, so the implementation
is written by hand — and that is exactly what belongs in `api/impl/`. The SEAM is identical on both
sides; only the AUTHOR of the implementation differs.

**`project` is a placeholder the fork renames.** The template cannot ship a real Supabase project, so
the access point id, the `base_url` host and this package are all the neutral word `project`. A fork
points them at its own project — the id becomes the project ref (the public identity in
`https://<ref>.supabase.co`, e.g. `azcxfedokrtlsyxueedn`), and the package is renamed to match. Rename
BOTH together: the annotation argument is matched against the declared access-point ids, so a package
rename alone leaves `@ApiBinding` naming an id app-profile no longer declares, which `network-ksp`
rejects at compile time (and NAP-4 catches in CI).

**One package per table, not one class per project.** A project with five tables has five API
interfaces, each with its own DTOs, rather than one class that grows without bound and drags every
table's wire types into every consumer.

## Interface + impl

`api/` holds BOTH, and the split is load-bearing:

- the **interface** is what consumers inject and what a test fakes — no `SupabaseConfigClient` needed
- the **impl** carries `@ApiBinding` (it is what gets constructed) and takes a single
  `SupabaseConfigClient` constructor argument, which is the whole contract `supabaseApi<T>` requires

The generated binding is `supabaseApi<ServerInstanceApi>("project") { ServerInstanceApiImpl(it) }` — the
processor resolves the impl's single supertype and binds THAT. Without the explicit type argument
`single<T>` would infer the impl and every `get<ServerInstanceApi>()` would miss at runtime, after
compiling cleanly. Give each API type exactly one interface; `network-ksp` errors on more than one
rather than guessing which to bind.

REST points are interfaces too — there Ktorfit generates the implementation, so only the interface is
written. The seam is identical on both sides; a caller should never be able to tell.

## Inert by default

The template declares the endpoint but ships no publishable key, so
`SupabaseConfigClient.isConfigured` is false and every call returns empty rather than throwing — an
unconfigured fork stays on the "no remote config, use defaults" path instead of crashing at start-up.
Construction never touches the network.

## Adding a table

1. `mkdir -p project/<table>/{api,dto}`
2. write the DTOs, then the interface, then the impl annotated `@ApiBinding("<access-point-id>")`
3. build — the Koin binding is generated; there is no wiring step

Delete this package by removing its access point from app-profile.
