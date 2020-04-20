package com.intel.cedar.features.splitpoint.reliability;

import java.util.List;

import com.intel.cedar.feature.AbstractFeature;
import com.intel.cedar.feature.Environment;
import com.intel.cedar.feature.TaskSummaryItem;
import com.intel.cedar.storage.IFolder;

public class ReliabilityTestFeature extends AbstractFeature {
	@Override
	public String getFeatureReport(Environment env) {
		StringBuilder sb = new StringBuilder();
		try{
			String engine = env.getVariable("engine").getValue();
			String spServer = env.getVariable("spServer").getValue();
			String dummyServer = env.getVariable("dummyServer").getValue();
			String threads = env.getVariable("thread_count").getValue();
			String loop = env.getVariable("loop_count").getValue();
			String perf_loop = env.getVariable("perfloop_count").getValue();
			String interval = env.getVariable("interval").getValue();
			String driver = env.getVariable("driver").getValue();
			
			sb.append("<p>&nbsp;&nbsp;&nbsp;&nbsp;SplitPoint Reliability Test Configuration:</p>\n");
	        sb.append("<table cellSpacing=0 cellPadding=1 width=\"40%\" border=1>\n");
	        sb.append(String.format("<tr><td bgcolor=#9ac0b0>%s</td><td>%s</td></tr>\n", "SplitPoint Server", spServer));
	        sb.append(String.format("<tr><td bgcolor=#9ac0b0>%s</td><td>%s</td></tr>\n", "Backend Server", dummyServer));
	        sb.append(String.format("<tr><td bgcolor=#9ac0b0>%s</td><td>%s</td></tr>\n", "Engine", engine));
	        sb.append(String.format("<tr><td bgcolor=#9ac0b0>%s</td><td>%s</td></tr>\n", "Driver", driver));
	        sb.append("</table>\n");
	        
			IFolder root = env.getStorageRoot();
	        String cpuChart = env.getHyperlink(root.getFile(driver + "CPU.png"));
	        String memChart = env.getHyperlink(root.getFile(driver + "Mem.png"));
	        String perfChart = env.getHyperlink(root.getFile(driver + "Perf.png"));
	        sb.append(String.format("<img src='%s'></img>%s", cpuChart, "CPU")).append( "\n" );
	        sb.append(String.format("<img src='%s'></img>%s", memChart, "Memory")).append( "\n" );
	        sb.append(String.format("<img src='%s'></img>%s", perfChart, "Performance")).append( "\n" );
		}
		catch(Exception e){
		}
		return sb.toString();
	}

    @Override
    protected List<TaskSummaryItem> getSummaryItems(Environment env) {
        return null;
    }

}
