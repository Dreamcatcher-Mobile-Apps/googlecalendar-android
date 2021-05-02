package com.example.GoogleCalendar.models;

import org.joda.time.LocalDate;

public class MonthChange {
    public LocalDate mMessage;
    public int mdy;

    public MonthChange(LocalDate message, int dy) {
        mMessage = message;
        mdy = dy;
    }

    public LocalDate getMessage() {
        return mMessage;
    }
}