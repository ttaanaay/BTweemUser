package com.btweeu.app.data.remote.dto

import com.btweeu.app.domain.model.User

fun UserResponseDto.toDomain(): User = User(
    id = id,
    username = username,
    displayName = displayName,
    avatarUrl = avatarUrl,
    bio = bio,
    followerCount = followerCount,
    followingCount = followingCount,
    isFollowedByMe = isFollowedByMe,
    emailVerified = emailVerified,
    authProvider = authProvider
)
