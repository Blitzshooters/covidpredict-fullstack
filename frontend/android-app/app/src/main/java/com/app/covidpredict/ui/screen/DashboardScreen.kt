package com.app.covidpredict.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.app.covidpredict.R
import com.app.covidpredict.theme.CovidPredictTheme
import com.app.covidpredict.viewmodels.DashboardViewModel
import com.app.covidpredict.viewmodels.SharedViewModel
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    sharedViewModel: SharedViewModel,
    onNavigateToPrediction: () -> Unit = {},
    onNavigateToData: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedLocation by sharedViewModel.selectedLocation.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Re-fetch data when location changes
    LaunchedEffect(selectedLocation) {
        viewModel.fetchDashboardData(sharedViewModel.getApiLocation())
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refreshData(sharedViewModel.getApiLocation())
        }
    }

    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing) {
            pullToRefreshState.endRefresh()
        }
    }

    // Staggered Animation State
    var visibleItems by remember { mutableIntStateOf(0) }
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            for (i in 1..7) {
                visibleItems = i
                delay(100)
            }
        }
    }

    Box(modifier = Modifier.nestedScroll(pullToRefreshState.nestedScrollConnection)) {
        if (uiState.isLoading && !uiState.isRefreshing) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF005EA4))
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F8FF)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                AnimatedVisibility(
                    visible = visibleItems >= 1,
                    enter = fadeIn() + slideInVertically { it / 2 }
                ) {
                    LocationRow(
                        lastUpdated = uiState.lastUpdated,
                        selectedLocation = selectedLocation,
                        locations = sharedViewModel.locations,
                        onLocationChange = { sharedViewModel.updateLocation(it) }
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = visibleItems >= 2,
                    enter = fadeIn() + slideInVertically { it / 2 }
                ) {
                    ConfirmedCard(
                        confirmed = uiState.confirmed,
                        todayIncrease = uiState.todayIncrease,
                        rawTodayIncrease = uiState.rawTodayIncrease,
                        isNasional = selectedLocation == "Nasional"
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = visibleItems >= 3,
                    enter = fadeIn() + slideInVertically { it / 2 }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SmallStatCard(
                            title = "Sembuh",
                            value = uiState.recovered,
                            percentage = uiState.recoveredRate,
                            chipColor = Color(0xFFD7F2DE),
                            textColor = Color(0xFF126D27),
                            valueColor = Color(0xFF126D27),
                            modifier = Modifier.weight(1f)
                        )

                        SmallStatCard(
                            title = "Meninggal",
                            value = uiState.deaths,
                            percentage = uiState.deathRate,
                            chipColor = Color(0xFFDCE5EC),
                            textColor = Color(0xFF005EA4),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = visibleItems >= 4,
                    enter = fadeIn() + slideInVertically { it / 2 }
                ) {
                    ShortcutSection(
                        trend = uiState.trendPercent,
                        trendStatus = uiState.trendStatus,
                        confidence = uiState.modelConfidence,
                        onNavigateToPrediction = onNavigateToPrediction
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = visibleItems >= 5,
                    enter = fadeIn() + slideInVertically { it / 2 }
                ) {
                    PredictionSection(
                        onNavigateToData = onNavigateToData,
                        onNavigateToPrediction = onNavigateToPrediction
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = visibleItems >= 6,
                    enter = fadeIn() + slideInVertically { it / 2 }
                ) {
                    ArticleCard()
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            containerColor = Color.White,
            contentColor = Color(0xFF005EA4)
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun AnimatedNumber(
    value: String,
    prefix: String = "",
    suffix: String = "",
    style: TextStyle,
    color: Color
) {
    // Hilangkan karakter non-digit kecuali tanda plus/minus di depan
    val numericString = value.filter { it.isDigit() }
    val targetValue = numericString.toLongOrNull() ?: 0L

    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(value) {
        animatedValue.animateTo(
            targetValue.toFloat(),
            animationSpec = tween(durationMillis = 1200)
        )
    }

    val formattedValue = NumberFormat.getInstance(Locale("id", "ID")).format(animatedValue.value.toLong())

    Text(
        text = "$prefix$formattedValue$suffix",
        style = style,
        color = color,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
fun LocationRow(
    lastUpdated: String,
    selectedLocation: String,
    locations: List<String>,
    onLocationChange: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Surface(
                shape = CircleShape,
                color = Color(0xFFCBE6F7),
                modifier = Modifier.clickable { expanded = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.map),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF005EA4)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = selectedLocation,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF005EA4),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(4.dp))

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
                locations.forEach { location ->
                    DropdownMenuItem(
                        text = { Text(location) },
                        onClick = {
                            expanded = false
                            onLocationChange(location)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF126D27))
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = "Terakhir diperbarui: $lastUpdated",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            lineHeight = 14.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ConfirmedCard(
    confirmed: String,
    todayIncrease: String,
    rawTodayIncrease: Int,
    isNasional: Boolean
) {
    val threshold = if (isNasional) 1000 else 300
    val showWarning = rawTodayIncrease > threshold

    // Pulsing animation for warning icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box (modifier = Modifier.fillMaxWidth()) {
            // Warning Icon with Pulsing Effect
            androidx.compose.animation.AnimatedVisibility(
                visible = showWarning,
                enter = fadeIn() + expandIn(expandFrom = Alignment.Center),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFDECEC),
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "!",
                            color = Color(0xFFB02528),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Kasus Terkonfirmasi",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                AnimatedNumber(
                    value = confirmed,
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 38.sp),
                    color = Color(0xFFB02528)
                )

                Spacer(modifier = Modifier.height(12.dp))

                val trendColor = if (rawTodayIncrease >= 0) Color(0xFFE53935) else Color(0xFF43A047)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(
                            id = if (rawTodayIncrease >= 0) R.drawable.trending_up else R.drawable.trending_down
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = trendColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    AnimatedNumber(
                        value = todayIncrease,
                        prefix = if (rawTodayIncrease >= 0) "+" else "",
                        suffix = " Hari Ini",
                        style = MaterialTheme.typography.labelMedium,
                        color = trendColor
                    )
                }
            }
        }
    }
}

@Composable
fun SmallStatCard(
    title: String,
    value: String,
    percentage: String,
    chipColor: Color,
    textColor: Color,
    valueColor: Color = Color(0xFF191A1C),
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            AnimatedNumber(
                value = value,
                style = MaterialTheme.typography.titleLarge,
                color = valueColor
            )

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = chipColor
            ) {
                Text(
                    text = percentage,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun ShortcutSection(
    trend: String,
    trendStatus: String,
    confidence: String,
    onNavigateToPrediction: () -> Unit = {}
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Pintasan Sistem",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF021F29)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Lihat model lengkap",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF005EA4),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToPrediction() }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        TrendCard(trend, trendStatus, confidence, onNavigateToPrediction)
    }
}

@Composable
fun TrendCard(
    trend: String,
    status: String,
    confidence: String,
    onNavigateToPrediction: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onNavigateToPrediction
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFCBE6F7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Tren Kemungkinan (7 Hari Ke Depan)",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF021F29),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.prediksi),
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = Color(0xFF126D27)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = trend,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF126D27),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                        Text(
                            text = status,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF126D27),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = confidence,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    lineHeight = 14.sp
                )
            }
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clickable { onNavigateToPrediction() },
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_right),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF005EA4)
                    )
                }
            }
        }
    }
}

