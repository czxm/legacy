package com.intel.cedar.core;

public abstract class DatabaseBootstrapper implements Bootstrapper {
    public abstract void start();

    protected abstract void preStart() throws Exception;

    protected abstract void postStart() throws Exception;

    protected abstract void initialize() throws Exception;
}
