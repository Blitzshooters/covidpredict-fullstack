package com.app.covidpredict.ui.screen

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.covidpredict.R
import com.app.covidpredict.theme.CovidPredictTheme
import com.app.covidpredict.viewmodels.ChartPoint
import com.app.covidpredict.viewmodels.GrafikViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.compose.common.shader.color
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.patrykandpatrick.vico.core.common.shader.DynamicShader

@Composable
fun GrafikScreen(viewModel: GrafikViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FBFE)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            GrafikHeaderSection()
        }

        item {
            FilterAndRegionRow(
                selectedTab = uiState.selectedTab,
                selectedRegion = uiState.selectedRegion,
                regions = uiState.regions,
                onTabSelected = { viewModel.selectTab(it) },
                onRegionSelected = { viewModel.selectRegion(it) }
            )
        }

        item {
            CaseTrajectoryCard(
                avgError = uiState.avgError,
                chartData = uiState.chartData
            )
        }

        item {
            Text(
                text = "Dibandingkan minggu sebelumnya",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF021F29)
            )
        }

        item {
            ModelInsightCard(
                title = uiState.insightTitle,
                content = uiState.insightText
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun GrafikHeaderSection() {
    Column {
        Text(
            text = "EPIDEMIOLOGICAL ANALYSIS",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF005EA4),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Akurasi Model & Prediksi Tren",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF021F29)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Membandingkan data kasus regional yang dilaporkan dengan hasil probabilistik jaringan saraf kami untuk siklus orbital berikutnya.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun FilterAndRegionRow(
    selectedTab: Int,
    selectedRegion: String,
    regions: List<String>,
    onTabSelected: (Int) -> Unit,
    onRegionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tab Switcher
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFEBF6FF)
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                val tabs = listOf("Harian", "Mingguan", "Bulanan")
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Button(
                        onClick = { onTabSelected(index) },
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFF005EA4) else Color.Transparent,
                            contentColor = if (isSelected) Color.White else Color.Gray
                        ),
                        contentPadding = PaddingValues(0.dp),
                        elevation = null
                    ) {
                        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Filter Icon
        Icon(
            painter = painterResource(id = R.drawable.filter),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color(0xFF005EA4).copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Region Text with Dropdown
        Box {
            Column(
                modifier = Modifier.clickable { expanded = true },
                horizontalAlignment = Alignment.Start
            ) {
                Text("Wilayah:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        selectedRegion,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF005EA4)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.dropdown),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color(0xFF005EA4)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                regions.forEach { region ->
                    DropdownMenuItem(
                        text = { Text(region) },
                        onClick = {
                            onRegionSelected(region)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CaseTrajectoryCard(avgError: String, chartData: List<ChartPoint>) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val marker = rememberMarker()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(chartData) {
        if (chartData.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries {
                    series(chartData.map { it.actual })
                    series(chartData.map { it.prediction })
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "Lintasan Kasus",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF021F29)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFF005EA4)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kasus Aktual", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(20.dp))
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF81C784).copy(alpha = 0.2f))
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF81C784))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kasus Prediksi", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("RATA-RATA ERROR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(avgError, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF126D27))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Chart Visualization
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                val scrollState = rememberVicoScrollState(scrollEnabled = false)
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberLineCartesianLayer(
                            lines = listOf(
                                remember {
                                    LineCartesianLayer.LineSpec(
                                        shader = DynamicShader.color(Color(0xFF005EA4)),
                                        thicknessDp = 2f
                                    )
                                },
                                remember {
                                    LineCartesianLayer.LineSpec(
                                        shader = DynamicShader.color(Color(0xFF81C784)),
                                        thicknessDp = 2f
                                    )
                                },
                            ),
                        ),
                        bottomAxis = rememberBottomAxis(
                            labelRotationDegrees = 0f,
                            valueFormatter = { x, _, _ -> chartData.getOrNull(x.toInt())?.label ?: "" }
                        ),
                    ),
                    modelProducer = modelProducer,
                    marker = marker,
                    markerVisibilityListener = object : CartesianMarkerVisibilityListener {
                        override fun onShown(
                            marker: CartesianMarker,
                            targets: List<CartesianMarker.Target>,
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }

                        override fun onMoved(
                            marker: CartesianMarker,
                            targets: List<CartesianMarker.Target>,
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }

                        override fun onHidden(marker: CartesianMarker) {}
                    },
                    scrollState = scrollState,
                    runInitialAnimation = true,
                    diffAnimationSpec = tween(durationMillis = 1000),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun rememberMarker(): CartesianMarker {
    val label = rememberTextComponent(
        color = Color.Black,
        background = rememberShapeComponent(
            shape = Shape.Rectangle,
            color = Color.White,
        ).apply {
            setShadow(radius = 4f, dy = 2f)
        },
        padding = Dimensions.of(8.dp, 4.dp),
    )
    return remember(label) {
        DefaultCartesianMarker(
            label = label,
            guideline = LineComponent(
                color = android.graphics.Color.LTGRAY,
                thicknessDp = 1f
            ),
        )
    }
}

@Composable
fun ModelInsightCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF005EA4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.idea),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GrafikScreenPreview() {
    CovidPredictTheme {
        GrafikScreen(viewModel = GrafikViewModel())
    }
}
