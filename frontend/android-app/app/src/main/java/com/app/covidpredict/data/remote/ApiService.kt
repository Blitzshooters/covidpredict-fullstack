package com.app.covidpredict.data.remote

import com.app.covidpredict.data.model.ApiResponse
import com.app.covidpredict.data.model.ChartResponse
import com.app.covidpredict.data.model.CovidHistoryResponse
import com.app.covidpredict.data.model.DashboardResponse
import com.app.covidpredict.data.model.PredictionRequest
import com.app.covidpredict.data.model.PredictionResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("v1/dashboard")
    suspend fun getDashboard(
        @Query("wilayah") wilayah: String = "Indonesia"
    ): ApiResponse<DashboardResponse>

    @GET("v1/covid")
    suspend fun getCovidHistory(
        @Query("wilayah") wilayah: String = "Indonesia",
        @Query("days") days: Int = 30,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): ApiResponse<List<CovidHistoryResponse>>

    @POST("v1/predict")
    suspend fun predict(
        @Body request: PredictionRequest
    ): ApiResponse<PredictionResponse>

    @GET("v1/chart")
    suspend fun getChart(
        @Query("wilayah") wilayah: String = "Indonesia",
        @Query("period") period: String = "harian"
    ): ApiResponse<ChartResponse>
}