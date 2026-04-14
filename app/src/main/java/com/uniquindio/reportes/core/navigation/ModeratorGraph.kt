package com.uniquindio.reportes.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.uniquindio.reportes.features.moderation.ModerationPanelScreen
import com.uniquindio.reportes.features.moderation.ReviewReportScreen

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
