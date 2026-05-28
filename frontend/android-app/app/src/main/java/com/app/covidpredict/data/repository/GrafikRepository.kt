package com.app.covidpredict.data.repository

import com.app.covidpredict.data.model.ApiResponse
import com.app.covidpredict.data.model.ChartResponse
import com.app.covidpredict.data.remote.ApiClient

class GrafikRepository {

    suspend fun getChart(
        wilayah: String,
        period: String
    ): ApiResponse<ChartResponse> {
        return ApiClient.api.getChart(
            wilayah = wilayah,
            period = period
        )
    }
}