/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.designsystem.core

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import kotlin.reflect.KClass

/**
 * The base contract every `Kpt*` component satisfies: a test tag, a content description and a
caller-supplied [Modifier].
 *
 * Uniform on purpose — a UI test can locate any component the same way, and no component invents
 * its own accessibility story.
 */
interface KptComponent {
    /**
     * Stable identifier for UI tests. Null means the component is not addressable — acceptable only for purely
     * decorative content.
     */
    val testTag: String?

    /**
     * Screen-reader description, or null when the component is decorative and should be skipped by accessibility
     * services.
     */
    val contentDescription: String?

    /**
     * Caller-supplied modifier, applied to the component's outermost node so padding and sizing from the call site
     * win.
     */
    val modifier: Modifier
}

/**
 * Mixed into components that respond to a tap.
 *
 * [interactionSource] is exposed so a caller can hoist ripple/press state — a component that owns
 * it privately cannot participate in a parent's interaction handling.
 */
interface Clickable {
    /** Invoked on tap. Not called while [enabled] is false. */
    val onClick: () -> Unit

    /** Whether the tap is accepted. A disabled component still renders and is still read by accessibility services. */
    val enabled: Boolean

    /**
     * Hoisted press/ripple state, or null to let the component own it. Pass one when a parent must react to the same
     * interaction.
     */
    val interactionSource: MutableInteractionSource?
}

/**
 * Mixed into components whose colors, shape and elevation can be overridden at the call site.
 *
 * Every member is nullable: null means "inherit from the theme", which keeps a component themed by
 * default and overridable only where a screen genuinely differs.
 */
interface Styleable {
    /** Color overrides, or null to inherit the theme's. */
    val colors: ComponentColors?

    /** Shape override, or null to inherit the theme's shape for this component's size class. */
    val shape: Shape?

    /** Elevation override, or null to inherit the theme's. */
    val elevation: ComponentElevation?
}

/**
 * Mixed into components that accept a whole [ComponentTheme] rather than individual style slots.
 */
interface Themeable {
    /** A complete theme for this component, or null to resolve from the ambient one. */
    val theme: ComponentTheme?
}

/**
 * Marker for a component's color set. Each component defines its own slots; the marker exists so
 * [Styleable] can carry them without knowing the shape.
 */
interface ComponentColors

/**
 * Marker for a component's elevation set, per interaction state (resting, pressed, focused).
 */
interface ComponentElevation

/**
 * Marker for a complete component theme — colors, shape and elevation resolved together.
 */
interface ComponentTheme

/**
 * Resolves the [ComponentTheme] for a component, letting a fork swap the whole theming rule rather
 * than overriding components one at a time.
 */
interface ThemeStrategy {
    /** Resolves the theme for [component]. Called per render, so it must be cheap and free of side effects. */
    fun applyTheme(component: KptComponent): ComponentTheme
}

/**
 * Builds a component of type [T] from a [ComponentConfiguration] — the seam that lets components be
 * constructed from data (a registry, a server-driven layout) instead of only from Kotlin call sites.
 */
interface ComponentFactory<T : KptComponent> {
    /** Builds the component described by [configuration]. */
    fun create(configuration: ComponentConfiguration): T
}

/**
 * A component's declarative description, convertible to the component itself via [build].
 */
interface ComponentConfiguration {
    /** Materialises this description into a component. */
    fun build(): KptComponent
}

/**
 * Observable holder for one component's mutable value.
 *
 * `@Stable` so Compose can skip recomposition when the reference is unchanged; mutate through
 * [update] rather than replacing the holder, or that guarantee is lost.
 */
@Stable
interface ComponentState<T> {
    /** The current value. */
    val value: T

    /**
     * Replaces the value in place. Mutating through this preserves the `@Stable` contract; swapping the holder does
     * not.
     */
    fun update(newValue: T)
}

/**
 * A named visual variant of a component (filled, outlined, tonal, …).
 *
 * Sealed so the variant set is closed and exhaustively handled at each render site.
 */
sealed interface ComponentVariant {
    /** The variant's stable name — used in registries and test tags, so renaming one is a breaking change. */
    val name: String

