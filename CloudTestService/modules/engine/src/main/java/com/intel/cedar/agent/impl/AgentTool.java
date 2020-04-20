package com.intel.cedar.agent.impl;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;

import com.intel.cedar.agent.runtime.ServerRuntimeInfo;
import com.intel.cedar.engine.impl.VariableManager;
import com.intel.cedar.feature.impl.FeatureManifest;
import com.intel.cedar.feature.util.JarUtility;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.storage.impl.CedarStorage;
import com.intel.cedar.tasklet.IResult;
import com.intel.cedar.tasklet.ITaskRunner;
import com.intel.cedar.tasklet.impl.CommandTaskItem;
import com.intel.cedar.tasklet.impl.CommandTaskRunner;
import com.intel.cedar.tasklet.impl.GenericTaskItem;
import com.intel.cedar.tasklet.impl.PerfMonTaskItem;
import com.intel.cedar.tasklet.impl.PerfMonTaskRunner;
import com.intel.xml.rss.util.DateTimeRoutine;

public class AgentTool {

    public static void printHelp() {
        System.out.println("usage:");
        System.out.println("    AgentTool -h");
        System.out.println("  or");
        System.out
                .println("    AgentTool server [--kill job_name] [--list] [--status]");
        System.out.println("  or");
        System.out
                .println("    AgentTool server -monitor PID,java:MAIN [seconds]");
        System.out.println("  or");
        System.out
                .println("    AgentTool server [--queue] [--workingdir dir] [--timeout seconds] [--output file] command");
        System.out.println("  or");
        System.out
                .println("    AgentTool server [--queue] [--workingdir dir] [--timeout seconds] [--output file] [--features files] [--taskitem name=value] [--variables name=v1,v2] [--clear true|false] [Tasklet]");
        System.out.println("");
        System.out.println("options:");
        System.out
                .println("  -m, --monitor                 this is a performance monitor job");
        System.out
                .println("  -q, --queue                   this job should be queued");
        System.out
                .println("  -k, --kill job_name           kill a job by name");
        System.out
                .println("  -l, --list                    list all the running jobs");
        System.out
                .println("  -s, --status                  query the service status");
        System.out
                .println("  -d, --workingdir dir          specify the working directory of");
        System.out
                .println("                                  the created job.");
        System.out
                .println("  -t, --timeout seconds         specify the time out value for");
        System.out.println("                                  the created job");
        System.out
                .println("  -o, --output file             save the output to file");
        System.out
                .println("  -f, --features files          the feature applications seperated by ';'");
        System.out
                .println("  -i, --taskitem name=value     attributes for the taskitem seperated by ';'");
        System.out
                .println("  -c, --clear true|false        whether to clear features specified");
        System.out
                .println("                                  by --features, default is true");
        System.out
                .println("  -v, --variables name=v1,v2    variables seperated by ';'");
        System.out
                .println("  -h, --help                    print this help message");
    }

