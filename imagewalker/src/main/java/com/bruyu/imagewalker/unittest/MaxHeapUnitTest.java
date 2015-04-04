/*
 * class Utility:
 * quickSort(List<Integer> keys, List<String> values)
 * swapIntInList()
 * swapStrInList()
 *
 * class MaxHeap:
 * MaxHeap(List<Int>, List<String>)
 * void updateTopN(int)
 * boolean heap_insert(int, String)
 * void getSortedKeysValues(List<Integer>, List<String>);
 *
 * */

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.Math;
import org.junit.*;
import org.junit.Assert;

import com.bruyu.imagewalker.MaxHeap;
import com.bruyu.imagewalker.Utility;

public class MaxHeapUnitTest{
    private int currN = 6;
    private MaxHeap game;
    private Utility utility;
    private List<Integer> resultKeys;
    private List<String> resultValues;

    @Before
    public void setUp(){
        game = new MaxHeap(currN);
        utility = new Utility();
        resultKeys = new ArrayList<Integer>();
        resultValues = new ArrayList<String>();
    }

    @Test
    public void Utitlity_positiveTest(){
        int[] arrKeys      = new int[]{       2,       6,     0,     5,      4,      1,      3};
        String[] arrValues = new String[]{"cat", "Inter", "dog", "day", "wolf", "burg", "juven"};
        List<Integer> keys = fillIntegerList(arrKeys);
        List<String> values = fillStringList(arrValues);

        int[] arrExpectedKeys      = new int[]{       0,     1,      2,       3,      4,    5,       6};
        String[] arrExpectedValues = new String[]{"dog", "burg", "cat", "juven", "wolf", "day", "Inter"};
        List<Integer> expectedKeys = fillIntegerList(arrExpectedKeys);
        List<String> expectedValues = fillStringList(arrExpectedValues);

        utility.quickSort(keys, values);

        assertIntegerList(expectedKeys, keys);
        assertStringList(expectedValues, values);
    }

    @Test
    public void MaxHeap_sortTest(){
        game.cleanHeap();

        int[] arrKeys      = new int[]{       2,       6,     0,     5,      4,      1,      3};
        String[] arrValues = new String[]{"cat", "Inter", "dog", "day", "wolf", "burg", "juven"};
        List<Integer> expectedKeys = fillIntegerList(arrKeys);
        List<String> expectedValues = fillStringList(arrValues);

        for(int i = 0; i < arrKeys.length; i++){
            game.heap_insert(arrKeys[i], arrValues[i]);
        }

        // make use of Utility.quickSort() to facilitate our assert
        utility.quickSort(expectedKeys, expectedValues);
        expectedKeys = expectedKeys.subList(0, currN);
        expectedValues = expectedValues.subList(0, currN);

        game.getSortedKeysValues(resultKeys, resultValues);

        assertIntegerList(expectedKeys, resultKeys);
        assertStringList(expectedValues, resultValues);
    }

    @Test
    public void MaxHeap_enlargeN_Test(){
        game.cleanHeap();

        int[] arrKeys      = new int[]{       2,       6,     0,     5,      4,      1,      3};
        String[] arrValues = new String[]{"cat", "Inter", "dog", "day", "wolf", "burg", "juven"};
        List<Integer> expectedKeys = fillIntegerList(arrKeys);
        List<String> expectedValues = fillStringList(arrValues);

        for(int i = 0; i < arrKeys.length; i++){
            game.heap_insert(arrKeys[i], arrValues[i]);
        }

        /* enlarge topN */
        currN = 7;
        game.updateTopN(currN);

        int nkey = 8;
        String nval = "bug";
        expectedKeys.add(nkey);
        expectedValues.add(nval);
        game.heap_insert(nkey, nval);

        nkey = 9;
        nval = "Bayer";
        expectedKeys.add(nkey);
        expectedValues.add(nval);
        game.heap_insert(nkey, nval);

        nkey = -1;
        nval = "X";
        expectedKeys.add(nkey);
        expectedValues.add(nval);
        game.heap_insert(nkey, nval);

        utility.quickSort(expectedKeys, expectedValues);
        expectedKeys = expectedKeys.subList(0, currN);
        expectedValues = expectedValues.subList(0, currN);

        game.getSortedKeysValues(resultKeys, resultValues);

        assertIntegerList(expectedKeys, resultKeys);
        assertStringList(expectedValues, resultValues);
    }

    @Test
    public void MaxHeap_decreaseN_Test(){
        game.cleanHeap();

        int[] arrKeys      = new int[]{       2,       6,     0,     5,      4,      1,      3};
        String[] arrValues = new String[]{"cat", "Inter", "dog", "day", "wolf", "burg", "juven"};
        List<Integer> expectedKeys = fillIntegerList(arrKeys);
        List<String> expectedValues = fillStringList(arrValues);

        for(int i = 0; i < arrKeys.length; i++){
            game.heap_insert(arrKeys[i], arrValues[i]);
        }

        /* decrease topN */
        currN = 3;
        utility.quickSort(expectedKeys, expectedValues);
        expectedKeys = expectedKeys.subList(0, currN);
        expectedValues = expectedValues.subList(0, currN);

        game.updateTopN(currN);
        game.getSortedKeysValues(resultKeys, resultValues);

        assertIntegerList(expectedKeys, resultKeys);
        assertStringList(expectedValues, resultValues);
    }

    private List<Integer> fillIntegerList(int[] arr){
        List<Integer> keys = new ArrayList<Integer>();
        for(int i = 0; i < arr.length; i++){
            keys.add(arr[i]);
        }
        return keys;
    }

    private List<String> fillStringList(String[] arr){
        List<String> values = new ArrayList<String>();
        for(int i = 0; i < arr.length; i++){
            values.add(arr[i]);
        }
        return values;
    }

    private void assertStringList(List<String> expected, List<String> result){
        Assert.assertEquals(expected.size(), result.size());
        for(int i = 0; i < result.size(); i++){
            Assert.assertEquals(expected.get(i), result.get(i));
        }
    }

    private void assertIntegerList(List<Integer> expected, List<Integer> result){
        Assert.assertEquals(expected.size(), result.size());
        for(int i = 0; i < result.size(); i++){
            Assert.assertEquals(expected.get(i), result.get(i));
        }
    }

    @After
    public void settleDown(){
        game = null;
        utility = null;
    }
}
