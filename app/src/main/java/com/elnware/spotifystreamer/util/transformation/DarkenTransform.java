package com.elnware.spotifystreamer.util.transformation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

/**
 * Created by elnoxvie on 6/28/15.
 */
public class DarkenTransform extends BitmapTransformation {

   public DarkenTransform(Context context) {
      super(context);
   }

   @Override
   protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
      Bitmap bitmap = pool.get(outWidth, outHeight, toTransform.getConfig());
      if (bitmap == null){
         bitmap = bitmap.createBitmap(outWidth, outHeight, toTransform.getConfig());
      }

      Canvas canvas = new Canvas(bitmap);
      Paint paint = new Paint();
      paint.setAntiAlias(true);
      paint.setColorFilter(new LightingColorFilter(Color.rgb(50, 50, 50), 0));
      canvas.drawBitmap(toTransform, 0, 0, paint);

      return bitmap;
   }

   @Override
   public String getId() {
      return "com.elnware.spotifystreamer.darkentransform";
   }
}
