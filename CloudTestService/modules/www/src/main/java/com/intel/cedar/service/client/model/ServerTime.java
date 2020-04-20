package com.intel.cedar.service.client.model;

public class ServerTime extends CedarBaseModel {

    private static final long serialVersionUID = 1L;

    public static long timeZoneDelta;
    public static long clockDelta;

    private long timeMillis;
    private long offset;

    /**
     * For serialization
     */
    public ServerTime() {
    }

    public ServerTime(long utc, long offset) {
        this.timeMillis = utc;
        this.offset = offset;
    }

    /**
     * System.currentTimeMillis() at server
     */
    public long getTimeMillis() {
        return timeMillis;
    }

    /**
     * calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET) at
     * server
     */
    public long getOffset() {
        return offset;
    }

    public static long getTimeZoneDelta() {
        return timeZoneDelta + clockDelta;
    }

    public static long getServerTime() {
        return System.currentTimeMillis() - timeZoneDelta - clockDelta;
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

}
