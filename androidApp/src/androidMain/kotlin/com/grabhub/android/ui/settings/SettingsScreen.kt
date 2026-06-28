package com.grabhub.android.ui.settings

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grabhub.android.BuildConfig
import com.grabhub.android.R
import com.grabhub.android.ui.components.label
import com.grabhub.android.ui.preferences.AppLanguage
import com.grabhub.android.ui.preferences.ResultsViewMode
import com.grabhub.android.ui.theme.GrabHubShapes
import com.grabhub.domain.SourceType
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel {
        parametersOf(
            BuildConfig.THINGIVERSE_ACCESS_TOKEN.isNotBlank(),
            BuildConfig.VERSION_NAME,
        )
    },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(20.dp))

        SettingsSection(title = stringResource(R.string.settings_language)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LanguageChip(
                    label = stringResource(R.string.settings_language_system),
                    selected = uiState.language == AppLanguage.SYSTEM,
                    onClick = { viewModel.setLanguage(AppLanguage.SYSTEM) { activity?.recreate() } },
                )
                LanguageChip(
                    label = stringResource(R.string.settings_language_ru),
                    selected = uiState.language == AppLanguage.RU,
                    onClick = { viewModel.setLanguage(AppLanguage.RU) { activity?.recreate() } },
                )
                LanguageChip(
                    label = stringResource(R.string.settings_language_en),
                    selected = uiState.language == AppLanguage.EN,
                    onClick = { viewModel.setLanguage(AppLanguage.EN) { activity?.recreate() } },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(
            title = stringResource(R.string.settings_default_sources),
            subtitle = stringResource(R.string.settings_default_sources_subtitle),
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SourceType.entries.forEach { source ->
                    val selected = source in uiState.enabledSources
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.toggleSource(source) },
                        label = { Text(source.label()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(
            title = stringResource(R.string.settings_view_mode),
            subtitle = stringResource(R.string.settings_view_mode_subtitle),
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LanguageChip(
                    label = stringResource(R.string.view_grid),
                    selected = uiState.viewMode == ResultsViewMode.GRID,
                    onClick = { viewModel.setViewMode(ResultsViewMode.GRID) },
                )
                LanguageChip(
                    label = stringResource(R.string.view_list),
                    selected = uiState.viewMode == ResultsViewMode.LIST,
                    onClick = { viewModel.setViewMode(ResultsViewMode.LIST) },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(title = stringResource(R.string.settings_thingiverse)) {
            Text(
                text = if (uiState.thingiverseConfigured) {
                    stringResource(R.string.settings_thingiverse_configured)
                } else {
                    stringResource(R.string.settings_thingiverse_missing)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(title = stringResource(R.string.settings_about)) {
            Text(
                text = stringResource(R.string.settings_version, uiState.versionName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = GrabHubShapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun LanguageChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    )
}
