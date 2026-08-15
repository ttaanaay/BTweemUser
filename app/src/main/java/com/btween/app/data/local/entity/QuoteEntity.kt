package com.btween.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quotes",
    indices = [Index("isFavorite"), Index("sourceType")]
)
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val text: String,
    val sourceTitle: String,
    val sourceType: String,
    val speaker: String,
    val author: String?,
    val category: String?,
    val tags: List<String>,
    val note: String?,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastViewedAt: Long? = null
)
