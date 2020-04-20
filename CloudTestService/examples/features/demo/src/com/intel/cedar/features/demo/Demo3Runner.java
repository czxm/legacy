package com.intel.cedar.features.demo;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.feature.Environment;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.tasklet.AbstractTaskRunner;
import com.intel.cedar.tasklet.SimpleTaskItem;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ResultID;

public class Demo3Runner extends AbstractTaskRunner {
	private static final long serialVersionUID = 1026928865806738284L;

	@Override
	public List<ITaskItem> getTaskItems(Environment env) {
		List<ITaskItem> items = new ArrayList<ITaskItem>();
		items.add(new SimpleTaskItem());
		return items;
	}
	
	@Override
	public ResultID run(ITaskItem ti, Writer output, Environment env) {
		try {
			output.write("in Demo3Runner\n");
			while(true){
				Variable var = env.getVariable("quit");
				output.write("quit's value is:");
				output.write(var.getValue());
				output.write("\n");
				if(var.getValue().equals("true"))
					break;
				Thread.sleep(10000);
			}
			output.write("quit Demo3Runner\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResultID.Passed;
	}

}
