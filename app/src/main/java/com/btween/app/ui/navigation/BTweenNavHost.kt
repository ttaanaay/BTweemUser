package com.btween.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.btween.app.ui.addedit.AddEditScreen
import com.btween.app.ui.detail.DetailScreen
import com.btween.app.ui.favorites.FavoritesScreen
import com.btween.app.ui.feed.FeedScreen
import com.btween.app.ui.feed.SocialQuoteDetailScreen
import com.btween.app.ui.home.HomeScreen
import com.btween.app.ui.library.LibraryScreen
import com.btween.app.ui.notifications.NotificationScreen
import com.btween.app.ui.profile.EditProfileScreen
import com.btween.app.ui.profile.EditSocialQuoteScreen
import com.btween.app.ui.profile.FollowListScreen
import com.btween.app.ui.profile.ProfileScreen
import com.btween.app.ui.search.SearchScreen
import com.btween.app.ui.settings.SettingsScreen

@Composable
fun BTweenNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Destination.Home.route) {

        composable(Destination.Home.route) {
            HomeScreen(
                onAddQuote = { navController.navigate(Destination.AddEditQuote.createRoute()) },
                onSearch = { navController.navigate(Destination.Search.route) },
                onUserClick = { userId -> navController.navigate(Destination.Profile.createRoute(userId)) },
                onNotificationsClick = { navController.navigate(Destination.Notifications.route) }
            )
        }

        composable(Destination.Library.route) {
            LibraryScreen(
                onQuoteClick = { id -> navController.navigate(Destination.QuoteDetail.createRoute(id)) }
            )
        }

        composable(Destination.Feed.route) {
            FeedScreen(
                onQuoteOwnerClick = { userId -> navController.navigate(Destination.Profile.createRoute(userId)) },
                onQuoteClick = { quoteId -> navController.navigate(Destination.SocialQuoteDetail.createRoute(quoteId)) }
            )
        }

        composable(
            route = Destination.SocialQuoteDetail.route,
            arguments = listOf(
                navArgument(Destination.SocialQuoteDetail.ARG_QUOTE_ID) { type = NavType.LongType }
            )
        ) {
            SocialQuoteDetailScreen(
                onBack = { navController.popBackStack() },
                onOwnerClick = { userId -> navController.navigate(Destination.Profile.createRoute(userId)) }
            )
        }

        composable(
            route = Destination.Profile.route,
            arguments = listOf(
                navArgument(Destination.Profile.ARG_USER_ID) { type = NavType.LongType }
            )
        ) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onEditProfile = { navController.navigate(Destination.EditProfile.route) },
                onSettingsClick = { navController.navigate(Destination.Settings.route) },
                onFollowListClick = { userId, type ->
                    navController.navigate(Destination.FollowList.createRoute(userId, type.name))
                },
                onEditQuote = { quoteId -> navController.navigate(Destination.EditSocialQuote.createRoute(quoteId)) },
                onQuoteClick = { quoteId -> navController.navigate(Destination.SocialQuoteDetail.createRoute(quoteId)) }
            )
        }

        composable(
            route = Destination.EditSocialQuote.route,
            arguments = listOf(
                navArgument(Destination.EditSocialQuote.ARG_QUOTE_ID) { type = NavType.LongType }
            )
        ) {
            EditSocialQuoteScreen(
                onDone = { navController.popBackStack() }
            )
        }

        composable(
            route = Destination.FollowList.route,
            arguments = listOf(
                navArgument(Destination.FollowList.ARG_USER_ID) { type = NavType.LongType },
                navArgument(Destination.FollowList.ARG_TYPE) { type = NavType.StringType }
            )
        ) {
            FollowListScreen(
                onBack = { navController.popBackStack() },
                onUserClick = { userId -> navController.navigate(Destination.Profile.createRoute(userId)) }
            )
        }

        composable(Destination.EditProfile.route) {
            EditProfileScreen(
                onDone = { navController.popBackStack() }
            )
        }

        composable(Destination.Notifications.route) {
            NotificationScreen(
                onBack = { navController.popBackStack() },
                onUserClick = { userId -> navController.navigate(Destination.Profile.createRoute(userId)) }
            )
        }

        composable(Destination.Favorites.route) {
            FavoritesScreen(
                onQuoteClick = { id -> navController.navigate(Destination.QuoteDetail.createRoute(id)) }
            )
        }

        composable(Destination.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Destination.Search.route) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onQuoteClick = { id -> navController.navigate(Destination.QuoteDetail.createRoute(id)) },
                onUserClick = { userId -> navController.navigate(Destination.Profile.createRoute(userId)) }
            )
        }

        composable(
            route = Destination.AddEditQuote.route,
            arguments = listOf(
                navArgument(Destination.AddEditQuote.ARG_QUOTE_ID) {
                    type = NavType.LongType
                    defaultValue = Destination.AddEditQuote.NEW_QUOTE_ID
                }
            )
        ) {
            AddEditScreen(
                onDone = { navController.popBackStack() }
            )
        }

        composable(
            route = Destination.QuoteDetail.route,
            arguments = listOf(
                navArgument(Destination.QuoteDetail.ARG_QUOTE_ID) { type = NavType.LongType }
            )
        ) {
            DetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { id ->
                    navController.navigate(Destination.AddEditQuote.createRoute(id))
                }
            )
        }
    }
}
