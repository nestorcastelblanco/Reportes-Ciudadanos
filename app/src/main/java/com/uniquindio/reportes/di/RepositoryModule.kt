package com.uniquindio.reportes.di

import com.uniquindio.reportes.core.utils.ResourceProvider
import com.uniquindio.reportes.core.utils.ResourceProviderImpl
import com.uniquindio.reportes.data.repository.FcmNotificationRepository
import com.uniquindio.reportes.data.repository.FirebaseAuthRepository
import com.uniquindio.reportes.data.repository.FirebaseImageStorageRepository
import com.uniquindio.reportes.data.repository.FirestoreCommentRepository
import com.uniquindio.reportes.data.repository.FirestoreReportRepository
import com.uniquindio.reportes.data.repository.GeminiCategoryClassifier
import com.uniquindio.reportes.data.repository.SessionRepositoryImpl
import com.uniquindio.reportes.domain.repository.AuthRepository
import com.uniquindio.reportes.domain.repository.CategoryClassifier
import com.uniquindio.reportes.domain.repository.CommentRepository
import com.uniquindio.reportes.domain.repository.ImageStorageRepository
import com.uniquindio.reportes.domain.repository.NotificationRepository
import com.uniquindio.reportes.domain.repository.ReportRepository
import com.uniquindio.reportes.domain.repository.SessionRepository
import dagger.Binds
import dagger.Module
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

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(impl: FirestoreReportRepository): ReportRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(impl: FirestoreCommentRepository): CommentRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: FcmNotificationRepository): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindImageStorageRepository(
        impl: FirebaseImageStorageRepository
    ): ImageStorageRepository

    @Binds
    @Singleton
    abstract fun bindCategoryClassifier(impl: GeminiCategoryClassifier): CategoryClassifier
}
