package com.grabhub.android.ui.preferences

import android.content.Context
import com.grabhub.domain.SourceType

class UiPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getResultsViewMode(): ResultsViewMode {
        return when (prefs.getString(KEY_VIEW_MODE, ResultsViewMode.GRID.name)) {
            ResultsViewMode.LIST.name -> ResultsViewMode.LIST
            else -> ResultsViewMode.GRID
        }
    }

    fun setResultsViewMode(mode: ResultsViewMode) {
        prefs.edit().putString(KEY_VIEW_MODE, mode.name).apply()
    }

    fun getAppLanguage(): AppLanguage = AppLanguage.fromStorageKey(prefs.getString(KEY_LANGUAGE, AppLanguage.RU.storageKey))

    fun setAppLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.storageKey).apply()
    }

    fun getEnabledSources(): Set<SourceType> {
        val raw = prefs.getString(KEY_ENABLED_SOURCES, null)
        if (raw.isNullOrBlank()) return SourceType.entries.toSet()
        val parsed = raw.split(',')
            .mapNotNull { name -> runCatching { SourceType.valueOf(name) }.getOrNull() }
            .toSet()
        return parsed.ifEmpty { SourceType.entries.toSet() }
    }

    fun setEnabledSources(sources: Set<SourceType>) {
        prefs.edit()
            .putString(KEY_ENABLED_SOURCES, sources.joinToString(",") { it.name })
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "grabhub_ui"
        const val KEY_VIEW_MODE = "results_view_mode"
        const val KEY_LANGUAGE = "app_language"
        const val KEY_ENABLED_SOURCES = "enabled_sources"
    }
}
