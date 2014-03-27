package com.myhoard.app.model;

/*
 * Created by Czyz on 2014-03-26.
 */
public class RowItem {

    private String title;
    private int imageId;


    public RowItem(){}


    public RowItem(String title, int icon)
    {
        this.title = title;
        this.imageId = icon;
    }



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int icon) {
        this.imageId = icon;
    }



}
