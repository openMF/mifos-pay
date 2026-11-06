// Cloudflare Pages worker for the web app (Pages "advanced mode": a _worker.js at the root of the
// deployed directory handles every request; other hosts serve it as an inert file).
//
// /cors/?<https-url> is the app's own relay. Fineract rejects any browser Origin on real requests, so
// the web build (org.mifos.corebase.network.WebRelayPlugin) sends calls to the Mifos servers here and
// the worker forwards them server-side, without the browser's Origin. It forwards every method and the
// body, but only over https and only to RELAYED_DOMAINS (keep in step with WebRelay.kt), so it is never
// an open proxy. Cookies are dropped both ways; an authenticated response is never cached.
// Everything else is the static site.

const RELAYED_DOMAINS = ["mifos.community"];
const DROPPED_REQUEST_HEADERS = ["origin", "referer", "cookie", "host", "connection", "content-length", "x-real-ip"];

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (url.pathname === "/cors" || url.pathname === "/cors/") return relay(request, url);
    return env.ASSETS.fetch(request);
  },
};

export function isRelayedHost(host) {
  return RELAYED_DOMAINS.some((d) => host === d || host.endsWith("." + d));
}

export async function relay(request, url, upstreamFetch = fetch) {
  if (request.method === "OPTIONS") return new Response(null, { status: 204 });
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
  if (request.headers.has("Authorization")) response.headers.set("Cache-Control", "no-store");
  return response;
}
