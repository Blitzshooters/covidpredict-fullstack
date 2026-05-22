package com.app.covidpredict.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val lokasi: String = "Nasional",
    val lastUpdated: String = "14 Okt 2023, 08:32 AM",

    val confirmed: String = "6,813,429",
    val todayIncrease: String = "+1.240 Hari Ini",
    val rawTodayIncrease: Int = 1240,

    val recovered: String = "6,642,810",
    val recoveredRate: String = "97.5% Tingkat",

    val deaths: String = "161,918",
    val deathRate: String = "2.3% Tingkat",

    val trendPercent: String = "-12.4%",
    val trendStatus: String = "Penurunan",
    val modelConfidence: String = "Kepercayaan model: 94.2% berdasarkan data terbaru.",

    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null
)

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun fetchDashboardData(lokasi: String = "Nasional") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                lokasi = lokasi,
                isLoading = true,
                isError = false,
                errorMessage = null
            )

            delay(800)

            _uiState.value = _uiState.value.copy(
                lokasi = lokasi,
                lastUpdated = "14 Okt 2023, 08:32 AM",
                confirmed = if (lokasi == "Nasional") "6,813,429" else "124,830",
                todayIncrease = if (lokasi == "Nasional") "+1.240 Hari Ini" else "+320 Hari Ini",
                rawTodayIncrease = if (lokasi == "Nasional") 1240 else 320,
                recovered = if (lokasi == "Nasional") "6,642,810" else "118,450",
                recoveredRate = if (lokasi == "Nasional") "97.5% Tingkat" else "94.9% Tingkat",
                deaths = if (lokasi == "Nasional") "161,918" else "2,130",
                deathRate = if (lokasi == "Nasional") "2.3% Tingkat" else "1.7% Tingkat",
                trendPercent = if (lokasi == "Nasional") "-12.4%" else "-8.7%",
                trendStatus = "Penurunan",
                modelConfidence = "Kepercayaan model: 94.2% berdasarkan data terbaru.",
                isLoading = false,
                isRefreshing = false,
                isError = false,
                errorMessage = null
            )
        }
    }

    fun refreshData(lokasi: String = "Nasional") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRefreshing = true,
                isError = false,
                errorMessage = null
            )

            delay(800)

            _uiState.value = _uiState.value.copy(
                lokasi = lokasi,
                lastUpdated = "Baru saja diperbarui",
                isRefreshing = false,
                isLoading = false,
                isError = false,
                errorMessage = null
            )
        }
    }
}