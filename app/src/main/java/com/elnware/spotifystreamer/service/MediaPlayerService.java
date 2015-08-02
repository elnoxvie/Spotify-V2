package com.elnware.spotifystreamer.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.elnware.spotifystreamer.R;
import com.elnware.spotifystreamer.activity.MediaPlayerActivity;
import com.elnware.spotifystreamer.fragment.TopTrackFragment;
import com.elnware.spotifystreamer.model.ParcelableImage;
import com.elnware.spotifystreamer.model.ParcelableTrack;
import com.elnware.spotifystreamer.model.PlaylistItem;
import com.elnware.spotifystreamer.util.ImageUtils;
import com.elnware.spotifystreamer.util.ResourceHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by elnoxvie on 7/12/15.
 */
public class MediaPlayerService extends Service implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnPreparedListener,
        AudioManager.OnAudioFocusChangeListener,
        SharedPreferences.OnSharedPreferenceChangeListener
{

   private static final String TAG             = MediaPlayerService.class.getSimpleName();
   public static final  int    NOTIFICATION_ID = 100;
   private static final int    REQUEST_CODE    = 101;
   public static final  int    DELAY_MILLIS    = 10000;

   public static final  String EXTRA_STARTED_FROM_NOTIFICATION = "started_from_notification";
   public static final  String EXTRA_CURRENT_TRACK             = "current_Track";
   private static final float  DUCK_VOLUME                     = 0.1f;
   private static final float  FULL_VOLUME                     = 1.0f;
   public static final  String MY_WIFI_LOCK                    = "mylock";

   private MediaPlayer mMediaPlayer;

   private PlaylistItem              mPlaylistItem;
   private NotificationManagerCompat mNotificationCompat;

   private static final String MY_PACKAGE = MediaPlayerService.class.getPackage() != null ?
           MediaPlayerService.class.getPackage().getName() : "com.elnware.spotifystreamer";

   public static final String ACTION_PLAY     = MY_PACKAGE + ".action.PLAY";
   public static final String ACTION_PAUSE    = MY_PACKAGE + ".action.PAUSE";
   public static final String ACTION_NEXT     = MY_PACKAGE + ".action.NEXT";
   public static final String ACTION_PREVIOUS = MY_PACKAGE + ".action.PREVIOUS";

   private int mNotificationColor;
   private boolean mForegroundStarted = false;

   private PendingIntent mPreviousIntent;
   private PendingIntent mNextIntent;
   private PendingIntent mPauseIntent;
   private PendingIntent mPlayIntent;

   private boolean              mClientConnected;
   private AudioManager         mAudioManager;
   private WifiManager.WifiLock mWifiLock;
   private boolean              mEnableNotification;


   //Media Player State
   public enum State {
      IDLE,
      PAUSE,
      STOP,
      COMPLETED,
      PREPARING,
      PLAYING,
   }


   private State mState = State.IDLE;
   private List<MediaPlayerCallbacks> mCallbacks;
   private Handler mHandler = new Handler();

   @Override
   public void onCreate() {
      super.onCreate();
      mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
      mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
              .createWifiLock(WifiManager.WIFI_MODE_FULL, MY_WIFI_LOCK);

      //get accent color from Theme
      mNotificationColor = ResourceHelper.getThemeColor(this, R.attr.colorAccent, Color.DKGRAY);
      mNotificationCompat = NotificationManagerCompat.from(this);

      mCallbacks = new ArrayList<>();

      mPreviousIntent = PendingIntent.getService(this,
              REQUEST_CODE,
              new Intent(this, MediaPlayerService.class).setAction(ACTION_PREVIOUS),
              PendingIntent.FLAG_UPDATE_CURRENT);

      mNextIntent = PendingIntent.getService(this,
              REQUEST_CODE,
              new Intent(this, MediaPlayerService.class).setAction(ACTION_NEXT),
              PendingIntent.FLAG_UPDATE_CURRENT);

      mPauseIntent = PendingIntent.getService(this,
              REQUEST_CODE,
              new Intent(this, MediaPlayerService.class).setAction(ACTION_PAUSE),
              PendingIntent.FLAG_UPDATE_CURRENT);

      mPlayIntent = PendingIntent.getService(this,
              REQUEST_CODE,
              new Intent(this, MediaPlayerService.class).setAction(ACTION_PLAY),
              PendingIntent.FLAG_UPDATE_CURRENT);

      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
      mEnableNotification = sharedPreferences.getBoolean(getString(R.string.enable_notification), true);
      sharedPreferences.registerOnSharedPreferenceChangeListener(this);
   }


   protected int requestAudioFocus() {
      // Request audio focus for playback
      return mAudioManager.requestAudioFocus(this,
              // Use the music stream.
              AudioManager.STREAM_MUSIC,
              // Request permanent focus.
              AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

   }

   protected void abandonAudioFocus() {
      mAudioManager.abandonAudioFocus(this);
   }

   @Override
   public void onAudioFocusChange(int focusChange) {
      if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {


         // Pause playback
         pause();
      } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {


         //resume
         resume();


         //reset the volume back to full
         mMediaPlayer.setVolume(FULL_VOLUME, FULL_VOLUME);
      } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {


         //stop
         stop();
      } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {


         //lower the volume
         mMediaPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);
      }

   }


   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      String action = intent.getAction();

      //Debug.waitForDebugger();

      //we don't have stop action as
      //we will be using a runnable to stop
      //the service
      if (action != null) {
         if (action.equals(ACTION_PLAY)) {
            play();
         } else if (action.equals(ACTION_PAUSE)) {
            pause();
         } else if (action.equals(ACTION_NEXT)) {
            skipToNext();
         } else if (action.equals(ACTION_PREVIOUS)) {
            skipToPrevious();
         }

      }

      return Service.START_NOT_STICKY;
   }


   @Nullable
   @Override
   public IBinder onBind(Intent intent) {
      return new MediaPlayerBinder();
   }


   public void initMediaPlayer() {
      if (mMediaPlayer == null) {
         mMediaPlayer = new MediaPlayer();
         mMediaPlayer.setOnCompletionListener(this);
         mMediaPlayer.setOnErrorListener(this);
         mMediaPlayer.setOnBufferingUpdateListener(this);
         mMediaPlayer.setOnPreparedListener(this);

         mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
      }
   }

   private void onMetaDataChanged(){
      for(MediaPlayerCallbacks callback: mCallbacks){
         callback.onMetaDataChanged();
      }
   }

   private void onStateChanged(State state){
      for(MediaPlayerCallbacks callback: mCallbacks){
         callback.onStateChanged(state);
      }
   }

   private void onError(Throwable throwable){
      for(MediaPlayerCallbacks callback: mCallbacks){
         callback.onError(throwable);
      }
   }


   private void onBufferingUpdate(int percentage){
      for(MediaPlayerCallbacks callback: mCallbacks){
         callback.onBufferingUpdate(percentage);
      }
   }


   /**
    * Play track
    */
   public void play() {
      if (mState == State.IDLE) {
         onMetaDataChanged();
//         if (mCallbacks != null) {
//            mCallbacks.onMetaDataChanged();
//         }

         playSelectedTrack();
      } else if (mState == State.PLAYING) {
         pause();
      } else if (mState == State.PAUSE) {
         resume();
      }
   }


   /**
    * Skip to next track
    */
   public void skipToNext() {
      mPlaylistItem.next();

      onMetaDataChanged();

//      if (mCallbacks != null) {
//         mCallbacks.onMetaDataChanged();
//      }

      reset();
      playSelectedTrack();
   }


   /**
    * Skip to previous track
    */
   public void skipToPrevious() {


      //navigate to the next track
      mPlaylistItem.previous();

      onMetaDataChanged();

//      if (mCallbacks != null) {
//
//
//         //We tell the client that we meta changes may change
//         //we may need to update it
//         mCallbacks.onMetaDataChanged();
//
//      }


      reset();
      playSelectedTrack();
   }


   /**
    * Releasing the player when the player is no longer in used
    */
   public void release() {
      if (mMediaPlayer != null) {
         mMediaPlayer.reset();
         mMediaPlayer.release();
         mMediaPlayer = null;
      }
   }


   @Override
   public void onDestroy() {
      super.onDestroy();
      PreferenceManager.getDefaultSharedPreferences(this)
              .unregisterOnSharedPreferenceChangeListener(this);
      setCurrentMediaPlayerState(State.STOP);
      release();
      stopNotification();
   }


   /**
    * Set Current media player state and
    * notify the client on the State changes
    *
    * @param state State
    */
   private void setCurrentMediaPlayerState(State state) {
      mState = state;
      onStateChanged(state);
//      if (mCallbacks != null) {
//         mCallbacks.onStateChanged(mState);
//      }
   }


   /**
    * Reset Media Player
    */
   public void reset() {
      mMediaPlayer.reset();
      setCurrentMediaPlayerState(State.IDLE);
   }


   /**
    * Playing the selected track
    */
   public void playSelectedTrack() {
      if (mState == State.IDLE) {

         if (mMediaPlayer == null) {
            initMediaPlayer();
         }

         int audioFocus = requestAudioFocus();


         //we don't have audio focus, so we will just quit playing
         if (audioFocus != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //mCallbacks.onError(new IllegalAccessException(getString(R.string.err_audio_focus_not_granted)));
            onError(new IllegalAccessException(getString(R.string.err_audio_focus_not_granted)));
            return;
         }

         ParcelableTrack track = mPlaylistItem.getCurrentTrack();
         Uri previewUrl = Uri.parse(track.preview_url);

         try {
            mMediaPlayer.setDataSource(this, previewUrl);
            mMediaPlayer.prepareAsync();

            if (!mWifiLock.isHeld()){
               mWifiLock.acquire();
            }

            setCurrentMediaPlayerState(State.PREPARING);
            if (mEnableNotification){
               notifyNotification();
            }
            stopDelayedCallback();

         } catch (IllegalStateException e) {
            onError(e);
           // mCallbacks.onError(e);
            Log.e(TAG, "MediaPlayer throws IllegalStateException, uri=" + previewUrl);
         } catch (IOException e) {
            onError(e);
            //mCallbacks.onError(e);
            Log.e(TAG, "MediaPlayer throws IOException, uri=" + previewUrl);
         } catch (IllegalArgumentException e) {
            onError(e);
           // mCallbacks.onError(e);
            Log.e(TAG, "MediaPlayer throws IllegalArgumentException, uri=" + previewUrl);
         } catch (SecurityException e) {
            onError(e);
           // mCallbacks.onError(e);
            Log.e(TAG, "MediaPlayer throws SecurityException, uri=" + previewUrl);
         }
      }
   }


   /**
    * Set Playlist
    *
    * @param playlistItem current playlist
    */
   public void setPlaylist(PlaylistItem playlistItem) {
      mPlaylistItem = playlistItem;
   }


   /**
    * Get Current Playlist
    *
    * @return current playlist
    */
   public PlaylistItem getCurrentPlayList() {
      return mPlaylistItem;
   }


   /**
    * Seek To position
    */
   public void seekTo(int position) {
      if (mMediaPlayer != null) {
         mMediaPlayer.seekTo(position);
      }
   }


   /**
    * Pause the media player
    */
   public void pause() {
      if (mState == State.PLAYING) {
         mMediaPlayer.pause();
         setCurrentMediaPlayerState(State.PAUSE);
         if (mEnableNotification){
            notifyNotification();
         }

         if (mWifiLock.isHeld()){
            mWifiLock.release();
         }
      }

      startStopDelayedCallback();
   }


   /**
    * Stop Media Player Service Runnable
    */
   public Runnable mStopDelayedCallback = new Runnable() {
      @Override
      public void run() {
         stop();
      }
   };


   /**
    * Stop Media Player Service
    * releasing media resources
    */
   public void stop() {
      release();
      //setCallbacks(null);
      mCallbacks.clear();
      abandonAudioFocus();
      stopSelf();
      stopNotification();
      stopDelayedCallback();
   }


   /***
    * Resuming Media Player service
    * if the player is in Pause State
    */
   public void resume() {
      if (mState == State.PAUSE) {


         //we will acquire the wifi lock
         if (!mWifiLock.isHeld()){
            mWifiLock.acquire();
         }

         mMediaPlayer.start();
         setCurrentMediaPlayerState(State.PLAYING);
         if (mEnableNotification){
            notifyNotification();
         }
         stopDelayedCallback();
      }
   }


   /**
    * Removing callbacks if the Media Player
    * Service is still needed
    */
   private void stopDelayedCallback() {


      //we can always safely removing the callback
      //whether the client is connected
      mHandler.removeCallbacks(mStopDelayedCallback);
   }


   /**
    * Start stop delayed callback to automatically
    * stop the following service when the client is no longer connected
    * and it's in Pause or Stop State
    */
   private void startStopDelayedCallback() {
      if (mCallbacks.size() == 0) {
         mHandler.postDelayed(mStopDelayedCallback, DELAY_MILLIS);
      }
   }


   /**
    * Getting the current state of the Media Player
    */
   public State getState() {
      return mState;
   }


   /***
    * Get current duration of the Media being played
    *
    * @return duration of the tracks in milliseconds
    */
   public int getDuration() {
      return mMediaPlayer.getDuration();
   }


   /**
    * Get the current position
    *
    * @return current position in milliseconds
    */
   public int getCurrentPosition() {
      return mMediaPlayer.getCurrentPosition();
   }


   @Override
   public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
      onBufferingUpdate(percent);
