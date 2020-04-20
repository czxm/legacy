package com.intel.cedar.features.splitpoint;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.feature.Environment;
import com.intel.cedar.feature.SVNRegressionTestFeature;
import com.intel.cedar.feature.TaskSummaryItem;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;

public class TestFeature extends SVNRegressionTestFeature {
	
	@Override
	protected List<JUnitReportConfig> getReportConfig(final Environment env){
	    List<JUnitReportConfig> configs = new ArrayList<JUnitReportConfig>();
	    configs.add(new AbstractJUnitReportConfig(){

            @Override
            public IFolder getReportFolder() {
                return env.getStorageRoot().getFolder("report");
            }

            @Override
            public IFolder getResultFolder() {
                return env.getStorageRoot().getFolder("junit");
            }
	        
	    });
	    return configs;
	}
	
	@Override
	public void onFinalize(Environment env) throws Exception{
        super.onFinalize(env);
        IFile junitZip = env.getStorageRoot().getFile("junit.zip");
        if (junitZip.exist()) {
            this.extractZipBundle(junitZip, env.getStorageRoot().getFolder("junit"));
        }
	}

	@Override
	protected String getTestName() {
		return "SplitPoint Checkin Test";
	}

	@Override
	protected boolean isBuildCompleted(Environment env) {
		IFile junitZip = env.getStorageRoot().getFile("junit.zip");
		return junitZip.exist();
	}
	
	protected boolean isCurlTestEnabled(Environment env){
		IFile lmLog = env.getStorageRoot().getFile("lm_summary.xml");
		return lmLog.exist();
	}
	
	protected String getSanityTestSummary(Environment env){
		try{
			IFile lmLog = env.getStorageRoot().getFile("sanitytest.log");
			InputStreamReader r = new InputStreamReader(lmLog.getContents());
			BufferedReader br = new BufferedReader(r);
			String line = null;
			boolean isEmptyFile = true;
			while((line = br.readLine()) != null){
				isEmptyFile = false;
				if(line.contains("Sanity Test Passed")){
					return "Passed";
				}
			}
			if(isEmptyFile){
				return "Skipped";
			}
		}
		catch(Exception e){	
		}
		return "Failed";
	}
	
	protected String getCurlTestSummary(Environment env){
		StringBuilder sb = new StringBuilder();
		try{
			int total = 0;
			int pass = 0;
			IFile lmLog = env.getStorageRoot().getFile("lm_summary.xml");
			InputStreamReader r = new InputStreamReader(lmLog.getContents());
			BufferedReader br = new BufferedReader(r);
			String line = null;
			while((line = br.readLine()) != null){
				if(line.contains("CaseResult")){
					total++;
					if(line.contains("Pass")){
						pass++;
					}
				}
			}
			if(pass == total)
				sb.append(String.format("%d/%d Passed", pass, total));
			else
				sb.append(String.format("%d/%d Failed", total - pass, total));
		}
		catch(Exception e){	
		}
		return sb.toString();
	}
	
	@Override
	protected List<TaskSummaryItem> getSummaryItems(Environment env){
		List<TaskSummaryItem> items = super.getSummaryItems(env);
		for(TaskSummaryItem i : items){
			if(i.getName().equals("Build Result")){
				i.setHyperLink(true);
				i.setUrl(env.getHyperlink(env.getStorageRoot().getFile("build.log")));
				break;
			}
		}
		
		if(isBuildCompleted(env)){
			TaskSummaryItem item = new TaskSummaryItem();
			item.setName("Package");
			item.setValue("package.zip");
			item.setHyperLink(true);
			item.setUrl(env.getHyperlink(env.getStorageRoot().getFile("package.zip")));
			items.add(item);
			
			item = new TaskSummaryItem();
			item.setName("Startup Log");
			item.setValue("startup.log");
			item.setHyperLink(true);
			item.setUrl(env.getHyperlink(env.getStorageRoot().getFile("startup.log")));
			items.add(item);
			
			item = new TaskSummaryItem();
			item.setName("Sanity Test");
			String v = getSanityTestSummary(env);
			if(v.length() == 0)
				v = "sanitytest.log";
			item.setValue(v);
			if(v.contains("Failed"))
				item.setStyle("Failed");
			item.setHyperLink(true);
			item.setUrl(env.getHyperlink(env.getStorageRoot().getFile("sanitytest.log")));
			items.add(item);
			
			if(isCurlTestEnabled(env)){				
				item = new TaskSummaryItem();
				item.setName("API curl test");
				v = getCurlTestSummary(env);
				if(v.length() == 0)
					v = "lm_summary.log";
				item.setValue(v);
				if(v.contains("Failed"))
					item.setStyle("Failed");
				item.setHyperLink(true);
				item.setUrl(env.getHyperlink(env.getStorageRoot().getFile("lm_summary.xml")));
				items.add(item);
			}
		}
		return items;
	}
}
