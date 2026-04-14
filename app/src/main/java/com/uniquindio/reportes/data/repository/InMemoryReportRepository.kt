package com.uniquindio.reportes.data.repository

import com.uniquindio.reportes.domain.model.CitizenReport
import com.uniquindio.reportes.domain.model.CreateReportData
import com.uniquindio.reportes.domain.model.ReportCategory
import com.uniquindio.reportes.domain.model.ReportStatus
import com.uniquindio.reportes.domain.repository.ReportRepository
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryReportRepository : ReportRepository {

    private val reports = MutableStateFlow(
        listOf(
            CitizenReport(
                id = "seed-1",
                title = "Robo a mano armada en Calle 80",
                description = "Se reportan dos sujetos en moto robando a transeuntes a punta de navaja. Uno de ellos viste camiseta roja y el otro jean azul con capucha negra. Ya se alertó a la policía.",
                address = "Calle 80 #15-22, Bogotá",
                category = ReportCategory.SECURITY,
                status = ReportStatus.VERIFIED,
                imageUrl = "https://picsum.photos/seed/robo80/600/400",
                reporterEmail = "demo@ciudad.com",
                reporterName = "Demo Usuario",
                createdAtMillis = System.currentTimeMillis() - 7_200_000,
                importance = 50
            ),
            CitizenReport(
                id = "seed-2",
                title = "Robo de celular en transporte",
                description = "Un sujeto hurtó un celular dentro del bus de la ruta T13.",
                address = "Av. Caracas #45-10, Bogotá",
                category = ReportCategory.SECURITY,
                status = ReportStatus.PENDING,
                imageUrl = "https://picsum.photos/seed/celular/600/400",
                reporterEmail = "ana@ciudad.com",
                reporterName = "Ana García",
                createdAtMillis = System.currentTimeMillis() - 1_800_000,
                importance = 12
            ),
            CitizenReport(
                id = "seed-3",
                title = "Fuga de agua en la calle 15",
                description = "Hay una fuga de agua considerable en la tubería principal de la calle 15.",
                address = "Calle 15 #8-30, Bogotá",
                category = ReportCategory.INFRASTRUCTURE,
                status = ReportStatus.PENDING,
                imageUrl = "https://picsum.photos/seed/fuga/600/400",
                reporterEmail = "miguel@ciudad.com",
                reporterName = "Miguel Torres",
                createdAtMillis = System.currentTimeMillis() - 3_600_000,
                importance = 25
            ),
            CitizenReport(
                id = "seed-4",
                title = "Perro labrador encontrado sin dueño",
                description = "Se encontró un perro labrador color dorado sin collar en el parque central.",
                address = "Parque Central, Bogotá",
                category = ReportCategory.PETS,
                status = ReportStatus.PENDING,
                imageUrl = "https://picsum.photos/seed/perro/600/400",
                reporterEmail = "laura@ciudad.com",
                reporterName = "Laura Martínez",
                createdAtMillis = System.currentTimeMillis() - 7_200_000,
                importance = 8
            ),
            CitizenReport(
                id = "seed-5",
                title = "Accidente en la intersección de la 7ma",
                description = "Colisión entre dos vehículos particulares, hay heridos leves.",
                address = "Carrera 7 con Calle 72, Bogotá",
                category = ReportCategory.MEDICAL_EMERGENCIES,
                status = ReportStatus.PENDING,
                imageUrl = "https://picsum.photos/seed/accidente/600/400",
                reporterEmail = "pedro@ciudad.com",
                reporterName = "Pedro López",
                createdAtMillis = System.currentTimeMillis() - 18_000_000,
                importance = 35
            ),
            CitizenReport(
                id = "seed-6",
                title = "Basura acumulada frente al parque",
                description = "Hay una gran cantidad de basura acumulada frente al parque del barrio que no ha sido recogida.",
                address = "Calle 50 #20-15, Bogotá",
                category = ReportCategory.COMMUNITY,
                status = ReportStatus.PENDING,
                imageUrl = "https://picsum.photos/seed/basura/600/400",
                reporterEmail = "sofia@ciudad.com",
                reporterName = "Sofía Ramírez",
                createdAtMillis = System.currentTimeMillis() - 28_800_000,
                importance = 15
            ),
            CitizenReport(
                id = "seed-7",
                title = "Poste de alumbrado caído en la Av. Principal",
                description = "El poste de alumbrado publico se encuentra caido sobre la acera.",
                address = "Av. Principal #30-10, Bogotá",
                category = ReportCategory.INFRASTRUCTURE,
                status = ReportStatus.PENDING,
                imageUrl = "https://picsum.photos/seed/poste/600/400",
                reporterEmail = "demo@ciudad.com",
                reporterName = "Demo Usuario",
                createdAtMillis = System.currentTimeMillis() - 86_400_000,
                importance = 80
            )
        )
    )

    override val reportsFlow: Flow<List<CitizenReport>> = reports

    override fun reportById(reportId: String): Flow<CitizenReport?> {
        return reports.map { list -> list.firstOrNull { it.id == reportId } }
    }

    override fun reportsByEmail(email: String): Flow<List<CitizenReport>> {
        return reports.map { list -> list.filter { it.reporterEmail == email } }
    }

    override suspend fun createReport(data: CreateReportData) {
        val newReport = CitizenReport(
            id = UUID.randomUUID().toString(),
            title = data.title,
            description = data.description,
            address = data.address,
            category = data.category,
            status = ReportStatus.PENDING,
            imageUrl = data.imageUrl.orEmpty(),
            reporterEmail = data.reporterEmail,
            reporterName = data.reporterName,
            createdAtMillis = System.currentTimeMillis(),
            latitude = data.latitude,
            longitude = data.longitude
        )
        reports.value = listOf(newReport) + reports.value
    }

    override suspend fun updateReport(report: CitizenReport) {
        reports.value = reports.value.map { if (it.id == report.id) report else it }
    }

    override suspend fun deleteReport(reportId: String) {
        reports.value = reports.value.filter { it.id != reportId }
    }

    override suspend fun toggleImportance(reportId: String, voterEmail: String) {
        reports.value = reports.value.map { report ->
            if (report.id == reportId) {
                val alreadyVoted = voterEmail in report.voterEmails
                if (alreadyVoted) {
                    report.copy(
                        importance = report.importance - 1,
                        voterEmails = report.voterEmails - voterEmail
                    )
                } else {
                    report.copy(
                        importance = report.importance + 1,
                        voterEmails = report.voterEmails + voterEmail
                    )
                }
            } else report
        }
    }

    override suspend fun verifyReport(reportId: String) {
        reports.value = reports.value.map {
            if (it.id == reportId) it.copy(status = ReportStatus.VERIFIED) else it
        }
    }

    override suspend fun rejectReport(reportId: String) {
        reports.value = reports.value.map {
            if (it.id == reportId) it.copy(status = ReportStatus.REJECTED) else it
        }
    }

    override suspend fun markResolved(reportId: String) {
        reports.value = reports.value.map {
            if (it.id == reportId) it.copy(isResolved = true, status = ReportStatus.RESOLVED) else it
        }
    }
}

