package com.app.covidpredict.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ChartPoint(val label: String, val actual: Float, val prediction: Float)

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
    val avgError: String = "-2.4%",
    val insightTitle: String = "Wawasan Model",
    val insightText: String = "Prediksi menunjukkan dataran 15% di sektor utara karena peningkatan kepadatan vaksinasi. Data aktual mencerminkan tren ini dengan fidelitas tinggi.",
    val chartData: List<ChartPoint> = listOf(
        ChartPoint("Mon", 10f, 12f),
        ChartPoint("Tue", 15f, 14f),
        ChartPoint("Wed", 25f, 24f),
        ChartPoint("Thu", 20f, 22f),
        ChartPoint("Fri", 18f, 19f),
        ChartPoint("Sat", 15f, 16f),
        ChartPoint("Sun", 30f, 28f)
    )
)

class GrafikViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GrafikUiState())
    val uiState: StateFlow<GrafikUiState> = _uiState.asStateFlow()

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(
            selectedTab = index,
            chartData = generateMockData(index)
        )
    }

    fun selectRegion(region: String) {
        _uiState.value = _uiState.value.copy(
            selectedRegion = region,
            chartData = generateMockData(_uiState.value.selectedTab)
        )
    }

    private fun generateMockData(tabIndex: Int): List<ChartPoint> {
        val labels = when (tabIndex) {
            0 -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            1 -> listOf("W1", "W2", "W3", "W4", "W5", "W6", "W7")
            else -> listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul")
        }
        return labels.map {
            val actual = (10..50).random().toFloat()
            ChartPoint(it, actual, actual + (-5..5).random())
        }
    }
}
