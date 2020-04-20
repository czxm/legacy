package com.intel.cedar.agent.runtime;

import java.io.BufferedWriter;
import java.util.concurrent.atomic.AtomicBoolean;

public class HeartBeat extends Thread {
    static final int HEARTBEAT_INTERVAL = 15 * 1000;
    private BufferedWriter bufferedWriter;
    private AtomicBoolean writtenHappened;
    private boolean terminate = false;

    public HeartBeat(BufferedWriter bufferedWriter,
            AtomicBoolean writtenHappened) {
        this.writtenHappened = writtenHappened;
        this.bufferedWriter = bufferedWriter;
    }

    @Override
    public void run() {
        while (!terminate) {
            try {
                Thread.sleep(HEARTBEAT_INTERVAL);
                if (!terminate && !writtenHappened.get()) {
                    bufferedWriter.write("@@CedarHeartBeat@@");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            } catch (Exception e) {
                break;
            }
        }
    }

    public void shutdown() {
        terminate = true;
        try{
            this.join(HEARTBEAT_INTERVAL * 2);
        }
        catch(Exception e){            
        }
    }
}