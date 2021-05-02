package com.example.GoogleCalendar.common

import com.example.GoogleCalendar.models.EventDataModel

class Converters {

    companion object {

        fun convertStringArrayToEventDataModelArray(stringArray: Array<String>): Array<EventDataModel> {
            return stringArray.map {
                EventDataModel(it, null, null)
            }.toTypedArray()
        }

    }

}