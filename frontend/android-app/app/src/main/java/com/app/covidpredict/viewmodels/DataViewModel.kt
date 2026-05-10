package com.app.covidpredict.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DataViewModel : ViewModel() {
    private val _dataList = MutableStateFlow(
        listOf("Jawa Timur", "DKI Jakarta", "Jawa Barat")
    )
    val dataList: StateFlow<List<String>> = _dataList
}