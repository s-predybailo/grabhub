package com.grabhub.android.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grabhub.android.R

/** Visible tab row height — matches common Figma bottom-nav templates. */
val GrabHubBottomNavHeight = 56.dp

private val NavIndicatorWidth = 32.dp
private val NavIndicatorHeight = 3.dp

data class BottomNavItem(
    val routeKey: String,
    val labelRes: Int,
    val icon: ImageVector,
)

val GrabHubBottomNavItems = listOf(
    BottomNavItem("favorites", R.string.nav_favorites, Icons.Default.Favorite),
    BottomNavItem("home", R.string.nav_home, Icons.Default.Home),
    BottomNavItem("search", R.string.nav_search, Icons.Default.Search),
    BottomNavItem("history", R.string.nav_history, Icons.Default.History),
    BottomNavItem("settings", R.string.nav_settings, Icons.Default.Settings),
)

/** @deprecated Use [GrabHubBottomNavItems] */
val GrabHubSideNavItems = GrabHubBottomNavItems.filter { it.routeKey != "search" }

/**
 * Bottom navigation in the style of the Figma "Mobile Navigation Menu Bar UI Template":
 * full-width docked bar, subtle top divider, equal-width tabs, active underline indicator.
 */
@Composable
fun GrabHubBottomNavBar(
    selectedRouteKey: String,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding(),
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(GrabHubBottomNavHeight),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GrabHubBottomNavItems.forEach { item ->
                NavBarItem(
                    item = item,
                    selected = selectedRouteKey == item.routeKey,
                    onClick = { onItemSelected(item) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** Alias kept for existing call sites. */
@Composable
fun GrabHubFloatingNavBar(
    selectedRouteKey: String,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    GrabHubBottomNavBar(
        selectedRouteKey = selectedRouteKey,
        onItemSelected = onItemSelected,
        modifier = modifier,
    )
}

@Composable
private fun NavBarItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
    val indicatorWidth by animateDpAsState(
        targetValue = if (selected) NavIndicatorWidth else 0.dp,
        animationSpec = spring(stiffness = 420f, dampingRatio = 0.78f),
        label = "navIndicatorWidth",
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "navIconScale",
    )

    Surface(
        onClick = onClick,
        modifier = modifier,
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = stringResource(item.labelRes),
                modifier = Modifier
                    .size(22.dp)
                    .scale(iconScale),
                tint = if (selected) activeColor else inactiveColor,
            )
            Text(
                text = stringResource(item.labelRes),
                fontSize = 11.sp,
                lineHeight = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) activeColor else inactiveColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Box(
                modifier = Modifier
                    .height(NavIndicatorHeight)
                    .width(indicatorWidth)
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) activeColor else Color.Transparent),
            )
        }
    }
}