    /**
     * Whether this variant is offered. Defaults to true; a fork overrides it to retire a variant without deleting the
     * branch.
     */
    val isEnabled: Boolean get() = true
}

/**
 * Renders a list of components as one composition — used where a screen's content is assembled from
 * data rather than written out.
 */
interface ComponentComposer {
    /** Renders [components] in order as one composition. */
    @Composable
    fun Compose(components: List<KptComponent>)
}

/**
 * Mixed into components with a tunable transition. See `theme/Motion.kt` for the shared durations;
 * overriding per component is what makes an app's motion feel inconsistent.
 */
interface Animatable {
    /**
     * Transition length in milliseconds. Prefer the shared values in `theme/Motion.kt`; a per-component number is what
     * makes motion feel uneven.
     */
    val animationDuration: Long

    /** Easing curve, or null for the theme's default. */
    val animationEasing: androidx.compose.animation.core.Easing?
}

/**
 * Supplies a component's semantics — description, role and any extra properties.
 *
 * Separate from [KptComponent] so a component can delegate accessibility to a wrapper rather than
 * re-declaring it.
 */
interface AccessibilityProvider {
    /** Extra semantics applied to the component's node, beyond description and role. */
    val semantics: androidx.compose.ui.semantics.SemanticsPropertyReceiver.() -> Unit

    /**
     * Screen-reader description, or null when the component is decorative and should be skipped by accessibility
     * services.
     */
    val contentDescription: String?

    /** The component's accessibility role, or null to let the platform infer it. */
    val role: androidx.compose.ui.semantics.Role?
}

/**
 * The whole design language in one object: colors, typography, shapes, spacing and elevation.
 *
 * A fork supplies its own and every component follows, which is the point of the indirection.
 */
interface KptThemeProvider {
    /** Color overrides, or null to inherit the theme's. */
    val colors: KptColorScheme

    /** The type scale. */
    val typography: KptTypography

    /** The corner-shape scale. */
    val shapes: KptShapes

    /** The spacing scale. */
    val spacing: KptSpacing

    /** Elevation override, or null to inherit the theme's. */
    val elevation: KptElevation
}

/**
 * The full Material 3 color role set.
 *
 * Roles, not literal colors — a component asks for `onSurfaceVariant`, never a hex value, so light
 * and dark themes and a fork's palette all work without touching the component.
 */
@Stable
interface KptColorScheme {
    /** The brand's main accent — filled buttons, active selection, the FAB. */
    val primary: Color

    /** Content drawn on [primary]. Guaranteed to meet contrast against it. */
    val onPrimary: Color

    /** A low-emphasis primary surface, for a tonal button or a selected chip. */
    val primaryContainer: Color

    /** Content drawn on [primaryContainer]. */
    val onPrimaryContainer: Color

    /** Primary as seen on an inverted surface — a snackbar's action, which sits on [inverseSurface]. */
    val inversePrimary: Color

    /** A supporting accent, for controls that must be visible without competing with [primary]. */
    val secondary: Color

    /** Content drawn on [secondary]. */
    val onSecondary: Color

    /** A low-emphasis secondary surface, typically a navigation item's selected indicator. */
    val secondaryContainer: Color

    /** Content drawn on [secondaryContainer]. */
    val onSecondaryContainer: Color

    /** A contrasting accent used to draw attention to a distinct third category — not a third brand colour. */
    val tertiary: Color

    /** Content drawn on [tertiary]. */
    val onTertiary: Color

    /** A low-emphasis tertiary surface. */
    val tertiaryContainer: Color

    /** Content drawn on [tertiaryContainer]. */
    val onTertiaryContainer: Color

    /** The window's base colour, behind all content. */
    val background: Color

    /** Content drawn directly on [background]. */
    val onBackground: Color

    /** The default colour for a component that sits above the background — card, sheet, menu. */
    val surface: Color

    /** Primary text and icons on [surface]. The highest-emphasis content colour. */
    val onSurface: Color

    /** A differentiated surface, for a field's fill or a divider band. */
    val surfaceVariant: Color

