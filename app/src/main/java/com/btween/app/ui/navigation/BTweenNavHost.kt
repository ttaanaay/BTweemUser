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
import com.btween.app.ui.home.HomeScreen
import com.btween.app.ui.library.LibraryScreen
import com.btween.app.ui.profile.EditProfileScreen
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
                onUserClick = { userId -> navController.navigate(Destination.Profile.createRoute(userId)) }
            )
        }

        composable(Destination.Library.route) {
            LibraryScreen(
                onQuoteClick = { id -> navController.navigate(Destination.QuoteDetail.createRoute(id)) }
            )
        }

        composable(Destination.Feed.route) {
            FeedScreen(
                onQuoteOwnerClick = { userId -> navController.navigate(Destination.Profile.createRoute(userId)) }
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
                onEditProfile = { navController.navigate(Destination.EditProfile.route) }
            )
        }

        composable(Destination.EditProfile.route) {
            EditProfileScreen(
                onDone = { navController.popBackStack() }
            )
        }

        composable(Destination.Favorites.route) {
            FavoritesScreen(
                onQuoteClick = { id -> navController.navigate(Destination.QuoteDetail.createRoute(id)) }
            )
        }

        composable(Destination.Settings.route) {
            SettingsScreen(
                onNavigateToProfile = { userId -> navController.navigate(Destination.Profile.createRoute(userId)) }
            )
        }

        composable(Destination.Search.route) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onQuoteClick = { id -> navController.navigate(Destination.QuoteDetail.createRoute(id)) }
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
