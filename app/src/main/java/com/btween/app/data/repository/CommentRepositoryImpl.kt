package com.btween.app.data.repository

import com.btween.app.data.remote.api.QuoteApi
import com.btween.app.data.remote.dto.CommentRequestDto
import com.btween.app.data.remote.dto.CommentResponseDto
import com.btween.app.data.remote.dto.toDomain
import com.btween.app.data.remote.safeApiCall
import com.btween.app.domain.model.Comment
import com.btween.app.domain.repository.CommentRepository
import javax.inject.Inject
import javax.inject.Singleton

private fun CommentResponseDto.toDomain(): Comment = Comment(
    id = id,
    quoteId = quoteId,
    text = text,
    author = author.toDomain(),
    isEdited = isEdited,
    createdAt = createdAt
)

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val quoteApi: QuoteApi
) : CommentRepository {

    override suspend fun getComments(quoteId: Long, limit: Int, offset: Long): Result<List<Comment>> = safeApiCall {
        quoteApi.getComments(quoteId, limit, offset).map { it.toDomain() }
    }

    override suspend fun addComment(quoteId: Long, text: String): Result<Comment> = safeApiCall {
        quoteApi.addComment(quoteId, CommentRequestDto(text)).toDomain()
    }

    override suspend fun editComment(id: Long, text: String): Result<Comment> = safeApiCall {
        quoteApi.editComment(id, CommentRequestDto(text)).toDomain()
    }

    override suspend fun deleteComment(id: Long): Result<Unit> = safeApiCall {
        quoteApi.deleteComment(id)
    }
}
