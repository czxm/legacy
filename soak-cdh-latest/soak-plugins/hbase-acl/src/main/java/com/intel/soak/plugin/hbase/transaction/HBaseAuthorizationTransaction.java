package com.intel.soak.plugin.hbase.transaction;


import com.intel.soak.plugin.hbase.util.HBaseHelper;
import com.intel.soak.transaction.AbstractTransaction;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.coprocessor.AggregationClient;
import org.apache.hadoop.hbase.security.User;
import org.apache.hadoop.hbase.security.access.Permission;
import org.apache.hadoop.hbase.util.Bytes;

import java.security.PrivilegedExceptionAction;

/**
 * Created with IntelliJ IDEA.
 * User: root
 * Date: 7/29/14
 * Time: 8:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class HBaseAuthorizationTransaction extends AbstractTransaction {

    private HBaseHelper hBaseHelper;
    private Configuration configuration;

    final byte[] TEST_ROW1 = Bytes.toBytes("row1");
    final byte[] TEST_Q1 = Bytes.toBytes("q1");
    final byte[] TEST_Q2 = Bytes.toBytes("q2");
    final byte[] USER1_VALUE1 = Bytes.toBytes("user1_val1");
    final byte[] USER1_VALUE2 = Bytes.toBytes("user1_val2");
    final byte[] USER2_VALUE1 = Bytes.toBytes("user2_val1");
    final String TEST_TABLE = getUserData().getUsername()+"_"+"testtable";
    final String TEST_FAMILITY = "Colfam";


    final User user1 = User.createUserForTesting(configuration,"user1",new String[0]);
    final User user2 = User.createUserForTesting(configuration,"user2",new String[0]);
    final User user3 = User.createUserForTesting(configuration,"user3",new String[0]);

    @Override
    public boolean startup() {
        try {
            configuration = HBaseConfiguration.create();
            hBaseHelper = HBaseHelper.getHelper(configuration);
        } catch (Exception e){
            e.printStackTrace();
        }
        return  true;
    }

    @Override
    public boolean execute() {
        try {
            sameUserACLTest();
            diffUserWithPermission();
            sameUserWithHighPermission();
            sameUserACLDeleteTest();
            diffUserACLDeleteTest();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        try{
            diffUserNotWithPermission();
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            aCLNoPermissionTest();
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            aCLColumnPermTest();
        }catch (Exception e){
            e.printStackTrace();
        }

        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void sameUserACLTest() throws Exception{

        hBaseHelper.dropTable(TEST_TABLE);
        hBaseHelper.createTable(TEST_TABLE, TEST_FAMILITY);

        user1.runAs(new PrivilegedExceptionAction<Void>() {
            @Override
            public Void run() throws Exception{
                HTable hTable = new HTable(configuration,TEST_TABLE);
                Put put = new Put(TEST_ROW1);
                put.add(TEST_FAMILITY.getBytes(),TEST_Q1,USER1_VALUE1);
                put.setACL(user1.getShortName(),new Permission(Permission.Action.READ,Permission.Action.WRITE));
                hTable.put(put);
                return null;
            }
        }) ;

        user1.runAs(new PrivilegedExceptionAction<Void>() {
            @Override
            public Void run() throws Exception{
                HTable hTable = new HTable(configuration,TEST_TABLE);
                Put put = new Put(TEST_ROW1);
                put.add(TEST_FAMILITY.getBytes(),TEST_Q1,USER1_VALUE2);
                hTable.put(put);
                return null;
            }
        }) ;

    }

    public void diffUserWithPermission() throws Exception{

        hBaseHelper.dropTable(TEST_TABLE);
        hBaseHelper.createTable(TEST_TABLE, TEST_FAMILITY);

        user1.runAs(new PrivilegedExceptionAction<Void>() {
            @Override
            public Void run() throws Exception{
                HTable hTable = new HTable(configuration,TEST_TABLE);
                Put put = new Put(TEST_ROW1);
                put.add(TEST_FAMILITY.getBytes(),TEST_Q1,"test".getBytes());
                put.setACL(user2.getShortName(),new Permission(Permission.Action.READ,Permission.Action.WRITE));
                hTable.put(put);
                return null;
            }
        }) ;

        user2.runAs(new PrivilegedExceptionAction<Void>() {
            @Override
            public Void run() throws Exception{
                HTable hTable = new HTable(configuration,TEST_TABLE);
                Put put = new Put(TEST_ROW1);
                put.add(TEST_FAMILITY.getBytes(),TEST_Q1,USER2_VALUE1);
                hTable.put(put);
                return null;
            }
        }) ;

    }

    public void diffUserNotWithPermission() throws Exception{

        hBaseHelper.dropTable(TEST_TABLE);
        hBaseHelper.createTable(TEST_TABLE, TEST_FAMILITY);

        user1.runAs(new PrivilegedExceptionAction<Void>() {
            @Override
            public Void run() throws Exception{
                HTable hTable = new HTable(configuration,TEST_TABLE);
                Put put = new Put(TEST_ROW1);
                put.add(TEST_FAMILITY.getBytes(),TEST_Q1,"test".getBytes());
                put.setACL(user2.getShortName(),new Permission(Permission.Action.READ,Permission.Action.WRITE));
                hTable.put(put);
                return null;
            }
        }) ;

        user3.runAs(new PrivilegedExceptionAction<Void>() {
            @Override
            public Void run() throws Exception{
                HTable hTable = new HTable(configuration,TEST_TABLE);
                Put put = new Put(TEST_ROW1);
                put.add(TEST_FAMILITY.getBytes(),TEST_Q1,USER2_VALUE1);
                hTable.put(put);
                return null;
            }
        }) ;

    }


    public void sameUserWithHighPermission() throws Exception{

        hBaseHelper.dropTable(TEST_TABLE);
        hBaseHelper.createTable(TEST_TABLE, TEST_FAMILITY);

        user1.runAs(new PrivilegedExceptionAction<Void>() {
            @Override
            public Void run() throws Exception{
                HTable hTable = new HTable(configuration,TEST_TABLE);
                Put put = new Put(TEST_ROW1);
                put.add(TEST_FAMILITY.getBytes(),TEST_Q1,USER1_VALUE1);
                put.setACL(user2.getShortName(),new Permission(Permission.Action.READ,Permission.Action.WRITE));
                hTable.put(put);
                return null;
            }
        }) ;

        user1.runAs(new PrivilegedExceptionAction<Void>() {
            @Override
            public Void run() throws Exception{
                HTable hTable = new HTable(configuration,TEST_TABLE);
                Put put = new Put(TEST_ROW1);
                put.add(TEST_FAMILITY.getBytes(),TEST_Q1,USER1_VALUE2);
                hTable.put(put);
                return null;
            }
        }) ;

    }

    public void sameUserACLDeleteTest() throws Exception{

        hBaseHelper.dropTable(TEST_TABLE);
        hBaseHelper.createTable(TEST_TABLE, TEST_FAMILITY);

        user1.runAs(new PrivilegedExceptionAction<Void>() {
            @Override
            public Void run() throws Exception{
                HTable hTable = new HTable(configuration,TEST_TABLE);
                Put put = new Put(TEST_ROW1);
                put.add(TEST_FAMILITY.getBytes(),TEST_Q1,USER1_VALUE1);
                put.setACL(user1.getShortName(),new Permission(Permission.Action.READ,Permission.Action.WRITE));
                hTable.put(put);
                return null;
            }
        }) ;

        user1.runAs(new PrivilegedExceptionAction<Void>() {
            @Override
            public Void run() throws Exception{
                HTable hTable = new HTable(configuration,TEST_TABLE);
                Delete delete = new Delete(TEST_ROW1);
                hTable.delete(delete);
                return null;
            }
        }) ;

    }


    public void diffUserACLDeleteTest() throws Exception{

        hBaseHelper.dropTable(TEST_TABLE);
        hBaseHelper.createTable(TEST_TABLE, TEST_FAMILITY);

        user1.runAs(new PrivilegedExceptionAction<Void>() {
            @Override
            public Void run() throws Exception{
                HTable hTable = new HTable(configuration,TEST_TABLE);
                Put put = new Put(TEST_ROW1);
                put.add(TEST_FAMILITY.getBytes(),TEST_Q1,USER1_VALUE1);
                put.setACL(user2.getShortName(),new Permission(Permission.Action.READ,Permission.Action.WRITE));
                hTable.put(put);
                return null;
            }
        }) ;

        user2.runAs(new PrivilegedExceptionAction<Void>() {
            @Override
            public Void run() throws Exception{
                HTable hTable = new HTable(configuration,TEST_TABLE);
                Delete delete = new Delete(TEST_ROW1);
                hTable.delete(delete);
                return null;
            }
        }) ;
    }


    public void aCLNoPermissionTest() throws Exception{

        hBaseHelper.dropTable(TEST_TABLE);
        hBaseHelper.createTable(TEST_TABLE, TEST_FAMILITY);

        user1.runAs(new PrivilegedExceptionAction<Void>() {
            @Override
            public Void run() throws Exception{
                HTable hTable = new HTable(configuration,TEST_TABLE);
                Put put = new Put(TEST_ROW1);
                put.add(TEST_FAMILITY.getBytes(),TEST_Q1,USER1_VALUE1);
                put.setACL(user2.getShortName(),new Permission(Permission.Action.READ,Permission.Action.WRITE));
                hTable.put(put);
                return null;
            }
        }) ;

        user3.runAs(new PrivilegedExceptionAction<Void>() {
            @Override
            public Void run() throws Exception{
                HTable hTable = new HTable(configuration,TEST_TABLE);
                Delete delete = new Delete(TEST_ROW1);
                hTable.delete(delete);
                return null;
            }
        }) ;
    }



    public void aCLColumnPermTest() throws Exception{

        hBaseHelper.dropTable(TEST_TABLE);
        hBaseHelper.createTable(TEST_TABLE, TEST_FAMILITY);

        user1.runAs(new PrivilegedExceptionAction<Void>() {
            @Override
            public Void run() throws Exception{
                HTable hTable = new HTable(configuration,TEST_TABLE);
                Put put = new Put(TEST_ROW1);
                put.add(TEST_FAMILITY.getBytes(),TEST_Q1,USER1_VALUE1);
                put.add(TEST_FAMILITY.getBytes(),TEST_Q2,USER1_VALUE1);
                put.setACL(user2.getShortName(),new Permission(Permission.Action.READ,Permission.Action.WRITE));
                hTable.put(put);
                return null;
            }
        }) ;

        user2.runAs(new PrivilegedExceptionAction<Void>() {
            @Override
            public Void run() throws Exception{
                HTable hTable = new HTable(configuration,TEST_TABLE);
                Delete delete = new Delete(TEST_ROW1);
                delete.deleteColumn(TEST_FAMILITY.getBytes(),TEST_Q1);
                hTable.delete(delete);
                return null;
            }
        }) ;
    }
}
