package com.grabhub.android.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grabhub.android.R
import com.grabhub.android.ui.theme.NavActiveOrange
import com.grabhub.android.ui.theme.NavBarDark

private val NavBarHorizontalMargin = 20.dp
private val NavBarCornerRadius = 26.dp
private val NavBubbleSize = 48.dp
private val NavBarBodyHeight = 56.dp
private val NavBubbleRise = NavBubbleSize / 2
private val NavIconSize = 22.dp

/** Total visible height above the system navigation inset (bubble half + bar body). */
val GrabHubBottomNavHeight = NavBarBodyHeight + NavBubbleRise

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
 * Animated bottom navigation with a sliding concave dip and floating active bubble,
 * inspired by curved mobile nav bar references.
 */
@Composable
fun GrabHubBottomNavBar(
    selectedRouteKey: String,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = GrabHubBottomNavItems.indexOfFirst { it.routeKey == selectedRouteKey }
        .coerceAtLeast(0)
    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(stiffness = 380f, dampingRatio = 0.72f),
        label = "navActiveIndex",
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = NavBarHorizontalMargin)
            .height(GrabHubBottomNavHeight),
    ) {
        val density = LocalDensity.current
        val barWidthPx = with(density) { maxWidth.toPx() }
        val bubbleRisePx = with(density) { NavBubbleRise.toPx() }
        val barBodyHeightPx = with(density) { NavBarBodyHeight.toPx() }
        val cornerRadiusPx = with(density) { NavBarCornerRadius.toPx() }
        val bubbleRadiusPx = with(density) { (NavBubbleSize / 2).toPx() }
        val tabWidthPx = barWidthPx / GrabHubBottomNavItems.size
        val bubbleCenterX = tabWidthPx * (animatedIndex + 0.5f)
        val bubbleCenterY = bubbleRisePx

        Canvas(modifier = Modifier.fillMaxSize()) {
            val barTop = bubbleRisePx
            val barPath = createCurvedBarPath(
                width = barWidthPx,
                barTop = barTop,
                barHeight = barBodyHeightPx,
                cornerRadius = cornerRadiusPx,
                dipCenterX = bubbleCenterX,
                dipRadius = bubbleRadiusPx,
            )
            drawPath(barPath, NavBarDark)

            drawCircle(
                color = NavActiveOrange,
                radius = bubbleRadiusPx,
                center = Offset(bubbleCenterX, bubbleCenterY),
            )
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
        ) {
            GrabHubBottomNavItems.forEach { item ->
                val selected = selectedRouteKey == item.routeKey
                NavBarItem(
                    item = item,
                    selected = selected,
                    onClick = { onItemSelected(item) },
                    inactiveIconTop = NavBubbleRise + (NavBarBodyHeight - NavIconSize) / 2,
                    activeIconTop = NavBubbleRise - NavIconSize / 2,
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
    inactiveIconTop: Dp,
    activeIconTop: Dp,
    modifier: Modifier = Modifier,
) {
    val inactiveColor = Color.White.copy(alpha = 0.55f)
    val activeColor = Color.White

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 28.dp),
                onClick = onClick,
            ),
        contentAlignment = Alignment.TopCenter,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = stringResource(item.labelRes),
            modifier = Modifier
                .size(NavIconSize)
                .offset(y = if (selected) activeIconTop else inactiveIconTop),
            tint = if (selected) activeColor else inactiveColor,
        )
    }
}

private fun createCurvedBarPath(
    width: Float,
    barTop: Float,
    barHeight: Float,
    cornerRadius: Float,
    dipCenterX: Float,
    dipRadius: Float,
): Path {
    val barBottom = barTop + barHeight
    val dipWidth = dipRadius * 2.4f
    val dipStart = (dipCenterX - dipWidth / 2f).coerceIn(cornerRadius, width - cornerRadius - dipWidth)
    val dipEnd = dipStart + dipWidth
    val dipDepth = dipRadius * 0.72f

    return Path().apply {
        moveTo(0f, barTop + cornerRadius)
        arcTo(
            rect = Rect(0f, barTop, cornerRadius * 2f, barTop + cornerRadius * 2f),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false,
        )
        lineTo(dipStart, barTop)
        cubicTo(
            dipStart + dipWidth * 0.22f,
            barTop,
            dipCenterX - dipRadius * 0.55f,
            barTop + dipDepth,
            dipCenterX,
            barTop + dipDepth,
        )
        cubicTo(
            dipCenterX + dipRadius * 0.55f,
            barTop + dipDepth,
            dipEnd - dipWidth * 0.22f,
            barTop,
            dipEnd,
            barTop,
        )
        lineTo(width - cornerRadius, barTop)
        arcTo(
            rect = Rect(width - cornerRadius * 2f, barTop, width, barTop + cornerRadius * 2f),
            startAngleDegrees = 270f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false,
        )
        lineTo(width, barBottom - cornerRadius)
        arcTo(
            rect = Rect(width - cornerRadius * 2f, barBottom - cornerRadius * 2f, width, barBottom),
            startAngleDegrees = 0f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false,
        )
        lineTo(cornerRadius, barBottom)
        arcTo(
            rect = Rect(0f, barBottom - cornerRadius * 2f, cornerRadius * 2f, barBottom),
            startAngleDegrees = 90f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false,
        )
        close()
    }
}
