package com.example.seguimiento1.domain.model

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

enum class UserLevel(val displayName: String, val minPoints: Int) {
    NOVATO("Novato", 0),
    COLABORADOR("Colaborador", 101),
    GUARDIAN("Guardián", 301),
    HEROE_COMUNITARIO("Héroe Comunitario", 501)
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

