package com.example.GoogleCalendar.common

import com.example.GoogleCalendar.models.EventDataModel
import org.joda.time.LocalDate

class Converters {

    companion object {

        fun convertEventsDataMapToEventTitlesMap(oldMap: HashMap<LocalDate, Array<EventDataModel>>): HashMap<LocalDate, Array<String>> {
            val newMap = HashMap<LocalDate, Array<String>>()
            val mapIterator = oldMap.entries.iterator()
            while (mapIterator.hasNext()) {
                val pair = mapIterator.next() as Map.Entry<LocalDate, EventDataModel>
                val key = pair.key
                val eventDataModels = pair.value as Array<EventDataModel>
                val eventTitles = eventDataModels.map {
                    it.eventName
                }
                newMap.put(key, eventTitles.toTypedArray())
            }
            return newMap
        }

    }

}