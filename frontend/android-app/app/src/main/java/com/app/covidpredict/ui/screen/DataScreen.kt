package com.app.covidpredict.ui.screen

import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.text.SimpleDateFormat
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import java.text.NumberFormat
import com.app.covidpredict.R
import com.app.covidpredict.theme.CovidPredictTheme
import com.app.covidpredict.viewmodels.*
import kotlinx.coroutines.launch

// Optimization: Pre-create expensive formatters as top-level private constants
// This avoids repeated object allocations during recompositions and animations.
private val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val outputSameYear = SimpleDateFormat("dd MMM", Locale("id", "ID"))
private val outputFull = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
private val idNumberFormat = NumberFormat.getNumberInstance(Locale("id", "ID"))
private val uiDateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}
private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}
private val exportDateFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DataScreen(
    viewModel: DataViewModel,
    sharedViewModel: SharedViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedLocation by sharedViewModel.selectedLocation.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                val csvData = viewModel.generateCsv()

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvData.toByteArray(Charsets.UTF_8))
                }

                Toast.makeText(
                    context,
                    "CSV berhasil diekspor",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Gagal ekspor CSV: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun exportCsv() {
        val dateNow = exportDateFormat.format(Date())

        val locationName = if (selectedLocation == "Nasional") {
            "Indonesia"
        } else {
            selectedLocation
        }.replace(" ", "_")

        val fileName = "covid_${locationName}_$dateNow.csv"

        exportLauncher.launch(fileName)
    }

    val pullToRefreshState = rememberPullToRefreshState()

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(pullToRefreshState.isRefreshing) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.refreshData(sharedViewModel.getApiLocation())
        }
    }

    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing && pullToRefreshState.isRefreshing) {
            pullToRefreshState.endRefresh()
        }
    }

    val showFab by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 15 }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(
                    onClick = { scope.launch { listState.animateScrollToItem(0) } },
                    containerColor = Color(0xFF005EA4),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Scroll to top")
                }
            }
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .nestedScroll(pullToRefreshState.nestedScrollConnection)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF8FBFE)),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 0.dp,
                        bottom = 72.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item(key = "header") { DataHeader() }

                    item(key = "search_bar") {
                        DataSearchBar(
                            query = uiState.searchQuery,
                            onQueryChange = { viewModel.onSearchQueryChange(it) }
                        )
                    }

                    item(key = "filter_section") {
                        FilterSection(
                            selectedLocation = selectedLocation,
                            locations = sharedViewModel.locations,
                            selectedFilter = uiState.selectedFilter,
                            onLocationChange = {
                                sharedViewModel.updateLocation(it)
                                viewModel.onLocationChange(sharedViewModel.getApiLocation())
                            },
                            onFilterChange = { viewModel.onFilterChange(it) }
                        )
                    }

                    item(key = "overview_card") {
                        NationalOverviewCard(
                            avgCases = uiState.avgDailyCases,
                            recoveryRate = uiState.recoveryRate,
                            location = selectedLocation
                        )
                    }

                    item(key = "history_header") {
                        HistorySectionHeader(
                            onExportClick = { exportCsv() }
                        )
                    }

                    stickyHeader(key = "table_header") {
                        TableHeader(
                            sortColumn = uiState.sortColumn,
                            sortOrder = uiState.sortOrder,
                            onSort = { viewModel.onSort(it) }
                        )
                    }

                    if (uiState.isLoading) {
                        item(key = "loading_skeleton") { DataLoadingSkeleton() }
                    } else if (uiState.paginatedList.isEmpty()) {
                        item(key = "empty_state") { EmptyDataState() }
                    } else {
                        // Optimization: Use keys for items in LazyColumn to improve scrolling performance
                        itemsIndexed(
                            items = uiState.paginatedList,
                            key = { _, data -> data.date }
                        ) { index, data ->
                            DataRecordRow(
                                data = data,
                                isEven = index % 2 == 0
                            )
                        }

                        if (uiState.totalPages > 1) {
                            item(key = "pagination_control") {
                                PaginationControl(
                                    currentPage = uiState.currentPage,
                                    totalPages = uiState.totalPages,
                                    onPageChange = { viewModel.onPageChange(it) }
                                )
                            }
                        }
                    }
                }

                PullToRefreshContainer(
                    state = pullToRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = Color.White,
                    contentColor = Color(0xFF005EA4)
                )
            }
        }
    )
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
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF126D27)))
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
fun DataSearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Cari berdasarkan tanggal atau data...", color = Color.Gray, fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
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

