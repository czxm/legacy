package com.intel.ca360.loadmeter;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.ca360.config.BatchConfigType;
import com.intel.ca360.config.LoadConfig;
import com.intel.ca360.config.MonitorConfigType;
import com.intel.ca360.config.RemoteConfigType;
import com.intel.ca360.loadmeter.util.Util;

public class LoadScheduler implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(LoadScheduler.class);
	
	private LoadConfig config;
	private ExecutorService exe;
	private VirtualUserManager userManager;
	private AtomicBoolean userCompleteFlag;
	private GenericDriver driver;
	
	public LoadScheduler(LoadMeter loadMeter){
		this.config = loadMeter.getLoadConfig();
		this.exe = loadMeter.getExecutorService();
		this.userManager = loadMeter.getUserManager();
		this.userCompleteFlag = loadMeter.getUserCompleteFlag();
		this.driver = loadMeter.getDriver();
	}
	
	private void createVirtualUsers(int startIndex, int users){
		createVirtualUsers(startIndex, users, 0);
	}
	
	private void createVirtualUsers(int startIndex, int users, int interval){
		float wait = ((float)interval) / users * 1000;
		for(int i = 0; i < users; i++){
			String userName = "N/A";
			if(config.getVirtualUserConfig().getIndexedCredential() != null){
				userName = String.format(config.getVirtualUserConfig().getIndexedCredential().getUsernamePattern(), startIndex + i);
			}
			else{
				userName = config.getVirtualUserConfig().getFixedCredential().getUsername();
			}
			exe.submit(new VirtualUser(userManager, config, userName, startIndex + i, driver.createTransactions(config.getTaskConfig().getTaskDriver().getTransaction())));
			if(wait > 0){
				Util.usleep((int)wait);
			}
		}
	}
	
	@Override
	public void run() {		
		if(driver != null){
			MonitorConfigType monitorConfig = config.getMonitorConfig();
			if(monitorConfig != null){
				String folderName = monitorConfig.getOutput();
				File folder = new File(folderName);
				if(!folder.exists())
					LOG.info(folderName + " created " + (folder.mkdir() ? "successfully" : "failed"));
			}
			exe.submit(new LoadMeasurer(userManager, config, userCompleteFlag));
			if(monitorConfig != null && monitorConfig.getRemoteConfig() != null){
				for(RemoteConfigType rc : monitorConfig.getRemoteConfig()){
					exe.submit(new RemoteMeasurer(userManager, config, userCompleteFlag, rc));
				}
			}
			
			BatchConfigType batchConfig = config.getBatchConfig();
			int totalUsers = config.getVirtualUserConfig().getTotalUsers();
			if(batchConfig != null){
				int users = 0;
				int userIndex = config.getVirtualUserConfig().getStartIndex();
				List<Integer> batchUsers = batchConfig.getBatchUsers();
				if(batchUsers.size() == 1){
					int batch = batchUsers.get(0);
					while(users < totalUsers){
						int startUsers = batch; 
						if(users +  startUsers > totalUsers){
							startUsers = totalUsers - users;
						}
						createVirtualUsers(userIndex, startUsers, batchConfig.getInterval());
						users += startUsers;
						userIndex += startUsers;
						if(userManager.isShutdown())
							break;
						LOG.info("Wait {}(s) for rampup", new Object[]{batchConfig.getRampup()});
						Util.sleep(batchConfig.getRampup());
					}
				}
				else{
					int startUsers = 0;
					for(Integer batch : batchUsers){
						if(users < totalUsers){
							startUsers = batch; 
							if(users +  startUsers > totalUsers){
								startUsers = totalUsers - users;
							}
							createVirtualUsers(userIndex, startUsers, batchConfig.getInterval());
							users += startUsers;
							userIndex += startUsers;
							if(userManager.isShutdown())
								break;
							LOG.info("Wait {}(s) for rampup", new Object[]{batchConfig.getRampup()});
							Util.sleep(batchConfig.getRampup());
						}
					}
					if(users < totalUsers && !userManager.isShutdown()){
						startUsers = totalUsers - users;
						createVirtualUsers(userIndex, startUsers, batchConfig.getInterval());
						users += startUsers;
						userIndex += startUsers;
						LOG.info("Wait {}(s) for rampup", new Object[]{batchConfig.getRampup()});
						Util.sleep(batchConfig.getRampup());
					}
				}
			}
			else{
				createVirtualUsers(config.getVirtualUserConfig().getStartIndex(),
						           config.getVirtualUserConfig().getTotalUsers());
			}
		}
		LOG.info("LoadScheduler finished");
		this.userCompleteFlag.set(true);
	}
}
