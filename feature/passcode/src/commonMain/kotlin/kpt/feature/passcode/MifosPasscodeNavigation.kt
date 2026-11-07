/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.passcode

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.toRoute
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithSlideTransitions
import kpt.core.base.ui.nav.composableWithStayTransitions

/**
 * String form of [RootPasscodeRoute], kept as a `const val` so it can be used
 * as `startDestination = ROOT_MIFOS_PASSCODE_ROUTE` in `NavHost { ... }`.
 * The matching `@SerialName` on [RootPasscodeRoute] makes the typed
 * `composable<RootPasscodeRoute>` registration resolve to the same route
 * string.
 */
const val ROOT_MIFOS_PASSCODE_ROUTE = "root_mifos_passcode_route"

/** String form of [ReAuthPasscodeRoute]; see [ROOT_MIFOS_PASSCODE_ROUTE]. */
const val RE_AUTH_MIFOS_PASSCODE_ROUTE = "reauth_mifos_passcode_route"

/**
 * Type-safe route for the root unlock / first-time-setup passcode screen.
 * Pushed onto the stack by `RootNavGraph` immediately after a successful
 * login. Backed by [rootMifosPasscodeScreen].
 */
@Serializable
@SerialName(ROOT_MIFOS_PASSCODE_ROUTE)
data object RootPasscodeRoute

/**
 * Type-safe route for the background-resume re-authentication screen. Pushed
 * by `MifosPayApp` when the app was backgrounded for >15 s and is still
 * unlocked. Backed by [reAuthMifosPasscodeScreen].
 */
@Serializable
@SerialName(RE_AUTH_MIFOS_PASSCODE_ROUTE)
data object ReAuthPasscodeRoute

/**
 * Type-safe route for the in-app sensitive-operation passcode gate. Carries
 * two parameters that the screen and the calling site coordinate on:
 *
 * @property verificationKey Optional `SavedStateHandle` key — when non-null,
 *           the screen writes `true` (verified) or `false` (cancelled /
 *           rejected) under this key on the **previous** back-stack entry's
 *           saved-state-handle. Callers observe their own key to react to
 *           the round-trip result. Pass `null` if the caller doesn't need
 *           the channel (e.g., change-passcode flow, where the result
 *           naturally returns via [PasscodeResult.Changed]).
 * @property allowBiometricAuth `false` to suppress the biometric shortcut on
 *           this entry only (e.g., disable-biometrics verification, where
 *           biometric bypass would be a security hole). Default `true`.
 */
@Serializable
data class InternalPasscodeRoute(
    val verificationKey: String? = null,
    val allowBiometricAuth: Boolean = true,
)

/** Push the root passcode screen. See [RootPasscodeRoute]. */
fun NavController.navigateToRootMifosPasscodeScreen(navOptions: NavOptions? = null) =
    navigate(RootPasscodeRoute, navOptions)

/** Push the re-auth passcode screen. See [ReAuthPasscodeRoute]. */
fun NavController.navigateToReAuthMifosPasscodeScreen(navOptions: NavOptions? = null) =
    navigate(ReAuthPasscodeRoute, navOptions)

/**
 * Push the internal passcode gate. See [InternalPasscodeRoute] for the
 * round-trip protocol and the meaning of each parameter.
 */
fun NavController.navigateToInternalMifosPasscodeScreen(
    verificationKey: String? = null,
    allowBiometricAuth: Boolean = true,
    navOptions: NavOptions? = null,
) = navigate(InternalPasscodeRoute(verificationKey, allowBiometricAuth), navOptions)

