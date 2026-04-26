package com.app.covidpredict.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.app.covidpredict.R
import com.app.covidpredict.ui.theme.CovidPredictTheme
import com.app.covidpredict.ui.viewmodels.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToPrediction: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F8FF)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            LocationRow(lastUpdated = uiState.lastUpdated)
        }

        item {
            ConfirmedCard(
                confirmed = uiState.confirmed,
                todayIncrease = uiState.todayIncrease
            )
        }

        item {
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

        item {
            ShortcutSection(
                trend = uiState.trendPercent,
                trendStatus = uiState.trendStatus,
                confidence = uiState.modelConfidence,
                onNavigateToPrediction = onNavigateToPrediction
            )
        }

        item {
            PredictionSection()
        }

        item {
            ArticleCard()
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun LocationRow(
    lastUpdated: String,
    onLocationChange: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf("Nasional") }

    val locations = listOf(
        "Nasional",
        "DKI Jakarta",
        "Jawa Barat",
        "Jawa Tengah",
        "Jawa Timur",
        "Bali"
    )

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
                            selectedLocation = location
                            expanded = false
                            onLocationChange(location) // 🔥 penting buat integrasi nanti
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
fun ConfirmedCard(confirmed: String, todayIncrease: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box (modifier = Modifier.fillMaxWidth()) {
            // Background decoration (exclamation mark corner)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(70.dp),
                color = Color(0xFFFDECEC),
                shape = RoundedCornerShape(bottomStart = 70.dp)
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    Text(
                        "!",
                        color = Color(0xFFB02528).copy(alpha = 0.6f),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 4.dp, end = 16.dp)
                    )
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
                Text(
                    text = confirmed,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFFB02528),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 38.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_trend_up),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFB02528)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = todayIncrease,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFB02528),
                        fontWeight = FontWeight.Bold
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
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
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
    Card(
        modifier = Modifier.fillMaxWidth(),
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
fun PredictionSection() {
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
                modifier = Modifier.weight(1f)
            )
            PredictionCard(
                title = "Analisis Lab",
                desc = "Jelajahi kumpulan data dasar yang digunakan untuk prediksi.",
                iconRes = R.drawable.lab,
                containerColor = Color(0xFFCDE8F9),
                contentColor = Color(0xFF021F29),
                iconBgColor = Color(0xFF8BCEF7).copy(alpha = 0.5f),
                modifier = Modifier.weight(1f)
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
    modifier: Modifier
) {
    Card(
        modifier = modifier.height(200.dp),
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
        DashboardScreen(viewModel = DashboardViewModel())
    }
}
