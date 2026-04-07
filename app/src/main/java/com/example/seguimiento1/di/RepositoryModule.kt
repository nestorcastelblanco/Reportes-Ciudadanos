package com.example.seguimiento1.di

import android.content.Context
import com.example.seguimiento1.data.datastore.SessionDataStore
import com.example.seguimiento1.data.datastore.UsersDataStore
import com.example.seguimiento1.data.repository.DataStoreAuthRepository
import com.example.seguimiento1.data.repository.SessionRepositoryImpl
import com.example.seguimiento1.domain.repository.AuthRepository
import com.example.seguimiento1.domain.repository.SessionRepository

object RepositoryModule {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val authRepository: AuthRepository by lazy {
        check(::appContext.isInitialized) {
            "RepositoryModule must be initialized before requesting authRepository"
        }

        DataStoreAuthRepository(
            usersDataStore = UsersDataStore(appContext)
        )
    }

    @Volatile
    private var sessionRepository: SessionRepository? = null

    fun provideSessionRepository(context: Context): SessionRepository {
        return sessionRepository ?: synchronized(this) {
            sessionRepository ?: SessionRepositoryImpl(
                sessionDataStore = SessionDataStore(context)
            ).also { sessionRepository = it }
        }
    }
}

