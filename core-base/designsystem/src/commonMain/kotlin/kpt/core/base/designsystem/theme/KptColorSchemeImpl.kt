/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.designsystem.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kpt.core.base.designsystem.core.ComponentDsl
import kpt.core.base.designsystem.core.KptColorScheme
import kpt.core.base.designsystem.core.KptElevation
import kpt.core.base.designsystem.core.KptShapes
import kpt.core.base.designsystem.core.KptSpacing
import kpt.core.base.designsystem.core.KptThemeProvider
import kpt.core.base.designsystem.core.KptTypography

/**
 * Default [KptColorScheme] — the Material 3 baseline palette.
 *
 * `@Immutable` so Compose can skip recomposition when the instance is unchanged. A fork overrides
 * only the roles it brands and inherits the rest, rather than restating all fifty.
 */
@Immutable
data class KptColorSchemeImpl(
    override val primary: Color = Color(0xFF6750A4),
    override val onPrimary: Color = Color(0xFFFFFFFF),
    override val primaryContainer: Color = Color(0xFFEADDFF),
    override val onPrimaryContainer: Color = Color(0xFF21005D),
    override val secondary: Color = Color(0xFF625B71),
    override val onSecondary: Color = Color(0xFFFFFFFF),
    override val secondaryContainer: Color = Color(0xFFE8DEF8),
    override val onSecondaryContainer: Color = Color(0xFF1D192B),
    override val tertiary: Color = Color(0xFF7D5260),
    override val onTertiary: Color = Color(0xFFFFFFFF),
    override val tertiaryContainer: Color = Color(0xFFFFD8E4),
    override val onTertiaryContainer: Color = Color(0xFF31111D),
    override val error: Color = Color(0xFFBA1A1A),
    override val onError: Color = Color(0xFFFFFFFF),
    override val errorContainer: Color = Color(0xFFFFDAD6),
    override val onErrorContainer: Color = Color(0xFF410002),
    override val background: Color = Color(0xFFFFFBFE),
    override val onBackground: Color = Color(0xFF1C1B1F),
    override val surface: Color = Color(0xFFFFFBFE),
    override val onSurface: Color = Color(0xFF1C1B1F),
    override val surfaceVariant: Color = Color(0xFFE7E0EC),
    override val onSurfaceVariant: Color = Color(0xFF49454F),
    override val outline: Color = Color(0xFF79747E),
    override val outlineVariant: Color = Color(0xFFCAC4D0),
    override val scrim: Color = Color(0xFF000000),
    override val inverseSurface: Color = Color(0xFF313033),
    override val inverseOnSurface: Color = Color(0xFFF4EFF4),
    override val inversePrimary: Color = Color(0xFFD0BCFF),
    override val surfaceDim: Color = Color(0xFFDAD6DC),
    override val surfaceBright: Color = Color(0xFFFFFBFE),
    override val surfaceContainerLowest: Color = Color(0xFFFFFFFF),
    override val surfaceContainerLow: Color = Color(0xFFF3EFF4),
    override val surfaceContainer: Color = Color(0xFFE7E0EC),
    override val surfaceContainerHigh: Color = Color(0xFFDAD6DC),
    override val surfaceContainerHighest: Color = Color(0xFFCFC8D0),
    override val surfaceTint: Color = Color(0xFF6750A4),
    override val primaryFixed: Color = Color(0xFFD0BCFF),
    override val primaryFixedDim: Color = Color(0xFFA694D1),
    override val onPrimaryFixed: Color = Color(0xFF21005D),
    override val onPrimaryFixedVariant: Color = Color(0xFF49307B),
    override val secondaryFixed: Color = Color(0xFFCCC2DC),
    override val secondaryFixedDim: Color = Color(0xFF9A8F9E),
    override val onSecondaryFixed: Color = Color(0xFF1D192B),
    override val onSecondaryFixedVariant: Color = Color(0xFF51445D),
    override val tertiaryFixed: Color = Color(0xFFFFD8E4),
    override val tertiaryFixedDim: Color = Color(0xFFFFB1C8),
    override val onTertiaryFixed: Color = Color(0xFF31111D),
    override val onTertiaryFixedVariant: Color = Color(0xFF4D2733),
) : KptColorScheme