//      if (mCallbacks != null) {
//         mCallbacks.onBufferingUpdate(percent);
//      }
   }

   @Override
   public void onCompletion(MediaPlayer mediaPlayer) {


      //On Completion, we will reset and skip to the next track
      reset();
      skipToNext();
   }

   @Override
   public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
      return false;
   }


   @Override
   public void onPrepared(MediaPlayer mediaPlayer) {


      //start the media player
      mediaPlayer.start();
      setCurrentMediaPlayerState(State.PLAYING);
      if (mEnableNotification){
         notifyNotification();
      }
   }

   public void addCallback(MediaPlayerCallbacks callbacks){
      mCallbacks.add(callbacks);

      if (mCallbacks.size() > 0){
         mClientConnected = true;
      }
   }

   public void removeCallback(MediaPlayerCallbacks callbacks){
      mCallbacks.remove(callbacks);

      if (mCallbacks.size() == 0){
         mClientConnected = false;

         //if the current service is not playing, we will stop delayed
         if (mState == State.PAUSE || mState == State.IDLE || mState == State.STOP ){
            startStopDelayedCallback();
         }
      }
   }


//   /***
//    * Setting callbacks for the client
//    *
//    * @param callbacks callbacks to be implemented by the client
//    */
//   public void setCallbacks(MediaPlayerCallbacks callbacks) {
//      if (callbacks != null) {
//         mClientConnected = true;
//         mCallbacks = callbacks;
//      } else {
//         mClientConnected = false;
//         mCallbacks = null;
//
//
//         //if the current service is not playing, we will stop delayed
//         if (mState == State.PAUSE || mState == State.IDLE || mState == State.STOP ){
//            startStopDelayedCallback();
//         }
//      }
//   }


   /**
    * Media Player Binder for the client UI
    * to bind to the service
    */
   public class MediaPlayerBinder extends Binder {
      public MediaPlayerService getService() {
         return MediaPlayerService.this;
      }
   }


   /**
    * Media Player Callbacks to be
    * implemented by client
    */
   public interface MediaPlayerCallbacks {

      void onError(Throwable throwable);


      //Notify Client on every state changed
      void onStateChanged(State state);


      //When we start playing, skip to previous or next
      //the track will change, we will need to notify
      //the client to update UI with correct track Info
      void onMetaDataChanged();


      //Notify the client on the current
      //percentage of the buffering
      void onBufferingUpdate(int percentage);
   }


   /**
    * Create a notification based on the current Media Player State
    *
    * @return Notification Builder
    */
   private NotificationCompat.Builder createNotification() {
      PlaylistItem    playlistItem = getCurrentPlayList();
      ParcelableTrack track        = playlistItem.getCurrentTrack();

      Intent showDetailsIntent = new Intent(this, MediaPlayerActivity.class);
      showDetailsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//      showDetailsIntent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);
//      showDetailsIntent.putExtra(EXTRA_CURRENT_TRACK, track);
      showDetailsIntent.putExtra(TopTrackFragment.EXTRA_PLAY_LIST, playlistItem);

      PendingIntent intent = PendingIntent.getActivity(this,
              (int) System.currentTimeMillis(),
              showDetailsIntent,
              PendingIntent.FLAG_UPDATE_CURRENT);

      final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
      builder.setAutoCancel(false);
      NotificationCompat.MediaStyle mediaStyle = new NotificationCompat.MediaStyle();
      builder.setStyle(mediaStyle);
      builder.setSmallIcon(android.R.drawable.ic_media_play);
      builder.setColor(mNotificationColor);
      builder.setVisibility(Notification.VISIBILITY_PUBLIC);
      builder.setWhen(System.currentTimeMillis());

      //if the client is no longer connected and the state is pause
      //we could remove the notification.
      builder.setOngoing((!mClientConnected &&
              (mState == State.PAUSE)) ? false : true);
      builder.setTicker(getString(R.string.app_name));
      builder.setContentTitle(track.name);
      builder.setContentText(track.album.name);
      builder.setContentIntent(intent);

      builder.addAction(R.drawable.ic_skip_previous_48dp,
              getString(R.string.action_skip_to_previous),
              mPreviousIntent);

      if (mState == State.PLAYING) {
         builder.addAction(R.drawable.ic_pause_48dp,
                 getString(R.string.action_pause),
                 mPauseIntent);
      } else {
         builder.addAction(R.drawable.ic_play_arrow_48dp,
                 getString(R.string.action_play),
                 mPlayIntent);
      }

      builder.addAction(R.drawable.ic_skip_next_48dp,
              getString(R.string.action_skip_to_next),
              mNextIntent);

      if (mState == State.PLAYING) {
         builder.setWhen(System.currentTimeMillis() - getCurrentPosition());
         builder.setShowWhen(true);
         builder.setUsesChronometer(true);
      } else {
         builder.setShowWhen(false);
         builder.setUsesChronometer(false);
      }

      Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_default_art);
      builder.setLargeIcon(bitmap);

      ParcelableImage image = ImageUtils.getOptimumParcelableImage(track.album.images, 64);

      if (image != null) {


         //we will load the image in the background and only
         //notify the notification again with the resource
         Glide.with(this).load(image.url).asBitmap().into(new SimpleTarget<Bitmap>(256, 256) {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
               builder.setLargeIcon(resource);
               mNotificationCompat.notify(NOTIFICATION_ID, builder.build());
            }
         });
      }

      return builder;
   }


   /**
    * Start foreground Notification
    */
   private void startNotification() {
      NotificationCompat.Builder builder = createNotification();
      startForeground(NOTIFICATION_ID, builder.build());
      mForegroundStarted = true;
   }


   /**
    * Notify Notification with the changes
    */
   private void notifyNotification() {
      if (!mForegroundStarted) {
         startNotification();
      } else {
         NotificationCompat.Builder builder = createNotification();
         mNotificationCompat.notify(NOTIFICATION_ID, builder.build());
      }
   }


   /***
    * Stop foreground Notification
    */
   private void stopNotification() {
      mNotificationCompat.cancel(NOTIFICATION_ID);
      stopForeground(true);
      mForegroundStarted = false;
   }

   @Override
   public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      if (key.equals(getString(R.string.pref_key_enable_notification))){
         boolean enableNotification = sharedPreferences.getBoolean(key, true);

         mEnableNotification = enableNotification;

         if (enableNotification){


            //if Notification is already started, we do nothing
            if (!mForegroundStarted){
               startNotification();
            }
         }else{
            if (mForegroundStarted){
               stopNotification();
            }
         }
      }
   }

}
