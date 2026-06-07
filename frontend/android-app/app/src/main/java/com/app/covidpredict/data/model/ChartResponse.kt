package com.app.covidpredict.data.model

import com.google.gson.annotations.SerializedName

data class ChartResponse(
    @SerializedName("avg_error")
    val avgError: String,

    @SerializedName("insight_title")
    val insightTitle: String,

    @SerializedName("insight_text")
    val insightText: String,

    @SerializedName("chart_data")
    val chartData: List<ChartPointResponse>
)

data class ChartPointResponse(
    @SerializedName("label")
    val label: String,

    @SerializedName("actual")
    val actual: Float,

    @SerializedName("prediction")
    val prediction: Float
)