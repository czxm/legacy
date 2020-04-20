package com.intel.ca360.loadmeter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.ca360.config.LoadConfig;
import com.intel.ca360.config.MonitorConfigType;
import com.intel.ca360.loadmeter.util.Util;

public class LoadMeasurer implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(LoadMeasurer.class);
	
	private VirtualUserManager userManager;
	private AtomicBoolean scheduled;
	private LoadConfig config;
	
	public LoadMeasurer(VirtualUserManager userManager, LoadConfig config, AtomicBoolean scheduled){
		this.userManager = userManager;
		this.config = config;
		this.scheduled = scheduled;
	}
	
	protected MeasureData measureData(int index, float interval){
		MeasureData data = new MeasureData(index);
		boolean beginMeasure = false;
		for(List<TransactionData> dataList : userManager.getAllTransactionDataList()){
			if(!beginMeasure){
				beginMeasure = true;
				data.beginMerge(dataList);
			}
			else{
				data.merge(dataList);
			}
			for(TransactionData t : dataList){
				t.reset();
			}
		}
		if(beginMeasure){
			data.endMerge(interval);
			return data;
		}
		else{
			return null;
		}
	}
	
	@Override
	public void run() {
		FileOutputStream fos = null;
		BufferedWriter writer = null;
		int interval = 1;
		try{
			MonitorConfigType monitorConfig = config.getMonitorConfig();
			if(monitorConfig != null){
				interval = monitorConfig.getInterval();			
				String fileName = monitorConfig.getOutput() + File.separator +  monitorConfig.getName() + ".csv";
				File file = new File(fileName);
				if(file.exists()){
					File newFile = new File(Util.getNextFileName(fileName));
					LOG.info(file.getName() + " is renamed to " + newFile.getName() + (file.renameTo(newFile) ? " successfully" : " failed"));
				}
				fos = new FileOutputStream(fileName);			
				writer = new BufferedWriter(new OutputStreamWriter(fos));			
				writer.write("Index,ActiveUsers,TPS,AvgResp,ErrorRate");
			}
			
			if(config.getTaskConfig().isSyncStartup() != null && config.getTaskConfig().isSyncStartup()){
				userManager.waitForReach(config.getVirtualUserConfig().getTotalUsers());
				long start = System.currentTimeMillis();
				userManager.waitForReach(config.getVirtualUserConfig().getTotalUsers());
				long elapse = System.currentTimeMillis() - start;
				StringBuilder sb = new StringBuilder();
				for(String name : userManager.getTransactionNames()){
					sb.append(String.format(",%s_TX,%s_MinResp,%s_AvgResp,%s_MaxResp,%s_Errors", name, name, name, name, name));
				}
				if(writer != null){
					writer.write(sb.toString());
					writer.newLine();
					writer.flush();
				}
				MeasureData data = measureData(0, ((float)elapse) / 1000);
				if(data != null && writer != null){
					writer.write(data.toString());
					writer.newLine();
					writer.flush();
				}
				userManager.waitForReach(config.getVirtualUserConfig().getTotalUsers());
			}
			else{
				while(userManager.getActiveUsers() == 0){
					Util.sleep(1);
					if(this.scheduled.get())
						break;
				}
				if(userManager.getActiveUsers() > 0){
					StringBuilder sb = new StringBuilder();
					for(String name : userManager.getTransactionNames()){
						sb.append(String.format(",%s_TX,%s_MinResp,%s_AvgResp,%s_MaxResp,%s_Error", name, name, name, name, name));
					}
					if(writer != null){
						writer.write(sb.toString());
						writer.newLine();
						writer.flush();
					}
				}
			}
			int index = 1;
			Util.sleep(interval);
			while(!this.scheduled.get() || userManager.getActiveUsers() > 0){
				MeasureData data = measureData(index, interval);
				if(data != null && writer != null){
					writer.write(data.toString());
					writer.newLine();
					writer.flush();
				}
				index++;
				if(userManager.isShutdown())
					break;
				Util.sleep(interval);
			}
		}
		catch(Exception e){
			LOG.info(e.getMessage(), e);
		}
		finally{
			try{
				if(writer != null)
					writer.close();
				if(fos != null)
					fos.close();
			}
			catch(Exception e){			
			}
		}
		LOG.info("LoadMeasure finished");
	}
}
