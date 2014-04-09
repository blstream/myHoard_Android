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

    public JSONObject toJson() throws JSONException {
        final String API_ID = "id";
        final String API_URL = "url";

        JSONObject json = new JSONObject();

        json.put(API_ID, id);
        json.put(API_URL, url);

        return json;
    }
}