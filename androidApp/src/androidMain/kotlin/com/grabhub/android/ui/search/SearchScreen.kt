package com.grabhub.android.ui.search

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grabhub.android.ui.components.ModelItemCard
import com.grabhub.domain.LicenseFilter
import com.grabhub.domain.PriceFilter
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

    androidx.compose.runtime.LaunchedEffect(prefilledQuery) {
        prefilledQuery?.let { viewModel.search(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Search") })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search 3D models…") },
                    singleLine = true,
                )
                IconButton(onClick = { viewModel.search() }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }

            SearchFiltersRow(
                priceFilter = uiState.filters.priceFilter,
                sortOrder = uiState.filters.sortOrder,
                licenseFilter = uiState.filters.licenseFilter,
                onPriceFilter = viewModel::setPriceFilter,
                onSortOrder = viewModel::setSortOrder,
                onLicenseFilter = viewModel::setLicenseFilter,
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.fatalError != null -> {
                    Text(
                        text = uiState.fatalError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }

                uiState.hasSearched && uiState.results.isEmpty() -> {
                    Text(
                        text = "No results found",
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }

                else -> {
                    if (uiState.errors.isNotEmpty()) {
                        ProviderErrorsBanner(errors = uiState.errors)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.results, key = { it.id }) { item ->
                            ModelItemCard(
                                item = item,
                                onClick = { onModelClick(item.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchFiltersRow(
    priceFilter: PriceFilter,
    sortOrder: SortOrder,
    licenseFilter: LicenseFilter,
    onPriceFilter: (PriceFilter) -> Unit,
    onSortOrder: (SortOrder) -> Unit,
    onLicenseFilter: (LicenseFilter) -> Unit,
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text("Price", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(selected = priceFilter == PriceFilter.ALL, onClick = { onPriceFilter(PriceFilter.ALL) }, label = { Text("All") })
            FilterChip(selected = priceFilter == PriceFilter.FREE_ONLY, onClick = { onPriceFilter(PriceFilter.FREE_ONLY) }, label = { Text("Free") })
            FilterChip(selected = priceFilter == PriceFilter.PAID_ONLY, onClick = { onPriceFilter(PriceFilter.PAID_ONLY) }, label = { Text("Paid") })
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Sort", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = sortOrder == SortOrder.RELEVANCE, onClick = { onSortOrder(SortOrder.RELEVANCE) }, label = { Text("Relevance") })
            FilterChip(selected = sortOrder == SortOrder.POPULARITY, onClick = { onSortOrder(SortOrder.POPULARITY) }, label = { Text("Popular") })
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("License", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(selected = licenseFilter == LicenseFilter.ALL, onClick = { onLicenseFilter(LicenseFilter.ALL) }, label = { Text("All") })
            FilterChip(selected = licenseFilter == LicenseFilter.COMMERCIAL_OK, onClick = { onLicenseFilter(LicenseFilter.COMMERCIAL_OK) }, label = { Text("Commercial OK") })
            FilterChip(selected = licenseFilter == LicenseFilter.NON_COMMERCIAL, onClick = { onLicenseFilter(LicenseFilter.NON_COMMERCIAL) }, label = { Text("Non-commercial") })
        }
    }
}

@Composable
private fun ProviderErrorsBanner(errors: List<com.grabhub.domain.ProviderError>) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Some sources failed:", style = MaterialTheme.typography.labelLarge)
            errors.forEach { error ->
                Text(
                    text = "${error.source.name}: ${error.message}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
