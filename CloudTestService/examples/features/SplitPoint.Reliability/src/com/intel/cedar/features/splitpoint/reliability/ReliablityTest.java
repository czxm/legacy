package com.intel.cedar.features.splitpoint.reliability;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
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

public class ReliablityTest extends AbstractTaskRunner{
	@Override
	public ResultID run(ITaskItem ti, Writer output, Environment env) {
		try{
			boolean isMonitorClient = false;
			if(ti.getValue() != null && ti.getValue().equals("MonitorClient")){
				isMonitorClient = true;
			}
			
			String svnCMD1 = "svn export --non-interactive --username=lab_xmldev --password=\"qnn8S*NP\" http://sh-svn.sh.intel.com/ssg_repos/svn_xtt/xtt/FederateTest/PerfStress/Framework/testEngine.jar";
			String svnCMD2 = "svn export --non-interactive --username=lab_xmldev --password=\"qnn8S*NP\" http://sh-svn.sh.intel.com/ssg_repos/svn_xtt/xtt/FederateTest/PerfStress/Framework/testCase.jar";
			
			output.write(svnCMD1 + "\n");
			output.flush();
			env.execute(svnCMD1);
			
			output.write(svnCMD2 + "\n");
			output.flush();
			env.execute(svnCMD2);
			
			String engine = "com.intel.splat.perftest.frame." + env.getVariable("engine").getValue();
			String spServer = env.getVariable("spServer").getValue();
			String dummyServer = env.getVariable("dummyServer").getValue();
			String threads = env.getVariable("thread_count").getValue();
			String genCpuMem = "";
			if(isMonitorClient){
				threads = "1";
				genCpuMem = "-cpumem";
			}
			String loop = env.getVariable("loop_count").getValue();
			String perf_loop = env.getVariable("perfloop_count").getValue();
			String interval = env.getVariable("interval").getValue();
			String driver = "com.intel.splat.test." + env.getVariable("driver").getValue();
			String clspath = "testEngine.jar" + java.io.File.pathSeparator +"testCase.jar";
			String cmdline = String.format("java -cp %s %s -server %s -dummyserver %s -thread %s -loop %s -interval %s -perfLoop %s -out ./ %s -driver %s", 
                    clspath, engine, spServer, dummyServer, threads, loop, interval, perf_loop, genCpuMem, driver);
			List<String> commands = new ArrayList<String>();
			if(!env.getOSName().contains("Windows")){
				commands.add(". /root/.bashrc");
				commands.add("ulimit -n 16535");
			}
			commands.add(cmdline);
			
			for(String cmd : commands){
				output.write(cmd + "\n");
			}
			output.flush();
			
			env.execute(commands.toArray(new String[]{}));
			
			if(isMonitorClient){
				// upload all generated files
				IFolder root = env.getStorageRoot();
				for(File png : new File(env.getCWD()).listFiles(new FilenameFilter(){
					@Override
					public boolean accept(File dir, String name) {
						if(name.endsWith("png"))
							return true;
						else
							return false;
					}
				})){
					IFile pngFile = root.getFile(png.getName());
					FileInputStream stream = new FileInputStream(png);
					pngFile.setContents(stream);
					stream.close();
					png.delete();
				}
			}
		}
		catch(Exception e){
		}
		return ResultID.Passed;
	}
	
	@Override
	public List<ITaskItem> getTaskItems(Environment env) {
		List<ITaskItem> results = new ArrayList<ITaskItem>();
		try{
			Variable clientCounts = env.getVariable("client_count");
			for(int i = 0; i < Integer.parseInt(clientCounts.getValue()); i++){
				SimpleTaskItem item = new SimpleTaskItem();
				results.add(item);
			}
			SimpleTaskItem item = new SimpleTaskItem();
			item.setValue("MonitorClient");
			results.add(item);
		}
		catch(Exception e){			
		}
		return results;
	}
}
