package com.intel.soak.plugin.hive.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class JDBCConnection {
    private Connection conn;
    private DatabaseMetaData md;
    private boolean needClose;

    public JDBCConnection(Connection conn){
        this.conn=conn;
    }

    public Connection getConnection(){
        return this.conn;
    }

    public void setNeedClose(boolean need){
        this.needClose=need;
    }

    private DatabaseMetaData getMetaData() throws SQLException{
        if(null==md)
            md=conn.getMetaData();
        return md;
    }

    public boolean isClosed(){
        boolean f=true;
        try {
            if(null!=conn)
                f=conn.isClosed();
        } catch (SQLException e) {
            close();
        }
        return f;
    }

    public void close(){
        try {
            if(!conn.isClosed())
                conn.close();
        } catch (SQLException e) {
        }
    }

    private boolean hasSchemas(){
        boolean flag=false;
        ResultSet rs=null;
        try {
            DatabaseMetaData md = getMetaData();
            if(null!=md){
                rs = md.getSchemas();
                if(rs.next())
                    flag=true;
            }
        } catch (SQLException e){
        }
        finally{
            try {
                if(null!=rs)
                    rs.close();
                if(needClose)
                    close();
            } catch (SQLException e1) {
            }
        }
        return flag;
    }

    public List<String> getSchemas() throws Exception{
        List<String> tables=new ArrayList<String>();
        ResultSet rs=null;
        try {
            DatabaseMetaData md = getMetaData();
            if(null!=md){
                if(hasSchemas()){
                    rs = md.getSchemas();
                    while (rs.next()) {
                        tables.add(rs.getString("TABLE_SCHEM"));
                    }
                }
                else{
                    rs = md.getCatalogs();
                    while (rs.next()) {
                        tables.add(rs.getString("TABLE_CAT"));
                    }
                }
            }
        } catch (SQLException e){
            throw new Exception(e);
        }
        finally{
            try {
                if(null!=rs)
                    rs.close();
                if(needClose)
                    close();
            } catch (SQLException e1) {
            }
        }
        return tables;
    }

    public List<String> getViews(String schema) throws Exception{
        List<String> tables=new ArrayList<String>();
        ResultSet rs=null;
        try {
            DatabaseMetaData md = getMetaData();
            if(null!=md){
                if(hasSchemas()){
                    rs = md.getTables(null, schema, null, new String[]{ "VIEW" });
                    while (rs.next()) {
                        tables.add(rs.getString(3));
                    }
                }
                else{
                    rs = md.getTables(schema, null, null, new String[]{ "VIEW" });
                    while (rs.next()) {
                        tables.add(rs.getString(3));
                    }
                }
            }
        } catch (SQLException e){
            throw new Exception(e);
        }
        finally{
            try {
                if(null!=rs)
                    rs.close();
                if(needClose)
                    close();
            } catch (SQLException e1) {
            }
        }
        return tables;
    }

    public List<String> getTables(String schema) throws Exception{
        List<String> tables=new ArrayList<String>();
        ResultSet rs=null;
        try {
            DatabaseMetaData md = getMetaData();
            if(null!=md){
                if(hasSchemas()){
                    rs = md.getTables(null, schema, null, new String[]{ "TABLE" });
                    while (rs.next()) {
                        tables.add(rs.getString(3));
                    }
                }
                else{
                    rs = md.getTables(schema, null, null, new String[]{ "TABLE" });
                    while (rs.next()) {
                        tables.add(rs.getString(3));
                    }
                }
            }
        } catch (SQLException e){
            throw new Exception(e);
        }
        finally{
            try {
                if(null!=rs)
                    rs.close();
                if(needClose)
                    close();
            } catch (SQLException e1) {
            }
        }
        return tables;
    }

    public List<SchemaField> getTableColumns(String schema, String name) throws Exception{
        List<SchemaField> columns=new ArrayList<SchemaField>();
        ResultSet rs=null;
        try {
            DatabaseMetaData md = getMetaData();
            if(null!=md){
                if(hasSchemas())
                    rs = md.getColumns(null, schema, name, null);
                else
                    rs = md.getColumns(schema, null, name, null);
                this.setTableHeader(columns, rs);
                this.setFieldKey(columns, md, schema, name);
            }
        } catch (SQLException e){
            throw new Exception(e);
        }
        finally{
            try {
                if(null!=rs)
                    rs.close();
                if(needClose)
                    close();
            } catch (SQLException e1) {
            }
        }
        return columns;
    }

    public ResultSetData getData(String statement) throws Exception{
        ResultSetData data = new ResultSetData();
        Statement stat=null;
        try {
            stat = conn.createStatement();
            ResultSet rs = stat.executeQuery(statement);
            this.setTableHeader(data.getColumns(), rs.getMetaData());
            data.setResultSet(rs);
        } catch (SQLException e) {
            throw new Exception(e);
        }
        finally{
            if(needClose)
                close();
        }
        return data;
    }

    protected void setFieldKey(List headers, DatabaseMetaData dmd, String schema, String table) throws SQLException{
        ResultSet rs=null;
        if(hasSchemas())
            rs=dmd.getPrimaryKeys(null, schema, table);
        else
            rs=dmd.getPrimaryKeys(schema, null, table);
        try{
            while(rs.next()){
                String c=rs.getString("COLUMN_NAME");
                for(int i = 0; i < headers.size(); i++){
                    SchemaField field=(SchemaField)headers.get(i);
                    if(c.equals(field.getName())){
                        field.setTableKey(true);
                    }
                }
            }
        }
        finally{
            rs.close();
        }
    }

    protected void setTableData(List<SchemaField> headers, List rows, ResultSet rs, int limits) throws SQLException{
        DatabaseMetaData dmd=getMetaData();
        if(null==rs || null==dmd)
            return;

        ResultSetMetaData md = rs.getMetaData();
        this.setTableHeader(headers, md);
        while(rs.next() && limits>0){
            List<String> data=new ArrayList<String>();
            for(int i = 1; i <= md.getColumnCount(); i++){
                data.add(rs.getString(i));
            }
            rows.add(data);
            limits--;
        }
    }

    protected void setTableHeader(List<SchemaField> headers, ResultSetMetaData md) throws SQLException{
        if(null==md)
            return;
        for(int i = 1; i <= md.getColumnCount(); i++){
            SchemaField field=new SchemaField(null);
            field.setName(md.getColumnName(i));
            field.setType(md.getColumnType(i));
            field.setLength(md.getColumnDisplaySize(i));
            field.setTypeName(md.getColumnTypeName(i));
            headers.add(field);
        }
    }

    protected void setTableHeader(List<SchemaField> headers,ResultSet rs) throws SQLException{
        if(null==rs)
            return;
        while(rs.next()){
            SchemaField field=new SchemaField(null);
            field.setName(rs.getString("COLUMN_NAME"));

            int type = rs.getInt("DATA_TYPE");
            field.setType(type);

            String typeName = rs.getString("TYPE_NAME");
            field.setTypeName(typeName);

            field.setLength(rs.getInt("COLUMN_SIZE"));
            field.setNullable(rs.getInt("NULLABLE")==DatabaseMetaData.columnNullable);
            headers.add(field);
        }
    }
}
