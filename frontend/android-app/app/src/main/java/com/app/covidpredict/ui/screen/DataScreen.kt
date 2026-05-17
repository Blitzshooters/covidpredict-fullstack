package com.app.covidpredict.ui.screen

import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.text.SimpleDateFormat
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.covidpredict.R
import com.app.covidpredict.theme.CovidPredictTheme
import com.app.covidpredict.viewmodels.DataViewModel
import com.app.covidpredict.viewmodels.HistoryData
import com.app.covidpredict.viewmodels.SharedViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DataScreen(
    viewModel: DataViewModel,
    sharedViewModel: SharedViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedLocation by sharedViewModel.selectedLocation.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FBFE)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            DataHeader()
        }

        item {
            DataSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) }
            )
        }

        item {
            FilterSection(
                selectedLocation = selectedLocation,
                locations = sharedViewModel.locations,
                selectedFilter = uiState.selectedFilter,
                onLocationChange = { sharedViewModel.updateLocation(it) },
                onFilterChange = { viewModel.onFilterChange(it) }
            )
        }

        item {
            NationalOverviewCard(
                avgCases = uiState.avgDailyCases,
                recoveryRate = uiState.recoveryRate,
                location = selectedLocation
            )
        }

        item {
            HistorySectionHeader()
        }

        stickyHeader {
            TableHeader()
        }

        items(uiState.filteredHistoryList) { data ->
            DataRecordRow(data)
        }

        item {
            DataLoadingFooter()
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DataHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Data Historis",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF021F29)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Surface(
            color = Color(0xFFD7F2DE),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF126D27))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Siaran Langsung",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF126D27),
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Cari berdasarkan tanggal atau data...",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFEBF6FF),
            unfocusedContainerColor = Color(0xFFEBF6FF),
            disabledContainerColor = Color(0xFFEBF6FF),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
