package com.btweeu.app.domain.repository

interface PublicSourceTypeRepository {
    suspend fun getSourceTypes(): Result<List<String>>
}
