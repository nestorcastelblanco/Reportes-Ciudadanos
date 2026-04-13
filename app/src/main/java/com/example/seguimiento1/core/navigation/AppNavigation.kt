package com.example.seguimiento1.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.seguimiento1.R
import com.example.seguimiento1.domain.model.UserRole

data class BottomNavItem(
    val label: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: Any
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val sessionViewModel: SessionViewModel = hiltViewModel()
    val sessionState by sessionViewModel.sessionState.collectAsState()

    val bottomNavItems = listOf(
        BottomNavItem(R.string.nav_home, Icons.Default.Home, HomeRoute),
        BottomNavItem(R.string.nav_map, Icons.Default.Place, MapRoute),
        BottomNavItem(R.string.nav_report, Icons.Default.AddCircle, CreateReportRoute),
        BottomNavItem(R.string.nav_data, Icons.Default.Info, StatisticsRoute),
        BottomNavItem(R.string.nav_profile, Icons.Default.AccountCircle, ProfileRoute)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = navBackStackEntry?.destination

    val showBottomBar = currentDest != null && (
            currentDest.hasRoute<HomeRoute>() ||
            currentDest.hasRoute<MapRoute>() ||
            currentDest.hasRoute<CreateReportRoute>() ||
            currentDest.hasRoute<StatisticsRoute>() ||
            currentDest.hasRoute<ProfileRoute>()
    )

    val isAuthenticated = sessionState is SessionUiState.Authenticated

    LaunchedEffect(sessionState) {
        when (val state = sessionState) {
            is SessionUiState.Authenticated -> {
                if (state.role == UserRole.MODERATOR) {
                    navController.navigate(ModerationPanelRoute) {
                        popUpTo(AuthGraph) { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    navController.navigate(HomeGraph) {
                        popUpTo(AuthGraph) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            SessionUiState.Unauthenticated -> {
                navController.navigate(AuthGraph) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
            SessionUiState.Loading -> Unit
        }
    }

    Scaffold(
        bottomBar = {
            if (isAuthenticated && showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDest?.hasRoute(item.route::class) == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(stringResource(item.label)) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AuthGraph,
            modifier = Modifier.padding(padding)
        ) {
            authGraph(
                navController = navController,
                onLoginSuccess = sessionViewModel::onLoginSuccess
            )
            homeGraph(navController = navController, onLogout = sessionViewModel::logout)
            moderatorGraph(navController = navController, onLogout = sessionViewModel::logout)
        }
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

