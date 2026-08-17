package com.btweeu.app.domain.repository

enum class ReportTargetType { QUOTE, USER, COMMENT }

enum class ReportReason { SPAM, HARASSMENT, INAPPROPRIATE, MISINFORMATION, OTHER }

interface ReportRepository {
    suspend fun submitReport(
        targetType: ReportTargetType,
        targetId: Long,
        reason: ReportReason,
        details: String?
    ): Result<Unit>
}
