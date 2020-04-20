package com.intel.cedar.agent.impl;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.UUID;

import com.intel.cedar.agent.runtime.ServerRuntimeInfo;
import com.intel.cedar.tasklet.IResult;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ITaskRunner;
import com.intel.cedar.tasklet.ResultID;
import com.intel.cedar.tasklet.impl.CommandTaskItem;
import com.intel.cedar.tasklet.impl.CommandTaskRunner;
import com.intel.cedar.tasklet.impl.Result;
import com.intel.xml.rss.util.rexec.RExec;
import com.intel.xml.rss.util.rexec.RJobKilledException;
import com.intel.xml.rss.util.rexec.RJobNotFoundException;
import com.intel.xml.rss.util.rexec.RJobTimedOutException;
import com.intel.xml.rss.util.rexec.RServerUnreachableException;

public class SimpleAgent extends AbstractAgent {
    public SimpleAgent(String host) {
        super(host, "");
    }

    public void kill(ITaskRunner runner) {
        if (!(runner instanceof CommandTaskRunner)) {
            return;
        }
        String runningid = getRunningId(runner);
        try {
            RExec rexec = new RExec();
            rexec.killJobById(host, runningid);
        } catch (RServerUnreachableException e) {
            System.err.println("Rserver unreachable");
        } catch (RJobNotFoundException e) {
            System.err.println("Job not found");
        }
    }

    public IResult run(ITaskRunner runner, ITaskItem taskitem, String timeout,
            String cwd) {
        if (!(runner instanceof CommandTaskRunner)) {
            return null;
        }
        String cmdline = ((CommandTaskItem) taskitem).getCommandLine();
        String runningid = UUID.randomUUID().toString();
        setRunningId(runner, runningid);
        Result result = new Result();
        int ret = 0;
        Writer writer = null;
        OutputStream output = getOutputStream(runner);
        if (output != null)
            writer = new OutputStreamWriter(output);
        else
            writer = new StringWriter();
        try {
            RExec rexec = new RExec();
            String[] args = new String[4];
            args[0] = host;
            args[1] = "-j";
            args[2] = runningid;
            args[3] = "-t";
            args[4] = timeout;
            args[5] = "-d";
            args[6] = cwd;
            args[7] = cmdline;
            ret = rexec.exec(writer, args);
        } catch (RJobKilledException e) {
            result.setID(ResultID.Killed);
            result.setFailureMessage(e.getMessage());
        } catch (RJobTimedOutException e) {
            result.setID(ResultID.Timeout);
            result.setFailureMessage(e.getMessage());
        } catch (RServerUnreachableException e) {
            result.setID(ResultID.Unreachable);
            result.setFailureMessage(e.getMessage());
        }
        result.setLog(writer.toString());
        if (ret == 0) {
            result.setID(ResultID.Passed);
        } else {
            result.setID(ResultID.Failed);
        }
        return result;
    }

    public TaskRunnerStatus getStatus(ITaskRunner runner) {
        if (!(runner instanceof CommandTaskRunner)) {
            return TaskRunnerStatus.NotAvailable;
        }
        String runningid = getRunningId(runner);
        StringWriter writer = new StringWriter();
        try {
            RExec rexec = new RExec();
            String[] args = new String[4];
            args[0] = host;
            args[1] = "-l";
            rexec.exec(writer, args);
            if (writer.toString().contains(runningid))
                return TaskRunnerStatus.Started;
            else
                return TaskRunnerStatus.Evicted;
        } catch (Exception e) {
            return TaskRunnerStatus.NotAvailable;
        }
    }

    public ServerRuntimeInfo getServerInfo() {
        RExec r = new RExec();
        RExec.QueryServerResult result = r.queryServer(host);
        if (result == null)
            return null;
        else
            return new ServerRuntimeInfo(result);
    }
}
