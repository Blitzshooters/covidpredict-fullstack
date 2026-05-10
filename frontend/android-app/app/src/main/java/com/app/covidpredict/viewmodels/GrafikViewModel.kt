package com.app.covidpredict.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GrafikViewModel : ViewModel() {
    private val _grafikData = MutableStateFlow(
        listOf(10, 20, 30, 25, 40)
    )
    val grafikData: StateFlow<List<Int>> = _grafikData
}