package com.bruyu.imagewalker;

import org.opencv.core.Mat;
import android.os.Process;
import java.util.List;

/*
 * calculate the compare value between base image and a test image
 * */
class ImageCompareRunnable implements Runnable{

    static final int COMPARE_STATE_FAILED = -1;
    static final int COMPARE_STATE_STARTED = 0;
    static final int COMPARE_STATE_COMPLETED = 1;

    static final int compMethod = ImageTask.getCompareMethod();
    static final float alpha    = ImageTask.getWeightAlpha();

    // package access
    TaskRunnableCompareMethods mImageTask;

    interface TaskRunnableCompareMethods{
        /*
         * Sets the actions for each state of the ImageTask instance
         * */
        void handleCompareState(int state);

        /*
         * Sets the calculated image compare value when its operations complete
         * */
        void setCompareValue(int val);

        /*
        * Gets test image name
        * */
        String getTestImageName();

        /*
        * Sets the thread for this compare image task
        * */
        void setCompareThread(Thread thread);

        /*
        * Get current thread allocating for work
        * */
        Thread getCurrentThread();
    }

    public ImageCompareRunnable(ImageTask task){
        mImageTask = task;
    }

    /*
     * Defines this object's task, which is a set of instructions designed to be run on a thread.
     * */
    @Override
    public void run(){

        /*
         * stores the current Thread in the ImageTask instance, so that the instance can
         * interrupt the Thread
         * */
        mImageTask.setCompareThread(Thread.currentThread());

        // move the current thread into background
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        // get test image name from ImageTask
        String testImage = mImageTask.getTestImageName();

        try{
            mImageTask.handleCompareState(COMPARE_STATE_STARTED);

            if(mImageTask.getCurrentThread().isInterrupted() || testImage == null){
                return;
            }

            /*
            * Calculate segment masked histograms of test image
            * */
            List<Mat> testHists = ImageTask.getMaskedHists(testImage);

            if(mImageTask.getCurrentThread().isInterrupted()){
                return;
            }

            /*
            * Get segment masked histogram of base image
            * */
            List<Mat> baseHists = ImageManager.getBaseHists();

            if(null == baseHists || baseHists.isEmpty()){
                baseHists = ImageManager.setBaseHists();
            }

            if(mImageTask.getCurrentThread().isInterrupted() || baseHists == null){
                return;
            }

            /*
            * calculate the image compare value
            * */
            int res = ImageTask.compHistWeighted(baseHists, testHists, compMethod, alpha);

            // save the image compare value
            mImageTask.setCompareValue(res);

            // reports a status of "complete"
            mImageTask.handleCompareState(COMPARE_STATE_COMPLETED);

        }catch(Throwable e){

        }finally{
        // sets the current thread to null, releasing its storage.
            mImageTask.setCompareThread(null);
        }
    }
}
