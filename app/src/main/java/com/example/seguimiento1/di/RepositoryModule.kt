package com.example.seguimiento1.di

import com.example.seguimiento1.core.utils.ResourceProvider
import com.example.seguimiento1.core.utils.ResourceProviderImpl
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
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    @Singleton
    abstract fun bindResourceProvider(impl: ResourceProviderImpl): ResourceProvider

    companion object {

        @Provides
        @Singleton
        fun provideAuthRepository(): AuthRepository {
            return InMemoryAuthRepository()
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
}

