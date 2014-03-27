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
    private String username;
    private String email;
    private String password;

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
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();


/* AWA:FIXME: Hardcoded value
            String "" powinien być jako stała np.
            private final static String NAZWA_STALEJ="Main"
                    */
        json.put("username", getUsername());
        json.put("email", getEmail());
        json.put("password", getPassword());

        return json;
    }
}
