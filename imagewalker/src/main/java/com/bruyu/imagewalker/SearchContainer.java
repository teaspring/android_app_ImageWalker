package com.bruyu.imagewalker;

import java.util.List;
import java.util.ArrayList;

/**
 * this class is designed intending to replace MaxHeap to work as container of search
 * which has dynamic insert and sort. but in test, it fails.
 * although the binary search in linear structure is good, the remove()/add() in List<> is
 * too expensive while MaxHeap uses set() instead.
 * anyway, this class is not used yet but can be optimized later.
 */
public class SearchContainer {
    private static final int DEFAULTCAPACITY = 12;
    private ArrayList<Integer> Keys;
    private ArrayList<String> Values;
    private int capacity;
    private static final Object obj = new Object();

    public SearchContainer(){
        this(DEFAULTCAPACITY);
    }

    public SearchContainer(int n){
        capacity = n;
        Keys = new ArrayList<Integer>();
        Values = new ArrayList<String>();
    }

    public void updateTopN(int topN){
        final int n = Keys.size();
        if(topN < n){   // shorten the container
            synchronized(obj){
                Keys = new ArrayList<Integer>(Keys.subList(0, topN));
                Values = new ArrayList<String>(Values.subList(0, topN));
            }
        }

        synchronized(obj){
            capacity = topN;
        }
    }

    /*
     * reset the container
     * */
    public void cleanContainer(){
        synchronized(obj){
            Keys.clear();
            Values.clear();
        }
    }

    /*
     * insert new key and value to container if new key belong to minimum N elements
     * it uses binary search to find the position to insert
     * */
    public boolean insert(int key, String val){
        synchronized(obj){
            if(Keys.isEmpty()){
                Keys.add(key);
                Values.add(val);
                return true;
            }

            final int n = Keys.size();

            if(n == capacity && key >= Keys.get(n-1)){ // container is full, skip the new key which is greater than max
                return false;
            }

            int v = 0, u = n;  // u is inclusive upper, v is inclusive lower
            int m = v;
            while(v < u){
                m = (v + u) / 2;
                if(Keys.get(m) < key && (m == n-1 || key <= Keys.get(m+1))){ // find it
                    break;
                }

                if(Keys.get(m) >= key){
                    u = m;
                }else{
                    v = m + 1;
                }
            }

            if(n == capacity){ // container is full, remove the max(tail of list)
                Keys.remove(n-1);
                Values.remove(n-1);
            }

            if(u == v){ // u reduce to v
                m = v-1;
            }
            Keys.add(m+1, key);
            Values.add(m+1, val);

            return true; // indicate the sorted container has update
        }
    }

    public void  getSortedKeysValues(List<Integer> outKeys, List<String> outValues){
        outKeys.clear();
        outValues.clear();
        outKeys.addAll(Keys);
        outValues.addAll(Values);
    }

    public ArrayList<String> getSortedValues(){
        return new ArrayList<String>(Values);
    }
}
