package com.uniquindio.reportes.di

import com.uniquindio.reportes.core.utils.ResourceProvider
import com.uniquindio.reportes.core.utils.ResourceProviderImpl
import com.uniquindio.reportes.data.repository.InMemoryAuthRepository
import com.uniquindio.reportes.data.repository.InMemoryCommentRepository
import com.uniquindio.reportes.data.repository.InMemoryNotificationRepository
import com.uniquindio.reportes.data.repository.InMemoryReportRepository
import com.uniquindio.reportes.data.repository.SessionRepositoryImpl
import com.uniquindio.reportes.domain.repository.AuthRepository
import com.uniquindio.reportes.domain.repository.CommentRepository
import com.uniquindio.reportes.domain.repository.NotificationRepository
import com.uniquindio.reportes.domain.repository.ReportRepository
import com.uniquindio.reportes.domain.repository.SessionRepository
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

