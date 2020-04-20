package com.intel.soak;

/**
 * Created with IntelliJ IDEA.
 * User: xzhan27
 * Date: 12/4/13
 * Time: 1:00 PM
 */
public class SoakException extends Exception {
    public SoakException() {
        super("General Soak Exception");
    }

    public SoakException(String msg){
        super(msg);
    }

    public SoakException(Throwable t){
        super(t);
    }
}
