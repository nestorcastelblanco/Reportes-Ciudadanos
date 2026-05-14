package com.uniquindio.reportes.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.uniquindio.reportes.features.moderation.ModerationPanelScreen
import com.uniquindio.reportes.features.moderation.ModeratorDashboardScreen
import com.uniquindio.reportes.features.moderation.ModeratorUserDetailScreen
import com.uniquindio.reportes.features.moderation.ModeratorUsersScreen
import com.uniquindio.reportes.features.moderation.ReviewReportScreen

fun NavGraphBuilder.moderatorGraph(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    composable<ModeratorDashboardRoute> {
        ModeratorDashboardScreen(
            onOpenReports = { navController.navigate(ModerationPanelRoute) },
            onOpenUsers = { navController.navigate(ModeratorUsersRoute) },
            onLogout = onLogout
        )
    }
    composable<ModerationPanelRoute> {
        ModerationPanelScreen(
            onReviewReport = { reportId ->
                navController.navigate(ReviewReportRoute(reportId))
            },
            onBack = { navController.popBackStack() }
        )
    }
    composable<ReviewReportRoute> {
        ReviewReportScreen(onBack = { navController.popBackStack() })
    }
    composable<ModeratorUsersRoute> {
        ModeratorUsersScreen(
            onOpenUser = { email -> navController.navigate(ModeratorUserDetailRoute(email)) },
            onBack = { navController.popBackStack() }
        )
    }
    composable<ModeratorUserDetailRoute> { backStackEntry ->
        val args: ModeratorUserDetailRoute = backStackEntry.toRoute()
        ModeratorUserDetailScreen(
            email = args.email,
            onBack = { navController.popBackStack() }
        )
    }
}
