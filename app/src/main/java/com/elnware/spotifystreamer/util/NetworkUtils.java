package com.elnware.spotifystreamer.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Utility to check for network availability
 * Created by elnoxvie on 6/30/15.
 */
public class NetworkUtils {
   public static boolean isNetworkAvailable(Context context) {
      ConnectivityManager connectivityManager = (ConnectivityManager)
              context.getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
      return activeNetworkInfo != null && activeNetworkInfo.isConnected();
   }
}
