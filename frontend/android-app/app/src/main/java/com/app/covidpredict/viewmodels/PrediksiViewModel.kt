package com.app.covidpredict.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PrediksiViewModel : ViewModel() {
    private val _prediksi = MutableStateFlow("Belum ada prediksi")
    val prediksi: StateFlow<String> = _prediksi

    fun generatePrediksi() {
        _prediksi.value = "Kasus akan meningkat 10%"
    }
}