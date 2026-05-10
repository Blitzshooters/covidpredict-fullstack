package com.app.covidpredict.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    var selectedWilayah = mutableStateOf("Nasional")

    fun setWilayah(wilayah: String) {
        selectedWilayah.value = wilayah
    }
}