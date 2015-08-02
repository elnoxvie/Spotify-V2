package com.elnware.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.elnware.spotifystreamer.util.ParcelableUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.TrackSimple;


/**
 * Created by elnoxvie on 7/8/15.
 */
public class ParcelableTrackSimple implements Parcelable {
   public List<ParcelableArtistSimple>  artists;
   public List<String>        available_markets;
   public Boolean             is_playable;
   public ParcelableLinkedTrack linked_from;
   public int                 disc_number;
   public long                duration_ms;
   public boolean             explicit;
   public Map<String, String> external_urls;
   public String              href;
   public String              id;
   public String              name;
   public String              preview_url;
   public int                 track_number;
   public String              type;
   public String              uri;

   public ParcelableTrackSimple() {
   }

   public ParcelableTrackSimple(List<ParcelableArtistSimple> artists, List<String> available_markets, Boolean is_playable, ParcelableLinkedTrack linked_from, int disc_number, long duration_ms, boolean explicit, Map<String, String> external_urls, String href, String id, String name, String preview_url, int track_number, String type, String uri) {
      this.artists = artists;
      this.available_markets = available_markets;
      this.is_playable = is_playable;
      this.linked_from = linked_from;
      this.disc_number = disc_number;
      this.duration_ms = duration_ms;
      this.explicit = explicit;
      this.external_urls = external_urls;
      this.href = href;
      this.id = id;
      this.name = name;
      this.preview_url = preview_url;
      this.track_number = track_number;
      this.type = type;
      this.uri = uri;
   }

   public static final Creator<ParcelableTrackSimple> CREATOR = new Creator<ParcelableTrackSimple>() {
      @Override
      public ParcelableTrackSimple createFromParcel(Parcel in) {
         return new ParcelableTrackSimple(in);
      }

      @Override
      public ParcelableTrackSimple[] newArray(int size) {
         return new ParcelableTrackSimple[size];
      }
   };

   @Override
   public int describeContents() {
      return 0;
   }

   @Override
   public void writeToParcel(Parcel dest, int flags) {
      dest.writeTypedList(artists);
      dest.writeStringList(this.available_markets);
      dest.writeValue(this.is_playable);
      dest.writeParcelable(this.linked_from, 0);
      dest.writeInt(this.disc_number);
      dest.writeLong(this.duration_ms);
      dest.writeByte(explicit ? (byte) 1 : (byte) 0);
      ParcelableUtils.writeMap(dest, this.external_urls);
      dest.writeString(this.href);
      dest.writeString(this.id);
      dest.writeString(this.name);
      dest.writeString(this.preview_url);
      dest.writeInt(this.track_number);
      dest.writeString(this.type);
      dest.writeString(this.uri);
   }

   protected ParcelableTrackSimple(Parcel in) {
      this.artists = in.createTypedArrayList(ParcelableArtistSimple.CREATOR);
      this.available_markets = in.createStringArrayList();
      this.is_playable = (Boolean) in.readValue(Boolean.class.getClassLoader());
      this.linked_from = in.readParcelable(ParcelableLinkedTrack.class.getClassLoader());
      this.disc_number = in.readInt();
      this.duration_ms = in.readLong();
      this.explicit = in.readByte() != 0;
      this.external_urls = ParcelableUtils.readMap(in);
      this.href = in.readString();
      this.id = in.readString();
      this.name = in.readString();
      this.preview_url = in.readString();
      this.track_number = in.readInt();
      this.type = in.readString();
      this.uri = in.readString();
   }

   public ParcelableTrackSimple copy(TrackSimple simple){

      List<ParcelableArtistSimple> parcelableArtistSimples = new ArrayList<>();

      if (simple.artists != null){
         for(ArtistSimple artistSimple: simple.artists){
            parcelableArtistSimples.add(ParcelableArtistSimple.copy(artistSimple));
         }
      }

      return new ParcelableTrackSimple(parcelableArtistSimples,
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
              simple.uri);
   }
}
