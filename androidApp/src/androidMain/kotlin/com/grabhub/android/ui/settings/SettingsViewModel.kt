package com.grabhub.android.ui.settings

import androidx.lifecycle.ViewModel
import com.grabhub.android.ui.preferences.AppLanguage
import com.grabhub.android.ui.preferences.ResultsViewMode
import com.grabhub.android.ui.preferences.UiPreferences
import com.grabhub.domain.SourceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val language: AppLanguage = AppLanguage.RU,
    val enabledSources: Set<SourceType> = SourceType.entries.toSet(),
    val viewMode: ResultsViewMode = ResultsViewMode.GRID,
    val thingiverseConfigured: Boolean = false,
    val versionName: String = "",
)

class SettingsViewModel(
    private val uiPreferences: UiPreferences,
    thingiverseConfigured: Boolean,
    versionName: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            language = uiPreferences.getAppLanguage(),
            enabledSources = uiPreferences.getEnabledSources(),
            viewMode = uiPreferences.getResultsViewMode(),
            thingiverseConfigured = thingiverseConfigured,
            versionName = versionName,
        ),
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setLanguage(language: AppLanguage, onApplied: () -> Unit) {
        if (language == uiPreferences.getAppLanguage()) return
        uiPreferences.setAppLanguage(language)
        _uiState.update { it.copy(language = language) }
        onApplied()
    }

    fun toggleSource(source: SourceType) {
        _uiState.update { state ->
            val current = state.enabledSources.toMutableSet()
            if (source in current) {
                if (current.size > 1) current.remove(source)
            } else {
                current.add(source)
            }
            uiPreferences.setEnabledSources(current)
            state.copy(enabledSources = current)
        }
    }

    fun setViewMode(mode: ResultsViewMode) {
        uiPreferences.setResultsViewMode(mode)
        _uiState.update { it.copy(viewMode = mode) }
    }
}
