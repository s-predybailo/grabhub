package com.grabhub.android.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grabhub.android.R
import com.grabhub.android.ui.components.GrabHubEmptyState
import com.grabhub.android.ui.components.ModelResultsLayout
import com.grabhub.android.ui.components.ViewModeToggle
import com.grabhub.android.ui.components.rememberResultsViewMode
import org.koin.androidx.compose.koinViewModel

@Composable
fun FavoritesScreen(
    onModelClick: (String) -> Unit,
    viewModel: FavoritesViewModel = koinViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val (viewMode, setViewMode) = rememberResultsViewMode()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.favorites_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                GrabHubEmptyState(
                    title = stringResource(R.string.favorites_empty_title),
                    subtitle = stringResource(R.string.favorites_empty_subtitle),
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.favorites_count, items.size),
                    style = MaterialTheme.typography.titleMedium,
                )
                ViewModeToggle(mode = viewMode, onModeChange = setViewMode)
            }
            Spacer(modifier = Modifier.height(8.dp))
            ModelResultsLayout(
                items = items,
                viewMode = viewMode,
                onItemClick = onModelClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
