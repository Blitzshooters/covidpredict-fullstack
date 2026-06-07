package com.app.covidpredict.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.covidpredict.data.repository.DashboardRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class DashboardUiState(
    val lastUpdated: String = "-",
    val confirmed: String = "0",
    val todayIncrease: String = "0 Hari Ini",
    val rawTodayIncrease: Int = 0,
    val recovered: String = "0",
    val recoveredRate: String = "0% Tingkat",
    val deaths: String = "0",
    val deathRate: String = "0% Tingkat",
    val trendPercent: String = "0%",
    val trendStatus: String = "Memuat...",
    val modelConfidence: String = "Menganalisis data terbaru...",
    val isRefreshing: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val repository: DashboardRepository = DashboardRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Pre-create NumberFormat to avoid expensive recreation during data updates
    private val idNumberFormat = NumberFormat.getInstance(Locale("id", "ID"))

    fun refreshData(wilayah: String) {
        fetchDashboardData(wilayah, isRefreshing = true)
    }

    fun fetchDashboardData(
        wilayah: String = "Indonesia",
        isRefreshing: Boolean = false
    ) {
        viewModelScope.launch {

            if (isRefreshing) {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }

            val startTime = System.currentTimeMillis()

            try {
                val response = repository.getDashboard(wilayah)

                if (isRefreshing) {
                    val elapsedTime = System.currentTimeMillis() - startTime
                    if (elapsedTime < 500L) {
                        delay(500L - elapsedTime)
                    }
                }

                _uiState.value = _uiState.value.copy(
                    lastUpdated = response.lastUpdated,
                    confirmed = idNumberFormat.format(response.confirmed),
                    todayIncrease = "${if (response.todayIncrease >= 0) "+" else ""}${idNumberFormat.format(response.todayIncrease)} Hari Ini",
                    rawTodayIncrease = response.todayIncrease,
                    recovered = idNumberFormat.format(response.recovered),
                    recoveredRate = "${response.recoveredRate}% Tingkat",
                    deaths = idNumberFormat.format(response.deaths),
                    deathRate = "${response.deathRate}% Tingkat",
                    trendPercent = "${if (response.trendPercent > 0) "+" else ""}${
                        String.format(Locale("id", "ID"), "%.1f", response.trendPercent)
                    }%",
                    trendStatus = response.trendStatus,
                    modelConfidence = "Kepercayaan model: ${
                        String.format(Locale("id", "ID"), "%.1f", response.modelConfidence)
                    }% berdasarkan data terbaru.",
                    isRefreshing = false,
                    isLoading = false,
                    errorMessage = null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    isLoading = false,
                    errorMessage = e.message ?: "Terjadi kesalahan saat mengambil data"
                )
            }
        }
    }
}
