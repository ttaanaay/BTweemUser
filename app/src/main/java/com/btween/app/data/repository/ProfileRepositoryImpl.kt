package com.btween.app.data.repository

import com.btween.app.data.remote.api.UserApi
import com.btween.app.data.remote.dto.DeleteAccountRequestDto
import com.btween.app.data.remote.dto.UpdateProfileRequestDto
import com.btween.app.data.remote.dto.toDomain
import com.btween.app.data.remote.safeApiCall
import com.btween.app.domain.model.SocialQuote
import com.btween.app.domain.model.User
import com.btween.app.domain.repository.ProfileRepository
import com.btween.app.domain.repository.TopContributor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val userApi: UserApi
) : ProfileRepository {

    override suspend fun getUser(id: Long): Result<User> = safeApiCall {
        userApi.getUser(id).toDomain()
    }

    override suspend fun updateProfile(displayName: String?, avatarUrl: String?, bio: String?): Result<User> =
        safeApiCall {
            userApi.updateProfile(UpdateProfileRequestDto(displayName, avatarUrl, bio)).toDomain()
        }

    override suspend fun deleteAccount(password: String): Result<Unit> = safeApiCall {
        userApi.deleteAccount(DeleteAccountRequestDto(password))
    }

    override suspend fun follow(id: Long): Result<Unit> = safeApiCall {
        userApi.follow(id)
    }

    override suspend fun unfollow(id: Long): Result<Unit> = safeApiCall {
        userApi.unfollow(id)
    }

    override suspend fun getUserQuotes(id: Long, limit: Int, offset: Long): Result<List<SocialQuote>> =
        safeApiCall {
            userApi.getUserQuotes(id, limit, offset).map { it.toDomain() }
        }

    override suspend fun getTopContributors(limit: Int): Result<List<TopContributor>> = safeApiCall {
        userApi.getTopContributors(limit).map { TopContributor(it.user.toDomain(), it.quoteCount) }
    }

    override suspend fun searchUsers(query: String, limit: Int): Result<List<User>> = safeApiCall {
        userApi.searchUsers(query, limit).map { it.toDomain() }
    }

    override suspend fun getFollowers(userId: Long, limit: Int, offset: Long): Result<List<User>> = safeApiCall {
        userApi.getFollowers(userId, limit, offset).map { it.toDomain() }
    }

    override suspend fun getFollowing(userId: Long, limit: Int, offset: Long): Result<List<User>> = safeApiCall {
        userApi.getFollowing(userId, limit, offset).map { it.toDomain() }
    }
}
