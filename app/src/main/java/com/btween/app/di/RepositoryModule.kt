package com.btween.app.di

import com.btween.app.data.repository.AuthRepositoryImpl
import com.btween.app.data.repository.CategoryRepositoryImpl
import com.btween.app.data.repository.CollectionRepositoryImpl
import com.btween.app.data.repository.CommentRepositoryImpl
import com.btween.app.data.repository.NotificationRepositoryImpl
import com.btween.app.data.repository.ProfileRepositoryImpl
import com.btween.app.data.repository.QuoteRepositoryImpl
import com.btween.app.data.repository.PublicCategoryRepositoryImpl
import com.btween.app.data.repository.ReportRepositoryImpl
import com.btween.app.data.repository.SettingsRepositoryImpl
import com.btween.app.data.repository.SocialQuoteRepositoryImpl
import com.btween.app.domain.repository.AuthRepository
import com.btween.app.domain.repository.CategoryRepository
import com.btween.app.domain.repository.CollectionRepository
import com.btween.app.domain.repository.CommentRepository
import com.btween.app.domain.repository.NotificationRepository
import com.btween.app.domain.repository.ProfileRepository
import com.btween.app.domain.repository.QuoteRepository
import com.btween.app.domain.repository.PublicCategoryRepository
import com.btween.app.domain.repository.ReportRepository
import com.btween.app.domain.repository.SettingsRepository
import com.btween.app.domain.repository.SocialQuoteRepository
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
    abstract fun bindQuoteRepository(impl: QuoteRepositoryImpl): QuoteRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

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
}
