/**
 * 
 */
package com.intel.soak.vuser;

import com.intel.soak.plugin.Pluggable;
import com.intel.soak.vuser.VUserData;

/**
 * Pluggable object to create customized UserData 
 * @author xzhan27
 * @see VUserData
 */
public interface VUserFeeder extends Pluggable {
    /** 
     * Create a new UserData
     * @param userindex    the user index
     * @return UserData
     */
    public VUserData feedUser(int userindex);
}
