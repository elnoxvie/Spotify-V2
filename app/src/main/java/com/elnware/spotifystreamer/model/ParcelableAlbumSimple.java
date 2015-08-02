package com.elnware.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.elnware.spotifystreamer.util.ParcelableUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by elnoxvie on 7/7/15.
 */
public class ParcelableAlbumSimple implements Parcelable {
      public String              album_type;
      public List<String>        available_markets;
      public Map<String, String> external_urls;
      public String              href;
      public String              id;
      public List<ParcelableImage> images;
      public String              name;
      public String              type;
      public String              uri;


   public ParcelableAlbumSimple(String album_type, List<String> available_markets, Map<String, String> external_urls, String href, String id, List<ParcelableImage> images, String name, String type, String uri) {
      this.album_type = album_type;
      this.available_markets = available_markets;
      this.external_urls = external_urls;
      this.href = href;
      this.id = id;
      this.images = images;
      this.name = name;
      this.type = type;
      this.uri = uri;
   }

   public ParcelableAlbumSimple() {
   }

   @Override
   public int describeContents() {
      return 0;
   }

   @Override
   public void writeToParcel(Parcel dest, int flags) {
      dest.writeString(this.album_type);
      dest.writeStringList(this.available_markets);
      ParcelableUtils.writeMap(dest, this.external_urls);
      dest.writeString(this.href);
      dest.writeString(this.id);
      dest.writeTypedList(images);
      dest.writeString(this.name);
      dest.writeString(this.type);
      dest.writeString(this.uri);
   }

   protected ParcelableAlbumSimple(Parcel in) {
      this.album_type = in.readString();
      this.available_markets = in.createStringArrayList();
      this.external_urls = ParcelableUtils.readMap(in);
      this.href = in.readString();
      this.id = in.readString();
      this.images = in.createTypedArrayList(ParcelableImage.CREATOR);
      this.name = in.readString();
      this.type = in.readString();
      this.uri = in.readString();
   }

   public static final Parcelable.Creator<ParcelableAlbumSimple> CREATOR = new Parcelable.Creator<ParcelableAlbumSimple>() {
      public ParcelableAlbumSimple createFromParcel(Parcel source) {
         return new ParcelableAlbumSimple(source);
      }

      public ParcelableAlbumSimple[] newArray(int size) {
         return new ParcelableAlbumSimple[size];
      }
   };

    public static ParcelableAlbumSimple copy(AlbumSimple album){

      List<ParcelableImage> images = null;

      if (album.images != null && album.images.size() > 0){
         images = new ArrayList<>();
      }

      for(Image image : album.images){
         images.add(ParcelableImage.copy(image));
      }

      return new ParcelableAlbumSimple(album.album_type,
              album.available_markets,
              album.external_urls,
              album.href,
              album.id,
              images,
              album.name,
              album.type,
              album.uri);

   }
}
