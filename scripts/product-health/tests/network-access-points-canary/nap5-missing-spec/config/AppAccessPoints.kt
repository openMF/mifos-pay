// (no package decl: fixture files are read by the gate, not compiled — a package would
// make detekt's InvalidPackageDeclaration fire on their fixture path.)
object AppAccessPoints {
    val points: List<AccessPoint> = listOf(
        AccessPoint(
            id = "main",
            kind = AccessPointKind.REST,
            baseUrl = "https://api.example.com/",
            loggableHost = "api.example.com",
        ),
        AccessPoint(
            id = "shop",
            kind = AccessPointKind.REST,
            baseUrl = "https://shop.example.com/",
            loggableHost = "shop.example.com",
        ),
        AccessPoint(
            id = "sb",
            kind = AccessPointKind.SUPABASE,
            baseUrl = "https://sb.supabase.co",
            loggableHost = "sb.supabase.co",
        ),
        AccessPoint(
            id = "pay-gw",
            kind = AccessPointKind.REST,
            baseUrl = "https://pay.example.com/",
            loggableHost = "pay.example.com",
        ),
    )
}
