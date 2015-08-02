package com.elnware.spotifystreamer.fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.elnware.spotifystreamer.R;
import com.elnware.spotifystreamer.model.ParcelableImage;
import com.elnware.spotifystreamer.model.ParcelableTrack;
import com.elnware.spotifystreamer.service.MediaPlayerService;
import com.elnware.spotifystreamer.model.PlaylistItem;
import com.elnware.spotifystreamer.util.ImageUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.elnware.spotifystreamer.service.MediaPlayerService.*;


/**
 * Dialog Fragment that implement the Media Controller
 * showing current tracks
 * Created by elnoxvie on 7/5/15.
 */
public class MediaPlayerFragment extends DialogFragment
        implements Toolbar.OnMenuItemClickListener{
   public static final int DELAY_MILLIS = 1000;

   @Bind(R.id.play_or_stop)
   ImageView   mPlayOrPauseView;
   @Bind(R.id.track_name_view)
   TextView    mTrackNameView;
   @Bind(R.id.artist_name_view)
   TextView    mArtistNameView;
   @Bind(R.id.album_name_view)
   TextView    mAlbumNameView;
   @Bind(R.id.album_view)
   ImageView   mAlbumView;
   @Bind(R.id.track_total_duration_view)
   TextView    mTotalDurationView;
   @Bind(R.id.track_duration_view)
   TextView    mDurationView;
   @Bind(R.id.seekBar)
   SeekBar     mSeekBarView;
   @Bind(R.id.progressBar)
   ProgressBar mProgressBarView;
   @Nullable
   @Bind(R.id.toolbar)
   Toolbar     mToolbar;

   private Handler mHandle = new Handler();

   private PlaylistItem item;

   private MediaPlayerService  mService;
   private boolean             mServiceConnected;
   private boolean             mSaveInstancedStated;
   private MediaPlayerCallback mCallback;

   public interface MediaPlayerCallback {
      void onDismiss();
   }

   public static MediaPlayerFragment newInstance(Bundle bundle) {
      MediaPlayerFragment mediaPlayerFragment = new MediaPlayerFragment();
      mediaPlayerFragment.setArguments(bundle);
      mediaPlayerFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
      return mediaPlayerFragment;
   }

   @Override
   public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setStyle(STYLE_NO_TITLE, 0);
      setHasOptionsMenu(true);
   }

   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);
      if (activity instanceof MediaPlayerCallback) {
         mCallback = (MediaPlayerCallback) activity;
      }
   }

   @Override
   public void onDismiss(DialogInterface dialog) {
      if (mCallback != null){
         mCallback.onDismiss();
      }
      super.onDismiss(dialog);
   }

   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      super.onCreateOptionsMenu(menu, inflater);
      if (!getShowsDialog()) {
         inflater.inflate(R.menu.menu_media_player, menu);
      }
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      if (onOptionItemSelected(item)) {
         return false;
      }

      return super.onOptionsItemSelected(item);
   }

   private boolean onOptionItemSelected(MenuItem item) {
      boolean isHandled = false;
      switch (item.getItemId()) {
         case R.id.action_share:
            if (mServiceConnected) {

               ParcelableTrack track = mService.getCurrentPlayList().getCurrentTrack();
               Map<String, String> externalUrls = track.external_urls;

               if (externalUrls.size() > 0) {
                  String firstKey = externalUrls.keySet().iterator().next();
                  String url = externalUrls.get(firstKey);

                  Intent i = new Intent(Intent.ACTION_SEND);
                  i.setType("text/plain");
                  i.putExtra(Intent.EXTRA_TEXT, url);
                  startActivity(Intent.createChooser(i, getString(R.string.share_spotify_url)));
                  isHandled = true;
               }
            }

            break;
      }

      return isHandled;
   }

   @Nullable
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view;

      if (getShowsDialog()){
         view = inflater.inflate(R.layout.fragment_media_player_control_toolbar, container, false);
      }else{
         view = inflater.inflate(R.layout.fragment_media_player_control, container, false);
      }

      ButterKnife.bind(this, view);

      if (mToolbar != null){
         mToolbar.inflateMenu(R.menu.menu_media_player);
         mToolbar.setOnMenuItemClickListener(this);
      }

      mSaveInstancedStated = savedInstanceState != null ? true : false;

      if (savedInstanceState == null) {
         item = getArguments().getParcelable(TopTrackFragment.EXTRA_PLAY_LIST);
      } else {
         item = savedInstanceState.getParcelable(TopTrackFragment.EXTRA_PLAY_LIST);
      }

      mSeekBarView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
         boolean isFromUser = false;

         @Override
         public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
            if (isFromUser) {
               if (mServiceConnected && (mService.getState() == State.PLAYING)) {
                  mService.seekTo((int) ((progress / 100f) * mService.getDuration()));
               }
            }
         }

         @Override
         public void onStartTrackingTouch(SeekBar seekBar) {


            //indicate that this is from user
            isFromUser = true;
         }

         @Override
         public void onStopTrackingTouch(SeekBar seekBar) {


            //Stop tracking
            isFromUser = false;
         }
      });

      return view;
   }


   /**
    * Set Current Progress Visibility
    *
    * @param isVisible visible
    */
   public void setProgressVisibility(boolean isVisible) {
      mProgressBarView.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
   }


   @Override
   public void onDestroyView() {
      super.onDestroyView();
      ButterKnife.unbind(this);
   }


   @OnClick({R.id.play_or_stop, R.id.rewind, R.id.forward})
   public void onMediaControlViewClicked(View view) {
      setAction(view.getId());
   }

   private void setAction(int id) {
      PlaylistItem playlistItem = mService.getCurrentPlayList();


      //if there is no playlist, we will
      //set the current playlist when we play
      if (playlistItem == null) {
         mService.setPlaylist(item);
      } else if (playlistItem != null) {


         //if session ID doesn't match,
         //we will set a new playlist
         //reset the player state and play
         if (!playlistItem.isMatch(item.getSessionId())) {
            mService.setPlaylist(item);
            mService.reset();
         }
      }

      switch (id) {
         case R.id.play_or_stop:
            mService.play();
            break;
         case R.id.forward:
            mService.skipToNext();
            break;
         case R.id.rewind:
            mService.skipToPrevious();
            break;
      }
   }


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
         mArtistNameView.setText(track.artists.get(0).name);

         ParcelableImage image = ImageUtils.getOptimumParcelableImage(track.album.images, getResources().getInteger(R.integer.default_cover_image_width));
         Glide.with(this).load(image.url).crossFade().into(mAlbumView);
      }
   }


   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      outState.putParcelable(TopTrackFragment.EXTRA_PLAY_LIST, item);
   }


   @Override
   public void onStart() {
      super.onStart();
      Intent intent = new Intent(getActivity(), MediaPlayerService.class);
      getActivity().bindService(intent, mMediaConnection, Context.BIND_AUTO_CREATE);
      getActivity().startService(intent);

      if (!mSaveInstancedStated) {
         updateUIWithTrackInfo(item.getCurrentTrack());
      }
   }


   @Override
   public void onStop() {
      super.onStop();
      if (mServiceConnected) {
         mHandle.removeCallbacks(mUpdateSeekbar);
         mService.removeCallback(mCallbacks);
         getActivity().unbindService(mMediaConnection);
      }
   }

   private Runnable mUpdateSeekbar = new Runnable() {
      @Override
      public void run() {
         if (mServiceConnected &&
                 mService != null &&
                 mService.getState() == State.PLAYING) {

            int currentPosition = mService.getCurrentPosition();
            int totalDuration = mService.getDuration();

            String durationText = formatTime(currentPosition);
            String totalDurationText = formatTime(totalDuration);

            mDurationView.setText(durationText);
            mTotalDurationView.setText(totalDurationText);

            try {
               int currentProgress = (int) ((currentPosition * 100.0f) / totalDuration);
               mSeekBarView.setProgress(currentProgress);
            } catch (Exception e) {
               e.printStackTrace();
            }

         }
         mHandle.postDelayed(this, DELAY_MILLIS);
      }
   };


   private String formatTime(long millis) {
      return String.format("%02d:%02d",
              TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
              TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
   }

   //Connect to the service
   private ServiceConnection mMediaConnection = new ServiceConnection() {

      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
         MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;

         mServiceConnected = true;

         //get service
         mService = binder.getService();
         mService.addCallback(mCallbacks);

         PlaylistItem playlistItem = mService.getCurrentPlayList();


         //check if there is a currently playing item
         if (playlistItem != null) {


            //if the activity is rotated
            if (mSaveInstancedStated) {


               //we check if the current playlist is from the same playlist
               //we will update the track view and state
               if (playlistItem.isMatch(item.getSessionId())) {
                  updateCurrentPlayerState(mService.getState());
                  updateCurrentTrackView();


                  //if they are from different playlist
                  //we will only update the current track
               } else {
                  updateUIWithTrackInfo(item.getCurrentTrack());
               }


               //if activity is not rotated
            } else {


               //if the current playlist passed in is the same
               //playlist currently being played
               if (playlistItem.getSessionId().equals(item.getSessionId())) {


                  //we will check if they selected position is the same in the playlist
                  //if they are the same we will only update the state
                  if (item.getCurrentPosition() == playlistItem.getCurrentPosition()) {
                     updateCurrentPlayerState(mService.getState());


                     //otherwise we will reset the player and
                     //start playing the selected track
                  } else {
                     mService.getCurrentPlayList().setCurrentPosition(item.getCurrentPosition());
                     mService.reset();
                     mService.play();
                  }


                  updateCurrentTrackView();
               } else {
                  mService.reset();
                  setAction(R.id.play_or_stop);
               }
            }


            //otherwise we will play the selected song
         } else {
            setAction(R.id.play_or_stop);
         }
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {
         mServiceConnected = false;
         mHandle.removeCallbacks(mUpdateSeekbar);
      }
   };


   /**
    * Callbacks for the client
    */
   private MediaPlayerCallbacks mCallbacks = new MediaPlayerCallbacks() {

      @Override
      public void onError(Throwable throwable) {

      }

      @Override
      public void onStateChanged(State state) {
         if (!mServiceConnected) {
            return;
         }

         PlaylistItem playlistItem = mService.getCurrentPlayList();


         //We don't want to update the UI
         //if there is already a currently playing track and
         //the Session ID is different
         if (playlistItem != null &&
                 !playlistItem.isMatch(item.getSessionId())) {
            return;
         }

         updateCurrentPlayerState(state);
      }


      @Override
      public void onMetaDataChanged() {
         PlaylistItem playlistItem = mService.getCurrentPlayList();
         if (playlistItem != null && playlistItem.isMatch(item.getSessionId())) {
            updateCurrentTrackView();
         }
      }


      @Override
      public void onBufferingUpdate(int percent) {
      }
   };


   /**
    * Update current player state
    *
    * @param state Media Player State
    */
   private void updateCurrentPlayerState(State state) {
      switch (state) {
         case IDLE:
            mHandle.removeCallbacks(mUpdateSeekbar);
            mSeekBarView.setProgress(0);
            mDurationView.setText("");
            mTotalDurationView.setText("");
            mPlayOrPauseView.setImageResource(R.drawable.ic_play_arrow_48dp);
            break;
         case PAUSE:
            mSeekBarView.setEnabled(false);
            mHandle.removeCallbacks(mUpdateSeekbar);
            mPlayOrPauseView.setImageResource(R.drawable.ic_play_arrow_48dp);
            break;
         case STOP:
            break;
         case COMPLETED:
            break;
         case PREPARING:
            setProgressVisibility(true);
            mDurationView.setText(getString(R.string.loading_with_dot));
            mPlayOrPauseView.setImageResource(R.drawable.ic_pause_48dp);
            mSeekBarView.setEnabled(false);
            break;
         case PLAYING:
            mSeekBarView.setEnabled(true);
            setProgressVisibility(false);
            mHandle.post(mUpdateSeekbar);
            mPlayOrPauseView.setImageResource(R.drawable.ic_pause_48dp);
            break;
      }
   }

   @Override
   public boolean onMenuItemClick(MenuItem item) {
      return onOptionItemSelected(item);
   }
}
