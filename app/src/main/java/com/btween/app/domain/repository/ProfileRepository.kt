package com.btween.app.domain.repository

import com.btween.app.domain.model.SocialQuote
import com.btween.app.domain.model.User

interface ProfileRepository {

    suspend fun getUser(id: Long): Result<User>

    suspend fun updateProfile(displayName: String?, avatarUrl: String?, bio: String?): Result<User>

    suspend fun deleteAccount(password: String): Result<Unit>

    suspend fun follow(id: Long): Result<Unit>

    suspend fun unfollow(id: Long): Result<Unit>

    suspend fun getUserQuotes(id: Long, limit: Int = 20, offset: Long = 0): Result<List<SocialQuote>>

    suspend fun getTopContributors(limit: Int = 10): Result<List<TopContributor>>

    suspend fun searchUsers(query: String, limit: Int = 20): Result<List<User>>

    suspend fun getFollowers(userId: Long, limit: Int = 30, offset: Long = 0): Result<List<User>>

    suspend fun getFollowing(userId: Long, limit: Int = 30, offset: Long = 0): Result<List<User>>
}

data class TopContributor(val user: User, val quoteCount: Int)
