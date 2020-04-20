package com.intel.cedar.engine.model.feature;

import com.intel.cedar.engine.model.IDataModelDocument;

public class ScmTrigger extends Trigger {
    protected String url;
    protected String url_bind;
    protected String rev;
    protected String rev_bind;
    protected String user;
    protected String user_bind;
    protected String password;
    protected String password_bind;
    protected String repo_name;
    protected String repo_bind;
    protected String interval;

    public ScmTrigger(IDataModelDocument document) {
        super(document);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl_bind() {
        return url_bind;
    }

    public void setUrl_bind(String urlBind) {
        url_bind = urlBind;
    }

    public String getRev() {
        return rev;
    }

    public void setRev(String rev) {
        this.rev = rev;
    }

    public String getRev_bind() {
        return rev_bind;
    }

    public void setRev_bind(String revBind) {
        rev_bind = revBind;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUser_bind() {
        return user_bind;
    }

    public void setUser_bind(String userBind) {
        user_bind = userBind;
    }

    public String getPassword_bind() {
        return password_bind;
    }

    public void setPassword_bind(String passwordBind) {
        password_bind = passwordBind;
    }

    public String getRepoName() {
        return repo_name;
    }

    public void setRepoName(String repoName) {
        repo_name = repoName;
    }

    public String getRepo_bind() {
        return repo_bind;
    }

    public void setRepo_bind(String repoBind) {
        repo_bind = repoBind;
    }
    
    public Integer getIntervalAsInteger(){
        try{
            return Integer.parseInt(this.interval);
        }
        catch(Exception e){
            return 0;
        }
    }
    
    public String getInterval(){
        return this.interval;
    }
    
    public void setInterval(String interval){
        this.interval = interval;
    }
}
