package com.elnware.spotifystreamer.activity.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.elnware.spotifystreamer.R;

/**
 * Convenient Class that wrap fragment inside Activity
 * toolbarless version
 * Created by elnoxvie on 6/20/15.
 */
public abstract class SingleFragmentToolbarlessActivity extends AppCompatActivity {

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(getLayoutId());

      if (savedInstanceState == null){
         getSupportFragmentManager()
                 .beginTransaction()
                 .add(R.id.fragment_container, createFragment())
                 .commit();
      }
   }

   public int getLayoutId(){
      return R.layout.main_toolbarless;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()){
         case android.R.id.home:
            onNavigationButtonClick();
            break;
      }

      return super.onOptionsItemSelected(item);
   }

   protected void onNavigationButtonClick(){ }

   protected abstract Fragment createFragment();
}
