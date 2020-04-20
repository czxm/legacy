package com.intel.cedar.tasklet.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.intel.cedar.agent.runtime.CircularByteBuffer;
import com.intel.cedar.feature.Environment;
import com.intel.cedar.feature.TaskRunnerEnvironment;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.tasklet.AbstractTaskRunner;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ResultID;

public class PerfMonTaskRunner extends AbstractTaskRunner {
    private static final long serialVersionUID = -8865647574400789850L;
    private HashSet<String> javaPids = new HashSet<String>();

    @Override
    public ResultID run(ITaskItem ti, Writer output, Environment env) {
        if (ti instanceof PerfMonTaskItem) {
            try {
                final boolean isWindows = env.getOSName().contains("Windows");
                final PerfMonTaskItem item = (PerfMonTaskItem) ti;
                final String[] pids = new String[item.getProcesses().size()];
                int i = 0;
                for (Object p : item.getProcesses()) {
                    if (p instanceof Integer) {
                        if (isValidPid((Integer) p, isWindows)) {
                            pids[i] = p.toString();
                            if (isJavaProcess(pids[i])) {
                                javaPids.add(pids[i]);
                            }
                        } else {
                            pids[i] = null;
                        }
                    } else {
                        String name = (String) p;
                        if (name.indexOf(":") >= 0) { // java
                            pids[i] = getPidByJava(name.substring(name
                                    .indexOf(":") + 1));
                            if (pids[i] != null) {
                                javaPids.add(pids[i]);
                                javaPids.add(name);
                            }
                        } else {
                            pids[i] = getPidByName(name, isWindows);
                        }
                    }
                    i++;
                }

                StringBuilder sb = new StringBuilder();
                sb.append("Index,Total_%CPU,");
                for (Object p : item.getProcesses()) {
                    String name = p.toString();
                    sb.append(name);
                    sb.append("_%CPU,");
                    sb.append(name);
                    sb.append("_VIRT,");
                    sb.append(name);
                    sb.append("_RES,");
                    if (javaPids.contains(p.toString())) {
                        sb.append(name);
                        sb.append("_HEAP,");
                    }
                }
                output.write(sb.toString());
                output.write("\n");
                output.flush();

                if (item.getLogFile() == null || item.getLogFile().equals(""))
                    monitorPerformance(item.getInterval(), pids, output,
                            isWindows);
                else {
                    IFile log = env.getStorageRoot().getFile(item.getLogFile());
                    if (log.create()) {
                        final CircularByteBuffer buffer = new CircularByteBuffer();
                        new Thread() {
                            public void run() {
                                try {
                                    monitorPerformance(item.getInterval(),
                                            pids, new OutputStreamWriter(buffer
                                                    .getOutputStream()),
                                            isWindows);
                                } catch (Exception e) {
                                } finally {
                                    try {
                                        buffer.getInputStream().close();
                                    } catch (Exception e) {
                                    }
                                }
                            }
                        }.start();
                        log.setContents(buffer.getInputStream());
                    }
                }
                return ResultID.Passed;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ResultID.Failed;
    }

    protected boolean isValidPid(int pid, boolean isWindows) throws Exception {
        Process p = null;
        BufferedReader reader = null;
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.redirectErrorStream(true);
            if (isWindows) {
                pb.command("tasklist", "/FI", "\"PID eq " + pid + "\"");
            } else {
                pb.command("ps", "-p", Integer.toString(pid));
            }
            p = pb.start();
            reader = new BufferedReader(new InputStreamReader(p
                    .getInputStream()));
            int lines = 0;
            while (reader.readLine() != null) {
                lines++;
            }
            if (lines > 1) {
                return true;
            }
        } catch (Exception e) {
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (p != null) {
                p.destroy();
                p.waitFor();
            }
        }
        return false;
    }

    protected String getPidByName(String name, boolean isWindows)
            throws Exception {
        Process p = null;
        BufferedReader reader = null;
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.redirectErrorStream(true);
            if (isWindows) {
                pb.command("tasklist", "/FI", "\"IMAGENAME eq " + name + "\"");
            } else {
                pb.command("pidof", name);
            }
            p = pb.start();
            reader = new BufferedReader(new InputStreamReader(p
                    .getInputStream()));
            String line = "";
            if (isWindows) {
                while ((line = reader.readLine()) != null) {
                    String[] args = line.split(" +");
                    if (args.length > 0 && args[0].equals(name))
                        return args[1];
                }
            } else {
                line = reader.readLine();
                String[] args = line.split(" ");
                if (args.length > 0)
                    return args[0] != null && args[0].length() > 0 ? args[0]
                            : null;
            }
        } catch (Exception e) {
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (p != null) {
                p.destroy();
                p.waitFor();
            }
        }
        return null;
    }

