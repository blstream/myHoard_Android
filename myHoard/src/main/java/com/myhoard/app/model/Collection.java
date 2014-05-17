package com.myhoard.app.model;

import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Description
 *
 * @author Tomasz Nosal
 *         Date: 17.03.14
 */
public class Collection implements IModel {

    private String id;
    private String name;
    private String description;
    private List<String> tags;
    private String owner;
    @SerializedName("public")
    private Boolean ifPublic;
    private String items_number;
    private String created_date;
    private String modified_date;

    public Collection(String name, String description, List<String> tags, String owner, String items_number, String created_date, String modified_date) {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.owner = owner;
        this.items_number = items_number;
        this.created_date = created_date;
        this.modified_date = modified_date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getItems_number() {
        return items_number;
    }

    public void setItems_number(String items_number) {
        this.items_number = items_number;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getCreated_date() {
        return created_date;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }

    public String getModified_date() {
        return modified_date;
    }

    public void setModified_date(String modified_date) {
        this.modified_date = modified_date;
    }

    public Boolean getIfPublic() {
        return ifPublic;
    }

    public void setIfPublic(Boolean ifPublic) {
        this.ifPublic = ifPublic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection () {

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append(id + ", ")
                .append(name + ", ")
                .append(description + ", ")
                .append(tags + ", ")
                .append(owner + ", ")
                .append(items_number + ", ")
                .append(created_date + ", ")
                .append(modified_date);
        return sb.toString();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id =  id;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        final String jsonOwner = "owner";
        final String jsonName = "name";
        final String jsonDescription = "description";
        final String jsonTags = "tags";
        final String jsonPublic = "public";
        final String jsonItemsNumber = "items_number";
        final String jsonCreatedDate = "created_date";
        final String jsonModifiedDate = "modified_date";

        JSONObject json = new JSONObject();
        json.put(jsonOwner, getOwner());
        json.put(jsonName, getName());
        json.put(jsonDescription, getDescription());
        if (getTags() != null) {
            JSONArray arrayTags = new JSONArray();
            for (String s : getTags()) {
                arrayTags.put(s);
            }
            json.put(jsonTags, arrayTags);
        }
        json.put(jsonPublic, getIfPublic());
        json.put(jsonItemsNumber, getItems_number());
        json.put(jsonCreatedDate, getCreated_date());
        json.put(jsonModifiedDate, getModified_date());

        return json;
    }
}
