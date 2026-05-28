package com.app.covidpredict.data.model

import com.google.gson.annotations.SerializedName

data class PredictionResponse(
    @SerializedName("wilayah")
    val wilayah: String? = null,

    @SerializedName("prediction_days")
    val predictionDays: Int? = null,

    @SerializedName("alpha")
    val alpha: Double? = null,

    @SerializedName("estimated_cases")
    val estimatedCases: Int,

    @SerializedName("forecast_daily_cases")
    val forecastDailyCases: Int,

    @SerializedName("last_daily_cases")
    val lastDailyCases: Int,

    @SerializedName("trend_status")
    val trendStatus: String,

    @SerializedName("trend_percentage")
    val trendPercentage: Double,

    @SerializedName("confidence_interval")
    val confidenceInterval: Double,

    @SerializedName("avg_error")
    val avgError: Double
)