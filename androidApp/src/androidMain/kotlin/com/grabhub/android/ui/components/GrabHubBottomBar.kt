package com.grabhub.android.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grabhub.android.R

private val NavBarHeight = 60.dp
private val FabSize = 56.dp
private val NavHostHeight = 76.dp

data class BottomNavItem(
    val routeKey: String,
    val labelRes: Int,
    val icon: ImageVector,
)

val GrabHubSideNavItems = listOf(
    BottomNavItem("favorites", R.string.nav_favorites, Icons.Default.Favorite),
    BottomNavItem("home", R.string.nav_home, Icons.Default.Home),
    BottomNavItem("history", R.string.nav_history, Icons.Default.History),
    BottomNavItem("settings", R.string.nav_settings, Icons.Default.Settings),
)

@Composable
fun GrabHubFloatingNavBar(
    selectedRouteKey: String,
    isSearchActive: Boolean,
    onItemSelected: (BottomNavItem) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(NavHostHeight)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(NavBarHeight),
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            shadowElevation = 10.dp,
            tonalElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GrabHubSideNavItems.take(2).forEach { item ->
                    FloatingNavBarItem(
                        item = item,
                        selected = selectedRouteKey == item.routeKey,
                        onClick = { onItemSelected(item) },
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(modifier = Modifier.width(FabSize))
                GrabHubSideNavItems.drop(2).forEach { item ->
                    FloatingNavBarItem(
                        item = item,
                        selected = selectedRouteKey == item.routeKey,
                        onClick = { onItemSelected(item) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onSearchClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(FabSize),
            shape = CircleShape,
            containerColor = if (isSearchActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surface
            },
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 10.dp,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.nav_search),
                modifier = Modifier.size(26.dp),
                tint = if (isSearchActive) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )
        }
    }
}

@Composable
private fun FloatingNavBarItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.04f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "navItemScale",
    )
    Surface(
        onClick = onClick,
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .scale(scale),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0f)
        },
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = stringResource(item.labelRes),
                modifier = Modifier.size(22.dp),
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Text(
                text = stringResource(item.labelRes),
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
