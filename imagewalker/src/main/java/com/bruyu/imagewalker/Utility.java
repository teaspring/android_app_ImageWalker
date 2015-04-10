package com.bruyu.imagewalker;

import java.util.List;
import java.lang.Float;

/**
 * Created by bruyu on 3/17/15.
 */
public class Utility {
    public static void swapIntInList(List<Integer> list, int i, int j){
        int tmp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, tmp);
    }

    public static void swapStrInList(List<String> list, int i, int j){
        String tmp = new String(list.get(i));
        list.set(i, list.get(j));
        list.set(j, tmp);
    }

    public void quickSort(List<Integer> keys, List<String> values){
        if(keys.size() != values.size())    return;
        quickSort(keys, values, 0, keys.size());
    }

    /*
     * [start, end], exclusive end
     * */
    private void quickSort(List<Integer> keys, List<String> values, int start, int end){
        if(start >= end - 1 || start < 0 || end > keys.size())    return;

        int p = start, q = start - 1, t = end - 1;  // t is inclusive
        while(p < t){
            if(keys.get(p) < keys.get(t)){ // put smaller element ahead
                q++;
                swapIntInList(keys, q, p);
                swapStrInList(values, q, p);
            }
            p++;
        }
        q++;
        swapIntInList(keys, q, t);
        swapStrInList(values, q, t);

        quickSort(keys, values, start, q);
        quickSort(keys, values, q+1, end);
    }
}