/**
 * Default [KptTypography] — the Material 3 type scale at its standard sizes and weights.
 */
@Immutable
data class KptTypographyImpl(
    override val displayLarge: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    override val displayMedium: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    ),
    override val displaySmall: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    override val headlineLarge: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    override val headlineMedium: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    override val headlineSmall: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    override val titleLarge: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    override val titleMedium: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    override val titleSmall: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    override val bodyLarge: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    override val bodyMedium: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    override val bodySmall: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    override val labelLarge: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    override val labelMedium: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    override val labelSmall: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
) : KptTypography

/**
 * Default [KptShapes] — the Material 3 corner scale, 4dp through 28dp.
 */
@Immutable
data class KptShapesImpl(
    override val extraSmall: CornerBasedShape = RoundedCornerShape(4.dp),
    override val small: CornerBasedShape = RoundedCornerShape(8.dp),
    override val medium: CornerBasedShape = RoundedCornerShape(12.dp),
    override val large: CornerBasedShape = RoundedCornerShape(16.dp),
    override val extraLarge: CornerBasedShape = RoundedCornerShape(28.dp),
) : KptShapes

/**
 * Default [KptSpacing] — a 4dp-based scale. Components reference these rather than literal `.dp`,
 * so retuning density is one edit here instead of a sweep through every layout.
 */
@Immutable
data class KptSpacingImpl(
    override val xs: Dp = 4.dp,
    override val sm: Dp = 8.dp,
    override val md: Dp = 16.dp,
    override val lg: Dp = 24.dp,
    override val xl: Dp = 32.dp,
    override val xxl: Dp = 64.dp,
) : KptSpacing

/**
 * Default [KptElevation] — Material 3 levels 0–5.
 */
@Immutable
data class KptElevationImpl(
    override val level0: Dp = 0.dp,
    override val level1: Dp = 1.dp,
    override val level2: Dp = 3.dp,
    override val level3: Dp = 6.dp,
    override val level4: Dp = 8.dp,
    override val level5: Dp = 12.dp,
) : KptElevation

/**
 * Default [KptThemeProvider], composing the five default scales into one design language.
 */
@Immutable
data class KptThemeProviderImpl(
    override val colors: KptColorScheme = KptColorSchemeImpl(),
    override val typography: KptTypography = KptTypographyImpl(),
    override val shapes: KptShapes = KptShapesImpl(),
    override val spacing: KptSpacing = KptSpacingImpl(),
    override val elevation: KptElevation = KptElevationImpl(),
) : KptThemeProvider

/**
 * CompositionLocal carrying the active [KptColorScheme]. `static` because the theme changes rarely —
 * a read does not subscribe, so a palette swap recomposes the subtree rather than every reader.
 */
val LocalKptColors = staticCompositionLocalOf<KptColorScheme> { KptColorSchemeImpl() }

/**
 * CompositionLocal carrying the active [KptTypography].
 */
val LocalKptTypography = staticCompositionLocalOf<KptTypography> { KptTypographyImpl() }

/**
 * CompositionLocal carrying the active [KptShapes].
 */
val LocalKptShapes = staticCompositionLocalOf<KptShapes> { KptShapesImpl() }

/**
 * CompositionLocal carrying the active [KptSpacing].
 */
val LocalKptSpacing = staticCompositionLocalOf<KptSpacing> { KptSpacingImpl() }

/**
 * CompositionLocal carrying the active [KptElevation].
 */
val LocalKptElevation = staticCompositionLocalOf<KptElevation> { KptElevationImpl() }

/**
 * DSL builder for a complete [KptThemeProvider]. Entry point: [kptTheme].
 */
@ComponentDsl
class KptThemeBuilder {
    private var colors: KptColorScheme = KptColorSchemeImpl()
    private var typography: KptTypography = KptTypographyImpl()
    private var shapes: KptShapes = KptShapesImpl()
    private var spacing: KptSpacing = KptSpacingImpl()
    private var elevation: KptElevation = KptElevationImpl()

