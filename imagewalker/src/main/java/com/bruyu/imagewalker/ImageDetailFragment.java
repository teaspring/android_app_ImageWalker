package com.bruyu.imagewalker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 *
 */
public class ImageDetailFragment extends Fragment{
    public static final String TAG = "ImageDetailFragment";

    public static final String IMG_FILENAME = "FILENAME";

    private String fileName;

    private ImageView mImageView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
               Bundle savedInstanceState){
        ViewGroup rootView
                = (ViewGroup)inflater.inflate(R.layout.detail_image_fragment, container, false);
        mImageView = (ImageView)rootView.findViewById(R.id.imageDetail);

        BitmapFarmerTask task = new BitmapFarmerTask(mImageView);
        task.execute(fileName);

        return rootView;
    }

    /*
    * set necessary arguments by data adapter
    * */
    public void setArguments(Bundle args){
        if(args.containsKey(IMG_FILENAME)){
            fileName = args.getString(IMG_FILENAME);
        }
    }

    /*
    * subclass of AsyncTask to execute full screen image decode
    * */
    static class BitmapFarmerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String data;

        public BitmapFarmerTask(ImageView imageView){
            imageViewReference = new WeakReference<>(imageView);
        }

        protected Bitmap doInBackground(String...params){
            data = params[0];
            return Helper.decodeSampledBitmap4Full(data);
        }

        protected void onPostExecute(Bitmap bitmap){
            if(bitmap == null){
                return;
            }

            if(isCancelled()){
                bitmap = null;
            }

            if(imageViewReference != null && bitmap != null){
                StringBuilder builder = new StringBuilder("decoded full screen bitmap width ");
                builder.append(bitmap.getWidth());
                builder.append(", height ");
                builder.append(bitmap.getHeight());
                builder.append(", KBytes: ");
                builder.append(bitmap.getByteCount() / 1024);
                Log.i(TAG, builder.toString());

                final ImageView imageView = imageViewReference.get();
                if(imageView != null){
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
}
