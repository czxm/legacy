package com.intel.soak.plugin.hive.transaction;

import java.io.IOException;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.intel.soak.model.ParamType;
import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.plugin.hive.HiveScript;
import com.intel.soak.plugin.hive.util.JDBCConnection;
import com.intel.soak.plugin.hive.util.ResultSetData;
import com.intel.soak.transaction.AbstractTransaction;
import com.intel.soak.utils.FileUtils;
import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

@Plugin( desc = "Hive Jdbc", type = PLUGIN_TYPE.TRANSACTION )
public class HiveJdbcTransaction extends AbstractTransaction {

    private JdbcTemplate template;
    protected List<String> queries = new ArrayList<String>();
    private UserGroupInformation ugi = null;
    private JDBCConnection conn;
    private boolean closeStatement = false;
    private boolean closeConnection = false;

    public  void setJdbcTemplate(JdbcTemplate template){
        this.template = template;
    }

    private boolean establishConnection(){
        try {
            ugi = UserGroupInformation.getCurrentUser();
            conn = new JDBCConnection(template.getDataSource().getConnection());
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
        return true;
    }

    private void closeConnection(){
        try{
            if(conn != null)
                conn.close();
        }
        catch(Exception e){
            logger.error(e.getMessage());
        }
    }

    @Override
    public boolean startup(){
        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("user", getUserData().getUsername());
        for(ParamType p : this.params){
            if(p.getName().startsWith("query")){
                String query = getParamValue(p.getName());
                if(query != null || query.length() > 0)
                    queries.add(query);
            }
            else if(p.getName().equalsIgnoreCase("closeStatement")){
                closeStatement = Boolean.parseBoolean(p.getValue());
            }
            else if(p.getName().equalsIgnoreCase("closeConnection")){
                closeConnection = Boolean.parseBoolean(p.getValue());
            }
            else{
                vars.put(p.getName(), p.getValue());
            }
        }
        for(String n : vars.keySet()){
            queries.add(0, "SET hiveconf:" + n + "=" + vars.get(n) + ";");
        }

        if(!closeConnection)
            return establishConnection();

        return true;
    }

    @Override
    public void shutdown(){
        if(!closeConnection){
            closeConnection();
        }
    }

    @Override
    public boolean beforeExecute(){
        return closeConnection ? establishConnection() : true;
    }

    @Override
    public boolean afterExecute(){
        if(closeConnection)
            closeConnection();
        return true;
    }

    @Override
    public boolean execute(){
        return ugi.doAs(new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    return executeWithinUGI();
                }
            });
    }

    protected boolean executeWithinUGI(){
        boolean ret = true;
        try{
            for(String q : queries){
                try{
                    ResultSetData data = conn.getData(q);
                    Iterator<List<Object>> iter = data.getRowIterator();
                    while(iter.hasNext()){
                        StringBuilder sb = new StringBuilder();
                        for(Object o : iter.next()){
                            if(o != null){
                                sb.append(o.toString());
                                sb.append(",");
                            }
                        }
                        logger.info(sb.toString());
                    }
                    if(closeStatement)
                        data.close();
                }
                catch (Exception e){
                    logger.warn(e.getMessage());
                    if(e.getCause() != null && !(e.getCause() instanceof SQLException)){
                        ret = false;
                        break;
                    }
                }
            }
            return ret;
        }
        catch(Exception e){
            logger.error(e.getMessage());
        }
        return false;
    }

}
