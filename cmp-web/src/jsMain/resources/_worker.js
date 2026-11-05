// Cloudflare Pages worker for the web app (Pages "advanced mode": a _worker.js at the root of the
// deployed directory handles every request; other hosts serve it as an inert file).
//
// 1. /cors/?<https-url> — the app's own CORS relay. The web build routes calls to hosts declared with
//    `proxied_host` in app-profile/app.yaml here (`network.web_cors_proxy: /cors`). It forwards GET/HEAD
//    to an ALLOWLISTED host only, so it is never an open proxy, and strips cookies both ways.
//    Allowlist: the PROXY_ALLOWED_HOSTS environment variable (comma-separated) if set on the Pages
//    project, else DEFAULT_PROXY_HOSTS below — keep it in sync with app.yaml's proxied_host entries.
// 2. Everything else is the static site, served with COOP + COEP so the page is cross-origin isolated
//    and the app's SQLite database persists (OPFS). In advanced mode Pages does not apply _headers, so
//    the worker sets them itself.

const DEFAULT_PROXY_HOSTS = ["api.stlouisfed.org"];
const ISOLATION_HEADERS = {
  "Cross-Origin-Opener-Policy": "same-origin",
  "Cross-Origin-Embedder-Policy": "require-corp",
};

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (url.pathname === "/cors" || url.pathname === "/cors/") {
      return relay(request, env, url);
    }
    const asset = await env.ASSETS.fetch(request);
    const response = new Response(asset.body, asset);
    for (const [name, value] of Object.entries(ISOLATION_HEADERS)) response.headers.set(name, value);
    return response;
  },
};

async function relay(request, env, url) {
  if (request.method !== "GET" && request.method !== "HEAD") {
    return new Response("Method not allowed", { status: 405, headers: { Allow: "GET, HEAD" } });
  }
  let target;
  try {
    target = new URL(url.search.slice(1)); // everything after the first "?" is the target URL, verbatim
  } catch {
    return new Response("Expected /cors/?<https-url>", { status: 400 });
  }
  const allowed = env.PROXY_ALLOWED_HOSTS
    ? env.PROXY_ALLOWED_HOSTS.split(",").map((h) => h.trim()).filter(Boolean)
    : DEFAULT_PROXY_HOSTS;
  if (target.protocol !== "https:" || !allowed.includes(target.hostname)) {
    return new Response("Host not allowed", { status: 403 });
  }
  const upstream = await fetch(target, {
    method: request.method,
    headers: { Accept: request.headers.get("Accept") || "*/*" },
    redirect: "follow",
  });
  const response = new Response(upstream.body, upstream);
  response.headers.delete("Set-Cookie");
  response.headers.set("Cross-Origin-Resource-Policy", "same-origin");
  return response;
}
