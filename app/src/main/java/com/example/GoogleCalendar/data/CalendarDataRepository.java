package com.example.GoogleCalendar.data;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.CalendarContract;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.Arrays;
import java.util.HashMap;

import androidx.core.app.ActivityCompat;

import com.example.GoogleCalendar.models.EventDataModel;


public class CalendarDataRepository {

    public static HashMap<LocalDate, EventDataModel[]> readCalendarEventsData(Context context, LocalDate mintime, LocalDate maxtime) {

        HashMap<LocalDate, EventDataModel[]> localDateHashMap = new HashMap<>();

        String selection = "(( " + CalendarContract.Events.DTSTART + " >= " + mintime.toDateTimeAtStartOfDay().getMillis() + " ) AND ( " + CalendarContract.Events.DTSTART + " <= " + maxtime.toDateTimeAtStartOfDay().getMillis() + " ))";
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }

        Cursor cursor = context.getContentResolver()
                .query(
                        CalendarContract.Events.CONTENT_URI,
                        new String[]{"_id", "title", "description",
                                "dtstart", "dtend", "eventLocation", "account_name"}, selection,
                        null, null);

        cursor.moveToFirst();

        // fetching calendars id
        String syncacc = null;
        while (cursor.moveToNext()) {

            if (syncacc == null) syncacc = cursor.getString(6);
            if (cursor.getString(6).equals(syncacc)) {
                LocalDate localDate = getDate(Long.parseLong(cursor.getString(3)));
                if (!localDateHashMap.containsKey(localDate)) {

                    // Fetch data of events per particular date, and add it to localDateHashMap.
                    String[] eventsNames = new String[]{cursor.getString(1)};
                    String[] eventsStartDateTimes = new String[]{cursor.getString(3)};
                    String[] eventsEndDateTimes = new String[]{cursor.getString(4)};
                    EventDataModel[] eventsPerThisDate = new EventDataModel[eventsNames.length];
                    for (int i = 0; i < eventsNames.length; i++) {
                        eventsPerThisDate[i] = new EventDataModel(
                                eventsNames[i],
                                getDateTime(Long.parseLong(eventsStartDateTimes[i])),
                                getDateTime(Long.parseLong(eventsEndDateTimes[i]))
                        );
                    }
                    localDateHashMap.put(localDate, eventsPerThisDate);
                } else {
                    EventDataModel[] eventsPerThisDate = localDateHashMap.get(localDate);
                    boolean isneed = true;
                    for (int i = 0; i < eventsPerThisDate.length; i++) {
                        if (eventsPerThisDate[i].getEventName().equals(cursor.getString(1))) {
                            isneed = false;
                            break;
                        }
                    }
                    if (isneed) {
                        EventDataModel[] eventsPerThisDateNew = Arrays.copyOf(eventsPerThisDate, eventsPerThisDate.length + 1);
                        EventDataModel newEvent = new EventDataModel(
                                cursor.getString(1),
                                getDateTime(Long.parseLong(cursor.getString(3))),
                                getDateTime(Long.parseLong(cursor.getString(4)))
                        );
                        eventsPerThisDateNew[eventsPerThisDateNew.length - 1] = newEvent;
                        localDateHashMap.put(localDate, eventsPerThisDateNew);
                    }
                }
            }
        }
        return localDateHashMap;
    }

    private static LocalDate getDate(long milliSeconds) {
        Instant instantFromEpochMilli = Instant.ofEpochMilli(milliSeconds);
        return instantFromEpochMilli.toDateTime(DateTimeZone.getDefault()).toLocalDate();
    }

    private static LocalDateTime getDateTime(long milliSeconds) {
        Instant instantFromEpochMilli = Instant.ofEpochMilli(milliSeconds);
        return instantFromEpochMilli.toDateTime(DateTimeZone.getDefault()).toLocalDateTime();
    }
}