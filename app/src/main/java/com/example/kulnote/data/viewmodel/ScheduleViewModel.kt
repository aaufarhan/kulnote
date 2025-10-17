package com.example.kulnote.data.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.kulnote.data.model.Schedule

class ScheduleViewModel : ViewModel() {
    private val _schedules = mutableStateListOf<Schedule>()
    val schedules: List<Schedule> = _schedules

    init {
        _schedules.addAll(
            listOf(
                Schedule(1, "Pemrograman Mobile", "08:00 - 09:40", "Ruang B201"),
                Schedule(2, "Basis Data", "10:00 - 11:40", "Ruang A305")
            )
        )
    }

    fun addSchedule(schedule: Schedule) {
        _schedules.add(schedule)
    }
}
