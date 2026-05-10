package com.app.covidpredict.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.covidpredict.viewmodels.DashboardViewModel
import com.app.covidpredict.viewmodels.SharedViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    sharedViewModel: SharedViewModel,
    onNavigateToPrediction: () -> Unit,
    onNavigateToData: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Wilayah: ${sharedViewModel.selectedWilayah.value}")
        Text("Total Kasus: ${state.totalKasus}")
        Text("Kasus Hari Ini: ${state.kasusHariIni}")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onNavigateToPrediction) {
            Text("Ke Prediksi")
        }

        Button(onClick = onNavigateToData) {
            Text("Ke Data")
        }
    }
}