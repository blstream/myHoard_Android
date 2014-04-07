package com.myhoard.app.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Description
 *
 * @author gohilukk
 *         Date: 03.04.14
 */
public class Media implements IModel {

    private String id;

    private byte[] file;

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public Media(byte[] file) {
        this.file = file;
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
        return null;
    }
}