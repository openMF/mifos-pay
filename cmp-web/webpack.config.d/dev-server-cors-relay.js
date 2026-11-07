// Local dev: the webpack-dev-server answers the CORS relay path (`/cors` by default) by running the SAME
// _worker.js the deployed site runs, against the same generated relay-allowlist.json — so
// `web_cors.mode: relay` (the default) works under jsBrowserDevelopmentRun / wasmJsBrowserDevelopmentRun
// exactly as on Cloudflare Pages, and nothing is relayed that the deployed site would refuse.
// Production bundles ignore devServer, so this has no effect on a distribution build.
config.devServer = config.devServer || {};
(function () {
  const fs = require("fs");
  const path = require("path");
  const statics = [].concat(config.devServer.static || [])
    .map((s) => (typeof s === "string" ? s : s && s.directory))
    .filter(Boolean)
    .map((d) => path.resolve(__dirname, d));
  const find = (name) => statics.map((d) => path.join(d, name)).find((f) => fs.existsSync(f));

  let workerPromise;
  const loadWorker = () => {
    if (!workerPromise) {
      const file = find("_worker.js");
      workerPromise = file
        ? import("data:text/javascript;base64," + Buffer.from(fs.readFileSync(file)).toString("base64")).then((m) => m.default)
        : Promise.resolve(null);
    }
    return workerPromise;
  };
  const relayPath = () => {
    try {
      const relay = JSON.parse(fs.readFileSync(find("relay-allowlist.json"), "utf8")).relay;
      return typeof relay === "string" && relay.startsWith("/") ? relay : "/cors";
    } catch {
      return "/cors";
    }
  };
  const env = {
    PROXY_ALLOWED_HOSTS: process.env.PROXY_ALLOWED_HOSTS,
    ASSETS: {
      async fetch(request) {
        const file = find(new URL(request.url).pathname.replace(/^\/+/, ""));
        return file ? new Response(fs.readFileSync(file)) : new Response("not found", { status: 404 });
      },
    },
  };

  const previous = config.devServer.setupMiddlewares;
  config.devServer.setupMiddlewares = (middlewares, devServer) => {
    const list = previous ? previous(middlewares, devServer) : middlewares;
    list.unshift({
      name: "cors-relay",
      middleware: async (req, res, next) => {
        const pathname = req.url.split("?")[0];
        const relay = relayPath();
        if (pathname !== relay && pathname !== relay + "/") return next();
        try {
          const worker = await loadWorker();
          if (!worker) return next();
          const chunks = [];
          for await (const chunk of req) chunks.push(chunk);
          const hasBody = req.method !== "GET" && req.method !== "HEAD";
          const request = new Request(`http://${req.headers.host}${req.url}`, {
            method: req.method,
            headers: Object.entries(req.headers).filter(([, v]) => typeof v === "string"),
            body: hasBody ? Buffer.concat(chunks) : undefined,
          });
          const response = await worker.fetch(request, env);
          // fetch() already decoded the body, so the upstream encoding/length no longer describe it.
          const headers = {};
          response.headers.forEach((value, name) => {
            if (name !== "content-encoding" && name !== "content-length") headers[name] = value;
          });
          res.writeHead(response.status, headers);
          res.end(Buffer.from(await response.arrayBuffer()));
        } catch (e) {
          res.writeHead(502, { "Content-Type": "text/plain", "X-Kpt-Relay": "1" });
          res.end(`dev CORS relay failed: ${e.message}`);
        }
      },
    });
    return list;
  };
})();
