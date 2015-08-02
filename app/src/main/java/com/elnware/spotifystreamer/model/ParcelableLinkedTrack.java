package com.elnware.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.elnware.spotifystreamer.util.ParcelableUtils;

import java.util.Map;

import kaaes.spotify.webapi.android.models.LinkedTrack;

/**
 * Created by elnoxvie on 7/8/15.
 */
public class ParcelableLinkedTrack implements Parcelable {
   public Map<String, String> external_urls;
   public String              href;
   public String              id;
   public String              type;
   public String              uri;

   public ParcelableLinkedTrack() { }

   public ParcelableLinkedTrack(Map<String, String> external_urls, String href, String id, String type, String uri) {
      this.external_urls = external_urls;
      this.href = href;
      this.id = id;
      this.type = type;
      this.uri = uri;
   }

   @Override
   public int describeContents() {
      return 0;
   }

   @Override
   public void writeToParcel(Parcel dest, int flags) {
      ParcelableUtils.writeMap(dest, this.external_urls);
      dest.writeString(this.href);
      dest.writeString(this.id);
      dest.writeString(this.type);
      dest.writeString(this.uri);
   }

   protected ParcelableLinkedTrack(Parcel in) {
      this.external_urls = ParcelableUtils.readMap(in);
      this.href = in.readString();
      this.id = in.readString();
      this.type = in.readString();
      this.uri = in.readString();
   }

   public static final Parcelable.Creator<ParcelableLinkedTrack> CREATOR = new Parcelable.Creator<ParcelableLinkedTrack>() {
      public ParcelableLinkedTrack createFromParcel(Parcel source) {
         return new ParcelableLinkedTrack(source);
      }

      public ParcelableLinkedTrack[] newArray(int size) {
         return new ParcelableLinkedTrack[size];
      }
   };

   public static ParcelableLinkedTrack copy(LinkedTrack linkedTrack){
      if (linkedTrack == null){
         return null;
      }

      return new ParcelableLinkedTrack(linkedTrack.external_urls, linkedTrack.href, linkedTrack.id,linkedTrack.type, linkedTrack.uri);
   }
}
