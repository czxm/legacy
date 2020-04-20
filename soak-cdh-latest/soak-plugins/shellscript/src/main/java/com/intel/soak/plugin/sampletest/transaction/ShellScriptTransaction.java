package com.intel.soak.plugin.sampletest.transaction;

import com.intel.soak.logger.TransactionLogger;
import com.intel.soak.model.ParamType;
import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.transaction.Transaction;
import com.intel.soak.vuser.VUserData;

import java.io.*;
import java.util.Iterator;
import java.util.List;

@Plugin(desc="shellscript Transaction", type = PLUGIN_TYPE.TRANSACTION)
public class ShellScriptTransaction implements Transaction {
	
	//protected List<ParamType> params;
	
	protected VUserData user;
	protected TransactionLogger logger;

    private String[] cmd;
    private StringBuilder sb = null;
    private ProcessBuilder builder = null;
    private int execCount = 0;
	
	public ShellScriptTransaction() {
	}
	
	public void setParams(List<ParamType> params) {
		//this.params = params;
        cmd = new String[params.size()];
        int i = 0;
        Iterator<ParamType> it = params.iterator();
        while (it.hasNext()) {
            cmd[i++] = it.next().getValue();
        }
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

        //String[] cmd = {"ycsb-intel/bin/ycsb", "run", "hbase", "-P", "ycsb-intel/workloads/workloada"};
        builder = new ProcessBuilder(cmd);
        builder.redirectErrorStream(true);

		return true;
	}

	@Override
	public boolean beforeExecute() {
	    logger.info(String.format("Job [%s]: beforeExecute", user.getUsername()));

        sb = new StringBuilder();

		return true;
	}

	@Override
	public boolean execute() {
        //long start = System.currentTimeMillis();
        logger.info(String.format("Job [%s]: execute", user.getUsername()));
        //BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            //writer = new BufferedWriter(new FileWriter("output/" + user.getUsername()));

            //Process proc = Runtime.getRuntime().exec("hadoop jar hadoop-example.jar pi 3 3");
            //Process proc = Runtime.getRuntime().exec("ycsb-intel/bin/ycsb run hbase -P ycsb-intel/workloads/workloada");
            //String[] cmd = {"ycsb-intel/bin/ycsb", "run", "hbase", "-P", "ycsb-intel/workloads/workloada"};
            //ProcessBuilder builder = new ProcessBuilder(cmd);
            //builder.redirectErrorStream(true);
            Process proc = builder.start();
            //proc.waitFor();
            reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String line = null;
            while ((line = reader.readLine()) != null) {
                processLine(line);
                sb.append(line).append("\n");
                //writer.write(line);
                //writer.newLine();
            }
            //writer.flush();
		} catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            try {
                //writer.close();
                reader.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return false;
            }
        }
        //System.out.println("execute time ========================================== " + (System.currentTimeMillis() - start));
		return true;
	}

	@Override
	public boolean afterExecute() {
	    logger.info(String.format("Job [%s]: afterExecute", user.getUsername()));

        BufferedWriter writer = null;
        String fileName = "output/" + user.getUsername() + "-" + execCount++;
        try {
            File file = new File("output");
            if (!file.isDirectory())
                file.mkdir();

            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(sb.toString());
            writer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            try {
                writer.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return false;
            }
        }

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

    // to be override by subclass
    protected void processLine(String line) {
        // do nothing
    }
}
