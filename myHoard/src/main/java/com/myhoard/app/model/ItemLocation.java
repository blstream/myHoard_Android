package com.myhoard.app.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Marcin Łaszcz
 *         Date: 09.04.14
 */
public class ItemLocation {
    public float lat;
    public float lng;

    public ItemLocation(float lat, float lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public ItemLocation() {
        this.lat = 0;
        this.lng = 0;
    }

    public JSONObject toJson() throws JSONException {
        final String API_LAT = "lat";
        final String API_LNG = "lng";

        JSONObject json = new JSONObject();

        json.put(API_LAT, lat);
        json.put(API_LNG, lng);

        return json;
    }
}