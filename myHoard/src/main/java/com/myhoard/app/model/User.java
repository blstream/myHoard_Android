package com.myhoard.app.model;

/**
 * Description
 *
 * @author gohilukk
 *         Date: 18.03.14
 */
 public class User {
    private String username;
    private String email;
    private String password;
    private String authentication_code;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthentication_code() {
        return authentication_code;
    }

    public void setAuthentication_code(String authentication_code) {
        this.authentication_code = authentication_code;
    }
}
