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
import com.grabhub.android.ui.components.BottomNavItem
import com.grabhub.android.ui.components.GrabHubFloatingNavBar
import com.grabhub.android.ui.detail.DetailScreen
import com.grabhub.android.ui.favorites.FavoritesScreen
import com.grabhub.android.ui.history.HistoryScreen
import com.grabhub.android.ui.feed.FeedScreen
import com.grabhub.android.ui.home.HomeScreen
import com.grabhub.android.ui.search.SearchScreen
import com.grabhub.android.ui.settings.SettingsScreen
import com.grabhub.domain.FeedType
import com.grabhub.domain.SortOrder
import java.net.URLDecoder
import java.net.URLEncoder

private val BottomNavClearance = 112.dp

private object Routes {
    const val HOME = "home"
    const val SEARCH = "search/{prefill}/{sortOrder}"
    const val FAVORITES = "favorites"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val FEED = "feed/{feedType}"
    const val DETAIL = "detail/{modelId}"
    const val NONE = "_none_"
    const val SORT_DEFAULT = "_default_"

    fun feedDestination(feedType: FeedType): String = "feed/${feedType.name.lowercase()}"

    fun searchDestination(prefill: String = NONE, sortOrder: String = SORT_DEFAULT): String =
        "search/$prefill/$sortOrder"
}

@Composable
fun GrabHubNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute == Routes.HOME ||
        currentRoute?.startsWith("feed/") == true ||
        currentRoute?.startsWith("search/") == true ||
        currentRoute == Routes.FAVORITES ||
        currentRoute == Routes.HISTORY ||
        currentRoute == Routes.SETTINGS

    val selectedRouteKey = when {
        currentRoute == Routes.HOME -> "home"
        currentRoute?.startsWith("search/") == true -> "search"
        currentRoute == Routes.FAVORITES -> "favorites"
        currentRoute == Routes.HISTORY -> "history"
        currentRoute == Routes.SETTINGS -> "settings"
        else -> "home"
    }

    val isSearchActive = currentRoute?.startsWith("search/") == true

    GrabHubTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = Routes.HOME,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (showBottomBar) BottomNavClearance else 0.dp),
                ) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            onModelClick = { modelId ->
                                navController.navigate(detailRoute(modelId))
                            },
                            onOpenFeed = { feedType ->
                                navController.navigate(Routes.feedDestination(feedType))
                            },
                            onOpenHistory = {
                                navigateToTab(navController, GrabHubSideNavItem("history"))
                            },
                        )
                    }
                    composable(
                        route = Routes.FEED,
                        arguments = listOf(
                            navArgument("feedType") { type = NavType.StringType },
                        ),
                    ) { entry ->
                        val feedType = parseFeedType(entry.arguments?.getString("feedType"))
                        FeedScreen(
                            feedType = feedType,
                            onBack = { navController.popBackStack() },
                            onModelClick = { modelId ->
                                navController.navigate(detailRoute(modelId))
                            },
                        )
                    }
                    composable(
                        route = Routes.SEARCH,
                        arguments = listOf(
                            navArgument("prefill") {
                                type = NavType.StringType
                                defaultValue = Routes.NONE
                            },
                            navArgument("sortOrder") {
                                type = NavType.StringType
                                defaultValue = Routes.SORT_DEFAULT
                            },
                        ),
                    ) { entry ->
                        val rawPrefill = entry.arguments?.getString("prefill").orEmpty()
                        val prefill = if (rawPrefill == Routes.NONE) {
                            null
                        } else {
                            URLDecoder.decode(rawPrefill, Charsets.UTF_8.name())
                        }
                        val sortOrder = parseSortOrder(entry.arguments?.getString("sortOrder"))
                        SearchScreen(
                            prefilledQuery = prefill,
                            initialSortOrder = sortOrder,
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
                                navigateToSearch(navController, query, SortOrder.RELEVANCE)
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
                        selectedRouteKey = if (isSearchActive) {
                            ""
                        } else {
                            selectedRouteKey
                        },
                        isSearchActive = isSearchActive,
                        onItemSelected = { item -> navigateToTab(navController, item) },
                        onSearchClick = {
                            navController.navigate(Routes.searchDestination()) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
    }
}

private data class GrabHubSideNavItem(val routeKey: String)

private fun navigateToTab(
    navController: androidx.navigation.NavHostController,
    item: BottomNavItem,
) {
    val destination = when (item.routeKey) {
        "home" -> Routes.HOME
        "favorites" -> Routes.FAVORITES
        "history" -> Routes.HISTORY
        "settings" -> Routes.SETTINGS
        else -> Routes.HOME
    }
    navController.navigate(destination) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun navigateToTab(
    navController: androidx.navigation.NavHostController,
    item: GrabHubSideNavItem,
) {
    val destination = when (item.routeKey) {
        "home" -> Routes.HOME
        "favorites" -> Routes.FAVORITES
        "history" -> Routes.HISTORY
        "settings" -> Routes.SETTINGS
        else -> Routes.HOME
    }
    navController.navigate(destination) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun navigateToSearch(
    navController: androidx.navigation.NavHostController,
    query: String,
    sortOrder: SortOrder,
) {
    val encoded = URLEncoder.encode(query, Charsets.UTF_8.name())
    val sortArg = when (sortOrder) {
        SortOrder.POPULARITY -> "popularity"
        SortOrder.RELEVANCE -> "relevance"
    }
    navController.navigate(Routes.searchDestination(encoded, sortArg)) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
    }
}

private fun parseFeedType(raw: String?): FeedType = runCatching {
    FeedType.valueOf(raw.orEmpty().uppercase())
}.getOrDefault(FeedType.DISCOVER)

private fun parseSortOrder(raw: String?): SortOrder? = when (raw) {
    "popularity" -> SortOrder.POPULARITY
    "relevance" -> SortOrder.RELEVANCE
    else -> null
}

private fun detailRoute(modelId: String): String {
    val encoded = URLEncoder.encode(modelId, Charsets.UTF_8.name())
    return "detail/$encoded"
}
