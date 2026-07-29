package com.btween.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.btween.app.R

data class BottomNavItem(
    val destination: Destination,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Destination.Home, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Destination.Feed, R.string.nav_feed, Icons.Filled.Public, Icons.Outlined.Public),
    BottomNavItem(Destination.Library, R.string.nav_library, Icons.Filled.Menu, Icons.Outlined.Menu),
    BottomNavItem(Destination.Favorites, R.string.nav_favorites, Icons.Filled.Favorite, Icons.Outlined.Favorite),
    BottomNavItem(Destination.Profile, R.string.nav_profile, Icons.Filled.Person, Icons.Outlined.Person)
)

@Composable
fun BTweenBottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val bottomNavViewModel: BottomNavViewModel = hiltViewModel()

    NavigationBar {
        bottomNavItems.forEach { item ->
            // Profile's route carries a {userId} path arg, so it never matches item.destination.route
            // by exact hierarchy comparison the way the other static-route tabs do - matched by
            // route prefix instead so viewing any profile still highlights this tab.
            val selected = if (item.destination == Destination.Profile) {
                currentDestination?.route?.startsWith("profile/") == true
            } else {
                currentDestination?.hierarchy?.any { it.route == item.destination.route } == true
            }
            val label = stringResource(item.labelRes)
            NavigationBarItem(
                selected = selected,
                onClick = {
                    val route = if (item.destination == Destination.Profile) {
                        val userId = bottomNavViewModel.getCurrentUserId() ?: return@NavigationBarItem
                        Destination.Profile.createRoute(userId)
                    } else {
                        item.destination.route
                    }
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = label
                    )
                },
                label = { Text(label) }
            )
        }
    }
}
