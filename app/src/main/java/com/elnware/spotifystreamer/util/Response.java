package com.elnware.spotifystreamer.util;


/**
 * A Response class that provides a
 * container for result and error message
 * Created by elnoxvie on 6/25/15.
 */
public class Response<T> {
   private T data;
   private Throwable throwable;

   public Response(){
      throwable = null;
   }

   public Response(T data, Throwable throwable) {
      this.data = data;
      this.throwable = throwable;
   }

   public T getData() {
      return data;
   }

   public void setData(T data) {
      this.data = data;
   }

   public Throwable getThrowable() {
      return throwable;
   }

   public void setThrowable(Throwable throwable) {
      this.throwable = throwable;
   }

   public boolean isError(){
      return throwable != null;
   }
}
