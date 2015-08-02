package com.elnware.spotifystreamer.fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.elnware.spotifystreamer.R;
import com.elnware.spotifystreamer.model.ParcelableImage;
import com.elnware.spotifystreamer.model.ParcelableTrack;
import com.elnware.spotifystreamer.service.MediaPlayerService;
import com.elnware.spotifystreamer.model.PlaylistItem;
import com.elnware.spotifystreamer.util.ImageUtils;


import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by elnoxvie on 8/1/15.
 */
public class NowPlayingInfoFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener{

   @Bind(R.id.img_track_thumbnail)
   ImageView mAlbumView;

   @Bind(R.id.tv_track_name)
   TextView  mTrackNameView;

   @Bind(R.id.tv_album_name)
   TextView  mAlbumNameView;

   @Bind(R.id.play_or_pause)
   ImageView mPlayOrPauseView;

   private boolean             mServiceConnected;
   private MediaPlayerService  mService;
   private View                mView;
   private NowPlayingCallbacks mCallback;
   private boolean             mEnableNowPlaying;

   public interface NowPlayingCallbacks {
      void showNowPlaying(boolean isVisible);

      void onNowPlayingContainerSelected(PlaylistItem item);
   }

   @Nullable
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      mView = inflater.inflate(R.layout.fragment_now_playing, container, false);
      ButterKnife.bind(this, mView);
      return mView;
   }

   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);

      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
      mEnableNowPlaying = sharedPreferences.getBoolean(getString(R.string.pref_key_enable_now_playing), true);
      sharedPreferences.registerOnSharedPreferenceChangeListener(this);

      if (activity instanceof NowPlayingCallbacks) {
         mCallback = (NowPlayingCallbacks) activity;
      } else {
         throw new ClassCastException("Activity must implement NowPlayingCallbacks");
      }
   }

   @Override
   public void onDetach() {
      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
      sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
      super.onDetach();
   }

   @OnClick(R.id.play_or_pause)
   public void onPlayOrPauseButtonClick() {
      if (mService.getState() == MediaPlayerService.State.PLAYING) {
         mService.pause();
      } else {
         mService.play();
      }
   }

   @OnClick(R.id.now_playing_container)
   public void onControlContainerClicked() {
      if (mServiceConnected) {
         mCallback.onNowPlayingContainerSelected(mService.getCurrentPlayList());
      }
   }

   @Override
   public void onStart() {
      super.onStart();
      Intent intent = new Intent(getActivity(), MediaPlayerService.class);
      getActivity().bindService(intent, mMediaConnection, Context.BIND_AUTO_CREATE);
      getActivity().startService(intent);
   }

   @Override
   public void onStop() {
      mService.removeCallback(mMediaPlayerCallbacks);
      getActivity().unbindService(mMediaConnection);
      super.onStop();
   }

   @Override
   public void onResume() {
      super.onResume();
      if (mServiceConnected && mService.getState() != MediaPlayerService.State.IDLE){
         if (mEnableNowPlaying){
            mCallback.showNowPlaying(true);
         }
      }else{
         mCallback.showNowPlaying(false);
      }
   }

   //Connect to the service
   private ServiceConnection mMediaConnection = new ServiceConnection() {

      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
         MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;

         mServiceConnected = true;

         //get service
         mService = binder.getService();
         mService.addCallback(mMediaPlayerCallbacks);

         if (mService.getState() == MediaPlayerService.State.IDLE) {
            mCallback.showNowPlaying(false);
         } else {
            if (mEnableNowPlaying) {
               mCallback.showNowPlaying(true);
            }
         }

         updateCurrentTrackView();
         updateCurrentPlayerState(mService.getState());
      }

      @Override
      public void onServiceDisconnected(ComponentName componentName) {
         mServiceConnected = true;
      }
   };

   private MediaPlayerService.MediaPlayerCallbacks mMediaPlayerCallbacks = new MediaPlayerService.MediaPlayerCallbacks() {
      @Override
      public void onError(Throwable throwable) {

      }

      @Override
      public void onStateChanged(MediaPlayerService.State state) {
         updateCurrentPlayerState(state);
      }

      @Override
      public void onMetaDataChanged() {
         updateCurrentTrackView();
      }

      @Override
      public void onBufferingUpdate(int percentage) {

      }
   };

   /**
    * Update Current track view
    */
   private void updateCurrentTrackView() {
      if (mServiceConnected && mService.getCurrentPlayList() != null) {
         PlaylistItem playlistItem = mService.getCurrentPlayList();
         ParcelableTrack track = playlistItem.getCurrentTrack();
         updateUIWithTrackInfo(track);
      }
   }

   /**
    * Update Current track UI View
    * with the track info
    */
   private void updateUIWithTrackInfo(ParcelableTrack track) {
      if (track != null) {
         mTrackNameView.setText(track.name);
         mAlbumNameView.setText(track.album.name);

         ParcelableImage image = ImageUtils.getOptimumParcelableImage(track.album.images, getResources().getInteger(R.integer.default_cover_image_width));
         Glide.with(this).load(image.url).crossFade().into(mAlbumView);
      }
   }

   /**
    * Update current player state
    *
    * @param state Media Player State
    */
   private void updateCurrentPlayerState(MediaPlayerService.State state) {
      switch (state) {
         case IDLE:
            mPlayOrPauseView.setImageResource(R.drawable.ic_play_arrow_48dp);
            break;
         case PAUSE:
            mPlayOrPauseView.setImageResource(R.drawable.ic_play_arrow_48dp);
            break;
         case STOP:
            break;
         case COMPLETED:
            break;
         case PREPARING:
            mPlayOrPauseView.setImageResource(R.drawable.ic_pause_48dp);
            break;
         case PLAYING:
            mPlayOrPauseView.setImageResource(R.drawable.ic_pause_48dp);
            break;
      }
   }

   @Override
   public void onDestroyView() {
      super.onDestroyView();
      ButterKnife.unbind(this);
   }

   @Override
   public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

      if (key.equals(getString(R.string.pref_key_enable_now_playing))) {
         mEnableNowPlaying = sharedPreferences.getBoolean(getString(R.string.pref_key_enable_now_playing), true);
         if (mEnableNowPlaying) {
            if (mServiceConnected && mService.getState() != MediaPlayerService.State.IDLE) {
               mCallback.showNowPlaying(true);
            }
         } else {
            mCallback.showNowPlaying(false);
         }
      }
   }
}
