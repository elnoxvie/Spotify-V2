package com.elnware.spotifystreamer.util;

import android.content.Context;

import com.elnware.spotifystreamer.R;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import retrofit.RetrofitError;

/**
 * Created by elnoxvie on 6/25/15.
 */
public class RetrofitErrorHandler {
   public static Throwable handleError(RetrofitError cause) {
      if (cause.getKind().equals(RetrofitError.Kind.NETWORK)) {
         if (cause.getCause() instanceof SocketTimeoutException) {
            return new SocketTimeoutException();
         } else {
            return new ConnectException();
         }
      }

      return cause;
   }

   public static String getStandardErrorMessage(Context context, Throwable throwable) {
      if (throwable instanceof SocketTimeoutException) {
         return context.getString(R.string.msg_connection_timeout);
      } else if (throwable instanceof ConnectException) {
         return context.getString(R.string.msg_no_connection);
      } else {
         return context.getString(R.string.msg_error_occurs);
      }

   }

}
