@file:Suppress("unused")

package com.example.seguimiento1.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.seguimiento1.features.change_password.NewPasswordScreen
import com.example.seguimiento1.features.login.LoginScreen
import com.example.seguimiento1.features.recover_password.RecoverPasswordScreen
import com.example.seguimiento1.features.register.RegisterScreen

@Suppress("unused")
fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    onLoginSuccess: (String) -> Unit
) {
    navigation(
        route = MainRoutes.AUTH_GRAPH,
        startDestination = MainRoutes.LOGIN
    ) {
        composable(MainRoutes.LOGIN) {
            LoginScreen(navController, onLoginSuccess)
        }
        composable(MainRoutes.REGISTER) { RegisterScreen(navController) }
        composable(MainRoutes.RECOVER_PASSWORD) { RecoverPasswordScreen(navController) }
        composable(MainRoutes.NEW_PASSWORD) { NewPasswordScreen(navController) }
    }
}

