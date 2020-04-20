package com.intel.cedar.features.IDH;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;



public class DateTimeUtils {
	public static Date currentTime() {
		  Calendar c = Calendar.getInstance();
		  c.setTimeInMillis(new Date().getTime());
		  return c.getTime();
	}
	
	public static String getCurrentTimeStringByFormat(String formatStr){
		Date currentDate = DateTimeUtils.currentTime();
		SimpleDateFormat dateFormat = new SimpleDateFormat(formatStr);
		return dateFormat.format(currentDate);
	}
}
