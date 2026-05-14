package com.uniquindio.reportes.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Obtiene la ubicación actual del dispositivo usando FusedLocationProviderClient.
 * Devuelve null si no hay permiso o si Google Play no puede entregar una posición.
 */
@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val client by lazy { LocationServices.getFusedLocationProviderClient(context) }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun currentLocation(): UserLocation? {
        if (!hasLocationPermission()) return null
        return runCatching {
            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .build()
            val location = client.getCurrentLocation(request, null).await()
                ?: client.lastLocation.await()
            location?.let { UserLocation(it.latitude, it.longitude) }
        }.getOrNull()
    }
}

data class UserLocation(val latitude: Double, val longitude: Double)

data class ReverseGeocodeResult(
    val fullAddress: String,
    val city: String,
    val country: String
) {
    val cityLabel: String
        get() = listOf(city, country).filter { it.isNotBlank() }.joinToString(", ")
}

suspend fun Context.reverseGeocode(lat: Double, lng: Double): ReverseGeocodeResult? {
    return withContext(Dispatchers.IO) {
        runCatching {
            val geocoder = Geocoder(this@reverseGeocode, Locale.getDefault())
            val addresses = geocoderGetFromLocation(geocoder, lat, lng, 1)
            addresses.firstOrNull()?.toReverseResult()
        }.getOrNull()
    }
}

private suspend fun geocoderGetFromLocation(
    geocoder: Geocoder,
    lat: Double,
    lng: Double,
    maxResults: Int
): List<Address> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        suspendCancellableCoroutine { continuation ->
            geocoder.getFromLocation(lat, lng, maxResults, object : Geocoder.GeocodeListener {
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
        geocoder.getFromLocation(lat, lng, maxResults) ?: emptyList()
    }
}

private fun Address.toReverseResult(): ReverseGeocodeResult {
    val full = getAddressLine(0).orEmpty().ifBlank {
        listOfNotNull(thoroughfare, subLocality, locality).joinToString(", ")
    }
    val city = locality ?: subAdminArea ?: adminArea.orEmpty()
    return ReverseGeocodeResult(
        fullAddress = full,
        city = city.orEmpty(),
        country = countryName.orEmpty()
    )
}
