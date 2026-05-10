package com.app.covidpredict.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.covidpredict.viewmodels.PrediksiViewModel

@Composable
fun PrediksiScreen(viewModel: PrediksiViewModel) {
    val prediksi by viewModel.prediksi.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Prediksi")

        Spacer(modifier = Modifier.height(16.dp))

        Text(prediksi)

        Button(onClick = {
            viewModel.generatePrediksi()
        }) {
            Text("Generate Prediksi")
        }
    }
}