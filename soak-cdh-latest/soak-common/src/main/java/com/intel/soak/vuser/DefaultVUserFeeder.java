/**
 * 
 */
package com.intel.soak.vuser;

import java.util.List;

import com.intel.soak.model.ParamType;
import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;

/**
 * The default implementation to feed a user with UserData <p>
 * @author xzhan27
 *
 */
@Plugin(desc="Default VUser Feeder", type = PLUGIN_TYPE.FEEDER)
public class DefaultVUserFeeder implements VUserFeeder {

    private String basename = "user";
    private int startIndex = 0;
    protected List<ParamType> params;

    protected String getParamValue(String name){
        if(params != null){
            for(ParamType param : params){
                if(param.getName().equals(name))
                    return param.getValue();
            }
        }
        return null;
    }

    @Override
    public VUserData feedUser(int userindex) {
        VUserData user = new VUserData();
        user.setUsername(basename + (startIndex + userindex));
        return user;
    }

    @Override
    public void setParams(List<ParamType> params) {
        this.params = params;

        String v = getParamValue("basename");
        if(v != null && v.length() > 0){
            basename = v;
        }
        v = getParamValue("startIndex");
        try{
            startIndex = Integer.parseInt(v);
        }
        catch(Exception e){
        }
    }
}
