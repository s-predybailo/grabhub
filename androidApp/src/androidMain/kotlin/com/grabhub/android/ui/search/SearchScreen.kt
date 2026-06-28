package com.grabhub.android.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grabhub.android.ui.components.GrabHubEmptyState
import com.grabhub.android.ui.components.GrabHubLoadingState
import com.grabhub.android.ui.components.ModelResultsLayout
import com.grabhub.android.ui.components.ViewModeToggle
import com.grabhub.android.ui.components.rememberResultsViewMode
import com.grabhub.android.ui.theme.GrabHubShapes
import com.grabhub.domain.LicenseFilter
import com.grabhub.domain.PriceFilter
import com.grabhub.domain.ProviderError
import com.grabhub.domain.SortOrder
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onModelClick: (String) -> Unit,
    prefilledQuery: String? = null,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val (viewMode, setViewMode) = rememberResultsViewMode()
    var filtersExpanded by rememberSaveable { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(prefilledQuery) {
        prefilledQuery?.let { viewModel.search(it) }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text("GrabHub")
                        Text(
                            text = "Search 3D models across sources",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            SearchInputBar(
                query = uiState.query,
                onQueryChange = viewModel::onQueryChange,
                onSearch = { viewModel.search() },
                filtersExpanded = filtersExpanded,
                onToggleFilters = { filtersExpanded = !filtersExpanded },
            )

            AnimatedVisibility(
                visible = filtersExpanded,
                enter = expandVertically(
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                ) + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                SearchFiltersPanel(
                    priceFilter = uiState.filters.priceFilter,
                    sortOrder = uiState.filters.sortOrder,
                    licenseFilter = uiState.filters.licenseFilter,
                    onPriceFilter = viewModel::setPriceFilter,
                    onSortOrder = viewModel::setSortOrder,
                    onLicenseFilter = viewModel::setLicenseFilter,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        GrabHubLoadingState()
                    }
                }

                uiState.fatalError != null -> {
                    GrabHubEmptyState(
                        title = "Search failed",
                        subtitle = uiState.fatalError ?: "Unknown error",
                    )
                }

                uiState.hasSearched && uiState.results.isEmpty() -> {
                    GrabHubEmptyState(
                        title = "No models found",
                        subtitle = "Try another query or adjust filters",
                    )
                }

                uiState.results.isNotEmpty() -> {
                    if (uiState.errors.isNotEmpty()) {
                        ProviderErrorsBanner(errors = uiState.errors)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    ResultsHeader(
                        count = uiState.results.size,
                        viewMode = viewMode,
                        onViewModeChange = setViewMode,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ModelResultsLayout(
                        items = uiState.results,
                        viewMode = viewMode,
                        onItemClick = onModelClick,
                        modifier = Modifier.weight(1f),
                    )
                }

                else -> {
                    GrabHubEmptyState(
                        title = "Start exploring",
                        subtitle = "Enter a model name, author, or keyword to search Printables, Thingiverse, MakerWorld, and Creality Cloud",
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchInputBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    filtersExpanded: Boolean,
    onToggleFilters: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = GrabHubShapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Benchy, vase, articulated dragon…") },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )
            IconButton(onClick = onToggleFilters) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Toggle filters",
                    tint = if (filtersExpanded) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            IconButton(onClick = onSearch) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun ResultsHeader(
    count: Int,
    viewMode: com.grabhub.android.ui.preferences.ResultsViewMode,
    onViewModeChange: (com.grabhub.android.ui.preferences.ResultsViewMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$count results",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        ViewModeToggle(mode = viewMode, onModeChange = onViewModeChange)
    }
}

@Composable
private fun SearchFiltersPanel(
    priceFilter: PriceFilter,
    sortOrder: SortOrder,
    licenseFilter: LicenseFilter,
    onPriceFilter: (PriceFilter) -> Unit,
    onSortOrder: (SortOrder) -> Unit,
    onLicenseFilter: (LicenseFilter) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .animateContentSize(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            ),
        shape = GrabHubShapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            FilterGroup(title = "Price") {
                FilterChipRow {
                    FilterChoice(selected = priceFilter == PriceFilter.ALL, label = "All", onClick = { onPriceFilter(PriceFilter.ALL) })
                    FilterChoice(selected = priceFilter == PriceFilter.FREE_ONLY, label = "Free", onClick = { onPriceFilter(PriceFilter.FREE_ONLY) })
                    FilterChoice(selected = priceFilter == PriceFilter.PAID_ONLY, label = "Paid", onClick = { onPriceFilter(PriceFilter.PAID_ONLY) })
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            FilterGroup(title = "Sort") {
                FilterChipRow {
                    FilterChoice(selected = sortOrder == SortOrder.RELEVANCE, label = "Relevance", onClick = { onSortOrder(SortOrder.RELEVANCE) })
                    FilterChoice(selected = sortOrder == SortOrder.POPULARITY, label = "Popular", onClick = { onSortOrder(SortOrder.POPULARITY) })
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            FilterGroup(title = "License") {
                FilterChipRow {
                    FilterChoice(selected = licenseFilter == LicenseFilter.ALL, label = "All", onClick = { onLicenseFilter(LicenseFilter.ALL) })
                    FilterChoice(selected = licenseFilter == LicenseFilter.COMMERCIAL_OK, label = "Commercial", onClick = { onLicenseFilter(LicenseFilter.COMMERCIAL_OK) })
                    FilterChoice(selected = licenseFilter == LicenseFilter.NON_COMMERCIAL, label = "Non-commercial", onClick = { onLicenseFilter(LicenseFilter.NON_COMMERCIAL) })
                }
            }
        }
    }
}

@Composable
private fun FilterGroup(
    title: String,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun FilterChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        content()
    }
}

@Composable
private fun FilterChoice(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    )
}

@Composable
private fun ProviderErrorsBanner(errors: List<ProviderError>) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f),
        shape = GrabHubShapes.small,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Some sources failed",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            errors.forEach { error ->
                Text(
                    text = "${error.source.name}: ${error.message}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}
