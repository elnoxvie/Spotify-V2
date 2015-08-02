package com.elnware.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;
import java.util.UUID;

/**
 * Playlist item for passing from fragment / activity to another
 * fragment / activity
 * Created by elnoxvie on 7/11/15.
 */
public class PlaylistItem implements Parcelable {
   private List<ParcelableTrack> mTrackList;
   private int mPosition = 0;
   private int mTrackSize;


   //A unique ID that will be used to identify the playlist
   private String mSessionId;

   public PlaylistItem(List<ParcelableTrack> mTrackList, int position) {
      this.mTrackList = mTrackList;
      this.mPosition = position;
      this.mTrackSize = mTrackList.size();
      this.mSessionId = UUID.randomUUID().toString();
   }

   public void next(){
      mPosition = ((mPosition + 1) >= mTrackSize)?
              mPosition = 0:
              (mPosition + 1);
   }

   public void previous(){
      mPosition = ((mPosition - 1) >= 0) ?
              (mPosition - 1) :
              (mTrackSize - 1);

   }

   public boolean isMatch(String sessionId){
      return this.mSessionId.equals(sessionId);
   }

   public ParcelableTrack getCurrentTrack(){
      return mTrackList.get(mPosition);
   }

   public List<ParcelableTrack> getmTrackList() {
      return mTrackList;
   }

   public void setTrackList(List<ParcelableTrack> mTrackList) {
      this.mTrackList = mTrackList;
   }

   public int getCurrentPosition() {
      return mPosition;
   }

   public String getSessionId() {
      return mSessionId;
   }

   public void setSessionId(String mSessionId) {
      this.mSessionId = mSessionId;
   }

   public void setCurrentPosition(int position){
      mPosition = position;
   }

   @Override
   public int describeContents() {
      return 0;
   }

   @Override
   public void writeToParcel(Parcel dest, int flags) {
      dest.writeTypedList(mTrackList);
      dest.writeInt(this.mPosition);
      dest.writeInt(this.mTrackSize);
      dest.writeString(this.mSessionId);
   }

   protected PlaylistItem(Parcel in) {
      this.mTrackList = in.createTypedArrayList(ParcelableTrack.CREATOR);
      this.mPosition = in.readInt();
      this.mTrackSize = in.readInt();
      this.mSessionId = in.readString();
   }

   public static final Parcelable.Creator<PlaylistItem> CREATOR = new Parcelable.Creator<PlaylistItem>() {
      public PlaylistItem createFromParcel(Parcel source) {
         return new PlaylistItem(source);
      }

      public PlaylistItem[] newArray(int size) {
         return new PlaylistItem[size];
      }
   };
}
