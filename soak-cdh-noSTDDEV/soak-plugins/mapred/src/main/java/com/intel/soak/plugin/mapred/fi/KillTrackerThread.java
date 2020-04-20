package com.intel.soak.plugin.mapred.fi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.util.Shell;
import org.apache.hadoop.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 11/27/13
 * Time: 4:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class KillTrackerThread extends Thread {

    private static final Log LOG = LogFactory.getLog(KillTrackerThread.class);

    private String dir = System.getProperty("user.dir");
    private volatile boolean killed = false;
    private JobClient jc;
    private RunningJob rJob;
    final private int thresholdMultiplier;
    private float threshold = 0.2f;
    private boolean onlyMapsProgress;
    private int numIterations;
    final private String slavesFile = dir + "/_reliability_test_slaves_file_";
    final String shellCommand = normalizeCommandPath("bin/slaves.sh");
    //    final private String STOP_COMMAND = "ps uwwx | grep java | grep " +
//            "org.apache.hadoop.mapred.TaskTracker" + " |" +
//            " grep -v grep | tr -s ' ' | cut -d ' ' -f2 | xargs kill -s STOP";
    final private String STOP_COMMAND = "service hadoop-tasktracker stop";
    //    final private String RESUME_COMMAND = "ps uwwx | grep java | grep " +
//            "org.apache.hadoop.mapred.TaskTracker" + " |" +
//            " grep -v grep | tr -s ' ' | cut -d ' ' -f2 | xargs kill -s CONT";
    final private String RESUME_COMMAND = "service hadoop-tasktracker start";

    //Only one instance must be active at any point
    public KillTrackerThread(JobClient jc, int threshaldMultiplier,
                             float threshold, boolean onlyMapsProgress, int numIterations) {
        this.jc = jc;
        this.thresholdMultiplier = threshaldMultiplier;
        this.threshold = threshold;
        this.onlyMapsProgress = onlyMapsProgress;
        this.numIterations = numIterations;
        setDaemon(true);
    }

    private String normalizeCommandPath(String command) {
        final String hadoopHome;
        if ((hadoopHome = System.getenv("HADOOP_HOME")) != null) {    //TODO
            command = hadoopHome + "/" + command;
        }
        return command;
    }

    public void setRunningJob(RunningJob rJob) {
        this.rJob = rJob;
    }

    public void kill() {
        killed = true;
    }

    public void run() {
        stopStartTrackers(true);
        if (!onlyMapsProgress) {
            stopStartTrackers(false);
        }
    }

    private void stopStartTrackers(boolean considerMaps) {
        if (considerMaps) {
            LOG.info("Will STOP/RESUME tasktrackers based on Maps'" +
                    " progress");
        } else {
            LOG.info("Will STOP/RESUME tasktrackers based on " +
                    "Reduces' progress");
        }
        LOG.info("Initial progress threshold: " + threshold +
                ". Threshold Multiplier: " + thresholdMultiplier +
                ". Number of iterations: " + numIterations);
        float thresholdVal = threshold;
        int numIterationsDone = 0;
        while (!killed) {
            try {
                float progress;
                if (jc.getJob(rJob.getID()).isComplete() ||
                        numIterationsDone == numIterations) {
                    return;
                }

                if (considerMaps) {
                    progress = jc.getJob(rJob.getID()).mapProgress();
                } else {
                    progress = jc.getJob(rJob.getID()).reduceProgress();
                }
                if (progress >= thresholdVal) {
                    numIterationsDone++;
                    ClusterStatus c;
                    stopTaskTrackers((c = jc.getClusterStatus(true)));
                    Thread.sleep((int) Math.ceil(1.5 * c.getTTExpiryInterval()));
                    startTaskTrackers();
                    thresholdVal = thresholdVal * thresholdMultiplier;
                }
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                killed = true;
                return;
            } catch (Exception e) {
                LOG.fatal(StringUtils.stringifyException(e));
            }
        }
    }

    private void stopTaskTrackers(ClusterStatus c) throws Exception {

        Collection<String> trackerNames = c.getActiveTrackerNames();
        ArrayList<String> trackerNamesList = new ArrayList<String>(trackerNames);
        Collections.shuffle(trackerNamesList);

        int count = 0;

        FileOutputStream fos = new FileOutputStream(new File(slavesFile));
        LOG.info(new Date() + " Stopping a few trackers");

        for (String tracker : trackerNamesList) {
            String host = convertTrackerNameToHostName(tracker);
            String localHost = InetAddress.getLocalHost().getHostName();
            if (localHost.equals(host)) continue;
            LOG.info(new Date() + " Marking tracker on host: " + host);
            fos.write((host + "\n").getBytes());
            if (count++ >= trackerNamesList.size() / 2) {
                break;
            }
        }
        fos.close();

        runOperationOnTT("suspend");
    }

    private void startTaskTrackers() throws Exception {
        LOG.info(new Date() + " Resuming the stopped trackers");
        runOperationOnTT("resume");
        new File(slavesFile).delete();
    }

    private void runOperationOnTT(String operation) throws IOException {
        Map<String, String> hMap = new HashMap<String, String>();
        hMap.put("HADOOP_SLAVES", slavesFile);
        StringTokenizer strToken;
        if (operation.equals("suspend")) {
            strToken = new StringTokenizer(STOP_COMMAND, " ");
        } else {
            strToken = new StringTokenizer(RESUME_COMMAND, " ");
        }
        String commandArgs[] = new String[strToken.countTokens() + 1];
        int i = 0;
        commandArgs[i++] = shellCommand;
        while (strToken.hasMoreTokens()) {
            commandArgs[i++] = strToken.nextToken();
        }
        String output = Shell.execCommand(hMap, commandArgs);
        if (output != null && !output.equals("")) {
            LOG.info(output);
        }
    }

    private String convertTrackerNameToHostName(String trackerName) {
        // Convert the trackerName to it's host name
        int indexOfColon = trackerName.indexOf(":");
        String trackerHostName = (indexOfColon == -1) ?
                trackerName :
                trackerName.substring(0, indexOfColon);
        return trackerHostName.substring("tracker_".length());
    }

}