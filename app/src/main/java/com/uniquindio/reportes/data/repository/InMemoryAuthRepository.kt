package com.uniquindio.reportes.data.repository

import com.uniquindio.reportes.domain.model.RegisterData
import com.uniquindio.reportes.domain.model.User
import com.uniquindio.reportes.domain.model.UserRole
import com.uniquindio.reportes.domain.repository.AuthRepository

class InMemoryAuthRepository : AuthRepository {

    private val passwords = mutableMapOf(
        "demo@ciudad.com" to "demo1234",
        "mod@ciudad.com" to "mod12345"
    )

    private val users = mutableMapOf(
        "demo@ciudad.com" to User(
            email = "demo@ciudad.com",
            nombre = "Valentina Cruz",
            telefono = "3217787061",
            ciudad = "Bogotá, Colombia",
            role = UserRole.USER,
            joinDateMillis = 1704067200000, // Jan 2024
            points = 340
        ),
        "mod@ciudad.com" to User(
            email = "mod@ciudad.com",
            nombre = "Carlos Ramirez",
            telefono = "3001234567",
            ciudad = "Bogotá, Colombia",
            role = UserRole.MODERATOR,
            joinDateMillis = 1696118400000,
            points = 0
        )
    )

    override suspend fun login(email: String, password: String): Boolean {
        return passwords[email] == password
    }

    override suspend fun register(data: RegisterData): Boolean {
        if (passwords.containsKey(data.email)) return false
        passwords[data.email] = data.password
        users[data.email] = User(
            email = data.email,
            nombre = data.nombre,
            telefono = data.telefono,
            ciudad = data.ciudad,
            role = UserRole.USER
        )
        return true
    }

    override suspend fun sendRecovery(email: String): Boolean {
        return passwords.containsKey(email)
    }

    override suspend fun changePassword(email: String, oldPassword: String, newPassword: String): Boolean {
        if (passwords[email] != oldPassword) return false
        passwords[email] = newPassword
        return true
    }

    override suspend fun getUserByEmail(email: String): User? {
        return users[email]
    }

    override suspend fun updateUser(user: User) {
        users[user.email] = user
    }

    override suspend fun deleteAccount(email: String): Boolean {
        passwords.remove(email)
        users.remove(email)
        return true
    }

    override suspend fun getUserRole(email: String): UserRole {
        return users[email]?.role ?: UserRole.USER
    }

    override suspend fun addPoints(email: String, points: Int) {
        val user = users[email] ?: return
        users[email] = user.copy(points = user.points + points)
    }
}

