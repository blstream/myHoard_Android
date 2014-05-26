package com.myhoard.app.model;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Description
 *
 * @author gohilukk
 *         Date: 03.04.14
 */
public class Media implements IModel {

    @SerializedName("id")
    private String id;

    @SerializedName("file")
    private File file;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Media(File file) {
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
