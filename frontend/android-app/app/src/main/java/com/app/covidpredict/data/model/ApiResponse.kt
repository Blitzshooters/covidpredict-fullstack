package com.app.covidpredict.data.model

data class ApiResponse<T>(
    val status: String,
    val message: String?,
    val data: T
)