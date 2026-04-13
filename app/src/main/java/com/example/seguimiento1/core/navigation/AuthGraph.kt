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
    navigation<AuthGraph>(startDestination = LoginRoute) {
        composable<LoginRoute> {
            LoginScreen(navController, onLoginSuccess)
        }
        composable<RegisterRoute> { RegisterScreen(navController) }
        composable<RecoverPasswordRoute> { RecoverPasswordScreen(navController) }
        composable<NewPasswordRoute> { NewPasswordScreen(navController) }
    }
}

