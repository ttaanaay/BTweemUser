package com.btween.app.di

import android.content.Context
import androidx.room.Room
import com.btween.app.data.local.dao.QuoteDao
import com.btween.app.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            // The local category system (Room-backed, custom colors) was removed in favor of
            // a single admin-managed category shared with the server - there's no sensible
            // automatic mapping from old local categories to the new server ones, so this
            // schema change just resets local-only quotes rather than risk a bad migration.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideQuoteDao(database: AppDatabase): QuoteDao = database.quoteDao()
}
