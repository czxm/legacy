package com.intel.soak.plugin.hbase.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: root
 * Date: 7/29/14
 * Time: 8:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class HBaseHelper {

    private Configuration configuration = null;

    private HBaseAdmin hBaseAdmin = null;

    protected HBaseHelper(Configuration configuration) throws IOException {
        this.configuration = configuration;
        this.hBaseAdmin = new HBaseAdmin(configuration);
    }

    public static HBaseHelper getHelper(Configuration configuration) throws IOException{
        return new HBaseHelper(configuration);
    }

    public boolean existsTable(String table) throws IOException{
        return hBaseAdmin.tableExists(table);
    }

    public void createTable(String table, String... colfams) throws IOException {
        createTable(table, null, colfams);
    }

    public void createTable(String table, byte[][] splitKeys, String... colfams) throws IOException {
        HTableDescriptor desc = new HTableDescriptor(table);
        for (String cf : colfams) {
            HColumnDescriptor coldef = new HColumnDescriptor(cf);
            desc.addFamily(coldef);
        }
        if (splitKeys != null) {
            hBaseAdmin.createTable(desc, splitKeys);
        } else {
            hBaseAdmin.createTable(desc);
        }
    }



    public void disableTable(String table) throws IOException {
        hBaseAdmin.disableTable(table);
    }

    public void dropTable(String table) throws IOException {
        if (existsTable(table)) {
            disableTable(table);
            hBaseAdmin.deleteTable(table);
        }
    }
}
