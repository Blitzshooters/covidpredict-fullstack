package com.app.covidpredict.data.repository

import com.app.covidpredict.data.model.ApiResponse
import com.app.covidpredict.data.model.CovidHistoryResponse
import com.app.covidpredict.data.remote.ApiClient

class DataRepository {

    suspend fun getCovidHistory(
        wilayah: String,
        days: Int,
        startDate: String?,
        endDate: String?
    ): ApiResponse<List<CovidHistoryResponse>> {
        return ApiClient.api.getCovidHistory(wilayah, days, startDate, endDate)
    }
}
