/** Probe fixture for scripts/docs/scanners/kotlin.awk. Each case is a bug the scanner once had. */
package probe

/** A string literal must not be read as a doc or a declaration. */
class StringTrap {
    /** Documented. */
    val pattern = "see /** not a doc */ and fun notADeclaration(): Int"
}

/** A lambda default in a signature must not be mistaken for the function body. */
@Composable
fun Widget(modifier: Modifier = Modifier, content: () -> Unit = {}) {
    val localNotApi = remember { 1 }
}

/** A multi-line annotation argument list must not break the doc→declaration link. */
@CacheKey(
    fn = "of",
    params = ["a:Int"],
)
fun annotated(): Int = 1

/**
 * Constructor properties are documented by a tag on the class.
 *
 * @property viaProperty credited by @property.
 * @param viaParam credited by @param, which detekt also accepts.
 */
data class Tagged(val viaProperty: Int, val viaParam: Int)

internal sealed class InternalHierarchy {
    data object NotPublicApi : InternalHierarchy()
}

/** An override inherits its contract; no doc is required. */
class Impl : Base {
    override fun inherited() = Unit
}

/** Abstract interface members detekt cannot see — the scanner must report these. */
interface Contract {
    val undocumentedMember: String
    fun undocumentedMethod(): Int
}

/** An annotated constructor property is still a property — `\b` in awk is a backspace, not a boundary. */
@Entity
data class Row(
    /** Documented. */
    @PrimaryKey val annotatedDocumented: Long,
    @ColumnInfo val annotatedUndocumented: String,
)

/** A top-level property AFTER a multi-line constructor must keep its own doc. */
@JvmField
val afterCtor: Int = 1

/** An `override val` in a constructor inherits the interface's doc. */
data class SchemeImpl(
    override val inheritedToken: Int = 1,
) : Contract2

/** An extension property is named by its member, not its receiver; its getter's locals are not API. */
val Contract.derived: Int
    get() {
        val localInsideGetter = 1
        return localInsideGetter
    }

/** A constructor property carries its own visibility modifier. */
class Proc(
    private val privateDep: Int,
    internal val internalDep: Int,
    val publicDep: Int,
)

/** A local inside a LAMBDA in a property initialiser is not API. */
class Holder {
    /** Derived. */
    val derived = listOf(1).map {
        val insideLambda = it + 1
        insideLambda
    }

    init {
        val insideInit = 1
    }
}

/** A constructor property's doc may sit above its own annotation. */
@Entity
data class Col(
    /** Documented above the annotation. */
    @PrimaryKey
    val docAboveAnno: String,
    @PrimaryKey
    val noDocAtAll: String,
)

/** A single-line enum body — its entries never get a line of their own. */
enum class Size { Xs, Sm }

/** Entries are Capitalised, not SCREAMING — an all-caps pattern misses them. */
enum class Way {
    Left,
    Right,
}

/** A companion object has no name after the keyword. */
class WithCompanion {
    companion object {
        /** Documented. */
        const val DOCUMENTED = 1
    }
}
