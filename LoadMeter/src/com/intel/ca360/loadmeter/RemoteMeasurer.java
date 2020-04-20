package com.intel.ca360.loadmeter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.ca360.config.LoadConfig;
import com.intel.ca360.config.MonitorConfigType;
import com.intel.ca360.config.ProcessConfig;
import com.intel.ca360.config.RemoteConfigType;
import com.intel.ca360.loadmeter.util.Util;
import com.intel.cedar.agent.impl.XmlBasedAgent;
import com.intel.cedar.tasklet.impl.PerfMonTaskItem;
import com.intel.cedar.tasklet.impl.PerfMonTaskRunner;

public class RemoteMeasurer implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(RemoteMeasurer.class);
	
	private VirtualUserManager userManager;
	private AtomicBoolean scheduled;
	private LoadConfig config;
	private RemoteConfigType remoteConfig;
	
	public RemoteMeasurer(VirtualUserManager userManager, LoadConfig config, AtomicBoolean scheduled, RemoteConfigType remoteConfig){
		this.userManager = userManager;
		this.config = config;
		this.scheduled = scheduled;
		this.remoteConfig = remoteConfig;
	}
	
	@Override
	public void run() {
		FileOutputStream fos = null;
		try{
			MonitorConfigType monitorConfig = config.getMonitorConfig();
			String fileName = monitorConfig.getOutput() + File.separator + remoteConfig.getServer() + ".csv";
			if(remoteConfig.getName() != null && remoteConfig.getName().length() > 0){
				fileName = monitorConfig.getOutput() + File.separator + remoteConfig.getName() + ".csv";
			}
			File file = new File(fileName);
			if(file.exists()){
				File newFile = new File(Util.getNextFileName(fileName));
				LOG.info(file.getName() + " is renamed to " + newFile.getName() + (file.renameTo(newFile) ? " successfully" : " failed"));
			}
			fos = new FileOutputStream(fileName);
			int interval = monitorConfig.getInterval();
			
			final XmlBasedAgent agent = new XmlBasedAgent(remoteConfig.getServer());
			final PerfMonTaskRunner runner = new PerfMonTaskRunner();
			PerfMonTaskItem item = new PerfMonTaskItem();
			item.setInterval(interval);
			for(ProcessConfig p : remoteConfig.getProcess()){
				String name = p.getName();
				if(name != null){
					item.addProcess(name, p.isJava() != null && p.isJava() ? true : false);
				}
				else{
					item.addProcess(p.getPid());
				}
			}
			
			new Thread(){
				public void run(){
					while(!scheduled.get() || userManager.getActiveUsers() > 0){
						Util.sleep(5);
						if(userManager.isShutdown())
							break;
					}
					agent.kill(runner);
				}
			}.start();
			Runtime.getRuntime().addShutdownHook(new Thread(){
				public void run(){
					agent.kill(runner);
				}
			});
			
			String timeout = "";
			if(config.getTaskConfig().getDuration() != null){
				timeout = Integer.toString(config.getTaskConfig().getDuration());
			}
			agent.setOutputStream(runner, fos);
			agent.run(runner, item, timeout, "");
		}
		catch(Exception e){
			LOG.info(e.getMessage(), e);
		}
		finally{
			try{
				if(fos != null)
					fos.close();
			}
			catch(Exception e){			
			}
		}
		LOG.info("RemoteMeasurer finished");
	}
}
