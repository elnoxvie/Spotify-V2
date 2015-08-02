package com.elnware.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.elnware.spotifystreamer.util.ParcelableUtils;

import java.util.Map;

import kaaes.spotify.webapi.android.models.ArtistSimple;

/**
 * Created by elnoxvie on 7/8/15.
 */
public class ParcelableArtistSimple implements Parcelable {
   public Map<String, String> external_urls;
   public String              href;
   public String              id;
   public String              name;
   public String              type;
   public String uri;

   public ParcelableArtistSimple(Map<String, String> external_urls, String href, String id, String name, String type, String uri) {
      this.external_urls = external_urls;
      this.href = href;
      this.id = id;
      this.name = name;
      this.type = type;
      this.uri = uri;
   }

   public static final Creator<ParcelableArtistSimple> CREATOR = new Creator<ParcelableArtistSimple>() {
      @Override
      public ParcelableArtistSimple createFromParcel(Parcel in) {
         return new ParcelableArtistSimple(in);
      }

      @Override
      public ParcelableArtistSimple[] newArray(int size) {
         return new ParcelableArtistSimple[size];
      }
   };

   @Override
   public int describeContents() {
      return 0;
   }

   @Override
   public void writeToParcel(Parcel dest, int flags) {
      ParcelableUtils.writeMap(dest, this.external_urls);
      dest.writeString(this.href);
      dest.writeString(this.id);
      dest.writeString(this.name);
      dest.writeString(this.type);
      dest.writeString(this.uri);
   }

   protected ParcelableArtistSimple(Parcel in) {
      this.external_urls = ParcelableUtils.readMap(in);
      this.href = in.readString();
      this.id = in.readString();
      this.name = in.readString();
      this.type = in.readString();
      this.uri = in.readString();
   }

   public static ParcelableArtistSimple copy(ArtistSimple artistSimple){

      return new ParcelableArtistSimple(artistSimple.external_urls,
              artistSimple.href,
              artistSimple.id,
              artistSimple.name,
              artistSimple.type,
              artistSimple.uri);

   }
}
