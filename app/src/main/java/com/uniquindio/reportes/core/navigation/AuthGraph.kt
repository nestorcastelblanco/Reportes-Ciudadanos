@file:Suppress("unused")

package com.uniquindio.reportes.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.uniquindio.reportes.features.change_password.NewPasswordScreen
import com.uniquindio.reportes.features.login.LoginScreen
import com.uniquindio.reportes.features.recover_password.RecoverPasswordScreen
import com.uniquindio.reportes.features.register.RegisterScreen

@Suppress("unused")
fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    onLoginSuccess: (String) -> Unit
) {
    navigation<AuthGraph>(startDestination = LoginRoute) {
        composable<LoginRoute> {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(RegisterRoute) },
                onNavigateToRecoverPassword = { navController.navigate(RecoverPasswordRoute) },
                onLoginSuccess = onLoginSuccess
            )
        }
        composable<RegisterRoute> {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<RecoverPasswordRoute> {
            RecoverPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNewPassword = { navController.navigate(NewPasswordRoute) },
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                }
            )
        }
        composable<NewPasswordRoute> {
            NewPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                }
            )
        }
    }
}

