package com.bruyu.imagewalker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by bruyu on 3/19/15.
 */
public class LimitedGridActivity extends BaseGridActivity {
    private static final String TAG = "::LimitedActivity";

    public static final String BASEIMAGE = "BASEIMAGE";
    public static final String TESTIMGLIST = "TESTIMGLIST";
    public static final String TOPN = "TOPN";

    private String baseImage;
    private ArrayList<String> testImgNameList = new ArrayList<String>();
    private int topN = 12;
    private DynamicImageFileAdapter mAdapter;
    private ImageView baseView;

    private AtomicInteger searchProgress;

    private int progressBar;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.search_grid);

        searchProgress = new AtomicInteger(); // default initial 0

        // clean ImageManager static structures in advance
        ImageManager.cleanHouse();

        progressBar = 0;

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            if(extras.containsKey(TESTIMGLIST)){
                testImgNameList.addAll(extras.getStringArrayList(TESTIMGLIST));
            }

            if(extras.containsKey(BASEIMAGE)){
                baseImage = extras.getString(BASEIMAGE);
            }

            if(extras.containsKey(TOPN)){
                topN = extras.getInt(TOPN);
            }
        }

        baseView = (ImageView)findViewById(R.id.baseImage);

        mAdapter = new DynamicImageFileAdapter(this, 0, new ArrayList<String>());

        Intent intent = new Intent();
        intent.putStringArrayListExtra(DynamicImageFileAdapter.RAWIMAGELIST, testImgNameList);
        mAdapter.setArguments(intent);

        GridView gridView = (GridView)findViewById(R.id.searchResult);
        gridView.setAdapter(mAdapter);
    }

    @Override
    public void onResume(){
        super.onResume();

        // load base image on top of search result
        BaseGridActivity.loadBitmapFromFile(baseImage, baseView, getResources());

        if(searchProgress.get() == 0) {
            Log.i(TAG, "base image is " + baseImage);

            // update top N images of searching
            ImageManager.updateTopN(topN);

            // push image search task to thread pool
            for (String testImg : testImgNameList) {
                ImageManager.startCompare(this, baseImage, testImg);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_update, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.check:
                checkProgress();
                break;
            default:
                return false;
        }
        return true;
    }

    /*
    *
    * */
    public void onAdapterDataChanges(List<Integer> keys, List<String> data){
        Log.d(TAG, ":AdapterDataChanges(): " + keys.toString() + ", " + data.toString());
        mAdapter.updateDataList(data);
    }

    /*
    * internal counter of image compare
    * access to package
    * */
    int increaseProgress(){
        return searchProgress.incrementAndGet();
    }

    /*
    *
    * */
    private void checkProgress(){
        int status = searchProgress.get();
        int imgCount = testImgNameList.size(); // remove base image from the repository

        if(status == imgCount){
            Toast.makeText(this, "search in all test images is done!",
                    Toast.LENGTH_SHORT).show();
        }else if(status < imgCount){
            StringBuilder builder = new StringBuilder("currently compared ");
            builder.append(status);
            builder.append(" images");

            Toast.makeText(this, builder.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /*
    * show a toast to indicate the search is done!
    * */
    void proposeProgress(){
        int status = searchProgress.get();
        int count = testImgNameList.size();

        if(status == count){
            progressBar = 100;  // 100%
            Toast.makeText(this, "Search in " + count + " images is done.",
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "search in " + count + " images is done");
            return;
        }

        StringBuilder builder = new StringBuilder();

        int percentage = status * 10 / count;
        switch(percentage){
            case 9:
                if(progressBar == 90){
                    return;
                }
                progressBar = 90;
                break;

            case 7:
                if(progressBar == 70){
                    return;
                }
                progressBar = 70;
                break;

            case 5:
                if(progressBar == 50){
                    return;
                }
                progressBar = 50;
                break;

            case 3:
                if(progressBar == 30){
                    return;
                }
                progressBar = 30;
                break;

            case 1:
                if(progressBar == 10){
                    return;
                }
                progressBar = 10;
                break;

            default:
                return;
        }

        builder.append(progressBar);
        builder.append("% search is done.");

        Toast.makeText(this, builder.toString(), Toast.LENGTH_SHORT).show();
    }

    /*
    * when Back button is pressed, this activity can be stopped
    * */
    @Override
    public void onBackPressed(){
        super.onBackPressed();

        ImageManager.cancelAll();
        ImageManager.cleanHouse();
    }
}
