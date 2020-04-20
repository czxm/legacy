package com.intel.ca360.loadmeter.util;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import com.intel.ca360.config.BatchConfigType;
import com.intel.ca360.config.ChartConfigType;
import com.intel.ca360.config.DelayType;
import com.intel.ca360.config.FilesConfigType;
import com.intel.ca360.config.FixDelayType;
import com.intel.ca360.config.IndexedCredentialType;
import com.intel.ca360.config.LoadConfig;
import com.intel.ca360.config.LoadMeasureConfigType;
import com.intel.ca360.config.MergeConfig;
import com.intel.ca360.config.MonitorConfigType;
import com.intel.ca360.config.ParamType;
import com.intel.ca360.config.ParamsType;
import com.intel.ca360.config.ProcessConfig;
import com.intel.ca360.config.RemoteConfigType;
import com.intel.ca360.config.RemoteMeasureConfigType;
import com.intel.ca360.config.SummaryConfigType;
import com.intel.ca360.config.TaskConfigType;
import com.intel.ca360.config.TaskDriverType;
import com.intel.ca360.config.TransactionType;
import com.intel.ca360.config.VirtualUserConfigType;

public class ConfigWriter {
	
	public static void write(String file, Class<?> clz, Object config){
		try {
			FileOutputStream fos = new FileOutputStream(file);
			InputStream is = JAXBuddy.serializeXMLFile(clz, config);
		    String prettyOutput = PrettyPrinter.prettyPrint(is);
			fos.write(prettyOutput.getBytes());
			fos.close();
		}catch (Exception e) {
		}
	}
	
	public static void write(File file, Class<?> clz, Object config){
		try {
			FileOutputStream fos = new FileOutputStream(file);
			InputStream is = JAXBuddy.serializeXMLFile(clz, config);
		    String prettyOutput = PrettyPrinter.prettyPrint(is);
			fos.write(prettyOutput.getBytes());
			fos.close();
		}catch (Exception e) {
		}
	}
	
