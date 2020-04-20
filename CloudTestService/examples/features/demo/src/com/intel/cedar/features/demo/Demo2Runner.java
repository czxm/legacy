package com.intel.cedar.features.demo;

import java.io.InputStreamReader;
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

public class Demo2Runner extends AbstractTaskRunner {
	private static final long serialVersionUID = -1995844259453104480L;

	@Override
	public List<ITaskItem> getTaskItems(Environment env) {
		List<ITaskItem> items = new ArrayList<ITaskItem>();
		items.add(new SimpleTaskItem());
		return items;
	}
	
	@Override
	public ResultID run(ITaskItem ti, Writer output, Environment env) {
		try {
			output.write("in Demo2Runner\n");
			Variable var = env.getVariable("test_var");
			output.write("test_var's value is:");
			output.write(var.getValue());
			output.write("\n");
			IFolder root = env.getStorageRoot();
			IFile file = root.getFile("test.txt");
			output.write("test.txt " + (file.exist() ? "exists" : "not exists"));
			output.write("\n");
			output.write("test.txt's content:");
			InputStreamReader reader = new InputStreamReader(file.getContents());
			char[] buf = new char[2048];
			int n = -1;
			while((n = reader.read(buf)) != -1){
				output.write(buf, 0, n);
			}
			reader.close();
			Thread.sleep(60000);
			var = env.getVariable("quit");
			var.setValue("true");
			env.setVariable(var);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResultID.Passed;
	}

}
