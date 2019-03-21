package com.devt3h.appchat.com;

import android.content.Context;
import android.net.ConnectivityManager;

public class CommonUtils {
    public static boolean isOnline(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if ((connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected())
                || (connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null &&
                connManager
                        .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                        .isConnected())) {
            return true;
        } else {
            return false;
        }
    }
}
