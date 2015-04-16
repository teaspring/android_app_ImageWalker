package com.bruyu.imagewalker;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created on 3/11/15.
 * 1. click image will start another activity of ViewPager as screen slide
 * 2. open image on sdcard
 * 3. the file name list should be shared between GridActivity and ImageDetailActivity
 */
public class ImageDetailActivity extends FragmentActivity {
    public static final String IMG_POSITION = "POSITION";
    public static final String IMG_FILELIST = "IMGFILELIST";

    private int itemPosition;

    private ArrayList<String> imgNameList;

    private ViewPager mPager;

    private PagerAdapter mPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        itemPosition = (extras != null && extras.containsKey(IMG_POSITION)?
                extras.getInt(IMG_POSITION) : 0);

        imgNameList = new ArrayList<>();
        if(extras != null && extras.containsKey(IMG_FILELIST)){
            imgNameList.addAll(extras.getStringArrayList(IMG_FILELIST));
        }

        setContentView(R.layout.detail_image_pager);

        mPager = (ViewPager)findViewById(R.id.pager);
        mPagerAdapter = new ImageDetailPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(itemPosition);
    }

    @Override
    public void onResume(){
        super.onResume();

        // update orientation status
        boolean currLandscape = (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE);
        if(Helper.isLandscape() != currLandscape){
            Helper.toggleLandscape(currLandscape);
        }

        // not elegant strategy to ask data adapter to reopen image
        mPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_settings:
                Toast.makeText(ImageDetailActivity.this, "position=" + itemPosition,
                        Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    * data adapter for a ViewPager layout which can scroll items like slides
    * */
    class ImageDetailPagerAdapter extends FragmentStatePagerAdapter{
        public ImageDetailPagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position){
            Fragment fragment = new ImageDetailFragment();

            Bundle bundle = new Bundle();
            bundle.putString(ImageDetailFragment.IMG_FILENAME,
                        imgNameList.get(position));
            fragment.setArguments(bundle);

            return fragment;
        }

        @Override
        public int getCount(){
            return imgNameList.size();
        }
    }
}