    /** Secondary text and icons — labels, supporting copy, inactive icons. */
    val onSurfaceVariant: Color

    /**
     * The tint blended into a surface as its elevation rises. Material 3 conveys elevation with colour, not only
     * shadow.
     */
    val surfaceTint: Color

    /** A surface deliberately opposite the theme, for a snackbar or a tooltip that must read as an overlay. */
    val inverseSurface: Color

    /** Content drawn on [inverseSurface]. */
    val inverseOnSurface: Color

    /** Error state — invalid field borders, destructive actions. */
    val error: Color

    /** Content drawn on [error]. */
    val onError: Color

    /** A low-emphasis error surface, for an inline error banner. */
    val errorContainer: Color

    /** Content drawn on [errorContainer]. */
    val onErrorContainer: Color

    /** Borders that must be clearly visible — an outlined text field, a focus ring. */
    val outline: Color

    /** Decorative separation — dividers and the quiet edge of a container. */
    val outlineVariant: Color

    /** The wash behind a modal, dimming what is not interactive. */
    val scrim: Color

    /** The brightest surface step, for an area that must sit visually above its siblings. */
    val surfaceBright: Color

    /** The dimmest surface step. */
    val surfaceDim: Color

    /** The default container step. The five container roles form a ladder, so nesting stays legible without shadows. */
    val surfaceContainer: Color

    /** One step above [surfaceContainer] — a nested card. */
    val surfaceContainerHigh: Color

    /** The top container step, for the innermost nesting level. */
    val surfaceContainerHighest: Color

    /** One step below [surfaceContainer]. */
    val surfaceContainerLow: Color

    /** The bottom container step, nearest the background. */
    val surfaceContainerLowest: Color

    /**
     * A primary tone that does NOT change between light and dark. For content that must look identical across themes —
     * a shared hero, a printable surface.
     */
    val primaryFixed: Color

    /** The dimmer variant of [primaryFixed]. */
    val primaryFixedDim: Color

    /** Content drawn on [primaryFixed]. */
    val onPrimaryFixed: Color

    /** Lower-emphasis content on [primaryFixed]. */
    val onPrimaryFixedVariant: Color

    /** The theme-invariant secondary tone — see [primaryFixed]. */
    val secondaryFixed: Color

    /** The dimmer variant of [secondaryFixed]. */
    val secondaryFixedDim: Color

    /** Content drawn on [secondaryFixed]. */
    val onSecondaryFixed: Color

    /** Lower-emphasis content on [secondaryFixed]. */
    val onSecondaryFixedVariant: Color

    /** The theme-invariant tertiary tone — see [primaryFixed]. */
    val tertiaryFixed: Color

    /** The dimmer variant of [tertiaryFixed]. */
    val tertiaryFixedDim: Color

    /** Content drawn on [tertiaryFixed]. */
    val onTertiaryFixed: Color

    /** Lower-emphasis content on [tertiaryFixed]. */
    val onTertiaryFixedVariant: Color
}

/**
 * The Material 3 type scale — display through label, each in three sizes.
 */
@Stable
interface KptTypography {
    /** The largest type — a single short string on a splash or hero. Never body copy. */
    val displayLarge: androidx.compose.ui.text.TextStyle

    /** Display at medium size. */
    val displayMedium: androidx.compose.ui.text.TextStyle

    /** Display at small size. */
    val displaySmall: androidx.compose.ui.text.TextStyle

    /** A screen's title. */
    val headlineLarge: androidx.compose.ui.text.TextStyle

    /** A major section heading. */
    val headlineMedium: androidx.compose.ui.text.TextStyle

    /** A minor section heading. */
    val headlineSmall: androidx.compose.ui.text.TextStyle

    /** A card or dialog title. */
    val titleLarge: androidx.compose.ui.text.TextStyle

    /** A list item's primary line. */
    val titleMedium: androidx.compose.ui.text.TextStyle

    /** A dense list item's primary line. */
    val titleSmall: androidx.compose.ui.text.TextStyle

    /** Default reading copy — the longest text on a screen. */
    val bodyLarge: androidx.compose.ui.text.TextStyle

