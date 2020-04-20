package com.intel.xml.rss.util;

import java.io.File;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;

/**
 * Author: hshen5
 */
public class SimpleCmdExecutor {

    private boolean redirectErrorStream = true;

    private File workingDirectory = null;

    private Map<String, String> environment = null;

    public SimpleCmdExecutor(boolean redirectErrorStreamP,
            File workingDirectoryP, Map<String, String> environmentP) {
        this.redirectErrorStream = redirectErrorStreamP;
        this.workingDirectory = workingDirectoryP;
        this.environment = environmentP;
    }

    public SimpleCmdExecutor() {
        this(true, null, null);
    }

    public CmdExecutionResult execute(String[] cmd, Writer outWriter,
            Writer errWriter) throws CmdExecutionException {
        return execute(Arrays.asList(cmd), outWriter, errWriter);
    }

    public CmdExecutionResult execute(String[] cmd)
            throws CmdExecutionException {
        return execute(Arrays.asList(cmd), null, null);
    }

    public CmdExecutionResult execute(List<String> cmd, Writer outWriter,
            Writer errWriter) throws CmdExecutionException {
        Process process = null;
        Thread logger1 = null;
        Thread logger2 = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);

            pb.redirectErrorStream(redirectErrorStream);

            if (workingDirectory != null) {
                pb.directory(workingDirectory);
            }
            if (environment != null && environment.size() > 0) {
                pb.environment().putAll(environment);
            }

            CmdExecutionResult result = new CmdExecutionResult();

            process = pb.start();

            FutureTask<String> logger1Task = new FutureTask<String>(
                    new CmdExecutionStreamRecorder(process.getInputStream(),
                            outWriter));
            logger1 = new Thread(logger1Task);

            logger1.start();

            FutureTask<String> logger2Task = null;

            if (!redirectErrorStream) {
                logger2Task = new FutureTask<String>(
                        new CmdExecutionStreamRecorder(
                                process.getErrorStream(), errWriter));
                logger2 = new Thread(logger2Task);
                logger2.start();
            }

            result.exitValue = process.waitFor();

            result.log = logger1Task.get();

            if (!redirectErrorStream) {
                result.log2 = logger2Task.get();
            } else {
                result.log2 = null;
            }
            process = null;
            return result;
        } catch (Exception e) {
            if (process != null) {
                process.destroy();
            }
            StringBuilder cmdSB = new StringBuilder();
            for (String str : cmd) {
                cmdSB.append(str).append(" ");
            }
            throw new CmdExecutionException(new String(cmdSB), Routine
                    .getExceptionLogInfo(e));
        }
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    public void setRedirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
    }

    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public static void main(String[] args) {
        // for test prupose
    }

}
