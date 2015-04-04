package com.bruyu.imagewalker;

import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * Created by bruyu on 3/17/15.
 */
public class MaxHeap {
    private static final int DEFAULTSIZE = 12;
    private List<Integer> Keys;
    private List<String> Values;
    private int N;
    private boolean heapified;
    private static Object obj = new Object();

    public MaxHeap(){
        this(DEFAULTSIZE);
    }

    public MaxHeap(int n){
        Keys = new ArrayList<Integer>();
        Values = new ArrayList<String>();
        N = n;
        heapified = false;
    }

    /*
     * update internal heap capacity
     * */
    public void updateTopN(int topN){
        if(topN != N) {
            N = topN;
            heap_sort();  // sort to remove excessive elements
        }
    }

    /*
     * necessary when external manager class needs to start another new sort
     * */
    public void cleanHeap(){
        synchronized(obj) {
            Keys.clear();
            Values.clear();
        }
    }

    /*
     * build whole heap heapified
     * */
    public void heap_build(){
        int hsize = Keys.size();
        for(int i = hsize >> 1; i >= 0; i--){
            max_heapify(i, hsize);
        }
        synchronized(obj) {
            heapified = true;
        }
        return;
    }

    /*
     * attempt to insert a new key/value to a heapified heap
     * return:
     * false if inner elements order does not change
      * true if inner elements order changes
      *
      * note: use CriticalSection for asynchronous threading use in SearchEngine
     * */
    public boolean heap_insert(int nkey, String nval){
        if(Keys.size() < N){
            synchronized(obj) {
                Keys.add(nkey);
                Values.add(nval);
            }

        }else{
            if(!heapified){
                heap_build();
            }

            if(nkey >= Keys.get(0)){
                return false;

            }else {
                synchronized(obj) {
                    Keys.set(0, nkey);
                    Values.set(0, nval);
                    max_heapify(0, Keys.size());
                }
            }
        }

        return true;
    }

    /*
     * return sorted keys and values
     * */
    public void getSortedKeysValues(List<Integer> outKeys, List<String> outValues){
        heap_sort();

        outKeys.clear();
        outValues.clear();
        outKeys.addAll(Keys);
        outValues.addAll(Values);
    }

    /*
     * sort the heap(list) in ascending order in place
     * after that, [0] becomes minimum of whole tree instead of maximum
     * note: it will be invoked while N has been updated externally
     * */
    private void heap_sort(){
        synchronized(obj) {
            if (!heapified)    heap_build();
            int hsize = Keys.size();
            while (hsize > 1) {
                Utility.swapIntInList(Keys, hsize - 1, 0);
                Utility.swapStrInList(Values, hsize - 1, 0);

                if (Keys.size() > N) {
                    Keys.remove(Keys.size() - 1);
                    Values.remove(Values.size() - 1);
                }

                hsize--;
                max_heapify(0, hsize);
            }
            heapified = false;  // don't forget it!
        }
        return;
    }

    /*
     * enable [i] is maximum of substree with root of [i]
     * */
    private void max_heapify(int i, int length){
        if(length <= 1)    return;

        int l = Left(i), r = Right(i), largest = i;
        int hsize = Keys.size();
        if(l < Math.min(hsize, length) && Keys.get(l) > Keys.get(i)){
            largest = l;
        }

        if(r < Math.min(hsize, length) && Keys.get(r) > Keys.get(largest)){
            largest = r;
        }

        if(largest != i){
            synchronized(obj) {
                Utility.swapIntInList(Keys, largest, i);
                Utility.swapStrInList(Values, largest, i);
            }
            max_heapify(largest, length);
        }
    }

    private int Left(int i){
        return 2 * (i + 1) - 1;
    }

    private int Right(int i){
        return 2 * (i + 1);
    }

}
