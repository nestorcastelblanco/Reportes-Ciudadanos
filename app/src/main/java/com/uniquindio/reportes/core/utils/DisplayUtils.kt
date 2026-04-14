package com.uniquindio.reportes.core.utils

import com.uniquindio.reportes.R
import com.uniquindio.reportes.domain.model.ReportCategory
import com.uniquindio.reportes.domain.model.ReportStatus

object DisplayUtils {

    fun categoryStringRes(category: ReportCategory): Int = when (category) {
        ReportCategory.SECURITY -> R.string.category_security
        ReportCategory.MEDICAL_EMERGENCIES -> R.string.category_medical
        ReportCategory.INFRASTRUCTURE -> R.string.category_infrastructure
        ReportCategory.PETS -> R.string.category_pets
        ReportCategory.COMMUNITY -> R.string.category_community
    }

    fun statusStringRes(status: ReportStatus): Int = when (status) {
        ReportStatus.PENDING -> R.string.status_pending
        ReportStatus.VERIFIED -> R.string.status_verified
        ReportStatus.REJECTED -> R.string.status_rejected
        ReportStatus.RESOLVED -> R.string.status_resolved
    }
}
