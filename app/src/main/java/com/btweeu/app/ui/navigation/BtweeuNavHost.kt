package com.btweeu.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navDeepLink
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.btweeu.app.ui.addedit.AddEditScreen
import com.btweeu.app.ui.auth.ForgotPasswordScreen
import com.btweeu.app.ui.auth.LoginScreen
import com.btweeu.app.ui.auth.RegisterScreen
import com.btweeu.app.ui.collections.CollectionDetailScreen
import com.btweeu.app.ui.collections.CollectionsScreen
import com.btweeu.app.ui.feed.CommentsScreen
import com.btweeu.app.ui.feed.FeedScreen
import com.btweeu.app.ui.feed.SocialQuoteDetailScreen
import com.btweeu.app.ui.home.CategoryQuotesScreen
import com.btweeu.app.ui.home.HomeScreen
import com.btweeu.app.ui.home.TagQuotesScreen
import com.btweeu.app.ui.library.LibraryScreen
import com.btweeu.app.ui.notifications.NotificationScreen
import com.btweeu.app.ui.profile.EditProfileScreen
import com.btweeu.app.ui.profile.EditSocialQuoteScreen
import com.btweeu.app.ui.profile.FollowListScreen
import com.btweeu.app.ui.profile.ProfileScreen
import com.btweeu.app.ui.search.SearchScreen
import com.btweeu.app.ui.settings.ChangePasswordScreen
import com.btweeu.app.ui.settings.SettingsScreen

@Composable
fun BtweeuNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Destination.Home.route) {

        composable(Destination.Home.route) {
            HomeScreen(
                onAddQuote = { navController.navigate(Destination.AddEditQuote.createRoute()) },
                onSearch = { navController.navigate(Destination.Search.route) },
                onUserClick = { userId -> navController.navigate(Destination.Profile.createRoute(userId)) },
                onNotificationsClick = { navController.navigate(Destination.Notifications.route) },
                onQuoteClick = { quoteId -> navController.navigate(Destination.SocialQuoteDetail.createRoute(quoteId)) },
                onCategoryClick = { category -> navController.navigate(Destination.CategoryQuotes.createRoute(category)) }
            )
        }

        // Reachable from anywhere while browsing as a guest (e.g. tapping the Profile tab,
        // or a "log in to like/comment/follow" prompt) - popping back on success returns to
        // wherever the person was, since isLoggedIn flipping doesn't remount this nav graph.
        composable(Destination.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.popBackStack() },
                onNavigateToRegister = { navController.navigate(Destination.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Destination.ForgotPassword.route) }
            )
        }
        composable(Destination.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.popBackStack() },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(Destination.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                onResetSuccess = { navController.popBackStack() }
            )
        }

        composable(Destination.Library.route) {
            LibraryScreen(
                onQuoteClick = { id -> navController.navigate(Destination.SocialQuoteDetail.createRoute(id)) }
            )
        }

        composable(Destination.Feed.route) {
            FeedScreen(
                onQuoteOwnerClick = { userId -> navController.navigate(Destination.Profile.createRoute(userId)) },
                onQuoteClick = { quoteId -> navController.navigate(Destination.SocialQuoteDetail.createRoute(quoteId)) },
                onNavigateToLogin = { navController.navigate(Destination.Login.route) }
            )
        }

        composable(
            route = Destination.SocialQuoteDetail.route,
            arguments = listOf(
                navArgument(Destination.SocialQuoteDetail.ARG_QUOTE_ID) { type = NavType.LongType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "btweeu://quote/{${Destination.SocialQuoteDetail.ARG_QUOTE_ID}}" }
            )
        ) {
            SocialQuoteDetailScreen(
                onBack = { navController.popBackStack() },
                onOwnerClick = { userId -> navController.navigate(Destination.Profile.createRoute(userId)) },
                onCommentsClick = { quoteId -> navController.navigate(Destination.Comments.createRoute(quoteId)) },
                onEditQuote = { quoteId -> navController.navigate(Destination.EditSocialQuote.createRoute(quoteId)) },
                onNavigateToLogin = { navController.navigate(Destination.Login.route) },
                onTagClick = { tag -> navController.navigate(Destination.TagQuotes.createRoute(tag)) }
            )
        }

        composable(
            route = Destination.CategoryQuotes.route,
            arguments = listOf(
                navArgument(Destination.CategoryQuotes.ARG_CATEGORY) { type = NavType.StringType }
            )
        ) {
            CategoryQuotesScreen(
                onBack = { navController.popBackStack() },
                onQuoteOwnerClick = { userId -> navController.navigate(Destination.Profile.createRoute(userId)) },
                onQuoteClick = { quoteId -> navController.navigate(Destination.SocialQuoteDetail.createRoute(quoteId)) }
            )
        }

        composable(
            route = Destination.TagQuotes.route,
            arguments = listOf(
                navArgument(Destination.TagQuotes.ARG_TAG) { type = NavType.StringType }
            )
        ) {
            TagQuotesScreen(
                onBack = { navController.popBackStack() },
                onQuoteOwnerClick = { userId -> navController.navigate(Destination.Profile.createRoute(userId)) },
                onQuoteClick = { quoteId -> navController.navigate(Destination.SocialQuoteDetail.createRoute(quoteId)) }
            )
        }

        composable(
            route = Destination.Comments.route,
            arguments = listOf(
                navArgument(Destination.Comments.ARG_QUOTE_ID) { type = NavType.LongType }
            )
        ) {
            CommentsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate(Destination.Login.route) }
            )
        }

        composable(Destination.Collections.route) {
            CollectionsScreen(
                onBack = { navController.popBackStack() },
                onCollectionClick = { id -> navController.navigate(Destination.CollectionDetail.createRoute(id)) }
            )
        }

        composable(
            route = Destination.CollectionDetail.route,
            arguments = listOf(
                navArgument(Destination.CollectionDetail.ARG_COLLECTION_ID) { type = NavType.LongType }
            )
        ) {
            CollectionDetailScreen(
                onBack = { navController.popBackStack() },
                onQuoteOwnerClick = { userId -> navController.navigate(Destination.Profile.createRoute(userId)) },
                onQuoteClick = { quoteId -> navController.navigate(Destination.SocialQuoteDetail.createRoute(quoteId)) }
            )
        }

        composable(
            route = Destination.Profile.route,
            arguments = listOf(
                navArgument(Destination.Profile.ARG_USER_ID) { type = NavType.LongType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "btweeu://profile/{${Destination.Profile.ARG_USER_ID}}" }
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
                onQuoteClick = { quoteId -> navController.navigate(Destination.SocialQuoteDetail.createRoute(quoteId)) },
                onCollectionsClick = { navController.navigate(Destination.Collections.route) },
                onCollectionDetailClick = { collectionId ->
                    navController.navigate(Destination.CollectionDetail.createRoute(collectionId))
                },
                onNavigateToLogin = { navController.navigate(Destination.Login.route) }
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

        composable(Destination.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onChangePasswordClick = { navController.navigate(Destination.ChangePassword.route) },
                onLoggedOut = {
                    navController.navigate(Destination.Home.route) {
                        popUpTo(Destination.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Destination.ChangePassword.route) {
            ChangePasswordScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Destination.Search.route) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onQuoteClick = { id -> navController.navigate(Destination.SocialQuoteDetail.createRoute(id)) },
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
                onDone = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate(Destination.Login.route) }
            )
        }
    }
}
