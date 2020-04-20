package com.intel.soak.plugin.hive.transaction;

import com.intel.bigdata.common.util.Command;
import com.intel.soak.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class HiveTPCHTransaction extends HiveCLITransaction {

    @Override
    public boolean startup(){
        if(super.startup()){
            try{
                if(queries.size() > 0){
                    FileOutputStream fos = new FileOutputStream(scriptFile);
                    for(String q : queries){
                        fos.write(q.getBytes());
                        fos.write("\n".getBytes());
                    }
                    FileInputStream ins = new FileInputStream(getParamValue("script"));
                    FileUtils.copyStream(ins, fos);
                    ins.close();
                    fos.close();
                }
            }
            catch(Exception e){
                logger.error(e.getMessage());
            }
        }
        return false;
    }
}
