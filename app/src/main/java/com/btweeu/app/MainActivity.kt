package com.btweeu.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.btweeu.app.ui.MainViewModel
import com.btweeu.app.ui.MaintenanceScreen
import com.btweeu.app.ui.navigation.BtweeuBottomNavBar
import com.btweeu.app.ui.navigation.BtweeuNavHost
import com.btweeu.app.ui.navigation.bottomNavItems
import com.btweeu.app.ui.onboarding.OnboardingScreen
import com.btweeu.app.ui.resolveIsDark
import com.btweeu.app.ui.theme.BtweeuTheme
import com.btweeu.app.util.LocaleManager
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host for the entire app. Below the Hilt-provided theme, the app is gated
 * on [MainViewModel.isLoggedIn]: signed-out users see the Login/Register flow ([AuthGate]);
 * once signed in, the main app ([BtweeuNavHost] + bottom nav) takes over. The bottom
 * navigation bar itself is shown only on the four top-level destinations (Home, Library,
 * Favorites, Settings) and hidden on Detail/Add-Edit/Search so those feel like focused,
 * full-screen flows.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // AppCompatActivity wires this automatically via its own attachBaseContext override;
    // plain ComponentActivity does not, so setApplicationLocales() + recreate() alone was a
    // no-op - the recreated Activity kept reading resources in the OLD locale. Applying the
    // chosen locale to the base Context by hand here is what actually makes the switch stick.
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val userSettings by mainViewModel.userSettings.collectAsStateWithLifecycle()
            val systemInDarkTheme = isSystemInDarkTheme()

            BtweeuTheme(
                darkTheme = userSettings.themeMode.resolveIsDark(systemInDarkTheme),
                dynamicColor = userSettings.useDynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val maintenanceStatus by mainViewModel.maintenanceStatus.collectAsStateWithLifecycle()
                    val shouldShowOnboarding by mainViewModel.shouldShowOnboarding.collectAsStateWithLifecycle()
                    when {
                        // Still checking (null) - render nothing rather than flash the app
                        // then immediately replace it with the maintenance screen.
                        maintenanceStatus == null -> Unit
                        maintenanceStatus?.enabled == true -> MaintenanceScreen(maintenanceStatus?.message)
                        shouldShowOnboarding -> OnboardingScreen(onFinished = mainViewModel::onOnboardingCompleted)
                        else -> MainAppContent()
                    }
                }
            }
        }
    }
}

@Composable
private fun MainAppContent() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    // Android 13+ requires this to be requested at runtime - just declaring it in the
    // manifest doesn't make the system show a prompt or allow notifications to display.
    // Without this, push notifications get silently dropped with no visible sign why.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val context = LocalContext.current
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { /* no-op either way - if denied, push notifications just won't show */ }

        LaunchedEffect(Unit) {
            val alreadyGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!alreadyGranted) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val showBottomBar = bottomNavItems.any { item ->
        currentRoute?.hierarchy?.any { it.route == item.destination.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) BtweeuBottomNavBar(navController)
        }
    ) { padding ->
        Surface(modifier = Modifier.padding(padding)) {
            BtweeuNavHost(navController = navController)
        }
    }
}
