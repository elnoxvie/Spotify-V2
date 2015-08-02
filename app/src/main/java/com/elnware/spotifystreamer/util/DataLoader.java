package com.elnware.spotifystreamer.util;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Created by elnoxvie on 7/8/15.
 */
public abstract class DataLoader<D> extends AsyncTaskLoader<D> {
   private D mData;
   private boolean mIsRunning;

   public DataLoader(Context context) {
      super(context);
   }

   //Handles a request to start the Loader
   @Override
   protected void onStartLoading() {
      mIsRunning = true;

      if (mData != null) {


         // If we currently have a result available, deliver it
         // immediately.
         deliverResult(mData);
      } else {

         //Calls superclass's forceLoad() method to go fetch the data
         if (isForceLoad()){
            forceLoad();
         }
      }
   }

   @Override
   protected void onStopLoading() {
      mIsRunning = false;
      super.onStopLoading();
   }

   public boolean isRunning(){
      return mIsRunning;
   }

   public boolean isForceLoad(){
      return false;
   }

   /*
    * Called when there is new data to deliver to the client
    * Stash away the new data object and, if the loader is started, call the
    * superclass implementation to make the delivery/
    */
   @Override
   public void deliverResult(D data) {
      mData = data;
      if (isStarted()) {					//Return whether this load has been started.
         super.deliverResult(data);		//Sends the result of the load to the registered listener
      }
   }
}
