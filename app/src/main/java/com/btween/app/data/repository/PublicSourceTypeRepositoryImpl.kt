package com.btween.app.data.repository

import com.btween.app.data.remote.api.SourceTypeApi
import com.btween.app.data.remote.safeApiCall
import com.btween.app.domain.repository.PublicSourceTypeRepository
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
