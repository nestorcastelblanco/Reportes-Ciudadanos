package com.uniquindio.reportes.domain.repository

import com.uniquindio.reportes.domain.model.RegisterData
import com.uniquindio.reportes.domain.model.User
import com.uniquindio.reportes.domain.model.UserRole

interface AuthRepository {
    suspend fun login(email: String, password: String): Boolean
    suspend fun register(data: RegisterData): Boolean
    suspend fun sendRecovery(email: String): Boolean
    suspend fun changePassword(email: String, oldPassword: String, newPassword: String): Boolean
    suspend fun getUserByEmail(email: String): User?
    suspend fun updateUser(user: User)
    suspend fun deleteAccount(email: String): Boolean
    suspend fun getUserRole(email: String): UserRole
    suspend fun addPoints(email: String, points: Int)
}

