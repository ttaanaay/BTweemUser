package com.btween.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.btween.app.data.local.converter.TagsConverter
import com.btween.app.data.local.dao.QuoteDao
import com.btween.app.data.local.entity.QuoteEntity

@Database(
    entities = [QuoteEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(TagsConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun quoteDao(): QuoteDao

    companion object {
        const val DATABASE_NAME = "btween_database"
    }
}
