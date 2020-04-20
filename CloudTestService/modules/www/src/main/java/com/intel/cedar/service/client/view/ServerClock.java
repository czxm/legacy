package com.intel.cedar.service.client.view;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.intel.cedar.service.client.CloudRemoteServiceAsync;
import com.intel.cedar.service.client.RPCInvocation;
import com.intel.cedar.service.client.model.ServerTime;

public class ServerClock {
    private static ServerClock INSTANCE = new ServerClock();
    private final RPCInvocation<ServerTime> systemTimeGetter = new RPCInvocation<ServerTime>() {
        @Override
        public void onComplete(ServerTime serverTime) {
            init(serverTime);
        }

        @Override
        public void execute(CloudRemoteServiceAsync remoteService,
                AsyncCallback<ServerTime> callback) {
            remoteService.getSystemTime(callback);
        }
    };

    private final Timer ticker = new Timer() {
        @Override
        public void run() {
            ServerClock.this.tick();
        }
    };

    private final Timer launcher = new Timer() {
        @Override
        public void run() {
            ticker.run();
            ticker.scheduleRepeating(1000);
        }
    };

    private static final DateTimeFormat format = DateTimeFormat
            .getFormat("EEE, dd MMM HH:mm:ss");

    private static long lastUpdate;
    private static long previousTick;
    private Label drawPanel;
    private long now;

    public static ServerClock getInstance(){
        return INSTANCE;
    }
    
    public void setLabel(Label clock) {
        drawPanel = clock;
    }

    public long now(){
        return new Date(now - ServerTime.timeZoneDelta
                - ServerTime.clockDelta).getTime();
    }
    
    public void start() {
        systemTimeGetter.invoke(false);
    }

    void tick() {
        now = System.currentTimeMillis();
        if (now < previousTick || now > previousTick + 15 * 1000) {
            // clock skew and/or adjustment detected
            restart();
        }
        previousTick = now;

        Date serverDate = new Date(now - ServerTime.timeZoneDelta
                - ServerTime.clockDelta);
        setTime(serverDate);
        if ((now - lastUpdate) / 1000 > 60 * 5) {
            // sync up with server every 5 minutes
            restart();
        }
    }

    private void restart() {
        ticker.cancel();
        this.start();
    }

    @SuppressWarnings("deprecation")
    private static int getTimezoneOffset() {
        int offsetInMinutes = -new Date().getTimezoneOffset();
        return offsetInMinutes * 60 * 1000;
    }

    private void init(ServerTime serverTime) {
        // aligning time to local time zone as we get time for UTC time zone
        long now = System.currentTimeMillis();
        lastUpdate = now;
        previousTick = now;
        ServerTime.clockDelta = now - serverTime.getTimeMillis();
        ServerTime.timeZoneDelta = getTimezoneOffset() - serverTime.getOffset();
        int millis = (int) (serverTime.getTimeMillis() % 1000);
        if (millis > 0) { // adjust ticker to beginning of second
            launcher.schedule(1000 - millis);
        } else {
            launcher.run();
        }
    }

    private void setTime(Date time) {
        if(drawPanel != null)
            drawPanel.setText(format.format(time));
    }
}
