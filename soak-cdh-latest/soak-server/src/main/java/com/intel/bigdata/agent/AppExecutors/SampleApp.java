package com.intel.bigdata.agent.AppExecutors;

import com.intel.bigdata.common.protocol.AppAction;
import com.intel.bigdata.common.protocol.HelloAkkaMessage;
import com.intel.bigdata.common.protocol.IMAgentMessage;

/**
 * Created with IntelliJ IDEA.
 * User: jzhu61
 * Date: 11/4/13
 * Time: 10:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class SampleApp implements AppSequExecutor{
    public String msg;

    @Override
    public Object executor(AppAction payload) {
        IMAgentMessage immsg = null;
        /*
        if(payload instanceof HelloAkkaMessage){
            HelloAkkaMessage pl = (HelloAkkaMessage)payload;

            msg = pl.getMessage() + "Reply: Hi, I AM AGENT!";
            immsg = new IMAgentMessage(pl.getId(), msg);
        }
         */
        return immsg;
    }
}
