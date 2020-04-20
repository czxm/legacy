package com.intel.bigdata.agent.AppExecutors;

import com.intel.bigdata.common.protocol.AppAction;

/**
 * Created with IntelliJ IDEA.
 * User: jzhu61
 * Date: 11/1/13
 * Time: 3:46 PM
 * To change this template use File | Settings | File Templates.
 */
public interface AppSequExecutor {

    public Object executor(AppAction payload);

}
