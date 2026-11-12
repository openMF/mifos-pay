// (no package decl — see note in the config/ fixtures.)
val GeneratedApiBindings: Module = module {
    restApi("shop") { it.createShopApi() }
    supabaseApi("sb") { ConfigApi(it) }
}
