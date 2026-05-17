package com.app.covidpredict.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.covidpredict.R
import com.app.covidpredict.theme.CovidPredictTheme
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F9FF))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        LocationRow(
            lokasi = state.lokasi,
            lastUpdated = state.lastUpdated
        )

        Spacer(modifier = Modifier.height(28.dp))

        ConfirmedCard(
            confirmed = state.confirmed,
            todayIncrease = state.todayIncrease
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SmallStatCard(
                title = "Sembuh",
                value = state.recovered,
                badgeText = state.recoveredRate,
                badgeColor = Color(0xFFB8F5BE),
                valueColor = Color(0xFF067A22),
                modifier = Modifier.weight(1f)
            )

            SmallStatCard(
                title = "Meninggal",
                value = state.deaths,
                badgeText = state.deathRate,
                badgeColor = Color(0xFFCBEAFA),
                valueColor = Color(0xFF2F3545),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(42.dp))

        SectionTitle(
            title = "Pintasan Sistem",
            actionText = "Lihat model lengkap",
            onActionClick = onNavigateToPrediction
        )

        Spacer(modifier = Modifier.height(16.dp))

        SystemInsightCard(
            trendPercent = state.trendPercent,
            trendStatus = state.trendStatus,
            modelConfidence = state.modelConfidence,
            onClick = onNavigateToPrediction
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Prediksi Aktif",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PredictionMenuCard(
                title = "Data Historis",
                description = "Akses arsip lengkap wabah masa lalu dan statistik regional.",
                iconRes = R.drawable.data,
                backgroundColor = Color(0xFF086CB2),
                iconBackground = Color(0xFF3897D8),
                contentColor = Color.White,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToData
            )

            PredictionMenuCard(
                title = "Analisis Lab",
                description = "Jelajahi kumpulan data dasar yang digunakan untuk prediksi.",
                iconRes = R.drawable.lab,
                backgroundColor = Color(0xFFC7E8F7),
                iconBackground = Color(0xFF8DD0F2),
                contentColor = Color(0xFF1A1F2B),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToPrediction
            )
        }

        Spacer(modifier = Modifier.height(42.dp))

        ArticleCard()

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun LocationRow(
    lokasi: String,
    lastUpdated: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFE0F3FF)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.map),
                    contentDescription = null,
                    tint = Color(0xFF006BB8),
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = lokasi,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    painter = painterResource(id = R.drawable.dropdown),
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .size(7.dp)
                .background(Color(0xFF16863D), CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Terakhir diperbarui: $lastUpdated",
            fontSize = 11.sp,
            color = Color(0xFF273142),
            lineHeight = 15.sp,
            maxLines = 2,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ConfirmedCard(
    confirmed: String,
    todayIncrease: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(148.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 24.dp, top = 26.dp, bottom = 22.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Kasus Terkonfirmasi",
                        fontSize = 15.sp,
                        color = Color(0xFF141927)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = confirmed,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC52C30),
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.trending_up),
                        contentDescription = null,
                        tint = Color(0xFFC52C30),
                        modifier = Modifier.size(15.dp)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(
                        text = todayIncrease,
                        fontSize = 13.sp,
                        color = Color(0xFFC52C30)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(68.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFFFF0F0)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .rotate(45f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE9C8C8)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "!",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.rotate(-45f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallStatCard(
    title: String,
    value: String,
    badgeText: String,
    badgeColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(132.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                color = Color(0xFF111827),
                maxLines = 1
            )

            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                softWrap = false
            )

            Surface(
                shape = RoundedCornerShape(50),
                color = badgeColor
            ) {
                Text(
                    text = badgeText,
                    fontSize = 10.sp,
                    color = Color(0xFF0B5F2A),
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = actionText,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF005BBE),
            modifier = Modifier.clickable { onActionClick() }
        )
    }
}

@Composable
private fun SystemInsightCard(
    trendPercent: String,
    trendStatus: String,
    modelConfidence: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(216.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F4FF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 26.dp, top = 26.dp, end = 26.dp, bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Tren Kemungkinan (7 Hari Ke\nDepan)",
                    fontSize = 16.sp,
                    color = Color.Black,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.prediksi),
                        contentDescription = null,
                        tint = Color(0xFF08752B),
                        modifier = Modifier.size(38.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = trendPercent,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF08752B),
                            maxLines = 1
                        )

                        Text(
                            text = trendStatus,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF08752B),
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Kepercayaan model: $modelConfidence\nberdasarkan data vaksinasi\ndan mobilitas terbaru.",
                    fontSize = 12.sp,
                    color = Color(0xFF3A4656),
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(start = 52.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(width = 68.dp, height = 68.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
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

@Composable
private fun PredictionMenuCard(
    title: String,
    description: String,
    iconRes: Int,
    backgroundColor: Color,
    iconBackground: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(174.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 22.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    tint = contentColor,
                    modifier = Modifier.size(27.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                fontSize = 11.sp,
                color = contentColor.copy(alpha = 0.85f),
                lineHeight = 15.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ArticleCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.artikel1)
                    .size(900, 500)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(horizontal = 26.dp, vertical = 22.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFEAF7ED),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text(
                            text = "KESEHATAN PUBLIK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF136C28),
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "5 menit baca",
                        fontSize = 11.sp,
                        color = Color(0xFF4B5563)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Memahami Dampak Pergeseran Musiman terhadap Penyebaran Virus",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    lineHeight = 24.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Studi terbaru menunjukkan bahwa faktor lingkungan terus memainkan peran halus namun...",
                    fontSize = 13.sp,
                    color = Color(0xFF4B5563),
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
            sharedViewModel = SharedViewModel(),
            onNavigateToPrediction = {},
            onNavigateToData = {}
        )
    }
}