    /** Configures the colors scale in [block]. Replaces the whole scale — call it once. */
    fun colors(block: KptColorSchemeBuilder.() -> Unit) {
        colors = KptColorSchemeBuilder().apply(block).build()
    }

    /** Sets [KptElevation.typography]. */
    fun typography(block: KptTypographyBuilder.() -> Unit) {
        typography = KptTypographyBuilder().apply(block).build()
    }

    /** Sets [KptElevation.shapes]. */
    fun shapes(block: KptShapesBuilder.() -> Unit) {
        shapes = KptShapesBuilder().apply(block).build()
    }

    /** Sets [KptElevation.spacing]. */
    fun spacing(block: KptSpacingBuilder.() -> Unit) {
        spacing = KptSpacingBuilder().apply(block).build()
    }

    /** Sets [KptElevation.elevation]. */
    fun elevation(block: KptElevationBuilder.() -> Unit) {
        elevation = KptElevationBuilder().apply(block).build()
    }

    /**
     * Builds the configured [KptElevation]. Any role left unset keeps its default, so a fork declares only what it
     * brands.
     */
    fun build(): KptThemeProvider = KptThemeProviderImpl(
        colors = colors,
        typography = typography,
        shapes = shapes,
        spacing = spacing,
        elevation = elevation,
    )
}

/**
 * DSL builder for a [KptColorScheme]; unset roles keep their defaults.
 */
@ComponentDsl
class KptColorSchemeBuilder {
    /** Sets [KptColorScheme.primary]. */
    var primary: Color = Color(0xFF6750A4)

    /** Sets [KptColorScheme.onPrimary]. */
    var onPrimary: Color = Color(0xFFFFFFFF)

    /** Sets [KptColorScheme.primaryContainer]. */
    var primaryContainer: Color = Color(0xFFEADDFF)

    /** Sets [KptColorScheme.onPrimaryContainer]. */
    var onPrimaryContainer: Color = Color(0xFF21005D)

    /** Sets [KptColorScheme.secondary]. */
    var secondary: Color = Color(0xFF625B71)

    /** Sets [KptColorScheme.onSecondary]. */
    var onSecondary: Color = Color(0xFFFFFFFF)

    /** Sets [KptColorScheme.secondaryContainer]. */
    var secondaryContainer: Color = Color(0xFFE8DEF8)

    /** Sets [KptColorScheme.onSecondaryContainer]. */
    var onSecondaryContainer: Color = Color(0xFF1D192B)

    /** Sets [KptColorScheme.tertiary]. */
    var tertiary: Color = Color(0xFF7D5260)

    /** Sets [KptColorScheme.onTertiary]. */
    var onTertiary: Color = Color(0xFFFFFFFF)

    /** Sets [KptColorScheme.tertiaryContainer]. */
    var tertiaryContainer: Color = Color(0xFFFFD8E4)

    /** Sets [KptColorScheme.onTertiaryContainer]. */
    var onTertiaryContainer: Color = Color(0xFF31111D)

    /** Sets [KptColorScheme.error]. */
    var error: Color = Color(0xFFBA1A1A)

    /** Sets [KptColorScheme.onError]. */
    var onError: Color = Color(0xFFFFFFFF)

    /** Sets [KptColorScheme.errorContainer]. */
    var errorContainer: Color = Color(0xFFFFDAD6)

    /** Sets [KptColorScheme.onErrorContainer]. */
    var onErrorContainer: Color = Color(0xFF410002)

    /** Sets [KptColorScheme.background]. */
    var background: Color = Color(0xFFFFFBFE)

    /** Sets [KptColorScheme.onBackground]. */
    var onBackground: Color = Color(0xFF1C1B1F)

    /** Sets [KptColorScheme.surface]. */
    var surface: Color = Color(0xFFFFFBFE)

    /** Sets [KptColorScheme.onSurface]. */
    var onSurface: Color = Color(0xFF1C1B1F)

