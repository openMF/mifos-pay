// (no package decl — see note in the config/ fixtures.)
val GeneratedApiBindings: Module = module {
    supabaseApi("sb") { ConfigApi(it) }
}
