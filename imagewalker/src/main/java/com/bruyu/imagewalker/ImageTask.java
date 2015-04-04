package com.bruyu.imagewalker;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfFloat;
import org.opencv.core.Size;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import com.bruyu.imagewalker.ImageCompareRunnable.TaskRunnableCompareMethods;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.ArrayList;

public class ImageTask implements TaskRunnableCompareMethods{

    private static final int COMPAREMETHOD = Imgproc.CV_COMP_CHISQR;
    private static final float WEIGHTALPHA = 0.28f;
    private static final int ERRORCOMPAREVALUE = 100000;

    public static final int H_BINS = 30;
    public static final int S_BINS = 36;
    public static final int V_BINS = 3;

    // the test image name
    private String mTestImageName;

    // Field containing the Thread this task is running on
    Thread mThreadThis;

    // Field containing reference to the runnable object that handle compare
    private Runnable mCompareRunnable;

    // the thread on which this task is currently running
    private Thread mCurrentThread;

    private int mCompareValue;

    // an object that contains the ThreadPool singleton
    private static ImageManager sImageManager;

    // a WeakReference of LimitedGridActivity on UI thread
    private WeakReference<LimitedGridActivity> mActivityWeakRef;

    // parameter for openCV compare method
    private static final MatOfInt mHistSize = new MatOfInt(new int[]{ H_BINS, S_BINS, V_BINS });

    // parameter for openCV compare method
    private static final MatOfInt mChannels = new MatOfInt(new int[]{ 0, 1, 2 });

    // parameter for openCV compare method
    private static final MatOfFloat mRanges = new MatOfFloat(new float[]{ 0,180, 0,256, 0,256 });

    /*
     * constructor, only accessible to this package
     * */
    ImageTask(String baseImage){
        mCompareRunnable = new ImageCompareRunnable(this);
        sImageManager = ImageManager.getInstance();
        mCompareValue = ERRORCOMPAREVALUE;
    }

    /*
     * Initialize the task
     * only accessible to this package
     * */
    void initializeCompareTask(ImageManager imageManager, LimitedGridActivity activity,
                               String testImage){
        sImageManager = imageManager;
        mActivityWeakRef = new WeakReference<>(activity);
        mTestImageName = testImage;
    }

    /*
     * Recycles an ImageTask object before it's put back into the pool.
     * */
    void recycle(){
        // delete the weak reference to the LimitGridActivity
        if(null != mActivityWeakRef){
            mActivityWeakRef.clear();
            mActivityWeakRef = null;
        }

        mTestImageName = null;
        setCompareValue(ERRORCOMPAREVALUE);
    }

    /*
     * Get segment masked histogram of image
     * */
    static List<Mat> getMaskedHists(String img){
        Mat matSrc = Highgui.imread(img, Highgui.CV_LOAD_IMAGE_COLOR);
        Mat hsvSrc = new Mat();

        Imgproc.cvtColor(matSrc, hsvSrc, Imgproc.COLOR_BGR2HSV); // BGR -> HSV, here V = R

        // release intermediate resource as soon as its use completes
        matSrc.release();

        List<Mat> masks = generateSegMasks(hsvSrc.height(), hsvSrc.width());

        return calcMaskedHists(hsvSrc, mChannels, masks, mHistSize, mRanges);
    }

    /*
     * Generate segment masks of given Size of image Mat
     * */
    private static List<Mat> generateSegMasks(int height, int width){
        List<Mat> masks = new ArrayList<>();
        int h = height, w = width;
        int cx = w / 2, cy = h / 2;
        Size axes = new Size(w / 8 * 3, h / 8 * 3); // half of size of ellipse's main axes

        final int type = CvType.CV_8UC1;
        Mat ellipseMask = Mat.zeros(h, w, type);
        Core.ellipse(ellipseMask,
                     new Point(new double[]{cx, cy}),
                     axes,
                     0, 0, 360, new Scalar(255), -1);
        masks.add(ellipseMask);

        //draw 4 rectangle corner masks which subtracts ellipse mask
        int[] XYs = new int[]{ 0,  0, cx, cy,
                              cx,  0,  w, cy,
                               0, cy, cx,  h,
                              cx, cy,  w,  h};
        for(int i = 0; i < 4; i++){
            Mat cornerMask = Mat.zeros(h, w, type);
            Core.rectangle(cornerMask,
                           new Point(new double[]{XYs[i*4], XYs[i*4 + 1]}),
                           new Point(new double[]{XYs[i*4 + 2], XYs[i*4 + 3]}),
                           new Scalar(255), -1);
            Core.subtract(cornerMask, ellipseMask, cornerMask);
            masks.add(cornerMask);
        }
        return masks;
    }

