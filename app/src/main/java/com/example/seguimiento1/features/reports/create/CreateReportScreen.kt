package com.example.seguimiento1.features.reports.create

import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.seguimiento1.R
import com.example.seguimiento1.core.utils.DisplayUtils.categoryStringRes
import com.example.seguimiento1.domain.model.ReportCategory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.io.File
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

private val bogotaCenter = LatLng(4.6097, -74.0817)

private data class AddressSuggestion(
    val fullAddress: String,
    val latLng: LatLng
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReportScreen(
    onBack: () -> Unit,
    viewModel: CreateReportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val address by viewModel.address.collectAsState()
    val category by viewModel.category.collectAsState()
    var categoryExpanded by remember { mutableStateOf(false) }
    var addressExpanded by remember { mutableStateOf(false) }
    var addressSuggestions by remember { mutableStateOf(emptyList<AddressSuggestion>()) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var addressSearchJob by remember { mutableStateOf<Job?>(null) }

    var photoUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bogotaCenter, 13f)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoUriString = uri.toString()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUriString = pendingCameraUri?.toString()
        }
    }

    LaunchedEffect(selectedLocation) {
        selectedLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 16f)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_report_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(R.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = viewModel::onTitleChange,
                label = { Text(stringResource(R.string.report_title_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text(stringResource(R.string.report_description_label)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )
            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = stringResource(categoryStringRes(category)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.report_category_label)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    ReportCategory.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(stringResource(categoryStringRes(option))) },
                            onClick = {
                                viewModel.onCategoryChange(option)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    val suggested = viewModel.suggestCategoryFromText()
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(
                                R.string.report_category_suggested,
                                context.getString(categoryStringRes(suggested))
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.report_suggest_category))
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                stringResource(R.string.report_ai_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(stringResource(R.string.report_add_photos), style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            if (photoUriString.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.report_photo_placeholder),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                AsyncImage(
                    model = photoUriString,
                    contentDescription = stringResource(R.string.report_photo_placeholder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Create, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(stringResource(R.string.report_gallery))
                }
                FilledTonalButton(
                    onClick = {
                        val imageFile = createImageFile(context.cacheDir)
                        val imageUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            imageFile
                        )
                        pendingCameraUri = imageUri
                        cameraLauncher.launch(imageUri)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(stringResource(R.string.report_camera))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box {
                OutlinedTextField(
                    value = address,
                    onValueChange = { query ->
                        viewModel.onAddressChange(query)
                        selectedLocation = null
                        addressSearchJob?.cancel()
                        if (query.length < 4) {
                            addressSuggestions = emptyList()
                            addressExpanded = false
                        } else {
                            addressSearchJob = scope.launch {
                                delay(400)
                                addressSuggestions = geocodeSuggestions(context, query)
                                addressExpanded = addressSuggestions.isNotEmpty()
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.report_address_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )

                DropdownMenu(
                    expanded = addressExpanded,
                    onDismissRequest = { addressExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.92f)
                ) {
                    addressSuggestions.forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion.fullAddress) },
                            onClick = {
                                viewModel.onAddressChange(suggestion.fullAddress)
                                selectedLocation = suggestion.latLng
                                addressExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.report_address_map_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                ) {
                    selectedLocation?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = stringResource(R.string.report_selected_location)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Moderator notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Text(
                    stringResource(R.string.report_moderator_notice),
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank() || address.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.report_snackbar_required_fields)
                            )
                        }
                    } else {
                        viewModel.submit(
                            imageUrl = photoUriString,
                            latitude = selectedLocation?.latitude,
                            longitude = selectedLocation?.longitude,
                            geocode = { addr ->
                                geocodeFirstResult(context, addr)
                            }
                        ) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.report_snackbar_created)
                                )
                                onBack()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.create_report_submit),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

private fun createImageFile(cacheDir: File): File {
    return File.createTempFile("report_photo_", ".jpg", cacheDir)
}

private suspend fun geocodeSuggestions(context: android.content.Context, query: String): List<AddressSuggestion> {
    return withContext(Dispatchers.IO) {
        runCatching {
            val geocoder = Geocoder(context, Locale.getDefault())
            val searchQuery = if (query.contains("colombia", ignoreCase = true)) query
            else "$query, Colombia"
            val addresses = geocoder.getFromLocationNameCompat(searchQuery, 8)
            addresses
                .mapNotNull { address -> address.toSuggestion() }
                .distinctBy { it.fullAddress }
        }.getOrDefault(emptyList())
    }
}

private fun Address.toSuggestion(): AddressSuggestion? {
    val line = getAddressLine(0).orEmpty().ifBlank {
        listOfNotNull(thoroughfare, subLocality, locality).joinToString(", ")
    }
    if (line.isBlank()) return null
    return AddressSuggestion(
        fullAddress = line,
        latLng = LatLng(latitude, longitude)
    )
}

private suspend fun Geocoder.getFromLocationNameCompat(
    locationName: String,
    maxResults: Int
): List<Address> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        suspendCancellableCoroutine { continuation ->
            getFromLocationName(locationName, maxResults, object : Geocoder.GeocodeListener {
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
        getFromLocationName(locationName, maxResults) ?: emptyList()
    }
}

private suspend fun geocodeFirstResult(context: android.content.Context, query: String): Pair<Double, Double>? {
    return withContext(Dispatchers.IO) {
        runCatching {
            val geocoder = Geocoder(context, Locale.getDefault())
            val searchQuery = if (query.contains("colombia", ignoreCase = true)) query
            else "$query, Colombia"
            val results = geocoder.getFromLocationNameCompat(searchQuery, 1)
            results.firstOrNull()?.let { it.latitude to it.longitude }
        }.getOrNull()
    }
}







