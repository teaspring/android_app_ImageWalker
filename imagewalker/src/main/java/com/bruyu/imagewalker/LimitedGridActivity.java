package com.bruyu.imagewalker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.AlertDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * display search result in GridView
 */
public class LimitedGridActivity extends BaseGridActivity
        implements GridView.MultiChoiceModeListener{
    private static final String TAG = "::LimitedActivity";

    public static final String BASEIMAGE = "BASEIMAGE";
    public static final String TESTIMGLIST = "TESTIMGLIST";
    public static final String TOPN = "TOPN";

    private String baseImage;
    private ArrayList<String> testImgNameList = new ArrayList<>();
    private int topN = 12;

    private GridView mGrid;

    // flag indicates whether action mode is started
    private ActionMode mActionMode = null;

    private DeleteDialog mDeleteDialog;

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

        // set baseImage, testImgList, and topN
        getArgsFromBundle(getIntent().getExtras());

        baseView = (ImageView)findViewById(R.id.baseImage);

        mAdapter = new DynamicImageFileAdapter(this, 0, new ArrayList<String>());

        Intent intent = new Intent();
        intent.putStringArrayListExtra(DynamicImageFileAdapter.RAWIMAGELIST, testImgNameList);
        mAdapter.setArguments(intent);

        mGrid = (GridView)findViewById(R.id.searchResult);
        mGrid.setAdapter(mAdapter);

        mGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(null != mActionMode){
                    return;
                }
                startImageDetailActivity(position);
            }
        });

        mGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id){
                if(searchProgress.get() < testImgNameList.size()){
                    return false;
                }

                mGrid.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);

                // set listener for multi-choice mode at action mode
                mGrid.setMultiChoiceModeListener(LimitedGridActivity.this);

                // necessary to start action mode by setting item checked immediately
                mGrid.setItemChecked(position, true);

                return true;
           }
        });

        mDeleteDialog = new DeleteDialog(this);
        mDeleteDialog.setProperties();
    }

    /*
    * attempt to get arguments from Bundle
    * */
    private void getArgsFromBundle(Bundle extras){
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
    * GridView to display search result can be updated
    * */
    public void onAdapterDataChanges(List<Integer> keys, List<String> data){
        Log.d(TAG, ":AdapterDataChanges(): " + keys.toString() + ", " + data.toString());
        mAdapter.updateDataList(data);
    }

    /*
    * start activity which display full-screen images in sliding
    * */
    private void startImageDetailActivity(int position){
        Intent intent = new Intent(getApplicationContext(), ImageDetailActivity.class);
        intent.putExtra(ImageDetailActivity.IMG_POSITION, position);
        intent.putStringArrayListExtra(ImageDetailActivity.IMG_FILELIST,
                mAdapter.getDataListNative());

        startActivity(intent);
    }

    /*
    * access to package, internal counter of image compare
    * */
    int increaseProgress(){
        return searchProgress.incrementAndGet();
    }

    /*
    * check searching progress invoked by menu item
    * */
    private void checkProgress(){
        int status = searchProgress.get();
        int imgCount = testImgNameList.size(); // remove base image from the repository

        if(status == imgCount){
            StringBuilder builder = new StringBuilder("search in all ");
            builder.append(imgCount);
            builder.append(" images is done");

            Toast.makeText(this, builder.toString(),Toast.LENGTH_SHORT).show();
        }else if(status < imgCount){
            StringBuilder builder = new StringBuilder("already compared ");
            builder.append(status);
            builder.append(" images");

            Toast.makeText(this, builder.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /*
    * show a toast to indicate the searching progress percentage
    * */
    void proposeProgress(){
        int status = searchProgress.get();
        int count = testImgNameList.size();

        if(status == count){
            progressBar = 100;  // 100%
            Toast.makeText(this, "Searching " + count + " images done.",
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "searching " + count + " images done");

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
        builder.append("% searching done");

        Toast.makeText(this, builder.toString(), Toast.LENGTH_SHORT).show();
    }

    /*
    * when Back button is pressed, this activity can be stopped
    * interrupt all working threads, and cancel all waiting searching tasks
    * */
    @Override
    public void onBackPressed(){
        super.onBackPressed();

        ImageManager.cancelAll();
        ImageManager.cleanHouse();
    }

    /* ActionMode.setCustomView() to customize view of action mode */
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu){
        mActionMode = mode;

        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.multichoice, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu){
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item){
        switch (item.getItemId()){
            case R.id.delete_multi:
                mDeleteDialog.show();
                break;
            case R.id.send_multi:
                operateSendItems();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode){
        mode.finish();

        mActionMode = null;
        mGrid.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position,
                                          long id, boolean checked){
        // checked position is stored in GridView.getCheckedPositions()
    }

    /*
    * send multi selected items in MultiChoice action mode
    * */
    void operateSendItems(){
    }

    /*
    * delete multi selected items in MultiChoice action mode
    * */
    void operateDeleteItems(){
        ArrayList<String> multiSelectedItems =
                parseCheckedPositions(mGrid.getCheckedItemPositions());

        boolean containsBase = multiSelectedItems.contains(baseImage);

        ArrayList<String> doneImgs = new ArrayList<>(mAdapter.getDataListNative());

        for(String fullPath : multiSelectedItems){
            File file = new File(fullPath);

            if(file.delete()){
                Log.d(TAG, "delete successfully: " + fullPath);
                doneImgs.remove(fullPath);
            }
        }
        mAdapter.updateDataList(doneImgs);

        Toast.makeText(this, "delete is done", Toast.LENGTH_SHORT).show();
        mActionMode.finish();

        if(containsBase){ // if contains base image, return to parent activity
            this.finish();
        }
    }

    /*
    * parse multi-selected positions from GridView.getCheckedPositions()
    * SparseBooleanArray: key is what you need(position), value is true or false
    * */
    private ArrayList<String> parseCheckedPositions(SparseBooleanArray array){
        ArrayList<String> multiSelected = new ArrayList<>();
        final int n = array.size();
        for(int index = 0; index < n; index++){
            if(array.valueAt(index)){
                multiSelected.add(mAdapter.getItem(array.keyAt(index)));
            }
        }
        return multiSelected;
    }

    class DeleteDialog extends AlertDialog{
        public DeleteDialog(Context context){
            super(context);
        }

        public void setProperties(){
            setMessage("Really delete selected images ?");

            setButton(BUTTON_NEGATIVE, "No", new OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                    cancel();
                }
            });

            setButton(BUTTON_POSITIVE, "Yes", new OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                    operateDeleteItems();
                    cancel();
                }
            });
        }

    };
}
