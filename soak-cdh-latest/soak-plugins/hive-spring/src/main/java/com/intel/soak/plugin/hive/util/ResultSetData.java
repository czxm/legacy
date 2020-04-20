package com.intel.soak.plugin.hive.util;

//import org.hibernate.exception.DataException;
//import sun.jdbc.odbc.ee.ConnectionPool;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ResultSetData {

    private List<SchemaField> columns = new ArrayList<SchemaField>();
    private ResultSet rs;

    public List<SchemaField> getColumns(){
        return this.columns;
    }

    protected void setResultSet(ResultSet rs){
        this.rs = rs;
    }

    public Iterator<List<Object>> getRowIterator(){
        return new ResultSetIterator(rs);
    }

    public void close() throws Exception {
        try{
            if(rs!=null)
                this.rs.close();
        } catch (SQLException e) {
            throw new Exception(e);
        }
    }

    class ResultSetIterator implements Iterator<List<Object>> {
        private ResultSet rs;
        private int columnCount;
        public ResultSetIterator(ResultSet rs){
            this.rs=rs;
            this.columnCount=0;
            try {
                this.columnCount=rs.getMetaData().getColumnCount();
            } catch (SQLException e) {
            }
        }

        public List<Object> next() {
            List<Object> data=new ArrayList<Object>();
            for(int i = 1; i <= this.columnCount; i++){
                try {
                    data.add(rs.getObject(i));
                } catch(SQLException e) {
                    String message = e.getMessage();
                    data.add( message != null ? message : new String("ERROR"));
                }
            }

            return data;
        }

        @Override
        public void remove() {
            throw new RuntimeException("Not Supported");
        }

        public boolean hasNext() {
            try {
                return this.rs.next();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }

        public boolean setPosition(int row) throws Exception{
            try {
                return this.rs.absolute(row);
            } catch (SQLException e) {
                throw new Exception(e);
            }
        }
    }
}
