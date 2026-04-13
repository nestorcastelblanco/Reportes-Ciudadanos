package com.example.seguimiento1.domain.repository

import com.example.seguimiento1.domain.model.RegisterData
import com.example.seguimiento1.domain.model.User
import com.example.seguimiento1.domain.model.UserRole

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

