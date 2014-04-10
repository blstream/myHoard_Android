package com.myhoard.app.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;

import com.myhoard.app.Managers.UserManager;
import com.myhoard.app.crudengine.CRUDEngine;
import com.myhoard.app.model.Collection;
import com.myhoard.app.model.Item;
import com.myhoard.app.provider.DataStorage;
import com.myhoard.app.provider.DataStorage.Collections;
import com.myhoard.app.provider.DataStorage.TypeOfCollection;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Description
 *
 * @author Tomasz Nosal
 *         Date: 08.04.14
 */
public class SynchronizationService extends IntentService {

    private static final String COLLECTION_ENDPOINT = "collections/";
    private static final String ITEM_ENDPOINT = "items/";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SynchronizationService(String name) {
        super(name);
    }

    public SynchronizationService() {
        super("Synchronizacja");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        UserManager uM = UserManager.getInstance();

        CRUDEngine<Collection> collectionCrud = new CRUDEngine<>(uM.getIp()+COLLECTION_ENDPOINT,Collection.class);
        List<Collection> collections = collectionCrud.getList(uM.getToken());
        for (Collection collection:collections) {
            insert(collection);
        }

        CRUDEngine<Item> itemCrud = new CRUDEngine<>(uM.getIp()+ITEM_ENDPOINT,Item.class);
        List<Item> items = itemCrud.getList(uM.getToken());
        for (Item item:items) {
            insert(item);
        }
    }

    private void insert(Collection collection) {
        ContentValues values = new ContentValues();
        values.put(Collections.NAME, collection.getName());
        values.put(Collections.DESCRIPTION, collection.getDescription());
        values.put(Collections.TAGS, collection.getTags().toString());
        values.put(Collections.TYPE, TypeOfCollection.PUBLIC.toString());

        try {
            java.util.Date modDate = new SimpleDateFormat(DATE_FORMAT).parse(collection.getModified_date());
            java.util.Date creDate = new SimpleDateFormat(DATE_FORMAT).parse(collection.getCreated_date());
            long modifiedDate = modDate.getTime();
            long createdDate = creDate.getTime();
            values.put(Collections.MODIFIED_DATE, modifiedDate);
            values.put(Collections.CREATED_DATE, createdDate);
        } catch (ParseException e) {
            values.put(Collections.MODIFIED_DATE, Calendar.getInstance().getTimeInMillis());
            values.put(Collections.CREATED_DATE, Calendar.getInstance().getTimeInMillis());
            e.printStackTrace();
        }

        Uri uriFromInsert = getContentResolver().insert(Collections.CONTENT_URI, values);
    }

    private void insert(Item item) {
        ContentValues values = new ContentValues();

        //LOCATION, SERVER_ID ?

        values.put(DataStorage.Items.NAME, item.name);
        values.put(DataStorage.Items.DESCRIPTION, item.description);
        values.put(DataStorage.Items.ID_COLLECTION, item.collection);
        values.put(DataStorage.Items.LOCATION_LAT, item.location.lat);
        values.put(DataStorage.Items.LOCATION_LNG, item.location.lng);

        try {
            java.util.Date modDate = new SimpleDateFormat(DATE_FORMAT).parse(item.modifiedDate);
            java.util.Date creDate = new SimpleDateFormat(DATE_FORMAT).parse(item.createdDate);

            long modifiedDate = modDate.getTime();
            long createdDate = creDate.getTime();

            values.put(DataStorage.Items.MODIFIED_DATE, modifiedDate);
            values.put(DataStorage.Items.CREATED_DATE, createdDate);

        } catch (ParseException e) {
            values.put(DataStorage.Items.MODIFIED_DATE, Calendar.getInstance().getTimeInMillis());
            values.put(DataStorage.Items.CREATED_DATE, Calendar.getInstance().getTimeInMillis());
        }

        getContentResolver().insert(DataStorage.Items.CONTENT_URI, values);
    }
}
