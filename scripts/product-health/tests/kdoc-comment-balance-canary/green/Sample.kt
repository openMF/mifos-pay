package sample

/**
 * Balanced prose: core/database/**/AppDatabase.kt reads as a path and closes what it opens.
 *
 * A deliberate inline example — { /* ... */ } — is balanced too.
 *
 * String literals are never scanned, so the glob in the property below is irrelevant to this check.
 */
class Sample {
    val pattern = "**/*.kts"
}

/*
 * A plain block comment whose closer is glued to the next KDoc's opener — Kotlin accepts this, and
 * the gate must not read the shared star as an unclosed opener.
 *//**
 * The KDoc that begins on the very same line the previous comment ended.
 *
 * Nothing in this prose may itself spell a comment delimiter: doing so would genuinely change the
 * depth, and the fixture would be asserting the gate is wrong about real breakage.
 */
class Other
