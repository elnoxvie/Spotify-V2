package com.elnware.spotifystreamer.model;

import android.os.Parcel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by elnoxvie on 8/2/15.
 */
public class ParcelableArtist extends ParcelableArtistSimple{
   public ParcelableFollowers followers;
   public List<String>        genres;
   public List<ParcelableImage> images;
   public Integer popularity;

   public ParcelableArtist(Map<String, String> external_urls, String href, String id, String name, String type, String uri, ParcelableFollowers followers, List<String> genres, List<ParcelableImage> images, Integer popularity) {
      super(external_urls, href, id, name, type, uri);
      this.followers = followers;
      this.genres = genres;
      this.images = images;
      this.popularity = popularity;
   }

   @Override
   public int describeContents() {
      return 0;
   }

   @Override
   public void writeToParcel(Parcel dest, int flags) {
      super.writeToParcel(dest, flags);
      dest.writeParcelable(this.followers, flags);
      dest.writeStringList(this.genres);
      dest.writeTypedList(images);
      dest.writeValue(this.popularity);
   }

   protected ParcelableArtist(Parcel in) {
      super(in);
      this.followers = in.readParcelable(ParcelableFollowers.class.getClassLoader());
      this.genres = in.createStringArrayList();
      this.images = in.createTypedArrayList(ParcelableImage.CREATOR);
      this.popularity = (Integer) in.readValue(Integer.class.getClassLoader());
   }

   public static final Creator<ParcelableArtist> CREATOR = new Creator<ParcelableArtist>() {
      public ParcelableArtist createFromParcel(Parcel source) {
         return new ParcelableArtist(source);
      }

      public ParcelableArtist[] newArray(int size) {
         return new ParcelableArtist[size];
      }
   };

   public static ParcelableArtist copy(Artist artist){

      List<ParcelableImage> parcelableImageList = new ArrayList<>();
      if (artist.images != null){
         for(Image image: artist.images){
            parcelableImageList.add(ParcelableImage.copy(image));
         }
      }

      ParcelableFollowers followers = null;
      if (artist.followers != null){
         followers = ParcelableFollowers.copy(artist.followers);
      }

      return new ParcelableArtist(artist.external_urls,
              artist.href,
              artist.id,
              artist.name,
              artist.type,
              artist.uri,
              followers,
              artist.genres,
              parcelableImageList,
              artist.popularity);
   }
}
