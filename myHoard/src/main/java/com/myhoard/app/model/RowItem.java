package com.myhoard.app.model;

/*
 * Created by Mateusz Czyszkiewicz on 2014-03-26.
 */
public class RowItem {

    private String title;
    private int imageId;

    public RowItem(String title, int icon)
    {
        this.title = title;
        this.imageId = icon;
    }

    public String getTitle() {
        return title;
    }
    public int getImageId() {
        return imageId;
    }
}
