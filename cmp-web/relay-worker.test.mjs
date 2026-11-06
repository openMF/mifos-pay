// Checks for cmp-web's relay worker (src/jsMain/resources/_worker.js). Run: node cmp-web/relay-worker.test.mjs
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { relay, isRelayedHost } from "./src/jsMain/resources/_worker.js";

const js = readFileSync(new URL("./src/jsMain/resources/_worker.js", import.meta.url), "utf8");
const wasm = readFileSync(new URL("./src/wasmJsMain/resources/_worker.js", import.meta.url), "utf8");
assert.equal(wasm, js, "the js and wasm workers must be identical");

assert.ok(isRelayedHost("mifos-bank-2.mifos.community"));
assert.ok(isRelayedHost("mifos.community"));
assert.ok(!isRelayedHost("evilmifos.community"));
assert.ok(!isRelayedHost("mifos.community.evil.com"));

const seen = [];
const fakeFetch = async (url, init) => {
  seen.push({ url, init });
  return new Response("{}", { status: 200, headers: { "Set-Cookie": "s=1", "Content-Type": "application/json" } });
};
const call = (target, init = {}) => {
  const u = new URL("https://site.pages.dev/cors/?" + target);
  return relay(new Request(u, init), u, fakeFetch);
};

// An authenticated POST is forwarded with its body and auth, without Origin/Cookie, and is not cached.
let r = await call("https://mifos-bank-2.mifos.community/fineract-provider/api/v1/self/authentication", {
  method: "POST",
  headers: { Origin: "https://site.pages.dev", Cookie: "c=1", Authorization: "Basic x", "Fineract-Platform-TenantId": "default", "Content-Type": "application/json" },
  body: '{"username":"u"}',
});
assert.equal(r.status, 200);
const sent = seen.at(-1);
assert.equal(sent.url, "https://mifos-bank-2.mifos.community/fineract-provider/api/v1/self/authentication");
assert.equal(sent.init.method, "POST");
assert.equal(new TextDecoder().decode(sent.init.body), '{"username":"u"}');
assert.equal(sent.init.headers.get("authorization"), "Basic x");
assert.equal(sent.init.headers.get("fineract-platform-tenantid"), "default");
assert.equal(sent.init.headers.get("origin"), null);
assert.equal(sent.init.headers.get("cookie"), null);
assert.equal(sent.init.redirect, "manual");
assert.equal(r.headers.get("set-cookie"), null);
assert.equal(r.headers.get("cache-control"), "no-store");

// A query string on the target survives.
await call("https://mifos-bank-1.mifos.community/api/x?a=1&b=2");
assert.equal(seen.at(-1).url, "https://mifos-bank-1.mifos.community/api/x?a=1&b=2");

// Never an open proxy.
const before = seen.length;
assert.equal((await call("https://example.com/")).status, 403);
assert.equal((await call("http://mifos-bank-2.mifos.community/")).status, 403);
assert.equal((await call("not a url")).status, 400);
assert.equal(seen.length, before, "a refused call must not reach upstream");

console.log("relay worker: all checks passed");
