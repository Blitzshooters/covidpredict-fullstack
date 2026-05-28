package com.app.covidpredict.data.model

import com.google.gson.annotations.SerializedName

data class CovidHistoryResponse(
    @SerializedName("tanggal")
    val tanggal: String,

    @SerializedName("wilayah")
    val wilayah: String,

    @SerializedName("positif")
    val positif: Int,

    @SerializedName("sembuh")
    val sembuh: Int,

    @SerializedName("meninggal")
    val meninggal: Int
)