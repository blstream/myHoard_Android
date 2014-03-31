package com.myhoard.app.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.myhoard.app.activities.MainActivity;
import com.myhoard.app.crudengine.CrudEngine;
import com.myhoard.app.model.Collection;
import com.myhoard.app.Managers.UserManager;
import com.myhoard.app.provider.DataProvider;
import com.myhoard.app.provider.DataStorage.Collections;

import java.util.ArrayList;
import java.util.List;

/**
 * Description
 *
 * @author Tomasz Nosal
 *         Date: 19.03.14
 */
public class SynchronizeService extends IntentService {

    public static final String URL = "http://78.133.154.18:8080/collections/";
    UserManager userManager;
    public static String currentStatus;
    public static String maxStatus;

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

        userManager = UserManager.getInstance();
        if (userManager.isLoggedIn()) {
            CrudEngine<Collection> collections = new CrudEngine<>(URL);


            List<Collection> localCollections = new ArrayList<Collection>();
            localCollections = getLocalCollections();

            Intent broadcastIntent = new Intent();
            Integer i = 1;
            for (Collection c : localCollections) {

                // your computer is too fast, sleep 1 second :D
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                collections.create(c,userManager.getToken());
                broadcastIntent.setAction(MainActivity.ResponseReceiver.ACTION_RESP);
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                broadcastIntent.putExtra(currentStatus, i.toString());
                broadcastIntent.putExtra(maxStatus, maxStatus);
                sendBroadcast(broadcastIntent);
                i=i+1;
            }
        }

    }

    protected List<Collection> getLocalCollections() {
        List<Collection> localCollections = new ArrayList<Collection>();
        DataProvider dataProvider = new DataProvider();

        //getting quantity of collections to synchronize
        Cursor cursor = getContentResolver().query(Collections.CONTENT_URI, new String[] {"count(*)"},
                null, null, null);
        cursor.moveToFirst();
        maxStatus = Integer.toString(cursor.getInt(0));

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