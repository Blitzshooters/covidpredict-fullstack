package com.app.covidpredict.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.covidpredict.data.repository.GrafikRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

data class ChartPoint(
    val axisLabel: String,
    val tooltipLabel: String,
    val actual: Float,
    val prediction: Float
)

data class GrafikUiState(
    val selectedTab: Int = 0, // 0: Harian, 1: Mingguan, 2: Bulanan
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
    val avgError: String = "-",
    val insightTitle: String = "Analisis Epidemiologi",
    val insightText: String = "Mengambil wawasan model SES terbaru...",
    val chartData: List<ChartPoint> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val refreshCount: Int = 0, // Menghitung berapa kali master reset dilakukan
    val errorMessage: String? = null,
    val showActual: Boolean = true,
    val showPrediction: Boolean = true
)

class GrafikViewModel(
    private val repository: GrafikRepository = GrafikRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(GrafikUiState())
    val uiState: StateFlow<GrafikUiState> = _uiState.asStateFlow()

    private var fetchJob: Job? = null

    // Optimization: Cache expensive date-related objects to avoid re-creation in list processing
    private val idLocale = Locale("id", "ID")
    private val weekFields = java.time.temporal.WeekFields.of(idLocale)
    private val axisDayFormatter = java.time.format.DateTimeFormatter.ofPattern("EEE", idLocale)
    private val axisMonthFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM", idLocale)
    private val tooltipDayFormatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy", idLocale)
    private val tooltipMonthYearFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM yyyy", idLocale)
    private val tooltipFullMonthFormatter = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", idLocale)

    init {
        fetchChartData()
    }

    fun selectTab(index: Int) {
        if (_uiState.value.selectedTab == index) return
        _uiState.value = _uiState.value.copy(selectedTab = index)
        fetchChartData()
    }

    fun selectRegion(region: String) {
        if (_uiState.value.selectedRegion == region) return
        _uiState.value = _uiState.value.copy(selectedRegion = region)
        fetchChartData()
    }

    fun toggleActual() {
        if (_uiState.value.showPrediction) {
            _uiState.value = _uiState.value.copy(showActual = !_uiState.value.showActual)
        }
    }

    fun togglePrediction() {
        if (_uiState.value.showActual) {
            _uiState.value = _uiState.value.copy(showPrediction = !_uiState.value.showPrediction)
        }
    }

    fun refreshChartData() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRefreshing = true,
                errorMessage = null
            )

            delay(1200)
            performFetch(isReset = false)
        }
    }

    private fun fetchChartData() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            performFetch(isReset = false)
        }
    }

    private suspend fun performFetch(isReset: Boolean = false) {
        val currentState = _uiState.value

        // Gunakan default jika isReset=true, jika tidak gunakan pilihan user saat ini
        val apiRegion = if (isReset) "Indonesia" else {
            if (currentState.selectedRegion == "Nasional") "Indonesia" else currentState.selectedRegion
        }
        val apiTabIndex = if (isReset) 0 else currentState.selectedTab

        val period = when (apiTabIndex) {
            0 -> "harian"
            1 -> "mingguan"
            2 -> "bulanan"
            else -> "harian"
        }

        try {
            val response = repository.getChart(apiRegion, period)
            val data = response.data
            _uiState.value = _uiState.value.copy(
                // Update filter ke default HANYA SETELAH data load berhasil (isReset)
                selectedTab = if (isReset) 0 else currentState.selectedTab,
                selectedRegion = if (isReset) "Nasional" else currentState.selectedRegion,
                showActual = if (isReset) true else currentState.showActual,
                showPrediction = if (isReset) true else currentState.showPrediction,

                avgError = data.avgError,
                insightTitle = data.insightTitle,
                insightText = data.insightText,
                chartData = data.chartData.map { point ->
                    val labels = formatLabels(point.label, apiTabIndex)
                    ChartPoint(
                        axisLabel = labels.first,
                        tooltipLabel = labels.second,
                        actual = point.actual,
                        prediction = point.prediction
                    )
                },
                isLoading = false,
                isRefreshing = false,
                refreshCount = if (isReset) currentState.refreshCount + 1 else currentState.refreshCount,
                errorMessage = null
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isRefreshing = false,
                errorMessage = "Gagal terhubung ke server: ${e.localizedMessage}"
            )
        }
    }

    private fun formatLabels(label: String, tabIndex: Int): Pair<String, String> {
        return try {
            // Optimization: Parse once, use cached formatters
            val date = java.time.LocalDate.parse(label.substring(0, 10))

            val axisLabel = when (tabIndex) {
                0 -> date.format(axisDayFormatter)
                1 -> "M${date.get(weekFields.weekOfMonth())}"
                2 -> date.format(axisMonthFormatter)
                else -> label
            }

            val tooltipLabel = when (tabIndex) {
                0 -> date.format(tooltipDayFormatter)
                1 -> {
                    val week = date.get(weekFields.weekOfMonth())
                    val monthYear = date.format(tooltipMonthYearFormatter)
                    "Minggu ke-$week, $monthYear"
                }
                2 -> date.format(tooltipFullMonthFormatter)
                else -> label
            }

            axisLabel to tooltipLabel
        } catch (e: Exception) {
            label to label
        }
    }
}
