package com.intel.cedar.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@PersistenceContext(name = "cedar_general")
@Table(name = "sessions")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SessionInfo {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id = -1L;
    @Column(name = "sessionid")
    private String sessionid;
    @Column(name = "userid")
    private Long userid;
    @Column(name = "timestamp")
    private Long timestamp;
    
    public SessionInfo() {

    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public Long getUserid() {
        return userid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public String getSessionid() {
        return sessionid;
    }
    
    public Long getTimestamp(){
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp){
        this.timestamp = timestamp;
    }

    public String toString() {
        return "[sessionid: " + sessionid + ", userid: " + userid + "]";
    }
}
