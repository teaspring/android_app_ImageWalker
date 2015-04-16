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
    public String getItem(int position){
        if(position >=0 && position < imgNameList.size()){
            return imgNameList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position){
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent ){
        CheckableLayout chLayout;
        ImageView imageView;

        if(convertView == null){ // if it is not recycled
            GridView.LayoutParams thumbParams = new GridView.LayoutParams(
                    BaseGridActivity.ThumbnailWidth, BaseGridActivity.ThumbnailHeight);

            imageView = new ImageView(mContext);
            imageView.setLayoutParams(thumbParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            chLayout = new CheckableLayout(mContext);
            chLayout.setLayoutParams(thumbParams);

            // add ImageView to FrameLayout
            chLayout.addView(imageView);
        }else{
            chLayout = (CheckableLayout)convertView;
            imageView = (ImageView)chLayout.getChildAt(0);
        }

        BaseGridActivity.loadBitmapFromFile(getItem(position),imageView, mContext.getResources());

        return chLayout;
    }

    /*
    * update inner data of adapter
    * */
    public void updateDataList(List<String> images){
        imgNameList.clear();
        imgNameList.addAll(images);
        this.notifyDataSetChanged();
    }
}
