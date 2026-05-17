package com.app.covidpredict.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DashboardState(
    val lokasi: String = "Nasional",
    val lastUpdated: String = "14 Okt 2023, 08:32 AM",

    val confirmed: String = "6,813,429",
    val todayIncrease: String = "+1.240 Hari Ini",

    val recovered: String = "6,642,810",
    val recoveredRate: String = "97.5% Tingkat",

    val deaths: String = "161,918",
    val deathRate: String = "2.3% Tingkat",

    val trendPercent: String = "-12.4%",
    val trendStatus: String = "Penurunan",
    val modelConfidence: String = "94.2%"
)

class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardState())
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()
}