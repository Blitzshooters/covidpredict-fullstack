package com.app.covidpredict.data.repository

import com.app.covidpredict.data.model.ApiResponse
import com.app.covidpredict.data.model.PredictionRequest
import com.app.covidpredict.data.model.PredictionResponse
import com.app.covidpredict.data.remote.ApiClient

class PredictionRepository {

    suspend fun predict(
        wilayah: String,
        days: Int,
        alpha: Double
    ): ApiResponse<PredictionResponse> {
        return ApiClient.api.predict(
            PredictionRequest(
                wilayah = wilayah,
                days = days,
                alpha = alpha
            )
        )
    }
}