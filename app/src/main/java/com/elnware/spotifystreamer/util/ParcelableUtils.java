package com.elnware.spotifystreamer.util;

import android.os.Parcel;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by elnoxvie on 7/8/15.
 */
public class ParcelableUtils {

   public static void writeMap(Parcel dest, Map<String,String> map){
      int size = map == null? 0: map.size();
      dest.writeInt(size);
      if (size > 0){
         for(String key: map.keySet()){
            dest.writeString(key);
            dest.writeString(map.get(key));
         }
      }
   }

   public static Map<String,String> readMap(Parcel in){
      int size = in.readInt();
      Map<String,String> map = new HashMap<>();
      if (size > 0){
         for(int i = 0; i < size;i++){
            map.put(in.readString(), in.readString());
         }
      }

      return map;
   }
}
