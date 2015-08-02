package com.elnware.spotifystreamer.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import com.elnware.spotifystreamer.R;
import com.elnware.spotifystreamer.activity.base.SingleFragmentActivity;
import com.elnware.spotifystreamer.activity.base.SingleFragmentToolbarlessActivity;
import com.elnware.spotifystreamer.fragment.MediaPlayerFragment;

/**
 * Media Player Activity for playing streaming
 * Created by elnoxvie on 7/5/15.
 */
public class MediaPlayerActivity extends SingleFragmentToolbarlessActivity{

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowTitleEnabled(false);
      getSupportActionBar().setIcon(null);
   }

   @Override
   protected Fragment createFragment() {
      Bundle bundle = getIntent().getExtras();
      return MediaPlayerFragment.newInstance(bundle);
   }

   @Override
   public int getLayoutId() {
      return R.layout.fragment_container_toolbar_shadow;
   }
}
