package com.app.covidpredict.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.covidpredict.data.repository.DataRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

data class HistoryData(
    val date: String,
    val positive: String,
    val recovered: String,
    val deaths: String,
    val rawDate: Date,
    val rawPositive: Int,
    val rawRecovered: Int,
    val rawDeaths: Int
)

enum class SortColumn {
    TANGGAL,
    POSITIF,
    SEMBUH,
    MENINGGAL
}

enum class SortOrder {
    ASC,
    DESC
}

data class DataUiState(
    val lastUpdated: String = "-",
    val avgDailyCases: String = "0",
    val recoveryRate: String = "0%",
    val historyList: List<HistoryData> = emptyList(),
    val paginatedList: List<HistoryData> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedFilter: String = "30 Hari Terakhir",
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val sortColumn: SortColumn = SortColumn.TANGGAL,
    val sortOrder: SortOrder = SortOrder.DESC
)

class DataViewModel(
    private val repository: DataRepository = DataRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DataUiState())
    val uiState: StateFlow<DataUiState> = _uiState.asStateFlow()

    private var currentWilayah: String = "Indonesia"

    private val itemsPerPage = 30

    private var activeSortColumn: SortColumn = SortColumn.TANGGAL
    private var activeSortOrder: SortOrder = SortOrder.DESC

    init {
        fetchHistory(
            wilayah = "Indonesia",
            days = 30,
            filterLabel = "30 Hari Terakhir"
        )
    }

    fun fetchHistory(
        wilayah: String = currentWilayah,
        days: Int = 30,
        startDate: String? = null,
        endDate: String? = null,
        filterLabel: String? = null,
        showLoading: Boolean = true,
        isRefresh: Boolean = false
    ) {
        currentWilayah = wilayah

        val newFilter = filterLabel ?: _uiState.value.selectedFilter

        viewModelScope.launch {
            if (showLoading) {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null,
                    // Jika refresh, jangan ganti label filter dulu agar transisi halus
                    selectedFilter = if (isRefresh) _uiState.value.selectedFilter else newFilter,
                    currentPage = 1,
                    sortColumn = activeSortColumn,
                    sortOrder = activeSortOrder
                )
            } else if (!isRefresh) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = null,
                    selectedFilter = newFilter,
                    currentPage = 1,
                    sortColumn = activeSortColumn,
                    sortOrder = activeSortOrder
                )
            }

            try {
                val response = repository.getCovidHistory(
                    wilayah = wilayah,
                    days = if (newFilter == "Semua Data") 0 else days,
                    startDate = startDate,
                    endDate = endDate
                )

                val history = response.data.map { item ->
                    val rawDate = parseRawDate(item.tanggal) ?: Date()

                    HistoryData(
                        date = formatDate(item.tanggal),
                        positive = formatNumber(item.positif),
                        recovered = formatNumber(item.sembuh),
                        deaths = formatNumber(item.meninggal),
                        rawDate = rawDate,
                        rawPositive = item.positif,
                        rawRecovered = item.sembuh,
                        rawDeaths = item.meninggal
                    )
                }

                val parsedData = response.data.mapNotNull { item ->
                    val date = parseRawDate(item.tanggal)

                    if (date != null) {
                        item to date
                    } else {
                        null
                    }
                }.sortedBy { it.second }

                val firstData = parsedData.firstOrNull()
                val latestData = parsedData.lastOrNull()

                val latestPositive = latestData?.first?.positif ?: 0
                val latestRecovered = latestData?.first?.sembuh ?: 0

                val recoveryRate = if (latestPositive > 0) {
                    (latestRecovered.toDouble() / latestPositive.toDouble()) * 100.0
                } else {
                    0.0
                }

                val avgDailyCases = if (
                    firstData != null &&
                    latestData != null &&
                    parsedData.size >= 2
                ) {
                    val firstPositive = firstData.first.positif
                    val lastPositive = latestData.first.positif

                    val dayDiff = TimeUnit.MILLISECONDS.toDays(
                        latestData.second.time - firstData.second.time
                    ).toInt().coerceAtLeast(1)

                    ((lastPositive - firstPositive).toDouble() / dayDiff)
                        .roundToInt()
                        .coerceAtLeast(0)
                } else {
                    0
                }

                _uiState.value = _uiState.value.copy(
                    historyList = history,
                    avgDailyCases = formatNumber(avgDailyCases),
                    recoveryRate = String.format(Locale.US, "%.1f%%", recoveryRate),
                    selectedFilter = newFilter,
                    sortColumn = activeSortColumn,
                    sortOrder = activeSortOrder,
                    errorMessage = null
                )

                applySortingAndPagination()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Gagal mengambil data historis"
                )
            } finally {
                _uiState.value = _uiState.value.copy(
                    isLoading = false
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            currentPage = 1,
            sortColumn = activeSortColumn,
            sortOrder = activeSortOrder
        )

        applySortingAndPagination()
    }

    fun onLocationChange(wilayah: String) {
        currentWilayah = wilayah

        onFilterChange(
            filter = _uiState.value.selectedFilter,
            wilayah = wilayah,
            simulateDelay = false
        )
    }

    fun refreshData(wilayah: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRefreshing = true,
                errorMessage = null
            )

            delay(1200) // Delay agar animasi refresh terasa natural

            // Reset sorting logic
            activeSortColumn = SortColumn.TANGGAL
            activeSortOrder = SortOrder.DESC

            fetchHistory(
                wilayah = wilayah,
                days = 30,
                filterLabel = "30 Hari Terakhir",
                showLoading = false,
                isRefresh = true
            )

            _uiState.value = _uiState.value.copy(
                isRefreshing = false,
                searchQuery = "" // Reset pencarian setelah data baru siap
            )
        }
    }

    fun onFilterChange(
        filter: String,
        wilayah: String = currentWilayah,
        simulateDelay: Boolean = true
    ) {
        viewModelScope.launch {
            if (simulateDelay) {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    selectedFilter = filter,
                    currentPage = 1,
                    sortColumn = activeSortColumn,
                    sortOrder = activeSortOrder
                )

                delay(300)
            }

            when (filter) {
                "7 Hari Terakhir" -> {
                    fetchHistory(
                        wilayah = wilayah,
                        days = 7,
                        filterLabel = filter,
                        showLoading = !simulateDelay
                    )
                }

                "30 Hari Terakhir" -> {
                    fetchHistory(
                        wilayah = wilayah,
                        days = 30,
                        filterLabel = filter,
                        showLoading = !simulateDelay
                    )
                }

                "Semua Data" -> {
                    fetchHistory(
                        wilayah = wilayah,
                        days = 0,
                        filterLabel = filter,
                        showLoading = !simulateDelay
                    )
                }

                else -> {
                    if (filter.contains(" - ")) {
                        val dates = filter.split(" - ")
                        val startDate = dates.getOrNull(0)
                        val endDate = dates.getOrNull(1)

                        if (startDate != null && endDate != null) {
                            fetchHistory(
                                wilayah = wilayah,
                                days = 0,
                                startDate = startDate,
                                endDate = endDate,
                                filterLabel = filter,
                                showLoading = !simulateDelay
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "Rentang tanggal tidak valid"
                            )
                        }
                    } else {
                        fetchHistory(
                            wilayah = wilayah,
                            days = 30,
                            filterLabel = "30 Hari Terakhir",
                            showLoading = !simulateDelay
                        )
                    }
                }
            }
        }
    }

    fun onSort(column: SortColumn) {
        val newOrder = if (column == activeSortColumn) {
            if (activeSortOrder == SortOrder.DESC) {
                SortOrder.ASC
            } else {
                SortOrder.DESC
            }
        } else {
            SortOrder.DESC
        }

        activeSortColumn = column
        activeSortOrder = newOrder

        _uiState.value = _uiState.value.copy(
            sortColumn = activeSortColumn,
            sortOrder = activeSortOrder,
            currentPage = 1
        )

        applySortingAndPagination()
    }

    fun onPageChange(page: Int) {
        if (page in 1.._uiState.value.totalPages) {
            _uiState.value = _uiState.value.copy(
                currentPage = page,
                sortColumn = activeSortColumn,
                sortOrder = activeSortOrder
            )

            applySortingAndPagination()
        }
    }

    fun generateCsv(): String {
        val state = _uiState.value

        var list = if (state.searchQuery.isNotEmpty()) {
            state.historyList.filter {
                it.date.contains(state.searchQuery, ignoreCase = true) ||
                        it.positive.contains(state.searchQuery, ignoreCase = true) ||
                        it.recovered.contains(state.searchQuery, ignoreCase = true) ||
                        it.deaths.contains(state.searchQuery, ignoreCase = true)
            }
        } else {
            state.historyList
        }

        list = when (activeSortColumn) {
            SortColumn.TANGGAL -> {
                if (activeSortOrder == SortOrder.DESC) {
                    list.sortedByDescending { it.rawDate }
                } else {
                    list.sortedBy { it.rawDate }
                }
            }

            SortColumn.POSITIF -> {
                if (activeSortOrder == SortOrder.DESC) {
                    list.sortedByDescending { it.rawPositive }
                } else {
                    list.sortedBy { it.rawPositive }
                }
            }

            SortColumn.SEMBUH -> {
                if (activeSortOrder == SortOrder.DESC) {
                    list.sortedByDescending { it.rawRecovered }
                } else {
                    list.sortedBy { it.rawRecovered }
                }
            }

            SortColumn.MENINGGAL -> {
                if (activeSortOrder == SortOrder.DESC) {
                    list.sortedByDescending { it.rawDeaths }
                } else {
                    list.sortedBy { it.rawDeaths }
                }
            }
        }

        val builder = StringBuilder()

        builder.append("Tanggal,Positif,Sembuh,Meninggal\n")

        list.forEach { item ->
            builder.append(csvSafe(formatDateIndonesia(item.rawDate)))
            builder.append(",")
            builder.append(item.rawPositive)
            builder.append(",")
            builder.append(item.rawRecovered)
            builder.append(",")
            builder.append(item.rawDeaths)
            builder.append("\n")
        }

        return builder.toString()
    }

    private fun csvSafe(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    private fun formatDateIndonesia(date: Date): String {
        return try {
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            outputFormat.format(date)
        } catch (e: Exception) {
            "-"
        }
    }

    private fun applySortingAndPagination() {
        val state = _uiState.value

        var list = if (state.searchQuery.isNotEmpty()) {
            state.historyList.filter {
                it.date.contains(state.searchQuery, ignoreCase = true) ||
                        it.positive.contains(state.searchQuery, ignoreCase = true) ||
                        it.recovered.contains(state.searchQuery, ignoreCase = true) ||
                        it.deaths.contains(state.searchQuery, ignoreCase = true)
            }
        } else {
            state.historyList
        }

        list = when (activeSortColumn) {
            SortColumn.TANGGAL -> {
                if (activeSortOrder == SortOrder.DESC) {
                    list.sortedByDescending { it.rawDate }
                } else {
                    list.sortedBy { it.rawDate }
                }
            }

            SortColumn.POSITIF -> {
                if (activeSortOrder == SortOrder.DESC) {
                    list.sortedByDescending { it.rawPositive }
                } else {
                    list.sortedBy { it.rawPositive }
                }
            }

            SortColumn.SEMBUH -> {
                if (activeSortOrder == SortOrder.DESC) {
                    list.sortedByDescending { it.rawRecovered }
                } else {
                    list.sortedBy { it.rawRecovered }
                }
            }

            SortColumn.MENINGGAL -> {
                if (activeSortOrder == SortOrder.DESC) {
                    list.sortedByDescending { it.rawDeaths }
                } else {
                    list.sortedBy { it.rawDeaths }
                }
            }
        }

        val totalItems = list.size

        val totalPages = if (totalItems == 0) {
            1
        } else {
            kotlin.math.ceil(totalItems.toDouble() / itemsPerPage).toInt()
        }

        val currentPage = state.currentPage.coerceIn(1, totalPages)

        val startIndex = (currentPage - 1) * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, totalItems)

        val paginatedList = if (totalItems > 0 && startIndex < totalItems) {
            list.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        _uiState.value = _uiState.value.copy(
            paginatedList = paginatedList,
            totalPages = totalPages,
            currentPage = currentPage,
            sortColumn = activeSortColumn,
            sortOrder = activeSortOrder
        )
    }

    private fun parseRawDate(date: String): Date? {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val dateStr = if (date.length >= 10) {
                date.substring(0, 10)
            } else {
                date
            }

            inputFormat.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }

    private fun formatNumber(value: Int): String {
        return NumberFormat.getNumberInstance(Locale.US).format(value)
    }

    private fun formatDate(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

            val dateStr = if (date.length >= 10) {
                date.substring(0, 10)
            } else {
                date
            }

            val parsedDate = inputFormat.parse(dateStr)
            outputFormat.format(parsedDate!!)
        } catch (e: Exception) {
            date
        }
    }
}