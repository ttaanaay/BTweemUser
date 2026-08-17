package com.btweeu.app.domain.repository

import com.btweeu.app.domain.model.Comment

interface CommentRepository {

    suspend fun getComments(quoteId: Long, limit: Int = 50, offset: Long = 0): Result<List<Comment>>

    suspend fun addComment(quoteId: Long, text: String): Result<Comment>

    suspend fun editComment(id: Long, text: String): Result<Comment>

    suspend fun deleteComment(id: Long): Result<Unit>
}