fun formatFilterRange(filter: String): String {
    return try {
        val dates = filter.split(" - ")
        val start = dates.getOrNull(0) ?: return filter
        val end = dates.getOrNull(1) ?: return filter

        val startDate = inputFormat.parse(start)
        val endDate = inputFormat.parse(end)

        if (startDate != null && endDate != null) {
            "${outputSameYear.format(startDate)} - ${outputFull.format(endDate)}"
        } else {
            filter
        }
    } catch (e: Exception) {
        filter
    }
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

    // Optimization: remember static list
    val filterOptions = remember { listOf("7 Hari Terakhir", "30 Hari Terakhir", "Semua Data", "Custom") }

    // Optimization: avoid re-formatting unless selectedFilter changes
    val displayFilter = remember(selectedFilter) {
        if (selectedFilter.contains(" - ")) {
            formatFilterRange(selectedFilter)
        } else {
            selectedFilter
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
// ... existing code ...
        Box(modifier = Modifier.weight(1.2f)) {
            Button(
                onClick = { showLocationMenu = true },
                modifier = Modifier.fillMaxWidth().height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005EA4)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.map), null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(selectedLocation, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
            DropdownMenu(expanded = showLocationMenu, onDismissRequest = { showLocationMenu = false }) {
                locations.forEach { location ->
                    DropdownMenuItem(text = { Text(location) }, onClick = {
                        showLocationMenu = false
                        onLocationChange(location)
                    })
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
                Icon(painterResource(R.drawable.calender), null, modifier = Modifier.size(16.dp), tint = Color(0xFF005EA4))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    displayFilter,
                    color = Color(0xFF005EA4),
                    fontSize = 10.sp,
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
                    Icon(painterResource(R.drawable.filter), null, modifier = Modifier.size(20.dp), tint = Color(0xFF005EA4))
                }
            }
            DropdownMenu(expanded = showFilterMenu, onDismissRequest = { showFilterMenu = false }) {
                filterOptions.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = {
                        showFilterMenu = false
                        if (option == "Custom") showDatePicker = true else onFilterChange(option)
                    })
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

@Composable
fun TableHeader(
    sortColumn: SortColumn,
    sortOrder: SortOrder,
    onSort: (SortColumn) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FBFE))
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SortableHeaderCell("TANGGAL", Modifier.weight(1.6f), SortColumn.TANGGAL, sortColumn, sortOrder, onSort)
        SortableHeaderCell("POSITIF", Modifier.weight(1.3f), SortColumn.POSITIF, sortColumn, sortOrder, onSort, textAlign = TextAlign.End)
        SortableHeaderCell("SEMBUH", Modifier.weight(1.3f), SortColumn.SEMBUH, sortColumn, sortOrder, onSort, textAlign = TextAlign.End)
        SortableHeaderCell("MENINGGAL", Modifier.weight(1.3f), SortColumn.MENINGGAL, sortColumn, sortOrder, onSort, textAlign = TextAlign.End)
    }
}

@Composable
fun RowScope.SortableHeaderCell(
    label: String,
    modifier: Modifier,
    column: SortColumn,
    activeSortColumn: SortColumn,
    sortOrder: SortOrder,
    onSort: (SortColumn) -> Unit,
    textAlign: TextAlign = TextAlign.Start
) {
    val isActive = column == activeSortColumn
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable { onSort(column) }
            .padding(horizontal = 2.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (textAlign == TextAlign.End) Arrangement.End else Arrangement.Start
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isActive) Color(0xFF005EA4) else Color.Gray,
            textAlign = textAlign
        )
        if (isActive) {
            Icon(
                painter = painterResource(if (sortOrder == SortOrder.DESC) R.drawable.trending_down else R.drawable.trending_up),
                contentDescription = null,
                modifier = Modifier.size(10.dp).padding(start = 2.dp),
                tint = Color(0xFF005EA4)
            )
        }
    }
}

