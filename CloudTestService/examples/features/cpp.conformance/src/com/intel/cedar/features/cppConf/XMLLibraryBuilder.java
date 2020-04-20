package com.intel.cedar.features.cppConf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.feature.Environment;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.tasklet.SimpleTaskItem;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ResultID;
import com.intel.cedar.tasklet.AbstractTaskRunner;


public class XMLLibraryBuilder extends AbstractTaskRunner{
	private static final long serialVersionUID = 7015715811284277036L;

	@Override
	public List<ITaskItem> getTaskItems(Environment env) {
		List<ITaskItem> result = new ArrayList<ITaskItem>();
		try{
			String os = env.getVariable("OS").getValue();
			String arch = env.getVariable("ARCH").getValue();
			String svn_url = env.getVariable("svn_url").getValue();
			String svn_rev = env.getVariable("svn_rev").getValue();
			SimpleTaskItem item = new SimpleTaskItem();
			item.setProperty("os", os);
			item.setProperty("arch", arch);
			item.setProperty("svn_url", svn_url);
			item.setProperty("svn_rev", svn_rev);
			result.add(item);
		}
		catch(Exception e){			
		}
		return result;
	}

	public ResultID run(ITaskItem ti, Writer output, Environment env) {
		try {
			env.extractResource("scripts/XMLLibraryBuilder.pl");
			env.extractResource("scripts/BUILD_ENTRY.pl");
			String cwd = env.getCWD();
			String os = ((SimpleTaskItem)ti).getProperty("os");
			String arch = ((SimpleTaskItem)ti).getProperty("arch");
			String svnUrl = ((SimpleTaskItem)ti).getProperty("svn_url");
			String svnRev = ((SimpleTaskItem)ti).getProperty("svn_rev");
			String buildTargetPlf = CppConformanceTestFeature.genBuildTargetPlf(os,arch);
			String testingPlf = CppConformanceTestFeature.genTestingPlf(buildTargetPlf);
			createBuildConfigFile(cwd + "/CT_demo.config",buildTargetPlf,testingPlf,
					svnUrl+"/xmlcore-src@"+svnRev,
					svnUrl+"/cpp_api@"+svnRev,
					svnUrl+"/java-api@"+svnRev);
			
			List<String> commands = new ArrayList<String>();
			if(testingPlf.equals("windows")){
				commands.add("set ICC_HOME=");
				commands.add("call \"C:\\Program Files\\Intel\\Compiler\\C++\\10.1.025\\IA32\\Bin\\ICLVars.bat\"");
			}
			else if(testingPlf.equals("windows64")){
				commands.add("set ICC_HOME=");
				commands.add("call \"C:\\Program Files (x86)\\Intel\\Compiler\\C++\\10.1.025\\EM64T\\Bin\\iclvars.bat\"");
			}
			else{
				commands.add(". /root/.bashrc");
			}
			commands.add("perl XMLLibraryBuilder.pl " + cwd + " " + testingPlf);
			int ret = env.execute(commands.toArray(new String[]{}), output); 
			IFolder root = env.getStorageRoot();
			IFile file = root.getFile("package.zip");			
			file.setContents(new FileInputStream("package.zip"));			
			return ret == 0 ? ResultID.Passed : ResultID.Failed;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResultID.Failed;
	}

	private void createBuildConfigFile(String configFileName, String buildTargetPlf, String testingPlf, String coreBranch, String cppBranch, String javaBranch) throws IOException{
		 File f = new File(configFileName);
		 BufferedWriter output = new BufferedWriter(new FileWriter(f));
		 output.write("\n<xmlcore_build>\n");
		 output.write("  turn_on=: true\n");
		 output.write("  build_target_platform=: " + buildTargetPlf + "\n");
		 output.write("  pgo_training_platform=: " + testingPlf + "\n");
		 output.write("  xmlcore_branch=: " + coreBranch + "\n");
		 output.write("  is_pgo_build=: false\n");
		 output.write("  is_cross_build=: false\n");
		 output.write("  is_debug_build=: false\n");
		 output.write("  is_rebuild=: true\n");
		 output.write("  is_recheckout=: true\n");
		 output.write("  parallel=: 4\n");
		 output.write("  upload_xmlcore=: false\n");
		 output.write("  ftp_server=:\n");
		 output.write("  built_date=:\n");
		 output.write("  mailto=:\n");
		 output.write("</xmlcore_build>\n");
		 output.write("\n<api_build>\n");
		 output.write("  turn_on=: true\n");
		 output.write("  platform=: " + testingPlf + "\n");
		 output.write("  cpp_branch=: " + cppBranch + "\n");
		 output.write("  java_branch=: " + javaBranch + "\n");
		 output.write("  is_debug_build=: false\n");
		 output.write("  is_rebuild=: true\n");
		 output.write("  is_recheckout=: true\n");
		 output.write("  parallel=: 4\n");
		 output.write("  download_xmlcore=: false\n");
		 output.write("  ftp_server=:\n");		 
		 output.write("  core_built_platform=:\n");
		 output.write("  core_built_date=:\n");
		 output.write("  mailto=:\n");
		 output.write("</api_build>\n");
		 output.close();
	}
	
	public void onStart(Environment env) {
		try {
			CppConformanceTestFeature.updateICCLicense(env);
			CppConformanceTestFeature.addSSLCertsToSVN(env);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
