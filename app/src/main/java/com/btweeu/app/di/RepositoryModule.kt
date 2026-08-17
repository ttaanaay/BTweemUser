package com.btweeu.app.di

import com.btweeu.app.data.repository.AuthRepositoryImpl
import com.btweeu.app.data.repository.CollectionRepositoryImpl
import com.btweeu.app.data.repository.CommentRepositoryImpl
import com.btweeu.app.data.repository.NotificationRepositoryImpl
import com.btweeu.app.data.repository.ProfileRepositoryImpl
import com.btweeu.app.data.repository.PublicCategoryRepositoryImpl
import com.btweeu.app.data.repository.MaintenanceRepositoryImpl
import com.btweeu.app.data.repository.PublicSourceTypeRepositoryImpl
import com.btweeu.app.data.repository.ReportRepositoryImpl
import com.btweeu.app.data.repository.SettingsRepositoryImpl
import com.btweeu.app.data.repository.SocialQuoteRepositoryImpl
import com.btweeu.app.domain.repository.AuthRepository
import com.btweeu.app.domain.repository.CollectionRepository
import com.btweeu.app.domain.repository.CommentRepository
import com.btweeu.app.domain.repository.NotificationRepository
import com.btweeu.app.domain.repository.ProfileRepository
import com.btweeu.app.domain.repository.PublicCategoryRepository
import com.btweeu.app.domain.repository.MaintenanceRepository
import com.btweeu.app.domain.repository.PublicSourceTypeRepository
import com.btweeu.app.domain.repository.ReportRepository
import com.btweeu.app.domain.repository.SettingsRepository
import com.btweeu.app.domain.repository.SocialQuoteRepository
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
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSocialQuoteRepository(impl: SocialQuoteRepositoryImpl): SocialQuoteRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(impl: CommentRepositoryImpl): CommentRepository

    @Binds
    @Singleton
    abstract fun bindCollectionRepository(impl: CollectionRepositoryImpl): CollectionRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository

    @Binds
    @Singleton
    abstract fun bindPublicCategoryRepository(impl: PublicCategoryRepositoryImpl): PublicCategoryRepository

    @Binds
    @Singleton
    abstract fun bindPublicSourceTypeRepository(impl: PublicSourceTypeRepositoryImpl): PublicSourceTypeRepository

    @Binds
    @Singleton
    abstract fun bindMaintenanceRepository(impl: MaintenanceRepositoryImpl): MaintenanceRepository
}
