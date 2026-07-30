package com.btween.app.domain.repository

enum class ReportTargetType { QUOTE, USER }

enum class ReportReason { SPAM, HARASSMENT, INAPPROPRIATE, MISINFORMATION, OTHER }

interface ReportRepository {
    suspend fun submitReport(
        targetType: ReportTargetType,
        targetId: Long,
        reason: ReportReason,
        details: String?
    ): Result<Unit>
}
