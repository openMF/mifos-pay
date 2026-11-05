/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.crypto

/**
 * Reads the persisted `CryptoKey` straight out of IndexedDB and attempts `crypto.subtle.exportKey`
 * on it, returning whether the export SUCCEEDED.
 *
 * This is the probe for the one property that makes web storage worth encrypting at all: the key is
 * generated with `extractable: false`, so no script — including injected script — can export its raw
 * bytes and use them offline. Asserting that from outside [WebSecureCrypto], against the real stored
 * key rather than the object the class happens to hold, is what makes it a test of the guarantee and
 * not of our own bookkeeping.
 *
 * Returns false when no key is stored yet.
 */
internal expect suspend fun canExportStoredKey(): Boolean
