package com.bruyu.imagewalker;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by bruyu on 4/4/15.
 */
public class Helper {

    // official MI1S screen size is around 480 * 800
    public static final int FullWidth_Portrait = 480;
    public static final int FullHeight_Portrait = 800;

    public static final int FullWidth_Landscape = 800;
    public static final int FullHeight_Landscape = 480;

    // initial is portrait mode
    private static AtomicBoolean inLandscape = new AtomicBoolean(false);

    /*
    * support external static method to get screen orientation status
    * */
    public static boolean isLandscape(){
        return inLandscape.get();
    }

    /*
    * set atomic boolean flag of screen orientation
    * */
    public static void toggleLandscape(boolean turnLandscape){
        inLandscape.set(turnLandscape);
    }

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
    * BitmapFactory.Options.inSampleSize must be power of 2. if not, rounded to the nearest
    * for portrait orientation, scale down image based on width only
    * for landscape orientation, scale down image based on height only
    * */
    public static int calcInSampleSizeSingle(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight){
        int length = 0, reqLength = 0;
        if(isLandscape()){ // for landscape full screen, image height is aligned strictly
            length = options.outHeight;
            reqLength = reqHeight;
        }else{ // for horizontal full screen, image width is aligned strictly
            length = options.outWidth;
            reqLength = reqWidth;
        }

        int inSampleSize = 1;
        int halfLength = length / 2;
        while((halfLength / inSampleSize) >= reqLength){
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    /*
    * decode image for full screen
    * */
    static Bitmap decodeSampledBitmap4Full(String filePath){
        // define requested width and height firstly
        int reqWidth = Helper.isLandscape() ?
                Helper.FullWidth_Landscape : Helper.FullWidth_Portrait;
        int reqHeight = Helper.isLandscape() ?
                Helper.FullHeight_Landscape : Helper.FullHeight_Portrait;

        // measure in width/height of image
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // calculate scale down parameter for out width/height
        options.inSampleSize = calcInSampleSizeSingle(options, reqWidth, reqHeight);

        // real decode
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
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
