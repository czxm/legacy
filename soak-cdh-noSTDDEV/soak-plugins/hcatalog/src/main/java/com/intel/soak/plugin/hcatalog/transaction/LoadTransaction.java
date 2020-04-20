package com.intel.soak.plugin.hcatalog.transaction;

import com.intel.soak.plugin.hcatalog.HCatMixUtils;
import com.intel.soak.plugin.hcatalog.load.HadoopLoadGenerator;
import com.intel.soak.plugin.hcatalog.load.LoadTestAllResults;
import com.intel.soak.plugin.hcatalog.load.LoadTestStatistics;
import com.intel.soak.plugin.hcatalog.load.hadoop.ReduceResult;
import com.intel.soak.plugin.hcatalog.load.tasks.HCatLoadTask;
import com.intel.soak.plugin.hcatalog.loadstore.LoadStoreScriptRunner;
import com.intel.soak.transaction.AbstractTransaction;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;

/**
 * Created by niweimin on 2014/9/2.
 */
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.transaction.AbstractTransaction;
import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
@Plugin(desc = "LoadTransaction", type = PLUGIN_TYPE.TRANSACTION)
public class LoadTransaction extends AbstractTransaction {
    private static LoadTestAllResults loadTestAllResults;
    private LoadStoreScriptRunner loadStoreScriptRunner;
    private static final String LOAD_TEST_CONF_FILE_ARG_NAME = "testConf";
    private static String resultsDir;
    private static final String RESULTS_ALL_HTML = "load_test_results_all.html";
    private static final String RESULTS_ALL_JSON = "load_test_results_all.json";
    ArrayList<Object[]> hcatSpecFiles = new ArrayList<Object[]>();
    private String user;
    public void loadTestConfFiles() {
        final String loadTestFiles = getParamValue(LOAD_TEST_CONF_FILE_ARG_NAME);
        final ArrayList<Object[]> loadTestConfFiles = new ArrayList<Object[]>();

        if (loadTestFiles.equals("")) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            final String loadTestConfDirName = getParamValue("test_cache_dir") + "/load";
            logger.info("Will look in directory:" + loadTestConfDirName + " for load test configuration files");
            File loadTestConfDir = new File(loadTestConfDirName);
            //logger.info(loadTestConfDir == null? "null" : "not null");
            FilenameFilter propertiesFilter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".properties");
                }
            };
            logger.info(loadTestConfDir.listFiles(propertiesFilter).length + "");
            for (File file : loadTestConfDir.listFiles(propertiesFilter)) {

                loadTestConfFiles.add(new Object[]{file.getAbsolutePath()});
            }
        } else {
            logger.info(MessageFormat.format("Honouring command line option: -D{0}={1}", LOAD_TEST_CONF_FILE_ARG_NAME, loadTestFiles));
            loadTestConfFiles.add(loadTestFiles.split(","));
        }
        this.hcatSpecFiles = loadTestConfFiles;
    }

    public void beforeClass(){
        user = getParamValue("user");
        resultsDir = HCatMixUtils.getTempDirName() + "/results/loadtest/";
        File resultsDirObj = new File(resultsDir);
        resultsDirObj.mkdirs();
        logger.info("Created results directory: " + resultsDirObj.getAbsolutePath());
        loadTestAllResults = new LoadTestAllResults(resultsDir + "/" + RESULTS_ALL_HTML,
                resultsDir + "/" + RESULTS_ALL_JSON);
    }
    public void beforeTest() throws  Exception{
        String hcatTableSpecFile = getParamValue("test_cache_dir") + "/" +HCatLoadTask.LOAD_TEST_HCAT_SPEC_FILE;
        loadStoreScriptRunner = new LoadStoreScriptRunner(hcatTableSpecFile, 1, user);
        loadStoreScriptRunner.setUp(false);
        loadStoreScriptRunner.runPigLoadHCatStoreScript();
    }
    @Override
    public boolean startup() {
        beforeClass();
        try {
            beforeTest();
        }catch(Exception e){
            logger.error("Exception caught: " + e.getMessage());
        }
        loadTestConfFiles();
        return true;
    }

    @Override
    public boolean execute() {
        for(Object[] file : hcatSpecFiles) {
            String confFile = (String) file[0];
            HadoopLoadGenerator loadGenerator = new HadoopLoadGenerator();
            try {
                SortedMap<Long, ReduceResult> results = loadGenerator.runLoadTest(confFile, null);
            }catch(Exception e){
                e.printStackTrace();
               logger.error("Test failed: " + confFile + ". " + e.getMessage());
            }
        }
        return false;
    }

    @Override
    public boolean afterExecute() {
        try {
            loadStoreScriptRunner.deleteHCatTables();

        }catch(Exception e){
            return false;
        }
        return true;
    }
}
