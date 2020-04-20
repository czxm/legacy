package com.intel.xml.rss.util;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * A useful class to execute commands.
 * <p/>
 * To use the CmdExecutor class, you must creat one instance of CmdExecutor, by
 * doing this, the CmdExecutor will creat a thread pool.
 * <p/>
 * After that, you could optionally set the workingDirectory and environment.
 * <p/>
 * One thing to be take in mind is that you must call dispose() after you finish
 * all the invocation, which will dispose the underlying thread pool.
 * <p/>
 * Author: Shen, Han
 */

public class CmdExecutor {

    /**
     * Internal used thread pool, which is a cachedThreadPool.
     */
    private ExecutorService executorService = null;

    /**
     * Internal used logger
     */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * The working directory for each command invocation Could be null.
     */
    private File workingDirectory = null;

    /**
     * Whether or not we will join stdio & stderr
     */
    private boolean redirectErrorStream = true;

    /**
     * The enviroment variables to pass to each invocation. Could be null.
     */
    private Map<String, String> environment = null;

    public CmdExecutor() {
        executorService = Executors.newCachedThreadPool();
        logger.info("ExecutorService in CmdExecutor created");
    }

    public void dispose() {
        assert (executorService != null);
        logger.info("Shutting down ExecutorService in CmdExecutor ...");
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        logger.info("ExecutorService in CmdExecutor has been shut down");
    }

    public CmdExecutionResult execute(String cmd) throws CmdExecutionException {
        return execute(Arrays.asList(cmd.split("\\s+")));
    }

    /**
     * Execute the command.
     * 
     * @param cmd
     *            The command to execute
     * @return The execution result
     * @throws CmdExecutionException
     *             Any exception happened during the invocation will be wrapped
     *             in CmdExecutionException with both command information and
     *             the exception information.
     */
    public CmdExecutionResult execute(List<String> cmd)
            throws CmdExecutionException {
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

            Process process = pb.start();

            Future<String> future = executorService
                    .submit(new CmdExecutionStreamRecorder(process
                            .getInputStream()));

            Future<String> future2 = null;
            if (!redirectErrorStream) {
                future2 = executorService
                        .submit(new CmdExecutionStreamRecorder(process
                                .getErrorStream()));
            }

            result.exitValue = process.waitFor();

            result.log = future.get();

            if (!redirectErrorStream) {
                result.log2 = future2.get();
            } else {
                result.log2 = null;
            }

            if (result.exitValue != 0) {
                for (String c : cmd) {
                    System.out.print(c + " ");
                }
                System.out.println("");
            }

            return result;
        } catch (Exception e) {
            StringBuilder cmdSB = new StringBuilder();
            for (String str : cmd) {
                cmdSB.append(str).append(" ");
            }
            throw new CmdExecutionException(new String(cmdSB), Routine
                    .getExceptionLogInfo(e));
        }
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(File workingDirectoryP) {
        this.workingDirectory = workingDirectoryP;
    }

    public boolean isRedirectErrorStream() {
        return redirectErrorStream;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environmentP) {
        this.environment = environmentP;
    }

    public void setRedirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
    }

}
