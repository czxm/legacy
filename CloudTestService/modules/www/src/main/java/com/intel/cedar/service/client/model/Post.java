package com.intel.cedar.service.client.model;

import java.io.Serializable;
import java.util.Date;

import com.extjs.gxt.ui.client.data.BaseTreeModel;

public class Post extends BaseTreeModel implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    protected Date dummy;

    public Post() {

    }

    public String getUsername() {
        return (String) get("username");
    }

    public void setUsername(String username) {
        set("username", username);
    }

    public String getForum() {
        return (String) get("forum");
    }

    public void setForum(String forum) {
        set("forum", forum);
    }

    public Date getDate() {
        return (Date) get("date");
    }

    public void setDate(Date date) {
        set("date", date);
    }

    public String getSubject() {
        return (String) get("subject");
    }

    public void setSubject(String subject) {
        set("subject", subject);
    }

}