	public static void main(String[] args) {
		LoadConfig loadConfig = new LoadConfig();
		loadConfig.setDescription("sample config");
		VirtualUserConfigType userConfig = new VirtualUserConfigType();
		userConfig.setTotalUsers(5);
		userConfig.setStartIndex(1);
		IndexedCredentialType credential = new IndexedCredentialType();
		credential.setPassword("123456");
		credential.setUsernamePattern("user%d");
		userConfig.setIndexedCredential(credential);
		loadConfig.setVirtualUserConfig(userConfig);
		TaskConfigType taskConfig = new TaskConfigType();
		taskConfig.setIterations(2);
		TaskDriverType driver = new TaskDriverType();
		driver.setDriver("SimpleHttpDriver");
		TransactionType transaction = new TransactionType();
		transaction.setName("SamlLdapECA360CloudAuthen");
		driver.getTransaction().add(transaction);
		List<ParamType> params = driver.getParam();
		ParamType param = new ParamType();
		param.setName("CookiePolicy");
		param.setValue("compatibility");
		params.add(param);
		param = new ParamType();
		param.setName("SO_Timeout");
		param.setValue("300");
		params.add(param);
		param = new ParamType();
		param.setName("http.protocol.allow-circular-redirects");
		param.setValue("true");
		params.add(param);
		taskConfig.setTaskDriver(driver);
		DelayType delay = new DelayType();
		FixDelayType fixDelay = new FixDelayType();
		fixDelay.setDelay(1);
		delay.setFixDelay(fixDelay);
		taskConfig.setDelay(delay);
		loadConfig.setTaskConfig(taskConfig);
		BatchConfigType batchConfig = new BatchConfigType();
		batchConfig.getBatchUsers().add(1);
		batchConfig.setInterval(5);
		batchConfig.setRampup(5);
		loadConfig.setBatchConfig(batchConfig);
		MonitorConfigType monitorConfig = new MonitorConfigType();
		monitorConfig.setInterval(5);
		monitorConfig.setOutput("result");
		monitorConfig.setName("SimpleHttpDriver");
		RemoteConfigType remote = new RemoteConfigType();
		remote.setServer("eca360.cloudtest.intel.com");
		ProcessConfig p = new ProcessConfig();
		p.setJava(true);
		p.setName("SOAEStarter");
		remote.getProcess().add(p);
		monitorConfig.getRemoteConfig().add(remote);
		loadConfig.setMonitorConfig(monitorConfig);
		
		MergeConfig mergeConfig = new MergeConfig();
		mergeConfig.setFolder("result");
		LoadMeasureConfigType loadMeasureConfig = new LoadMeasureConfigType();
		FilesConfigType filesConfig = new FilesConfigType();
		filesConfig.setFiles("SimpleHttpDriver.csv SimpleHttpDriver2.csv");
		loadMeasureConfig.setFilesConfig(filesConfig);
		loadMeasureConfig.setMergeResult("merged.csv");
		List<ChartConfigType> chartConfigs = loadMeasureConfig.getChartConfig();
		ChartConfigType chartConfig = new ChartConfigType();
		chartConfig.setTitle("Workload Performance (P1 Scenario)");
		chartConfig.setName("merged");
		chartConfig.setHeight(500);
		chartConfig.setWidth(800);
		chartConfig.setDatasetSize(600);
		chartConfig.setXSeries("Index");
		chartConfig.setXAxisName("Time (minute)");
		chartConfig.setYSeries("SamlLdapSSO_MinResp SamlLdapSSO_AvgResp SamlLdapSSO_MaxResp");
		chartConfig.setYSeriesLabel("MinResponseTime AverageResponseTime MaxResponseTime");
		chartConfig.setYAxisName("Response Time (ms)");
		chartConfig.setSecondYSeries("ActiveUsers");
		chartConfig.setSecondYAxisName("Active Users");
		chartConfigs.add(chartConfig);
		mergeConfig.setLoadMeasureConfig(loadMeasureConfig);
		List<RemoteMeasureConfigType> remoteConfigs = mergeConfig.getRemoteMeasureConfig();
		RemoteMeasureConfigType remoteConfig = new RemoteMeasureConfigType();
		remoteConfig.setFile("ECA360.csv");
		chartConfigs = remoteConfig.getChartConfig();
		chartConfig = new ChartConfigType();
		chartConfig.setTitle("ECA360 Performance (P1 Scenario)");
		chartConfig.setName("ECA360");
		chartConfig.setHeight(500);
		chartConfig.setWidth(800);
		chartConfig.setDatasetSize(600);
		chartConfig.setXSeries("Index");
		chartConfig.setXAxisName("Time (minute)");
		chartConfig.setYSeries("java:SOAEStarter_MEM java:SOAEStarter_HEAP");
		chartConfig.setYSeriesLabel("VirtualMemory JavaHeap");
		chartConfig.setYAxisName("Memory (MB)");
		chartConfig.setSecondYSeries("java:SOAEStarter_%CPU");
		chartConfig.setSecondYSeriesLabel("%CPU");
		chartConfig.setSecondYAxisName("CPU Utilization");
		chartConfigs.add(chartConfig);
		remoteConfigs.add(remoteConfig);
		SummaryConfigType summaryConfig = new SummaryConfigType();
		summaryConfig.setActiveUsers("100 500 1000 1500 2000 2500 3000 3500 4000 4500 5000 5500 6000 6500 7000 7500 8000 8500 9000 9500");
		summaryConfig.setName("summary");
		chartConfigs = summaryConfig.getChartConfig();
		chartConfig = new ChartConfigType();
		chartConfig.setTitle("ECA360 Performance Summary (P1 Scenario)");
		chartConfig.setName("summery");
		chartConfig.setHeight(500);
		chartConfig.setWidth(800);
		chartConfig.setDatasetSize(600);
		chartConfig.setXSeries("ActiveUsers");
		chartConfig.setXAxisName("Users");
		chartConfig.setYSeries("SamlLdapSSO_MinResp SamlLdapSSO_AvgResp SamlLdapSSO_MaxResp");
		chartConfig.setYSeriesLabel("MinResponseTime AverageResponseTime MaxResponseTime");
		chartConfig.setYAxisName("Response Time (ms)");
		chartConfig.setSecondYSeries("java:SOAEStarter_%CPU");
		chartConfig.setSecondYSeriesLabel("%CPU");
		chartConfig.setSecondYAxisName("CPU Utilization");		chartConfigs.add(chartConfig);
		mergeConfig.setSummaryConfig(summaryConfig);
		
		try {
			String name = "merge.xml";
			if(args.length > 0){
				name = args[0];
			}
			FileOutputStream fos = new FileOutputStream(name);
			InputStream is = JAXBuddy.serializeXMLFile(MergeConfig.class, mergeConfig);
		    String prettyOutput = PrettyPrinter.prettyPrint(is);
			fos.write(prettyOutput.getBytes());
			fos.close();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
