package com.uniquindio.reportes.data.repository

import com.uniquindio.reportes.data.datastore.UsersDataStore
import com.uniquindio.reportes.domain.model.RegisterData
import com.uniquindio.reportes.domain.model.User
import com.uniquindio.reportes.domain.model.UserRole
import com.uniquindio.reportes.domain.repository.AuthRepository
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

    override suspend fun changePassword(email: String, oldPassword: String, newPassword: String): Boolean = false
    override suspend fun getUserByEmail(email: String): User? = null
    override suspend fun updateUser(user: User) {}
    override suspend fun deleteAccount(email: String): Boolean = false
    override suspend fun getUserRole(email: String): UserRole = UserRole.USER
    override suspend fun addPoints(email: String, points: Int) {}
}

