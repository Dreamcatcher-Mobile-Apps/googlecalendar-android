package com.example.GoogleCalendar.common;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class DeviceStateUtils {

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    static public boolean isDeviceOnline(ConnectivityManager connectivityManager) {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
