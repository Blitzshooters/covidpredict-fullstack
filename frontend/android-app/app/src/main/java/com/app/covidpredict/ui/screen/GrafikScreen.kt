package com.app.covidpredict.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.covidpredict.viewmodels.GrafikViewModel

@Composable
fun GrafikScreen(viewModel: GrafikViewModel) {
    val data by viewModel.grafikData.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Grafik (Dummy)")

        data.forEach {
            Text("Value: $it")
        }
    }
}