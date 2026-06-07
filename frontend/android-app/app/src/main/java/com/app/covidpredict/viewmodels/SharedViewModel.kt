package com.app.covidpredict.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedViewModel : ViewModel() {

    private val _selectedLocation = MutableStateFlow("Nasional")
    val selectedLocation: StateFlow<String> = _selectedLocation.asStateFlow()

    // 🔥 list untuk UI
    val locations = listOf(
        "Nasional", "Aceh", "Bali", "Banten", "Bengkulu", "DKI Jakarta",
        "Daerah Istimewa Yogyakarta", "Gorontalo", "Jambi", "Jawa Barat",
        "Jawa Tengah", "Jawa Timur", "Kalimantan Barat", "Kalimantan Selatan",
        "Kalimantan Tengah", "Kalimantan Timur", "Kalimantan Utara",
        "Kepulauan Bangka Belitung", "Kepulauan Riau", "Lampung", "Maluku",
        "Maluku Utara", "Nusa Tenggara Barat", "Nusa Tenggara Timur",
        "Papua", "Papua Barat", "Riau", "Sulawesi Barat", "Sulawesi Selatan",
        "Sulawesi Tengah", "Sulawesi Tenggara", "Sulawesi Utara",
        "Sumatera Barat", "Sumatera Selatan", "Sumatera Utara"
    )

    fun updateLocation(location: String) {
        _selectedLocation.value = location
    }

    fun getApiLocation(): String {
        return if (_selectedLocation.value == "Nasional") {
            "Indonesia"
        } else {
            _selectedLocation.value
        }
    }
}