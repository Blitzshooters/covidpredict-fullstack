package com.app.covidpredict.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HistoryData(
    val date: String,
    val positive: String,
    val recovered: String,
    val deaths: String
)

data class DataUiState(
    val lastUpdated: String = "14 Okt 2023, 08:32 AM",
    val avgDailyCases: String = "12,482",
    val recoveryRate: String = "94.2%",
    val historyList: List<HistoryData> = listOf(
        HistoryData("Oct 24, 2023", "14,201", "13,105", "92"),
        HistoryData("Oct 23, 2023", "12,894", "11,942", "84"),
        HistoryData("Oct 22, 2023", "15,042", "14,220", "112"),
        HistoryData("Oct 21, 2023", "11,203", "10,800", "76"),
        HistoryData("Oct 20, 2023", "13,556", "12,980", "89"),
        HistoryData("Oct 19, 2023", "14,110", "13,400", "95"),
        HistoryData("Oct 18, 2023", "12,110", "11,400", "75"),
        HistoryData("Oct 17, 2023", "11,510", "10,200", "65"),
        HistoryData("Oct 16, 2023", "10,110", "9,400", "55"),
        HistoryData("Oct 15, 2023", "9,110", "8,400", "45")
    ),
    val filteredHistoryList: List<HistoryData> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedFilter: String = "30 Hari Terakhir"
)

class DataViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DataUiState())
    val uiState: StateFlow<DataUiState> = _uiState.asStateFlow()

    init {
        updateFilteredList()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        updateFilteredList()
    }

    fun onFilterChange(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        updateFilteredList()
    }

    private fun updateFilteredList() {
        val state = _uiState.value

        // Mock filtering by date range
        var filtered = when (state.selectedFilter) {
            "7 Hari Terakhir" -> state.historyList.take(7)
            "30 Hari Terakhir" -> state.historyList // Assuming 30 days is the whole list for this demo
            else -> state.historyList // For custom range, we just show all in this demo
        }

        // Search filtering
        if (state.searchQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.date.contains(state.searchQuery, ignoreCase = true) ||
                        it.positive.contains(state.searchQuery) ||
                        it.recovered.contains(state.searchQuery) ||
                        it.deaths.contains(state.searchQuery)
            }
        }

        _uiState.value = state.copy(filteredHistoryList = filtered)
    }
}
