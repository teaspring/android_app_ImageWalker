package com.bruyu.imagewalker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * BaseAdapter to process image file dynamically in grid view
 */
public class DynamicImageFileAdapter extends ArrayAdapter {
    public static final String RAWIMAGELIST = "RAWIMAGELIST";

    private Context mContext;
    private ArrayList<String> rawImageList;
    private ArrayList<String> selectImageList;

    public DynamicImageFileAdapter(Context cxt, int resId, List<String> data){
        super(cxt, resId, data);
        mContext = cxt;
        rawImageList = new ArrayList<>();
        selectImageList = new ArrayList<>(data);
    }

    @Override
    public int getCount(){
        return selectImageList.size();
    }

    @Override
    public String getItem(int position){
        return selectImageList.get(position);
    }

    @Override
    public long getItemId(int position){
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent ) {
        ImageView imageView;
        if(convertView == null){ // if it is not recycled
            imageView = new ImageView(mContext);
            GridView.LayoutParams mParams = new GridView.LayoutParams(
                    BaseGridActivity.ThumbnailWidth,
                    BaseGridActivity.ThumbnailHeight);
            imageView.setLayoutParams(mParams);

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }else{
            imageView = (ImageView)convertView;
        }

        BaseGridActivity.loadBitmapFromFile(getItem(position), imageView, mContext.getResources());
        return imageView;
    }

    public void setArguments(Intent intent){
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            if(bundle.containsKey(RAWIMAGELIST)){
                this.rawImageList.clear();
                this.rawImageList.addAll(bundle.getStringArrayList(RAWIMAGELIST));
            }
        }
    }

    /*
    * use CriticalSection to control the asynchronous access
    * */
    public synchronized void updateDataList(List<String> data){
        selectImageList.clear();
        selectImageList.addAll(data);

        this.notifyDataSetChanged();
    }

    synchronized List<String> getDataList(){
        return selectImageList;
    }
}
