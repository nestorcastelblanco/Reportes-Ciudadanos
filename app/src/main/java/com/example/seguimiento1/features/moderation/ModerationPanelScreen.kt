package com.example.seguimiento1.features.moderation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.seguimiento1.R
import com.example.seguimiento1.core.utils.DisplayUtils.categoryStringRes
import com.example.seguimiento1.core.utils.TimeUtils
import com.example.seguimiento1.domain.model.CitizenReport

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModerationPanelScreen(
    onReviewReport: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: ModerationPanelViewModel = hiltViewModel()
) {
    val tab by viewModel.selectedTab.collectAsState()
    val reports by viewModel.filteredReports.collectAsState(initial = emptyList())
    val stats by viewModel.stats.collectAsState(initial = Triple(0, 0, 0))

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.moderation_title)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = ModerationTab.entries.indexOf(tab)) {
                ModerationTab.entries.forEach { t ->
                    Tab(
                        selected = tab == t,
                        onClick = { viewModel.selectTab(t) },
                        text = {
                            Text(
                                when (t) {
                                    ModerationTab.PENDING -> stringResource(R.string.moderation_pending)
                                    ModerationTab.VERIFIED -> stringResource(R.string.moderation_verified)
                                    ModerationTab.REJECTED -> stringResource(R.string.moderation_rejected)
                                }
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(reports, key = { it.id }) { report ->
                    ModerationReportCard(
                        report = report,
                        onReview = { onReviewReport(report.id) },
                        onVerify = { viewModel.verifyReport(report.id) },
                        onReject = { viewModel.rejectReport(report.id) },
                        showActions = tab == ModerationTab.PENDING
                    )
                }
                if (reports.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.moderation_no_reports),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider()

            // Stats footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${stats.first}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.moderation_pending), style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${stats.second}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.moderation_verified), style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${stats.third}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.moderation_rejected), style = MaterialTheme.typography.labelSmall)
                }
            }

            FilledTonalButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                Text(stringResource(R.string.moderation_logout))
            }
        }
    }
}

@Composable
private fun ModerationReportCard(
    report: CitizenReport,
    onReview: () -> Unit,
    onVerify: () -> Unit,
    onReject: () -> Unit,
    showActions: Boolean
) {
    Card(
        onClick = onReview,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(report.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(categoryStringRes(report.category)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        TimeUtils.timeAgo(report.createdAtMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (showActions) {
                    Row {
                        IconButton(onClick = onVerify) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = stringResource(R.string.moderation_verify),
                                tint = Color(0xFF4CAF50)
                            )
                        }
                        IconButton(onClick = onReject) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.moderation_reject),
                                tint = Color(0xFFF44336)
                            )
                        }
                    }
                }
            }

            Text(
                "${report.importance} ${stringResource(R.string.report_importance)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
