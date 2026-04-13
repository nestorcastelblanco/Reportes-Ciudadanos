package com.example.seguimiento1.features.map

import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.seguimiento1.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
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

        // Location FAB — reset to Bogotá center
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
