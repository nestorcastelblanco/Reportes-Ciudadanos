package com.example.seguimiento1.domain.repository

import com.example.seguimiento1.domain.model.RegisterData

interface AuthRepository {
    suspend fun login(email: String, password: String): Boolean
    suspend fun register(data: RegisterData): Boolean
    suspend fun sendRecovery(email: String): Boolean
}

