package com.btweeu.app.data.repository

import com.btweeu.app.data.remote.api.SourceTypeApi
import com.btweeu.app.data.remote.safeApiCall
import com.btweeu.app.domain.repository.PublicSourceTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PublicSourceTypeRepositoryImpl @Inject constructor(
    private val sourceTypeApi: SourceTypeApi
) : PublicSourceTypeRepository {

    override suspend fun getSourceTypes(): Result<List<String>> = safeApiCall {
        sourceTypeApi.getSourceTypes().map { it.name }
    }
}
