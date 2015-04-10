/*
 * class SearchContainer:
 * SearchContainer(List<Int>, List<String>)
 * void updateTopN(int)
 * boolean insert(int, String)
 * void getSortedKeysValues(List<Integer>, List<String>);
 * void cleanContainer()
 *
 * */

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.Math;
import org.junit.*;
import org.junit.Assert;

import com.bruyu.imagewalker.SearchContainer;
import com.bruyu.imagewalker.Utility;

public class SearchContainerUnitTest{
    private int currN = 6;
    private SearchContainer game;
    private Utility utility;

    private List<Integer> resultKeys;
    private List<String> resultValues;

    @Before
    public void setUp(){
        game = new SearchContainer(currN);
        utility = new Utility();

        resultKeys = new ArrayList<Integer>();
        resultValues = new ArrayList<String>();
    }

    @Test
    public void SearchContainer_sortTest(){
        game.cleanContainer();

        int[] arrKeys      = new int[]{       2,       6,     0,     5,      4,      1,      3};
        String[] arrValues = new String[]{"cat", "Inter", "dog", "day", "wolf", "burg", "juven"};
        List<Integer> expectedKeys = fillIntegerList(arrKeys);
        List<String> expectedValues = fillStringList(arrValues);

        for(int i = 0; i < arrKeys.length; i++){
            game.insert(arrKeys[i], arrValues[i]);
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
    public void SearchContainer_enlargeN_Test(){
        game.cleanContainer();

        int[] arrKeys      = new int[]{       2,       6,     0,     5,      4,      1,      3};
        String[] arrValues = new String[]{"cat", "Inter", "dog", "day", "wolf", "burg", "juven"};
        List<Integer> expectedKeys = fillIntegerList(arrKeys);
        List<String> expectedValues = fillStringList(arrValues);

        for(int i = 0; i < arrKeys.length; i++){
            game.insert(arrKeys[i], arrValues[i]);
        }

        /* enlarge topN */
        currN = 7;
        game.updateTopN(currN);

        int nkey = 8;
        String nval = "bug";
        expectedKeys.add(nkey);
        expectedValues.add(nval);
        game.insert(nkey, nval);

        nkey = 9;
        nval = "Bayer";
        expectedKeys.add(nkey);
        expectedValues.add(nval);
        game.insert(nkey, nval);

        nkey = -1;
        nval = "X";
        expectedKeys.add(nkey);
        expectedValues.add(nval);
        game.insert(nkey, nval);

        utility.quickSort(expectedKeys, expectedValues);
        expectedKeys = expectedKeys.subList(0, currN);
        expectedValues = expectedValues.subList(0, currN);

        game.getSortedKeysValues(resultKeys, resultValues);

        assertIntegerList(expectedKeys, resultKeys);
        assertStringList(expectedValues, resultValues);
    }

    @Test
    public void SearchContainer_decreaseN_Test(){
        game.cleanContainer();

        int[] arrKeys      = new int[]{       2,       6,     0,     5,      4,      1,      3};
        String[] arrValues = new String[]{"cat", "Inter", "dog", "day", "wolf", "burg", "juven"};
        List<Integer> expectedKeys = fillIntegerList(arrKeys);
        List<String> expectedValues = fillStringList(arrValues);

        for(int i = 0; i < arrKeys.length; i++){
            game.insert(arrKeys[i], arrValues[i]);
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
