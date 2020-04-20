package com.intel.soak.commons;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A utility class for manage timed command execution
 */
public class TimerFactory {
    private ArrayList<Timer> timerList = null;
    private ArrayList<Boolean> timerStatusList = null;
    private static TimerFactory instance = null;
    private TimerFactory() {
        timerList = new ArrayList<Timer>();
        timerStatusList = new ArrayList<Boolean>();
    }

    public static synchronized TimerFactory getInstance() {
        if (instance == null) {
            instance = new TimerFactory();
        }
        return instance;
    }

    private synchronized int findFreeTimer() {
        int index = 0;
        boolean isFind = false;
        for (int i = 0; i < timerList.size(); i++) {
            if (timerStatusList.get(i)) {
                isFind = true;
                index = i;
                break;
            }
        }
        if (!isFind) {
            timerList.add(new Timer());
            timerStatusList.add(true);
        }
        return index;
    }

    public Timer schedule(final TimerTask tt, long delay) {
        final int index = findFreeTimer();
        timerStatusList.set(index, false);
        Timer t = timerList.get(index);
        t.schedule(new TimerTask() {
            public void run() {
                tt.run();
                timerStatusList.set(index, true);
            }
        }, delay);
        return t;
    }

    public Timer scheduleRepeat(TimerTask tt, long startTime, long period) {
        int index = findFreeTimer();
        timerStatusList.set(index, false);
        Timer t = timerList.get(index);
        t.schedule(tt, startTime, period);
        return t;
    }

    public void cancelAllTimer(){
        for(Timer timer : timerList){
            if(timer!=null){
                timer.cancel();
            }
        }
    }
}