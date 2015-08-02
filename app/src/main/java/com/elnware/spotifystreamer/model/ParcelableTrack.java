package com.elnware.spotifystreamer.model;

import android.os.Parcel;

import com.elnware.spotifystreamer.util.ParcelableUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TrackSimple;

/**
 * Created by elnoxvie on 7/8/15.
 */
public class ParcelableTrack extends ParcelableTrackSimple {
   public ParcelableAlbumSimple album;
   public Map<String, String>   external_ids;
   public Integer               popularity;

   public ParcelableTrack() {
   }

   public ParcelableTrack(List<ParcelableArtistSimple> artists, List<String> available_markets, Boolean is_playable, ParcelableLinkedTrack linked_from, int disc_number, long duration_ms, boolean explicit, Map<String, String> external_urls, String href, String id, String name, String preview_url, int track_number, String type, String uri, ParcelableAlbumSimple album, Map<String, String> external_ids, Integer popularity) {
      super(artists, available_markets, is_playable, linked_from, disc_number, duration_ms, explicit, external_urls, href, id, name, preview_url, track_number, type, uri);
      this.album = album;
      this.external_ids = external_ids;
      this.popularity = popularity;
   }

   @Override
   public int describeContents() {
      return 0;
   }

   @Override
   public void writeToParcel(Parcel dest, int flags) {
      super.writeToParcel(dest, flags);
      dest.writeParcelable(this.album, 0);
      ParcelableUtils.writeMap(dest, this.external_ids);
      dest.writeValue(this.popularity);
   }

   protected ParcelableTrack(Parcel in) {
      super(in);
      this.album = in.readParcelable(ParcelableAlbumSimple.class.getClassLoader());
      this.external_ids = ParcelableUtils.readMap(in);
      this.popularity = (Integer) in.readValue(Integer.class.getClassLoader());
   }

   public static final Creator<ParcelableTrack> CREATOR = new Creator<ParcelableTrack>() {
      public ParcelableTrack createFromParcel(Parcel source) {
         return new ParcelableTrack(source);
      }

      public ParcelableTrack[] newArray(int size) {
         return new ParcelableTrack[size];
      }
   };

   public static ParcelableTrack copy(Track simple){

      List<ParcelableArtistSimple> parcelableArtistSimples = new ArrayList<>();

      if (simple.artists != null){
         for(ArtistSimple artistSimple: simple.artists){
            parcelableArtistSimples.add(ParcelableArtistSimple.copy(artistSimple));
         }
      }

      return new ParcelableTrack(parcelableArtistSimples,
              simple.available_markets,
              simple.is_playable,
              ParcelableLinkedTrack.copy(simple.linked_from),
              simple.disc_number,
              simple.duration_ms,
              simple.explicit,
              simple.external_urls,
              simple.href,
              simple.id,
              simple.name,
              simple.preview_url,
              simple.track_number,
              simple.type,
              simple.uri,
              ParcelableAlbumSimple.copy(simple.album),
              simple.external_urls,
              simple.popularity );
   }
}
