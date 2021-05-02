package com.example.GoogleCalendar.models;

import org.joda.time.LocalDate;

import androidx.annotation.NonNull;

public class EventModel implements Comparable<EventModel> {
    private EventDataModel eventData;
    private LocalDate localDate;
    private int type;

    public EventModel(EventDataModel eventData, LocalDate localDate, int type) {
        this.eventData = eventData;
        this.localDate = localDate;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public EventDataModel getEventData() {
        return eventData;
    }

    @Override
    public int compareTo(@NonNull EventModel eventModel) {
        return localDate.compareTo(eventModel.localDate);
    }
}
