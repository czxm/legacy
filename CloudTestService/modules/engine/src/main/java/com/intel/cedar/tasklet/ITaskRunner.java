package com.intel.cedar.tasklet;

import java.io.Writer;

import com.intel.cedar.feature.Environment;

public interface ITaskRunner {
    public ResultID run(ITaskItem ti, Writer output, Environment env);

    public void onStart(Environment env);

    public void onFinish(Environment env);

    public void onError(Throwable e, Environment env);
}
