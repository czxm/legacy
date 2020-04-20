package com.intel.soak.plugin.hbase.transaction;

import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.plugin.hbase.util.CompareUtils;
import com.intel.soak.plugin.hbase.util.StringUtils;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.util.Bytes;


import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * Created with IntelliJ IDEA.
 * User: xhao1
 * Date: 11/27/13
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
 */
@Plugin(desc = "HBaseScanTransaction", type = PLUGIN_TYPE.TRANSACTION)
public class HBaseScanTransaction extends HBaseDummyTransaction{
    protected static final String DEFAULT_CACHING = "1000";
    protected Scan scan;
    Vector<HashMap<String,String>> resultVector = new Vector<HashMap<String,String>>();

    @Override
    public boolean execute() {
        //logger.info(String.format("Enter HBaseScanTransaction execute()" ));
        String startRowStr = getParamValue("StartRow");
        String stopRowStr = getParamValue("StopRow");
        String columnInfoStr = getParamValue("ColumnInfo");
        String cachingStr = getParamValue("Caching");
        cachingStr = StringUtils.setAsDefaultValueIfEmpty(cachingStr,DEFAULT_CACHING);
        scan = new Scan();
        if(!StringUtils.isNullOrEmpty(startRowStr)){
            scan.setStartRow(Bytes.toBytes(startRowStr));
        }
        if(!StringUtils.isNullOrEmpty(stopRowStr)){
            scan.setStopRow(Bytes.toBytes(stopRowStr));
        }
        if(!StringUtils.isNullOrEmpty(columnInfoStr)){
            if(!configureColumnInfoForScan(columnInfoStr)){
                logger.error("Error occurs during configureScanByColumnInfo()!");
                logger.error(String.format("## Param 'ColumnInfo' is '%s'", columnInfoStr));
                return false;
            }
        }
        if(!StringUtils.isNullOrEmpty(cachingStr)){
            scan.setCaching(Integer.parseInt(cachingStr));
        }
        if(!configureFilterForScan()){
            logger.error("Error occurs during configureFilterForScan()!");
            return false;
        }

        String isCheckResult = getParamValue("IsCheckResult");
        isCheckResult = StringUtils.setAsDefaultValueIfEmpty(isCheckResult,"false");
        String expectedResultStr = getParamValue("ExpectedResult");
        boolean compareResult = true;

        ResultScanner scanner = null;
        resultVector.clear();
        try {
            scanner = htable.getScanner(scan);
            /*for(Result aa: scanner){
                System.out.println(aa);
                HashMap<String,String> rowResult = new HashMap<String, String>();
                resultVector.add(rowResult);
            }*/
            for (Result rr = scanner.next(); rr != null; rr = scanner.next())
            {
                //get row key
                //String key = Bytes.toString(rr.getRow());
                //logger.info(String.format("Got scan result for key: %s", key));

                //get rowResult
                HashMap<String,String> rowResult = new HashMap<String, String>();
                for (KeyValue kv : rr.raw()) {
                    rowResult.put(Bytes.toString(kv.getQualifier()),Bytes.toString(kv.getValue()));
                }
                //add rowResult to result vector
                resultVector.add(rowResult);
            }
            //logger.info(String.format("Scan result number: %d", resultVector.size()));

            if(isCheckResult.equalsIgnoreCase("true")){
                compareResult = CompareUtils.compareResultWithRefAsString(String.valueOf(resultVector.size()), expectedResultStr);
            }else{
                compareResult = true;
            }
            //logger.info(String.format("Enter HBaseScanTransaction execute() , RESULT: %s",  compareResult));
            if(false == compareResult){
                logger.error("Result NOT equal!\n");
                logger.error(String.format("##Expected Result: %s",expectedResultStr));
                logger.error(String.format("##Output Result: %d", resultVector.size()));
                return false;
            }
        }catch (IOException e) {
            e.printStackTrace();
            logger.error("Error occurs during getting/parsing scan result!");
            return false;
        }
        finally {
            scanner.close();
        }
        return true;
    }

    protected boolean configureColumnInfoForScan(String columnInfoStr){
        StringTokenizer st = new StringTokenizer(columnInfoStr, ",");
        while (st.hasMoreTokens()) {
            String column[] = st.nextToken().split(":");
            switch (column.length) {
                case 2:
                    scan.addColumn(Bytes.toBytes(column[0]), Bytes.toBytes(column[1]));
                    break;
                case 1:
                    scan.addFamily(Bytes.toBytes(column[0]));
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    protected boolean configureFilterForScan(){
        return true;
    }

}
