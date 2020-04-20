package com.intel.cedar.feature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.intel.ca360.config.ChartConfigType;
import com.intel.ca360.config.MergeConfig;
import com.intel.ca360.config.RemoteMeasureConfigType;
import com.intel.ca360.loadmeter.LoadMerger;
import com.intel.ca360.loadmeter.util.ConfigReader;
import com.intel.ca360.loadmeter.util.ConfigWriter;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;

public class SplatPerfTestFeature extends AbstractFeature {
	
	public static void deleteFolderContents(File folder) {
		if (folder.exists() && folder.isDirectory()) {
			File[] files = folder.listFiles();
			for (File file : files) {
				if (!file.isDirectory()) {
					file.delete();
				} else {
					deleteFolderContents(file);
					file.delete();
				}
			}
		}
	}

	public static void deleteFolderAndContents(File folder) {
		deleteFolderContents(folder);
		folder.delete();
	}

	@Override
	public String getFeatureReport(Environment env) {
		StringBuilder sb = new StringBuilder();
		try{
			String durationStr = env.getVariable("duration").getValue();
			int unit = 1;
			if(durationStr.endsWith("hours")){
				durationStr = durationStr.replace(" hours", "");
				unit = 3600;
			}
			else if(durationStr.endsWith("days")){
				durationStr = durationStr.replace(" days", "");
				unit = 24 * 3600;
			}
			int duration = Integer.parseInt(durationStr) * unit;
			String mainClz = null;
			int pid = 0;
			try{
				pid = Integer.parseInt(env.getVariable("pid").getValue());
			}
			catch(Exception e){
				mainClz = "java:" + env.getVariable("pid").getValue();
			}
			int interval = 10;
			if(duration > 28000){
				interval = 60;
			}
			boolean multiTenance = Boolean.parseBoolean(env.getVariable("multi_tenance").getValue());
			String scenario = "Reliability";
			if(env.getVariable("scenario").getValues().size() == 1)
				scenario = env.getVariable("scenario").getValue();
			int users = Integer.parseInt(env.getVariable("users").getValue());
			int usersPerClient = Integer.parseInt(env.getVariable("usersPerClient").getValue());
			int count = users / usersPerClient;
		    if(users % usersPerClient != 0){
		    	count = count + 1;
		    }
		    
			List<String> files = new ArrayList<String>();
			files.add("ECA360.csv");
			files.add("ECA360_2.csv");
			files.add("LDAP.csv");
			files.add("AD.csv");
			files.add("AD2.csv");
			files.add("SaaS.csv");
			files.add("MySQL.csv");
			files.add("config.xml");
			StringBuilder sb1 = new StringBuilder();
			for(int i = 1; i <= count; i++){
				sb1.append("SimpleHttpDriver" + i + ".csv");
				files.add("SimpleHttpDriver" + i + ".csv");
				if(i != count){
					sb1.append(" ");
				}
			}
			File tmpFolder = null;
			String tmpDir = System.getProperty("java.io.tmpdir");
			if(tmpDir == null || tmpDir.length() == 0)
				tmpFolder = new File(Long.toString(System.currentTimeMillis()));
			else
				tmpFolder = new File(tmpDir, Long.toString(System.currentTimeMillis()));
			tmpFolder.mkdir();
			IFolder storage = env.getStorageRoot();
			for(String f : files){
				IFile file = storage.getFile(f);
				if(file.exist()){
					env.copyFile(file, new File(tmpFolder, f));
				}
			}
			
			MergeConfig config = new ConfigReader<MergeConfig>().load(SplatPerfTestFeature.class.getClassLoader().getResourceAsStream("conf/merge" + scenario + ".xml"), MergeConfig.class);
			config.setFolder(tmpFolder.getAbsolutePath());
			config.getLoadMeasureConfig().setMergeResult("SimpleHttpDriver.csv");
			config.getLoadMeasureConfig().getFilesConfig().setFiles(sb1.toString());
			
			for(ChartConfigType chartConfig : config.getLoadMeasureConfig().getChartConfig()){
				if(chartConfig.getXAxisName() != null){
					chartConfig.setXAxisName(chartConfig.getXAxisName().replace("10", Integer.toString(interval)));
				}				
			}
			
			for(RemoteMeasureConfigType remote : config.getRemoteMeasureConfig()){
				for(ChartConfigType chartConfig : remote.getChartConfig()){
					if(chartConfig.getYSeries() != null)
						chartConfig.setYSeries(chartConfig.getYSeries().replace("java:SOAEStarter", pid > 0 ? Integer.toString(pid) : mainClz));
					if(chartConfig.getSecondYSeries() != null)
						chartConfig.setSecondYSeries(chartConfig.getSecondYSeries().replace("java:SOAEStarter", pid > 0 ? Integer.toString(pid) : mainClz));
					if(chartConfig.getXAxisName() != null){
						chartConfig.setXAxisName(chartConfig.getXAxisName().replace("10", Integer.toString(interval)));
					}
				}
			}

			for(ChartConfigType chartConfig : config.getSummaryConfig().getChartConfig()){
				if(chartConfig.getYSeries() != null)
					chartConfig.setYSeries(chartConfig.getYSeries().replace("java:SOAEStarter", pid > 0 ? Integer.toString(pid) : mainClz));
				if(chartConfig.getSecondYSeries() != null)
					chartConfig.setSecondYSeries(chartConfig.getSecondYSeries().replace("java:SOAEStarter", pid > 0 ? Integer.toString(pid) : mainClz));					
			}
			
			// dump the config
			File mergeFile = new File(tmpFolder, "merge.xml");
			ConfigWriter.write(mergeFile, config.getClass(), config);
			env.copyFile(mergeFile, storage.getFile("merge.xml"));
			
			LoadMerger merger = new LoadMerger(config);
			merger.doMerge();
			merger.createReport(env);
			merger.createZipBundle();
			
			for(File r : tmpFolder.listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					if(name.endsWith("jpg") || name.endsWith("csv") || name.endsWith("html"))
						return true;
					else
						return false;
				}
			})){
				IFile file = storage.getFile(r.getName());
				if(!file.exist())
					env.copyFile(r, file);
			}
			File resultZip = new File(tmpFolder.getName() + ".zip");
			IFile file = storage.getFile("result.zip");
			env.copyFile(resultZip, file);
			resultZip.delete();
			deleteFolderAndContents(tmpFolder);
			
			File summary = new File("summary.fragment");
			InputStreamReader reader = new InputStreamReader(
					new FileInputStream(summary));
			BufferedReader br = new BufferedReader(reader);
			String line;
			while ((line = br.readLine()) != null)
				sb.append(line);
			br.close();
			summary.delete();
		}
		catch(Exception e){
			sb.setLength(0);
			sb.append("<p>&nbsp;&nbsp;&nbsp;&nbsp;Failed to generate LoadMeter report: " + e.getMessage() + "</p>");
		}
		return sb.toString();
	}

	@Override
	protected List<TaskSummaryItem> getSummaryItems(Environment env) {
		List<TaskSummaryItem> items = new ArrayList<TaskSummaryItem>();
		TaskSummaryItem item = new TaskSummaryItem();
		item.setName("LoadMeter config");
		item.setValue("config.xml");
		item.setHyperLink(true);
		item.setUrl(env.getHyperlink(env.getStorageRoot().getFile("config.xml")));
		items.add(item);

		item = new TaskSummaryItem();
		item.setName("Merge config");
		item.setValue("merge.xml");
		item.setHyperLink(true);
		item.setUrl(env.getHyperlink(env.getStorageRoot().getFile("merge.xml")));
		items.add(item);

		item = new TaskSummaryItem();
		item.setName("Result");
		item.setValue("result.zip");
		item.setHyperLink(true);
		item.setUrl(env.getHyperlink(env.getStorageRoot().getFile("result.zip")));
		items.add(item);
		return items;
	}
}
