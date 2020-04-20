package com.intel.bigdata.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.TimerTask;

/**
 * This service class provides wrapped methods for shell commands.
 */
public class Command {

    protected static Logger LOG = LoggerFactory.getLogger(Command.class);

    public static int execute(String command) throws Exception {
        LOG.debug(command);
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
        return process.exitValue();
    }

    public static String executeWithOutput(String command) throws Exception {
        LOG.debug(command);
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        try {
            process.waitFor();
            StringBuffer output = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                output.append(line);
            }
            return output.toString();
        }
        finally {
            br.close();
        }
    }

    public static int executeWithOutput(List<String> resultOutput, int timeout, String ... command) throws IOException {
        int val = 255;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);

            final Process p = createProcess(pb);
            if (timeout > 0) {
                TimerTask tt = new TimerTask() {
                    public void run() {
                        try {
                            p.destroy();
                        } catch (Exception e) {
                            LOG.error("Fail to destroy timed command.", e);
                        }
                    }
                };
                TimerFactory.getInstance().schedule(tt, timeout);
            }

            val = handleProcess(resultOutput, p);
        } catch (IOException e1) {
            LOG.error("Fail to execute command", e1);
            throw e1;
        }
        return val;
    }
    public static Process createProcess(ProcessBuilder pb) throws IOException {
        pb = pb.redirectErrorStream(true);
        Process p = null;
        int retry = 0;
        IOException x = null;

        while(retry < 5)
            try {
                p = pb.start();
                if(p!=null)
                    return p;
            } catch (IOException e) {
                x = e;
                retry++;
                //due to some stupid issue in Linux kernel, it is possible to get "java.io.Exception: error=26, Text file busy"
                //see https://issues.apache.org/jira/browse/MAPREDUCE-2374 for discussion
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e1) {
                }
            }
        throw x;
    }

    private static int handleProcess(List<String> resultOutput, Process p) throws IOException {
        int val = 255;
        BufferedReader bos = null;
        try {
            bos = new BufferedReader(new InputStreamReader(p.getInputStream()));
            readOutput(resultOutput, bos);
            p.waitFor();
            val = p.exitValue();

            if (val != 0 && resultOutput!=null) {
                for(String line : resultOutput)
                    System.out.println(line);
            }
        } catch (InterruptedException e1) {
            val = 255;
            LOG.error("Fail to execute command.", e1);
        }finally {
            if(bos!=null)
                try {
                    bos.close();
                } catch (IOException e) {
                    LOG.warn("Fail to close output stream", e);
                }
        }
        return val;
    }
    private static void readOutput(List<String> output, BufferedReader bf) {
        final long SLEEP_TIME = 50;
        String line;
        try {
            if (!bf.ready()) {
                Thread.sleep(SLEEP_TIME);
            }
            line = bf.readLine();
            while (line != null) {
                if(!line.isEmpty()) {
                    if(output!=null) {
                        synchronized(output){
                            output.add(line);
                        }
                    }
                }
                if (!bf.ready()) {
                    Thread.sleep(SLEEP_TIME);
                }
                line = bf.readLine();
            }
        }
        catch (IOException ex) {
            LOG.warn("Fail to read output stream", ex);
        }
        catch (InterruptedException ex) {
            LOG.warn("Fail to read output stream", ex);
        } finally {
            try {
                bf.close();
            } catch (IOException e) {
                LOG.warn("Fail to close output stream", e);
            }
        }
    }
}
