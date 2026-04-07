package com.example.seguimiento1.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val sessionViewModel: SessionViewModel = viewModel(
        factory = SessionViewModel.factory(context)
    )
    val sessionState by sessionViewModel.sessionState.collectAsState()

    LaunchedEffect(sessionState) {
        when (sessionState) {
            is SessionUiState.Authenticated -> {
                navController.navigate(MainRoutes.HOME_GRAPH) {
                    popUpTo(MainRoutes.AUTH_GRAPH) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }

            SessionUiState.Unauthenticated -> {
                navController.navigate(MainRoutes.AUTH_GRAPH) {
                    popUpTo(MainRoutes.HOME_GRAPH) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }

            SessionUiState.Loading -> Unit
        }
    }

    NavHost(
        navController = navController,
        startDestination = MainRoutes.AUTH_GRAPH
    ) {
        authGraph(
            navController = navController,
            onLoginSuccess = sessionViewModel::onLoginSuccess
        )
        homeGraph(onLogout = sessionViewModel::logout)
    }

    if (sessionState is SessionUiState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