    public static void main(String[] args) throws Exception {
        System.err.println("Agent Command line tool for Cloud Test Services");
        System.err.println("");

        if (args.length < 1) {
            System.err
                    .println("Missing required arguments. Using '-h' or '--help' to get the help.");
            System.exit(10);
        } else if (args[0].equals("-h") || args[0].equals("--help")) {
            printHelp();
            System.exit(10);
        } else if (args.length < 2) {
            System.err
                    .println("Missing required arguments. Using '-h' or '--help' to get the help.");
            System.exit(10);
        }

        String host = args[0];
        String port = "10614";
        int i = 1;
        boolean queuedJob = false;
        boolean doListTasks = false;
        boolean showStatus = false;
        boolean optionEnd = false;
        boolean clearFeatures = true;
        String monitorTarget = null;
        int interval = 2;
        String features = null;
        String taskitem = "";
        LinkedList<Variable> vars = new LinkedList<Variable>();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("timeout", "0");
        params.put("cwd", "");
        LinkedList<String> cl = new LinkedList<String>();
        while (i < args.length) {
            if (!optionEnd && "-t".equals(args[i])
                    || "--timeout".equalsIgnoreCase(args[i])) {
                if (args.length < i + 2) {
                    System.err.println("Missing arguments for --timeout.");
                    System.exit(13);
                }
                params.put("timeout", args[i + 1]);
                i += 2;
            } else if (!optionEnd && "-d".equals(args[i])
                    || "--workingdir".equalsIgnoreCase(args[i])) {
                if (args.length < i + 2) {
                    System.err.println("Missing arguments for --workingdir.");
                    System.exit(14);
                }
                params.put("cwd", args[i + 1]);
                i += 2;
            } else if (!optionEnd && "-l".equals(args[i])
                    || "--list".equalsIgnoreCase(args[i])) {
                i += 1;
                doListTasks = true;
            } else if (!optionEnd && "-s".equals(args[i])
                    || "--status".equalsIgnoreCase(args[i])) {
                i += 1;
                showStatus = true;
            } else if (!optionEnd && "-k".equals(args[i])
                    || "--kill".equalsIgnoreCase(args[i])) {
                if (args.length < i + 2) {
                    System.err.println("Missing arguments for --kill.");
                    System.exit(14);
                }
                params.put("kill", args[i + 1]);
                i += 2;
            } else if (!optionEnd && "-m".equals(args[i])
                    || "--monitor".equalsIgnoreCase(args[i])) {
                if (args.length < i + 2) {
                    System.err.println("Missing arguments for --monitor.");
                    System.exit(14);
                }
                monitorTarget = args[i + 1];
                if (args.length > i + 2) {
                    interval = Integer.parseInt(args[i + 2]);
                    i += 3;
                } else {
                    i += 2;
                }
            } else if (!optionEnd && "-o".equals(args[i])
                    || "--output".equalsIgnoreCase(args[i])) {
                if (args.length < i + 2) {
                    System.err.println("Missing arguments for --output.");
                    System.exit(14);
                }
                params.put("output", args[i + 1]);
                i += 2;
            } else if (!optionEnd && "-q".equals(args[i])
                    || "--queue".equalsIgnoreCase(args[i])) {
                i += 1;
                queuedJob = true;
            } else if (!optionEnd && "-c".equals(args[i])
                    || "--clear".equalsIgnoreCase(args[i])) {
                if (args.length < i + 2) {
                    System.err.println("Missing arguments for --clear.");
                    System.exit(14);
                }
                clearFeatures = Boolean.parseBoolean(args[i + 1]);
                i += 2;
            } else if (!optionEnd && "-f".equals(args[i])
                    || "--features".equalsIgnoreCase(args[i])) {
                if (args.length < i + 2) {
                    System.err.println("Missing arguments for --features.");
                    System.exit(14);
                }
                features = args[i + 1];
                i += 2;
            } else if (!optionEnd && "-i".equals(args[i])
                    || "--taskitem".equalsIgnoreCase(args[i])) {
                if (args.length < i + 2) {
                    System.err.println("Missing arguments for --taskitem.");
                    System.exit(14);
                }
                taskitem = args[i + 1];
                i += 2;
            } else if (!optionEnd && "-v".equals(args[i])
                    || "--variables".equalsIgnoreCase(args[i])) {
                if (args.length < i + 2) {
                    System.err.println("Missing arguments for --variables.");
                    System.exit(14);
                }
                for (String v : args[i + 1].split(";")) {
                    int index = v.indexOf("=");
                    if (index > 0 && index < v.length()) {
                        String name = v.substring(0, index);
                        String value = v.substring(index + 1);
                        Variable var = new Variable();
                        var.setName(name);
                        for (String vp : value.split(",")) {
                            var.addValue(vp);
                        }
                        vars.add(var);
                    }
                }
                i += 2;
            } else {
                optionEnd = true;
                cl.addLast(args[i]);
                ++i;
            }
        }

        String http_proxy = System.getenv("http_proxy");
        String proxyHost = null;
        String proxyPort = null;
        if (http_proxy != null && !http_proxy.equals("")) {
            URL url = new URL(http_proxy);
            proxyHost = url.getHost();
            proxyPort = new Integer(url.getPort()).toString();
        }
        if (host.contains(":")) {
            String[] s = host.split(":");
            host = s[0].trim();
            port = s[1].trim();
        }
        final ExtensiveAgent agent = new ExtensiveAgent(host, port);
        if (proxyHost != null && proxyPort != null) {
            System.out.println("Using " + proxyHost + ":" + proxyPort);
            agent.setProxy(proxyHost, proxyPort, "", "");
        }

        if (doListTasks) {
            agent.listTaskRunners();
        } else if (showStatus) {
            ServerRuntimeInfo info = agent.getServerInfo();
            if (info != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("              Version: ").append(info.getVersion())
                        .append(" (").append(info.getDigest()).append(")\n");
                sb.append("       Server started: ").append(
                        DateTimeRoutine.millisToStdTimeString(info
                                .getStartupTime())).append("\n");
                sb.append("           Server age: ").append(
                        DateTimeRoutine.millisToDuration(info.getDuration()))
                        .append("\n");
                sb.append("  Connection accepted: ").append(
                        info.getAcceptedConnections()).append("\n");
                sb.append("         Job executed: ").append(
                        info.getExecutedJobs()).append("\n");
                HashMap<String, Long> taskMap = info.getExecutedTasks();
                HashMap<String, Long> timeMap = info.getExecutedTime();
                boolean first = true;
                for (String key : taskMap.keySet()) {
                    if (first) {
                        sb.append("    Executed Tasklets: ").append(key)
                                .append(" ").append(taskMap.get(key)).append(
                                        " (~avg ").append(
                                        timeMap.get(key) / taskMap.get(key)
                                                / 1000).append("s)\n");
                        first = false;
                    } else
                        sb.append("                       ").append(key)
                                .append(" ").append(taskMap.get(key)).append(
                                        " (~avg ").append(
                                        timeMap.get(key) / taskMap.get(key)
                                                / 1000).append("s)\n");
                    ;
                }
                System.out.println(sb.toString());
            } else {
                agent.listTaskRunners();
            }
        } else if (params.get("kill") != null) {
            agent.kill(params.get("kill"));
        } else if (features == null) {
            if (monitorTarget != null) {
                PerfMonTaskItem item = new PerfMonTaskItem();
                item.setInterval(interval);
                for (String a : monitorTarget.split(",")) {
                    if (a.startsWith("java:")) {
                        item.addProcess(a.replace("java:", ""), true);
                    } else if (a.matches("[0-9]+")) {
                        item.addProcess(Integer.parseInt(a));
                    } else {
                        item.addProcess(a);
                    }
                }
                OutputStream output = null;
                if (params.get("output") != null) {
                    output = new FileOutputStream(params.get("output"));
                } else {
                    output = System.out;
                }
                final PerfMonTaskRunner runner = new PerfMonTaskRunner();
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        agent.kill(runner);
                    }
                });
                agent.setOutputStream(runner, output);
                if (queuedJob) {
                    agent.addPostParam(runner, "queued", "true");
                }
                IResult result = agent.run(runner, item, params.get("timeout"),
                        params.get("cwd"));
                System.out.println(result.getID().name());
                System.out.println(result.getFailureMessage());
                if (params.get("output") != null)
                    output.close();
            } else if (cl.size() == 0) {
                System.err.println("Missing command");
                System.exit(14);
            }
            CommandTaskItem item = new CommandTaskItem();
            StringBuilder sb = new StringBuilder();
            for (String s : cl) {
                sb.append(s);
                sb.append(" ");
            }
            item.setCommandLine(sb.toString());

            OutputStream output = null;
            if (params.get("output") != null) {
                output = new FileOutputStream(params.get("output"));
            } else {
                output = System.out;
            }
            final CommandTaskRunner runner = new CommandTaskRunner();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    agent.kill(runner);
                }
            });
            agent.setOutputStream(runner, output);
            if (queuedJob) {
                agent.addPostParam(runner, "queued", "true");
            }
            IResult result = agent.run(runner, item, params.get("timeout"),
                    params.get("cwd"));
            System.out.println(result.getID().name());
            System.out.println(result.getFailureMessage());
            if (params.get("output") != null)
                output.close();
        } else {
            if (cl.size() == 0 && clearFeatures == false) {
                System.err.println("Missing tasklet");
                System.exit(14);
            }

            StringBuilder featureIds = new StringBuilder();
            if (!features.equals("cedar")) {
                for (String feature : features.split(";")) {
                    agent.uploadFeature(feature);
                    featureIds.append(new FeatureManifest(feature)
                            .getFeatureId());
                    featureIds.append(" ");
                }
                featureIds.deleteCharAt(featureIds.length() - 1);
            }

            if (cl.size() == 0 && clearFeatures && featureIds.length() > 0) {
                if (agent.removeFeatures(featureIds.toString().split(" "))) {
                    System.out.println(features.replace(";", " ")
                            + " are successfully cleared");
                } else {
                    System.err.println("Failed to clear "
                            + features.replace(";", " "));
                }
                System.exit(0);
            }

            agent.setVariableManager(new VariableManager(vars));
            IFolder storage = (IFolder) new CedarStorage(System
                    .getProperty("user.dir")).getRoot();
            OutputStream output = null;
            if (params.get("output") != null) {
                output = new FileOutputStream(params.get("output"));
            } else {
                output = System.out;
            }
            ClassLoader clzLdr = JarUtility.loadJar(features.split(";"));
            for (String tasklet : cl) {
                try {
                    final ITaskRunner runner = (ITaskRunner) Class.forName(
                            tasklet, true, clzLdr).newInstance();
                    if (featureIds.length() > 0)
                        agent.addPostParam(runner, "features", featureIds
                                .toString());
                    agent.setStorageRoot(runner, storage);
                    GenericTaskItem item = new GenericTaskItem();
                    for (String p : taskitem.split(";")) {
                        int index = p.indexOf("=");
                        if (index > 0 && index < p.length()) {
                            String name = p.substring(0, index);
                            String value = p.substring(index + 1);
                            item.setProperty(name, value);
                        }
                    }

                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        public void run() {
                            agent.kill(runner);
                        }
                    });
                    agent.setOutputStream(runner, output);
                    if (queuedJob) {
                        agent.addPostParam(runner, "queued", "true");
                    }
                    IResult result = agent.run(runner, item, params
                            .get("timeout"), params.get("cwd"));
                    System.out.println(result.getID().name());
                    System.out.println(result.getFailureMessage());
                } catch (Throwable t) {
                    System.err.println("Failed to execute tasklet " + tasklet
                            + " : " + t.getClass().getCanonicalName() + "("
                            + t.getMessage() + ")");
                }
            }
            if (params.get("output") != null)
                output.close();

            if (clearFeatures && featureIds.length() > 0) {
                if (agent.removeFeatures(featureIds.toString().split(" "))) {
                    System.out.println(features.replace(";", " ")
                            + " are successfully cleared");
                } else {
                    System.err.println("Failed to clear "
                            + features.replace(";", " "));
                }
            }
            if (agent != null)
                agent.finalize();
        }
    }
}