/**
 * Registers the root passcode destination. Used by `RootNavGraph` for both
 * the post-login first-time setup and the post-login unlock — the underlying
 * [MifosPasscode] composable picks the right step based on whether a passcode
 * is already stored.
 *
 * Back navigation is disabled (`allowBackNavigation = false`) so the user
 * can't escape the lock; biometric auth is enabled.
 *
 * @param navigateToLogin Fired on [PasscodeResult.Forgotten] — typically
 *        clears session and pops back to login.
 * @param onAuthenticationSuccess Fired on [PasscodeResult.Verified] — pop
 *        and navigate to the authenticated graph.
 * @param onPasscodeCreation Fired on [PasscodeResult.Created] — typically
 *        navigate to biometric setup or main.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun NavGraphBuilder.rootMifosPasscodeScreen(
    navigateToLogin: () -> Unit,
    onAuthenticationSuccess: () -> Unit,
    onPasscodeCreation: () -> Unit = {},
    onAuthenticationFailed: () -> Unit = {},
    onPasscodeChanged: () -> Unit = {},
) {
    composableWithStayTransitions<RootPasscodeRoute> {
        MifosPasscode(
            onAuthenticationSuccess = onAuthenticationSuccess,
            navigateToLogin = navigateToLogin,
            onPasscodeCreation = onPasscodeCreation,
            onAuthenticationFailed = onAuthenticationFailed,
            onPasscodeChanged = onPasscodeChanged,
            allowBackNavigation = false,
            allowBiometricAuth = true,
        )
    }
}

/**
 * Registers the background-resume re-auth destination. Pushed on top of the
 * authenticated graph by the lifecycle observer in `MifosPayApp`; pops on
 * [PasscodeResult.Verified]. Back navigation disabled, biometric auth
 * enabled.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun NavGraphBuilder.reAuthMifosPasscodeScreen(
    navigateToLogin: () -> Unit,
    onAuthenticationSuccess: () -> Unit,
    onAuthenticationFailed: () -> Unit = {},
    onPasscodeChanged: () -> Unit = {},
) {
    composableWithSlideTransitions<ReAuthPasscodeRoute> {
        MifosPasscode(
            onAuthenticationSuccess = onAuthenticationSuccess,
            navigateToLogin = navigateToLogin,
            onAuthenticationFailed = onAuthenticationFailed,
            onPasscodeChanged = onPasscodeChanged,
            allowBackNavigation = false,
            allowBiometricAuth = true,
        )
    }
}

/**
 * Registers the in-app sensitive-operation passcode gate (change passcode,
 * disable biometrics, intra-bank transfer auth). Back navigation is enabled
 * so users can cancel; biometric auth is gated on [InternalPasscodeRoute.allowBiometricAuth].
 *
 * The verification-key plumbing — see [InternalPasscodeRoute] — is the
 * primary contract this builder publishes to its callers.
 *
 * @param navigateToLogin Fired on [PasscodeResult.Forgotten] (forgot button
 *        from inside an in-app gate).
 * @param onAuthenticationSuccess `(verificationKey) -> Unit` — typically
 *        writes `true` to the previous back-stack entry's saved-state-handle
 *        under `verificationKey` and pops.
 * @param onAuthenticationFailed `(verificationKey) -> Unit` — typically
 *        writes `false` and pops.
 * @param onBackPress Fired when the user backs out without entering a
 *        passcode; usually just pops.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun NavGraphBuilder.internalMifosPasscodeScreen(
    navigateToLogin: () -> Unit,
    onAuthenticationSuccess: (verificationKey: String?) -> Unit,
    onAuthenticationFailed: (verificationKey: String?) -> Unit = {},
    onPasscodeChanged: () -> Unit = {},
    onBackPress: () -> Unit = {},
) {
    composableWithSlideTransitions<InternalPasscodeRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<InternalPasscodeRoute>()
        MifosPasscode(
            onAuthenticationSuccess = { onAuthenticationSuccess(route.verificationKey) },
            navigateToLogin = navigateToLogin,
            onPasscodeCreation = {},
            onAuthenticationFailed = { onAuthenticationFailed(route.verificationKey) },
            onPasscodeChanged = onPasscodeChanged,
            onBackPress = onBackPress,
            allowBackNavigation = true,
            allowBiometricAuth = route.allowBiometricAuth,
        )
    }
}