@Composable
fun DataRecordRow(data: HistoryData, isEven: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEven) Color.White else Color(0xFFF9F9F9)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* ripple effect */ }
                .padding(horizontal = 8.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = data.date,
                modifier = Modifier.weight(1.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF021F29)
            )

            // Optimization: remember transition spec to avoid repeated allocations
            val numberTransitionSpec: AnimatedContentTransitionScope<String>.() -> ContentTransform = remember {
                { (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut()) }
            }

            AnimatedContent(
                targetState = data.positive,
                label = "pos",
                transitionSpec = numberTransitionSpec,
                modifier = Modifier.weight(1.3f)
            ) { text ->
                Text(
                    text = text,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF021F29),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            AnimatedContent(
                targetState = data.recovered,
                label = "rec",
                transitionSpec = numberTransitionSpec,
                modifier = Modifier.weight(1.3f)
            ) { text ->
                Text(
                    text = text,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF126D27),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            AnimatedContent(
                targetState = data.deaths,
                label = "death",
                transitionSpec = numberTransitionSpec,
                modifier = Modifier.weight(1.3f)
            ) { text ->
                Text(
                    text = text,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFB02528),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun PaginationControl(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp, bottom = 0.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = { onPageChange(currentPage - 1) },
            enabled = currentPage > 1,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Icon(
                Icons.Default.KeyboardArrowLeft,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text("Prev", fontSize = 10.sp)
        }

        Text(
            text = "Halaman $currentPage dari $totalPages",
            modifier = Modifier.padding(horizontal = 8.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )

        OutlinedButton(
            onClick = { onPageChange(currentPage + 1) },
            enabled = currentPage < totalPages,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text("Next", fontSize = 10.sp)
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun DataLoadingSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(6) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .shimmerEffect()
            )
        }
    }
}

@Composable
fun EmptyDataState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Data tidak ditemukan", fontWeight = FontWeight.Bold, color = Color(0xFF021F29))
        Text("Dataset terbatas hingga Sept 2022.", color = Color.Gray, fontSize = 12.sp)
    }
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("WAWASAN WILAYAH", style = MaterialTheme.typography.labelMedium, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("Ikhtisar $location", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFF021F29))
                }
                Icon(painterResource(R.drawable.prediksi), null, modifier = Modifier.size(28.dp), tint = Color(0xFF005EA4))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OverviewItem(
                    label = "RATA-RATA KASUS\nHARIAN",
                    value = avgCases,
                    valueColor = Color(0xFF021F29),
                    modifier = Modifier.weight(1f),
                    isPercent = false
                )

                OverviewItem(
                    label = "TINGKAT\nKESEMBUHAN",
                    value = recoveryRate,
                    valueColor = Color(0xFF126D27),
                    modifier = Modifier.weight(1f),
                    isPercent = true
                )
            }
        }
    }
}

@Composable
fun OverviewItem(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier,
    isPercent: Boolean = false
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFEBF6FF),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Gray,
                lineHeight = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedNumberText(
                value = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
                isPercent = isPercent
            )
        }
    }
}

@Composable
fun AnimatedNumberText(
    value: String,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 22.sp,
    fontWeight: FontWeight = FontWeight.ExtraBold,
    color: Color = Color(0xFF021F29),
    isPercent: Boolean = false
) {
    val numericValue = remember(value, isPercent) {
        if (isPercent) {
            value
                .replace("%", "")
                .replace(",", ".")
                .trim()
                .toFloatOrNull() ?: 0f
        } else {
            value
                .filter { it.isDigit() || it == '-' }
                .toFloatOrNull() ?: 0f
        }
    }

    val animatedValue by animateFloatAsState(
        targetValue = numericValue,
        animationSpec = tween(durationMillis = 1200),
        label = "numberTicker"
    )

    val formatter = remember {
        NumberFormat.getNumberInstance(Locale("id", "ID"))
    }

    val displayText = remember(animatedValue, isPercent) {
        if (isPercent) {
            String.format(Locale("id", "ID"), "%.1f%%", animatedValue)
        } else {
            formatter.format(animatedValue.toInt())
        }
    }

    Text(
        text = displayText,
        modifier = modifier,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color
    )
}

@Composable
fun HistorySectionHeader(
    onExportClick: () -> Unit
) {
// ...
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "RIWAYAT DATA",
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Ekspor CSV",
            modifier = Modifier.clickable { onExportClick() },
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF005EA4),
            fontWeight = FontWeight.Bold
        )
    }
}

fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(animation = tween(1000)),
        label = "shimmer"
    )

    // Optimization: Cache the brush to avoid recreating it on every recomposition/draw
    val brush = remember(startOffsetX, size) {
        Brush.linearGradient(
            colors = listOf(Color(0xFFEBEBF4), Color(0xFFDEDEE7), Color(0xFFEBEBF4)),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    }

    background(brush = brush)
        .onGloballyPositioned { size = it.size }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateRangePicker(onDismiss: () -> Unit, onDateSelected: (String, String) -> Unit) {
    val calendar = remember {
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
    }
    var displayedMonthMillis by remember { mutableLongStateOf(calendar.timeInMillis) }
    var selectedStartDate by remember { mutableStateOf<Long?>(null) }
    var selectedEndDate by remember { mutableStateOf<Long?>(null) }

    // Optimization: remember static lists
    val months = remember { listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember") }
    val years = remember { (2020..2030).map { it.toString() } }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { if (selectedStartDate != null && selectedEndDate != null) onDateSelected(convertMillisToDate(selectedStartDate!!), convertMillisToDate(selectedEndDate!!)) },
                enabled = selectedStartDate != null && selectedEndDate != null
            ) { Text("Simpan", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text("Pilih Rentang Tanggal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = if (selectedStartDate != null && selectedEndDate != null) "${formatDateUI(selectedStartDate!!)} - ${formatDateUI(selectedEndDate!!)}" else "Mulai - Selesai",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
                )
            }
            HorizontalDivider()
            key(displayedMonthMillis) {
                val state = rememberDateRangePickerState(initialDisplayedMonthMillis = displayedMonthMillis, initialSelectedStartDateMillis = selectedStartDate, initialSelectedEndDateMillis = selectedEndDate)
                LaunchedEffect(state.selectedStartDateMillis, state.selectedEndDateMillis) {
                    selectedStartDate = state.selectedStartDateMillis
                    selectedEndDate = state.selectedEndDateMillis
                }

                // Optimization: avoid frequent calendar creation
                val currentCal = remember(state.displayedMonthMillis) {
                    Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = state.displayedMonthMillis }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    var mExp by remember { mutableStateOf(false) }
                    var yExp by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = mExp, onExpandedChange = { mExp = it }, modifier = Modifier.weight(1.2f)) {
                        OutlinedTextField(value = months[currentCal.get(Calendar.MONTH)], onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(mExp) }, modifier = Modifier.menuAnchor(), shape = RoundedCornerShape(8.dp), textStyle = MaterialTheme.typography.bodySmall, singleLine = true)
                        ExposedDropdownMenu(expanded = mExp, onDismissRequest = { mExp = false }) {
                            months.forEachIndexed { index, name -> DropdownMenuItem(text = { Text(name, style = MaterialTheme.typography.bodySmall) }, onClick = { currentCal.set(Calendar.MONTH, index); displayedMonthMillis = currentCal.timeInMillis; mExp = false }) }
                        }
                    }
                    ExposedDropdownMenuBox(expanded = yExp, onExpandedChange = { yExp = it }, modifier = Modifier.weight(1f)) {
                        OutlinedTextField(value = currentCal.get(Calendar.YEAR).toString(), onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(yExp) }, modifier = Modifier.menuAnchor(), shape = RoundedCornerShape(8.dp), textStyle = MaterialTheme.typography.bodySmall, singleLine = true)
                        ExposedDropdownMenu(expanded = yExp, onDismissRequest = { yExp = false }) {
                            years.forEach { yearStr -> DropdownMenuItem(text = { Text(yearStr, style = MaterialTheme.typography.bodySmall) }, onClick = { currentCal.set(Calendar.YEAR, yearStr.toInt()); displayedMonthMillis = currentCal.timeInMillis; yExp = false }) }
                        }
                    }
                }
                DateRangePicker(state = state, modifier = Modifier.height(340.dp), showModeToggle = false, title = null, headline = null)
            }
        }
    }
}

fun formatDateUI(millis: Long): String {
    return uiDateFormat.format(Date(millis))
}

private fun convertMillisToDate(millis: Long): String {
    return apiDateFormat.format(Date(millis))
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
