package com.elnware.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by elnoxvie on 7/8/15.
 */
public class ParcelableImage implements Parcelable {

   public Integer width;
   public Integer height;
   public String url;

   public ParcelableImage() { }

   public ParcelableImage(Integer width, Integer height, String url) {
      this.width = width;
      this.height = height;
      this.url = url;
   }

   @Override
   public int describeContents() {
      return 0;
   }

   @Override
   public void writeToParcel(Parcel dest, int flags) {
      dest.writeValue(this.width);
      dest.writeValue(this.height);
      dest.writeString(this.url);
   }

   protected ParcelableImage(Parcel in) {
      this.width = (Integer) in.readValue(Integer.class.getClassLoader());
      this.height = (Integer) in.readValue(Integer.class.getClassLoader());
      this.url = in.readString();
   }

   public static final Parcelable.Creator<ParcelableImage> CREATOR = new Parcelable.Creator<ParcelableImage>() {
      public ParcelableImage createFromParcel(Parcel source) {
         return new ParcelableImage(source);
      }

      public ParcelableImage[] newArray(int size) {
         return new ParcelableImage[size];
      }
   };

   public static ParcelableImage copy(Image image){
      return new ParcelableImage(image.width, image.height, image.url);
   }
}
