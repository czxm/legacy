package com.intel.soak.plugin.helloworld.transaction;

import com.intel.soak.MetricsData.Aggregator;
import com.intel.soak.logger.TransactionLogger;
import com.intel.soak.annotation.Metrics;
import com.intel.soak.model.ParamType;
import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.transaction.Transaction;
import com.intel.soak.vuser.VUserData;

import java.util.List;

@Plugin(desc="helloworld Transaction", type = PLUGIN_TYPE.TRANSACTION)
public class HelloWorldTransaction implements Transaction {
	protected float m;
	protected float n;
	protected List<ParamType> params;
	
	protected VUserData user;
	protected TransactionLogger logger;
	
	public HelloWorldTransaction() {
	}
	
	public void setParams(List<ParamType> params) {
		this.params = params;
	}

    @Override
    public void setUserData(VUserData user) {
        logger.info(String.format("Job [%s]: setup", user.getUsername()));
        this.user = user;
    }

    @Override
    public void setLogger(TransactionLogger logger) {
        this.logger = logger;
    }
    
	@Override
	public boolean startup() {
		logger.info(String.format("Job [%s]: startup", user.getUsername()));
		return true;
	}

	@Override
	public boolean beforeExecute() {
	    logger.info(String.format("Job [%s]: beforeExecute", user.getUsername()));
		return true;
	}

	@Override
	public boolean execute() {
		try {
		    logger.info(String.format("Job [%s]: execute", user.getUsername()));
			Thread.sleep(10000);
		} catch (InterruptedException e) {
		}
		return true;
	}

	@Override
	public boolean afterExecute() {
	    logger.info(String.format("Job [%s]: afterExecute", user.getUsername()));
		return true;
	}

	@Override
	public void shutdown() {
	    logger.info(String.format("Job [%s]: shutdown", user.getUsername()));
	}

    @Override
    public void kill() {
        // TODO Auto-generated method stub        
    }
    
    @Metrics(name="metric0", aggregators = {Aggregator.STDDEV, Aggregator.PCT95})
    public Float getMetric0(){
        return m++;
    }
    
    @Metrics(name="metric1", aggregators={Aggregator.MIN, Aggregator.MED, Aggregator.MAX})
    public Float getMetric1(){
        return n++;
    }
}
