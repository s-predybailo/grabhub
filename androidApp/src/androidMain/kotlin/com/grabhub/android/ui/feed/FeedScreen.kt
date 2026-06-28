package com.grabhub.android.ui.feed

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grabhub.android.R
import com.grabhub.android.ui.components.GrabHubEmptyState
import com.grabhub.android.ui.components.GrabHubLoadingState
import com.grabhub.android.ui.components.ModelGridItem
import com.grabhub.domain.FeedType
import org.koin.androidx.compose.koinViewModel

@Composable
fun FeedScreen(
    feedType: FeedType,
    onBack: () -> Unit,
    onModelClick: (String) -> Unit,
    viewModel: FeedViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(feedType) {
        viewModel.load(feedType)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        FeedTopBar(
            title = stringResource(feedTitleRes(feedType)),
            subtitle = stringResource(feedSubtitleRes(feedType)),
            onBack = onBack,
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    GrabHubLoadingState(message = stringResource(R.string.loading))
                }
            }

            uiState.error != null && uiState.items.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    GrabHubEmptyState(
                        title = stringResource(R.string.home_feed_error_title),
                        subtitle = stringResource(R.string.home_feed_error_subtitle),
                    )
                    TextButton(onClick = { viewModel.load(feedType) }) {
                        Text(stringResource(R.string.home_feed_retry))
                    }
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 168.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = stringResource(R.string.home_feed_count, uiState.items.size),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                    items(uiState.items, key = { it.id }) { item ->
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
private fun FeedTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.detail_back))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun feedTitleRes(feedType: FeedType): Int = when (feedType) {
    FeedType.TRENDING -> R.string.feed_trending_title
    FeedType.POPULAR -> R.string.feed_popular_title
    FeedType.LATEST -> R.string.feed_latest_title
    FeedType.DISCOVER -> R.string.home_feed_title
}

private fun feedSubtitleRes(feedType: FeedType): Int = when (feedType) {
    FeedType.TRENDING -> R.string.feed_trending_subtitle
    FeedType.POPULAR -> R.string.feed_popular_subtitle
    FeedType.LATEST -> R.string.feed_latest_subtitle
    FeedType.DISCOVER -> R.string.feed_discover_subtitle
}
