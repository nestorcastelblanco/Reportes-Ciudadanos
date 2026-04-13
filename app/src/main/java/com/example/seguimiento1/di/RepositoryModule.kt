package com.example.seguimiento1.di

import android.content.Context
import com.example.seguimiento1.data.datastore.SessionDataStore
import com.example.seguimiento1.data.repository.InMemoryAuthRepository
import com.example.seguimiento1.data.repository.InMemoryCommentRepository
import com.example.seguimiento1.data.repository.InMemoryNotificationRepository
import com.example.seguimiento1.data.repository.InMemoryReportRepository
import com.example.seguimiento1.data.repository.SessionRepositoryImpl
import com.example.seguimiento1.domain.repository.AuthRepository
import com.example.seguimiento1.domain.repository.CommentRepository
import com.example.seguimiento1.domain.repository.NotificationRepository
import com.example.seguimiento1.domain.repository.ReportRepository
import com.example.seguimiento1.domain.repository.SessionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return InMemoryAuthRepository()
    }

    @Provides
    @Singleton
    fun provideSessionRepository(@ApplicationContext context: Context): SessionRepository {
        return SessionRepositoryImpl(
            sessionDataStore = SessionDataStore(context)
        )
    }

    @Provides
    @Singleton
    fun provideReportRepository(): ReportRepository {
        return InMemoryReportRepository()
    }

    @Provides
    @Singleton
    fun provideCommentRepository(): CommentRepository {
        return InMemoryCommentRepository()
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(): NotificationRepository {
        return InMemoryNotificationRepository()
    }
}

