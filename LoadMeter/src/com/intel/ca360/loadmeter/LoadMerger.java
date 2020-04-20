package com.intel.ca360.loadmeter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import com.intel.ca360.config.ChartConfigType;
import com.intel.ca360.config.FilesConfigType;
import com.intel.ca360.config.MergeConfig;
import com.intel.ca360.config.RemoteMeasureConfigType;
import com.intel.ca360.config.SummaryConfigType;
import com.intel.ca360.loadmeter.util.ChartGenerator;
import com.intel.ca360.loadmeter.util.FileUtils;
import com.intel.ca360.loadmeter.util.ReportGenerator;
import com.intel.cedar.feature.Environment;


public class LoadMerger implements Runnable{	
	private MergeConfig config;
	private SummaryResultFile summary = new SummaryResultFile();
	private ArrayList<String> arcFiles = new ArrayList<String>();

	abstract class ResultFile{
		String[] header;
		
		public String getHeader(){
			StringBuilder sb = new StringBuilder();
			try{
				sb.append(header[0]);
				for(int i = 1; i < header.length; i++){
					sb.append(",");
					sb.append(header[i]);
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
			return sb.toString();
		};
		
		public String[] getHeaderArray(){
			return this.header;
		}
		
		public String getHeader(int column){
			return header[column];
		}
		
		public void setHeader(String h){
			if(h != null)
				this.header = h.split(",");
		}
		
		abstract public List<Object> getSubListByColumn(String column, int max);
		abstract public int getDataListSize();
	}
	
	class MeasureResultFile extends ResultFile{
		List<MeasureData> dataList;

		public List<MeasureData> getDataList(){
			return this.dataList;
		}
		
		public MeasureResultFile(String file){
			dataList = new ArrayList<MeasureData>();
			FileInputStream fis = null;
			try{
				fis = new FileInputStream(file);
				BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
				String line = "";
				setHeader(reader.readLine());
				while((line = reader.readLine()) != null){
					if(line.length() > 0)
						dataList.add(new MeasureData(line));
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
			finally{
				try{
					if(fis != null){
						fis.close();
					}
				}
				catch(Exception e){				
				}
			}
		}
		
		public void save(String file){
			FileOutputStream fos = null;
			try{
				fos = new FileOutputStream(file);
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
				writer.write(header[0]);
				for(int i = 1; i < header.length; i++)
					writer.write("," + header[i]);
				writer.newLine();
				for(MeasureData d : dataList){
					writer.write(d.toString());
					writer.newLine();
				}
				writer.flush();
				writer.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
			finally{
				try{
					if(fos != null){
						fos.close();
					}
				}
				catch(Exception e){				
				}
			}
		}
		
		@Override
		public List<Object> getSubListByColumn(String column, int max){
			int index = 0;
			List<Object> result = new ArrayList<Object>();
			try{
				for(String h : header){
					if(h.equals(column)){
						break;
					}
					index++;
				}
				if(index < header.length){
					int count = 0;
					for(MeasureData data : dataList){
						if(count < max){
							Object o = data.getByIndex(index);
							if(o != null){
								result.add(o);
							}
						}
						count++;
					}
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
			return result;
		}

		@Override
		public int getDataListSize() {
			return this.dataList.size();
		}
	}
	
	class RemoteResultFile extends ResultFile{
		List<RemoteMeasureData> dataList;
		
		public List<RemoteMeasureData> getDataList(){
			return this.dataList;
		}
		
		public RemoteResultFile(String file){
			dataList = new ArrayList<RemoteMeasureData>();
			FileInputStream fis = null;
			try{
				fis = new FileInputStream(file);
				BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
				String line = "";
				setHeader(reader.readLine());
				while((line = reader.readLine()) != null){
					if(line.length() > 0)
						dataList.add(new RemoteMeasureData(line));
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
			finally{
				try{
					if(fis != null){
						fis.close();
					}
				}
				catch(Exception e){				
				}
			}
		}

		@Override
		public List<Object> getSubListByColumn(String column, int max) {
			int index = 0;
			List<Object> result = new ArrayList<Object>();
			for(String h : header){
				if(h.equals(column)){
					break;
				}
				index++;
			}
			if(index < header.length){
				int count = 0;
				for(RemoteMeasureData data : dataList){
					if(count < max){
						Object o = data.getByIndex(index);
						if(o != null){
							result.add(o);
						}
					}
					count++;
				}
			}
			return result;
		}
		
		@Override
		public int getDataListSize() {
			return this.dataList.size();
		}
	}
	
	class SummaryResultFile extends ResultFile{
		List<SummaryData> dataList;
		
		public SummaryResultFile(){
			this.dataList = new ArrayList<SummaryData>();
		}
		
		public void addSummaryLine(SummaryData data){
			this.dataList.add(data);
		}
		
		public List<SummaryData> getSummaryDataList(){
			return this.dataList;
		}
		
		public void save(String file){
			FileOutputStream fos = null;
			try{
				fos = new FileOutputStream(file);
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
				if(header != null && header.length > 0)
					writer.write(header[0]);
				for(int i = 1; i < header.length; i++)
					writer.write("," + header[i]);
				writer.newLine();
				for(SummaryData d : dataList){
					writer.write(d.toString());
					writer.newLine();
				}
				writer.flush();
				writer.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
			finally{
				try{
					if(fos != null){
						fos.close();
					}
				}
				catch(Exception e){				
				}
			}
		}

		@Override
		public List<Object> getSubListByColumn(String column, int max) {
			int index = 0;
			List<Object> result = new ArrayList<Object>();
			for(String h : header){
				if(h.equals(column)){
					break;
				}
				index++;
			}
			if(index < header.length){
				int count = 0;
				for(SummaryData data : dataList){
					if(count < max){
						Object o = data.getByIndex(index);
						if(o != null){
							result.add(o);
						}
					}
					count++;
				}
			}
			return result;
		}
		
		@Override
		public int getDataListSize() {
			return this.dataList.size();
		}
	}
	
	public LoadMerger(MergeConfig config){
		this.config = config;
	}
	
	public void generateCharts(ResultFile resultFile, List<ChartConfigType> chartConfigs) throws Exception {
		// skip this chart if there's too few data points
		if(resultFile.getDataListSize() <= 1){
			return;
		}
		for(ChartConfigType chartConfig : chartConfigs){
			int limit = chartConfig.getDatasetSize() == null ? Integer.MAX_VALUE : chartConfig.getDatasetSize();
			List<Object> indexList = resultFile.getSubListByColumn(chartConfig.getXSeries(), limit);
			if(indexList.size() > 0){
				String title = chartConfig.getTitle();
				String[] ySeries = chartConfig.getYSeries().split(" |,|;");
				String[] ySeriesLabel = null;
				if(chartConfig.getYSeriesLabel() != null){
					ySeriesLabel = chartConfig.getYSeriesLabel().split(" |,|;");
				}
				if(ySeriesLabel != null && ySeriesLabel.length != ySeries.length || ySeries.length == 0){
					continue;
				}
				if(ySeriesLabel == null){
					ySeriesLabel = new String[ySeries.length];
					for(int i = 0; i < ySeries.length; i++){
						ySeriesLabel[i] = ySeries[i];
					}
				}
				ChartGenerator gen = new ChartGenerator(title, chartConfig.getXAxisName(), chartConfig.getYAxisName());
				for(int i = 0; i < ySeries.length; i++){
					List<Object> dataset = resultFile.getSubListByColumn(ySeries[i],limit);
					if(dataset.size() > 0){
						gen.addDataset(false, ySeriesLabel[i], indexList, dataset);
					}
				}
				if(chartConfig.getSecondYSeries() != null && chartConfig.getSecondYAxisName() != null){
					gen.setSecondaryAxis(chartConfig.getSecondYAxisName());
					String[] secondYSeries = chartConfig.getSecondYSeries().split(" |,|;");
					String[] secondYSeriesLabel = null;
					if(chartConfig.getSecondYSeriesLabel() != null){
						secondYSeriesLabel = chartConfig.getSecondYSeriesLabel().split(" |,|;");
					}
					if(secondYSeriesLabel != null && secondYSeriesLabel.length != secondYSeries.length || secondYSeries.length == 0){
						continue;
					}
					if(secondYSeriesLabel == null){
						secondYSeriesLabel = new String[secondYSeries.length];
						for(int i = 0; i < secondYSeries.length; i++){
							secondYSeriesLabel[i] = secondYSeries[i];
						}
					}
					for(int i = 0; i < secondYSeries.length; i++){
						List<Object> dataset = resultFile.getSubListByColumn(secondYSeries[i],limit);
						if(dataset.size() > 0){
							gen.addDataset(true, secondYSeriesLabel[i], indexList, dataset);
						}
					}
				}
				int width = chartConfig.getWidth() == null ? 800 : chartConfig.getWidth();
				int height = chartConfig.getHeight() == null ? 500 : chartConfig.getHeight();
				int titleFontSize = chartConfig.getTitleFontSize() == null ? 30 : chartConfig.getTitleFontSize();
				int labelFontSize = chartConfig.getLabelFontSize() == null ? 20 : chartConfig.getLabelFontSize();
				String fileName = config.getFolder() + File.separator + chartConfig.getName() + ".jpg";
				gen.generateChart(new FileOutputStream(fileName), width, height, titleFontSize, labelFontSize);
				if(!arcFiles.contains(fileName))
					arcFiles.add(fileName);
			}
		}
	}
	
	public void doMerge(){
		if(!new File(config.getFolder()).exists()){
			System.out.println("Can't access dir: " + config.getFolder());
			return;
		}
		
		FilesConfigType filesConfig = config.getLoadMeasureConfig().getFilesConfig();
		String files = filesConfig.getFiles();
		List<String> validFiles = new ArrayList<String>();
		if(files == null){
			files = filesConfig.getFilePattern();
			//TODO: pattern based files
		}
		else{
			for(String s : files.split(" |,|;")){
				String f = config.getFolder() + File.separator + s;
				if(new File(f).canRead()){
					validFiles.add(f);
				}
			}
		}
		if(validFiles.size() == 0){
			System.out.println("No valid files found!");
			return;
		}
				
		arcFiles.addAll(validFiles);
		
		MeasureResultFile mergeFile = new MeasureResultFile(validFiles.remove(0));
		List<MeasureData> dataList = mergeFile.getDataList();
		for(MeasureData d : dataList){
			d.beginMerge();
		}
		int size = dataList.size();
		for(String file : validFiles){
			List<MeasureData> nextList = new MeasureResultFile(file).getDataList();
			for(int  i = 0; i < dataList.size() && i < nextList.size(); i++){
				MeasureData d = dataList.get(i);
				MeasureData nd = nextList.get(i);
				d.merge(nd);
			}
			if(size > nextList.size())
				size = nextList.size();
		}
		for(int  i = 0; i < size; i++){
			MeasureData d = dataList.get(i);
			d.endMerge();
		}
		while(dataList.size() > size){
			dataList.remove(size);
		}
		String fileName = config.getFolder() + File.separator + config.getLoadMeasureConfig().getMergeResult();
		mergeFile.save(fileName);
		arcFiles.add(fileName);
		
		try {
			generateCharts(mergeFile, config.getLoadMeasureConfig().getChartConfig());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		StringBuilder header = new StringBuilder();
		header.append(mergeFile.getHeader());
		List<RemoteResultFile> remoteResults = new ArrayList<RemoteResultFile>();
		for(RemoteMeasureConfigType remoteConfig : config.getRemoteMeasureConfig()){
			String f = config.getFolder() + File.separator + remoteConfig.getFile();
			if(new File(f).canRead()){
				RemoteResultFile file = new RemoteResultFile(f);
				header.append(",");
				header.append(file.getHeader().replace("Index,", ""));
				remoteResults.add(file);
				try {
					generateCharts(file, remoteConfig.getChartConfig());
				} catch (Exception e) {
					e.printStackTrace();
				}
				arcFiles.add(f);
			}
		}
		
		SummaryConfigType summaryConfig = config.getSummaryConfig();
		summary.setHeader(header.toString());
		int index = 1;
		for(String u : summaryConfig.getActiveUsers().split(" |,|;")){
			int users = Integer.parseInt(u);
			List<Integer> range = getRangeByActiveUsers(users, dataList);
			if(range.size() > 0){
				MeasureData md = getAverageData1(getRangedList(range, dataList));
				List<RemoteMeasureData> rmd = new ArrayList<RemoteMeasureData>();
				for(RemoteResultFile rf : remoteResults){
					rmd.add(getAverageData2(getRangedList(range, rf.getDataList())));
				}
				SummaryData sum = new SummaryData(md, rmd);
				sum.setActiveUsers(users);
				sum.setIndex(index);
				index++;
				summary.addSummaryLine(sum);
			}
		}
		fileName = config.getFolder() + File.separator + "summary.csv";
		if(summaryConfig.getName() != null)
			fileName = config.getFolder() + File.separator + summaryConfig.getName() + ".csv";
		summary.save(fileName);
		arcFiles.add(fileName);
		
		try {
			generateCharts(summary, config.getSummaryConfig().getChartConfig());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// we archive all XML files
		for(String f : new File(config.getFolder()).list()){
			if(f.endsWith(".xml")){
				arcFiles.add(config.getFolder() + File.separator + f);
			}
		}
	}
	
	public void createReport(Environment env){
		try{
			ReportGenerator rptGen = new ReportGenerator(env);
			String result = config.getLoadMeasureConfig().getMergeResult();
			if(new File(config.getFolder() + File.separator + result).exists()){
				List<String> charts = new ArrayList<String>();
				for(ChartConfigType chartConfig : config.getLoadMeasureConfig().getChartConfig()){
					if(new File(config.getFolder() + File.separator + chartConfig.getName() + ".jpg").exists()){
						charts.add(chartConfig.getName() + ".jpg");
					}
				}
				rptGen.addDetail(charts, result);
			}
			
			for(RemoteMeasureConfigType remoteConfig : config.getRemoteMeasureConfig()){
				result = remoteConfig.getFile();
				if(new File(config.getFolder() + File.separator + result).exists()){
					List<String> charts = new ArrayList<String>();
					for(ChartConfigType chartConfig : remoteConfig.getChartConfig()){
						if(new File(config.getFolder() + File.separator + chartConfig.getName() + ".jpg").exists()){
							charts.add(chartConfig.getName() + ".jpg");
						}
					}
					rptGen.addDetail(charts, result);
				}
			}
			
			List<String> charts = new ArrayList<String>();
			for(ChartConfigType chartConfig : config.getSummaryConfig().getChartConfig()){
				if(new File(config.getFolder() + File.separator + chartConfig.getName() + ".jpg").exists()){
					charts.add(chartConfig.getName() + ".jpg");
				}
			}
			rptGen.setSummary(charts, summary.getHeaderArray(), summary.getSummaryDataList());
			String file = config.getFolder() + File.separator + "summary.html";
			if(config.getSummaryConfig().getName() != null)
				file = config.getFolder() + File.separator + config.getSummaryConfig().getName() + ".html";
			rptGen.genReport(file);
			arcFiles.add(file);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void createZipBundle(){
		try {
			if(arcFiles.size() >  0)
				FileUtils.zip(new File(config.getFolder()).getName() + ".zip", arcFiles);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<Integer> getRangeByActiveUsers(int users, List<MeasureData> dataList){
		List<Integer> result = new ArrayList<Integer>();
		for(int i = 0; i < dataList.size(); i++){
			if(dataList.get(i).getActiveUsers() == users && dataList.get(i).getIndex() > 0 /* don't count the startup */){
				result.add(i);
			}
		}
		return result;
	}
	
	public <T> List<T> getRangedList(List<Integer> range, List<T> dataList){
		List<T> result = new ArrayList<T>();
		for(Integer i : range){
			if(i < dataList.size())
				result.add(dataList.get(i));
		}
		return result;
	}
	
	public MeasureData getAverageData1(List<MeasureData> dataList){
		MeasureData data = new MeasureData();
		data.beginAverage();
		for(MeasureData d : dataList){
			data.merge(d);
		}
		data.endAverage();
		return data;
	}
	
	public RemoteMeasureData getAverageData2(List<RemoteMeasureData> dataList){
		RemoteMeasureData data = new RemoteMeasureData();
		data.beginAverage();
		for(RemoteMeasureData d : dataList){
			data.merge(d);
		}
		data.endAverage();
		return data;
	}
	
	@Override
	public void run() {
		doMerge();
		createReport(null);
		createZipBundle();
	}
}
