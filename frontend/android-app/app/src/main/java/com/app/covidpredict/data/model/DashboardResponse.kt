package com.app.covidpredict.data.model

import com.google.gson.annotations.SerializedName

data class DashboardResponse(

    @SerializedName("wilayah")
    val wilayah: String,

    @SerializedName("last_updated")
    val lastUpdated: String,

    @SerializedName("confirmed")
    val confirmed: Long,

    @SerializedName("today_increase")
    val todayIncrease: Int,

    @SerializedName("recovered")
    val recovered: Long,

    @SerializedName("recovered_rate")
    val recoveredRate: Double,

    @SerializedName("deaths")
    val deaths: Long,

    @SerializedName("death_rate")
    val deathRate: Double,

    @SerializedName("trend_percent")
    val trendPercent: Double,

    @SerializedName("trend_status")
    val trendStatus: String,

    @SerializedName("model_confidence")
    val modelConfidence: Double
)