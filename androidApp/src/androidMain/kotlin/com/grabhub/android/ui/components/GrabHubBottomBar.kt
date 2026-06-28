package com.grabhub.android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grabhub.android.R
import com.grabhub.android.ui.theme.NavGlassBorder
import com.grabhub.android.ui.theme.NavGlassSurface
import com.grabhub.android.ui.theme.NavInactive
import com.grabhub.android.ui.theme.NavViolet

private val NavBarBodyHeight = 64.dp
private val NavBarOuterPadding = 12.dp
private val NavBarHorizontalPadding = 16.dp
private val NavBarCornerRadius = 28.dp
private val NavIndicatorWidth = 28.dp
private val NavIndicatorHeight = 3.dp
private val NavSpring = spring<Float>(stiffness = 380f, dampingRatio = 0.72f)
private val NavColorSpring = spring<Color>(stiffness = 420f, dampingRatio = 0.78f)

/** Total clearance for content above the floating nav (margins + bar body). */
val GrabHubBottomNavHeight = NavBarBodyHeight + NavBarOuterPadding * 2

data class BottomNavItem(
    val routeKey: String,
    val labelRes: Int,
    val icon: ImageVector,
)

val GrabHubBottomNavItems = listOf(
    BottomNavItem("favorites", R.string.nav_favorites, Icons.Outlined.FavoriteBorder),
    BottomNavItem("home", R.string.nav_home, Icons.Outlined.Home),
    BottomNavItem("search", R.string.nav_search, Icons.Outlined.Search),
    BottomNavItem("history", R.string.nav_history, Icons.Outlined.History),
    BottomNavItem("settings", R.string.nav_settings, Icons.Outlined.Settings),
)

/** @deprecated Use [GrabHubBottomNavItems] */
val GrabHubSideNavItems = GrabHubBottomNavItems.filter { it.routeKey != "search" }

/**
 * Floating glass-style bottom navigation: pill bar with margins, outlined icons,
 * violet active state, and sliding underline indicator — per GrabHub UI mockup.
 */
@Composable
fun GrabHubBottomNavBar(
    selectedRouteKey: String,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    GrabHubFloatingNavBar(
        selectedRouteKey = selectedRouteKey,
        onItemSelected = onItemSelected,
        modifier = modifier,
    )
}

@Composable
fun GrabHubFloatingNavBar(
    selectedRouteKey: String,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val barShape = RoundedCornerShape(NavBarCornerRadius)
    val selectedIndex = GrabHubBottomNavItems.indexOfFirst { it.routeKey == selectedRouteKey }
        .coerceAtLeast(0)
    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = NavSpring,
        label = "navActiveIndex",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                horizontal = NavBarHorizontalPadding,
                vertical = NavBarOuterPadding,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 10.dp, shape = barShape, clip = false)
                .clip(barShape)
                .background(NavGlassSurface)
                .border(width = 1.dp, color = NavGlassBorder, shape = barShape),
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NavBarBodyHeight),
            ) {
                val rowPadding = 4.dp
                val contentWidth = maxWidth - rowPadding * 2
                val tabWidth = contentWidth / GrabHubBottomNavItems.size
                val indicatorOffset = rowPadding + tabWidth * animatedIndex +
                    (tabWidth - NavIndicatorWidth) / 2

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = rowPadding),
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

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = indicatorOffset, y = (-10).dp)
                        .width(NavIndicatorWidth)
                        .height(NavIndicatorHeight)
                        .clip(RoundedCornerShape(50))
                        .background(NavViolet),
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeColor = NavViolet
    val inactiveColor = NavInactive
    val contentColor by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        animationSpec = NavColorSpring,
        label = "navItemColor",
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = NavSpring,
        label = "navIconScale",
    )

    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = stringResource(item.labelRes),
                modifier = Modifier
                    .size(24.dp)
                    .scale(iconScale),
                tint = contentColor,
            )
            Text(
                text = stringResource(item.labelRes),
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