    /*
     * Calculate masked histogram of one image Mat
     * */
    private static List<Mat> calcMaskedHists(Mat hsvImg, MatOfInt channels,
                     List<Mat> masks, MatOfInt histSize, MatOfFloat ranges){
        List<Mat> images = new ArrayList<>();
        images.add(hsvImg);

        List<Mat> hists = new ArrayList<>();
        for(Mat mask : masks){
            Mat hist = new Mat();

            // accumulated flag is false
            Imgproc.calcHist(images, channels, mask, hist, histSize, ranges, false);
            Core.normalize(hist, hist);

            hists.add(hist);
        }

        // mask Mats become useless after masked histograms are calculated
        for(Mat mask : masks){
            mask.release();
        }
        masks.clear();

        // HSV Mat become useless after masked histograms are calculated
        hsvImg.release();
        images.clear();

        return hists;
    }

    /*
     * Compare the base image histogram and the test image histogram
	 * */
    static int compHistWeighted(List<Mat> histsBase, List<Mat> histsTest, int methodIdx,
                                float alpha){
        if(histsBase.size() != histsTest.size()){
            return ERRORCOMPAREVALUE;
        }

        int n = histsBase.size();
        float beta = (1 - alpha) / (n-1);
        float res = (float)Imgproc.compareHist(histsBase.get(0),
                                               histsTest.get(0),
                                               methodIdx) * alpha;

        for(int i = 1; i < n; i++){
            res += (float)Imgproc.compareHist(histsBase.get(i),
                                              histsTest.get(i),
                                              methodIdx) * beta;
        }

        // histograms of test image become useless after the compare is done
        for(Mat segHist : histsTest){
            segHist.release();
        }
        histsTest.clear();

        return (int)res;
    }

    /*
     * Implements TaskRunnableCompareMethods.handleCompareState()
     * */
    @Override
    public void handleCompareState(int state){
        int outState;

        // convert the compare state to the overall task state
        switch(state){
            case ImageCompareRunnable.COMPARE_STATE_COMPLETED:
                outState = ImageManager.TASK_COMPLETED;
                break;
            case ImageCompareRunnable.COMPARE_STATE_FAILED:
                outState = ImageManager.TASK_COMPLETED;
                break;
            default:
                outState = ImageManager.TASK_STARTED;
                break;
        }

        // pass the state to the ThreadPool object
        handleState(outState);
    }

    /*
     * save the calculated image compare result value
     * */
    @Override
    public void setCompareValue(int val){
        mCompareValue = val;
    }

    /*
     * return the compare result value
     * */
    int getCompareValue(){
        return mCompareValue;
    }

    /*
     * implements TaskRunnableCompareMethods.getTestImageName()
     * */
    @Override
    public String getTestImageName(){
        return mTestImageName;
    }

    /*
    * implements TaskRunnableCompareMethods.setCompareThread()
    * */
    @Override
    public void setCompareThread(Thread thread){
        setCurrentThread(thread);
    }

    /*
     * Returns the masked histogram of base image
     * only accessible to this package
     * */
    static List<Mat> getBaseHists(){
        return ImageManager.getBaseHists();
    }

    // return the instance which compare the image
    Runnable getCompareRunnable(){
        return mCompareRunnable;
    }

    /*
     * return the LimitedGridActivity that is referenced
     * */
    public LimitedGridActivity getLimitedGridActivity(){
        if(null != mActivityWeakRef){
            return mActivityWeakRef.get();
        }
        return null;
    }

    /*
     * Returns the Thread that this Task is running on. The method must first get a lock on a
     * static field, In this case the ThreadPool singleton. The lock is needed because the Thread
     * object reference is stored in the Thread object itself, and that object can be changed
     * by processes outside of this app.
     * */
    public Thread getCurrentThread(){
        synchronized(sImageManager){
            return mCurrentThread;
        }
    }

    /*
     * Sets the current thread. this must be a synchronized operation.
     * */
    public void setCurrentThread(Thread thread){
        synchronized(sImageManager){
            mCurrentThread = thread;
        }
    }

    /*
    * Delegates handling the current state of the task to the ImageManager object
    * */
    void handleState(int state){
        sImageManager.handleState(this, state);
    }

    static int getCompareMethod(){
        return COMPAREMETHOD;
    }

    static float getWeightAlpha(){
        return WEIGHTALPHA;
    }
}
