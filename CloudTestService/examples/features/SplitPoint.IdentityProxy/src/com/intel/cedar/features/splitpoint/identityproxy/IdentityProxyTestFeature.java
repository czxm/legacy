package com.intel.cedar.features.splitpoint.identityproxy;

import java.util.List;
import java.util.Properties;

import com.intel.cedar.feature.AbstractFeature;
import com.intel.cedar.feature.Environment;
import com.intel.cedar.feature.TaskSummaryItem;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;

public class IdentityProxyTestFeature extends AbstractFeature{
	private int getNumber(String n){
		try{
			return Integer.parseInt(n);
		}
		catch(Exception e){
			return 0;
		}
	}

	@Override
	public String getFeatureReport(Environment env) {
		StringBuilder sb = new StringBuilder();
		try{
			IFolder root = env.getStorageRoot();
			
			sb.append(String.format("<p>&nbsp;&nbsp;&nbsp;&nbsp;SplitPoint Server URL: %s</p>\n",env.getVariable("spURL").getValue()));
			sb.append("<p>&nbsp;&nbsp;&nbsp;&nbsp;Outlook Plugin Performance:</p>\n");
	        sb.append("<table cellSpacing=0 cellPadding=1 width=\"70%\" border=1>\n");
	        sb.append("<tr bgColor=#9acd32><th>Client</th><th>User</th><th>Logon</th><th>Sync Contacts</th><th>Sync Events</th><th>Sync Tasks</th></tr>\n");
	        
	        int tLogon = 0;
	        int tSync1 = 0;
	        int tSync2 = 0;
	        int tSync3 = 0;
	        
			Variable clientCounts = env.getVariable("client_count");
			Variable users = env.getVariable("users");
			int count = Integer.parseInt(clientCounts.getValue());
			int userCount = env.getVariable("users").getValues().size();
			for(int i = 0; i < count; i++){
				String name = "Client"+i;
				IFolder folder = root.getFolder(name);
				
				sb.append(String.format("<tr rowspan=\"%d\"><th bgcolor=#9ac0b0><a href=\"%s\">%s</a></th>", userCount, env.getHyperlink(folder), name));
				
				for(int u = 0; u < userCount; u++){
					String user = users.getValues().get(u);
					IFolder userFolder = folder.getFolder(user);
					IFile result = userFolder.getFile("result.txt");
					Properties prop = new Properties();
					prop.load(result.getContents());

					if(u > 0){
						sb.append("<tr>");
					}
					sb.append("<td align=middle>");
					sb.append(String.format("<a href=\"%s\">%s</a>", env.getHyperlink(userFolder), user));
					sb.append("</td>");
					
					sb.append("<td align=middle>");
					String logon = prop.getProperty("Logon", "");
					if(logon.startsWith("-")){
						logon = logon.substring(1);
						sb.append("<font color=red>" + logon + "</font>");
					}
					else{
						sb.append(logon);
					}
					tLogon += getNumber(logon);
					sb.append("</td>");
					
					String n = prop.getProperty("Sync1", "");
					sb.append("<td align=middle>" + n + "</td>");
					tSync1 += getNumber(n);
					
					n = prop.getProperty("Sync2", "");
					sb.append("<td align=middle>" + n + "</td>");
					tSync2 += getNumber(n);
					
					n = prop.getProperty("Sync3", "");
					sb.append("<td align=middle>" + n + "</td>");
					tSync3 += getNumber(n);
					
					sb.append("</tr>\n");
				}
			}			
			
			count = count * userCount;
			if(count > 1){
				sb.append("<tr><th bgcolor=#9ac0b0>Average</th><td/>");
				sb.append(String.format("<td align=middle>%.2f</td>", ((float)tLogon) / count));
				sb.append(String.format("<td align=middle>%.2f</td>", ((float)tSync1) / count));
				sb.append(String.format("<td align=middle>%.2f</td>", ((float)tSync2) / count));
				sb.append(String.format("<td align=middle>%.2f</td>", ((float)tSync3) / count));
				sb.append("</tr>\n");
			}
			sb.append("</table>\n");
		}
		catch(Exception e){
			sb.delete(0, sb.length()-1);
			sb.append("<p><font color=red>Failed to generate report</font></p>");
		}
		return sb.toString();
	}

    @Override
    protected List<TaskSummaryItem> getSummaryItems(Environment env) {
        return null;
    }
}
