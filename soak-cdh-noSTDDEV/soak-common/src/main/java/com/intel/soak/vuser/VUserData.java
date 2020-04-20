/**
 * 
 */
package com.intel.soak.vuser;

import java.io.Serializable;

/**
 * User Data represents user specified data such as credential, properties etc. 
 * Only username is saved for the default implementation. <p> 
 * It could be extended via a customized VUserFeeder. <p>
 * One possible usage for VUserData is to accomplish transaction communication by
 * putting synchronization primitive in VUserData object <p>
 * @author xzhan27
// * @see VUserFeeder
 */
public class VUserData implements Serializable{
    private String username;
    public String getUsername(){
        return username;
    }
    public void setUsername(String user){
        this.username = user;
    }
}
