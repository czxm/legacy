package com.intel.soak;

/**
 * Lifecycle contract for services which wish to be notified when it is time to start.
 *
 * @author : Joshua Yao (yi.a.yao@intel.com)
 * @since : 11/1/13 1:23 AM
 */
public interface Bootable {

    void start();

    void stop();

}
