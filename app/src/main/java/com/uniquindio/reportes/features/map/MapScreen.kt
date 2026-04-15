package com.uniquindio.reportes.features.map

import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.uniquindio.reportes.R
import com.uniquindio.reportes.domain.model.ReportCategory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

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

private fun categoryMarkerColor(category: ReportCategory): Color = when (category) {
    ReportCategory.SECURITY          -> Color(0xFFE53935)
    ReportCategory.MEDICAL_EMERGENCIES -> Color(0xFFE91E63)
    ReportCategory.INFRASTRUCTURE    -> Color(0xFFFF8F00)
    ReportCategory.PETS              -> Color(0xFF43A047)
    ReportCategory.COMMUNITY         -> Color(0xFF1E88E5)
}

private fun categoryMarkerIcon(category: ReportCategory): ImageVector = when (category) {
    ReportCategory.SECURITY            -> Icons.Default.Shield
    ReportCategory.MEDICAL_EMERGENCIES -> Icons.Default.LocalHospital
    ReportCategory.INFRASTRUCTURE      -> Icons.Default.Build
    ReportCategory.PETS                -> Icons.Default.Pets
    ReportCategory.COMMUNITY           -> Icons.Default.People
}

@Composable
fun MapScreen(
    onOpenReportDetail: (String) -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reports by viewModel.reports.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bogotaCenter, 14f)
    }

    val searchLocation: () -> Unit = {
        if (searchQuery.isNotBlank()) {
            scope.launch {
                val result = geocodeLocation(context, searchQuery)
                if (result != null) {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(result, 14f),
                        durationMs = 800
                    )
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
        ) {
            reports.forEach { report ->
                val position = if (report.latitude != null && report.longitude != null) {
                    LatLng(report.latitude, report.longitude)
                } else {
                    reportCoordinates[report.id]
                        ?: LatLng(
                            bogotaCenter.latitude + (report.id.hashCode() % 100) * 0.0005,
                            bogotaCenter.longitude + (report.id.hashCode() % 73) * 0.0005
                        )
                }

                val markerColor = categoryMarkerColor(report.category)
                val markerIcon  = categoryMarkerIcon(report.category)

                MarkerComposable(
                    keys = arrayOf<Any>(report.id, report.category),
                    state = MarkerState(position = position),
                    title = report.title,
                    snippet = report.address,
                    onInfoWindowClick = { onOpenReportDetail(report.id) }
                ) {
                    CategoryPinMarker(color = markerColor, icon = markerIcon)
                }
            }
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchChange,
            placeholder = { Text(stringResource(R.string.map_search_hint)) },
            trailingIcon = {
                IconButton(onClick = searchLocation) {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { searchLocation() }),
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

        // Location FAB
        FloatingActionButton(
            onClick = {
                scope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(bogotaCenter, 14f),
                        durationMs = 600
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null)
        }
    }
}

/** Circular pin marker with category color and icon, plus a downward triangle pointer. */
@Composable
private fun CategoryPinMarker(color: Color, icon: ImageVector) {
    val triangleSize = 10.dp
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(color, CircleShape)
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        // Downward triangle pointer
        Box(
            modifier = Modifier
                .size(width = triangleSize, height = triangleSize / 2)
                .drawBehind {
                    drawTriangle(color)
                }
        )
    }
}

private fun DrawScope.drawTriangle(color: Color) {
    val path = Path().apply {
        moveTo(0f, 0f)
        lineTo(size.width, 0f)
        lineTo(size.width / 2f, size.height)
        close()
    }
    drawPath(path, color = color)
}

private suspend fun geocodeLocation(context: android.content.Context, query: String): LatLng? {
    return withContext(Dispatchers.IO) {
        runCatching {
            val geocoder = Geocoder(context, Locale.getDefault())
            val searchQuery = if (query.contains("colombia", ignoreCase = true)) query
            else "$query, Colombia"
            val results = geocoderGetFromLocationNameCompat(geocoder, searchQuery, 1)
            results.firstOrNull()?.let { LatLng(it.latitude, it.longitude) }
        }.getOrNull()
    }
}

private suspend fun geocoderGetFromLocationNameCompat(
    geocoder: Geocoder,
    locationName: String,
    maxResults: Int
): List<Address> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        suspendCancellableCoroutine { continuation ->
            geocoder.getFromLocationName(locationName, maxResults, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    continuation.resume(addresses.toList())
                }
                override fun onError(errorMessage: String?) {
                    continuation.resume(emptyList())
                }
            })
        }
    } else {
        @Suppress("DEPRECATION")
        geocoder.getFromLocationName(locationName, maxResults) ?: emptyList()
    }
}
