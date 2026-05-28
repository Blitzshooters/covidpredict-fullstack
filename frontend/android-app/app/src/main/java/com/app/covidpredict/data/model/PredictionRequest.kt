package com.app.covidpredict.data.model

import com.google.gson.annotations.SerializedName

data class PredictionRequest(
    @SerializedName("wilayah")
    val wilayah: String,

    @SerializedName("days")
    val days: Int,

    @SerializedName("alpha")
    val alpha: Double
)