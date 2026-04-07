package com.example.seguimiento1.data.repository

import com.example.seguimiento1.data.datastore.UsersDataStore
import com.example.seguimiento1.domain.model.RegisterData
import com.example.seguimiento1.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first

class DataStoreAuthRepository(
    private val usersDataStore: UsersDataStore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Boolean {
        val users = usersDataStore.usersFlow.first()
        return users[email] == password
    }

    override suspend fun register(data: RegisterData): Boolean {
        val users = usersDataStore.usersFlow.first().toMutableMap()
        if (users.containsKey(data.email)) return false

        users[data.email] = data.password
        usersDataStore.saveUsers(users)
        return true
    }

    override suspend fun sendRecovery(email: String): Boolean {
        val users = usersDataStore.usersFlow.first()
        return users.containsKey(email)
    }
}

