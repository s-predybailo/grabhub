package com.grabhub.android.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grabhub.android.BuildConfig
import com.grabhub.android.R
import com.grabhub.android.ui.components.GrabHubEmptyState
import com.grabhub.android.ui.components.GrabHubLoadingState
import com.grabhub.android.ui.components.ModelGridItem
import com.grabhub.android.ui.theme.Amber400
import com.grabhub.android.ui.theme.Emerald400
import com.grabhub.android.ui.theme.GrabHubShapes
import com.grabhub.android.ui.theme.Rose400
import com.grabhub.android.ui.theme.Violet400
import com.grabhub.domain.FeedType
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onModelClick: (String) -> Unit,
    onOpenFeed: (FeedType) -> Unit,
    onOpenHistory: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        HomeHeader()
        Spacer(modifier = Modifier.height(12.dp))

        HomeShortcuts(
            onPopular = { onOpenFeed(FeedType.POPULAR) },
            onLatest = { onOpenFeed(FeedType.LATEST) },
            onTrending = { onOpenFeed(FeedType.TRENDING) },
            onHistory = onOpenHistory,
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    GrabHubLoadingState(message = stringResource(R.string.loading))
                }
            }

            uiState.error != null && uiState.feedItems.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    GrabHubEmptyState(
                        title = stringResource(R.string.home_feed_error_title),
                        subtitle = stringResource(R.string.home_feed_error_subtitle),
                    )
                    TextButton(onClick = viewModel::loadFeed) {
                        Text(stringResource(R.string.home_feed_retry))
                    }
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 168.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.home_feed_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = stringResource(R.string.home_feed_count, uiState.feedItems.size),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    items(uiState.feedItems, key = { it.id }) { item ->
                        ModelGridItem(
                            item = item,
                            onClick = { onModelClick(item.id) },
                            overlayBadges = true,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(R.string.home_subtitle, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun HomeShortcuts(
    onPopular: () -> Unit,
    onLatest: () -> Unit,
    onTrending: () -> Unit,
    onHistory: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HomeShortcutCard(
                label = stringResource(R.string.home_shortcut_popular),
                icon = Icons.Default.TrendingUp,
                tint = Amber400,
                onClick = onPopular,
                modifier = Modifier.weight(1f),
            )
            HomeShortcutCard(
                label = stringResource(R.string.home_shortcut_latest),
                icon = Icons.Default.NewReleases,
                tint = Emerald400,
                onClick = onLatest,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HomeShortcutCard(
                label = stringResource(R.string.home_shortcut_trending),
                icon = Icons.Default.Whatshot,
                tint = Rose400,
                onClick = onTrending,
                modifier = Modifier.weight(1f),
            )
            HomeShortcutCard(
                label = stringResource(R.string.home_shortcut_history),
                icon = Icons.Default.History,
                tint = Violet400,
                onClick = onHistory,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HomeShortcutCard(
    label: String,
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(88.dp),
        shape = GrabHubShapes.large,
        color = tint.copy(alpha = 0.12f),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = tint,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
