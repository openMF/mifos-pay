// (no package decl — see note in the config/ fixtures.)
val ForkNetworkModule = module {
    includes(GeneratedApiBindings)
    restApi("main") { it.createMainApi() }
}
