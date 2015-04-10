package com.bruyu.imagewalker;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
import android.widget.AdapterView;
import android.view.View;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * main activity to display all images in grid
 */
public class GridActivity extends BaseGridActivity {
    private static final String TAG = "::GridActivity";
    public static final String imgPath = "/storage/sdcard0/DCIM/Camera";
    public static final Pattern imgPattern = Pattern.compile("\\.jpg$");

    private ImageFileAdapter mAdapter;
    private ArrayList<String> imgNameList;

    private int selectedItem = -1;
    protected ActionMode mActionMode;  // for action mode

    private int topN = 20;

    /// for all spawned activity which uses OpenCV lib
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, " OpenCV loaded successfully");
                    break;
                default:
                    Log.e(TAG, " OpenCV not connect manager!");
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        imgNameList = new ArrayList<>();

        readImageNames(imgPath);
        mAdapter = new ImageFileAdapter(this);
        mAdapter.setImageList(imgNameList);

        setContentView(R.layout.activity_grid);
        GridView gridView = (GridView)findViewById(R.id.gridview);
        gridView.setAdapter(mAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(selectedItem != -1){ // action mode is working
                    if(selectedItem != position){ // click another image, ignore the click
                        return;
                    }else if(mActionMode != null){ // click same image, cancel action mode
                        mActionMode.finish();
                        mActionMode = null;
                        selectedItem = -1;
                    }
                }
                startImageDetailActivity(position);
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent,
                    View view, int position, long id) {
                if(mActionMode != null){
                    return false;
                }
                selectedItem = position;
                mActionMode = GridActivity.this.startActionMode(mACB);
                view.setSelected(true);
                return false;
            }
        });
    }

    /*
    * to load openCV library
    * */
    @Override
    public void onResume(){
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, this, mLoaderCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_settings:
                StringBuilder builder = new StringBuilder("cache ");
                builder.append("size is ");
                builder.append(mMemoryCache.size());
                builder.append("KB, miss count is ");
                builder.append(mMemoryCache.missCount());
                builder.append(", hit count is ");
                builder.append(mMemoryCache.hitCount());

                Log.d(TAG, builder.toString());
            default:
                break;
        }
        return true;
    }

    /*
    * for action mode
    * */
    private ActionMode.Callback mACB = new ActionMode.Callback(){
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu){
            MenuInflater mInflater = mode.getMenuInflater();
            mInflater.inflate(R.menu.action_mode, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu){
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item){
            switch (item.getItemId()){
                case R.id.search:
                    startLimitedGridActivity(selectedItem);
                    mode.finish();
                    break;
                default:
                    return false;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode){
            mActionMode = null;
            selectedItem = -1;
        }
    };

    /*
    * start activity which display full-screen images in sliding
    * */
    private void startImageDetailActivity(int position){
        Intent intent = new Intent(getApplicationContext(), ImageDetailActivity.class);
        intent.putExtra(ImageDetailActivity.IMG_POSITION, position);

        ArrayList<String> sendImgNames = new ArrayList<>(imgNameList);
        intent.putStringArrayListExtra(ImageDetailActivity.IMG_FILELIST, sendImgNames);

        startActivity(intent);
    }

    /*
    * start activity which display image searching
    * */
    private void startLimitedGridActivity(int position){
        Intent intent = new Intent(getApplicationContext(), LimitedGridActivity.class);

        ArrayList<String> sendImgNames = new ArrayList<>(imgNameList);
        intent.putStringArrayListExtra(LimitedGridActivity.TESTIMGLIST, sendImgNames);

        intent.putExtra(LimitedGridActivity.BASEIMAGE, imgNameList.get(position));

        intent.putExtra(LimitedGridActivity.TOPN, topN);

        startActivity(intent);
    }

    private void readImageNames(String folderPath){
        imgNameList.clear();
        final File folder = new File(folderPath);
        if(!folder.exists())    return;
        for(final File fileEntry : folder.listFiles()){
            Matcher matcher = imgPattern.matcher(fileEntry.getName());
            if(matcher.find()){
                imgNameList.add(fileEntry.getPath());
            }
        }
    }
}
