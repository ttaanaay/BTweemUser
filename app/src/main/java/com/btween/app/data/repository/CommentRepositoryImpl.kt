package com.btween.app.data.repository

import com.btween.app.data.remote.api.QuoteApi
import com.btween.app.data.remote.dto.CommentRequestDto
import com.btween.app.data.remote.dto.toDomain
import com.btween.app.data.remote.safeApiCall
import com.btween.app.domain.model.Comment
import com.btween.app.domain.repository.CommentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val quoteApi: QuoteApi
) : CommentRepository {

    override suspend fun getComments(quoteId: Long, limit: Int, offset: Long): Result<List<Comment>> = safeApiCall {
        quoteApi.getComments(quoteId, limit, offset).map {
            Comment(id = it.id, quoteId = it.quoteId, text = it.text, author = it.author.toDomain(), createdAt = it.createdAt)
        }
    }

    override suspend fun addComment(quoteId: Long, text: String): Result<Comment> = safeApiCall {
        val response = quoteApi.addComment(quoteId, CommentRequestDto(text))
        Comment(
            id = response.id,
            quoteId = response.quoteId,
            text = response.text,
            author = response.author.toDomain(),
            createdAt = response.createdAt
        )
    }

    override suspend fun deleteComment(id: Long): Result<Unit> = safeApiCall {
        quoteApi.deleteComment(id)
    }
}
