package com.msa.qiblapro.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.msa.qiblapro.R
import com.msa.qiblapro.ui.about.AboutScreen
import com.msa.qiblapro.ui.compass.CompassScreen
import com.msa.qiblapro.ui.compass.QiblaViewModel
import com.msa.qiblapro.ui.map.MapScreen
import com.msa.qiblapro.ui.onboarding.OnboardingScreen
import com.msa.qiblapro.ui.permissions.PermissionScreen
import com.msa.qiblapro.ui.permissions.isLocationPermissionGranted
import com.msa.qiblapro.ui.pro.ProBackground
import com.msa.qiblapro.ui.settings.SettingsRoute
import com.msa.qiblapro.ui.settings.SettingsViewModel

object Routes {
    const val ONBOARDING = "onboarding"
    const val PERMISSION = "permission"
    const val MAIN = "main"
    const val COMPASS = "compass"
    const val MAP = "map"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
}

@Composable
fun AppNavGraph(
    hasSeenOnboarding: Boolean,
    onOnboardingFinish: () -> Unit
) {
    val rootNav = rememberNavController()

    val startRoute = if (!hasSeenOnboarding) Routes.ONBOARDING else Routes.PERMISSION

    NavHost(
        navController = rootNav,
        startDestination = startRoute
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    onOnboardingFinish()
                    rootNav.navigate(Routes.PERMISSION) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
                onSkip = {
                    onOnboardingFinish()
                    rootNav.navigate(Routes.PERMISSION) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.PERMISSION) {
            PermissionScreen(
                onPermissionGranted = {
                    rootNav.navigate(Routes.MAIN) {
                        popUpTo(Routes.PERMISSION) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            val vm: QiblaViewModel = hiltViewModel()
            MainScaffold(
                vm = vm,
                onNavigateToPermission = {
                    rootNav.navigate(Routes.PERMISSION) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                },
                onNavigateToAbout = {
                    rootNav.navigate(Routes.ABOUT)
                }
            )
        }

        composable(Routes.ABOUT) {
            AboutScreen(onBack = { rootNav.popBackStack() })
        }
    }
}

@Composable
private fun MainScaffold(
    vm: QiblaViewModel,
    onNavigateToPermission: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val tabNav = rememberNavController()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // ✅ Permission یک‌کاسه: هر بار برگشت به اپ، وضعیت permission چک میشه
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = isLocationPermissionGranted(context)
                vm.setPermissionGranted(granted)
                if (!granted) onNavigateToPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    val items = listOf(
        Routes.COMPASS to Pair(Icons.Filled.Explore, R.string.tab_compass),
        Routes.MAP to Pair(Icons.Filled.Map, R.string.tab_map),
        Routes.SETTINGS to Pair(Icons.Filled.Settings, R.string.tab_settings),
    )

    ProBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(
                    containerColor = Color.Black.copy(alpha = 0.22f),
                    tonalElevation = 0.dp
                ) {
                    val current by tabNav.currentBackStackEntryAsState()
                    val currentRoute = current?.destination?.route

                    items.forEach { (route, iconAndTitle) ->
                        NavigationBarItem(
                            selected = currentRoute == route,
                            onClick = {
                                tabNav.navigate(route) {
                                    popUpTo(tabNav.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(iconAndTitle.first, contentDescription = null) },
                            label = { Text(stringResource(iconAndTitle.second)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                unselectedIconColor = Color.White.copy(alpha = 0.65f),
                                unselectedTextColor = Color.White.copy(alpha = 0.65f),
                                indicatorColor = Color.White.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = tabNav,
                startDestination = Routes.COMPASS,
                modifier = Modifier.padding(padding)
            ) {
                composable(Routes.COMPASS) {
                    CompassScreen(
                        vm = vm,
                        onNavigateToMap = {
                            tabNav.navigate(Routes.MAP) {
                                popUpTo(tabNav.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(Routes.MAP) { MapScreen(vm = vm) }
                composable(Routes.SETTINGS) {
                    SettingsRoute(onNavigateToAbout = onNavigateToAbout)
                }
            }
        }
    }
}
