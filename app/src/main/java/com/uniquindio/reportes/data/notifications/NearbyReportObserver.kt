package com.uniquindio.reportes.data.notifications

import com.uniquindio.reportes.data.location.LocationProvider
import com.uniquindio.reportes.domain.model.CitizenReport
import com.uniquindio.reportes.domain.model.NotificationType
import com.uniquindio.reportes.domain.repository.ReportRepository
import com.uniquindio.reportes.domain.repository.SessionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Detecta nuevos reportes cercanos (<5 km) al usuario actual y publica una
 * notificación en Firestore para él mismo.
 *
 * Esto reemplaza el rol de Cloud Functions: cada cliente, mientras está
 * activo, observa la colección `reports` y se "auto-notifica" cuando aparece
 * un reporte que no es suyo y queda dentro del radio.
 *
 * Para evitar spam al primer snapshot, descartamos el conjunto inicial y solo
 * notificamos por reportes que llegan después.
 */
@Singleton
class NearbyReportObserver @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val reportRepository: ReportRepository,
    private val locationProvider: LocationProvider,
    private val notificationStore: FirestoreNotificationStore
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        job = scope.launch {
            sessionRepository.sessionFlow
                .map { it.email.orEmpty() }
                .distinctUntilChanged()
                .collect { email ->
                    if (email.isBlank()) return@collect
                    watchFor(email)
                }
        }
    }

    private suspend fun watchFor(email: String) {
        val seen = mutableSetOf<String>()
        var initialized = false

        reportRepository.reportsFlow.collect { reports ->
            if (!initialized) {
                reports.forEach { seen.add(it.id) }
                initialized = true
                return@collect
            }
            val newOnes = reports.filter { it.id !in seen }
            newOnes.forEach { seen.add(it.id) }
            if (newOnes.isEmpty()) return@collect

            val userLocation = locationProvider.currentLocation() ?: return@collect

            newOnes
                .filter { it.reporterEmail != email }
                .filter { withinRadius(userLocation.latitude, userLocation.longitude, it, RADIUS_METERS) }
                .forEach { report ->
                    notificationStore.publish(
                        recipientEmail = email,
                        type = NotificationType.NEARBY_REPORT,
                        title = "Reporte cercano",
                        message = "Nuevo reporte cerca de ti: \"${report.title}\".",
                        reportId = report.id
                    )
                }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private fun withinRadius(lat: Double, lon: Double, report: CitizenReport, limitMeters: Double): Boolean {
        val rLat = report.latitude ?: return false
        val rLon = report.longitude ?: return false
        return haversineMeters(lat, lon, rLat, rLon) <= limitMeters
    }

    private companion object {
        const val RADIUS_METERS = 5_000.0
        const val EARTH_RADIUS_METERS = 6_371_000.0

        fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return EARTH_RADIUS_METERS * c
        }
    }
}
