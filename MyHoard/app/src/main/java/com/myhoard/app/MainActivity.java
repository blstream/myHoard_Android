package com.myhoard.app;

import android.content.ContentValues;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import com.myhoard.app.provider.DataStorage.*;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createCollection("Kapsle","Moje fajne kapsle","/galery/kapsle.jpg","1,2");
        createElement(1,"Tymbark","Moj najlepszy kapsel", "/galery/kapsle/tymbark.jpg", "1,2");
    }

    private void createCollection(String name, String description, String avatarFileName, String tags) {
        ContentValues values = new ContentValues();
        values.put(Collections.NAME, name);
        values.put(Collections.DESCRIPTION, description);
        values.put(Collections.AVATAR_FILE_NAME, avatarFileName);
        values.put(Collections.TAGS, tags);

        getContentResolver().insert(Collections.CONTENT_URI, values);
    }

    private void createElement(Integer collectionId, String name, String description, String avatarFileName, String tags) {
        ContentValues values = new ContentValues();
        values.put(Elements.NAME, name);
        values.put(Elements.COLLECTION_ID, collectionId);
        values.put(Elements.DESCRIPTION, description);
        values.put(Elements.AVATAR_FILE_NAME, avatarFileName);
        values.put(Elements.TAGS, tags);

        getContentResolver().insert(Elements.CONTENT_URI,values);
    }

}
