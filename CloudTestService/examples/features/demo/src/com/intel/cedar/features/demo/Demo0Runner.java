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

public class Demo0Runner extends AbstractTaskRunner {
	private static final long serialVersionUID = 6167971645397052473L;

	@Override
	public List<ITaskItem> getTaskItems(Environment env) {
		List<ITaskItem> items = new ArrayList<ITaskItem>();
		for(int i = 0; i < 10; i++){
			SimpleTaskItem item = new SimpleTaskItem();
			item.setValue(Integer.toString(i));
			items.add(item);
		}
		return items;
	}

	@Override
	public ResultID run(ITaskItem ti, Writer output, Environment env) {
		try {
			output.write("in Demo0Runner\n");
			output.write(ti.getValue());
			DemoResult res = new DemoResult();
			res.setValue(Integer.parseInt(ti.getValue()) * 10);
			ti.setResult(res);
			env.setFeatureProperty("KEY", "This is a value", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResultID.Passed;
	}

}
