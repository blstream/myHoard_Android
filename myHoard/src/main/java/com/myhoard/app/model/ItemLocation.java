package com.myhoard.app.model;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Marcin Łaszcz
 *         Date: 09.04.14
 */
public class ItemLocation {
    @SerializedName("lat")
    public float lat;
    @SerializedName("lng")
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