package com.elnware.spotifystreamer.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Recycler ViewHolder that provides View OnClickListener
 * This will be used along with MyRecyclerView to
 * provide On Item Click and On Long Item Click Listener
 * Created by elnoxvie on 6/21/15.
 */
public abstract class MyRecyclerViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnLongClickListener{
   public MyRecyclerViewHolder(View view) {
      super(view);
      itemView.setOnClickListener(this);
      itemView.setOnLongClickListener(this);
   }

   @Override
   public void onClick(View view) { }

   @Override
   public boolean onLongClick(View view) {
      return false;
   }
}
