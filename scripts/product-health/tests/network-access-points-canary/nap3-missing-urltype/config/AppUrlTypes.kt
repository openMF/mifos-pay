// (no package decl: fixture files are read by the gate, not compiled — a package would
// make detekt's InvalidPackageDeclaration fire on their fixture path.)
object AppUrlTypes {
    val MAIN: UrlType = UrlType.MAIN
    val SB: UrlType = UrlType("SB")
    val PAY_GW: UrlType = UrlType("PAY-GW")
    val all: List<UrlType> = listOf(
        MAIN,
        SB,
        PAY_GW,
    )
}
