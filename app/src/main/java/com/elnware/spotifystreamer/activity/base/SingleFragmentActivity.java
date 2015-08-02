package com.elnware.spotifystreamer.activity.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.elnware.spotifystreamer.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Convenient Class that wrap fragment inside Activity
 * Created by elnoxvie on 6/20/15.
 */
public abstract class SingleFragmentActivity extends AppCompatActivity
        implements FragmentManager.OnBackStackChangedListener{

   @Bind(R.id.controls_container)
   View mControlContainerView;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      ButterKnife.bind(this);

      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
      getSupportActionBar().setIcon(R.mipmap.ic_launcher);

      if (savedInstanceState == null) {
         getSupportFragmentManager()
                 .beginTransaction()
                 .add(R.id.fragment_container, createFragment())
                 .commit();
      }else{
         enableOrDisableHomeAsUp();
      }

      getSupportFragmentManager().addOnBackStackChangedListener(this);
   }


   //set the now playing container visibility
   public void showNowPlayingContainer(boolean isShown){
      if (isShown){
         mControlContainerView.setVisibility(View.VISIBLE);
      }else{
         mControlContainerView.setVisibility(View.GONE);
      }
   }


   //set detail fragment
   public void setChildFragment(Fragment fragment){
      getSupportFragmentManager()
              .beginTransaction()
      .replace(R.id.fragment_detail_container, fragment)
      .commit();
   }


   //check if activity contains two panes
   //this is usually a tablet
   public boolean hasTwoPanes(){
      return getResources().getBoolean(R.bool.has_two_panes);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()){
         case android.R.id.home:
            FragmentManager fm = getSupportFragmentManager();
            int count = fm.getBackStackEntryCount();
            if (count > 0){
               fm.popBackStackImmediate();
            } else{
               onNavigationButtonClick();
            }
            break;
      }

      return super.onOptionsItemSelected(item);
   }

   protected void onNavigationButtonClick(){ }

   @Override
   public void onBackStackChanged() {
      enableOrDisableHomeAsUp();
   }


   /** Enable or disable home as up when fragment changes **/
   private void enableOrDisableHomeAsUp() {
      FragmentManager fm    = getSupportFragmentManager();
      int             count = fm.getBackStackEntryCount();
      if (count > 0) {
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      } else {
         getSupportActionBar().setDisplayHomeAsUpEnabled(false);
      }
   }

   protected abstract Fragment createFragment();

}