    /** Sets [KptColorScheme.surfaceVariant]. */
    var surfaceVariant: Color = Color(0xFFE7E0EC)

    /** Sets [KptColorScheme.onSurfaceVariant]. */
    var onSurfaceVariant: Color = Color(0xFF49454F)

    /** Sets [KptColorScheme.outline]. */
    var outline: Color = Color(0xFF79747E)

    /** Sets [KptColorScheme.outlineVariant]. */
    var outlineVariant: Color = Color(0xFFCAC4D0)

    /**
     * Builds the configured [KptElevation]. Any role left unset keeps its default, so a fork declares only what it
     * brands.
     */
    fun build(): KptColorScheme = KptColorSchemeImpl(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        outline = outline,
        outlineVariant = outlineVariant,
    )
}

/**
 * DSL builder for a [KptTypography]; unset styles keep their defaults.
 */
@ComponentDsl
class KptTypographyBuilder {
    /** Sets [KptTypography.displayLarge]. */
    var displayLarge: TextStyle = TextStyle(fontWeight = FontWeight.Normal, fontSize = 57.sp)

    /** Sets [KptTypography.displayMedium]. */
    var displayMedium: TextStyle = TextStyle(fontWeight = FontWeight.Normal, fontSize = 45.sp)

    /** Sets [KptTypography.displaySmall]. */
    var displaySmall: TextStyle = TextStyle(fontWeight = FontWeight.Normal, fontSize = 36.sp)

    /** Sets [KptTypography.headlineLarge]. */
    var headlineLarge: TextStyle = TextStyle(fontWeight = FontWeight.Normal, fontSize = 32.sp)

    /** Sets [KptTypography.headlineMedium]. */
    var headlineMedium: TextStyle = TextStyle(fontWeight = FontWeight.Normal, fontSize = 28.sp)

    /** Sets [KptTypography.headlineSmall]. */
    var headlineSmall: TextStyle = TextStyle(fontWeight = FontWeight.Normal, fontSize = 24.sp)

    /** Sets [KptTypography.titleLarge]. */
    var titleLarge: TextStyle = TextStyle(fontWeight = FontWeight.Normal, fontSize = 22.sp)

    /** Sets [KptTypography.titleMedium]. */
    var titleMedium: TextStyle = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp)

    /** Sets [KptTypography.titleSmall]. */
    var titleSmall: TextStyle = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp)

    /** Sets [KptTypography.bodyLarge]. */
    var bodyLarge: TextStyle = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp)

    /** Sets [KptTypography.bodyMedium]. */
    var bodyMedium: TextStyle = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp)

    /** Sets [KptTypography.bodySmall]. */
    var bodySmall: TextStyle = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp)

    /** Sets [KptTypography.labelLarge]. */
    var labelLarge: TextStyle = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp)

    /** Sets [KptTypography.labelMedium]. */
    var labelMedium: TextStyle = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp)

    /** Sets [KptTypography.labelSmall]. */
    var labelSmall: TextStyle = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp)

    /**
     * Builds the configured [KptElevation]. Any role left unset keeps its default, so a fork declares only what it
     * brands.
     */
    fun build(): KptTypography = KptTypographyImpl(
        displayLarge = displayLarge,
        displayMedium = displayMedium,
        displaySmall = displaySmall,
        headlineLarge = headlineLarge,
        headlineMedium = headlineMedium,
        headlineSmall = headlineSmall,
        titleLarge = titleLarge,
        titleMedium = titleMedium,
        titleSmall = titleSmall,
        bodyLarge = bodyLarge,
        bodyMedium = bodyMedium,
        bodySmall = bodySmall,
        labelLarge = labelLarge,
        labelMedium = labelMedium,
        labelSmall = labelSmall,
    )
}

/**
 * DSL builder for a [KptShapes]; unset corners keep their defaults.
 */
@ComponentDsl
class KptShapesBuilder {
    /** Sets [KptShapes.extraSmall]. */
    var extraSmall: CornerBasedShape = RoundedCornerShape(4.dp)

