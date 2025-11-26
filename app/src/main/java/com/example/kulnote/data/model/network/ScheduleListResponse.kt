package com.example.kulnote.data.model.network

// Wrapper response untuk GET /api/schedules jika server mengirim wrapped object
data class ScheduleListResponse(
    val status: String? = null,
    val message: String? = null,
    val data: List<ScheduleApiModel>? = null,
    // Jika server langsung return array, field ini tidak digunakan
)

