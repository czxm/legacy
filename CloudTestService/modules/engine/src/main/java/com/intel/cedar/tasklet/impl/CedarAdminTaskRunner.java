package com.intel.cedar.tasklet.impl;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.feature.Environment;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ResultID;

public class CedarAdminTaskRunner extends CommandTaskRunner {
    private static final long serialVersionUID = -4840455024565431108L;

    public ResultID run(ITaskItem ti, Writer output, Environment env) {
        String cmdline = ti.getValue();
        String script = ((CedarAdminTaskItem) ti).getProperty("script", null);
        int ret = 0;
        try {
            List<String> commands = new ArrayList<String>();
            if (script != null) {
                env.extractResource("cedar-tools/" + script);
                if (!env.getOSName().contains("Windows")) {
                    commands.add("export PATH=.:$PATH");
                    commands.add("chmod +x " + script);
                }
            }
            commands.add(cmdline);
            ret = env.execute(commands.toArray(new String[] {}), output);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret == 0 ? ResultID.Passed : ResultID.Failed;
    }
}
