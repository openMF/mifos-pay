/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package kpt.core.base.crypto

import kotlinx.coroutines.await
import kotlin.js.JsAny
import kotlin.js.JsString
import kotlin.js.Promise

/** The helper object installed on `globalThis` by [installHelpers]. */
private external interface KptSecureHelpers : JsAny {
    fun loadOrCreateKey(): Promise<JsAny>
    fun encrypt(key: JsAny, plaintext: String): Promise<JsString>
    fun decrypt(key: JsAny, b64: String): Promise<JsString>
}

/**
 * Installs the shared crypto helpers on `globalThis` once and returns them.
 *
 * The JS body is byte-identical to the one in the js actual — see that file for why it is written
 * in Promise-chain style. `generateKey(..., false, ...)` is the non-extractable flag.
 */
@JsFun(
    """() => (
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
)""",
)
private external fun installHelpers(): KptSecureHelpers

/**
 * Browser crypto for the WasmJS target. Same WebCrypto surface as the JS actual, kept separate only because the two
 * targets do not share an interop layer.
 */
actual class WebSecureCrypto {

    private var key: JsAny? = null

    /** `warmUp` on this target. Web (WasmJS). */
    actual suspend fun warmUp() {
        if (key == null) key = installHelpers().loadOrCreateKey().await()
    }

    private fun requireKey(): JsAny =
        requireNotNull(key) { "WebSecureCrypto.warmUp() must complete before use" }

    /** `encrypt` on this target. Web (WasmJS). */
    actual suspend fun encrypt(plaintext: String): String =
        installHelpers().encrypt(requireKey(), plaintext).await().toString()

    /** `decrypt` on this target. Web (WasmJS). */
    actual suspend fun decrypt(ciphertext: String): String =
        installHelpers().decrypt(requireKey(), ciphertext).await().toString()
}
