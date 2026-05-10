package com.app.covidpredict.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.covidpredict.viewmodels.DataViewModel
import com.app.covidpredict.viewmodels.SharedViewModel

@Composable
fun DataScreen(
    viewModel: DataViewModel,
    sharedViewModel: SharedViewModel
) {
    val data by viewModel.dataList.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Data Wilayah")

        data.forEach {
            Button(onClick = {
                sharedViewModel.setWilayah(it)
            }) {
                Text(it)
            }
        }
    }
}