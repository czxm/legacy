package com.intel.soak.plugin.hive.transaction;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

@Plugin( desc = "Hive Jdbc", type = PLUGIN_TYPE.TRANSACTION )
public class HiveJdbcTransaction extends AbstractTransaction {

    private JdbcTemplate template;
    protected List<String> queries = new ArrayList<String>();

    public  void setJdbcTemplate(JdbcTemplate template){
        this.template = template;
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
            else{
                vars.put(p.getName(), p.getValue());
            }
        }
        for(String n : vars.keySet()){
            queries.add(0, "SET hiveconf:" + n + "=" + vars.get(n) + ";");
        }
        return true;
    }

    @Override
    public boolean execute(){
        JDBCConnection conn = null;
        try{
            conn = new JDBCConnection(template.getDataSource().getConnection());
            boolean ret = true;
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
        finally {
            try{
                if(conn != null)
                    conn.close();
            }
            catch(Exception e){
            }
        }
        return false;
    }

}
