# Smart QR Code Payment Routing for MifosPay

## Overview

This document describes the approach for implementing intelligent QR code payment routing that automatically detects whether a transfer should be processed as **intra-bank** (same bank) or **inter-bank** (different bank).

---

## Problem Statement

When a user scans a QR code, the system needs to automatically determine:
- **Intra-bank transfer**: Both sender and receiver are in the SAME bank (same Fineract instance)
- **Inter-bank transfer**: Sender and receiver are in DIFFERENT banks (requires payment hub/Mojaloop)

---

## Industry Standards Reference

### 1. Mojaloop Scheme Rules
> "The scheme should standardize the format of QR code to include **FSP ID**, Party ID Type and Party ID"
>
> "Each participant will be issued a **unique FSP ID** by the scheme... FSP should **prepend the FSP ID to merchant code** so that the merchant code is unique across all participants"

Source: [Mojaloop Scheme Rules](https://docs.mojaloop.io/api/fspiop/scheme-rules.html)

### 2. UPI Virtual Payment Address (VPA)
> VPA format: `username@bankhandle` (e.g., `rajan@icici`, `john@paytm`)

The **bank handle** (`@icici`, `@paytm`) identifies which bank/PSP to route to.

Source: [Razorpay VPA Guide](https://razorpay.com/learn/what-is-virtual-payment-address-vpa/)

### 3. EMVCo QR Code Standard
> A single QR code can support **multiple payment networks** (Visa, Mastercard, domestic schemes)... includes **merchant identifier with acquirer routing**

Source: [EMVCo QR Specifications](https://www.emvco.com/knowledge-hub/qr-codes-are-here-what-do-they-mean-for-payments/)

### 4. IBAN/BIC (Europe)
> "IBAN carries all routing information needed... contains **country code, bank code, branch code**, and account number"

The first 4-8 characters identify the bank, enabling instant same-bank vs different-bank detection.

Source: [Wikipedia - IBAN](https://en.wikipedia.org/wiki/International_Bank_Account_Number)

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        MifosPay QR Payment System                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   USER A (Bank 1)                              USER B (Bank 1 or Bank 2)    │
│   ┌─────────────┐                              ┌─────────────┐              │
│   │  Generate   │                              │    Scan     │              │
│   │  QR Code    │ ──────── QR Code ──────────► │   QR Code   │              │
│   │             │                              │             │              │
│   └─────────────┘                              └──────┬──────┘              │
│                                                       │                     │
│                                                       ▼                     │
│                                        ┌──────────────────────────┐         │
│                                        │   Detect Transfer Type   │         │
│                                        │   (Same Bank or Other?)  │         │
│                                        └────────────┬─────────────┘         │
│                                                     │                       │
│                              ┌──────────────────────┴──────────────────┐    │
│                              ▼                                         ▼    │
│                    ┌─────────────────┐                     ┌───────────────┐│
│                    │   INTRA-BANK    │                     │  INTER-BANK   ││
│                    │   (Same Bank)   │                     │ (Other Bank)  ││
│                    └────────┬────────┘                     └───────┬───────┘│
│                             │                                      │        │
│                             ▼                                      ▼        │
│                    ┌─────────────────┐                     ┌───────────────┐│
│                    │ Direct Transfer │                     │   Mojaloop/   ││
│                    │  via Fineract   │                     │  Payment Hub  ││
│                    └─────────────────┘                     └───────────────┘│
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## QR Code Structure

### Current Format
```
mpay://pay?qt=0&ci=123&am=100&cn=John&an=ACC001&ai=456&cu=USD&oi=1&pi=2
```

### New Format (with FSP ID)
```
mpay://pay?qt=0&fsp=mifos-bank-1&ci=123&am=100&cn=John&an=ACC001&ai=456&cu=USD&oi=1&pi=2&ae=EXT123
         │     │                │                                                      │
         │     │                │                                                      └── Account External ID
         │     │                └── Internal IDs (clientId, accountId, etc.)
         │     └── FSP/Bank Identifier (NEW)
         └── QR Type (0=INTRA, 1=INTER, etc.)
```

### Field Reference

| Field | Key | Intra-Bank | Inter-Bank | Description |
|-------|-----|------------|------------|-------------|
| QR Type | `qt` | 0 | 1 | Transfer type indicator |
| **FSP ID** | `fsp` | ✅ Required | ✅ Required | Bank/Tenant identifier |
| Client ID | `ci` | ✅ Required | ❌ Not needed | Internal client ID |
| Account ID | `ai` | ✅ Required | ❌ Not needed | Internal account ID |
| Office ID | `oi` | ✅ Required | ❌ Not needed | Internal office ID |
| Account External ID | `ae` | ✅ Optional | ✅ Required | For participant lookup |
| Client Name | `cn` | ✅ Required | ✅ Optional | Display name |
| Account No | `an` | ✅ Required | ❌ Not needed | Account number |
| Amount | `am` | ✅ Optional | ✅ Optional | Pre-filled amount |
| Currency | `cu` | ✅ Required | ✅ Required | Currency code |

---

## Flow Diagrams

### Flow A: QR Code Generation

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         QR CODE GENERATION                               │
└──────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌──────────────────────────────────────────────────────────────────────────┐
│  1. User opens "Receive Money" / "My QR" screen                          │
└──────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌──────────────────────────────────────────────────────────────────────────┐
│  2. Load user data from UserPreferencesRepository:                       │
│     - client.id, client.displayName, client.officeId                     │
│     - defaultAccount.accountId, defaultAccount.accountNo                 │
│     - selectedInstance.tenantId (FSP ID)                                 │
│     - accountExternalId (for inter-bank)                                 │
└──────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌──────────────────────────────────────────────────────────────────────────┐
│  3. User selects QR Type:                                                │
│     ┌─────────────────┐    ┌─────────────────┐                           │
│     │   INTRA-BANK    │    │   INTER-BANK    │                           │
│     │  (Same Bank)    │    │  (Any Bank)     │                           │
│     └────────┬────────┘    └────────┬────────┘                           │
│              │                      │                                    │
│              ▼                      ▼                                    │
│     ┌─────────────────┐    ┌─────────────────┐                           │
│     │ Include all     │    │ Include only:   │                           │
│     │ internal IDs    │    │ - fspId         │                           │
│     │ + fspId         │    │ - accountExtId  │                           │
│     │ + accountExtId  │    │ - amount/curr   │                           │
│     └─────────────────┘    └─────────────────┘                           │
└──────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌──────────────────────────────────────────────────────────────────────────┐
│  4. Encode QR Data:                                                      │
│     MpayQrCodeProcessor.encodeMpayString(qrCodeData) → Base64 string     │
└──────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌──────────────────────────────────────────────────────────────────────────┐
│  5. Display QR Code to user                                              │
└──────────────────────────────────────────────────────────────────────────┘
```

### Flow B: QR Code Scanning & Routing

```
┌──────────────────────────────────────────────────────────────────────────┐
│                    QR CODE SCANNING & ROUTING                            │
└──────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌──────────────────────────────────────────────────────────────────────────┐
│  1. User scans QR Code                                                   │
│     - Camera scan OR                                                     │
│     - Upload from gallery                                                │
└──────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌──────────────────────────────────────────────────────────────────────────┐
│  2. Decode QR Data                                                       │
│     MpayQrCodeProcessor.decodeMpayString(base64) → QrCodeData            │
└──────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌──────────────────────────────────────────────────────────────────────────┐
│  3. DETECT TRANSFER TYPE                                                 │
│                                                                          │
│     val scannerFspId = userPreferences.selectedInstance.tenantId         │
│     val qrFspId = qrCodeData.fspId                                       │
│                                                                          │
│     ┌─────────────────────────────────────────────────────────────────┐  │
│     │  if (qrFspId == scannerFspId) {                                 │  │
│     │      // SAME BANK → Intra-bank transfer                         │  │
│     │  } else {                                                       │  │
│     │      // DIFFERENT BANK → Inter-bank transfer                    │  │
│     │  }                                                              │  │
│     └─────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    ▼                               ▼
┌───────────────────────────────┐   ┌───────────────────────────────┐
│       INTRA-BANK FLOW         │   │       INTER-BANK FLOW         │
│       (Same Bank)             │   │       (Different Bank)        │
└───────────────┬───────────────┘   └───────────────┬───────────────┘
                │                                   │
                ▼                                   ▼
┌───────────────────────────────┐   ┌───────────────────────────────┐
│  4a. Check Beneficiary Exists │   │  4b. Navigate to Inter-bank   │
│      in local database        │   │      Transfer Screen          │
│                               │   │      (with accountExternalId) │
│  beneficiaryRepo.findBy(      │   │                               │
│    accountId OR accountNo     │   │  InterbankTransferScreen(     │
│  )                            │   │    accountExternalId,         │
│                               │   │    recipientName,             │
└───────────────┬───────────────┘   │    amount                     │
                │                   │  )                            │
    ┌───────────┴───────────┐       └───────────────────────────────┘
    ▼                       ▼
┌─────────────┐     ┌─────────────┐
│  EXISTS     │     │ NOT EXISTS  │
└──────┬──────┘     └──────┬──────┘
       │                   │
       ▼                   ▼
┌─────────────────┐ ┌─────────────────┐
│ 5a. Go to Send  │ │ 5b. Direct      │
│ Money Screen    │ │ Transfer OR     │
│ (pre-filled     │ │ Add Beneficiary │
│ with QR data)   │ │ first           │
└─────────────────┘ └─────────────────┘
```

---

## Routing Logic (Pseudocode)

```kotlin
fun routeQrScan(qrData: QrCodeData): RouteResult {
    val currentFspId = userPreferences.selectedInstance.tenantId
    val qrFspId = qrData.fspId

    // Step 1: Determine if same bank or different bank
    val isSameBank = when {
        // If QR has fspId, compare directly
        qrFspId != null && currentFspId != null -> qrFspId == currentFspId

        // If QR type is explicitly INTER_BANK
        qrData.type == QrCodeType.INTER_BANK -> false

        // If QR has internal IDs (clientId > 0), assume same bank
        qrData.clientId > 0 && qrData.accountId > 0 -> true

        // Default: treat as inter-bank if only accountExternalId present
        else -> false
    }

    // Step 2: Route accordingly
    return if (isSameBank) {
        // Check if beneficiary exists
        val beneficiary = beneficiaryRepo.findByAccountId(qrData.accountId)

        if (beneficiary != null) {
            RouteResult.IntraBank.ExistingBeneficiary(beneficiary, qrData)
        } else {
            RouteResult.IntraBank.NewTransfer(qrData)
        }
    } else {
        RouteResult.InterBank(
            accountExternalId = qrData.accountExternalId,
            recipientName = qrData.clientName,
            amount = qrData.amount
        )
    }
}
```

---

## Testing Scenarios

| # | Scenario | QR Generated By | Scanned By | Expected Result |
|---|----------|-----------------|------------|-----------------|
| 1 | Same bank, beneficiary exists | User A (Bank 1) | User B (Bank 1) | → Send Money (pre-filled) |
| 2 | Same bank, new beneficiary | User A (Bank 1) | User B (Bank 1) | → MakeTransferV2 |
| 3 | Different bank | User A (Bank 1) | User C (Bank 2) | → Interbank Transfer |
| 4 | Inter-bank QR type | User A (Bank 1) | User B (Bank 1) | → Interbank Transfer |
| 5 | Legacy QR (no fspId) | Old app | New app | → Fallback to type detection |

---

## Files to Modify

| File | Action | Priority |
|------|--------|----------|
| `QrCodeData.kt` | Add `fspId` field | HIGH |
| `MpayQrCodeProcessor.kt` | Encode/decode `fspId` | HIGH |
| `QrTransferRouter.kt` | **NEW** - Routing logic | HIGH |
| `MpayQrViewModel.kt` | Include `fspId` in QR | HIGH |
| `ScanQrViewModel.kt` | Use `QrTransferRouter` | HIGH |
| `ForkNavHost.kt` | Update navigation callbacks | MEDIUM |
| `BeneficiaryRepository.kt` | Add lookup methods | MEDIUM |
| `DataModule.kt` | Add DI binding | LOW |

---

## Benefits

1. **Automatic Detection**: No user intervention needed to choose transfer type
2. **Industry Standard**: Follows Mojaloop/UPI/EMVCo best practices
3. **Backward Compatible**: Works with legacy QR codes (fallback logic)
4. **Offline Capable**: FSP ID comparison doesn't require network call
5. **Extensible**: Easy to add new routing rules in the future

---

## References

- [Mojaloop Scheme Rules](https://docs.mojaloop.io/api/fspiop/scheme-rules.html)
- [EMVCo QR Specifications](https://www.emvco.com/knowledge-hub/qr-codes-are-here-what-do-they-mean-for-payments/)
- [UPI Virtual Payment Address](https://razorpay.com/learn/what-is-virtual-payment-address-vpa/)
- [World Bank - QR Codes in Payments](https://fastpayments.worldbank.org/sites/default/files/2021-10/QR_Codes_in_Payments_Final.pdf)
- [IBAN Structure](https://en.wikipedia.org/wiki/International_Bank_Account_Number)
