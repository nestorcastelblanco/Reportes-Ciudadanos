package com.example.seguimiento1.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.seguimiento1.features.moderation.ModerationPanelScreen
import com.example.seguimiento1.features.moderation.ReviewReportScreen

fun NavGraphBuilder.moderatorGraph(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    composable<ModerationPanelRoute> {
        ModerationPanelScreen(
            onReviewReport = { reportId ->
                navController.navigate(ReviewReportRoute(reportId))
            },
            onLogout = onLogout
        )
    }
    composable<ReviewReportRoute> {
        ReviewReportScreen(onBack = { navController.popBackStack() })
    }
}
