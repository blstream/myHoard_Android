package com.myhoard.app.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Interface dla klas korzystających z CRUD-a
 *
 * @author Marcin Łaszcz
 *         Date: 18.03.14
 */
public interface IModel {
    public JSONObject toJson() throws JSONException;
}