    protected String getPidByJava(String name) throws Exception {
        Process p = null;
        BufferedReader reader = null;
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.redirectErrorStream(true);
            pb.command("jps");
            p = pb.start();
            reader = new BufferedReader(new InputStreamReader(p
                    .getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                String[] args = line.split(" ");
                if (args.length > 1 && args[1].equals(name)) {
                    return args[0];
                }
            }
        } catch (Exception e) {
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (p != null) {
                p.destroy();
                p.waitFor();
            }
        }
        return null;
    }

    protected boolean isJavaProcess(String pid) throws Exception {
        Process p = null;
        BufferedReader reader = null;
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.redirectErrorStream(true);
            pb.command("jps");
            p = pb.start();
            reader = new BufferedReader(new InputStreamReader(p
                    .getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                String[] args = line.split(" ");
                if (args.length > 0 && args[0].equals(pid)) {
                    return true;
                }
            }
        } catch (Exception e) {
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (p != null) {
                p.destroy();
                p.waitFor();
            }
        }
        return false;
    }

    protected float getHeapSize(String pid) throws Exception {
        Process p = null;
        BufferedReader reader = null;
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.redirectErrorStream(true);
            pb.command("jstat", "-gc", pid);
            p = pb.start();
            reader = new BufferedReader(new InputStreamReader(p
                    .getInputStream()));
            reader.readLine();// skip first line
            String line = reader.readLine();
            if (line != null && line.length() > 0) {
                String[] args = line.split(" +");
                return Float.parseFloat(args[5]) + Float.parseFloat(args[7])
                        + Float.parseFloat(args[9]);
            }
        } catch (Exception e) {
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (p != null) {
                p.destroy();
                p.waitFor();
            }
        }
        return 0;
    }

    protected List<String> getPerfMonCmd(int interval, List<String> pids,
            boolean isWindows) throws Exception {
        List<String> cmd = new ArrayList<String>();
        if (isWindows) {
            cmd.add("typeperf");
            cmd.add("-si");
            cmd.add(Integer.toString(interval));
            cmd.add("\"\\Processor(_Total)\\% Processor Time\"");
            if (pids.size() > 0) {
                ProcessBuilder pb = new ProcessBuilder();
                pb.redirectErrorStream(true);
                pb.command("typeperf", "-sc", "1",
                        "\"\\Process(*)\\ID Process\"");
                Process p = pb.start();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(p.getInputStream()));
                reader.readLine();
                String counterLine = reader.readLine();
                String valueLine = reader.readLine();
                reader.close();
                p.waitFor();
                String[] counters = counterLine.split(",");
                String[] values = valueLine.split(",");
                for (int i = 1; i < counters.length; i++) {
                    String c = values[i].replaceAll("\\.0+|\"", "");
                    for (String pid : pids) {
                        if (pid.equals(c)) {
                            String counter = counters[i];
                            String cpuCounter = counter.replace("ID Process",
                                    "% Processor Time");
                            String virtCounter = counter.replace("ID Process",
                                    "Virtual Bytes");
                            String resCounter = counter.replace("ID Process",
                                    "Working Set");
                            cmd.add(cpuCounter);
                            cmd.add(virtCounter);
                            cmd.add(resCounter);
                            break;
                        }
                    }
                }
            }
        } else {
            cmd.add("top");
            cmd.add("-d");
            cmd.add(Integer.toString(interval));
            cmd.add("-b");
            if (pids.size() > 0) {
                cmd.add("-p");
                StringBuilder sb = new StringBuilder();
                sb.append(pids.get(0));
                for (int i = 1; i < pids.size(); i++) {
                    sb.append(",");
                    sb.append(pids.get(i));
                }
                cmd.add(sb.toString());
            } else {
                // this is to reduce top's output, 1 actually is the init
                // process
                cmd.add("-p");
                cmd.add("1");
            }
        }
        return cmd;
    }

    protected int monitorPerformance(int interval, String[] pids,
            Writer writer, boolean isWindows) throws Exception {
        ArrayList<String> validPids = new ArrayList<String>();
        for (String pid : pids) {
            if (pid != null) {
                validPids.add(pid);
            }
        }
        int processors = Runtime.getRuntime().availableProcessors();
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        pb.command(getPerfMonCmd(interval, validPids, isWindows));
        Process p = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p
                .getInputStream()));
        String line = "";
        StringBuilder sb = new StringBuilder();
        int outputIndex = 0;
        while ((line = reader.readLine()) != null) {
            if (isWindows) {
                if (!line.startsWith("\"(") && line.length() > 1) {
                    if (outputIndex > 0) { // don't use the first result, as it
                                           // may be not accurate enough
                        sb.setLength(0);
                        String[] args = line.split(",");
                        int i = 1;
                        Float f = Float.parseFloat(args[i].replace("\"", ""));
                        sb.append(String.format("%d,%.2f", outputIndex, f));
                        i++;
                        for (String pid : pids) {
                            sb.append(",");
                            if (pid == null) {
                                sb.append("0,0,0");
                            } else {
                                f = Float.parseFloat(args[i].replace("\"", ""));
                                if (f < 0) { // process terminated
                                    sb.append("0,0,0");
                                } else {
                                    // %CPU
                                    sb.append(String.format("%.2f", f
                                            / processors));
                                    // Virtual Bytes
                                    f = Float.parseFloat(args[i + 1].replace(
                                            "\"", ""));
                                    sb.append(String.format(",%.2f",
                                            f / 1048576));
                                    // Working Set
                                    f = Float.parseFloat(args[i + 2].replace(
                                            "\"", ""));
                                    sb.append(String.format(",%.2f",
                                            f / 1048576));
                                }
                                if (javaPids.contains(pid)) {
                                    sb.append(String.format(",%.2f",
                                            getHeapSize(pid) / 1024));
                                }
                                i += 2;
                            }
                        }
                        writer.write(sb.toString());
                        writer.write("\n");
                        writer.flush();
                    }
                    outputIndex++;
                }
            } else {
                if (line.startsWith("Cpu(s)")) {
                    String[] args = line.split(", *");
                    if (args.length > 3 && args[3].endsWith("id")) {
                        String idle = args[3].replace("%id", "");
                        Float f = Float.parseFloat(idle);
                        sb.append(String
                                .format("%d,%.2f", outputIndex, 100 - f));
                    }
                } else if (sb.length() > 0) {
                    if (line.startsWith("top")) { // all processes are
                                                  // terminated
                        for (int x = 0; x < pids.length; x++) {
                            sb.append(",0,0,0");
                            if (pids[x] != null && javaPids.contains(pids[x])) {
                                sb.append(",0");
                            }
                        }
                        if (outputIndex > 0) { // don't use the first result, as
                                               // it may be not accurate enough
                            writer.write(sb.toString());
                            writer.write("\n");
                            writer.flush();
                        }
                        sb.setLength(0);
                        outputIndex++;
                    } else {
                        String[] s = line.trim().split(" +");
                        if (s.length >= 12 && !s[0].equals("PID")) {
                            // this may be buggy for different Linux version
                            // load all PID line till empty line encountered
                            HashMap<String, String> lines = new HashMap<String, String>();
                            String[] args = s;
                            lines.put(args[0], getTopNumbers(args, args[0],
                                    processors));
                            while (lines.size() < validPids.size()
                                    && (line = reader.readLine()) != null) {
                                if (line.length() <= 2) {
                                    break;
                                }
                                args = line.trim().split(" +");
                                lines.put(args[0], getTopNumbers(args, args[0],
                                        processors));
                            }
                            for (String pid : pids) {
                                sb.append(",");
                                if (pid == null || lines.get(pid) == null) {
                                    sb.append("0,0,0");
                                } else {
                                    sb.append(lines.get(pid));
                                }
                            }
                            if (outputIndex > 0) { // don't use the first
                                                   // result, as it may be not
                                                   // accurate enough
                                writer.write(sb.toString());
                                writer.write("\n");
                                writer.flush();
                            }
                            sb.setLength(0);
                            outputIndex++;
                        }
                    }
                }
            }
        }
        reader.close();
        p.destroy();
        return p.waitFor();
    }

    protected String getTopNumbers(String[] args, String pid, int processors)
            throws Exception {
        StringBuilder sb = new StringBuilder();
        if (args.length >= 12) {
            Float f = Float.parseFloat(args[8]); // %CPU
            sb.append(String.format("%.2f,", f / processors));
            if (args[4].contains("m")) { // VIRT
                sb.append(args[4].replace("m", "") + ",");
            } else if (args[4].contains("g")) {
                sb.append(String.format("%.2f,", Float.parseFloat(args[4]
                        .replace("g", "")) * 1024));
            } else {
                sb.append(String.format("%.2f,",
                        Float.parseFloat(args[4]) / 1048576));
            }
            if (args[5].contains("m")) { // RES
                sb.append(args[5].replace("m", ""));
            } else if (args[5].contains("g")) {
                sb.append(String.format("%.2f", Float.parseFloat(args[5]
                        .replace("g", "")) * 1024));
            } else {
                sb.append(String.format("%.2f",
                        Float.parseFloat(args[5]) / 1048576));
            }
        } else {
            sb.append("0,0,0");
        }
        if (javaPids.contains(pid)) {
            sb.append(String.format(",%.2f", getHeapSize(pid) / 1024));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        PerfMonTaskItem item = new PerfMonTaskItem();
        item.setInterval(2);
        for (String a : args) {
            if (a.startsWith("java:")) {
                item.addProcess(a.replace("java:", ""), true);
            } else if (a.matches("[0-9]+")) {
                item.addProcess(Integer.parseInt(a));
            } else {
                item.addProcess(a);
            }
        }
        new PerfMonTaskRunner().run(item, new OutputStreamWriter(System.out),
                TaskRunnerEnvironment.createInstance(PerfMonTaskRunner.class
                        .getClassLoader(), null));
    }
}
