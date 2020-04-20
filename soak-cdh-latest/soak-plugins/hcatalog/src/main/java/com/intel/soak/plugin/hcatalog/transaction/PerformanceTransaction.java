package com.intel.soak.plugin.hcatalog.transaction;

import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.plugin.hcatalog.HCatMixSetup;
import com.intel.soak.transaction.AbstractTransaction;
import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.hcatalog.HCatMixUtils;
import com.intel.soak.plugin.hcatalog.loadstore.LoadStoreScriptRunner;
import com.intel.soak.plugin.hcatalog.loadstore.LoadStoreTestAllResults;
import com.intel.soak.plugin.hcatalog.loadstore.LoadStoreTestStatistics;
import com.intel.soak.plugin.hcatalog.performance.conf.LoadStoreTestConf;
import com.intel.soak.plugin.hcatalog.performance.conf.LoadStoreTestsConf;
import com.intel.soak.transaction.AbstractTransaction;
import com.intel.soak.logger.TransactionLogger;

import org.perf4j.GroupedTimingStatistics;
import org.testng.Assert;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.perf4j.GroupedTimingStatistics;

import java.io.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.*;

import static org.testng.Assert.assertNotNull;

/**
 * Created by niweimin on 2014/8/27.
 */
@Plugin(desc = "PerformanceTransaction", type = PLUGIN_TYPE.TRANSACTION)
public class PerformanceTransaction extends AbstractTransaction {
    ArrayList<Object[]> hcatSpecFiles = new ArrayList<Object[]>();
    private static final String HCAT_SPEC_FILE_ARG_NAME = "testConf";
    private static final String HCAT_DATAGEN_NUM_MAPPERS_ARG_NAME = "numDataGenMappers";
    private static final String RESULTS_ALL_HTML = "load_store_results_all.html";
    private static final String RESULTS_ALL_JSON = "load_store_results_all.json";
    private static String resultsDir;
    private  String testCacheDir;
    private  String user;
    private  String metastore;
    private  HCatMixSetup hcat;

    public void loadStoreConfProvider() {
        final String hcatSpecFile = getParamValue(HCAT_SPEC_FILE_ARG_NAME);
        final ArrayList<Object[]> testArgs = new ArrayList<Object[]>();

        if (hcatSpecFile.equals("")) {
            testArgs.add(new Object[]{testCacheDir + "/performance/105MB_no_partitions.xml", 3});
            testArgs.add(new Object[]{testCacheDir + "/performance/105MB_300_partitions.xml", 3});
            testArgs.add(new Object[]{testCacheDir + "/performance/105MB_600_partitions.xml", 3});
            testArgs.add(new Object[]{testCacheDir + "/performance/105MB_900_partitions.xml", 3});
            testArgs.add(new Object[]{testCacheDir + "/performance/105MB_1200_partitions.xml", 3});
            testArgs.add(new Object[]{testCacheDir + "/performance/105MB_1500_partitions.xml", 3});
            testArgs.add(new Object[]{testCacheDir + "/performance/105MB_2000_partitions.xml", 3});
            testArgs.add(new Object[]{testCacheDir + "/performance/105MB_4000_partitions.xml", 3});
            testArgs.add(new Object[]{testCacheDir + "/performance/1GB_no_partitions.xml", 3});
            testArgs.add(new Object[]{testCacheDir + "/performance/1GB_300_partitions.xml", 3});
            testArgs.add(new Object[]{testCacheDir + "/performance/10GB_no_paritions.xml", 3});
            testArgs.add(new Object[]{testCacheDir + "/performance/10GB_300_partitions.xml", 3});
            testArgs.add(new Object[]{testCacheDir + "/performance/100GB_no_partitions.xml", 1});
            testArgs.add(new Object[]{testCacheDir + "/performance/100GB_300_parititons.xml", 1});
        } else {
            int numDataGenMappers = Integer.parseInt(System.getProperty(HCAT_DATAGEN_NUM_MAPPERS_ARG_NAME, "1"));
            Object[] argument = {testCacheDir + "/performance/" + hcatSpecFile, numDataGenMappers};
            testArgs.add(argument);
        }
        this.hcatSpecFiles = testArgs;
    }
    @Override
    public boolean startup() {
        try{
            setupResultsDirectory();
            user = getParamValue("user");
            testCacheDir = getParamValue("test_cache_dir");
            metastore = getParamValue("metastore");
            loadStoreConfProvider();
            boolean splitHMS = false;
            try{
                splitHMS = Boolean.parseBoolean(getParamValue("split_metastore"));
            }
            catch(Exception e){
            }
            hcat = new HCatMixSetup(user, metastore, splitHMS);
        }
        catch(Exception e){
            return false;
        }
        return true;
    }

    public  void setupResultsDirectory() {
        resultsDir = HCatMixUtils.getTempDirName() + "/results/loadstoretest/";
        File resultsDirObj = new File(resultsDir);
        resultsDirObj.mkdirs();
        logger.info("Created results directory: " + resultsDirObj.getAbsolutePath());
    }

    @Override
    public boolean execute(){
        for(Object [] file: hcatSpecFiles){
            String hcatSpecFileName = (String)file[0];
            int numDataGenMappers = (Integer)file[1];
            logger.info(MessageFormat.format("HCatalog spec file name: {0}, number of mapper for data generation {1}",
                    hcatSpecFileName, numDataGenMappers));
            try {
                LoadStoreScriptRunner runner = new LoadStoreScriptRunner(hcatSpecFileName, numDataGenMappers, user, hcat);
                try {
                    runner.setUp(true);
                    //runner.runPigLoadHCatStoreScript();
                    //runner.runHCatLoadPigStoreScript();
                   // runner.runPigLoadPigStoreScript();
                   // runner.runHCatLoadHCatStoreScript();
                    runner.alterPartition();
                } catch (Exception e) {
                    logger.error(MessageFormat.format("{0}: Run  failed", hcatSpecFileName));
                    e.printStackTrace();
                    return false;
                } finally {
                    runner.deleteHCatTables();
                    //runner.deletePigData();
                }
            }catch(Exception e){
                logger.error(MessageFormat.format("{0}: Run  failed", hcatSpecFileName));
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
