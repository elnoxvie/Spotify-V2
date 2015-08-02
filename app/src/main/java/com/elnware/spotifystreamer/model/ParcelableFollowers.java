package com.elnware.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Followers;

/**
 * Created by elnoxvie on 8/2/15.
 */
public class ParcelableFollowers implements Parcelable {
      public String href;
      public int total;

      public ParcelableFollowers() {
      }

      public ParcelableFollowers(String href, int total) {
            this.href = href;
            this.total = total;
      }

      @Override
      public int describeContents() {
            return 0;
      }

      @Override
      public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.href);
            dest.writeInt(this.total);
      }

      protected ParcelableFollowers(Parcel in) {
            this.href = in.readString();
            this.total = in.readInt();
      }

      public static final Parcelable.Creator<ParcelableFollowers> CREATOR = new Parcelable.Creator<ParcelableFollowers>() {
            public ParcelableFollowers createFromParcel(Parcel source) {
                  return new ParcelableFollowers(source);
            }

            public ParcelableFollowers[] newArray(int size) {
                  return new ParcelableFollowers[size];
            }
      };

      public static ParcelableFollowers copy(Followers followers){
            return new ParcelableFollowers(followers.href, followers.total);
      }
}
