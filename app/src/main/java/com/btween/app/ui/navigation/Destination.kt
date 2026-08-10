package com.btween.app.ui.navigation

sealed class Destination(val route: String) {

    data object Home : Destination("home")
    data object Feed : Destination("feed")
    data object Library : Destination("library")
    data object Favorites : Destination("favorites")
    data object Search : Destination("search")
    data object Categories : Destination("categories")
    data object Settings : Destination("settings")
    data object Login : Destination("login")
    data object Register : Destination("register")
    data object ForgotPassword : Destination("forgot_password")

    data object AddEditQuote : Destination("add_edit_quote?quoteId={quoteId}") {
        const val ARG_QUOTE_ID = "quoteId"
        const val NEW_QUOTE_ID = -1L
        fun createRoute(quoteId: Long? = null) = "add_edit_quote?quoteId=${quoteId ?: NEW_QUOTE_ID}"
    }

    data object QuoteDetail : Destination("quote_detail/{quoteId}") {
        const val ARG_QUOTE_ID = "quoteId"
        fun createRoute(quoteId: Long) = "quote_detail/$quoteId"
    }

    data object Profile : Destination("profile/{userId}") {
        const val ARG_USER_ID = "userId"
        fun createRoute(userId: Long) = "profile/$userId"
    }

    data object EditProfile : Destination("edit_profile")

    data object ChangePassword : Destination("change_password")

    data object Notifications : Destination("notifications")

    data object FollowList : Destination("follow_list/{userId}/{type}") {
        const val ARG_USER_ID = "userId"
        const val ARG_TYPE = "type"
        fun createRoute(userId: Long, type: String) = "follow_list/$userId/$type"
    }

    data object EditSocialQuote : Destination("edit_social_quote/{quoteId}") {
        const val ARG_QUOTE_ID = "quoteId"
        fun createRoute(quoteId: Long) = "edit_social_quote/$quoteId"
    }

    data object SocialQuoteDetail : Destination("social_quote_detail/{quoteId}") {
        const val ARG_QUOTE_ID = "quoteId"
        fun createRoute(quoteId: Long) = "social_quote_detail/$quoteId"
    }

    data object CategoryQuotes : Destination("category_quotes/{category}") {
        const val ARG_CATEGORY = "category"
        fun createRoute(category: String) = "category_quotes/${java.net.URLEncoder.encode(category, "UTF-8")}"
    }

    data object TagQuotes : Destination("tag_quotes/{tag}") {
        const val ARG_TAG = "tag"
        fun createRoute(tag: String) = "tag_quotes/${java.net.URLEncoder.encode(tag, "UTF-8")}"
    }

    data object Comments : Destination("comments/{quoteId}") {
        const val ARG_QUOTE_ID = "quoteId"
        fun createRoute(quoteId: Long) = "comments/$quoteId"
    }

    data object Collections : Destination("collections")

    data object CollectionDetail : Destination("collection_detail/{collectionId}") {
        const val ARG_COLLECTION_ID = "collectionId"
        fun createRoute(collectionId: Long) = "collection_detail/$collectionId"
    }
}
