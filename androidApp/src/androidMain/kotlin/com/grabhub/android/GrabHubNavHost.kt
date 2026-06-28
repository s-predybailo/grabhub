package com.grabhub.android

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.grabhub.android.ui.GrabHubTheme
import com.grabhub.android.ui.components.GrabHubFloatingNavBar
import com.grabhub.android.ui.components.BottomNavItem
import com.grabhub.android.ui.detail.DetailScreen
import com.grabhub.android.ui.favorites.FavoritesScreen
import com.grabhub.android.ui.history.HistoryScreen
import com.grabhub.android.ui.search.SearchScreen
import com.grabhub.android.ui.settings.SettingsScreen
import java.net.URLDecoder
import java.net.URLEncoder

private val BottomNavClearance = 96.dp

private object Routes {
    const val SEARCH = "search/{prefill}"
    const val FAVORITES = "favorites"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val DETAIL = "detail/{modelId}"
    const val NONE = "_none_"

    fun searchDestination(prefill: String = NONE): String = "search/$prefill"
}

@Composable
fun GrabHubNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute?.startsWith("search/") == true ||
        currentRoute == Routes.FAVORITES ||
        currentRoute == Routes.HISTORY ||
        currentRoute == Routes.SETTINGS

    val selectedRouteKey = when {
        currentRoute?.startsWith("search/") == true -> "search"
        currentRoute == Routes.FAVORITES -> "favorites"
        currentRoute == Routes.HISTORY -> "history"
        currentRoute == Routes.SETTINGS -> "settings"
        else -> "search"
    }

    GrabHubTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = Routes.searchDestination(),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (showBottomBar) BottomNavClearance else 0.dp),
                ) {
                    composable(
                        route = Routes.SEARCH,
                        arguments = listOf(
                            navArgument("prefill") {
                                type = NavType.StringType
                                defaultValue = Routes.NONE
                            },
                        ),
                    ) { entry ->
                        val rawPrefill = entry.arguments?.getString("prefill").orEmpty()
                        val prefill = if (rawPrefill == Routes.NONE) {
                            null
                        } else {
                            URLDecoder.decode(rawPrefill, Charsets.UTF_8.name())
                        }
                        SearchScreen(
                            prefilledQuery = prefill,
                            onModelClick = { modelId ->
                                navController.navigate(detailRoute(modelId))
                            },
                        )
                    }
                    composable(Routes.FAVORITES) {
                        FavoritesScreen(
                            onModelClick = { modelId ->
                                navController.navigate(detailRoute(modelId))
                            },
                        )
                    }
                    composable(Routes.HISTORY) {
                        HistoryScreen(
                            onQueryClick = { query ->
                                val encoded = URLEncoder.encode(query, Charsets.UTF_8.name())
                                navController.navigate(Routes.searchDestination(encoded)) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                    composable(Routes.SETTINGS) {
                        SettingsScreen()
                    }
                    composable(
                        route = Routes.DETAIL,
                        arguments = listOf(
                            navArgument("modelId") { type = NavType.StringType },
                        ),
                    ) { backStackEntry ->
                        val encodedId = backStackEntry.arguments?.getString("modelId").orEmpty()
                        val modelId = URLDecoder.decode(encodedId, Charsets.UTF_8.name())
                        DetailScreen(
                            modelId = modelId,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }

                if (showBottomBar) {
                    GrabHubFloatingNavBar(
                        selectedRouteKey = selectedRouteKey,
                        onItemSelected = { item -> navigateToTab(navController, item) },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
    }
}

private fun navigateToTab(navController: androidx.navigation.NavHostController, item: BottomNavItem) {
    val destination = when (item.routeKey) {
        "search" -> Routes.searchDestination()
        "favorites" -> Routes.FAVORITES
        "history" -> Routes.HISTORY
        "settings" -> Routes.SETTINGS
        else -> Routes.searchDestination()
    }
    navController.navigate(destination) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun detailRoute(modelId: String): String {
    val encoded = URLEncoder.encode(modelId, Charsets.UTF_8.name())
    return "detail/$encoded"
}
