package com.msa.qiblapro.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.msa.qiblapro.ui.screens.*
import com.msa.qiblapro.ui.viewmodels.QiblaViewModel
import com.msa.qiblapro.ui.viewmodels.SettingsViewModel

private object Routes {
    const val PERMISSION = "permission"
    const val MAIN = "main"
    const val COMPASS = "compass"
    const val MAP = "map"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavGraph() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.PERMISSION) {
        composable(Routes.PERMISSION) {
            val vm: QiblaViewModel = hiltViewModel()
            PermissionScreen(
                onPermissionGranted = {
                    vm.setPermissionGranted(true)
                    nav.navigate(Routes.MAIN) {
                        popUpTo(Routes.PERMISSION) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScaffold()
        }
    }
}

@Composable
private fun MainScaffold() {
    val nav = rememberNavController()

    val items = listOf(
        Routes.COMPASS to Pair(Icons.Filled.Explore, "Compass"),
        Routes.MAP to Pair(Icons.Filled.Map, "Map"),
        Routes.SETTINGS to Pair(Icons.Filled.Settings, "Settings"),
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val current by nav.currentBackStackEntryAsState()
                val currentRoute = current?.destination?.route
                items.forEach { (route, iconTitle) ->
                    NavigationBarItem(
                        selected = currentRoute == route,
                        onClick = {
                            nav.navigate(route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(iconTitle.first, null) },
                        label = { Text(iconTitle.second) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.COMPASS,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.COMPASS) { CompassScreen() }
            composable(Routes.MAP) { MapScreen() }
            composable(Routes.SETTINGS) { SettingsRoute() }
        }
    }
}
