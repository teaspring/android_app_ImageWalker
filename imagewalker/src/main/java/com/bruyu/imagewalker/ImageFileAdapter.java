package com.bruyu.imagewalker;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * image adapter for GridView
 */
public class ImageFileAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> imgNameList;

    public ImageFileAdapter(Context cxt){
        mContext = cxt;
        imgNameList = new ArrayList<>();
    }

    @Override
    public int getCount(){
        return imgNameList.size();
    }

    @Override
    public Object getItem(int position){
        return null;
    }

    @Override
    public long getItemId(int position){
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent ){
        ImageView imageView;
        if(convertView == null){ // if it is not recycled
            imageView = new ImageView(mContext);
            GridView.LayoutParams mParams = new GridView.LayoutParams(
                    BaseGridActivity.ThumbnailWidth, BaseGridActivity.ThumbnailHeight);
            imageView.setLayoutParams(mParams);

            // scale image uniformly, crop it to fill the fixed width/height
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }else{
            imageView = (ImageView)convertView;
        }

        if(mContext instanceof BaseGridActivity){
            BaseGridActivity mGridActivity = (BaseGridActivity)mContext;
            mGridActivity.loadBitmapFromFile(imgNameList.get(position),
                    imageView, mContext.getResources());
        }else {
            Bitmap bm = Helper.decodeSampledBitmapFromFile(imgNameList.get(position),
                    BaseGridActivity.ThumbnailWidth, BaseGridActivity.ThumbnailHeight);
            imageView.setImageBitmap(bm);
        }

        return imageView;
    }

    public void setImageList(List<String> images){
        imgNameList.clear();
        imgNameList.addAll(images);
    }
}
