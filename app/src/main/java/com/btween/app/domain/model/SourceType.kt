package com.btween.app.domain.model

/**
 * The original fixed set of source types, before they became admin-manageable via
 * `GET /source-types`. Kept as plain string constants (not an enum) since quotes now store
 * whatever name the admin configured, which the client can't enumerate ahead of time.
 * Used only as a fallback default and for resolving display labels of the well-known names -
 * see [com.btween.app.ui.util.sourceTypeLabel].
 */
object SourceType {
    const val MOVIE = "MOVIE"
    const val TV_SERIES = "TV_SERIES"
    const val BOOK = "BOOK"
    const val ANIME = "ANIME"
    const val GAME = "GAME"
    const val PODCAST = "PODCAST"
    const val SPEECH = "SPEECH"
    const val OTHER = "OTHER"

    val DEFAULT_OPTIONS = listOf(MOVIE, TV_SERIES, BOOK, ANIME, GAME, PODCAST, SPEECH, OTHER)
}
