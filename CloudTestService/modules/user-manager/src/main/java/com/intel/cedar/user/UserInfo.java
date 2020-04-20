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
@Table(name = "users")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class UserInfo implements Comparable<UserInfo> {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id = -1L;
    @Column(name = "user")
    private String user;
    @Column(name = "password")
    private String password;
    @Column(name = "email")
    private String email;
    @Column(name = "admin")
    private Boolean admin;

    public UserInfo() {

    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public String toString() {
        return "[ " + "user: " + user + " " + "email: " + email + " "
                + "admin: " + admin + " " + "]";
    }

    @Override
    public int compareTo(UserInfo o) {
        return this.getId().compareTo(o.getId());
    }
}
