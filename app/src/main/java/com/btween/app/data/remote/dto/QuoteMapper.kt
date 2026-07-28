package com.btween.app.data.remote.dto

import com.btween.app.domain.model.SocialQuote
import com.btween.app.domain.model.SourceType

fun QuoteResponseDto.toDomain(): SocialQuote = SocialQuote(
    id = id,
    text = text,
    sourceTitle = sourceTitle,
    sourceType = SourceType.fromName(sourceType),
    speaker = speaker,
    author = author,
    category = category,
    tags = tags,
    visibility = visibility,
    likeCount = likeCount,
    isLikedByMe = isLikedByMe,
    owner = owner.toDomain(),
    createdAt = createdAt
)