    /** Secondary copy and supporting text. */
    val bodyMedium: androidx.compose.ui.text.TextStyle

    /** Captions, timestamps and footnotes. */
    val bodySmall: androidx.compose.ui.text.TextStyle

    /** A button's label. */
    val labelLarge: androidx.compose.ui.text.TextStyle

    /** A chip or tab label. */
    val labelMedium: androidx.compose.ui.text.TextStyle

    /** The smallest label — an overline or a badge. */
    val labelSmall: androidx.compose.ui.text.TextStyle
}

/**
 * The corner-shape scale, from `extraSmall` to `extraLarge`, applied by component size rather than
 * chosen per call site.
 */
@Stable
interface KptShapes {
    /** Tightest corner — a badge or a small chip. */
    val extraSmall: CornerBasedShape

    /** A text field or a compact button. */
    val small: CornerBasedShape

    /** The default: cards and most containers. */
    val medium: CornerBasedShape

    /** A bottom sheet or a large dialog. */
    val large: CornerBasedShape

    /** A full-bleed or hero surface. */
    val extraLarge: CornerBasedShape
}

/**
 * The spacing scale every layout measures with.
 *
 * Components reference these rather than literal `.dp` values so density stays uniform and a fork
 * can retune the whole app's rhythm in one place.
 */
@Stable
interface KptSpacing {
    /** Tightest step — icon-to-label gaps and chip padding. */
    val xs: Dp

    /** Padding inside a compact control. */
    val sm: Dp

    /** The default step: padding inside a card, and the gap between sibling controls. */
    val md: Dp

    /** Gap between sections of a screen. */
    val lg: Dp

    /** Screen-edge margin on a large window. */
    val xl: Dp

    /** Reserved for full-bleed layouts; rarely the right answer inside a card. */
    val xxl: Dp
}

/**
 * The elevation scale, in Material 3 levels 0–5.
 */
@Stable
interface KptElevation {
    /** Flat on the surface — no tint, no shadow. */
    val level0: Dp

    /** A resting card. */
    val level1: Dp

    /** A resting menu or a raised button. */
    val level2: Dp

    /** A dialog, or a card while pressed. */
    val level3: Dp

    /** A navigation drawer. */
    val level4: Dp

    /** The highest step — reserved for a temporary overlay above everything else. */
    val level5: Dp
}

/**
 * Renders component type [T]. Registered in a [ComponentRegistry] so a data-driven layout can resolve
 * a renderer by type at runtime.
 */
interface ComponentRenderer<T : KptComponent> {
    /** Renders [component]. */
    @Composable
    fun Render(component: T)
}

/**
 * Maps component types to their renderers and factories — the lookup a [ComponentComposer] uses.
 */
interface ComponentRegistry {
    /** Registers [renderer] for [type], replacing any previous registration. */
    fun <T : KptComponent> register(type: KClass<T>, renderer: ComponentRenderer<T>)

    /**
     * The renderer for [type], or null when none is registered — a data-driven layout must handle null rather than
     * assume coverage.
     */
    fun <T : KptComponent> getRenderer(type: KClass<T>): ComponentRenderer<T>?
}

/**
 * DSL marker for the component-configuration builders.
 *
 * Stops an inner builder from implicitly seeing an outer scope's receivers, which is how nested
 * DSL blocks silently configure the wrong component.
 */
@DslMarker
annotation class ComponentDsl

/**
 * Receiver for the component-configuration DSL, scoped by [ComponentDsl].
 */
@ComponentDsl
interface ComponentConfigurationScope {
    /**
     * Stable identifier for UI tests. Null means the component is not addressable — acceptable only for purely
     * decorative content.
     */
    var testTag: String?

    /**
     * Screen-reader description, or null when the component is decorative and should be skipped by accessibility
     * services.
     */
    var contentDescription: String?

    /** Whether the tap is accepted. A disabled component still renders and is still read by accessibility services. */
    var enabled: Boolean

    /**
     * Caller-supplied modifier, applied to the component's outermost node so padding and sizing from the call site
     * win.
     */
    var modifier: Modifier
}
