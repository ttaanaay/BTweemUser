package com.btweeu.app.data.repository

import com.btweeu.app.data.remote.api.ReportApi
import com.btweeu.app.data.remote.dto.ReportRequestDto
import com.btweeu.app.data.remote.safeApiCall
import com.btweeu.app.domain.repository.ReportReason
import com.btweeu.app.domain.repository.ReportRepository
import com.btweeu.app.domain.repository.ReportTargetType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val reportApi: ReportApi
) : ReportRepository {

    override suspend fun submitReport(
        targetType: ReportTargetType,
        targetId: Long,
        reason: ReportReason,
        details: String?
    ): Result<Unit> = safeApiCall {
        reportApi.submitReport(ReportRequestDto(targetType.name, targetId, reason.name, details))
    }
}
