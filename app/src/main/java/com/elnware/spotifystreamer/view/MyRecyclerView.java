package com.elnware.spotifystreamer.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Recyclerview that provides emptyView.
 * Similar to the AbsListView
 * Created by elnoxvie on 6/21/15.
 */
public class  MyRecyclerView extends RecyclerView{
   @Nullable
   private View               mEmptyView;
   private SparseBooleanArray mSelectedItems;

   public interface MyRecylerCallbacks {
      void OnItemClick(View view, int position);

      void OnLongItemClick(View view, int position);
   }

   public MyRecyclerView(Context context) {
      super(context);
   }

   public MyRecyclerView(Context context, AttributeSet attrs) {
      super(context, attrs);
   }

   public MyRecyclerView(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
   }

   public static class SimpleRecyclerCallbacks implements MyRecylerCallbacks {

      @Override
      public void OnItemClick(View view, int position) {
      }

      @Override
      public void OnLongItemClick(View view, int position) {
      }
   }

   public void setEmptyView(View view) {
      mEmptyView = view;
      checkForEmptyView();
   }

   @Override
   public void setAdapter(Adapter adapter) {
      final Adapter oldAdapter = getAdapter();
      if (oldAdapter != null) {
         oldAdapter.unregisterAdapterDataObserver(mObserver);
      }
      super.setAdapter(adapter);
      if (adapter != null){
         adapter.registerAdapterDataObserver(mObserver);
      }

      checkForEmptyView();
   }

   private void checkForEmptyView() {
      if (mEmptyView != null && getAdapter() != null){
         if (getAdapter().getItemCount() > 0){
            mEmptyView.setVisibility(View.GONE);
            setVisibility(View.VISIBLE);
         }else{
            mEmptyView.setVisibility(View.VISIBLE);
            setVisibility(View.GONE);
         }
      }
   }

   final private AdapterDataObserver mObserver = new AdapterDataObserver() {
      @Override
      public void onChanged() {
         super.onChanged();
         checkForEmptyView();
      }

      @Override
      public void onItemRangeInserted(int positionStart, int itemCount) {
         super.onItemRangeInserted(positionStart, itemCount);
         checkForEmptyView();
      }

      @Override
      public void onItemRangeRemoved(int positionStart, int itemCount) {
         super.onItemRangeRemoved(positionStart, itemCount);
         checkForEmptyView();
      }
   };

}
