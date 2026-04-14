package com.uniquindio.reportes.core.navigation

import kotlinx.serialization.Serializable

@Serializable
data object AuthGraph

@Serializable
data object HomeGraph

@Serializable
data object ModeratorGraph

@Serializable
data object LoginRoute

@Serializable
data object RegisterRoute

@Serializable
data object RecoverPasswordRoute

@Serializable
data object NewPasswordRoute

@Serializable
data object HomeRoute

@Serializable
data object MapRoute

@Serializable
data object CreateReportRoute

@Serializable
data class ReportDetailRoute(val reportId: String)

@Serializable
data class EditReportRoute(val reportId: String)

@Serializable
data object NotificationsRoute

@Serializable
data object ProfileRoute

@Serializable
data object MyReportsRoute

@Serializable
data object ChangePasswordRoute

@Serializable
data object ReputationRoute

@Serializable
data object StatisticsRoute

@Serializable
data object ModerationPanelRoute

@Serializable
data class ReviewReportRoute(val reportId: String)

