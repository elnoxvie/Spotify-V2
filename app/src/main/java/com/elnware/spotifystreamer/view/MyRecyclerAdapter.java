package com.elnware.spotifystreamer.view;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.ViewGroup;

import com.elnware.spotifystreamer.util.SparseBooleanArrayParcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom RecyclerViewAdapter that implements onClick and OnLongItemClick
 * and some common functions similar to those in ListView
 * Created by elnoxvie on 6/21/15.
 * @param <L> List Type
 * @param <M> Class that extends MyRecylerViewHolder
 * @param <T> Type of the class returned by getItem(int position)
 */
@SuppressWarnings("unchecked")
public class MyRecyclerAdapter<L extends List, M extends MyRecyclerViewHolder, T> extends RecyclerView.Adapter<M>{
   private static final String TAG = MyRecyclerAdapter.class.getSimpleName();

   private L                                 mData;
   private MyRecyclerView.MyRecylerCallbacks mCallbacks;
   private SparseBooleanArrayParcelable      mSelectedItems;

   public MyRecyclerAdapter(L data) {
      this.mData = data;
      this.mSelectedItems = new SparseBooleanArrayParcelable();
   }

   public void setRecyclerCallbacks(MyRecyclerView.MyRecylerCallbacks callbacks) {
      this.mCallbacks = callbacks;
   }

   public void setItemChecked(int position, boolean isChecked) {
      mSelectedItems.clear();
      mSelectedItems.put(position, isChecked);
      notifyDataSetChanged();
   }

   /**
    * Indicates if the item at position position is selected
    * @param position Position of the item to check
    * @return true if the item is selected, false otherwise
    */
   public boolean isSelected(int position) {
      return getSelectedItems().contains(position);
   }

   /**
    * Toggle the selection status of the item at a given position
    * @param position Position of the item to toggle the selection status for
    */
   public void toggleSelection(int position) {
      if (mSelectedItems.get(position, false)) {
         mSelectedItems.delete(position);
      } else {
         mSelectedItems.put(position, true);
      }
      notifyItemChanged(position);
   }

   /**
    * Clear the selection status for all items
    */
   public void clearSelection() {
      List<Integer> selection = getSelectedItems();
      mSelectedItems.clear();
      for (Integer i : selection) {
         notifyItemChanged(i);
      }
   }

   /**
    * Count the selected items
    * @return Selected items count
    */
   public int getSelectedItemCount() {
      return mSelectedItems.size();
   }

   /**
    * Save the state of the current selection on the items.
    * @param outState
    */
   public void onSaveInstanceState(Bundle outState) {
      outState.putParcelable(TAG, mSelectedItems);
   }

   /**
    * Restore the previous state of the selection on the items.
    * @param savedInstanceState
    */
   public void onRestoreInstanceState(Bundle savedInstanceState) {
      mSelectedItems = savedInstanceState.getParcelable(TAG);
   }

   /**
    * Indicates the list of selected items
    * @return List of selected items ids
    */
   public List<Integer> getSelectedItems() {
      List<Integer> items = new ArrayList<>(mSelectedItems.size());
      for (int i = 0; i < mSelectedItems.size(); ++i) {
         items.add(mSelectedItems.keyAt(i));
      }
      return items;
   }

   public MyRecyclerView.MyRecylerCallbacks getRecyclerCallbacks(){
      return mCallbacks;
   }

   public L getItems(){
      return mData;
   }

   public void changeData(L data){
      mData = data;
      notifyDataSetChanged();
   }

   @Override
   public M onCreateViewHolder(ViewGroup viewGroup, int i) {  return null; }

   @Override
   public void onBindViewHolder(M holder , int i) {
   }

   @Override
   public int getItemCount() {
      return mData.size();
   }

   @SuppressWarnings("unchecked")
   public T getItem(int position){
      //noinspection unchecked
      return (T) mData.get(position);
   }
}
