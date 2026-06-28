package com.grabhub.android.ui.preferences

import android.content.Context

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

    private companion object {
        const val PREFS_NAME = "grabhub_ui"
        const val KEY_VIEW_MODE = "results_view_mode"
    }
}
