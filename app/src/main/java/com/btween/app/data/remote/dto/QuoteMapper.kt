package com.btween.app.data.remote.dto

import com.btween.app.domain.model.SocialQuote

fun QuoteResponseDto.toDomain(): SocialQuote = SocialQuote(
    id = id,
    text = text,
    sourceTitle = sourceTitle,
    sourceType = sourceType,
    speaker = speaker,
    author = author,
    category = category,
    tags = tags,
    imageUrl = imageUrl,
    visibility = visibility,
    likeCount = likeCount,
    commentCount = commentCount,
    isLikedByMe = isLikedByMe,
    owner = owner.toDomain(),
    createdAt = createdAt
)
