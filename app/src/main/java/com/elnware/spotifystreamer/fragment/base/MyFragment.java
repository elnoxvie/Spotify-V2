package com.elnware.spotifystreamer.fragment.base;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.elnware.spotifystreamer.R;

/**
 * Base Fragment that implement retained fragment.
 * This will allow fragment with the need to request data
 * to automatically have it's data retained
 * Created by elnoxvie on 6/20/15.
 */
@SuppressWarnings("unchecked")
public abstract class MyFragment extends Fragment {

   protected ActionBar getCompatActionBar(){
      FragmentActivity activity = getActivity();
      if (activity != null){
         return ((AppCompatActivity) getActivity()).getSupportActionBar();
      }

      return null;
   }

   protected boolean hasTwoPanes(){
      return getResources().getBoolean(R.bool.has_two_panes);
   }

   protected AppCompatActivity getCompatActivity(){
      FragmentActivity activity = getActivity();
      if (activity != null){
         return (AppCompatActivity) getActivity();
      }

      return null;
   }

   protected Window getWindow(){
     return getActivity().getWindow();
   }

   protected FragmentManager getSupportFragmentManager(){
      return getActivity().getSupportFragmentManager();
   }
}
