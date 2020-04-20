package com.intel.soak.plugin.hive.transaction;

import com.intel.bigdata.common.util.Command;
import com.intel.soak.model.ParamType;
import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.transaction.AbstractTransaction;
import com.intel.soak.utils.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: xzhan27
 * Date: 12/24/13
 * Time: 1:48 AM
 */

@Plugin( desc = "Hive CLI", type = PLUGIN_TYPE.TRANSACTION )
public class HiveCLITransaction extends AbstractTransaction {

    protected String hiveCmd;
    protected List<String> queries = new ArrayList<String>();
    protected String scriptFile;
    protected String authnScheme="SIMPLE";

    public void setHiveCmd(String cmd){
        this.hiveCmd = cmd;
    }

    @Override
    public boolean startup(){
        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("user", getUserData().getUsername());
        for(ParamType p : this.params){
            if(p.getName().startsWith("query")){
                String query = getParamValue(p.getName());
                if(query != null || query.length() > 0){
                    if(!query.endsWith(";"))
                        query = query + ";";
                    queries.add(query);
                }
            }
            else{
                vars.put(p.getName(), p.getValue());
            }
        }
        for(String n : vars.keySet()){
            queries.add(0, "SET hiveconf:" + n + "=" + vars.get(n) + ";");
        }
        String scheme = getParamValue("authnScheme");
        if(scheme != null)
            authnScheme = scheme;
        try{
            if(queries.size() > 0){
                scriptFile = File.createTempFile("hcli", ".hql").getAbsolutePath();
                FileOutputStream fos = new FileOutputStream(scriptFile);
                for(String q : queries){
                    fos.write(q.getBytes());
                    fos.write("\n".getBytes());
                }
                fos.close();
            }
        }
        catch(Exception e){
            logger.error(e.getMessage());
        }
        return true;
    }

    @Override
    public void shutdown(){
        if(scriptFile != null){
            new File(scriptFile).delete();
        }
    }

    @Override
    public boolean execute(){
        try {
            if(scriptFile != null){
                boolean success = true;
                List<String> result = new ArrayList<String>();
                //TODO add kerberos support
                int ret = Command.executeWithOutput(result, 0, hiveCmd, "-v", "-f", scriptFile);
                for(String r : result){
                    logger.info(r);
                }
                if(ret > 0){
                    success = false;
                }
                return success;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return false;
    }
}
