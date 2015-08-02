package com.elnware.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.elnware.spotifystreamer.activity.MediaPlayerActivity;
import com.elnware.spotifystreamer.activity.SettingsPreferenceActivity;
import com.elnware.spotifystreamer.fragment.MediaPlayerFragment;
import com.elnware.spotifystreamer.fragment.NowPlayingInfoFragment;
import com.elnware.spotifystreamer.fragment.SearchFragment;
import com.elnware.spotifystreamer.activity.base.SingleFragmentActivity;
import com.elnware.spotifystreamer.fragment.TopTrackFragment;
import com.elnware.spotifystreamer.model.PlaylistItem;


/***
 * Main Activity
 */
public class MainActivity extends SingleFragmentActivity
        implements TopTrackFragment.TopTrackCallback,
        SearchFragment.SearchArtistCallback,
        NowPlayingInfoFragment.NowPlayingCallbacks,
        MediaPlayerFragment.MediaPlayerCallback
{

   @Override
   public Fragment createFragment() {
      return SearchFragment.newInstance();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.menu_main, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      int id = item.getItemId();

      if (id == R.id.action_settings) {
         startActivity(new Intent(this, SettingsPreferenceActivity.class));
         return true;
      }

      return super.onOptionsItemSelected(item);
   }

   @Override
   public void onSongSelected(PlaylistItem playlistItem) {
      Bundle bundle = new Bundle();
      bundle.putParcelable(TopTrackFragment.EXTRA_PLAY_LIST, playlistItem);

      MediaPlayerFragment mediaPlayerFragment = new MediaPlayerFragment();
      mediaPlayerFragment.setArguments(bundle);
      mediaPlayerFragment.show(getSupportFragmentManager(),
              MediaPlayerFragment.class.getSimpleName());
   }

   @Override
   protected void onNavigationButtonClick() { }

   @Override
   public void onArtistSelected(Bundle bundle) {
      setChildFragment(TopTrackFragment.newInstance(bundle));
   }

   @Override
   public void showNowPlaying(boolean isVisible) {
      showNowPlayingContainer(isVisible);
   }

   @Override
   public void onNowPlayingContainerSelected(PlaylistItem item) {
      Bundle bundle = new Bundle();
      bundle.putParcelable(TopTrackFragment.EXTRA_PLAY_LIST, item);

      if (hasTwoPanes()){
         MediaPlayerFragment mediaPlayerFragment = new MediaPlayerFragment();
         mediaPlayerFragment.setArguments(bundle);
         mediaPlayerFragment.show(getSupportFragmentManager(),
                 MediaPlayerFragment.class.getSimpleName());

      }else{
         Intent intent = new Intent(this, MediaPlayerActivity.class);
         intent.putExtra(TopTrackFragment.EXTRA_PLAY_LIST, item);
         startActivity(intent);
      }
   }

   @Override
   public void onDismiss() {


      //we know for sure that this will only be called
      //when dialog fragment is dismissed, so we will start the UI
      //as when the MediaPlayerFragment is run as dialog, the lifecycle
      //on now playing won't be called.
      showNowPlaying(true);
   }
}
