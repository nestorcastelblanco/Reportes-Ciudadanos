package com.uniquindio.reportes.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.uniquindio.reportes.features.change_password.ChangePasswordScreen
import com.uniquindio.reportes.features.home.HomeScreen
import com.uniquindio.reportes.features.map.MapScreen
import com.uniquindio.reportes.features.my_reports.MyReportsScreen
import com.uniquindio.reportes.features.notifications.NotificationsScreen
import com.uniquindio.reportes.features.profile.ProfileScreen
import com.uniquindio.reportes.features.reports.create.CreateReportScreen
import com.uniquindio.reportes.features.reports.detail.ReportDetailScreen
import com.uniquindio.reportes.features.reports.edit.EditReportScreen
import com.uniquindio.reportes.features.reputation.ReputationScreen
import com.uniquindio.reportes.features.statistics.StatisticsScreen

fun NavGraphBuilder.homeGraph(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    navigation<HomeGraph>(startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                onOpenCreateReport = { navController.navigate(CreateReportRoute) },
                onOpenNotifications = { navController.navigate(NotificationsRoute) },
                onOpenReportDetail = { reportId ->
                    navController.navigate(ReportDetailRoute(reportId))
                },
                onOpenProfile = { navController.navigate(ProfileRoute) }
            )
        }
        composable<CreateReportRoute> {
            CreateReportScreen(onBack = { navController.popBackStack() })
        }
        composable<MapRoute> {
            MapScreen(
                onOpenReportDetail = { reportId ->
                    navController.navigate(ReportDetailRoute(reportId))
                }
            )
        }
        composable<ReportDetailRoute> {
            ReportDetailScreen(
                onBack = { navController.popBackStack() },
                onEditReport = { reportId ->
                    navController.navigate(EditReportRoute(reportId))
                }
            )
        }
        composable<EditReportRoute> {
            EditReportScreen(onBack = { navController.popBackStack() })
        }
        composable<NotificationsRoute> {
            NotificationsScreen(onBack = { navController.popBackStack() })
        }
        composable<ProfileRoute> {
            ProfileScreen(
                onMyReports = { navController.navigate(MyReportsRoute) },
                onChangePassword = { navController.navigate(ChangePasswordRoute) },
                onReputation = { navController.navigate(ReputationRoute) },
                onLogout = onLogout
            )
        }
        composable<MyReportsRoute> {
            MyReportsScreen(
                onBack = { navController.popBackStack() },
                onReportDetail = { reportId ->
                    navController.navigate(ReportDetailRoute(reportId))
                }
            )
        }
        composable<ChangePasswordRoute> {
            ChangePasswordScreen(onBack = { navController.popBackStack() })
        }
        composable<ReputationRoute> {
            ReputationScreen(onBack = { navController.popBackStack() })
        }
        composable<StatisticsRoute> {
            StatisticsScreen()
        }
    }
}

