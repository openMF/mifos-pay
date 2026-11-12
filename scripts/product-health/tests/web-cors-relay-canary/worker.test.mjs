// Behaviour test for cmp-web's _worker.js CORS relay — run by run.sh with plain `node` (no deps).
// The worker is loaded from a data: URL (fresh per case, so its per-isolate config cache starts empty)
// against a stub env.ASSETS and a recording fetch. Each case prints `ok <id>` or `FAIL <id>: why`.
import { readFileSync } from "node:fs";

const workerPath = process.argv[2];
const source = readFileSync(workerPath, "utf8");
let n = 0;
let failed = 0;

async function freshWorker() {
  const code = `${source}\n// instance ${n++}`;
  return (await import("data:text/javascript;base64," + Buffer.from(code).toString("base64"))).default;
}

function env(allowlist, extra = {}) {
  return {
    ASSETS: {
      async fetch(req) {
        const path = new URL(req.url).pathname;
        if (path === "/relay-allowlist.json") {
          return allowlist ? Response.json(allowlist) : new Response("not found", { status: 404 });
        }
        return new Response("<html>", { headers: { "Content-Type": "text/html" } });
      },
    },
    ...extra,
  };
}

const calls = [];
let reply = () => new Response("{}", { status: 200 });
globalThis.fetch = async (target, init) => {
  const body = init.body === undefined ? undefined : Buffer.from(init.body).toString();
  calls.push({ url: String(target), method: init.method, headers: new Headers(init.headers), body, redirect: init.redirect });
  return reply(String(target), init);
};

async function check(id, fn) {
  calls.length = 0;
  reply = () => new Response("{}", { status: 200 });
  try {
    await fn();
    console.log(`ok ${id}`);
  } catch (e) {
    failed++;
    console.log(`FAIL ${id}: ${e.message}`);
  }
}
function assert(cond, msg) {
  if (!cond) throw new Error(msg);
}

const LIST = { mode: "relay", relay: "/cors", hosts: ["bank.mifos.community", "*.example.com"] };
const SITE = "https://app.pages.dev";
const req = (target, init = {}) => new Request(`${SITE}/cors/?${target}`, init);
const tagged = (res) => res.headers.get("x-kpt-relay") === "1";

await check("WCR-1 allowed POST forwards body + credentials, drops browser headers", async () => {
  const w = await freshWorker();
  const res = await w.fetch(
    req("https://bank.mifos.community/fineract-provider/api/v1/authentication", {
      method: "POST",
      body: '{"username":"u"}',
      headers: {
        Authorization: "Basic dTpw",
        "Fineract-Platform-TenantId": "default",
        "Content-Type": "application/json",
        Origin: SITE,
        Referer: `${SITE}/`,
        Cookie: "s=1",
        "CF-Connecting-IP": "1.2.3.4",
        "X-Forwarded-For": "1.2.3.4",
        "X-Real-IP": "1.2.3.4",
      },
    }),
    env(LIST),
  );
  assert(res.status === 200, `status ${res.status}`);
  const c = calls[0];
  assert(c && c.method === "POST" && c.url === "https://bank.mifos.community/fineract-provider/api/v1/authentication", "not forwarded as POST to the target");
  assert(c.body === '{"username":"u"}', `body ${c.body}`);
  assert(c.headers.get("authorization") === "Basic dTpw" && c.headers.get("fineract-platform-tenantid") === "default", "credential headers lost");
  for (const h of ["origin", "referer", "cookie", "cf-connecting-ip", "x-forwarded-for", "x-real-ip", "content-length", "host"]) {
    assert(!c.headers.has(h), `${h} leaked upstream`);
  }
  assert(c.redirect === "manual", "redirects must not be auto-followed");
});

await check("WCR-2 a host off the allowlist is 403 and never fetched", async () => {
  const res = await (await freshWorker()).fetch(req("https://evil.test/x"), env(LIST));
  assert(res.status === 403 && calls.length === 0, `status ${res.status}, ${calls.length} upstream call(s)`);
});

await check("WCR-3 *.example.com matches subdomains, not the apex", async () => {
  const w = await freshWorker();
  assert((await w.fetch(req("https://api.example.com/v1"), env(LIST))).status === 200, "subdomain refused");
  assert((await w.fetch(req("https://example.com/v1"), env(LIST))).status === 403, "apex allowed");
  assert((await w.fetch(req("https://evilexample.com/v1"), env(LIST))).status === 403, "suffix look-alike allowed");
});

await check("WCR-4 http and explicit ports are refused", async () => {
  const w = await freshWorker();
  assert((await w.fetch(req("http://bank.mifos.community/x"), env(LIST))).status === 403, "http allowed");
  assert((await w.fetch(req("https://bank.mifos.community:8443/x"), env(LIST))).status === 403, "port allowed");
  assert(calls.length === 0, "fetched anyway");
});

await check("WCR-5 Set-Cookie stripped, no-store when authed, CORP same-origin", async () => {
  reply = () => new Response("{}", { headers: { "Set-Cookie": "JSESSIONID=1", "Cache-Control": "max-age=600" } });
  const w = await freshWorker();
  const authed = await w.fetch(req("https://bank.mifos.community/c", { headers: { Authorization: "Basic x" } }), env(LIST));
  assert(!authed.headers.has("set-cookie"), "Set-Cookie leaked");
  assert(authed.headers.get("cache-control") === "no-store", `cache-control ${authed.headers.get("cache-control")}`);
  assert(authed.headers.get("cross-origin-resource-policy") === "same-origin", "CORP missing");
  const anon = await w.fetch(req("https://bank.mifos.community/c"), env(LIST));
  assert(anon.headers.get("cache-control") === "max-age=600", "anonymous caching altered");
});

