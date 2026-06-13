package org.noztek.esktransport.app.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.navArgument
import org.koin.compose.koinInject
import org.noztek.esktransport.core.session.domain.SessionUser
import org.noztek.esktransport.core.session.domain.usecase.ObserveCurrentSessionUseCase
import org.noztek.esktransport.feature.common.forgot_password.presentation.ForgotPasswordScreen
import org.noztek.esktransport.feature.common.login.presentation.LoginScreen
import org.noztek.esktransport.feature.common.otp.data.local.OtpStateStore
import org.noztek.esktransport.feature.common.otp.presentation.OtpScreen
import org.noztek.esktransport.feature.common.register.presentation.RegisterScreen
import org.noztek.esktransport.feature.common.reset_password.presentation.ResetPasswordScreen

private object AuthNavState {
    var registerRole: String = "passenger"
    var otpPhone: String = ""
    var otpPurpose: String = "register"
    var resetPhone: String = ""
    var resetToken: String = ""
}

fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    onAuthenticated: (String) -> Unit,
) {
    navigation(startDestination = AuthRoute.LOGIN, route = RootRoute.AUTH) {
        composable(AuthRoute.LOGIN) {
            val otpPendingStore: OtpStateStore = koinInject()
            val observeCurrentSessionUseCase: ObserveCurrentSessionUseCase = koinInject()
            val pendingPhone by otpPendingStore.pendingPhone.collectAsState(initial = null)
            val pendingPurpose by otpPendingStore.pendingPurpose.collectAsState(initial = null)
            val session by observeCurrentSessionUseCase().collectAsState(
                initial = SessionUser(
                    userId = null,
                    name = null,
                    phone = null,
                    roles = emptySet(),
                    primaryRole = null,
                ),
            )
            var waitingForSessionRoute by remember { mutableStateOf(false) }

            LaunchedEffect(waitingForSessionRoute, session) {
                if (!waitingForSessionRoute) return@LaunchedEffect
                val route = session.authenticatedRootRoute()
                if (session.userId != null && route != RootRoute.AUTH) {
                    waitingForSessionRoute = false
                    onAuthenticated(route)
                }
            }

            LaunchedEffect(pendingPhone, pendingPurpose) {
                val phone = pendingPhone ?: return@LaunchedEffect
                AuthNavState.otpPhone = phone
                AuthNavState.otpPurpose = pendingPurpose ?: "register"
                if (navController.currentDestination?.route == AuthRoute.LOGIN) {
                    navController.navigate(AuthRoute.OTP)
                }
            }

            LoginScreen(
                onBackToWelcome = { navController.popBackStack() },
                onRegisterClick = {
                    AuthNavState.registerRole = "passenger"
                    navController.navigate(AuthRoute.REGISTER)
                },
                onLoginSuccess = { success -> if (success) waitingForSessionRoute = true },
                onForgotPassword = { navController.navigate(AuthRoute.FORGOT_PASSWORD) },
            )
        }

        composable(AuthRoute.REGISTER) {
            RegisterScreen(
                selectedRole = AuthNavState.registerRole,
                onBackToWelcome = { navController.popBackStack() },
                onLoginClick = { navController.navigate(AuthRoute.LOGIN) },
                onRegisterSuccess = { phone ->
                    AuthNavState.otpPhone = phone
                    AuthNavState.otpPurpose = "register"
                    navController.navigate(AuthRoute.OTP)
                },
            )
        }

        composable(
            route = "${AuthRoute.REGISTER}/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType }),
        ) { backStackEntry ->
            AuthNavState.registerRole = backStackEntry.arguments
                ?.let { NavType.StringType[it, "role"] }
                ?: "passenger"
            RegisterScreen(
                selectedRole = AuthNavState.registerRole,
                onBackToWelcome = { navController.popBackStack() },
                onLoginClick = { navController.navigate(AuthRoute.LOGIN) },
                onRegisterSuccess = { phone ->
                    AuthNavState.otpPhone = phone
                    AuthNavState.otpPurpose = "register"
                    navController.navigate(AuthRoute.OTP)
                },
            )
        }

        composable(AuthRoute.OTP) {
            OtpScreen(
                phone = AuthNavState.otpPhone,
                purpose = AuthNavState.otpPurpose,
                onOtpVerified = { resetToken ->
                    if (AuthNavState.otpPurpose == "reset_password" && !resetToken.isNullOrBlank()) {
                        AuthNavState.resetPhone = AuthNavState.otpPhone
                        AuthNavState.resetToken = resetToken
                        navController.navigate(AuthRoute.RESET_PASSWORD)
                    } else {
                        navController.navigate(AuthRoute.LOGIN)
                    }
                },
                onBackToLogin = { navController.navigate(AuthRoute.LOGIN) },
            )
        }

        composable(AuthRoute.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBackToLogin = { navController.popBackStack() },
                onOtpSent = { phone ->
                    AuthNavState.otpPhone = phone
                    AuthNavState.otpPurpose = "reset_password"
                    navController.navigate(AuthRoute.OTP)
                },
            )
        }

        composable(AuthRoute.RESET_PASSWORD) {
            ResetPasswordScreen(
                phone = AuthNavState.resetPhone,
                resetToken = AuthNavState.resetToken,
                onResetSuccess = {
                    navController.navigate(AuthRoute.LOGIN) {
                        popUpTo(AuthRoute.LOGIN) { inclusive = true }
                    }
                },
            )
        }
    }
}
