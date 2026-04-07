package com.example.seguimiento1.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.seguimiento1.features.home.HomeScreen

fun NavGraphBuilder.homeGraph(onLogout: () -> Unit) {
    navigation(
        route = MainRoutes.HOME_GRAPH,
        startDestination = MainRoutes.HOME
    ) {
        composable(MainRoutes.HOME) {
            HomeScreen(null, onLogout)
        }
    }
}

