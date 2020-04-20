package com.intel.soak.logger;

public interface TransactionLogger extends SoakLogger{

    public void setUser(String user);
    public String getUser();

}
