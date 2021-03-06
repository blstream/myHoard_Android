package com.myhoard.app.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Description
 *
 * @author Tomasz Nosal
 *         Date: 18.03.14
 */
 public class User implements IModel {
    private String id;
    private String username;
    private String email;
    private String password;

    private static final String USERNAME = "username";
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";

    public User() {
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

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

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();

        json.put(USERNAME, getUsername());
        json.put(EMAIL, getEmail());
        json.put(PASSWORD, getPassword());

        return json;
    }
}
