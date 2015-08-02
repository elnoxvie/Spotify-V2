package com.elnware.spotifystreamer.util;

import com.elnware.spotifystreamer.model.ParcelableImage;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Image utility tools for spotify data
 * Created by elnoxvie on 6/24/15.
 */
public class ImageUtils {


   /**
    * This function will try to pick for
    * the image that match the width. if none is found,
    * we will pick the slightly larger image that match the size.
    * This will help us save some bandwith when requesting the image size
    * @param images images from spotify api
    * @param width width criteria
    * @return image Image that fits the criteria
    */
   public static Image getOptimumImage(List<Image> images, int width) {
      // if images is empty, we will return null
      if (images.size() == 0) {
         return null;
      }

      Image image;
      /**
       * By Default the spotify Api return images that start from the biggest to smallest
       * we will reverse the images and pick the matching width. However, if none is found,
       * we will pick the closest to conserve bandwidth.
       */
      for (int i = images.size() -1; i > 0; i--){
         image = images.get(i);
         if (image.width == width) { // return the closest match
            return image;
         }

         if (image.width > width){ // return the slightly larger match if no matching found
            return image;
         }
      }

      return images.get(0);
   }


   /**
    * This function will try to pick for
    * the image that match the width. if none is found,
    * we will pick the slightly larger image that match the size.
    * This will help us save some bandwith when requesting the image size
    * @param images images from spotify api
    * @param width width criteria
    * @return image Image that fits the criteria
    */
   public static ParcelableImage getOptimumParcelableImage(List<ParcelableImage> images, int width) {
      // if images is empty, we will return null
      if (images.size() == 0) {
         return null;
      }

      ParcelableImage image;
      /**
       * By Default the spotify Api return images that start from the biggest to smallest
       * we will reverse the images and pick the matching width. However, if none is found,
       * we will pick the closest to conserve bandwidth.
       */
      for (int i = images.size() -1; i > 0; i--){
         image = images.get(i);
         if (image.width == width) { // return the closest match
            return image;
         }

         if (image.width > width){ // return the slightly larger match if no matching found
            return image;
         }
      }

      return images.get(0);
   }

   /**
    * Getting the largest Image
    * @param images
    * @return
    */
   public static Image getLargestImage(List<Image> images) {
      // if images is empty, we will return null
      if (images.size() == 0) {
         return null;
      }

      return images.get(0);
   }
}
