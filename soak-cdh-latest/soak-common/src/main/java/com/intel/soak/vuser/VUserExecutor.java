package com.intel.soak.vuser;

import com.intel.soak.MetricsData;
import com.intel.soak.MetricsData.Aggregator;
import com.intel.soak.logger.TransactionLogger;
import com.intel.soak.config.ConfigUtils;
import com.intel.soak.annotation.Metrics;
import com.intel.soak.model.LoadConfig;
import com.intel.soak.model.TransactionType;
import com.intel.soak.transaction.Transaction;
import com.intel.soak.utils.LoadUtils;
import com.intel.soak.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.*;

public class VUserExecutor {
    private static Logger LOG = LoggerFactory.getLogger(VUserExecutor.class);
    
    static class WeightRange{
        float from;
        float to;
        boolean accept(float r){
            return (r <= to && r >= from) ? true : false;
        }
    }
    
    static class MetricsMethod{
        Method m;
        MetricsData d;
    }
    
    private List<WeightRange> weightRanges = new ArrayList<WeightRange>();
	private List<Transaction> transactions;
	private List<TransactionLogger> loggers;
	private List<MetricsData> dataList = new ArrayList<MetricsData>();
	private Map<Transaction, List<MetricsMethod>> metricsMethods = new HashMap<Transaction, List<MetricsMethod>>();
	private LoadConfig config;
	private boolean stopFlag;
	private int currentTransactionIndex;
    private Random random = new Random();
    
	public VUserExecutor(LoadConfig config, List<TransactionLogger> loggers, List<Transaction> transactions) {
	    this.config = config;
		this.transactions = transactions;
		this.loggers = loggers;
		String[] names = LoadUtils.getTransactionNames(transactions.toArray());
		for(int i = 0; i < transactions.size(); i++){
		    MetricsData data = new MetricsData(names[i], new Aggregator[]{
		            Aggregator.CNT,Aggregator.NCNT,Aggregator.MIN,
		            Aggregator.AVG,Aggregator.MED,Aggregator.MAX,
                    Aggregator.STDDEV,Aggregator.PCT75,Aggregator.PCT95,Aggregator.PCT99});
		    dataList.add(data);
		}
		for(int i = 0; i < transactions.size(); i++){	         
            Transaction tran = transactions.get(i);
            List<MetricsMethod> mmList = new ArrayList<MetricsMethod>();
            metricsMethods.put(tran, mmList);
            for(Method m : tran.getClass().getMethods()){
                Metrics mx = m.getAnnotation(Metrics.class);
                if(mx != null){
                    MetricsData data = new MetricsData(names[i] + "_" + mx.name(), mx.aggregators());
                    dataList.add(data);
                    MetricsMethod mm = new MetricsMethod();
                    mm.m = m;
                    mm.d = data;
                    mmList.add(mm);
                }
            }
		}
		
		initWeightRange();
	}
	
	private void initWeightRange(){
	    // collect the MIN/MAX of all the weights
	    int minWeight = Integer.MAX_VALUE;
	    int maxWeight = Integer.MIN_VALUE;
	    List<TransactionType> trans = ConfigUtils.getTransactions(config);
	    for(TransactionType t : trans){
	        if(t.getWeight() != null){
	            int w = t.getWeight();
	            if(minWeight > w)
	                minWeight = w;
	            if(maxWeight < w)
	                maxWeight = w;
	        }
	    }
	    int fillWeight = 1;
	    if(minWeight == maxWeight){
	        // only 1 weight is defined, fill all with this wight
	        fillWeight = minWeight;
	    }
	    else{
	        fillWeight = minWeight;
	    }
	    float totalWeight = 0;
	    for(TransactionType t : trans){
            if(t.getWeight() == null){
                t.setWeight(fillWeight);
            }
            totalWeight += t.getWeight();
        }
	    float start = 0f;
        for(TransactionType t : trans){
            WeightRange r = new WeightRange();
            r.from = start;
            r.to = r.from + t.getWeight() / totalWeight;
            start = r.to;
            weightRanges.add(r);
        }	    
	}
	
	private int getNextWeightedRandom(){
	    int next = -1;
	    while(next < 0){
	        float r = random.nextFloat();
    	    for(int i = 0; i < weightRanges.size(); i++){
    	        if(weightRanges.get(i).accept(r)){
    	            next = i;
    	            break;
    	        }
    	    }
	    }
	    return next;	    
	}
	
	public void kill(){
	    stopFlag = true;
	    this.transactions.get(currentTransactionIndex).kill();
	}
	
	public List<MetricsData> getMetricsData(){
	    return this.dataList;
	}
	
    public void run() {
        if (transactions.size() == 0) {
            LOG.warn("No transactions defined!");
            return;
        }

        try {
            for (Transaction t : transactions) {
                t.startup();
            }

            int maxCount = Integer.MAX_VALUE;
            Integer iterations = config.getTaskConfig().getIterations();
            if (iterations != null && iterations > 0)
                maxCount = config.getTaskConfig().getIterations();
            Boolean isOrdered = config.getTaskConfig().getTaskDriver().getOrdered();
            int duration = ConfigUtils.getTaskDuration(config);

            long startTime = System.currentTimeMillis();
            for (int i = 0; i < maxCount; i++) {
                currentTransactionIndex = i % transactions.size();
                if(isOrdered != null && !isOrdered){
                    currentTransactionIndex = getNextWeightedRandom();
                }
                Transaction tran = transactions.get(currentTransactionIndex);
                MetricsData d = dataList.get(currentTransactionIndex);
                boolean suc = true;
                Float resp = null;
                try{
                    suc = tran.beforeExecute();
                    long start = System.currentTimeMillis();
                    suc = tran.execute() && suc;
                    resp = (float)(System.currentTimeMillis() - start);
                    suc = tran.afterExecute() && suc;
                    if(!suc)
                        resp = null;
                }
                catch(Throwable t){
                    resp = null;
                    loggers.get(currentTransactionIndex).error(t.getMessage());
                }
                d.mergeValue(resp);

                for(MetricsMethod mm : metricsMethods.get(tran)){
                    Float t = null;
                    try{
                        t = (Float)mm.m.invoke(tran);
                    }
                    catch(Throwable e){
                        t = null;
                    }
                    mm.d.mergeValue(t);
                }
                
                if (stopFlag || (duration > 0
                        && System.currentTimeMillis() - startTime > (duration * 1000)))
                    break;

                if (config.getTaskConfig().getDelay().getFixDelay() != null) {
                    ThreadUtils.sleep(config.getTaskConfig().getDelay()
                            .getFixDelay().getDelay());
                }
            }
            for (Transaction t : transactions) {
                t.shutdown();
            }
        } catch (Throwable t) {
            StringWriter buf = new StringWriter();
            PrintWriter writer = new PrintWriter(buf);
            t.printStackTrace(writer);
            writer.close();
            LOG.error(buf.toString());
        }
    }

}
