package com.example.seguimiento1.features.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.seguimiento1.R
import com.example.seguimiento1.core.utils.DisplayUtils
import com.example.seguimiento1.domain.model.ReportCategory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

private val bogotaCenter = LatLng(4.6097, -74.0817)

private val reportCoordinates = mapOf(
    "seed-1" to LatLng(4.6160, -74.0780),
    "seed-2" to LatLng(4.6050, -74.0730),
    "seed-3" to LatLng(4.6020, -74.0850),
    "seed-4" to LatLng(4.6120, -74.0890),
    "seed-5" to LatLng(4.6090, -74.0710),
    "seed-6" to LatLng(4.6200, -74.0830),
    "seed-7" to LatLng(4.5980, -74.0760)
)

@Composable
fun MapScreen(
    onOpenReportDetail: (String) -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val reports by viewModel.reports.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var categoryExpanded by remember { mutableStateOf(false) }
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bogotaCenter, 14f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
        ) {
            reports.forEach { report ->
                val position = reportCoordinates[report.id]
                    ?: LatLng(
                        bogotaCenter.latitude + (report.id.hashCode() % 100) * 0.0005,
                        bogotaCenter.longitude + (report.id.hashCode() % 73) * 0.0005
                    )
                Marker(
                    state = MarkerState(position = position),
                    title = report.title,
                    snippet = report.address,
                    onInfoWindowClick = { onOpenReportDetail(report.id) }
                )
            }
        }

        // Search bar at top
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchChange,
            placeholder = { Text(stringResource(R.string.map_search_hint)) },
            trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.TopCenter),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Category chip at bottom left
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp)
        ) {
            SuggestionChip(
                onClick = { categoryExpanded = !categoryExpanded },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (selectedCategory != null)
                                stringResource(DisplayUtils.categoryStringRes(selectedCategory!!))
                            else
                                stringResource(R.string.map_categories)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
            DropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.category_all)) },
                    onClick = {
                        viewModel.onCategoryFilter(null)
                        categoryExpanded = false
                    }
                )
                ReportCategory.entries.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(stringResource(DisplayUtils.categoryStringRes(cat))) },
                        onClick = {
                            viewModel.onCategoryFilter(cat)
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        // Location FAB at bottom right
        FloatingActionButton(
            onClick = { },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null)
        }
    }
}
