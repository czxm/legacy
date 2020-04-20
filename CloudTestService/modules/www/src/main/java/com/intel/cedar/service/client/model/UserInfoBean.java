package com.intel.cedar.service.client.model;

public class UserInfoBean extends CedarBaseModel {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private Long id;
    private String userName;
    private String password;
    private String email;
    private Boolean admin;

    public UserInfoBean() {

    }

    public UserInfoBean(String userName, String password, String email,
            Boolean admin) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.admin = admin;
    }

    @Override
    public void refresh() {
        set("Id", id);
        set("UserName", userName);
        set("Password", password);
        set("Email", email);
        set("Admin", admin);
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
        set("Admin", admin);
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setEmail(String email) {
        this.email = email;
        set("Email", email);
    }

    public String getEmail() {
        return email;
    }

    public void setPassword(String password) {
        this.password = password;
        set("Password", password);
    }

    public String getPassword() {
        return password;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        set("UserName", userName);
    }

    public String getUserName() {
        return userName;
    }

    public String toString() {
        return "[ " + "userName: " + userName + " " + "email: " + email + " "
                + "admin: " + admin + " " + "]";
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
