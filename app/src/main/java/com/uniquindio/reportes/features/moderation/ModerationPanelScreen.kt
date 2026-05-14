package com.uniquindio.reportes.features.moderation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.uniquindio.reportes.core.utils.DisplayUtils
import com.uniquindio.reportes.core.utils.TimeUtils
import com.uniquindio.reportes.domain.model.CitizenReport
import com.uniquindio.reportes.domain.model.ReportCategory
import com.uniquindio.reportes.domain.model.ReportStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModerationPanelScreen(
    onReviewReport: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ModerationPanelViewModel = hiltViewModel()
) {
    val tab by viewModel.selectedTab.collectAsState()
    val reports by viewModel.filteredReports.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
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
                                    ModerationTab.PENDING -> "Pendientes"
                                    ModerationTab.VERIFIED -> "Verificados"
                                    ModerationTab.REJECTED -> "Rechazados"
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
            ) {
                items(reports, key = { it.id }) { report ->
                    ReportModerationCard(
                        report = report,
                        tab = tab,
                        onOpen = { onReviewReport(report.id) },
                        onVerify = { viewModel.verifyReport(report.id) },
                        onReject = { viewModel.rejectReport(report.id) },
                        onResolve = { viewModel.markResolved(report.id) }
                    )
                }
                if (reports.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No hay reportes en esta categoría.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportModerationCard(
    report: CitizenReport,
    tab: ModerationTab,
    onOpen: () -> Unit,
    onVerify: () -> Unit,
    onReject: () -> Unit,
    onResolve: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        report.reporterName.ifBlank { report.reporterEmail },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        DisplayUtils.categoryStringRes(report.category).let { res ->
                            androidx.compose.ui.res.stringResource(res)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = categoryColor(report.category)
                    )
                }
                StatusPill(report.status)
            }

            if (report.imageUrls.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Sin imagen",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                AsyncImage(
                    model = report.imageUrls.first(),
                    contentDescription = report.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(0.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    report.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        report.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    report.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    TimeUtils.timeAgo(report.createdAtMillis, LocalContext.current),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(10.dp))

                when (tab) {
                    ModerationTab.PENDING -> Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onVerify,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Verificar")
                        }
                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFFC62828))
                            Spacer(Modifier.width(6.dp))
                            Text("Rechazar", color = Color(0xFFC62828))
                        }
                    }
                    ModerationTab.VERIFIED -> if (!report.isResolved) {
                        Button(
                            onClick = onResolve,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.TaskAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Marcar como finalizado")
                        }
                    } else {
                        Text(
                            "Finalizado",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    ModerationTab.REJECTED -> Unit
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: ReportStatus) {
    val (label, color) = when (status) {
        ReportStatus.PENDING -> "Pendiente" to Color(0xFFF9A825)
        ReportStatus.VERIFIED -> "Verificado" to Color(0xFF2E7D32)
        ReportStatus.REJECTED -> "Rechazado" to Color(0xFFC62828)
        ReportStatus.RESOLVED -> "Finalizado" to MaterialTheme.colorScheme.primary
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
    }
}

private fun categoryColor(category: ReportCategory): Color = when (category) {
    ReportCategory.SECURITY -> Color(0xFFE65100)
    ReportCategory.MEDICAL_EMERGENCIES -> Color(0xFFC62828)
    ReportCategory.INFRASTRUCTURE -> Color(0xFF1565C0)
    ReportCategory.PETS -> Color(0xFF2E7D32)
    ReportCategory.COMMUNITY -> Color(0xFF6A1B9A)
}
