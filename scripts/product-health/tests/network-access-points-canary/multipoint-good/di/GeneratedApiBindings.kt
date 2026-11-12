// (no package decl — see note in the config/ fixtures.)
val GeneratedApiBindings: Module = module {
    restApi("shop") { it.createShopApi() }
    restApi("main", qualifier = named("main")) { it.createShopApi() }
    supabaseApi("sb") { ConfigApi(it) }
}
