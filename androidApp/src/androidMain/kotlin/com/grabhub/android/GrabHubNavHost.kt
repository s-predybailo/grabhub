package com.grabhub.android

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.grabhub.android.ui.GrabHubTheme
import com.grabhub.android.ui.detail.DetailScreen
import com.grabhub.android.ui.search.SearchScreen
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun GrabHubNavHost() {
    val navController = rememberNavController()

    GrabHubTheme {
        NavHost(
            navController = navController,
            startDestination = "search",
        ) {
            composable("search") {
                SearchScreen(
                    onModelClick = { modelId ->
                        val encoded = URLEncoder.encode(modelId, Charsets.UTF_8.name())
                        navController.navigate("detail/$encoded")
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
