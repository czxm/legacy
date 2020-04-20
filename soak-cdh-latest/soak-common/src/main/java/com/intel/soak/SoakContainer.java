package com.intel.soak;

import com.intel.soak.model.LoadConfig;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: xzhan27
 * Date: 12/4/13
 * Time: 12:52 PM
 */
public interface SoakContainer {
    public String submit(LoadConfig config) throws SoakException;
    public Set<String> list();
    public <T>T retrieve(String id);
    public void remove(String id);
}
