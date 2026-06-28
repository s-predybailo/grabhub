package com.grabhub.android.ui.history

import androidx.lifecycle.ViewModel
import com.grabhub.cache.SearchHistoryEntry
import com.grabhub.data.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistoryViewModel(
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    private val _entries = MutableStateFlow(historyRepository.getHistory())
    val entries: StateFlow<List<SearchHistoryEntry>> = _entries.asStateFlow()

    fun refresh() {
        _entries.value = historyRepository.getHistory()
    }

    fun clearAll() {
        historyRepository.clearAll()
        refresh()
    }

    fun remove(query: String) {
        historyRepository.removeEntry(query)
        refresh()
    }
}
