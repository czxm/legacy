package com.intel.cedar.tasklet.impl;

import java.io.Writer;

import com.intel.cedar.feature.Environment;
import com.intel.cedar.feature.TaskRunnerEnvironment;
import com.intel.cedar.tasklet.AbstractTaskRunner;
import com.intel.cedar.tasklet.IProgressProvider;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ResultID;

public class CommandTaskRunner extends AbstractTaskRunner implements
        IProgressProvider {
    private static final long serialVersionUID = -1219328720616911967L;

    public ResultID run(ITaskItem ti, Writer output, Environment env) {
        String cmdline = ((CommandTaskItem) ti).getCommandLine();
        int ret = 0;
        try {
            ret = TaskRunnerEnvironment.getInstance().execute(cmdline, output);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret == 0 ? ResultID.Passed : ResultID.Failed;
    }
}
