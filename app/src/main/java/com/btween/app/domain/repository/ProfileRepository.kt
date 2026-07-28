package com.btween.app.domain.repository

import com.btween.app.domain.model.SocialQuote
import com.btween.app.domain.model.User

interface ProfileRepository {

    suspend fun getUser(id: Long): Result<User>

    suspend fun updateProfile(displayName: String?, avatarUrl: String?, bio: String?): Result<User>

    suspend fun follow(id: Long): Result<Unit>

    suspend fun unfollow(id: Long): Result<Unit>

    suspend fun getUserQuotes(id: Long, limit: Int = 20, offset: Long = 0): Result<List<SocialQuote>>

    suspend fun getTopContributors(limit: Int = 10): Result<List<TopContributor>>
}

data class TopContributor(val user: User, val quoteCount: Int)
