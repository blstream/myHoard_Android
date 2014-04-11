package com.myhoard.app.services;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;

import com.myhoard.app.Managers.UserManager;
import com.myhoard.app.crudengine.CRUDEngine;
import com.myhoard.app.crudengine.MediaCrudEngine;
import com.myhoard.app.model.Collection;
import com.myhoard.app.model.Item;
import com.myhoard.app.model.ItemMedia;
import com.myhoard.app.model.Media;
import com.myhoard.app.provider.DataStorage;
import com.myhoard.app.provider.DataStorage.Collections;
import com.myhoard.app.provider.DataStorage.TypeOfCollection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    private static final String MEDIA_ENDPOINT = "media/";
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

        CRUDEngine<Collection> collectionCrud = new CRUDEngine<>(uM.getIp() + COLLECTION_ENDPOINT, Collection.class);
        List<Collection> collections = collectionCrud.getList(uM.getToken());
        for (Collection collection : collections) {
            insert(collection);
        }

        CRUDEngine<Item> itemCrud = new CRUDEngine<>(uM.getIp() + ITEM_ENDPOINT, Item.class);
        MediaCrudEngine<Media> mediaCrud = new MediaCrudEngine<>(uM.getIp() + MEDIA_ENDPOINT);
        List<Item> items = itemCrud.getList(uM.getToken());
        for (Item item : items) {
            long id = insert(item);
            if (item.media != null) {
                for (ItemMedia iM : item.media) {
                    Media media = mediaCrud.get(iM.id, uM.getToken());
                    try {
                        insert(media, id, item.name);
                    } catch (IOException e) {
                        //TODO: powiedz uzytkownikowi ze wystapil blad przy obrazkach
                    }
                }
            }
        }
    }

    private void insert(Collection collection) {
        ContentValues values = new ContentValues();
        values.put(Collections.ID_SERVER, collection.getId());
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

        Uri insert = getContentResolver().insert(Collections.CONTENT_URI, values);
    }

    private long insert(Item item) {
        ContentValues values = new ContentValues();

        values.put(DataStorage.Items.ID_SERVER, item.id);
        if (item.name != null) values.put(DataStorage.Items.NAME, item.name);
        if (item.description != null) values.put(DataStorage.Items.DESCRIPTION, item.description);
        if (item.location != null) {
            values.put(DataStorage.Items.LOCATION_LAT, item.location.lat);
            values.put(DataStorage.Items.LOCATION_LNG, item.location.lng);
        }

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

        values.put(DataStorage.Items.ID_COLLECTION, item.collection);

        Uri uri = Collections.CONTENT_URI;
        String[] projection = new String[]{Collections._ID};
        String selection = String.format("%s = %s", Collections.ID_SERVER, item.collection);

        Cursor cursor = getContentResolver().query(uri, projection, selection, null, null);

        cursor.moveToFirst();
        values.put(DataStorage.Items.ID_COLLECTION, cursor.getString(0));
        return ContentUris.parseId(getContentResolver().insert(DataStorage.Items.CONTENT_URI, values));
    }

    private void insert(Media media, Long idItem, String name) throws IOException {
        ContentValues values = new ContentValues();

        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream fOut = null;

        File folder = new File(path + "/myHoardFiles");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
            File file = new File(path+ "/myHoardFiles", name+".jpg");
            fOut = new FileOutputStream(file);

            BitmapFactory.decodeByteArray(media.getFile(), 0, media.getFile().length).compress(Bitmap.CompressFormat.JPEG, 100, fOut);

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(file.getPath());
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);

            fOut.flush();
            fOut.close();
            values.put(DataStorage.Media.FILE_NAME, contentUri.toString());
        } else {
            //TODO: throw error;
        }


        values.put(DataStorage.Media.ID_SERVER, media.getId());
        values.put(DataStorage.Media.ID_ITEM, idItem);
        getContentResolver().insert(DataStorage.Media.CONTENT_URI, values);
    }
}
