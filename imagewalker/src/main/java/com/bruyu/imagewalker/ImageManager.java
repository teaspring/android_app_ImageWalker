package com.bruyu.imagewalker;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.opencv.core.Mat;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.lang.Runtime;
import java.util.LinkedList;

public class ImageManager{
    //private static final String TAG = "::ImageManager";
    static final int TASK_FAILED = -1;
    static final int TASK_STARTED = 1;
    static final int TASK_COMPLETED = 2;

    // Sets the amount of time an idle thread will wait for a task before terminating
    private static final int KEEP_ALIVE_TIME = 1;

    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;

    // Sets the initial ThreadPool size to 4
    private static final int CORE_POOL_SIZE = 4;

    // Sets the maximum ThreadPool size to 4
    private static final int MAXIMUM_POOL_SIZE = 4;

    /*
     * Note: this is the number of total available cores. On current versions of
     * Android, with devices that use plug-and-play cores, this will return less
     * than total number of cores.
     *
     * on MI1S, NUMBER_OF_CORES == 2
     * */
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    // Use a customized MaxHeap to sort the images dependent on their compare value with base image
    private static MaxHeap mMaxHeap;

    // A queue of working Runnable objects, handed to Thread Pool
    private final BlockingQueue<Runnable> mCompareWorkQueue;

    // A queue of working ImageTask objects
    private final LinkedList<ImageTask> mImageTaskWorkingList;

    // A queue of recycled free ImageTask objects
    private final Queue<ImageTask> mImageTaskReadyQueue;

    // A managed pool of background compare threads
    private final ThreadPoolExecutor mCompareThreadPool;

    // A object that manages messages in a Thread
    private Handler mHandler;

    private static List<Mat> mBaseHists;

    private static String mBaseImage;

    // A single instance of ImageManager, used to implement singleton pattern
    private static ImageManager sInstance = null;

    // A static block that sets class fields
    static{
        // The time unit for "keep alive" is in seconds
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

        // Create a single static instance of ImageManager
        if(null == sInstance) {
            sInstance = new ImageManager();
        }
    }

    /*
     * Constructs the work queues and thread pools used to compare images
     * */
    private ImageManager(){

        /*
         * Creates a customized MaxHeap to store and sort the test images
         * */
        mMaxHeap = new MaxHeap();

        /*
        * segment histograms of base image
        * */
        mBaseHists = new ArrayList<Mat>();

        /*
         * A queue of working Runnable for comparing images, each of which works on
         * one thread of ThreadPool
         * using a linked list queue that blocks when the queue is empty
         * */
        mCompareWorkQueue = new LinkedBlockingQueue<Runnable>();

        /*
         * A queue of recycled ImageTask objects
         * using a linked list queue that blocks when the queue is empty
         * */
        mImageTaskReadyQueue = new LinkedBlockingQueue<ImageTask>();

        /*
        * A linked list of working ImageTask objects
        * */
        mImageTaskWorkingList = new LinkedList<ImageTask>();

        /*
         * Create a new pool of Thread objects for the compare work queue
         * */
         //final int AGREED_POOL_SIZE = Math.min(CORE_POOL_SIZE, NUMBER_OF_CORES);

         mCompareThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mCompareWorkQueue);

