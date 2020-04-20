package com.intel.soak.plugin.hdfs.basic.transaction;

import com.intel.soak.logger.TransactionLogger;
import com.intel.bigdata.common.util.Command;
import com.intel.soak.plugin.hdfs.CLITestHelper;
import com.intel.soak.plugin.hdfs.util.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FsShell;
import org.apache.hadoop.hdfs.DFSConfigKeys;
import org.apache.hadoop.util.StringUtils;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public abstract class HDFSBasicTest extends CLITestHelper {

    private String test_cache_dir;
    private String TEST_DIR_ABSOLUTE;
    private String sug;
    protected String namenode;
    protected String replication;
    private TransactionLogger logger;
    private boolean checkResult = true;
    private String user;

    private List<String> execCMD(String[] cmd) throws Exception {
        List<String> result = new ArrayList<String>();
        Command.executeWithOutput(result, 0, cmd);
        return result;
    }

    public HDFSBasicTest(TransactionLogger logger, String user, String testRoot, String test_cache_dir, Boolean checkResult) {
        this.logger = logger;
        this.TEST_DIR_ABSOLUTE = testRoot + "/testcli_" + Long.valueOf(System.currentTimeMillis());
        this.test_cache_dir = test_cache_dir;
        this.checkResult = checkResult;
        this.user = user;
    }


    /**
     * Copied from CLITestHelper
     * Removed junit dependencies
     */
    protected void readTestConfigFile() {
        String testConfigFile = getTestFile();
        if (testsFromConfigFile == null) {
            boolean success = false;
            testConfigFile = test_cache_dir + File.separator + testConfigFile;
            try {
                SAXParser p = (SAXParserFactory.newInstance()).newSAXParser();
                p.parse(testConfigFile, getConfigParser());
                success = true;
            } catch (Exception e) {
                logger.info("File: " + testConfigFile + " not found");
                success = false;
            }

        }
    }

    public void setUp() throws Exception {
        readTestConfigFile();
        conf = new Configuration();
        clitestDataDir =
                new File(test_cache_dir).toURI().toString().replace(' ', '+');
        sug = conf.get(DFSConfigKeys.DFS_PERMISSIONS_SUPERUSERGROUP_KEY, "hadoop");
        namenode = conf.get(DFSConfigKeys.FS_DEFAULT_NAME_KEY, "file:///");
        replication = conf.get(DFSConfigKeys.DFS_REPLICATION_KEY, "3");

        String[] createTestcliDirCmds = {"sudo -u " + user + " hadoop fs -mkdir " + TEST_DIR_ABSOLUTE};
        for(String c : createTestcliDirCmds)
            execCMD(c.split(" "));
    }

    public void tearDown() throws Exception{
        String removeTestcliDirCmd = "sudo -u " + user + " hadoop fs -rmr " + TEST_DIR_ABSOLUTE;
        execCMD(removeTestcliDirCmd.split(" "));
    }

    public int getNrOfTests(){
        return testsFromConfigFile.size();
    }

    @Override
    protected String expandCommand(final String cmd) {
        String expCmd = super.expandCommand(cmd);
        expCmd = expCmd.replaceAll("TEST_DIR_ABSOLUTE", TEST_DIR_ABSOLUTE);
        expCmd = expCmd.replaceAll("supergroup", "(" + sug + "|hadoop|supergroup)");
        expCmd = expCmd.replaceAll("NAMENODE", namenode);
        expCmd = expCmd.replaceAll("USER_NAME", user);
        expCmd = expCmd.replace("-( )*1( )*", "-( )*" + replication + "( )*");
        expCmd = expCmd.replace("[a-z]*", user) ;
        if(expCmd.startsWith("^"))
            expCmd = expCmd.substring(1);
        return expCmd;
    }

    @Override
    protected CommandExecutor.Result execute(CLICommand cmd) throws Exception {
        if (cmd.getType() instanceof CLICommandFS) {
            CommandExecutor cmdExecutor = new FSCmdExecutor(namenode, new FsShell(conf)){
                @Override
                protected String[] getCommandAsArgs(String cmd, String masterKey, String master) {
                    StringTokenizer tokenizer = new StringTokenizer(cmd, " ");
                    String[] args = new String[tokenizer.countTokens()];

                    int i = 0;
                    while (tokenizer.hasMoreTokens()) {
                        args[i] = tokenizer.nextToken();

                        args[i] = args[i].replaceAll(masterKey, master);
                        args[i] = args[i].replaceAll("CLITEST_DATA",
                                new File(test_cache_dir).
                                        toURI().toString().replace(' ', '+'));
                        args[i] = args[i].replaceAll("USERNAME", user);
                        args[i] = args[i].replaceAll("USER_NAME", user);
                        args[i] = args[i].replaceFirst("TEST_DIR_ABSOLUTE", TEST_DIR_ABSOLUTE);
                        i++;
                    }

                    return args;
                }

                @Override
                public Result executeCommand(final String cmd) throws Exception {
                    int exitCode = 0;
                    Exception lastException = null;

                    StringBuilder sb = new StringBuilder();

                    try {
                        for(String s : exec(cmd)){
                            sb.append(s);
                            sb.append("\n");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        lastException = e;
                        exitCode = -1;
                    } finally {
                    }
                    return new Result(sb.toString(), exitCode, lastException, cmd);
                }

                protected List<String> exec(final String cmd) throws Exception{
                    String[] args = getCommandAsArgs("sudo -u " + user + " hadoop fs " + cmd, "NAMENODE", this.namenode);
                    return execCMD(args);
                }
            };
            return cmdExecutor.executeCommand(cmd.getCmd());
        } else {
            throw new IllegalArgumentException("Unknown type of test command: " + cmd.getType());
        }
    }

    // copied from CLITestHelper
    private boolean compareTestOutput(ComparatorData compdata, CommandExecutor.Result cmdResult) {
        // Compare the output based on the comparator
        String comparatorType = compdata.getComparatorType();
        Class<?> comparatorClass = null;

        // If testMode is "test", then run the command and compare the output
        // If testMode is "nocompare", then run the command and dump the output.
        // Do not compare

        boolean compareOutput = false;

        if (testMode.equals(TESTMODE_TEST)) {
            try {
                // Initialize the comparator class and run its compare method
                comparatorClass = Class.forName("com.intel.soak.plugin.hdfs.util." +
                        comparatorType);
                ComparatorBase comp = (ComparatorBase) comparatorClass.newInstance();
                compareOutput = comp.compare(cmdResult.getCommandOutput(),
                        expandCommand(compdata.getExpectedOutput()));
            } catch (Exception e) {
                logger.error("Error in instantiating the comparator" + e);
            }
        }

        return compareOutput;
    }

    public boolean runOneTest(int index){
        CLITestData testdata = testsFromConfigFile.get(index - 1);

        // Execute the test commands
        ArrayList<CLICommand> testCommands = testdata.getTestCommands();
        CommandExecutor.Result cmdResult = null;
        for (CLICommand cmd : testCommands) {
            try {
                cmdResult = execute(cmd);
            } catch (Exception e) {
                logger.error(StringUtils.stringifyException(e));
            }
        }

        boolean overallTCResult = true;

        if(checkResult){
            // Run comparators
            ArrayList<ComparatorData> compdata = testdata.getComparatorData();
            for (ComparatorData cd : compdata) {
                final String comptype = cd.getComparatorType();

                boolean compareOutput = false;

                if (! comptype.equalsIgnoreCase("none")) {
                    compareOutput = compareTestOutput(cd, cmdResult);
                    overallTCResult &= compareOutput;
                }

                cd.setExitCode(cmdResult.getExitCode());
                cd.setActualOutput(cmdResult.getCommandOutput());
                cd.setTestResult(compareOutput);
            }
            testdata.setTestResult(overallTCResult);
        }

        // Execute the cleanup commands
        ArrayList<CLICommand> cleanupCommands = testdata.getCleanupCommands();
        for (CLICommand cmd : cleanupCommands) {
            try {
                execute(cmd);
            } catch (Exception e) {
                logger.error(StringUtils.stringifyException(e));
            }
        }

        if(!overallTCResult){
            logger.info((overallTCResult ? "Passed" : "Failed") + ": case(" + index + ") " + testdata.getTestDesc());
            logger.info(cmdResult.getCommandOutput());
        }
        return overallTCResult;
    }

}
