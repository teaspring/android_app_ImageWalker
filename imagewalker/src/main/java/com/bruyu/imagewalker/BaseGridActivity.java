package com.bruyu.imagewalker;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;

import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by bruyu on 3/10/15.
 */
public class BaseGridActivity extends Activity{
    private static final String TAG = "BaseGridActivity";

    // below width/height is tested to be optimal for GridView image on MI1S
    public static final int ThumbnailWidth = 115;
    public static final int ThumbnailHeight = 115;

    protected static LruCache<String, Bitmap> mMemoryCache; // for memory cache
    protected static Bitmap mPlaceHolderBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        /// stored in KB,used by constructor LruCache(maxSize)
        /// on MI1S, maxMemory is 65536KB, equals to 64MB
        final int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);
        /*
        * use portion of available memory for this memory cache
        * a full screen GridView filled with images on device 800 * 400 resolutions
        * would use around 1.5 MB (800*400* 4 bytes).
        * */
        final int cacheSize = maxMemory / 4;

        /// use RetainFragment to hold LruCache<> across activity recreation
        RetainFragment retainFragment =
                RetainFragment.findOrCreateRetainFragment(getFragmentManager());

        mMemoryCache = retainFragment.mRetainCache;
        if(mMemoryCache == null){
            mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
                @Override
                protected int sizeOf(String key, Bitmap bitmap){
                    /// cache size will be measured in KB rather than number of entries by default
                    return bitmap.getByteCount() / 1024;
                }
            };
            retainFragment.mRetainCache = mMemoryCache;
        }

        mPlaceHolderBitmap = Helper.decodeSampledBitmapFromResource(
                getResources(),
                R.drawable.allblack,
                ThumbnailWidth,
                ThumbnailHeight);
    }

    public static void addBitmapToMemoryCache(String key, Bitmap bitmap){
        if(getBitmapFromMemCache(key) == null){
            mMemoryCache.put(key, bitmap);
        }
    }

    public static Bitmap getBitmapFromMemCache(String key){
        return mMemoryCache.get(key);
    }

    /*
    * trigger method which can be invoked by adapter
    * */
    public static void loadBitmapFromFile(String filePath, ImageView imageView, Resources res){
        final Bitmap bitmap = getBitmapFromMemCache(filePath);
        if(bitmap != null){
            imageView.setImageBitmap(bitmap);
            return;
        }

        if(cancelPotentialWork(filePath, imageView)){
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            final AsyncDrawable asyncDrawable
                    = new AsyncDrawable(res, mPlaceHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(filePath);
        }
    }

    /*
     * AsyncTask<Params, Progress, Result>
     * use WeakReference to ensure the holding resource can be garbage collected
     * currently it decodes image to Thumbnail width/height only
     * */
    public static class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String data;

        public BitmapWorkerTask(ImageView imageView){
            imageViewReference = new WeakReference<>(imageView);
        }

        /// decode image in background
        protected Bitmap doInBackground(String...params){
            data = params[0];  // image file path
            return Helper.decodeSampledBitmapFromFile(data, ThumbnailWidth, ThumbnailHeight);
        }

        /*
         * onPoseExecute(Result) runs on UI thread after doInBackground(Params...)
         * the specified result is value returned by doInBackground(Params)
         * */
        protected void onPostExecute(Bitmap bitmap){
            if(bitmap == null){
                Log.e(TAG, "decode() fails - " + data);
                return;
            }

            if(isCancelled()){
                bitmap = null;
            }

            if(imageViewReference != null && bitmap != null){
                addBitmapToMemoryCache(data, bitmap);  // with memory cache ready

                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                if( this == bitmapWorkerTask && imageView != null){
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    /*
    * sub class of BitmapDrawable holding a AsyncTask sub
    * purpose: as middle layer between ImageView and AsyncTask
    * @param bitmap: place-holder bitmap
    * */
    public static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask){
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask(){
            return bitmapWorkerTaskReference.get();
        }
    }

    public static boolean cancelPotentialWork(String data, ImageView imageView){
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if(bitmapWorkerTask != null){
            final String imgFilePath = bitmapWorkerTask.data;
            if(imgFilePath == null || !imgFilePath.equals(data)){
                bitmapWorkerTask.cancel(true);
            }else{
              return false; // the same image file is in decoding with this ImageView
            }
        }
        return true; // no task associated with this ImageView or an existing one is cancelled
    }

    public static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView){
        if(imageView != null){
            final Drawable drawable = imageView.getDrawable();
            if(drawable instanceof AsyncDrawable){
                final AsyncDrawable asyncDrawable = (AsyncDrawable)drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    /*
    * Fragment sub class holding a LruCache to retain
    * */
    public static class RetainFragment extends Fragment {
        private static final String TAG = "RetainFragment";
        public LruCache<String, Bitmap> mRetainCache;

        public RetainFragment(){}

        public static RetainFragment findOrCreateRetainFragment(FragmentManager fm){
            RetainFragment fragment = (RetainFragment)fm.findFragmentByTag(TAG);
            if(fragment == null){
                fragment = new RetainFragment();
                fm.beginTransaction().add(fragment, TAG).commit();
            }
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            /// Fragment feature, to control a fragment instance to be retained
            /// across Activity re-creation
            setRetainInstance(true);
        }
    }
}
