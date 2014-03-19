package com.myhoard.app.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.myhoard.app.httpengine.HttpEngine;
import com.myhoard.app.model.Collection;
import com.myhoard.app.model.UserSingleton;
import com.myhoard.app.provider.DataProvider;
import com.myhoard.app.provider.DataStorage.Collections;

import java.util.ArrayList;
import java.util.List;

/**
 * Description
 *
 * @author gohilukk
 *         Date: 19.03.14
 */
public class SynchronizeService extends IntentService {

    public static final String URL = "http://78.133.154.18:8080/collections/";
    UserSingleton userSingleton;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SynchronizeService(String name) {
        super(name);
    }

    public SynchronizeService() {
        super("Synchronizacja");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();

        userSingleton = UserSingleton.getInstance();
        if (userSingleton.user != null) {
            HttpEngine<Collection> collections = new HttpEngine<>(URL);


            List<Collection> localCollections = new ArrayList<Collection>();
            localCollections = getLocalCollections();

            for (Collection c : localCollections) {
                collections.create(c,userSingleton.token);
            }
        }
    }

    protected List<Collection> getLocalCollections() {
        List<Collection> localCollections = new ArrayList<Collection>();
        DataProvider dataProvider = new DataProvider();
        Cursor c = getContentResolver().query(Collections.CONTENT_URI, null, null, null, null);
        while (c.moveToNext()) {
            //do {
            String id = c.getString(c.getColumnIndex(Collections._ID));
            String name = c.getString(c.getColumnIndex(Collections.NAME));
            String desc = c.getString(c.getColumnIndex(Collections.DESCRIPTION));
            String date = c.getString(c.getColumnIndex(Collections.CREATED_DATE));
            String credate = c.getString(c.getColumnIndex(Collections.CREATED_DATE));
            String moddate = c.getString(c.getColumnIndex(Collections.MODIFIED_DATE));
            String tags = c.getString(c.getColumnIndex(Collections.TAGS));
            String servers = c.getString(c.getColumnIndex(Collections.SERVERS));
            Log.d("TAG", id + " " + name + " " + desc + " " + date + " " + moddate + " " + tags + " " + servers);
            localCollections.add(new Collection(name, desc, null, null, null, credate, moddate));
        }
        return localCollections;
    }
}