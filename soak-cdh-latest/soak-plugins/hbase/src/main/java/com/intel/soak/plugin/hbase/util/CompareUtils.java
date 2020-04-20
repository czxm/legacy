package com.intel.soak.plugin.hbase.util;

/**
 * Created with IntelliJ IDEA.
 * User: xhao1
 * Date: 11/22/13
 * Time: 3:05 PM
 * To change this template use File | Settings | File Templates.
 */
import java.util.List;

public class CompareUtils {
    public static  boolean compareSortedList(List<String> sortedArrayList1,
                                             List<String> sortedArrayList2) {
        if(sortedArrayList1.size()!=sortedArrayList2.size()){
            return false;
        }
        for(int i=0; i < sortedArrayList1.size();i++){
            String v1=sortedArrayList1.get(i);
            String v2=sortedArrayList2.get(i);
            if(0!=v1.compareTo(v2)){
                return false;
            }
        }
        return true;
    }

    public static boolean compareResultWithRefAsString(String outputResult, String expectedResult) {
        if(null == outputResult){
            return false;
        }
        if(0!=outputResult.compareTo(expectedResult)){
            return false;
        }else{
            return true;
        }
    }
}
