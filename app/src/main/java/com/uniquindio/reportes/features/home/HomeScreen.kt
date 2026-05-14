package com.uniquindio.reportes.features.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.uniquindio.reportes.R
import com.uniquindio.reportes.core.preferences.PreferencesViewModel
import com.uniquindio.reportes.core.preferences.SortOption
import com.uniquindio.reportes.core.utils.DisplayUtils
import com.uniquindio.reportes.core.utils.TimeUtils
import com.uniquindio.reportes.domain.model.CitizenReport
import com.uniquindio.reportes.domain.model.ReportCategory
import com.uniquindio.reportes.domain.model.ReportStatus

private fun reporterInitials(name: String, email: String): String {
    if (name.isNotBlank()) {
        return name.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
    }
    return email.substringBefore("@").take(2).uppercase()
}

private fun categoryColor(category: ReportCategory): Color = when (category) {
    ReportCategory.SECURITY -> Color(0xFFE65100)
    ReportCategory.MEDICAL_EMERGENCIES -> Color(0xFFC62828)
    ReportCategory.INFRASTRUCTURE -> Color(0xFF1565C0)
    ReportCategory.PETS -> Color(0xFF2E7D32)
    ReportCategory.COMMUNITY -> Color(0xFF6A1B9A)
}

private fun statusColor(status: ReportStatus): Color = when (status) {
    ReportStatus.PENDING -> Color(0xFFF9A825)
    ReportStatus.VERIFIED -> Color(0xFF2E7D32)
    ReportStatus.REJECTED -> Color(0xFFC62828)
    ReportStatus.RESOLVED -> Color(0xFF1565C0)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenCreateReport: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenReportDetail: (String) -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    prefsViewModel: PreferencesViewModel = hiltViewModel()
) {
    val reports by viewModel.reports.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val userInitials by viewModel.userInitials.collectAsState()
    val cityLabel by viewModel.cityLabel.collectAsState()
    val radiusKm by viewModel.radiusKm.collectAsState()
    val sortBy by prefsViewModel.sortBy.collectAsState()
    val verifiedOnly by prefsViewModel.verifiedOnly.collectAsState()
    val darkMode by prefsViewModel.darkMode.collectAsState()

    var showRadiusDialog by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshUserLocation()
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                cityLabel.ifBlank { stringResource(R.string.home_location) },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            stringResource(R.string.home_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenNotifications) {
                        Icon(Icons.Default.Notifications, contentDescription = stringResource(R.string.notifications_title))
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable(onClick = onOpenProfile),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            userInitials.ifEmpty { stringResource(R.string.home_default_avatar) },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchChange,
                placeholder = { Text(stringResource(R.string.home_search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Ajustes",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ReportCategory.entries.toList()) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            viewModel.onCategoryFilter(
                                if (selectedCategory == category) null else category
                            )
                        },
                        label = { Text(stringResource(DisplayUtils.categoryStringRes(category))) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val nearbyLabel = radiusKm?.let { km ->
                        if (km < 1.0) "Cercanos (<${(km * 1000).toInt()}m)"
                        else "Cercanos (<${km.toInt()}km)"
                    } ?: "Sin límite"
                    SuggestionChip(
                        onClick = { showRadiusDialog = true },
                        label = { Text(nearbyLabel, style = MaterialTheme.typography.labelMedium) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { showRadiusDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Radio de distancia",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    stringResource(R.string.home_report_count, reports.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(reports) { report ->
                    ReportCard(report = report, onClick = { onOpenReportDetail(report.id) })
                }
            }
        }
    }

    if (showRadiusDialog) {
        RadiusDialog(
            currentKm = radiusKm,
            onSelect = {
                viewModel.setRadiusKm(it)
                showRadiusDialog = false
            },
            onDismiss = { showRadiusDialog = false }
        )
    }

    if (showSettingsSheet) {
        SettingsSheet(
            darkMode = darkMode,
            sortBy = sortBy,
            verifiedOnly = verifiedOnly,
            onDarkModeChange = prefsViewModel::setDarkMode,
            onSortByChange = prefsViewModel::setSortBy,
            onVerifiedOnlyChange = prefsViewModel::setVerifiedOnly,
            onDismiss = { showSettingsSheet = false }
        )
    }
}

@Composable
private fun RadiusDialog(
    currentKm: Double?,
    onSelect: (Double?) -> Unit,
    onDismiss: () -> Unit
) {
    val options: List<Pair<String, Double?>> = listOf(
        "<1 km" to 1.0,
        "<2 km" to 2.0,
        "<5 km" to 5.0,
        "<10 km" to 10.0,
        "Sin límite" to null
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Radio de distancia") },
        text = {
            Column {
                options.forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(value) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentKm == value,
                            onClick = { onSelect(value) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(
    darkMode: Boolean?,
    sortBy: SortOption,
    verifiedOnly: Boolean,
    onDarkModeChange: (Boolean?) -> Unit,
    onSortByChange: (SortOption) -> Unit,
    onVerifiedOnlyChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                "Ajustes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Modo oscuro")
                Switch(
                    checked = darkMode == true,
                    onCheckedChange = { onDarkModeChange(if (it) true else false) }
                )
            }
            TextButton(onClick = { onDarkModeChange(null) }) {
                Text("Usar tema del sistema")
            }

            Divider(Modifier.padding(vertical = 8.dp))

            Text("Ordenar por", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            val sortOptions = listOf(
                SortOption.MAS_RECIENTE to "Más reciente",
                SortOption.MAS_CERCANO to "Más cercano",
                SortOption.MAS_RELEVANTE to "Más relevante"
            )
            sortOptions.forEach { (opt, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSortByChange(opt) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = sortBy == opt,
                        onClick = { onSortByChange(opt) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(label)
                }
            }

            Divider(Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mostrar solo reportes verificados")
                Switch(
                    checked = verifiedOnly,
                    onCheckedChange = onVerifiedOnlyChange
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReportCard(report: CitizenReport, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Header: avatar + category + status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(categoryColor(report.category)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        reporterInitials(report.reporterName, report.reporterEmail),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        stringResource(DisplayUtils.categoryStringRes(report.category)),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = categoryColor(report.category)
                    )
                    Text(
                        stringResource(DisplayUtils.statusStringRes(report.status)),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor(report.status)
                    )
                }
            }

            // Large image
            if (report.imageUrls.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.report_photo_placeholder),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                AsyncImage(
                    model = report.imageUrls.first(),
                    contentDescription = report.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Content section
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = report.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = report.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = report.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.home_time_ago, TimeUtils.timeAgo(report.createdAtMillis, LocalContext.current)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${report.importance}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
