package com.example.GoogleCalendar.api;

import android.os.AsyncTask;

import com.example.GoogleCalendar.R;
import com.example.GoogleCalendar.models.EventDataModel;
import com.example.GoogleCalendar.ui.MainActivity;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * An asynchronous task that handles the Google Calendar API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */


public class ApiAsyncTask extends AsyncTask<Void, Void, Void> {
    private MainActivity mActivity;

    /**
     * Constructor.
     * @param activity MainActivity that spawned this task.
     */
    public ApiAsyncTask(MainActivity activity) {
        this.mActivity = activity;
    }

    /**
     * Background task to call Google Calendar API.
     * @param params no parameters needed for this task.
     */
    @Override
    protected Void doInBackground(Void... params) {
        try {
            mActivity.updateResultsOnUi(getDataFromApi());

        } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
            mActivity.showGooglePlayServicesAvailabilityErrorDialog(
                    availabilityException.getConnectionStatusCode());

        } catch (UserRecoverableAuthIOException userRecoverableException) {
            mActivity.startActivityForResult(
                    userRecoverableException.getIntent(),
                    MainActivity.REQUEST_AUTHORIZATION);

        } catch (IOException e) {
            mActivity.displayStatusAsToastMessage(mActivity.getString(R.string.the_following_error_occurred, e.getMessage()));
        }
        return null;
    }

    /**
     * Fetch a list of the events from the primary calendar.
     * @return List of Strings describing returned events.
     * @throws IOException
     */
    private HashMap<LocalDate, EventDataModel[]> getDataFromApi() throws IOException {
        LocalDate ld = new LocalDate().minusYears(MainActivity.YEARS_BACK);
        Date now = ld.toDateTimeAtCurrentTime().toDate();
        LocalDate md = new LocalDate().plusYears(MainActivity.YEARS_FORWARD);
        Date then = md.toDateTimeAtCurrentTime().toDate();
        Events events = mActivity.calendarService.events().list("primary")
//                .setMaxResults(20)
                .setTimeMin(new DateTime(now))
                .setTimeMax(new DateTime(then))
                .setOrderBy("updated")
//                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        HashMap<LocalDate, EventDataModel[]> localDateHashMap = new HashMap<>();

        for (Event event : items) {
            DateTime start = event.getStart().getDateTime();
            DateTime end = event.getEnd().getDateTime();
            if (start == null) {
                // All-day events don't have start times, so just use
                // the start date.
                start = event.getStart().getDate();
            }
            if (end == null) {
                // All-day events don't have end times, so just use
                // the end date.
                end = event.getEnd().getDate();
            }
            if (start!=null && end!=null) {
                LocalDate localDate = getDate(start.getValue());
                if (!localDateHashMap.containsKey(localDate)) {

                    // Fetch data of events per particular date, and add it to localDateHashMap.
                    String[] eventsNames = new String[]{event.getSummary()};
                    EventDataModel[] eventsPerThisDate = new EventDataModel[eventsNames.length];
                    for (int i = 0; i < eventsNames.length; i++) {
                        eventsPerThisDate[i] = new EventDataModel(
                                eventsNames[i],
                                getDateTime(start.getValue()),
                                getDateTime(end.getValue())
                        );
                    }
                    localDateHashMap.put(localDate, eventsPerThisDate);
                } else {
                    EventDataModel[] eventsPerThisDate = localDateHashMap.get(localDate);
                    boolean isneed = true;
                    for (int i = 0; i < eventsPerThisDate.length; i++) {
                        if (eventsPerThisDate[i].getEventName().equals(event.getSummary())) {
                            isneed = false;
                            break;
                        }
                    }
                    if (isneed) {
                        EventDataModel[] eventsPerThisDateNew = Arrays.copyOf(eventsPerThisDate, eventsPerThisDate.length + 1);
                        EventDataModel newEvent = new EventDataModel(
                                event.getSummary(),
                                getDateTime(start.getValue()),
                                getDateTime(end.getValue())
                        );
                        eventsPerThisDateNew[eventsPerThisDateNew.length - 1] = newEvent;
                        localDateHashMap.put(localDate, eventsPerThisDateNew);
                    }
                }
            }
        }
        return localDateHashMap;
    }

    private LocalDate getDate(long milliSeconds) {
        Instant instantFromEpochMilli = Instant.ofEpochMilli(milliSeconds);
        return instantFromEpochMilli.toDateTime(DateTimeZone.getDefault()).toLocalDate();
    }

    private LocalDateTime getDateTime(long milliSeconds) {
            Instant instantFromEpochMilli = Instant.ofEpochMilli(milliSeconds);
            return instantFromEpochMilli.toDateTime(DateTimeZone.getDefault()).toLocalDateTime();
    }
}