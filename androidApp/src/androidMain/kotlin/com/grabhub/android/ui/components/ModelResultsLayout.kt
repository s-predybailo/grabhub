package com.grabhub.android.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grabhub.android.ui.preferences.ResultsViewMode
import com.grabhub.domain.ModelItem

@Composable
fun ModelResultsLayout(
    items: List<ModelItem>,
    viewMode: ResultsViewMode,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = 8.dp),
) {
    AnimatedContent(
        targetState = viewMode,
        modifier = modifier.fillMaxSize(),
        transitionSpec = {
            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
        },
        label = "resultsLayout",
    ) { mode ->
        when (mode) {
            ResultsViewMode.LIST -> {
                LazyColumn(
                    contentPadding = contentPadding,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items, key = { it.id }) { item ->
                        ModelListItem(
                            item = item,
                            onClick = { onItemClick(item.id) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }

            ResultsViewMode.GRID -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 172.dp),
                    contentPadding = contentPadding,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items, key = { it.id }) { item ->
                        ModelGridItem(
                            item = item,
                            onClick = { onItemClick(item.id) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}
