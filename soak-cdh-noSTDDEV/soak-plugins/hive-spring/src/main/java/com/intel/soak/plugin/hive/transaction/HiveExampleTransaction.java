package com.intel.soak.plugin.hive.transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.intel.soak.model.ParamType;
import org.springframework.core.io.ClassPathResource;

import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.plugin.hive.HiveScript;

@Plugin( desc = "Hive Example", type = PLUGIN_TYPE.TRANSACTION )
public class HiveExampleTransaction extends HiveThriftTransaction {

    @Override
    public boolean startup(){
        ParamType p = new ParamType();
        p.setName("script");
        p.setValue("example/run.hql");
        this.params.add(p);
        return super.startup();
    }

}
