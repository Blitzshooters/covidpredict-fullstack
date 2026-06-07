package com.app.covidpredict.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.covidpredict.data.repository.PredictionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

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
    val forecastDailyCases: String = "-",
    val trendStatus: String = "Menunggu perhitungan...",
    val trendDescription: String = "Menunggu perhitungan...",
    val confidenceInterval: String = "-",
    val avgError: String = "-",
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null
)

class PrediksiViewModel(
    private val repository: PredictionRepository = PredictionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrediksiUiState())
    val uiState: StateFlow<PrediksiUiState> = _uiState.asStateFlow()

    // Optimization: Cache expensive formatters to avoid re-creation in data updates
    private val idLocale = Locale("id", "ID")
    private val idNumberFormat = NumberFormat.getNumberInstance(idLocale)

    fun onAlphaChange(newAlpha: Float) {
        _uiState.value = _uiState.value.copy(
            alpha = newAlpha.coerceIn(0.1f, 1.0f),
            isError = false,
            errorMessage = null
        )
    }

    fun resetPrediction() {
        _uiState.value = PrediksiUiState()
    }

    fun onRegionChange(region: String) {
        _uiState.value = _uiState.value.copy(selectedRegion = region)
    }

    fun onDaysChange(days: String) {
        val cleanValue = days.trim()

        // Hanya izinkan angka atau kosong
        if (cleanValue.all { it.isDigit() } || cleanValue.isEmpty()) {

            val hasLeadingZero = cleanValue.length > 1 && cleanValue.startsWith("0")
            val number = cleanValue.toIntOrNull()
            val isInvalid = cleanValue.isNotEmpty() &&
                    (hasLeadingZero || number == null || number !in 1..30)

            _uiState.value = _uiState.value.copy(
                predictionDays = cleanValue,
                isError = isInvalid,
                errorMessage = if (isInvalid) {
                    "Masukkan angka 1-30 tanpa awalan 0"
                } else {
                    null
                }
            )
        }
    }

    fun calculatePrediction() {
        val state = _uiState.value
        val input = state.predictionDays.trim()

        val hasLeadingZero = input.length > 1 && input.startsWith("0")
        val daysInput = input.toIntOrNull()

        if (
            input.isEmpty() ||
            hasLeadingZero ||
            daysInput == null ||
            daysInput !in 1..30
        ) {
            _uiState.value = state.copy(
                isError = true,
                errorMessage = "Masukkan angka 1-30 tanpa awalan 0"
            )
            return
        }

        val apiRegion = if (state.selectedRegion == "Nasional") {
            "Indonesia"
        } else {
            state.selectedRegion
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                isError = false,
                errorMessage = null
            )

            delay(1000)

            try {
                val response = repository.predict(
                    wilayah = apiRegion,
                    days = daysInput,
                    alpha = state.alpha.toDouble()
                )

                val result = response.data

                val trendDirection = if (result.trendPercentage >= 0) {
                    "kenaikan"
                } else {
                    "penurunan"
                }

                val trendDescription = String.format(
                    idLocale,
                    "%.1f%% %s dari tren saat ini",
                    kotlin.math.abs(result.trendPercentage),
                    trendDirection
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isError = false,
                    estimatedCases = idNumberFormat.format(result.estimatedCases),
                    forecastDailyCases = idNumberFormat.format(result.forecastDailyCases),
                    trendStatus = result.trendStatus,
                    trendDescription = trendDescription,
                    confidenceInterval = String.format(
                        idLocale,
                        "%.1f%%",
                        result.confidenceInterval
                    ),
                    avgError = String.format(
                        idLocale,
                        "%.2f%%",
                        result.avgError
                    ),
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message ?: "Gagal menghitung prediksi"
                )
            }
        }
    }
}