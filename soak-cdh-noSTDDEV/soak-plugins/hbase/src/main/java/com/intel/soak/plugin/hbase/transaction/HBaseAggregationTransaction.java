package com.intel.soak.plugin.hbase.transaction;

import java.util.StringTokenizer;
import org.apache.hadoop.hbase.client.coprocessor.AggregationClient;
import org.apache.hadoop.hbase.client.coprocessor.LongColumnInterpreter;
import org.apache.hadoop.hbase.coprocessor.ColumnInterpreter;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos;
import org.apache.hadoop.hbase.util.Bytes;
import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.plugin.hbase.util.CompareUtils;
import com.intel.soak.plugin.hbase.util.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: xhao1
 * Date: 11/22/13
 * Time: 2:59 PM
 * To change this template use File | Settings | File Templates.
 */
@Plugin(desc = "HBaseAggregationTransaction", type = PLUGIN_TYPE.TRANSACTION)
public class HBaseAggregationTransaction extends HBaseDummyTransaction{
    protected static final String COLUMN_INTERPRETER_LONGSTR = "LongStr";
    protected static final String COLUMN_INTERPRETER_COMPOSITELONGSTR = "CompositeLongStr";
    protected static final String COLUMN_INTERPRETER_LONG = "Long";
    protected static final String SEPARATOR = "#\\$";

    protected AggregationClient aClient;

    @Override
    public boolean startup() {
        super.startup();
        aClient = new AggregationClient(conf);
        return true;

    }

    @Override
    public boolean execute() {
        //logger.info(String.format("Enter HBaseAggregationTransaction execute()" ));
        String actionStr = getParamValue("Action");
        String scanParamStr = getParamValue("Scan");
        String ciParamStr = getParamValue("ColumnInterpreter");
        ciParamStr = StringUtils.setAsDefaultValueIfEmpty(ciParamStr,COLUMN_INTERPRETER_LONGSTR);
        String isCheckResult = getParamValue("IsCheckResult");
        isCheckResult = StringUtils.setAsDefaultValueIfEmpty(isCheckResult,"false");
        String expectedResultStr = getParamValue("ExpectedResult");
        byte[] tableName = htable.getTableName();

            Scan scan = new Scan();
            if (!configureScan(scan, scanParamStr)){
                logger.error("Failed to configure Scan!");
                logger.error(String.format("##Scan Param String: %s", scanParamStr));
                return false;
            }

            final ColumnInterpreter<Long, Long, HBaseProtos.EmptyMsg, HBaseProtos.LongMsg, HBaseProtos.LongMsg> columnInterpreter = getColumnInterpreter(ciParamStr);
            if (columnInterpreter == null) {
                logger.error("ColumnInterpreter is NULL!");
                logger.error(String.format("##ColumnInterpreter Param String: %s", ciParamStr));
                return false;
            }

            Long longResult = null;
            Double doubleResult = null;
            boolean compareResult = true;
            String outputResultStr = "";
            try {
                if (actionStr.equalsIgnoreCase("rowcount")) {
                    longResult = aClient.rowCount(htable, columnInterpreter, scan);
                    outputResultStr = (null==longResult)? "" : longResult.toString();
                } else if (actionStr.equalsIgnoreCase("max")) {
                    longResult = aClient.max(htable, columnInterpreter, scan);
                    outputResultStr = (null==longResult)? "" : longResult.toString();
                } else if (actionStr.equalsIgnoreCase("min")) {
                    longResult = aClient.min(htable, columnInterpreter, scan);
                    outputResultStr = (null==longResult)? "" : longResult.toString();
                } else if (actionStr.equalsIgnoreCase("sum")) {
                    longResult = aClient.sum(htable, columnInterpreter, scan);
                    outputResultStr = (null==longResult)? "" : longResult.toString();
                } else if (actionStr.equalsIgnoreCase("avg")) {
                    doubleResult = aClient.avg(htable, columnInterpreter, scan);
                    outputResultStr = (null==doubleResult)? "" : doubleResult.toString();
                } else if (actionStr.equalsIgnoreCase("std")) {
                    doubleResult = aClient.std(htable, columnInterpreter, scan);
                    outputResultStr = (null==doubleResult)? "" : doubleResult.toString();
                } else if (actionStr.equalsIgnoreCase("median")) {
                    longResult = aClient.median(htable, columnInterpreter, scan);
                    outputResultStr = (null==longResult)? "" : longResult.toString();
                } else{
                    logger.error("The input action '" + actionStr + "' is NOT supported!");
                    return false;
                }
                //logger.info(String.format("Enter HBaseAggregationTransaction execute() , OUTPUT: %s",  outputResultStr));
                if(isCheckResult.equalsIgnoreCase("true")){
                    compareResult = CompareUtils.compareResultWithRefAsString(outputResultStr,expectedResultStr);
                }else{
                    compareResult = true;
                }
                //logger.info(String.format("Enter HBaseAggregationTransaction execute() , RESULT: %s",  compareResult));
                if(false == compareResult){
                    logger.error("Result NOT equal!\n");
                    logger.error(String.format("##Expected Result: %s",expectedResultStr));
                    logger.error(String.format("##Output Result: %s",outputResultStr));
                    return false;
                }
            } catch (Throwable e) {
                e.printStackTrace();
                logger.error("Error occurs during aggregation operation!");
                return false;
            }
            return true;
    }

    protected boolean configureScan(Scan scan, String paramStr) {
        String[] paramArray = paramStr.split(SEPARATOR, -1);
        String columnInfo = "";
        String startRowKey = "";
        String stopRowKey = "";
        if(paramArray.length > 2){
            columnInfo = paramArray[0];
            startRowKey = paramArray[1];
            stopRowKey = paramArray[2];
        }else if(paramArray.length == 2){
            columnInfo = paramArray[0];
            startRowKey = paramArray[1];
        }else if(paramArray.length == 1){
            columnInfo = paramArray[0];
        }else{
            return false;
        }
        StringTokenizer st = new StringTokenizer(columnInfo, ",");
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
        if(!StringUtils.isNullOrEmpty(startRowKey)){
            scan.setStartRow(Bytes.toBytes(startRowKey));
        }
        if(!StringUtils.isNullOrEmpty(stopRowKey)){
            scan.setStopRow(Bytes.toBytes(stopRowKey));
        }
        return true;
    }

    protected ColumnInterpreter<Long, Long, HBaseProtos.EmptyMsg, HBaseProtos.LongMsg, HBaseProtos.LongMsg> getColumnInterpreter(String ciParamStr) {
        String[] paramArray = ciParamStr.split(SEPARATOR, -1);
        if(paramArray.length < 1){
            return null;
        }
        String ciStr = paramArray[0];
        /*if (ciStr.equalsIgnoreCase(COLUMN_INTERPRETER_LONGSTR)) {
            return new LongStrColumnInterpreter();
        } else if (ciStr.equalsIgnoreCase(COLUMN_INTERPRETER_COMPOSITELONGSTR)) {
            if(paramArray.length < 3){
                return null;
            }
            String delimStr = paramArray[1];
            String indexStr = paramArray[2];
            int indexInt = Integer.parseInt(indexStr);
            return new CompositeLongStrColumnInterpreter(delimStr, indexInt);
        } else */if (ciStr.equalsIgnoreCase(COLUMN_INTERPRETER_LONG)) {
            return new LongColumnInterpreter();
        } else{
            return null;
        }
    }

}
