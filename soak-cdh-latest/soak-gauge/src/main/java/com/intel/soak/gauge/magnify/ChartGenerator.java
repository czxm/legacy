package com.intel.soak.gauge.magnify;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ChartGenerator {
	private List<XYDataset> datasets = new ArrayList<XYDataset>();
	private List<XYDataset> secondaryDatasets = new ArrayList<XYDataset>();
	private List<Object> xSeries;
	private Number maxValue = Double.MIN_VALUE;
	private Number secondaryMaxValue = Double.MIN_VALUE;
	private String title;
	private String XAxisName;
	private String YAxisName;
	private Boolean isYAxisDecimal;
	private String secondYAxisName;
	private Boolean isSecondYAxisDecimal;

    public ChartGenerator(String title, String xName, String yName, Boolean isYAxisDecimal) {
    	this.title = title;
    	this.XAxisName = xName;
    	this.YAxisName = yName;
    	this.isYAxisDecimal = isYAxisDecimal;
    }
    
    public void generateChart(OutputStream output, int width, int height, int titleFontSize, int labelFontSize, DateTickUnit tickUnit) throws Exception{
    	if(xSeries == null || xSeries.size() == 0 || datasets.size() < 1)
    		return;
    	
    	JFreeChart chart = ChartFactory.createXYLineChart(title, XAxisName, YAxisName, datasets.get(0), PlotOrientation.VERTICAL,  true, true, false);
    	datasets.remove(0);
    	int index = 1;
    	chart.getTitle().setFont(new Font("Verdana", Font.PLAIN, titleFontSize));
    	XYPlot plot = chart.getXYPlot();

        XYItemRenderer renderer = new XYLineAndShapeRenderer();
        plot.setRenderer(0, renderer);
    	for(XYDataset dataset : datasets){
    		plot.setDataset(index, dataset);
            renderer = new XYLineAndShapeRenderer();
    		renderer.setSeriesStroke(0, new BasicStroke(2.0f,
    				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f,
    				null/*new float[] { 10.0f, 6.0f }*/, 0.0f));
    		plot.setRenderer(index, renderer);
    		index++;
    	}
        ValueAxis raxis = plot.getRangeAxis();
        raxis.setAutoRange(true);
        raxis.setUpperBound(maxValue.doubleValue() + maxValue.doubleValue() * 0.05);
        raxis.setLabelFont(new Font("Verdana", Font.PLAIN, labelFontSize));
        if(raxis instanceof NumberAxis && isYAxisDecimal != null && isYAxisDecimal)
            ((NumberAxis)raxis).setNumberFormatOverride(new DecimalFormat("###,###"));
        else
            ((NumberAxis)raxis).setNumberFormatOverride(new DecimalFormat("###,###.###"));
        
    	if(secondYAxisName != null && secondYAxisName.length() > 0 && secondaryDatasets.size() > 0){
    		raxis = new NumberAxis(secondYAxisName);
            plot.setRangeAxis(1, raxis);
    		raxis.setAutoRange(true);
    		if(raxis instanceof NumberAxis && isSecondYAxisDecimal != null && isSecondYAxisDecimal){
    			DecimalFormat f = new DecimalFormat("###,###");
    			f.setGroupingSize(3);
    			f.setGroupingUsed(true);
    		    ((NumberAxis)raxis).setNumberFormatOverride(f);
    		}
    		else{
    			DecimalFormat f = new DecimalFormat("###,###.###");
    			f.setGroupingSize(3);
    			f.setGroupingUsed(true);
    		    ((NumberAxis)raxis).setNumberFormatOverride(f);
    		}
    		raxis.setUpperBound(secondaryMaxValue.doubleValue() + secondaryMaxValue.doubleValue() * 0.05);
            raxis.setLabelFont(new Font("Verdana", Font.PLAIN, labelFontSize));
    		for(XYDataset dataset : secondaryDatasets){
                plot.setDataset(index, dataset);
                renderer = new XYLineAndShapeRenderer();
        		renderer.setSeriesStroke(0, new BasicStroke(2.0f,
        				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f,
        				null/*new float[] { 10.0f, 6.0f }*/, 0.0f));                
        		plot.setRenderer(index, renderer);
                plot.mapDatasetToRangeAxis(index, 1);
                index++;
    		}
    	}
    	chart.getLegend().setItemFont(new Font("Verdana", Font.PLAIN, labelFontSize));
    	
        chart.setBackgroundPaint(Color.white);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        
        ValueAxis xAxis = plot.getDomainAxis();
        if(tickUnit != null){
            xAxis = new DateAxis();
            ((DateAxis)xAxis).setTickUnit(tickUnit);
            ((DateAxis)xAxis).setDateFormatOverride(
                    tickUnit.getUnitType().equals(DateTickUnitType.SECOND) ?
                            new SimpleDateFormat("HH:mm:ss") :
                            new SimpleDateFormat("HH:mm"));
            plot.setDomainAxis(xAxis);
        }
        xAxis.setAutoRange(true);
        xAxis.setLabelFont(new Font("Verdana", Font.PLAIN, labelFontSize));
        xAxis.setLabel(XAxisName);
		ChartUtilities.writeChartAsJPEG(output, chart, width, height, null);
    }
    
    public void addDataset(boolean secondaryAxis, String name, List<Object> xSeries, List<Object> ySeries) {
    	if(this.xSeries == null || this.xSeries.size() > xSeries.size()){
    		this.xSeries = xSeries;
    	}
    	if(secondaryAxis){
    		secondaryDatasets.add(createDataset(name, xSeries, ySeries));
    		for(Object o : ySeries){
    			if(o instanceof Number){
    				if(((Number)o).doubleValue() > secondaryMaxValue.doubleValue()){
    					secondaryMaxValue = ((Number)o).doubleValue();
    				}
    			}
    		}
    	}
    	else{
    		datasets.add(createDataset(name, xSeries, ySeries));
    		for(Object o : ySeries){
    			if(o instanceof Number){
    				if(((Number)o).doubleValue() > maxValue.doubleValue()){
    					maxValue = ((Number)o).doubleValue();
    				}
    			}
    		}
    	}
    }
    
    public void setSecondaryAxis(String name, Boolean isSecondYAxisDecimal){
    	this.secondYAxisName = name;
    	this.isSecondYAxisDecimal = isSecondYAxisDecimal;
    }

    private XYSeriesCollection createDataset(String name, List<Object> xSeries, List<Object> ySeries) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        if(xSeries.size() > ySeries.size())
        	return dataset;
        XYSeries s1 = new XYSeries(name, false, true);
        for(int i = 0; i < xSeries.size(); i++){
        	Object x = xSeries.get(i);
        	Object y = ySeries.get(i);
        	if(y != null)
        	    s1.add(x instanceof Number ? (Number)x : 0, y instanceof Number ? (Number)y : 0);
        }
        dataset.addSeries(s1);
        return dataset;
    }
}