@Composable
fun PredictionSection(
    onNavigateToData: () -> Unit = {},
    onNavigateToPrediction: () -> Unit = {}
) {
    Column {
        Text(
            "Prediksi Aktif",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF021F29)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            PredictionCard(
                title = "Data Historis",
                desc = "Akses arsip lengkap wabah masa lalu dan statistik regional.",
                iconRes = R.drawable.data,
                containerColor = Color(0xFF005EA4),
                contentColor = Color.White,
                iconBgColor = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToData
            )
            PredictionCard(
                title = "Analisis Lab",
                desc = "Jelajahi kumpulan data dasar yang digunakan untuk prediksi.",
                iconRes = R.drawable.lab,
                containerColor = Color(0xFFCDE8F9),
                contentColor = Color(0xFF021F29),
                iconBgColor = Color(0xFF8BCEF7).copy(alpha = 0.5f),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToPrediction
            )
        }
    }
}

@Composable
fun PredictionCard(
    title: String,
    desc: String,
    iconRes: Int,
    containerColor: Color,
    contentColor: Color,
    iconBgColor: Color,
    modifier: Modifier,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "scale"
    )

    Card(
        modifier = modifier
            .height(200.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Matikan ripple default agar micro-interaction terasa lebih halus
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(8.dp),
                color = iconBgColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = if (contentColor == Color.White) Color.White else Color(0xFF005EA4)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.7f),
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun ArticleCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            AsyncImage(
                model = R.drawable.artikel1,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFD7F2DE),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "KESEHATAN PUBLIK",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF126D27),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "5 menit baca",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Memahami Dampak Pergeseran Musiman terhadap Penyebaran Virus",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF021F29)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Studi terbaru menunjukkan bahwa faktor lingkungan terus memainkan peran halus namun...",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    CovidPredictTheme {
        DashboardScreen(
            viewModel = DashboardViewModel(),
            sharedViewModel = SharedViewModel()
        )
    }
}
