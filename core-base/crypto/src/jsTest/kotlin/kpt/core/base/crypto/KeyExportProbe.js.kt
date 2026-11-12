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

import kotlinx.coroutines.await
import kotlin.js.Promise

private fun probe(): dynamic = js(
    """
    (new Promise(function (res, rej) {
      var r = indexedDB.open('kpt-secure', 1);
      r.onupgradeneeded = function () { r.result.createObjectStore('keys'); };
      r.onsuccess = function () { res(r.result); };
      r.onerror = function () { rej(r.error); };
    }).then(function (db) {
      return new Promise(function (res, rej) {
        var q = db.transaction('keys', 'readonly').objectStore('keys').get('aes-gcm-v1');
        q.onsuccess = function () { res(q.result); };
        q.onerror = function () { rej(q.error); };
      });
    }).then(function (key) {
      if (!key) return false;
      return crypto.subtle.exportKey('raw', key).then(
        function () { return true; },
        function () { return false; }
      );
    }))
    """,
)

@Suppress("UNCHECKED_CAST")
internal actual suspend fun canExportStoredKey(): Boolean = (probe() as Promise<Boolean>).await()
