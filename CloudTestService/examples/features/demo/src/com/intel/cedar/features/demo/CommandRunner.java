package com.intel.cedar.features.demo;

import java.io.ByteArrayInputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.feature.Environment;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.tasklet.AbstractTaskRunner;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ResultID;
import com.intel.cedar.tasklet.SimpleTaskItem;

public class CommandRunner extends AbstractTaskRunner {
	private static final long serialVersionUID = 6167971645397052473L;

	@Override
	public List<ITaskItem> getTaskItems(Environment env) {
		int count = 0;
		String command = "N/A";
		try{
			count = Integer.parseInt(env.getVariable("machine_count").getValue());	
			command = env.getVariable("command").getValue();
		}
		catch(Exception e){			
		}
		
		List<ITaskItem> items = new ArrayList<ITaskItem>();
		for(int i = 0; i < count; i++){
			SimpleTaskItem item = new SimpleTaskItem();
			item.setValue(command);
			items.add(item);
		}
		return items;
	}

	@Override
	public ResultID run(ITaskItem ti, Writer output, Environment env) {
		try {
			output.write("in CommandRunner\n");
			int ret = env.execute(ti.getValue(), output);
			if(ret == 0)
				return ResultID.Passed;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResultID.Failed;
	}

}
