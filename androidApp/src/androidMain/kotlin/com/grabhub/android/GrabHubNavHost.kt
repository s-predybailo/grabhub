package com.grabhub.android

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.grabhub.android.ui.GrabHubTheme
import com.grabhub.android.ui.detail.DetailScreen
import com.grabhub.android.ui.favorites.FavoritesScreen
import com.grabhub.android.ui.history.HistoryScreen
import com.grabhub.android.ui.search.SearchScreen
import java.net.URLDecoder
import java.net.URLEncoder

private sealed class MainTab(val route: String, val label: String) {
    data object Search : MainTab("search/{prefill}", "Search") {
        fun destination(prefill: String = NONE): String = "search/$prefill"
        const val NONE = "_none_"
    }
    data object Favorites : MainTab("favorites", "Favorites")
    data object History : MainTab("history", "History")
}

@Composable
fun GrabHubNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val tabs = listOf(MainTab.Search, MainTab.Favorites, MainTab.History)
    val showBottomBar = currentRoute?.startsWith("search/") == true ||
        currentRoute == MainTab.Favorites.route ||
        currentRoute == MainTab.History.route

    GrabHubTheme {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        tabs.forEach { tab ->
                            NavigationBarItem(
                                selected = when (tab) {
                                    MainTab.Search -> currentRoute?.startsWith("search/") == true
                                    else -> currentRoute == tab.route
                                },
                                onClick = {
                                    val destination = when (tab) {
                                        MainTab.Search -> MainTab.Search.destination()
                                        else -> tab.route
                                    }
                                    navController.navigate(destination) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = when (tab) {
                                            MainTab.Search -> Icons.Default.Search
                                            MainTab.Favorites -> Icons.Default.Favorite
                                            MainTab.History -> Icons.Default.History
                                        },
                                        contentDescription = tab.label,
                                    )
                                },
                                label = { Text(tab.label) },
                            )
                        }
                    }
                }
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = MainTab.Search.destination(),
                modifier = Modifier.padding(padding),
            ) {
                composable(
                    route = MainTab.Search.route,
                    arguments = listOf(
                        navArgument("prefill") {
                            type = NavType.StringType
                            defaultValue = MainTab.Search.NONE
                        },
                    ),
                ) { entry ->
                    val rawPrefill = entry.arguments?.getString("prefill").orEmpty()
                    val prefill = if (rawPrefill == MainTab.Search.NONE) {
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
                composable(MainTab.Favorites.route) {
                    FavoritesScreen(
                        onModelClick = { modelId ->
                            navController.navigate(detailRoute(modelId))
                        },
                    )
                }
                composable(MainTab.History.route) {
                    HistoryScreen(
                        onQueryClick = { query ->
                            val encoded = URLEncoder.encode(query, Charsets.UTF_8.name())
                            navController.navigate(MainTab.Search.destination(encoded)) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(
                    route = "detail/{modelId}",
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
        }
    }
}

private fun detailRoute(modelId: String): String {
    val encoded = URLEncoder.encode(modelId, Charsets.UTF_8.name())
    return "detail/$encoded"
}
