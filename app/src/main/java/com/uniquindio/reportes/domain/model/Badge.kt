package com.uniquindio.reportes.domain.model

import androidx.annotation.StringRes

data class Badge(
    val id: String,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    val isUnlocked: Boolean
)
