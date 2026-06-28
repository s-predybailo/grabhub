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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grabhub.android.BuildConfig
import com.grabhub.android.R
import com.grabhub.android.ui.components.GrabHubEmptyState
import com.grabhub.android.ui.components.GrabHubLoadingState
import com.grabhub.android.ui.components.ModelResultsLayout
import com.grabhub.android.ui.components.ViewModeToggle
import com.grabhub.android.ui.components.label
import com.grabhub.android.ui.components.rememberResultsViewMode
import com.grabhub.android.ui.theme.GrabHubShapes
import com.grabhub.domain.LicenseFilter
import com.grabhub.domain.PriceFilter
import com.grabhub.domain.ProviderError
import com.grabhub.domain.SortOrder
import com.grabhub.domain.SourceType
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onModelClick: (String) -> Unit,
    prefilledQuery: String? = null,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val (viewMode, setViewMode) = rememberResultsViewMode()
    var filtersExpanded by rememberSaveable { mutableStateOf(false) }
    var providerErrorsDismissed by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(prefilledQuery) {
        prefilledQuery?.let { viewModel.search(it) }
    }

    LaunchedEffect(uiState.errors) {
        if (uiState.errors.isNotEmpty()) {
            providerErrorsDismissed = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp),
    ) {
        SearchScreenHeader()
        Spacer(modifier = Modifier.height(12.dp))

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
                enabledSources = uiState.filters.enabledSources.toSet(),
                onPriceFilter = viewModel::setPriceFilter,
                onSortOrder = viewModel::setSortOrder,
                onLicenseFilter = viewModel::setLicenseFilter,
                onToggleSource = viewModel::toggleSource,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    GrabHubLoadingState(message = stringResource(R.string.loading))
                }
            }

            uiState.fatalError != null -> {
                GrabHubEmptyState(
                    title = stringResource(R.string.search_failed),
                    subtitle = uiState.fatalError ?: stringResource(R.string.unknown_error),
                )
            }

            uiState.hasSearched && uiState.results.isEmpty() -> {
                GrabHubEmptyState(
                    title = stringResource(R.string.no_models_found),
                    subtitle = stringResource(R.string.no_models_found_subtitle),
                )
            }

            uiState.results.isNotEmpty() -> {
                if (uiState.errors.isNotEmpty() && !providerErrorsDismissed) {
                    ProviderErrorsBanner(
                        errors = uiState.errors,
                        onDismiss = { providerErrorsDismissed = true },
                    )
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
                    title = stringResource(R.string.start_exploring),
                    subtitle = stringResource(R.string.start_exploring_subtitle),
                )
            }
        }
    }
}

@Composable
private fun SearchScreenHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(R.string.search_subtitle, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
        shadowElevation = 4.dp,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.search_clear),
                            )
                        }
                    }
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
                    contentDescription = stringResource(R.string.search_toggle_filters),
                    tint = if (filtersExpanded) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
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
            text = stringResource(R.string.results_count, count),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        ViewModeToggle(mode = viewMode, onModeChange = onViewModeChange)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchFiltersPanel(
    priceFilter: PriceFilter,
    sortOrder: SortOrder,
    licenseFilter: LicenseFilter,
    enabledSources: Set<SourceType>,
    onPriceFilter: (PriceFilter) -> Unit,
    onSortOrder: (SortOrder) -> Unit,
    onLicenseFilter: (LicenseFilter) -> Unit,
    onToggleSource: (SourceType) -> Unit,
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
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            FilterGroup(title = stringResource(R.string.filter_sources)) {
                Text(
                    text = stringResource(R.string.filter_sources_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SourceType.entries.forEach { source ->
                        FilterChip(
                            selected = source in enabledSources,
                            onClick = { onToggleSource(source) },
                            label = { Text(source.label()) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            FilterGroup(title = stringResource(R.string.filter_price)) {
                FilterChipRow {
                    FilterChoice(selected = priceFilter == PriceFilter.ALL, label = stringResource(R.string.filter_all), onClick = { onPriceFilter(PriceFilter.ALL) })
                    FilterChoice(selected = priceFilter == PriceFilter.FREE_ONLY, label = stringResource(R.string.filter_free), onClick = { onPriceFilter(PriceFilter.FREE_ONLY) })
                    FilterChoice(selected = priceFilter == PriceFilter.PAID_ONLY, label = stringResource(R.string.filter_paid), onClick = { onPriceFilter(PriceFilter.PAID_ONLY) })
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            FilterGroup(title = stringResource(R.string.filter_sort)) {
                FilterChipRow {
                    FilterChoice(selected = sortOrder == SortOrder.RELEVANCE, label = stringResource(R.string.filter_relevance), onClick = { onSortOrder(SortOrder.RELEVANCE) })
                    FilterChoice(selected = sortOrder == SortOrder.POPULARITY, label = stringResource(R.string.filter_popular), onClick = { onSortOrder(SortOrder.POPULARITY) })
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            FilterGroup(title = stringResource(R.string.filter_license)) {
                FilterChipRow {
                    FilterChoice(selected = licenseFilter == LicenseFilter.ALL, label = stringResource(R.string.filter_all), onClick = { onLicenseFilter(LicenseFilter.ALL) })
                    FilterChoice(selected = licenseFilter == LicenseFilter.COMMERCIAL_OK, label = stringResource(R.string.filter_commercial), onClick = { onLicenseFilter(LicenseFilter.COMMERCIAL_OK) })
                    FilterChoice(selected = licenseFilter == LicenseFilter.NON_COMMERCIAL, label = stringResource(R.string.filter_non_commercial), onClick = { onLicenseFilter(LicenseFilter.NON_COMMERCIAL) })
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
private fun ProviderErrorsBanner(
    errors: List<ProviderError>,
    onDismiss: () -> Unit,
) {
    val tokenHint = stringResource(R.string.thingiverse_token_hint)
    Surface(
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.75f),
        shape = GrabHubShapes.small,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.some_sources_failed),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(start = 4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.dismiss_warnings),
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
            errors.forEach { error ->
                Text(
                    text = formatProviderErrorLine(error, tokenHint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun formatProviderErrorLine(error: ProviderError, tokenHint: String): String {
    val source = error.source.name.lowercase().replaceFirstChar { it.uppercase() }
    val message = error.message
        .replace(Regex("https?://\\S+"), "")
        .replace(Regex("\\s+"), " ")
        .trim()
        .trimEnd('.', ' ')
    return if (message.contains("access token", ignoreCase = true)) {
        "$source: $tokenHint"
    } else {
        "$source: $message"
    }
}
