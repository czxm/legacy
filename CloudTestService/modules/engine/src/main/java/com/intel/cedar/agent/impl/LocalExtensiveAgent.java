package com.intel.cedar.agent.impl;

import java.io.OutputStreamWriter;

import com.intel.cedar.agent.IExtensiveAgent;
import com.intel.cedar.agent.runtime.ServerRuntimeInfo;
import com.intel.cedar.engine.impl.FeaturePropsManager;
import com.intel.cedar.engine.impl.VariableManager;
import com.intel.cedar.feature.LocalEnvironment;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.storage.impl.LocalFolder;
import com.intel.cedar.tasklet.IResult;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ITaskRunner;
import com.intel.cedar.tasklet.ResultID;
import com.intel.cedar.tasklet.impl.OnFinishTaskItem;
import com.intel.cedar.tasklet.impl.OnStartTaskItem;
import com.intel.cedar.tasklet.impl.Result;

public class LocalExtensiveAgent extends AbstractAgent implements
        IExtensiveAgent {
    private LocalFolder theStorage;
    private VariableManager vars;

    public LocalExtensiveAgent() {
        super("localhost", "N/A");
    }

    @Override
    public void addPostParam(ITaskRunner runner, String key, String value) {
        throw new RuntimeException("NOT SUPPORTED!");
    }

    @Override
    public void installFeatures(ITaskRunner runner, String[] features) {
        throw new RuntimeException("NOT SUPPORTED!");
    }

    @Override
    public void setStorageRoot(ITaskRunner runner, IFolder storage) {
        if (storage instanceof LocalFolder) {
            theStorage = (LocalFolder) storage;
        } else {
            throw new RuntimeException("NOT SUPPORTED!");
        }
    }

    @Override
    public void setVariableManager(VariableManager variables) {
        this.vars = variables;
    }

    @Override
    public ServerRuntimeInfo getServerInfo() {
        return ServerRuntimeInfo.getInstance();
    }

    @Override
    public TaskRunnerStatus getStatus(ITaskRunner runner) {
        return this.getRunningId(runner) != null ? TaskRunnerStatus.Started
                : TaskRunnerStatus.NotAvailable;
    }

    @Override
    public void kill(ITaskRunner runner) {
        // local agent actually doesn't support killing
    }

    @Override
    public IResult run(ITaskRunner runner, ITaskItem taskItem, String timeout,
            String cwd) {
        Result result = new Result(ResultID.NotAvailable);
        try {
            if (taskItem instanceof OnStartTaskItem) {
                runner.onStart(new LocalEnvironment(vars, theStorage));
            } else if (taskItem instanceof OnFinishTaskItem) {
                runner.onFinish(new LocalEnvironment(vars, theStorage));
            } else {
                OutputStreamWriter writer = new OutputStreamWriter(this
                        .getOutputStream(runner));
                ResultID resultID = runner.run(taskItem, writer,
                        new LocalEnvironment(vars, theStorage));
                writer.flush();
                if (resultID != null)
                    result = new Result(resultID);
                else
                    throw new RuntimeException("null returned from "
                            + runner.getClass().getName());
            }
        } catch (Throwable t) {
            result = new Result(ResultID.Failed);
            result.setFailureMessage(t.getMessage());
            runner.onError(t, new LocalEnvironment(vars, theStorage));
            return result;
        } finally {
            taskItem.setResult(result);
        }
        return result;
    }

    @Override
    public void setPropertiesManager(FeaturePropsManager props) {
    }
}
