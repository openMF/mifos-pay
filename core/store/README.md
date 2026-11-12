# `core/store` — Consumer Customization Seam

> See training-layer/instructions/stream-first/latest/CORE_STORE.md

The single unified build guide (archetype decision matrix, screen-type → framework-API taxonomy,
customization seam) is the sole source of truth. This README is a redirect stub (white-label E2/T3).

## What you get for free

- **Store5 factories keyed by `store_archetype`** — declare a feature's `store_archetype` (one of the
  8: `NETWORK_WITH_CACHE | MUTABLE | OFFLINE_LOCAL_ONLY | NETWORK_ONLY | CACHE_ONLY | PERIODIC |
  MEMORY_ONLY | LOAD_ONCE`) and codegen emits the matching `StoreFactory.create*` factory + `FetchPolicy`.
- **`KptScreenStateDefaults`** — brand your loading / empty / error / no-network visuals in one place.
- **`KptErrorMapper`** — domain-error → user-message mapping (`categorize()`).
- **`AppStoreRegistry`** — feature-tagged Store5 factory qualifiers + TTLs.
- **`appStoreModule`** — Koin DI for the above.
- **Write side** — the unified `BaseMutationViewModel` + `MutationMode` (`InSession` / `Draft`) idiom
  for every mutation screen, composed with `SubmitHandler` / `DraftSubmitHandler`.

See the archetype decision matrix + module chain in `FEATURE_AUTHORING.md` and
`docs/architecture/STORE_DATA_API.md`.
