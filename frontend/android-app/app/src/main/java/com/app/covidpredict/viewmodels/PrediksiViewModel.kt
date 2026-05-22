package com.app.covidpredict.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

data class PrediksiUiState(
    val selectedRegion: String = "Nasional",
    val regions: List<String> = listOf(
        "Nasional", "Aceh", "Bali", "Banten", "Bengkulu", "DKI Jakarta",
        "Daerah Istimewa Yogyakarta", "Gorontalo", "Jambi", "Jawa Barat",
        "Jawa Tengah", "Jawa Timur", "Kalimantan Barat", "Kalimantan Selatan",
        "Kalimantan Tengah", "Kalimantan Timur", "Kalimantan Utara",
        "Kepulauan Bangka Belitung", "Kepulauan Riau", "Lampung", "Maluku",
        "Maluku Utara", "Nusa Tenggara Barat", "Nusa Tenggara Timur",
        "Papua", "Papua Barat", "Riau", "Sulawesi Barat", "Sulawesi Selatan",
        "Sulawesi Tengah", "Sulawesi Tenggara", "Sulawesi Utara",
        "Sumatera Barat", "Sumatera Selatan", "Sumatera Utara"
    ),
    val predictionDays: String = "14",
    val alpha: Float = 0.7f,
    val estimatedCases: String = "-",
    val trendStatus: String = "Menunggu perhitungan...",
    val confidenceInterval: String = "-",
    val avgError: String = "-",
    val isLoading: Boolean = false,
    val isError: Boolean = false
)

class PrediksiViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PrediksiUiState())
    val uiState: StateFlow<PrediksiUiState> = _uiState.asStateFlow()

    // Mock dataset untuk simulasi (Kasus harian 10 hari terakhir)
    private val mockTimeSeriesData = listOf(1200.0, 1150.0, 1300.0, 1250.0, 1400.0, 1350.0, 1500.0, 1450.0, 1600.0, 1550.0)

    fun onAlphaChange(newAlpha: Float) {
        _uiState.value = _uiState.value.copy(alpha = newAlpha)
    }

    fun onRegionChange(region: String) {
        _uiState.value = _uiState.value.copy(selectedRegion = region)
    }

    fun onDaysChange(days: String) {
        // Hanya izinkan angka
        if (days.all { it.isDigit() } || days.isEmpty()) {
            _uiState.value = _uiState.value.copy(predictionDays = days)
        }
    }

    fun calculatePrediction() {
        val daysInput = _uiState.value.predictionDays.toIntOrNull() ?: 0

        // Validasi 1-30 hari
        if (daysInput !in 1..30) {
            _uiState.value = _uiState.value.copy(isError = true)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isError = false)

            // Simulasi loading/kalkulasi
            delay(1500)

            val alpha = _uiState.value.alpha
            val data = mockTimeSeriesData

            // Logika Single Exponential Smoothing (SES)
            // S_t = alpha * Y_t + (1 - alpha) * S_{t-1}
            var level = data[0]
            val errors = mutableListOf<Double>()

            for (i in 1 until data.size) {
                val forecast = level
                val actual = data[i]
                errors.add(abs(actual - forecast))
                level = alpha * actual + (1 - alpha) * level
            }

            // Hasil peramalan SES adalah nilai 'level' terakhir
            val forecastResult = level.toInt()
            val mae = if (errors.isNotEmpty()) errors.average() else 0.0

            // Mock Confidence Interval berdasarkan MAE
            val ciValue = 100 - (mae / forecastResult * 100).coerceIn(0.0, 10.0)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                estimatedCases = String.format(Locale.getDefault(), "%,d", forecastResult),
                avgError = String.format(Locale.getDefault(), "%.2f%%", (mae / forecastResult * 100)),
                confidenceInterval = String.format(Locale.getDefault(), "%.1f%%", ciValue),
                trendStatus = if (forecastResult > data.last()) "Potensi Kenaikan" else "Potensi Penurunan"
            )
        }
    }
}
