// Cloudflare Pages worker for the web app (Pages "advanced mode": a _worker.js at the root of the
// deployed directory handles every request; other hosts serve it as an inert file).
//
// /cors/?<https-url> is the app's own relay. Fineract rejects any browser Origin on real requests, so
// the web build (org.mifos.corebase.network.WebRelayPlugin) sends calls to the Mifos servers here and
// the worker forwards them server-side, without the browser's Origin. It forwards every method and the
// body, but only over https and only to RELAYED_DOMAINS (keep in step with WebRelay.kt), so it is never
// an open proxy. Cookies are dropped both ways; an authenticated response is never cached.
// Everything else is the static site.
//
// Hosts that cannot run this worker (GitHub Pages) point their relay here instead. Only the origins in
// ALLOWED_ORIGINS get CORS headers — any other page still cannot read a relayed response.

const RELAYED_DOMAINS = ["mifos.community"];
const ALLOWED_ORIGINS = ["https://openmf.github.io"];
const DROPPED_REQUEST_HEADERS = ["origin", "referer", "cookie", "host", "connection", "content-length", "x-real-ip"];
// The static site is served cross-origin isolated, so the app's SQLite worker can run its persistent
// (OPFS) database. In advanced mode Pages does not apply _headers, so the worker sets them itself.
const ISOLATION_HEADERS = {
  "Cross-Origin-Opener-Policy": "same-origin",
  "Cross-Origin-Embedder-Policy": "require-corp",
};

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (url.pathname === "/cors" || url.pathname === "/cors/") return relay(request, url);
    const asset = await env.ASSETS.fetch(request);
    const response = new Response(asset.body, asset);
    for (const [name, value] of Object.entries(ISOLATION_HEADERS)) response.headers.set(name, value);
    return response;
  },
};

export function isRelayedHost(host) {
  return RELAYED_DOMAINS.some((d) => host === d || host.endsWith("." + d));
}

// CORS headers for a cross-origin page on the allowlist; none for anyone else (same-origin needs none).
export function corsHeaders(request) {
  const origin = request.headers.get("Origin");
  if (!origin || !ALLOWED_ORIGINS.includes(origin)) return {};
  return {
    "Access-Control-Allow-Origin": origin,
    "Access-Control-Allow-Methods": "GET, POST, PUT, PATCH, DELETE, OPTIONS",
    "Access-Control-Allow-Headers": request.headers.get("Access-Control-Request-Headers") ?? "*",
    "Access-Control-Max-Age": "86400",
    "Vary": "Origin",
  };
}

export async function relay(request, url, upstreamFetch = fetch) {
  const cors = corsHeaders(request);
  if (request.method === "OPTIONS") return new Response(null, { status: 204, headers: cors });
  let target;
  try {
    target = new URL(url.search.slice(1)); // everything after the first "?" is the target URL, verbatim
  } catch {
    return new Response("Expected /cors/?<https-url>", { status: 400 });
  }
  if (target.protocol !== "https:" || !isRelayedHost(target.hostname)) {
    return new Response("Host not allowed", { status: 403 });
  }
  const headers = new Headers();
  for (const [name, value] of request.headers) {
    const n = name.toLowerCase();
    if (DROPPED_REQUEST_HEADERS.includes(n) || n.startsWith("cf-") || n.startsWith("x-forwarded-")) continue;
    headers.set(name, value);
  }
  const hasBody = request.method !== "GET" && request.method !== "HEAD";
  const upstream = await upstreamFetch(target.toString(), {
    method: request.method,
    headers,
    body: hasBody ? await request.arrayBuffer() : undefined,
    redirect: "manual", // never follow a redirect off the allowed domains
  });
  const response = new Response(upstream.body, upstream);
  response.headers.delete("Set-Cookie");
  for (const [name, value] of Object.entries(cors)) response.headers.set(name, value);
  response.headers.set("Cross-Origin-Resource-Policy", Object.keys(cors).length ? "cross-origin" : "same-origin");
  if (request.headers.has("Authorization")) response.headers.set("Cache-Control", "no-store");
  return response;
}
