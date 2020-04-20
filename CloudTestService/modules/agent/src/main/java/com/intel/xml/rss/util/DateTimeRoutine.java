package com.intel.xml.rss.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeRoutine {

    public static String millisToDuration(long timeMillis) {
        int millis = (int) (timeMillis) % 1000;
        timeMillis /= 1000;
        int seconds = (int) (timeMillis) % 60;
        timeMillis /= 60;
        int minutes = (int) (timeMillis) % 60;
        timeMillis /= 60;
        int hours = (int) (timeMillis % 24);
        timeMillis /= 24;
        int day = (int) timeMillis;

        StringBuilder sb = new StringBuilder();
        if (day > 0) {
            sb.append(day).append(" day(s) ");
        }
        if (hours > 0) {
            sb.append(hours).append(" hour(s) ");
        }
        if (minutes > 0) {
            sb.append(minutes).append(" minute(s) ");
        }
        sb.append(seconds).append(" second(s) ");
        sb.append(millis).append(" millis");
        return new String(sb);
    }
    
    public static String millisToShortDuration(long timeMillis) {
        int millis = (int) (timeMillis) % 1000;
        timeMillis /= 1000;
        int seconds = (int) (timeMillis) % 60;
        timeMillis /= 60;
        int minutes = (int) (timeMillis) % 60;
        timeMillis /= 60;
        int hours = (int) (timeMillis % 24);
        timeMillis /= 24;
        int day = (int) timeMillis;

        StringBuilder sb = new StringBuilder();
        if (day > 0) {
            sb.append(day).append(" day(s) ");
        }
        if (hours > 0) {
            sb.append(hours).append(" hour(s) ");
        }
        if (minutes > 0) {
            sb.append(minutes).append(" minute(s) ");
        }
        sb.append(seconds).append(" second(s) ");
        return new String(sb);
    }

    public static int millisToDurationDays(long timeMillis) {
        int millis = (int) (timeMillis) % 1000;
        timeMillis /= 1000;
        int seconds = (int) (timeMillis) % 60;
        timeMillis /= 60;
        int minutes = (int) (timeMillis) % 60;
        timeMillis /= 60;
        int hours = (int) (timeMillis % 24);
        timeMillis /= 24;
        return (int) timeMillis;
    }

    public static String millisToStdTimeString(long timeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(timeMillis));
    }

    public static String dateToStdTimeString(Date date) {
        return millisToStdTimeString(date.getTime());
    }

    public static long stdTimeStringToMillis(String str) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.parse(str).getTime();
    }

    public static Date stdTimeStringToDate(String str) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.parse(str);
    }

}
