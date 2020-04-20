package com.intel.soak.utils;

import java.util.concurrent.TimeUnit;

public class ThreadUtils {
	
	public static void sleep(long seconds) {
		try {
            TimeUnit.SECONDS.sleep(seconds);
		} catch (Exception e) {
		}
	}
	
	public static void usleep(long mill){
		try {
            TimeUnit.MILLISECONDS.sleep(mill);
		} catch (Exception e){
		}
	}

}
