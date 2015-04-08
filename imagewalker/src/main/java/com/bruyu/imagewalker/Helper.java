package com.bruyu.imagewalker;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by bruyu on 4/4/15.
 */
public class Helper {

    /*
    * for image scale down considering both width and height
    * */
    public static int calcInSampleSizeDuo(BitmapFactory.Options options,
                                          int reqWidth, int reqHeight){
        final int height = options.outHeight;
        final int width  = options.outWidth;
        int inSampleSize = 1;

        while(width / inSampleSize > reqWidth
                && height / inSampleSize > reqHeight){
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    /*
    * for image scale down considering only width
    * */
    public static int calcInSampleSizeSingle(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight){
        final int width  = options.outWidth;
        int inSampleSize = 1;

        int halfWidth = width / 2;
        while((halfWidth / inSampleSize) >= reqWidth){
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    /*
     * it must be static as it will be invoked in AsyncTask.doInBackground()
     * */
    static Bitmap decodeSampledBitmapFromFile(String filePath,
                                                     int reqWidth, int reqHeight){
        /// first avoid allocate memory to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        options.inSampleSize = calcInSampleSizeDuo(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /*
    *
    * */
    static Bitmap decodeSampledBitmapFromResource(Resources resources, int resId,
                                                         int reqWidth, int reqHeight){
        /// first avoid allocate memory to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resId, options);

        options.inSampleSize = calcInSampleSizeDuo(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, resId, options);
    }
}
