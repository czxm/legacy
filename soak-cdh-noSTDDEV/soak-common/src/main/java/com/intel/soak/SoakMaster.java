/**
 * 
 */
package com.intel.soak;

import com.intel.soak.model.LoadConfig;

/**
 * @author xzhan27
 *
 */
public interface SoakMaster {
    void run(LoadConfig config);
    void kill();
}
