# `cmp-web/`

> **Kind:** Project area  
> **Measured:** 22 tracked files — 6× `.js`, 3× `.kt`, 2× `.md`, 2× `.ico`, 2× `.html`

## Shape

| Path | Files |
|---|---:|
| `cmp-web/src/` | 15 |
| (files at the root) | 5 |
| `cmp-web/webpack.config.d/` | 2 |

## Docs in the tree

_Authored beside the code, where the module's own consumers read them._

| Path | |
|---|---|
| `cmp-web/CONSUMPTION.md` | Consuming `cmp-web` in a fork |
| `cmp-web/README.md` |  |

<!-- tree-scaffold:end -->
## Significance

The Kotlin/JS and Kotlin/Wasm browser targets. Both compile the same shared Compose app; they are
separate source sets only because the two Kotlin backends do not share an interop layer.

**Web has one ordering constraint worth knowing.** Secure storage — passcode, auth state — is AES-GCM
encrypted under a **non-extractable** WebCrypto key, and decrypting it is asynchronous. Koin builds
the secure `Settings` from that store, so `SecureSettingsFactory.warmUp()` must COMPLETE before
`initKoin()`. Starting Koin first makes the very first read race the key load and the factory throws —
which is why `main()` is a coroutine rather than a straight call.
