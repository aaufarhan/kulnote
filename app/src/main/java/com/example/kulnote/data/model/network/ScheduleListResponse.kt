package com.example.kulnote.data.model.network

data class ScheduleListResponse(
    val status: String? = null,
    val message: String? = null,
    val data: List<ScheduleApiModel>? = null
)

