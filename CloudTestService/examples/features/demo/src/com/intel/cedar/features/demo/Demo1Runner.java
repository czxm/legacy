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
import com.intel.cedar.tasklet.SimpleTaskItem;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ResultID;

public class Demo1Runner extends AbstractTaskRunner {
	private static final long serialVersionUID = 6167971645397052473L;

	@Override
	public List<ITaskItem> getTaskItems(Environment env) {
		List<ITaskItem> items = new ArrayList<ITaskItem>();
		items.add(new SimpleTaskItem());
		return items;
	}

	@Override
	public ResultID run(ITaskItem ti, Writer output, Environment env) {
		try {
			output.write("in Demo1Runner\n");
			Variable var = null;
			try{
				var = env.getVariable("invalid_var");
			}
			catch(Exception e1){
				e1.printStackTrace();
			}
			var = env.getVariable("test_var");
			output.write("test_var's value is:");
			output.write(var.getValue());
			output.write("\n");
			var.setValue("this is a totaly new value");
			env.setVariable(var);
			
			ByteArrayInputStream stream = new ByteArrayInputStream("This is a test file".getBytes());
			IFolder root = env.getStorageRoot();
			IFile file = root.getFile("test.txt");
			file.create();
			file.setContents(stream);
			
			String value = env.getFeatureProperty("KEY", null);
			output.write("KEY's value is: " + value + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResultID.Passed;
	}

}
