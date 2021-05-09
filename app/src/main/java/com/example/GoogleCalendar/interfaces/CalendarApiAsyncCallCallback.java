package com.example.GoogleCalendar.interfaces;

import android.content.Intent;

import com.example.GoogleCalendar.models.EventDataModel;

import org.joda.time.LocalDate;

import java.util.HashMap;

public interface CalendarApiAsyncCallCallback {
    void calendarDataFetchedSuccessfully(HashMap<LocalDate, EventDataModel[]> calendarEventsData);
    void googlePlayServicesAvailabilityError(int connectionStatusCode);
    void userRecoverableException(Intent exceptionIntent);
    void unknownError(String errorMessage);
    void dataFetchingFinished();
}
