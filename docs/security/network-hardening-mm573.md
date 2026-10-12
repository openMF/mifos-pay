## MM-573 Network Hardening Notes

This document captures the security hardening added to dynamic network configuration and request headers in `mifos-pay`.

### Why this change

The app reads instance configuration (protocol, endpoint, path, tenant) dynamically. Without strict validation, malformed values can produce unsafe request targets or unsafe header values.

### What was hardened

- Sanitized tenant ID before adding `Fineract-Platform-TenantId` header.
- Sanitized Authorization token values before header insertion.
- Enforced HTTPS-only protocol from dynamic config (invalid protocol falls back to default secure instance).
- Sanitized endpoint and path from dynamic config before URL construction.
- Added safe defaults for invalid protocol/endpoint/path/tenant values.
- Restricted auth header attachment to HTTPS requests (local dev hosts allowed for emulator/local testing).

### Scope in code

- `core/network/src/commonMain/kotlin/org/mifospay/core/network/config/InstanceConfigManager.kt`
- `core/network/src/commonMain/kotlin/org/mifospay/core/network/utils/KtorInterceptor.kt`

### Security impact

- Reduces header injection risk from CR/LF in tenant/token values.
- Reduces risk of accidental insecure transport due to malformed instance protocol.
- Prevents malformed endpoint/path values from being used directly in request URL generation.

### Follow-up items

- Add shared KMP unit tests for sanitizers in `core/network`.
- Add telemetry for invalid instance config fallbacks (without logging sensitive values).
- Evaluate certificate pinning integration in the HTTP stack as part of high-severity items.
