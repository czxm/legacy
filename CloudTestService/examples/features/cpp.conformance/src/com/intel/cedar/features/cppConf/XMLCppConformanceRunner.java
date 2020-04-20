package com.intel.cedar.features.cppConf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.feature.Environment;
import com.intel.cedar.feature.util.FileUtils;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.tasklet.SimpleTaskItem;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ResultID;
import com.intel.cedar.tasklet.AbstractTaskRunner;

public class XMLCppConformanceRunner extends AbstractTaskRunner{
	private static final long serialVersionUID = -2918649630258082967L;

	@Override
	public List<ITaskItem> getTaskItems(Environment env) {
		List<ITaskItem> result = new ArrayList<ITaskItem>();
		try{
			String os = env.getVariable("OS").getValue();
			String arch = env.getVariable("ARCH").getValue();
			String taskId = env.getVariable("taskId").getValue();
			String buildTargetPlf = CppConformanceTestFeature.genBuildTargetPlf(os,arch);
			String testingPlf = CppConformanceTestFeature.genTestingPlf(buildTargetPlf);
			for(String v : env.getVariable("case").getValues()){
				SimpleTaskItem item = new SimpleTaskItem();
				item.setProperty("os", os);
				item.setProperty("arch", arch);
				item.setValue("perl " + v + " cop=t autocheckin=true plf=" + testingPlf + " uploadraw=true job=" + taskId);
				result.add(item);
			}
		}
		catch(Exception e){			
		}
		return result;
	}

	public ResultID run(ITaskItem ti, Writer output, Environment env) {
		try {
			env.extractResource("scripts/CppConfTestRunSingle.pl");
			List<String> commands = new ArrayList<String>();
			String ICC_PATH="";
			String VS_PATH="";
			if(env.getOSName().contains("Windows")){
				if(env.getArchitecture().equals("x86_64")){
					ICC_PATH="\"C:\\Program Files (x86)\\Intel\\Compiler\\C++\\10.1.025\\EM64T\\Bin\"";
					VS_PATH="\"C:\\Program Files (x86)\\Microsoft Visual Studio 8\\Common7\\IDE\"";
				}
				else{
					ICC_PATH="\"C:\\Program Files\\Intel\\Compiler\\C++\\10.1.025\\IA32\\Bin\"";
					VS_PATH="\"C:\\Program Files\\Microsoft Visual Studio .NET 2003\\Common7\\IDE\"";
				}
				commands.add("set Path="+ICC_PATH+";"+VS_PATH+";%Path%");
				commands.add("set XCHECK=" + env.getCWD()+File.separator+"xcheck");
			}
			else{
				commands.add(". /root/.bashrc");
				commands.add("export XCHECK=" + env.getCWD()+File.separator+"xcheck");
			}
			String os = ((SimpleTaskItem)ti).getProperty("os");
			String arch = ((SimpleTaskItem)ti).getProperty("arch");
			String buildTargetPlf = CppConformanceTestFeature.genBuildTargetPlf(os,arch);
			String testingPlf = CppConformanceTestFeature.genTestingPlf(buildTargetPlf);
			String cwd = env.getCWD();
			commands.add("perl CppConfTestRunSingle.pl "+ cwd + " " + testingPlf + " " + ti.getValue());
			if(0==env.execute(commands.toArray(new String[]{}), output))
				return ResultID.Passed;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResultID.Failed;
	}

	public void onStart(Environment env) {
		try {
			CppConformanceTestFeature.updateICCLicense(env);
			CppConformanceTestFeature.addSSLCertsToSVN(env);
			IFolder root = env.getStorageRoot();
			IFile file = root.getFile("package.zip");			
			InputStream input = file.getContents();
			FileOutputStream output = new FileOutputStream("package.zip");
			FileUtils.copyStream(input, output);
			input.close();
			output.close();
			env.extractResource("scripts/send-to-repserver.pl");
			env.extractResource("scripts/CppConfTestPrepare.pl");
			String cwd = env.getCWD();
			env.execute("perl CppConfTestPrepare.pl " + cwd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
