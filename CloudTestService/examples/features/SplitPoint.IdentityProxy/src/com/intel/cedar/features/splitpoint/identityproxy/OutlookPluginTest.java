package com.intel.cedar.features.splitpoint.identityproxy;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.intel.cedar.feature.Environment;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.tasklet.SimpleTaskItem;
import com.intel.cedar.tasklet.AbstractTaskRunner;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ResultID;

public class OutlookPluginTest extends AbstractTaskRunner {
	private static final long serialVersionUID = 2810144455071793989L;

	@Override
	public List<ITaskItem> getTaskItems(Environment env) {
		List<ITaskItem> results = new ArrayList<ITaskItem>();
		try{
			Variable clientCounts = env.getVariable("client_count");
			Variable users = env.getVariable("users");
			Variable passwords = env.getVariable("passwords");
			for(int i = 0; i < Integer.parseInt(clientCounts.getValue()); i++){
				SimpleTaskItem item = new SimpleTaskItem();
				int uid = (new Random().nextInt()) % users.getValues().size();
				String user = users.getValues().get(uid);
				String password = passwords.getValues().get(uid);
				item.setProperty("user", user);
				item.setProperty("password", password);
				item.setProperty("id", "Client"+i);
				results.add(item);
			}
		}
		catch(Exception e){			
		}
		return results;
	}
	
	private String getCount(String line){
		int pt1 = line.indexOf("(");
		int pt2 = line.lastIndexOf("s)");
		return line.substring(pt1+1, pt2);
	}

	@Override
	public ResultID run(ITaskItem ti, Writer output, Environment env) {
		try{
			String spURL = env.getVariable("spURL").getValue();
			env.extractResource("scripts/ConfigOutlook.vbs");
			env.extractResource("scripts/ClearOutlook.vbs");
			env.extractResource("scripts/TestOutlook.au3");
			SimpleTaskItem item = (SimpleTaskItem)ti;
			
			env.execute("ConfigOutlook.vbs " + spURL);
			env.execute("ClearOutlook.vbs");
			
			Variable users = env.getVariable("users");
			Variable passwords = env.getVariable("passwords");
			int userCount = env.getVariable("users").getValues().size();			
			for(int i = 0; i < userCount; i++){
				String user = users.getValues().get(i);
				String passwd = passwords.getValues().get(i);
				
				StringWriter writer = new StringWriter();
				env.execute("\"c:\\Program Files\\AutoIt3\\AutoIt3\" TestOutlook.au3 " + user + " " + passwd, writer);
				System.out.print(writer.toString());
				env.execute("ClearOutlook.vbs");
				
				// upload all generated screen shots
				IFolder root = env.getStorageRoot();
				IFolder clientFolder = root.getFolder(item.getProperty("id"));
				clientFolder.create();
				IFolder userFolder = clientFolder.getFolder(user);
				userFolder.create();
				for(File png : new File("C:\\").listFiles(new FilenameFilter(){
					@Override
					public boolean accept(File dir, String name) {
						if(name.endsWith("png"))
							return true;
						else
							return false;
					}
				})){
					IFile pngFile = userFolder.getFile(png.getName());
					FileInputStream stream = new FileInputStream(png);
					pngFile.setContents(stream);
					stream.close();
					png.delete();
				}
				
				// result for report generator
				IFile result = userFolder.getFile("result.txt");
				StringWriter writer2 = new StringWriter();
				writer2.append("user=" + user + "\n");
				BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
				String line = reader.readLine();
				while(line != null){
					if(line.startsWith("++Logon")){
						String n = getCount(line);
						if(!line.contains("succeeded")){
							n = "-" + n;
						}
						writer2.write("Logon=" + n + "\n");
					}
					else if(line.startsWith("++Sync1")){
						String n = getCount(line);
						writer2.write("Sync1=" + n + "\n");
					}
					else if(line.startsWith("++Sync2")){
						String n = getCount(line);
						writer2.write("Sync2=" + n + "\n");
					}
					else if(line.startsWith("++Sync3")){
						String n = getCount(line);
						writer2.write("Sync3=" + n + "\n");
					}
					line = reader.readLine();
				}
				result.setContents(new ByteArrayInputStream(writer2.toString().getBytes()));
			}
			
			return ResultID.Passed;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return ResultID.Failed;
	}
}