await check("WCR-6 OPTIONS is 204 and unknown methods 405", async () => {
  const w = await freshWorker();
  assert((await w.fetch(req("https://bank.mifos.community/c", { method: "OPTIONS" }), env(LIST))).status === 204, "OPTIONS");
  assert((await w.fetch(req("https://bank.mifos.community/c", { method: "PROPFIND" }), env(LIST))).status === 405, "PROPFIND");
});

await check("WCR-7 redirects are re-checked per hop", async () => {
  const w = await freshWorker();
  reply = (u) => (u.endsWith("/a") ? new Response(null, { status: 302, headers: { Location: "https://evil.test/steal" } }) : new Response("{}"));
  assert((await w.fetch(req("https://bank.mifos.community/a"), env(LIST))).status === 502, "followed a redirect off the allowlist");
  assert(calls.length === 1, "fetched the off-list hop");
  calls.length = 0;
  reply = (u) => (u.endsWith("/a") ? new Response(null, { status: 303, headers: { Location: "https://api.example.com/b" } }) : new Response("{}"));
  const ok = await w.fetch(req("https://bank.mifos.community/a", { method: "POST", body: "x", headers: { Authorization: "Basic x" } }), env(LIST));
  assert(ok.status === 200 && calls.length === 2, `status ${ok.status}, ${calls.length} hop(s)`);
  assert(calls[1].method === "GET" && calls[1].body === undefined, "303 must continue as a bodyless GET");
  assert(!calls[1].headers.has("authorization"), "Authorization followed a cross-host redirect");
});

await check("WCR-8 PROXY_ALLOWED_HOSTS adds to the generated list", async () => {
  const w = await freshWorker();
  assert((await w.fetch(req("https://extra.test/x"), env(LIST, { PROXY_ALLOWED_HOSTS: "extra.test, other.test" }))).status === 200, "env host refused");
});

await check("WCR-9 no allowlist file = relay nothing (never an open proxy)", async () => {
  const res = await (await freshWorker()).fetch(req("https://bank.mifos.community/x"), env(null));
  assert(res.status === 403 && calls.length === 0, `status ${res.status}`);
});

await check("WCR-10 static assets keep COOP + COEP", async () => {
  const res = await (await freshWorker()).fetch(new Request(`${SITE}/index.html`), env(LIST));
  assert(res.headers.get("cross-origin-opener-policy") === "same-origin" && res.headers.get("cross-origin-embedder-policy") === "require-corp", "isolation headers missing");
});

await check("WCR-12 every relay answer is tagged X-Kpt-Relay — worker errors and upstream errors too", async () => {
  const w = await freshWorker();
  reply = () => new Response("upstream boom", { status: 500 });
  const cases = {
    "upstream 500": await w.fetch(req("https://api.example.com/ok"), env(LIST)),
    "403 host": await w.fetch(req("https://evil.test/x"), env(LIST)),
    "400 bad target": await w.fetch(new Request(`${SITE}/cors/?not a url`), env(LIST)),
    "405 method": await w.fetch(req("https://api.example.com/x", { method: "PROPFIND" }), env(LIST)),
    "204 OPTIONS": await w.fetch(req("https://api.example.com/x", { method: "OPTIONS" }), env(LIST)),
  };
  reply = () => { throw new TypeError("network down"); };
  cases["502 unreachable"] = await w.fetch(req("https://api.example.com/x"), env(LIST));
  for (const [name, res] of Object.entries(cases)) assert(tagged(res), `${name} (status ${res.status}) not tagged`);
  assert(cases["upstream 500"].status === 500 && cases["502 unreachable"].status === 502, "statuses passed through wrongly");
  const asset = await w.fetch(new Request(`${SITE}/index.html`), env(LIST));
  assert(!tagged(asset), "a static asset must not look like a relay answer");
});

await check("WCR-13 an allowed origin gets its preflight answered and can read the relayed reply + tag", async () => {
  const w = await freshWorker();
  const list = { ...LIST, allowedOrigins: ["https://org.github.io"] };
  const origin = { Origin: "https://org.github.io" };
  const pre = await w.fetch(req("https://bank.mifos.community/x", { method: "OPTIONS", headers: { ...origin, "Access-Control-Request-Headers": "authorization" } }), env(list));
  assert(pre.status === 204 && calls.length === 0, `preflight status ${pre.status}, upstream calls ${calls.length}`);
  assert(pre.headers.get("access-control-allow-origin") === "https://org.github.io", "preflight not allowed");
  assert(pre.headers.get("access-control-allow-headers") === "authorization", "requested headers not allowed");
  const res = await w.fetch(req("https://bank.mifos.community/x", { headers: origin }), env(list));
  assert(res.status === 200 && res.headers.get("access-control-allow-origin") === "https://org.github.io", "reply not readable cross-origin");
  assert(res.headers.get("access-control-expose-headers") === "X-Kpt-Relay" && tagged(res), "tag not exposed to the page");
  assert(res.headers.get("cross-origin-resource-policy") === "cross-origin", "CORP blocks the cross-origin page");
});

await check("WCR-14 any other origin gets no CORS — not a relay for arbitrary sites", async () => {
  const w = await freshWorker();
  const list = { ...LIST, allowedOrigins: ["https://org.github.io"] };
  const res = await w.fetch(req("https://bank.mifos.community/x", { headers: { Origin: "https://evil.test" } }), env(list));
  assert(res.headers.get("access-control-allow-origin") === null, "CORS granted to an unlisted origin");
  assert(res.headers.get("cross-origin-resource-policy") === "same-origin", "CORP relaxed for an unlisted origin");
});

process.exit(failed ? 1 : 0);