    /** Sets [KptShapes.small]. */
    var small: CornerBasedShape = RoundedCornerShape(8.dp)

    /** Sets [KptShapes.medium]. */
    var medium: CornerBasedShape = RoundedCornerShape(12.dp)

    /** Sets [KptShapes.large]. */
    var large: CornerBasedShape = RoundedCornerShape(16.dp)

    /** Sets [KptShapes.extraLarge]. */
    var extraLarge: CornerBasedShape = RoundedCornerShape(28.dp)

    /**
     * Builds the configured [KptElevation]. Any role left unset keeps its default, so a fork declares only what it
     * brands.
     */
    fun build(): KptShapes = KptShapesImpl(
        extraSmall = extraSmall,
        small = small,
        medium = medium,
        large = large,
        extraLarge = extraLarge,
    )
}

/**
 * DSL builder for a [KptSpacing]; unset steps keep their defaults.
 */
@ComponentDsl
class KptSpacingBuilder {
    /** Sets [KptSpacing.xs]. */
    var xs: Dp = 4.dp

    /** Sets [KptSpacing.sm]. */
    var sm: Dp = 8.dp

    /** Sets [KptSpacing.md]. */
    var md: Dp = 16.dp

    /** Sets [KptSpacing.lg]. */
    var lg: Dp = 24.dp

    /** Sets [KptSpacing.xl]. */
    var xl: Dp = 32.dp

    /** Sets [KptSpacing.xxl]. */
    var xxl: Dp = 64.dp

    /**
     * Builds the configured [KptElevation]. Any role left unset keeps its default, so a fork declares only what it
     * brands.
     */
    fun build(): KptSpacing = KptSpacingImpl(
        xs = xs,
        sm = sm,
        md = md,
        lg = lg,
        xl = xl,
        xxl = xxl,
    )
}

/**
 * DSL builder for a [KptElevation]; unset levels keep their defaults.
 */
@ComponentDsl
class KptElevationBuilder {
    /** Sets [KptElevation.level0]. */
    var level0: Dp = 0.dp

    /** Sets [KptElevation.level1]. */
    var level1: Dp = 1.dp

    /** Sets [KptElevation.level2]. */
    var level2: Dp = 3.dp

    /** Sets [KptElevation.level3]. */
    var level3: Dp = 6.dp

    /** Sets [KptElevation.level4]. */
    var level4: Dp = 8.dp

    /** Sets [KptElevation.level5]. */
    var level5: Dp = 12.dp

    /**
     * Builds the configured [KptElevation]. Any role left unset keeps its default, so a fork declares only what it
     * brands.
     */
    fun build(): KptElevation = KptElevationImpl(
        level0 = level0,
        level1 = level1,
        level2 = level2,
        level3 = level3,
        level4 = level4,
        level5 = level5,
    )
}

/**
 * Composition-local accessor for the active design language — `KptTheme.colors`, `.typography`,
 * `.shapes`, `.spacing`, `.elevation`.
 *
 * The read side of the theme; [kptTheme] is the write side.
 */
object KptTheme {
    /** The active colour scheme, from the nearest [KptTheme] in the composition. */
    val colorScheme: KptColorScheme
        @Composable get() = LocalKptColors.current

    /** The active type scale. */
    val typography: KptTypography
        @Composable get() = LocalKptTypography.current

    /** The active corner-shape scale. */
    val shapes: KptShapes
        @Composable get() = LocalKptShapes.current

    /** The active spacing scale — what every layout measures with. */
    val spacing: KptSpacing
        @Composable get() = LocalKptSpacing.current

    /** The active elevation scale, Material 3 levels 0–5. */
    val elevation: KptElevation
        @Composable get() = LocalKptElevation.current
}

/**
 * Builds a [KptThemeProvider] with the DSL, overriding only what a fork brands:
 *
 * ```kotlin
 * val theme = kptTheme { colors { primary = BrandPurple } }
 * ```
 */
fun kptTheme(block: KptThemeBuilder.() -> Unit): KptThemeProvider {
    return KptThemeBuilder().apply(block).build()
}
