package com.intel.soak.plugin.hbase.transaction;

import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.plugin.hbase.util.StringUtils;

import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * Created with IntelliJ IDEA.
 * User: xhao1
 * Date: 11/27/13
 * Time: 3:27 PM
 * To change this template use File | Settings | File Templates.
 */
@Plugin(desc = "HBaseScanWithComparisonFilterTransaction", type = PLUGIN_TYPE.TRANSACTION)
public class HBaseScanWithComparisonFilterTransaction extends HBaseScanTransaction{
    protected static final String DEFAULT_FILTER_TYPE = "RowFilter";
    protected static final String DEFAULT_COMPARISON_OPERATOR = "EQUAL";
    protected static final String DEFAULT_COMPARATOR = "BinaryComparator";

    protected CompareFilter.CompareOp compareOp;
    protected ByteArrayComparable comparator;
    protected CompareFilter filter;

    @Override
    public boolean startup() {
        super.startup();

        String filterTypeStr = getParamValue("FilterType");
        filterTypeStr = StringUtils.setAsDefaultValueIfEmpty(filterTypeStr, DEFAULT_FILTER_TYPE);
        String comparisonOperatorStr = getParamValue("ComparisonOperator");
        comparisonOperatorStr = StringUtils.setAsDefaultValueIfEmpty(comparisonOperatorStr, DEFAULT_COMPARISON_OPERATOR);
        String comparatorStr = getParamValue("Comparator");
        comparatorStr = StringUtils.setAsDefaultValueIfEmpty(comparatorStr, DEFAULT_COMPARATOR);
        String comparatorParamStr = getParamValue("ComparatorParam");
        comparatorParamStr = StringUtils.setAsDefaultValueIfEmpty(comparatorParamStr, "");

        configureFilter(filterTypeStr, comparisonOperatorStr, comparatorStr, comparatorParamStr);

        return true;
    }

    @Override
    protected boolean configureFilterForScan(){
        scan.setFilter(filter);
        return true;
    }

    protected boolean configureCompareOp(String comparisonOperatorStr){
        if(comparisonOperatorStr.equalsIgnoreCase("LESS")){
            compareOp = CompareFilter.CompareOp.LESS;
        }else if(comparisonOperatorStr.equalsIgnoreCase("LESS_OR_EQUAL")){
            compareOp = CompareFilter.CompareOp.LESS_OR_EQUAL;
        }else if(comparisonOperatorStr.equalsIgnoreCase("EQUAL")){
            compareOp = CompareFilter.CompareOp.EQUAL;
        }else if(comparisonOperatorStr.equalsIgnoreCase("NOT_EQUAL")){
            compareOp = CompareFilter.CompareOp.NOT_EQUAL;
        }else if(comparisonOperatorStr.equalsIgnoreCase("GREATER_OR_EQUAL")){
            compareOp = CompareFilter.CompareOp.GREATER_OR_EQUAL;
        }else if(comparisonOperatorStr.equalsIgnoreCase("GREATER")){
            compareOp = CompareFilter.CompareOp.GREATER;
        }else if(comparisonOperatorStr.equalsIgnoreCase("NO_OP")){
            compareOp = CompareFilter.CompareOp.NO_OP;
        }else{
            return false;
        }
        return true;
    }

    protected boolean configureComparator(String comparatorStr, String comparatorParamStr){
        String[] params = comparatorParamStr.split("#\\$");;
        if(comparatorStr.equalsIgnoreCase("BinaryComparator")){
            if(params.length < 1){
                return false;
            }
            comparator = new BinaryComparator(Bytes.toBytes(params[0]));
        }else if(comparatorStr.equalsIgnoreCase("BinaryPrefixComparator")){
            if(params.length < 1){
                return false;
            }
            comparator = new BinaryPrefixComparator(Bytes.toBytes(params[0]));
        }else if(comparatorStr.equalsIgnoreCase("NullComparator")){
            comparator = new NullComparator();
        }else if(comparatorStr.equalsIgnoreCase("BitComparator")){
            if(params.length < 2){
                return false;
            }
            String bitwiseOpStr = params[1];
            if(bitwiseOpStr.equalsIgnoreCase("AND")){
                comparator = new BitComparator(Bytes.toBytes(params[0]), BitComparator.BitwiseOp.AND);
            }else if(bitwiseOpStr.equalsIgnoreCase("OR")){
                comparator = new BitComparator(Bytes.toBytes(params[0]), BitComparator.BitwiseOp.OR);
            }else if(bitwiseOpStr.equalsIgnoreCase("XOR")){
                comparator = new BitComparator(Bytes.toBytes(params[0]), BitComparator.BitwiseOp.XOR);
            }else{
                return false;
            }
        }else if(comparatorStr.equalsIgnoreCase("RegexStringComparator")){
            if(params.length < 1){
                return false;
            }
            comparator = new RegexStringComparator(params[0]);
        }else if(comparatorStr.equalsIgnoreCase("SubstringComparator")){
            if(params.length < 1){
                return false;
            }
            comparator = new SubstringComparator(params[0]);
        }else{
            return false;
        }
        return true;
    }

    protected boolean configureFilter(String filterTypeStr, String comparisonOperatorStr, String comparatorStr, String comparatorParamStr){

        if(!configureCompareOp(comparisonOperatorStr)){
            logger.error("Failed to configure CompareOp!");
            logger.error(String.format("##ComparisonOperator String: %s", comparisonOperatorStr));
            return false;
        }
        if(!configureComparator(comparatorStr, comparatorParamStr)){
            logger.error("Failed to configure Comparator!");
            logger.error(String.format("##Comparator String: %s", comparatorStr));
            logger.error(String.format("##ComparatorParam String: %s", comparatorParamStr));
            return false;
        }

        if(filterTypeStr.equalsIgnoreCase("RowFilter")){
            filter = new RowFilter(compareOp, comparator);
        }else if(filterTypeStr.equalsIgnoreCase("FamilyFilter")){
            filter = new FamilyFilter(compareOp, comparator);
        }else if(filterTypeStr.equalsIgnoreCase("QualifierFilter")){
            filter = new QualifierFilter(compareOp, comparator);
        }else if(filterTypeStr.equalsIgnoreCase("ValueFilter")){
            filter = new ValueFilter(compareOp, comparator);
        }else{
            return false;
        }
        logger.info(String.format("## compareOp: %s", compareOp.name()));
        logger.info(String.format("## comparator: %s", comparator.getClass().getCanonicalName()));
        logger.info(String.format("## filter: %s", filterTypeStr));
        return true;
    }

}
