package com.btween.app.domain.repository

interface PublicSourceTypeRepository {
    suspend fun getSourceTypes(): Result<List<String>>
}
