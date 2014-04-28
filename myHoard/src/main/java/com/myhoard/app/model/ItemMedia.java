package com.myhoard.app.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Marcin Łaszcz
 *         Date: 09.04.14
 */
public class ItemMedia {
    public String id;
    public String url;

    public ItemMedia(String url, String id) {
        this.url = url;
        this.id = id;
    }

    public ItemMedia(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public JSONObject toJson() throws JSONException {
        final String API_ID = "id";
        final String API_URL = "url";

        JSONObject json = new JSONObject();

        json.put(API_ID, id);
        json.put(API_URL, url);

        return json;
    }
}