package com.example.seguimiento1.data.repository

import com.example.seguimiento1.domain.model.RegisterData
import com.example.seguimiento1.domain.repository.AuthRepository

class InMemoryAuthRepository : AuthRepository {

    private val usersByEmail = mutableMapOf(
        "demo@ciudad.com" to "demo1234"
    )

    override suspend fun login(email: String, password: String): Boolean {
        return usersByEmail[email] == password
    }

    override suspend fun register(data: RegisterData): Boolean {
        if (usersByEmail.containsKey(data.email)) return false
        usersByEmail[data.email] = data.password
        return true
    }

    override suspend fun sendRecovery(email: String): Boolean {
        return usersByEmail.containsKey(email)
    }
}

