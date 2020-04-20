package com.intel.splat.identityservice.utils;


import org.joda.time.DateTime;

public class CurrDateTime {
	private static DateTime _currDateTime = null;
	
	/**
     * Get the current dateTime,
     * If @update is true, return updated time,
     * otherwise return the stored dateTime
     * @param update - whether need to update current time
     * @return
     */
    public static DateTime getCurrDateTime(boolean update) {
        if (update) {
            _currDateTime = new DateTime();
        }
        return _currDateTime;
    }
}
