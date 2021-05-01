package com.example.GoogleCalendar.models

import org.joda.time.LocalDateTime


data class EventDataModel (
        val eventName: String,
        val startDateTime: LocalDateTime?,
        val endDateTime: LocalDateTime?
)