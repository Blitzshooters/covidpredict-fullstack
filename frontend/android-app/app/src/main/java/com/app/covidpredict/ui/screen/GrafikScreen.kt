package com.app.covidpredict.ui.screen

import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.compose.common.shader.color
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.compose.common.shape.dashed
import com.patrykandpatrick.vico.core.common.shape.Shape
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrafikScreen(viewModel: GrafikViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.refreshChartData()
        }
    }

    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing && pullToRefreshState.isRefreshing) {
            delay(800)
            pullToRefreshState.endRefresh()
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { GrafikHeaderSection() }

            item {
                FilterAndRegionRow(
                    selectedTab = uiState.selectedTab,
                    selectedRegion = uiState.selectedRegion,
                    regions = uiState.regions,
                    onTabSelected = viewModel::selectTab,
                    onRegionSelected = viewModel::selectRegion
                )
            }

            item {
                ChartStatusDispatcher(uiState, viewModel)
            }

            item {
                Text(
                    text = "Wawasan Epidemiologi",
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

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            containerColor = Color.White,
            contentColor = Color(0xFF005EA4)
        )
    }
}

@Composable
fun ChartStatusDispatcher(uiState: com.app.covidpredict.viewmodels.GrafikUiState, viewModel: GrafikViewModel) {
    when {
        uiState.isLoading && !uiState.isRefreshing -> {
            Box(modifier = Modifier.fillMaxWidth().height(280.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF005EA4))
            }
        }
        uiState.errorMessage != null -> {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFDECEC)), shape = RoundedCornerShape(12.dp)) {
                Text(text = uiState.errorMessage, modifier = Modifier.padding(16.dp), color = Color(0xFFB02528), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
        uiState.chartData.isEmpty() -> {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(text = "Data grafik belum tersedia.", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }
        else -> {
            // Menggunakan key(refreshCount) agar reset aktifitas (zoom/scroll)
            // terjadi SETELAH data load selesai, bukan saat loading baru dimulai.
            key(uiState.refreshCount) {
                CaseTrajectoryCard(
                    selectedTab = uiState.selectedTab,
                    avgError = uiState.avgError,
                    chartData = uiState.chartData,
                    showActual = uiState.showActual,
                    showPrediction = uiState.showPrediction,
                    onToggleActual = viewModel::toggleActual,
                    onTogglePrediction = viewModel::togglePrediction
                )
            }
        }
    }
}

@Composable
fun GrafikHeaderSection() {
    Column {
        Text("EPIDEMIOLOGICAL ANALYSIS", style = MaterialTheme.typography.labelMedium, color = Color(0xFF005EA4), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Akurasi Model & Prediksi Tren", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF021F29))
        Spacer(modifier = Modifier.height(12.dp))
        Text("Membandingkan data kasus aktual dengan hasil prediksi model SES berdasarkan wilayah dan periode yang dipilih.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, lineHeight = 20.sp)
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

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.width(230.dp), shape = RoundedCornerShape(14.dp), color = Color(0xFFE3F4FF)) {
            Row(modifier = Modifier.padding(4.dp).height(36.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf("Harian", "Mingguan", "Bulanan").forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF006DB2) else Color.Transparent)
                            .clickable { onTabSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color(0xFF263238), maxLines = 1)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))
        Icon(painterResource(id = R.drawable.filter), null, modifier = Modifier.size(18.dp), tint = Color(0xFF005EA4).copy(alpha = 0.7f))
        Spacer(modifier = Modifier.width(6.dp))

        Box(modifier = Modifier.width(100.dp)) {
            Column(modifier = Modifier.fillMaxWidth().clickable { expanded = true }) {
                Text("Wilayah:", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = selectedRegion, modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF005EA4), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Icon(painterResource(id = R.drawable.dropdown), null, modifier = Modifier.size(10.dp), tint = Color(0xFF005EA4))
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                regions.forEach { region ->
                    DropdownMenuItem(text = { Text(text = region, maxLines = 1, overflow = TextOverflow.Ellipsis) }, onClick = { onRegionSelected(region); expanded = false })
                }
            }
        }
    }
}

@Composable
fun CaseTrajectoryCard(
    selectedTab: Int,
    avgError: String,
    chartData: List<ChartPoint>,
    showActual: Boolean,
    showPrediction: Boolean,
    onToggleActual: () -> Unit,
    onTogglePrediction: () -> Unit
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val haptic = LocalHapticFeedback.current

    val axisSpacing = remember(selectedTab, chartData.size) {
        when {
            selectedTab == 0 && chartData.size > 14 -> 3
            selectedTab == 0 && chartData.size > 7  -> 2
            else -> 1
        }
    }

    val itemPlacer = remember(axisSpacing) {
        com.patrykandpatrick.vico.core.cartesian.axis.AxisItemPlacer.Horizontal.default(
            spacing = axisSpacing,
            addExtremeLabelPadding = true
        )
    }

    val lineColors = remember(showActual, showPrediction) {
        when {
            showActual && showPrediction -> listOf(Color(0xFF005EA4), Color(0xFF81C784))
            showActual -> listOf(Color(0xFF005EA4))
            showPrediction -> listOf(Color(0xFF81C784))
            else -> listOf(Color(0xFF005EA4))
        }
    }

    // Key untuk paksa recompose layer saat toggle berubah
    val layerKey = "${showActual}_${showPrediction}"

    LaunchedEffect(chartData, showActual, showPrediction) {
        if (chartData.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries {
                    if (showActual) series(chartData.map { it.actual })
                    if (showPrediction) series(chartData.map { it.prediction })
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
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("Lintasan Kasus", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFF021F29))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LegendItem(
                            label = "Kasus Aktual",
                            color = Color(0xFF005EA4),
                            isSelected = showActual,
                            onClick = {
                                if (showPrediction) {
                                    // Optimization: Move haptic outside if logic is simple
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onToggleActual()
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        LegendItem(
                            label = "Kasus Prediksi",
                            color = Color(0xFF81C784),
                            isSelected = showPrediction,
                            onClick = {
                                if (showActual) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onTogglePrediction()
                                }
                            }
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("RATA-RATA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 0.5.sp)
                    Text("ERROR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = avgError, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF126D27))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                val scrollState = rememberVicoScrollState()

                val zoomState = rememberVicoZoomState(
                    zoomEnabled = true,
                    initialZoom = Zoom.Content,
                    minZoom = Zoom.Content,
                    maxZoom = Zoom.static(3f)
                )

                val marker = rememberCustomMarker(chartData, showActual, showPrediction)

                CartesianChartHost(
                    chart = rememberCartesianChart(
                        key(layerKey) {
                            rememberLineCartesianLayer(
                                lines = remember(showActual, showPrediction) {
                                    lineColors.map { color ->
                                        LineCartesianLayer.LineSpec(
                                            shader = DynamicShader.color(color),
                                            thicknessDp = 2.5f
                                        )
                                    }
                                }
                            )
                        },
                        startAxis = rememberStartAxis(
                            label = rememberTextComponent(
                                color = Color(0xFF9E9E9E),
                                textSize = 9.sp,
                                padding = Dimensions.of(end = 6.dp)
                            ),
                            guideline = null,
                            tick = null,
                            itemPlacer = remember {
                                com.patrykandpatrick.vico.core.cartesian.axis.AxisItemPlacer.Vertical.count(
                                    count = { 4 }
                                )
                            }
                        ),
                        bottomAxis = rememberBottomAxis(
                            label = rememberTextComponent(
                                color = Color(0xFF7A7A7A),
                                textSize = 9.sp,
                                padding = Dimensions.of(top = 6.dp)
                            ),
                            labelRotationDegrees = 0f,
                            valueFormatter = { x, _, _ ->
                                chartData.getOrNull(x.toInt())?.axisLabel ?: ""
                            },
                            itemPlacer = itemPlacer,
                            tick = null,
                            guideline = rememberLineComponent(
                                color = Color(0xFF9E9E9E).copy(alpha = 0.55f),
                                thickness = 1.dp,
                                shape = Shape.dashed(
                                    shape = Shape.Rectangle,
                                    dashLength = 4.dp,
                                    gapLength = 4.dp
                                )
                            )
                        ),
                    ),
                    modelProducer = modelProducer,
                    marker = marker,
                    markerVisibilityListener = object : CartesianMarkerVisibilityListener {
                        override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        @Deprecated("Deprecated in base interface")
                        override fun onMoved(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                            //haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        override fun onHidden(marker: CartesianMarker) {
                            if (marker is CustomMarker) {
                                marker.resetAnimation()
                            }
                        }
                    },
                    scrollState = scrollState,
                    zoomState = zoomState,
                    runInitialAnimation = true,
                    diffAnimationSpec = tween(durationMillis = 350),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Gunakan dua jari untuk memperbesar area tertentu",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Surface(modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onClick() }, color = if (isSelected) color.copy(alpha = 0.1f) else Color.Transparent, border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(if (isSelected) color else Color.Gray.copy(alpha = 0.5f)))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = if (isSelected) Color(0xFF021F29) else Color.Gray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
        }
    }
}

@Composable
fun rememberCustomMarker(
    chartData: List<ChartPoint>,
    showActual: Boolean,
    showPrediction: Boolean
): CustomMarker {
    return remember(chartData, showActual, showPrediction) {
        CustomMarker(
            showActual = showActual,
            showPrediction = showPrediction
        ) { index ->
            chartData.getOrNull(index)?.let {
                Triple(it.tooltipLabel, it.actual, it.prediction)
            }
        }
    }
}

@Composable
fun ModelInsightCard(title: String, content: String) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF005EA4)), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
            Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.2f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painterResource(id = R.drawable.idea), null, modifier = Modifier.size(24.dp), tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = content, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f), lineHeight = 18.sp)
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
