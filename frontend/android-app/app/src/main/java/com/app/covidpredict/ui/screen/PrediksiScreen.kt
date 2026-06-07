package com.app.covidpredict.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.app.covidpredict.R
import com.app.covidpredict.theme.CovidPredictTheme
import com.app.covidpredict.viewmodels.PrediksiViewModel
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrediksiScreen(viewModel: PrediksiViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val pullToRefreshState = rememberPullToRefreshState()
    val context = LocalContext.current

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(pullToRefreshState.isRefreshing) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

            delay(1200)

            viewModel.resetPrediction()

            if (pullToRefreshState.isRefreshing) {
                pullToRefreshState.endRefresh()
            }

            Toast.makeText(
                context,
                "Form prediksi berhasil direset",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
            .background(Color(0xFFF8FBFE))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                PrediksiHeader()
            }

            item {
                PredictionForm(
                    selectedRegion = uiState.selectedRegion,
                    regions = uiState.regions,
                    predictionDays = uiState.predictionDays,
                    alpha = uiState.alpha,
                    isLoading = uiState.isLoading,
                    isError = uiState.isError,
                    onAlphaChange = { viewModel.onAlphaChange(it) },
                    onRegionChange = { viewModel.onRegionChange(it) },
                    onDaysChange = { viewModel.onDaysChange(it) },
                    onProcess = { viewModel.calculatePrediction() }
                )
            }

            item {
                Text(
                    text = "Hasil Prediksi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF021F29)
                )
            }

            if (uiState.estimatedCases == "-" && !uiState.isLoading) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEBF6FF)
                    ) {
                        Text(
                            text = "Atur parameter lalu tekan Proses Prediksi untuk melihat hasil estimasi.",
                            modifier = Modifier.padding(14.dp),
                            color = Color.Gray,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            item {
                EstimationResultCard(
                    estimatedCases = if (uiState.isLoading) "..." else uiState.estimatedCases,
                    forecastDailyCases = if (uiState.isLoading) "..." else uiState.forecastDailyCases,
                    trendStatus = if (uiState.isLoading) "Menghitung..." else uiState.trendStatus,
                    trendDescription = if (uiState.isLoading) "Menghitung tren..." else uiState.trendDescription
                )
            }

            item {
                PredictionStatCard(
                    label = "Tingkat Kepercayaan Model",
                    value = if (uiState.isLoading) "..." else uiState.confidenceInterval
                )
            }

            item {
                PredictionStatCard(
                    label = "Rata-rata Error Prediksi (MAE)",
                    value = if (uiState.isLoading) "..." else uiState.avgError
                )
            }

            item {
                MethodologySection()
            }
        }

        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun PrediksiHeader() {
    Column {
        Text(
            text = "Analisis Prediksi",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF021F29)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Atur parameter untuk model penghalusan klinis guna memperkirakan lintasan transmisi.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            lineHeight = 20.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionForm(
    selectedRegion: String,
    regions: List<String>,
    predictionDays: String,
    alpha: Float,
    isLoading: Boolean,
    isError: Boolean,
    onAlphaChange: (Float) -> Unit,
    onRegionChange: (String) -> Unit,
    onDaysChange: (String) -> Unit,
    onProcess: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Pilih Wilayah",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF021F29)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEBF6FF)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            selectedRegion,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF021F29)
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.dropdown),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.Gray
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    regions.forEach { region ->
                        DropdownMenuItem(
                            text = { Text(region) },
                            onClick = {
                                onRegionChange(region)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Cakupan Prediksi (Hari)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF021F29)
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = predictionDays,
                onValueChange = onDaysChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("1-30 hari") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFEBF6FF),
                    unfocusedContainerColor = Color(0xFFEBF6FF),
                    focusedIndicatorColor = if (isError) Color.Red else MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = if (isError) Color.Red else Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                isError = isError
            )

            if (isError) {
                Text(
                    "Masukkan nilai antara 1-30 hari",
                    color = Color.Red,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Cakupan jangka pendek (7-14 hari) menawarkan reliabilitas statistik yang lebih tinggi.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Faktor Penghalusan (Alpha)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF021F29)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    value = alpha.coerceIn(0.1f, 1.0f),
                    onValueChange = {
                        onAlphaChange(it.coerceIn(0.1f, 1.0f))
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    valueRange = 0.1f..1.0f,
                    steps = 8,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = Color.LightGray.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    color = Color(0xFFEBF6FF),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f", alpha.coerceIn(0.1f, 1.0f)),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            val alphaDescription = remember(alpha) {
                when {
                    alpha < 0.4f -> "Alpha rendah: prediksi lebih stabil dan halus."
                    alpha < 0.7f -> "Alpha sedang: seimbang antara stabil dan responsif."
                    else -> "Alpha tinggi: prediksi lebih responsif terhadap data terbaru."
                }
            }

            val isDaysValid = remember(predictionDays) {
                predictionDays.toIntOrNull() in 1..30 &&
                        !(predictionDays.length > 1 && predictionDays.startsWith("0"))
            }

            Text(
                text = alphaDescription,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onProcess,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading && isDaysValid,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.chart_bar),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Proses Prediksi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EstimationResultCard(
    estimatedCases: String,
    forecastDailyCases: String,
    trendStatus: String,
    trendDescription: String
) {
    val isDecrease = trendStatus.contains("Penurunan", ignoreCase = true) ||
            trendDescription.contains("penurunan", ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFCDE8F9).copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                painter = painterResource(id = R.drawable.chart_line),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(200.dp)
                    .offset(x = 30.dp),
                tint = Color.Gray.copy(alpha = 0.2f)
            )

            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "ESTIMASI PREDIKSI",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    AnimatedContent(
                        targetState = estimatedCases,
                        transitionSpec = {
                            (slideInVertically { height -> height } + fadeIn())
                                .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                        },
                        label = "estimatedCasesAnimation"
                    ) { targetCases ->
                        Text(
                            text = targetCases,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF021F29),
                            fontSize = if (estimatedCases.length > 8) 36.sp else 42.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Estimasi Total\nKasus Positif",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Prediksi kasus harian SES: $forecastDailyCases kasus/hari",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(
                            id = if (isDecrease) {
                                R.drawable.trending_down
                            } else {
                                R.drawable.trending_up
                            }
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isDecrease) {
                            Color(0xFF126D27)
                        } else {
                            Color(0xFFB02528)
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    AnimatedContent(
                        targetState = trendDescription,
                        transitionSpec = {
                            (fadeIn() + slideInHorizontally { width -> width / 2 })
                                .togetherWith(fadeOut() + slideOutHorizontally { width -> -width / 2 })
                        },
                        label = "trendStatusAnimation"
                    ) { targetTrend ->
                        Text(
                            text = targetTrend,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isDecrease) {
                                Color(0xFF126D27)
                            } else {
                                Color(0xFFB02528)
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PredictionStatCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF021F29)
            )
        }
    }
}

@Composable
fun MethodologySection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFEBF6FF).copy(alpha = 0.8f)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Metodologi SES",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            val annotatedString = remember {
                buildAnnotatedString {
                    append("Metode ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Single Exponential Smoothing (SES)")
                    }
                    append(" adalah teknik peramalan deret waktu untuk data univariat. Ini menggunakan rata-rata bergerak berbobot di mana bobot berkurang secara eksponensial seiring bertambahnya usia observasi. Pendekatan ini sangat efektif untuk data epidemiologi di mana tren terkini (yang diatur oleh Faktor Penghalusan α) lebih representatif untuk hasil masa depan segera dibandingkan dengan puncak historis.")
                }
            }

            Text(
                text = annotatedString,
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray,
                lineHeight = 18.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PrediksiScreenPreview() {
    CovidPredictTheme {
        PrediksiScreen(viewModel = PrediksiViewModel())
    }
}
