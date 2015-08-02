package com.elnware.spotifystreamer.util;

import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Created by elnoxvie on 6/30/15.
 */
public class TransitionUtils {
   /**
    * Schedule the start postponed Enter Transtion
    * Reference: http://www.androiddesignpatterns.com/2015/03/activity-postponed-shared-element-transitions-part3b.html
    * @param sharedElement
    * @param activity
    *
    */
   public static  void scheduleStartPostponedTransition(final Activity activity, final View sharedElement){
      sharedElement.getViewTreeObserver().addOnPreDrawListener(
              new ViewTreeObserver.OnPreDrawListener() {
                 @Override
                 public boolean onPreDraw() {
                    sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                    ActivityCompat.startPostponedEnterTransition(activity);
                    return true;
                 }
              });
   }
}
