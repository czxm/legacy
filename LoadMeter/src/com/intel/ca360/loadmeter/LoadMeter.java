package com.intel.ca360.loadmeter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.intel.ca360.config.LoadConfig;
import com.intel.ca360.config.MergeConfig;
import com.intel.ca360.loadmeter.util.ConfigReader;
import com.intel.ca360.loadmeter.util.Util;

public class LoadMeter implements Runnable{
	LoadConfig config;
	private ExecutorService exe = Executors.newCachedThreadPool();
	GenericDriver driver = null;
	private AtomicBoolean virtualUserScheduled = new AtomicBoolean(false);
	private VirtualUserManager userManager = new VirtualUserManager();
	
	public AtomicBoolean getUserCompleteFlag(){
		return this.virtualUserScheduled;
	}
	
	public VirtualUserManager getUserManager(){
		return this.userManager;
	}
	
	public LoadConfig getLoadConfig(){
		return this.config;
	}
	
	public GenericDriver getDriver(){
		return driver;
	}
	
	public ExecutorService getExecutorService(){
		return this.exe;
	}
	
	public LoadMeter(LoadConfig config){
		this.config = config;
		try{
			String packageName = LoadScheduler.class.getPackage().getName();
			String driverName = config.getTaskConfig().getTaskDriver().getDriver();
			if(driverName == null || driverName.length() == 0){
				driverName = "GenericDriver";
			}
			else{
				packageName = packageName + ".driver.";
			}			
			String clz = packageName + driverName;			
			driver = (GenericDriver)Class.forName(clz).newInstance();
			driver.setup(config);
		}
		catch(Throwable t){
			driver = null;
			t.printStackTrace();
		}
	}
	
	@Override
	public void run(){
		if(driver != null){
			exe.submit(new LoadScheduler(this));
			Util.sleep(10);
			long start = System.currentTimeMillis();
			int i = 1;
			while(!virtualUserScheduled.get() || userManager.getActiveUsers() > 0){
				Util.sleep(5);
				if((i % 20) == 0 && System.getProperty("RegGC") != null){
					System.gc();
				}
				i++;
				Integer duration = config.getTaskConfig().getDuration();
				if(duration != null && (System.currentTimeMillis() - start) >= duration * 1000){
					userManager.shutdown();
					break;
				}
			}
			exe.shutdownNow();
			try {
				exe.awaitTermination(1, TimeUnit.MINUTES);
				driver.shutDown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static ExecutorService theExecutor = Executors.newCachedThreadPool();
	
	protected static List<Object> parseParams(String[] args){
		List<Object> configs = new ArrayList<Object>();
		for(int i = 0; i < args.length; i++){
			String configFile = args[i];
			if(!new File(configFile).canRead()){
				System.err.println("Invalid config: " + configFile);
			}
			else{
				Object config = new ConfigReader<LoadConfig>().load(configFile, LoadConfig.class);				
				if(config == null){
					config = new ConfigReader<MergeConfig>().load(configFile, MergeConfig.class);
				}
				if(config != null){
					configs.add(config);
				}
				else{
					System.out.println("Invalid config: " + configFile);
				}
			}
		}
		return configs;
	}
	
	protected static void runLoadMeters(List<LoadConfig> configs){		
		List<Future> futures = new ArrayList<Future>();
		for(LoadConfig lConfig : configs){
			futures.add(theExecutor.submit(new LoadMeter(lConfig)));
		}
		int finishes = 0;
		while(finishes < futures.size()){
			Util.sleep(5);
			finishes = 0;
			for(Future f : futures){
				if(f.isDone())
					finishes++;
			}
		}
	}
	
	protected static void runLoadMergers(List<MergeConfig> configs){		
		List<Future> futures = new ArrayList<Future>();
		for(MergeConfig lConfig : configs){
			futures.add(theExecutor.submit(new LoadMerger(lConfig)));
		}
		int finishes = 0;
		while(finishes < futures.size()){
			Util.sleep(5);
			finishes = 0;
			for(Future f : futures){
				if(f.isDone())
					finishes++;
			}
		}
	}
	
	public static void main(String[] args) {
		List<Object> configs = null;
		if(args.length == 0){
			configs = parseParams(new String[]{"config.xml"});
		}
		else{
			configs = parseParams(args);
		}
		
		List<LoadConfig> lconfigs = new ArrayList<LoadConfig>();
		List<MergeConfig> mconfigs = new ArrayList<MergeConfig>();
		if(configs.size() > 0){
			for(Object c : configs){
				if(c instanceof LoadConfig){
					lconfigs.add((LoadConfig)c);
				}
				else if(c instanceof MergeConfig){
					mconfigs.add((MergeConfig)c);
				}
			}
			runLoadMeters(lconfigs);
			runLoadMergers(mconfigs);
			theExecutor.shutdownNow();
			try {
				theExecutor.awaitTermination(1, TimeUnit.MINUTES);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
