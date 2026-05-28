package com.app.covidpredict.data.repository

import com.app.covidpredict.data.model.DashboardResponse
import com.app.covidpredict.data.remote.ApiClient

class DashboardRepository {

    suspend fun getDashboard(wilayah: String): DashboardResponse {
        return ApiClient.api.getDashboard(wilayah).data
    }
}