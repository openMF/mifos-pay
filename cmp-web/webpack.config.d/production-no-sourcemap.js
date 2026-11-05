// No source maps in the production bundle.
// The web app bundles the whole app shell, and generating its source maps (cmp-web.js.map alone
// is ~17 MB) pushed webpack past the memory of a GitHub-hosted runner, which then killed the
// "PR Check · Web" job mid-build. Development builds keep their source maps.
if (config.mode === 'production') {
    config.devtool = false;
}
