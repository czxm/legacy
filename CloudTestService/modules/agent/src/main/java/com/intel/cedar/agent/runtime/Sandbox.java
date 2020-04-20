package com.intel.cedar.agent.runtime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;

import com.intel.cedar.feature.TaskRunnerEnvironment;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ITaskRunner;
import com.intel.cedar.tasklet.ResultID;
import com.intel.cedar.tasklet.impl.OnFinishTaskItem;
import com.intel.cedar.tasklet.impl.OnStartTaskItem;
import com.intel.cedar.util.protocal.ConversionProtocal;

public class Sandbox {
    public static void main(String[] args) {
        ConversionProtocal conversionProtocal = new ConversionProtocal();
        String classOfTasklet = args[0];
        String classOfTaskItem = args[1];
        String itemContentLength = args[2];
        String agentID = "invalid";
        boolean isPersist = false;
        String storageRoot = null;
        if (args.length >= 4) {
            agentID = args[3];
            if (args.length >= 5) {
                isPersist = Boolean.parseBoolean(args[4]);
                if (args.length >= 6) {
                    storageRoot = args[5];
                }
            }
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            int length = Integer.parseInt(itemContentLength);
            byte[] buf = new byte[length];
            System.in.read(buf);
            output.write(buf);
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        String itemContent = output.toString();
        ITaskRunner runner = null;
        try {
            Class<?> clz = Class.forName(classOfTasklet);
            TaskRunnerEnvironment.createInstance(clz.getClassLoader(),
                    storageRoot);
            File persistFile = new File(".cedar.persist." + agentID);
            if (isPersist && persistFile.exists()) {
                FileInputStream fis = new FileInputStream(persistFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Object o = ois.readObject();
                if (o instanceof ITaskRunner) {
                    runner = (ITaskRunner) o;
                }
                ois.close();
            } else {
                runner = (ITaskRunner) clz.newInstance();
            }

            if (runner != null) {
                Class.forName(classOfTaskItem);
                ITaskItem item = (ITaskItem) conversionProtocal
                        .generateTaskItem(itemContent);
                Writer writer = new OutputStreamWriter(System.out);
                ResultID resultId = ResultID.NotAvailable;
                if (item instanceof OnStartTaskItem) {
                    runner.onStart(TaskRunnerEnvironment.getInstance());
                } else if (item instanceof OnFinishTaskItem) {
                    runner.onFinish(TaskRunnerEnvironment.getInstance());
                } else {
                    resultId = runner.run(item, writer, TaskRunnerEnvironment
                            .getInstance());
                }

                writer.flush();
                System.err.println();
                System.err.println("@@CedarResult");
                System.err.println(resultId.name());
                System.err.println("CedarResult@@");
                System.err.flush();

                if (isPersist && runner instanceof Serializable) {
                    FileOutputStream fos = new FileOutputStream(persistFile);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(runner);
                    oos.close();
                }
            } else {
                throw new RuntimeException(
                        "TaskRunner is not successfully instantiated!");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            if (runner != null) {
                runner.onError(e, TaskRunnerEnvironment.getInstance());
            }
        }
    }
}
