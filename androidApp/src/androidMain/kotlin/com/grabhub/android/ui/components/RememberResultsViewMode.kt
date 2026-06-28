package com.grabhub.android.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.grabhub.android.ui.preferences.ResultsViewMode
import com.grabhub.android.ui.preferences.UiPreferences
import org.koin.compose.koinInject

@Composable
fun rememberResultsViewMode(): Pair<ResultsViewMode, (ResultsViewMode) -> Unit> {
    val preferences = koinInject<UiPreferences>()
    var mode by remember { mutableStateOf(preferences.getResultsViewMode()) }
    val setMode: (ResultsViewMode) -> Unit = { newMode ->
        mode = newMode
        preferences.setResultsViewMode(newMode)
    }
    return mode to setMode
}
