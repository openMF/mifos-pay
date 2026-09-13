# :feature:pocket module

Pocket is the client account-linking feature. It presents linked loan, savings, and share accounts together, calculates the displayed Pocket balance, and lets the client add or remove account links.

## Screens and navigation

The feature has two destinations:

- `PocketDashboardRoute` displays linked accounts grouped by loan, savings, and share type. Selecting an account emits an event for the matching account-detail feature.
- `ManagePocketRoute` displays linked accounts and provides link-account and delink-confirmation flows. Linking supports account-type tabs, search, and multi-selection.

`PocketDashboardViewModel` and `ManagePocketViewModel` consume `ScreenState` values from the repository streams and map them into UI models used by the Compose screens. `PocketModule` provides the feature dependencies.

## Dashboard behavior

`PocketDashboardViewModel` maps `DetailedPocketAccount` values into `PocketBuckets`:

- Accounts are grouped into savings, loan, and share lists.
- Active balances are formatted with currency code, symbol, and decimal places.
- Non-active accounts display their `AccountStatus`.
- Active balances are aggregated by currency for the total balance.
- Loading, empty, network, error, freshness, retry, and refresh states come from the Pocket stream.

## Manage Pocket behavior

`ManagePocketViewModel` maintains linked and available Pocket streams, selection state, account-type tabs, and search results. Search is performed locally over the available stream using product name and account number.

Link and delink submissions use the shared submit-state handler. After a successful mutation, the ViewModel refreshes the linked and available streams; failures map to the feature’s localized error strings.

## Repository and data flow

Repository: [`PocketRepositoryImp`](../../core/data/src/commonMain/kotlin/org/mifospay/core/data/repositoryImpl/PocketRepositoryImp.kt)

Store: [`PocketStore`](../../core/store/src/commonMain/kotlin/kpt/core/store/wallet/pocket/PocketStore.kt)

Key: [`PocketKey`](../../core/store/src/commonMain/kotlin/kpt/core/store/wallet/pocket/PocketKey.kt)

The Pocket store fetches basic Pocket mappings, joins them with the client account response, and enriches share accounts with current market-price data. Its Room source of truth is the client-scoped `wallet_pockets` table:

- `PocketDao.observeByClient` provides the reactive page read.
- `PocketDao.replacePage` atomically replaces a client page while preserving pending local changes.
- `PocketEntity` stores the mapping, account details, fetched timestamp, and synchronization status.
- `PocketKey.clientId` scopes both the page and the client-account enrichment request.

`PocketRepositoryImp` exposes streams for detailed linked accounts, linked Pocket accounts, and accounts available to link. The streams are converted to `ScreenDataStream` values so the feature receives content, empty, network, error, and freshness information consistently.

Link and delink mutations are optimistic. A link is written locally with a temporary negative mapping ID when necessary. A failed link or delink remains marked as pending. The Pocket fetcher pushes pending changes before pulling the server list, marks successful links as synced, removes successful delinks, and then writes the refreshed page.

## Repository tests

[`PocketRepositoryTest`](../../core/data/src/commonTest/kotlin/org/mifospay/core/data/repositoryImpl/PocketRepositoryTest.kt) verifies the repository’s Store5 stream contract with fake Store5 stores, a deterministic network monitor, and a fake fetched-at repository.

The tests cover:

- Reading detailed Pocket accounts from the Pocket store.
- Reading linked Pocket accounts from the linked-account stream.
- Reading available accounts from the linkable-account store.
- Failing clearly when a required Pocket or linkable-account store is not provided.

Store and DAO behavior is covered by the Pocket implementation under `core/store` and `core/database`. Feature-level ViewModel and Compose tests are under `feature/pocket/src/commonTest`.

## Source layout

```text
feature/pocket/src/commonMain/kotlin/org/mifospay/feature/pocket/
├── di/PocketModule.kt
├── navigation/
│   ├── PocketDashboardRoute.kt
│   └── ManagePocketRoute.kt
├── screens/
│   ├── PocketDashboardScreen.kt
│   └── ManagePocketScreen.kt
└── viewmodels/
    ├── PocketDashboardViewModel.kt
    └── ManagePocketViewModel.kt
```

Feature tests are under `feature/pocket/src/commonTest`, with dashboard, management, link, and delink coverage. Pocket data support lives in `core/model`, `core/network`, `core/database`, `core/store`, and `core/data`.