fun FilterSection(
    selectedLocation: String,
    locations: List<String>,
    selectedFilter: String,
    onLocationChange: (String) -> Unit,
    onFilterChange: (String) -> Unit
) {
    var showFilterMenu by remember { mutableStateOf(false) }
    var showLocationMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val filterOptions = listOf("7 Hari Terakhir", "30 Hari Terakhir", "Custom")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1.2f)) {
            Button(
                onClick = { showLocationMenu = true },
                modifier = Modifier.fillMaxWidth().height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005EA4)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.map),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(selectedLocation, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            DropdownMenu(
                expanded = showLocationMenu,
                onDismissRequest = { showLocationMenu = false }
            ) {
                locations.forEach { location ->
                    DropdownMenuItem(
                        text = { Text(location) },
                        onClick = {
                            showLocationMenu = false
                            onLocationChange(location)
                        }
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.weight(1.3f).height(40.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFCDE8F9)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.calender),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF005EA4)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    selectedFilter,
                    color = Color(0xFF005EA4),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }

        Box {
            Surface(
                modifier = Modifier.size(40.dp).clickable { showFilterMenu = true },
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFCDE8F9)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.filter),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF005EA4)
                    )
                }
            }

            DropdownMenu(
                expanded = showFilterMenu,
                onDismissRequest = { showFilterMenu = false }
            ) {
                filterOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            showFilterMenu = false
                            if (option == "Custom") {
                                showDatePicker = true
                            } else {
                                onFilterChange(option)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        CustomDateRangePicker(
            onDismiss = { showDatePicker = false },
            onDateSelected = { start, end ->
                onFilterChange("$start - $end")
                showDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateRangePicker(
    onDismiss: () -> Unit,
    onDateSelected: (String, String) -> Unit
) {
    // Gunakan UTC timezone agar sinkron dengan DateRangePicker Material 3
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    var displayedMonthMillis by remember { mutableLongStateOf(calendar.timeInMillis) }
    var selectedStartDate by remember { mutableStateOf<Long?>(null) }
    var selectedEndDate by remember { mutableStateOf<Long?>(null) }

    val months = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val years = (2020..2030).map { it.toString() }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedStartDate != null && selectedEndDate != null) {
                        onDateSelected(
                            convertMillisToDate(selectedStartDate!!),
                            convertMillisToDate(selectedEndDate!!)
                        )
                    }
                },
                enabled = selectedStartDate != null && selectedEndDate != null
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // --- Custom Headline ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Pilih Rentang Tanggal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (selectedStartDate != null && selectedEndDate != null) {
                        "${formatDateUI(selectedStartDate!!)} - ${formatDateUI(selectedEndDate!!)}"
                    } else {
                        "Mulai - Selesai"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            HorizontalDivider()

            key(displayedMonthMillis) {
                val dateRangePickerState = rememberDateRangePickerState(
                    initialDisplayedMonthMillis = displayedMonthMillis,
                    initialSelectedStartDateMillis = selectedStartDate,
                    initialSelectedEndDateMillis = selectedEndDate
                )

                LaunchedEffect(dateRangePickerState.selectedStartDateMillis, dateRangePickerState.selectedEndDateMillis) {
                    selectedStartDate = dateRangePickerState.selectedStartDateMillis
                    selectedEndDate = dateRangePickerState.selectedEndDateMillis
                }

                val currentCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    timeInMillis = dateRangePickerState.displayedMonthMillis
                }

                // --- Dropdown Selector UI ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var monthExpanded by remember { mutableStateOf(false) }
                    var yearExpanded by remember { mutableStateOf(false) }

                    // Month Dropdown
                    ExposedDropdownMenuBox(
                        expanded = monthExpanded,
                        onExpandedChange = { monthExpanded = it },
                        modifier = Modifier.weight(1.2f)
                    ) {
                        OutlinedTextField(
                            value = months[currentCal.get(Calendar.MONTH)],
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) },
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = monthExpanded,
                            onDismissRequest = { monthExpanded = false }
                        ) {
                            months.forEachIndexed { index, name ->
                                DropdownMenuItem(
                                    text = { Text(name, style = MaterialTheme.typography.bodySmall) },
                                    onClick = {
                                        currentCal.set(Calendar.MONTH, index)
                                        displayedMonthMillis = currentCal.timeInMillis
                                        monthExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Year Dropdown
                    ExposedDropdownMenuBox(
                        expanded = yearExpanded,
                        onExpandedChange = { yearExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = currentCal.get(Calendar.YEAR).toString(),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = yearExpanded,
                            onDismissRequest = { yearExpanded = false }
                        ) {
                            years.forEach { yearStr ->
                                DropdownMenuItem(
                                    text = { Text(yearStr, style = MaterialTheme.typography.bodySmall) },
                                    onClick = {
                                        currentCal.set(Calendar.YEAR, yearStr.toInt())
                                        displayedMonthMillis = currentCal.timeInMillis
                                        yearExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                DateRangePicker(
                    state = dateRangePickerState,
                    modifier = Modifier.height(340.dp),
                    showModeToggle = false,
                    title = null,
                    headline = null
                )
            }
        }
    }
}

// Fungsi helper untuk UI
fun formatDateUI(millis: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    formatter.timeZone = TimeZone.getTimeZone("UTC") // Sangat penting agar tanggal tidak meleset 1 hari
    return formatter.format(Date(millis))
}

private fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    return formatter.format(Date(millis))
}

@Composable
fun NationalOverviewCard(avgCases: String, recoveryRate: String, location: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "WAWASAN WILAYAH",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Ikhtisar $location",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF021F29)
                    )
                }
                Icon(
                    painter = painterResource(id = R.drawable.prediksi),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = Color(0xFF005EA4)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFEBF6FF),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "RATA-RATA KASUS\nHARIAN",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Gray,
                            lineHeight = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            avgCases,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF021F29)
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFEBF6FF),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "TINGKAT\nKESEMBUHAN",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Gray,
                            lineHeight = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            recoveryRate,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF126D27)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistorySectionHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "RIWAYAT DATA",
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Ekspor CSV",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF005EA4),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DataRecordRow(data: HistoryData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(data.date, modifier = Modifier.weight(1.3f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF021F29))
            Text(data.positive, modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF021F29))
            Text(data.recovered, modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF126D27))
            Text(data.deaths, modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB02528))
        }
    }
}

@Composable
fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FBFE))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("TANGGAL", modifier = Modifier.weight(1.3f), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
        Text("POSITIF", modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
        Text("SEMBUH", modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
        Text("MENINGGAL", modifier = Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
    }
}

fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000)
        ),
        label = "shimmer"
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFEBEBF4),
                Color(0xFFDEDEE7),
                Color(0xFFEBEBF4),
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    )
        .onGloballyPositioned {
            size = it.size
        }
}

@Composable
fun DataLoadingFooter() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shimmerEffect(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {}
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Mengambil data historis...",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color.Gray,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DataScreenPreview() {
    CovidPredictTheme {
        DataScreen(
            viewModel = DataViewModel(),
            sharedViewModel = SharedViewModel()
        )
    }
}
