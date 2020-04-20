package com.intel.soak.plugin.hdfs.basic.transaction;

import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.transaction.AbstractTransaction;

@Plugin(desc = "HDFSBasicTransaction", type = PLUGIN_TYPE.TRANSACTION)

public class HDFSBasicTransaction extends AbstractTransaction {

    HDFSBasicTest basicTest;
    int nextTest = 0;
    int selectedTest = 0;

    @Override
    public boolean startup(){
        try{
            boolean checkResult = true;
            String testRoot = getParamValue("testRoot");
            String checkResultStr = getParamValue("checkResult");
            if(checkResultStr != null && checkResultStr.equalsIgnoreCase("false")){
                checkResult = false;
            }
            String selectedTestStr = getParamValue("case");
            if(selectedTestStr != null && selectedTestStr.length() > 0){
                selectedTest = Integer.parseInt(selectedTestStr);
            }
            final String testConf = getParamValue("testConf");
            basicTest = new HDFSBasicTest(logger, getParamValue("user"), testRoot, getParamValue("test_cache_dir"), checkResult){
                @Override
                protected String getTestFile() {
                    return testConf;
                }
            };
            basicTest.setUp();
            return basicTest.getNrOfTests() > 0;
        }
        catch(Exception e){
            logger.error(e.getMessage());
        }
        return false;
    }

    @Override
    public void shutdown(){
        try{
            if(basicTest != null)
                basicTest.tearDown();
        }
        catch(Exception e){
            logger.error(e.getMessage());
        }
    }

    @Override
    public boolean beforeExecute(){
        if(selectedTest > 0){
            nextTest = selectedTest;
        }
        else{
            if(basicTest.getNrOfTests() > 0){
                nextTest = nextTest + 1;
                if(nextTest > basicTest.getNrOfTests())
                    nextTest = 1;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean execute() {
        try{
            if(nextTest % 5 == 0){
                logger.info(nextTest + "/" + basicTest.getNrOfTests() + " completed");
            }
            return basicTest.runOneTest(nextTest);
        }
        catch(Exception e){
            logger.error("Failed: case(\"" + nextTest + "\") " + e.getMessage());
            return false;
        }
    }
}
