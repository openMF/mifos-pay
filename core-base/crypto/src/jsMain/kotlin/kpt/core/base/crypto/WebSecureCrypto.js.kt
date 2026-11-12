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

/**
 * Installs the shared crypto helpers on `globalThis` once and returns them.
 *
 * The JS body is byte-identical to the one in the wasmJs actual: one behaviour, two thin bindings.
 * It is written in Promise-chain style rather than async/await because the Kotlin/JS `js()`
 * intrinsic parses its argument at compile time with a parser that rejects async/await — and
 * matching that constraint on both sides keeps the two targets from drifting.
 *
 * `generateKey(..., false, ...)` — that `false` is the non-extractable flag, the security property
 * this whole class exists for.
 */
private fun helpers(): dynamic = js(
    """
    (function () {
      if (globalThis.__kptSecure) return globalThis.__kptSecure;
      function openDb() {
        return new Promise(function (res, rej) {
          var r = indexedDB.open('kpt-secure', 1);
          r.onupgradeneeded = function () { r.result.createObjectStore('keys'); };
          r.onsuccess = function () { res(r.result); };
          r.onerror = function () { rej(r.error); };
        });
      }
      globalThis.__kptSecure = {
        loadOrCreateKey: function () {
          return openDb().then(function (db) {
            return new Promise(function (res, rej) {
              var q = db.transaction('keys', 'readonly').objectStore('keys').get('aes-gcm-v1');
              q.onsuccess = function () { res(q.result); };
              q.onerror = function () { rej(q.error); };
            }).then(function (existing) {
              if (existing) return existing;
              return crypto.subtle.generateKey(
                { name: 'AES-GCM', length: 256 }, false, ['encrypt', 'decrypt']
              ).then(function (key) {
                return new Promise(function (res, rej) {
                  var q = db.transaction('keys', 'readwrite').objectStore('keys').put(key, 'aes-gcm-v1');
                  q.onsuccess = function () { res(key); };
                  q.onerror = function () { rej(q.error); };
                });
              });
            });
          });
        },
        encrypt: function (key, plaintext) {
          var iv = crypto.getRandomValues(new Uint8Array(12));
          return crypto.subtle.encrypt(
            { name: 'AES-GCM', iv: iv }, key, new TextEncoder().encode(plaintext)
          ).then(function (ct) {
            var body = new Uint8Array(ct);
            var out = new Uint8Array(iv.length + body.length);
            out.set(iv, 0);
            out.set(body, iv.length);
            var s = '';
            for (var i = 0; i < out.length; i++) s += String.fromCharCode(out[i]);
            return btoa(s);
          });
        },
        decrypt: function (key, b64) {
          var bin = atob(b64);
          var raw = new Uint8Array(bin.length);
          for (var i = 0; i < bin.length; i++) raw[i] = bin.charCodeAt(i);
          return crypto.subtle.decrypt(
            { name: 'AES-GCM', iv: raw.slice(0, 12) }, key, raw.slice(12)
          ).then(function (pt) { return new TextDecoder().decode(pt); });
        }
      };
      return globalThis.__kptSecure;
    })()
    """,
)

/**
 * Browser crypto for the JS target, over WebCrypto's SubtleCrypto. Every operation is asynchronous because the
 * underlying key is NON-EXTRACTABLE, which is also why the store must be warmed up before Koin builds anything that
 * reads it.
 */
actual class WebSecureCrypto {

    private var key: dynamic = null

    /** `warmUp` on this target. Web (JS). */
    actual suspend fun warmUp() {
        if (key == null) {
            @Suppress("UNCHECKED_CAST")
            key = (helpers().loadOrCreateKey() as Promise<dynamic>).await()
        }
    }

    private fun requireKey(): dynamic =
        requireNotNull(key) { "WebSecureCrypto.warmUp() must complete before use" }

    /** `encrypt` on this target. Web (JS). */
    @Suppress("UNCHECKED_CAST")
    actual suspend fun encrypt(plaintext: String): String =
        (helpers().encrypt(requireKey(), plaintext) as Promise<String>).await()

    /** `decrypt` on this target. Web (JS). */
    @Suppress("UNCHECKED_CAST")
    actual suspend fun decrypt(ciphertext: String): String =
        (helpers().decrypt(requireKey(), ciphertext) as Promise<String>).await()
}
