package com.uniquindio.reportes.domain.model

import androidx.annotation.StringRes
import com.uniquindio.reportes.R

data class RegisterData(
    val nombre: String,
    val email: String,
    val telefono: String,
    val ciudad: String,
    val password: String
)

enum class UserRole {
    USER,
    MODERATOR
}

enum class UserLevel(@StringRes val displayNameRes: Int, val minPoints: Int) {
    NOVATO(R.string.level_novato, 0),
    COLABORADOR(R.string.level_colaborador, 101),
    GUARDIAN(R.string.level_guardian, 301),
    HEROE_COMUNITARIO(R.string.level_heroe_comunitario, 501)
}

data class User(
    val email: String,
    val nombre: String,
    val telefono: String,
    val ciudad: String,
    val role: UserRole = UserRole.USER,
    val joinDateMillis: Long = System.currentTimeMillis(),
    val points: Int = 0
) {
    val level: UserLevel
        get() = UserLevel.entries.last { points >= it.minPoints }

    val initials: String
        get() = nombre.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
}

