package com.intel.cedar.feature;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.intel.ca360.config.DelayType;
import com.intel.ca360.config.FixDelayType;
import com.intel.ca360.config.LoadConfig;
import com.intel.ca360.config.MonitorConfigType;
import com.intel.ca360.config.ParamType;
import com.intel.ca360.config.ProcessConfig;
import com.intel.ca360.config.RemoteConfigType;
import com.intel.ca360.config.TransactionType;
import com.intel.ca360.loadmeter.LoadMeter;
import com.intel.ca360.loadmeter.util.ConfigReader;
import com.intel.ca360.loadmeter.util.ConfigWriter;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.tasklet.AbstractTaskRunner;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ResultID;

public class SplatPerfTestRunner extends AbstractTaskRunner {
	
	private static final long serialVersionUID = -5880173441163268477L;

	@Override
	public List<ITaskItem> getTaskItems(Environment env) {
		List<ITaskItem> items = new ArrayList<ITaskItem>();
		float negRate = 0;
		String server = null;
		try{
			negRate = Float.parseFloat(env.getVariable("neg_rate").getValue());	
		}
		catch(Exception e){
			negRate = 0;
		}
		try{
			server = env.getVariable("server").getValue();
		}
		catch(Exception e){
		}
		if(server == null){
			server = "eca360.cloudtest.intel.com";
		}
		int delay = 0;
		try{
			delay = Integer.parseInt(env.getVariable("delay").getValue());	
		}
		catch(Exception e){
			delay = 0;
		}
		
		try{
			boolean multiTenance = Boolean.parseBoolean(env.getVariable("multi_tenance").getValue());
			boolean multiIWA = Boolean.parseBoolean(env.getVariable("multi_iwa").getValue());
			boolean updateCredOnly = Boolean.parseBoolean(env.getVariable("updateCredOnly").getValue());
			boolean doLogout = Boolean.parseBoolean(env.getVariable("doLogout").getValue());
			
			String mainClz = null;
			int pid = 0;
			try{
				pid = Integer.parseInt(env.getVariable("pid").getValue());
			}
			catch(Exception e){
				mainClz = env.getVariable("pid").getValue();
			}
			
			int tenances = Integer.parseInt(env.getVariable("tenances").getValue());
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
			int numSamlApps = Integer.parseInt(env.getVariable("numSamlApps").getValue());
			int numPostApps = Integer.parseInt(env.getVariable("numPostApps").getValue());
			String type = env.getVariable("type").getValue();
			StringBuilder scenario = new StringBuilder();
			List<String> scenarios = env.getVariable("scenario").getValues();
			for(int i = 0; i < scenarios.size(); i++){
				if(i > 0){
					scenario.append(":");
				}
				scenario.append(scenarios.get(i));
			}
			int users = Integer.parseInt(env.getVariable("users").getValue());
			int usersPerClient = Integer.parseInt(env.getVariable("usersPerClient").getValue());
			int count = users / usersPerClient;
		    if(users % usersPerClient != 0){
		    	count = count + 1;
		    }
			
			for(int i = 0; i < count - 1; i++){
				SplatPerfTaskItem item = new SplatPerfTaskItem();
				item.setMultiIWA(multiIWA);
				item.setMultiTenance(multiTenance);
				item.setTenances(tenances);
			    if(type.equals("batch")){
			    	item.setBatch(true);
			    }
			    else{
			    	item.setBatch(false);
			    }
			    item.setDuration(duration);
			    item.setMonitorRemoteHosts(false);
			    item.setServer(server);
			    item.setUsers(usersPerClient);
			    item.setUserIndex(1 + i * usersPerClient);
			    item.setResultFile("SimpleHttpDriver" + (i + 1));
			    item.setScenario(scenario.toString());
			    item.setClientCount(count);
			    item.setNegRate(negRate);
			    item.setUpdateCredOnly(updateCredOnly);
			    item.setDoLogout(doLogout);
			    item.setNumPostApps(numPostApps);
			    item.setNumSamlApps(numSamlApps);
			    item.setPid(0);
			    item.setDelay(delay);
				items.add(item);
			}
			
		    SplatPerfTaskItem item = new SplatPerfTaskItem();
		    item.setMultiIWA(multiIWA);
			item.setMultiTenance(multiTenance);
			item.setTenances(tenances);
		    if(type.equals("batch")){
		    	item.setBatch(true);
		    }
		    else{
		    	item.setBatch(false);
		    }
		    item.setDuration(duration);
		    // only one host is scheduled to remote the servers
		    item.setMonitorRemoteHosts(true);
		    item.setServer(server);
		    item.setUsers(users - (count - 1) * usersPerClient);
		    item.setUserIndex(1 + (count - 1) * usersPerClient);
		    item.setResultFile("SimpleHttpDriver" + count);
		    item.setScenario(scenario.toString());
		    item.setClientCount(count);
		    item.setNegRate(negRate);
		    item.setUpdateCredOnly(updateCredOnly);
		    item.setDoLogout(doLogout);
		    item.setNumPostApps(numPostApps);
		    item.setNumSamlApps(numSamlApps);
		    item.setPid(pid);
		    item.setJavaMainClass(mainClz);
		    item.setDelay(delay);
			items.add(item);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return items;
	}
	
	@Override
	public void onFinish(final Environment env) {
		try{
			final IFolder result = env.getStorageRoot();
			for(File csv : new File(".").listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					if(name.endsWith("csv")){
						if(name.startsWith("SimpleHttpDriver")){ //save the log file
							String fileIndex = name.replace("SimpleHttpDriver", "").replace(".csv", "");
							IFile logFile = result.getFile("loadmeter" + fileIndex + ".log");
							try {
								env.copyFile(new File("loadmeter.log"), logFile);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						return true;
					}
					else if(name.endsWith("config.xml")){
						return true;
					}
					else
						return false;
				}
			})){
				IFile csvFile = result.getFile(csv.getName());
				env.copyFile(csv, csvFile);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public ResultID run(ITaskItem ti, Writer output, Environment env) {
		try{
			env.extractResource("resource/login.conf");
			env.extractResource("resource/krb5.conf");
			SplatPerfTaskItem item = (SplatPerfTaskItem)ti;
			String scenario = item.getScenario();
			if(scenario.contains(":")){
				scenario = "Reliability";
			}
			
			output.write("Performing scenario: " + scenario + "\n");
			output.write("ECA360 Server: " + item.getServer() + "\n");
			LoadConfig config = new ConfigReader<LoadConfig>().load(SplatPerfTestRunner.class.getClassLoader().getResourceAsStream("conf/config" + scenario + ".xml"), LoadConfig.class);
			
			if(item.isBatch()){
				List<Integer> batchUsers = config.getBatchConfig().getBatchUsers();
				if(batchUsers != null){
					output.write("Reset batch users\n");
					for(int i = 0; i < batchUsers.size(); i++){
						int batchUser = batchUsers.get(i) / item.getClientCount();
						int addition = batchUsers.get(i) % item.getClientCount();
						batchUsers.set(i, item.isMonitorRemoteHosts() ?  batchUser + addition : batchUser);
						output.write(batchUsers.get(i) + "\n");
					}
					if(item.getDuration() > 7200){
						config.getBatchConfig().setRampup(item.getDuration() / 7200 * 300);
					}
				}
			}
			else{
				config.setBatchConfig(null);
				output.write("Disable batch startup!\n");
			}
			if(item.isMultiTenance()){
				output.write("MultiTenance mode is enabled!\n");
				output.write("Number of tenances: " + item.getTenances() + "\n");
			}
			if(item.isMultiIWA()){
				output.write("MultiIWA mode is enabled!\n");
			}
			config.getVirtualUserConfig().setTotalUsers(item.getUsers());
			output.write("Total Users: " + item.getUsers() + "\n");
			config.getVirtualUserConfig().setStartIndex(item.getUserIndex());
			output.write("User Index: " + item.getUserIndex() + "\n");
			config.getTaskConfig().setDuration(item.getDuration());
			output.write("Duration: " + item.getDuration() + "(seconds)\n");
			if(item.getNegRate() > 0 && item.getNegRate() < 1){
				output.write(String.format("Negative Rate: %.2f%%\n", item.getNegRate() * 100));
				config.getTaskConfig().setNegRate(item.getNegRate());
			}
			if(item.isUpdateCredOnly())
				output.write("Credential is updated each time for PostConnector!\n");
			if(!item.isDoLogout())
				output.write("No SLO action will be performed!\n");
			output.write("Number of SAML2 Apps: " + item.getNumSamlApps() + "\n");
			output.write("Number of POST Apps: " + item.getNumPostApps() + "\n");
			if(item.getDelay() > 0){
				DelayType delayType = new DelayType();
				FixDelayType ft = new FixDelayType();
				ft.setDelay(item.getDelay());
				delayType.setFixDelay(ft);
				config.getTaskConfig().setDelay(delayType);
				output.write("Fixed delay: " + item.getDelay() + " seconds\n");
			}
			
			// only for test
			/*
			config.getTaskConfig().setIterations(1);
			if(config.getBatchConfig() != null){
				config.getBatchConfig().setInterval(1);
				config.getBatchConfig().setRampup(1);
			}
			*/
			
			if(item.getScenario().contains(":")){
				List<String> kept = new ArrayList<String>();
				for(String s : item.getScenario().split(":")){
					if(s.equals("P1")){
						kept.add("SamlLdapSSO");
					}
					else if(s.equals("P2") || s.equals("P2_noSaaS")){
						kept.add("IwaSSO");
					}
					else if(s.equals("P3")){
						kept.add("IwaSSOAuthz");
					}
					else if(s.equals("P4")){
						kept.add("LdapSSO");
					}
					else if(s.equals("BHWM")){
						kept.add("LdapSSOPostSignIn");
					}
					else if(s.equals("SamlSSO")){
						kept.add("SamlSSO");
					}
				}
				
				List<TransactionType> toRemove = new ArrayList<TransactionType>();
				for(TransactionType tran : config.getTaskConfig().getTaskDriver().getTransaction()){
					if(!kept.contains(tran.getName()))
						toRemove.add(tran);
				}
				List<TransactionType> transactions = config.getTaskConfig().getTaskDriver().getTransaction();
				for(TransactionType tran : toRemove){
					transactions.remove(tran);
				}
			}
			
			for(TransactionType tran : config.getTaskConfig().getTaskDriver().getTransaction()){
				output.write("Transaction:" + tran.getName() + " Params:\n");
				if(item.isMultiTenance()){
					ParamType tenances = new ParamType();
					tenances.setName("tenances");
					tenances.setValue(Integer.toString(item.getTenances()));
					tran.getParam().add(tenances);
				}
				if(item.isMultiIWA() && tran.getName().startsWith("IwaSSO")){
					ParamType tenances = new ParamType();
					tenances.setName("multiIWA");
					tenances.setValue("true");
					tran.getParam().add(tenances);					
				}
				if(tran.getName().equals("IwaSSO") && item.getScenario().contains("P2_noSaaS")){
					for(ParamType param : tran.getParam()){
						if(param.getName().equals("SaaS")){
							tran.getParam().remove(param);
							break;
						}
					}
				}
				if(tran.getName().equals("LdapSSOPostSignIn")){
					for(ParamType param : tran.getParam()){
						if(param.getName().equals("updateCredOnly")){
							param.setValue(Boolean.toString(item.isUpdateCredOnly()));
							break;
						}
					}
				}
				if(tran.getName().equals("SamlSSO")){
					for(ParamType param : tran.getParam()){
						if(param.getName().equals("samlApps")){
							param.setValue(Integer.toString(item.getNumSamlApps()));
						}
						else if(param.getName().equals("postApps")){
							param.setValue(Integer.toString(item.getNumPostApps()));
						}
					}
				}				
				for(ParamType param : tran.getParam()){
					param.setValue(param.getValue().replace("eca360.cloudtest.intel.com", item.getServer()));
					if(item.isMultiTenance() && item.getTenances() == 0 && !param.getName().contains("Issuer")){
						param.setValue(param.getValue().replace("/splat", ""));
					}
					if(!item.isDoLogout() && param.getName().equalsIgnoreCase("logoutURL")){
						param.setValue("unreachable");
					}
					output.write("Param " + param.getName());
					output.write(" 's value: " + param.getValue() + "\n");
				}
			}
			
			MonitorConfigType monitorConfig = new MonitorConfigType();
			if(item.getDuration() > 28000){
				monitorConfig.setInterval(60);
			}
			else{
				monitorConfig.setInterval(10);
			}
			monitorConfig.setOutput(".");
			monitorConfig.setName(item.getResultFile());
			if(item.isMonitorRemoteHosts()){
				RemoteConfigType remote = new RemoteConfigType();
				remote.setName("ECA360");
				remote.setServer(item.getServer());
				ProcessConfig p = new ProcessConfig();
				p.setJava(true);
				if(item.getPid() > 0)
					p.setPid(item.getPid());
				else
					p.setName(item.getJavaMainClass());
				remote.getProcess().add(p);		
				monitorConfig.getRemoteConfig().add(remote);
				
				if(item.getScenario().contains("P1") || item.getScenario().contains("P4") || item.getScenario().contains("BHWM")){
					remote = new RemoteConfigType();
					remote.setName("LDAP");
					remote.setServer("192.168.101.24");
					p = new ProcessConfig();
					p.setJava(false);
					p.setName("ns-slapd");
					remote.getProcess().add(p);		
					monitorConfig.getRemoteConfig().add(remote);					
				}
				if(item.getScenario().contains("P2") || item.getScenario().contains("P3")){
					if(!item.getScenario().contains("P2_noSaaS")){
						remote = new RemoteConfigType();
						remote.setName("SaaS");
						remote.setServer("dummy-saas.cloudtest.intel.com");
						p = new ProcessConfig();
						p.setJava(true);
						p.setName("Bootstrap");
						remote.getProcess().add(p);		
						monitorConfig.getRemoteConfig().add(remote);	
					}
					
					remote = new RemoteConfigType();
					remote.setName("AD");
					remote.setServer("192.168.101.23");	
					monitorConfig.getRemoteConfig().add(remote);
					
					if(item.isMultiIWA()){
						remote = new RemoteConfigType();
						remote.setName("AD2");
						remote.setServer("192.168.100.215");	
						monitorConfig.getRemoteConfig().add(remote);						
					}
				}
				if(item.getScenario().contains("SamlSSO")){
					remote = new RemoteConfigType();
					remote.setName("MySQL");
					remote.setServer("192.168.101.24");
					p = new ProcessConfig();
					p.setJava(false);
					p.setName("mysqld");
					remote.getProcess().add(p);		
					monitorConfig.getRemoteConfig().add(remote);					
				}
			}
			config.setMonitorConfig(monitorConfig);
			if(item.isMonitorRemoteHosts()){
				// dump the config
				ConfigWriter.write("config.xml", config.getClass(), config);
			}
			output.write("Running LoadMeter\n");
			output.flush();
			new LoadMeter(config).run();
			output.write("LoadMeter finished!\n");
			output.flush();
		}
		catch(Exception e){
			return ResultID.Failed;
		}
		return ResultID.Passed;
	}
}