        /*
        * Instantiate a new anonymous Handler object and defines its HandleMessage() method.
        * The Handler *must* run on the UI thread, because it send the sorted test image list to
        * data adapter of Activity
        *
        * To force the Handler to run on the UI thread, it is defined as part of ImageManager
        * constructor. The constructor is invoked when the class is first referenced, and that
        * happens when the View invokes startCompare().
        *
        * Since the View runs on the UI thread, so does the ImageManager constructor and the handler
        * */
        mHandler = new Handler(Looper.getMainLooper()){
            /*
             * Handler receives a new Message to process
             * */
            @Override
            public void handleMessage(Message inputMessage){

                // gets the ImageTask from the incoming Message object
                ImageTask imageTask = (ImageTask)inputMessage.obj;

                switch (inputMessage.what){
                    case TASK_STARTED:
                        break;

                    case TASK_COMPLETED:
                        // sets the LimitedGridActivity contained in ImageTask as a weak reference
                        LimitedGridActivity activity = imageTask.getLimitedGridActivity();

                        if(null != activity){
                            int key = imageTask.getCompareValue();
                            String img = imageTask.getTestImageName();

                            // ImageTask can be recycled as soon as image compare done
                            recycleTask(imageTask);

                            // internal process record
                            activity.increaseProgress();

                            if(mMaxHeap.heap_insert(key, img)){
                                List<Integer> keys = new ArrayList<>();
                                List<String> values = new ArrayList<>();
                                mMaxHeap.getSortedKeysValues(keys, values);

                                activity.onAdapterDataChanges(keys, values);
                            }

                            // show a toast if the search is done!
                            activity.proposeProgress();
                        }
                        break;

                    case TASK_FAILED:
                        recycleTask(imageTask);
                        break;
                    default:
                        super.handleMessage(inputMessage);
                }
            }
        };
    }

    /*
     * Returns the ImageManager object
     * @return the global ImageManager object
     * */
    public static ImageManager getInstance(){
        return sInstance;
    }

    /*
     * Handle state messages for a particular task object
     * @param state: the state of the task
     * */
    public void handleState(ImageTask imageTask, int state){
        switch(state){
            case TASK_COMPLETED:
                // it seems no difference between all branches
                mHandler.obtainMessage(state, imageTask).sendToTarget();
                break;
            default:
                mHandler.obtainMessage(state, imageTask).sendToTarget();
                break;
        }
    }

    /*
     * Cancel all Threads in the CompareThreadPool
     * */
    public static void cancelAll(){
        ImageTask[] taskArray = new ImageTask[sInstance.mImageTaskWorkingList.size()];

        sInstance.mImageTaskWorkingList.toArray(taskArray);

        /*
         * Locks on the singleton to ensure that other processes aren't mutating Threads.
         * then iterate over tasks array and interrupt the task's current Thread
         * */
        synchronized(sInstance){
            for(ImageTask task : taskArray){
                Thread thread = task.mThreadThis;

                if(null != thread){
                    thread.interrupt();
                }
            }
        }
    }

    /*
     * Starts an image compare task on one thread allocated from Thread Pool
     * */
    public static ImageTask startCompare(LimitedGridActivity activity,
            String baseImage, String testImage){

        if(null == mBaseImage && null != baseImage){
            mBaseImage = baseImage;
            mBaseHists = ImageTask.getMaskedHists(mBaseImage);
        }

        ImageTask compareTask = sInstance.mImageTaskReadyQueue.poll();

        // if the queue is empty, create a new task instead
        if(null == compareTask){
            compareTask = new ImageTask(baseImage);
        }

        compareTask.initializeCompareTask(ImageManager.sInstance, activity, testImage);

        sInstance.mImageTaskWorkingList.add(compareTask);

        sInstance.mCompareThreadPool.execute(compareTask.getCompareRunnable());

        return compareTask;
    }

    /*
    * as ImageManager works as singleton pattern, this clean operation is necessary
    * when external invoker wants to drive another total new search
    * */
    static void cleanHouse(){
        if(null != mMaxHeap) {
            mMaxHeap.cleanHeap();
        }

        if(null != mBaseHists) {
            for (Mat segHist : mBaseHists) {
                segHist.release();
            }
            mBaseHists.clear();
            mBaseHists = null;
        }

        if(null != mBaseImage) {
            mBaseImage = null;
        }
    }

    /*
     * Recycles tasks by calling their internal recycle() method and then put them back into the task queue
     * */
    void recycleTask(ImageTask compareTask){
        // Free up memory in the task
        compareTask.recycle();

        mImageTaskWorkingList.removeFirstOccurrence(compareTask);

        // Put the task object back into the queue for re-use
        mImageTaskReadyQueue.offer(compareTask);
    }

    /*
    * return masked histograms of base image
    * */
    static List<Mat> getBaseHists(){
        return mBaseHists;
    }

    /*
    * Update heap size in MaxHeap
    * */
    static void updateTopN(int displayN){
        mMaxHeap.updateTopN(displayN);
    }
}
