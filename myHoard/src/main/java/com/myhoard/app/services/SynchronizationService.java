package com.myhoard.app.services;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;

import com.myhoard.app.Managers.UserManager;
import com.myhoard.app.crudengine.CRUDEngine;
import com.myhoard.app.crudengine.MediaCrudEngine;
import com.myhoard.app.model.Collection;
import com.myhoard.app.model.IModel;
import com.myhoard.app.model.Item;
import com.myhoard.app.model.ItemMedia;
import com.myhoard.app.provider.DataStorage;
import com.myhoard.app.provider.DataStorage;
import com.myhoard.app.provider.DataStorage.Items;
import com.myhoard.app.provider.DataStorage.Collections;
import com.myhoard.app.provider.DataStorage.TypeOfCollection;
import com.myhoard.app.model.Media;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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
    ArrayList<ContentProviderOperation> operations;
    private boolean first = true;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SynchronizationService(String name) {
        super(name);
        operations = new
                ArrayList<>();
    }

    public SynchronizationService() {
        super("Synchronizacja");
        operations = new
                ArrayList<>();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String tmp = intent.getStringExtra("option");

        switch (tmp) {
            case "download":
                downloadCollections();
                downloadItems();

                Intent intentt = new Intent("notification");
                intentt.putExtra("result", "downloaded");
                sendBroadcast(intentt);
                break;
            case "upload":
                uploadCollections();
                uploadItems();
                break;
        }
    }

    private void uploadItems() {
        UserManager uM = UserManager.getInstance();
        CRUDEngine<Item> itemCrud = new CRUDEngine<>(uM.getIp() + ITEM_ENDPOINT, Item.class);

        String[] projection = new String[] { Items.TABLE_NAME + "." + Items.ID_SERVER,
                Items.TABLE_NAME + "." + Items._ID,
                Items.TABLE_NAME + "." + Items.ID_COLLECTION,
                Items.TABLE_NAME + "." + Items.SYNCHRONIZED,
                Items.TABLE_NAME + "." + Items.DESCRIPTION,
                Items.TABLE_NAME + "." + Items.NAME,
        };
        Cursor cursor = getContentResolver().query(Items.CONTENT_URI, projection, null, null, null);

        if (cursor != null)
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Item item = new Item();
            item.setName(cursor.getString(cursor.getColumnIndex(Items.NAME)));
            item.setDescription(cursor.getString(cursor.getColumnIndex(Items.DESCRIPTION)));
            if (cursor.getInt(cursor.getColumnIndex(Items.SYNCHRONIZED))==0) {
                String where = String.format("%s = %s", Collections._ID, cursor.getString(cursor.getColumnIndex(Items.ID_COLLECTION)));
                Cursor c = getContentResolver().query(Collections.CONTENT_URI, new String[]{Collections.ID_SERVER, Collections.TYPE}, where, null, null);
                if (c != null) c.moveToFirst();
                //if (c.getInt(c.getColumnIndex(Collections.TYPE)) != TypeOfCollection.OFFLINE.getType()) {
                    item.setCollection(c.getString(c.getColumnIndex(Collections.ID_SERVER)));

                    List<ItemMedia> mediaId = uploadMedia(cursor.getString(cursor.getColumnIndex(Items._ID)));
                    if (mediaId.size()>0)
                        item.setMedia(mediaId);

                    ContentValues values = new ContentValues();
                    if (cursor.getString(cursor.getColumnIndex(Items.ID_SERVER)) != null) {
                        itemCrud.update(item, c.getString(c.getColumnIndex(Items.ID_SERVER)), uM.getToken());
                        where = String.format("%s = %s", Items.ID_SERVER, cursor.getString(cursor.getColumnIndex(Items.ID_SERVER)));

                    } else {
                        IModel imodel = itemCrud.create(item, uM.getToken());
                        where = String.format("%s = %s", Items._ID, cursor.getString(cursor.getColumnIndex(Items._ID)));
                        values.put(Items.ID_SERVER,imodel.getId());
                    }

                    values.put(Items.SYNCHRONIZED, true);
                    int update = getContentResolver().update(Items.CONTENT_URI, values, where, null);
            //}
            }
        }
    }

    private List<ItemMedia> uploadMedia(String id) {
        List<ItemMedia> listId = new ArrayList<>();
        UserManager uM = UserManager.getInstance();
        MediaCrudEngine<Media> mediaCrud = new MediaCrudEngine<>(uM.getIp()+MEDIA_ENDPOINT);
        Cursor cursor = getContentResolver().query(DataStorage.Media.CONTENT_URI,
               new String[]{DataStorage.Media._ID, DataStorage.Media.FILE_NAME, DataStorage.Media.SYNCHRONIZED},
               DataStorage.Media.ID_ITEM+"="+id, null, null);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            if (cursor.getInt(cursor.getColumnIndex(DataStorage.Media.SYNCHRONIZED))==0) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(cursor.getString(cursor.getColumnIndex(DataStorage.Media.FILE_NAME))));
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    IModel imodel = mediaCrud.create(new Media(byteArray), uM.getToken());
                    if (imodel != null) {
                        listId.add(new ItemMedia(imodel.getId()));

                        ContentValues values = new ContentValues();
                        values.put(DataStorage.Media.SYNCHRONIZED, true);
                        values.put(DataStorage.Media.ID_SERVER, imodel.getId());
                        String where = String.format("%s = %s", DataStorage.Media._ID, cursor.getString(cursor.getColumnIndex(DataStorage.Media._ID)));
                        int idd = getContentResolver().update(DataStorage.Media.CONTENT_URI, values, where, null);
                        Log.d("MainActivity","ile zupdatowano "+idd);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return listId;
    }

    private void downloadItems() {
        operations = new ArrayList<>();
        UserManager uM = UserManager.getInstance();
        CRUDEngine<Item> itemReadForSpecificCollection = new CRUDEngine<>(uM.getIp() + ITEM_ENDPOINT, Item.class);
        List<Item> items = itemReadForSpecificCollection.getList(uM.getToken());

        String selection = String.format(Items.TABLE_NAME + "." + Items.ID_SERVER+" IN (");
        for (Item item : items) {
            if (selection.charAt(selection.length() - 1)!='(') {
                selection += ",";
            }
            selection += item.getId();
        }
        selection += ")";
        HashMap<String, Long> mapka = new HashMap<>();
        if (items.size()>0) {
            Cursor cursor = getContentResolver().query(Items.CONTENT_URI, new String[]{Items.TABLE_NAME + "." + Items.ID_SERVER, Items.TABLE_NAME + "." + Items.MODIFIED_DATE}, selection, null, null);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                mapka.put(cursor.getString(cursor.getColumnIndex(Items.ID_SERVER)), cursor.getLong(cursor.getColumnIndex(Items.MODIFIED_DATE)));
            }
        }
        for (Item item : items) {
            if (mapka.get(item.getId())==null) { //jezeli nie bylo takiego w bazie no to insert
                insert(item);
            }   else { //jezeli data modyfikacji jest wieksza
                Long dataMod = mapka.get(item.getId()); //TODO sprawdzenie czy data modyfikacji wieksza..
                //c.getString(c.getColumnIndex(Collections.MODIFIED_DATE));
                //update
                update(item);
            }
        }

        try {
            getContentResolver().applyBatch(DataStorage.AUTHORITY,operations);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }

        for (Item item : items) {
            if (item.getMedia()!=null)
                downloadMedia(item.getMedia(),item.getId());
        }
    }

    private void downloadMedia(List<ItemMedia> media, String itemId) {
        String selection = String.format(DataStorage.Media.ID_SERVER+" IN (");
        for (ItemMedia med : media) {
            if (selection.charAt(selection.length() - 1)!='(') {
                selection += ",";
            }
            selection += med.id;
        }
        selection += ")";
        //List<String> listId = new ArrayList<>();
        HashMap<String, String> mapka = new HashMap<>();
        if (media.size()>0) {
            Cursor cursor = getContentResolver().query(DataStorage.Media.CONTENT_URI, null, selection, null, null);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                //listId.add(cursor.getString(cursor.getColumnIndex(DataStorage.Media.ID_SERVER)));
                mapka.put(cursor.getString(cursor.getColumnIndex(DataStorage.Media.ID_SERVER)), cursor.getString(cursor.getColumnIndex(DataStorage.Media.ID_ITEM)));
            }
        }
        for (ItemMedia med : media) {
            if(!mapka.containsKey(med.id)) { //jezeli serwer id nie znajduje sie w bazie
                //create
                insert(med, itemId);
            } else { //jezeli się znajduję sprawdź czy media jest w danym itemie
                //if(mapka.get(med.id) != itemId) {
                    //przenies do innego itemu
                //}
            }
        }
    }

    private void insert(ItemMedia med, String itemId) {
        ContentValues values = new ContentValues();

        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream fOut = null;

        File folder = new File(path + "/myHoardFiles");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
            UserManager uM = UserManager.getInstance();
            MediaCrudEngine<Media> mediaCrud = new MediaCrudEngine<>(uM.getIp()+MEDIA_ENDPOINT);
            Media media = mediaCrud.get(med.id, uM.getToken());

            Cursor cursor = getContentResolver().query(Items.CONTENT_URI,
                    new String[] {Items.TABLE_NAME+"."+Items._ID, Items.TABLE_NAME+"."+Items.NAME},
                    Items.TABLE_NAME+"."+Items.ID_SERVER+"="+itemId, null, null);
            if (cursor != null)
                cursor.moveToFirst();

            File file = new File(path+ "/myHoardFiles", cursor.getString(cursor.getColumnIndex(Items.NAME))+med.id+".jpg");
            try {
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
                values.put(DataStorage.Media.ID_SERVER, media.getId());
                values.put(DataStorage.Media.ID_ITEM, cursor.getString(cursor.getColumnIndex(Items._ID)));
                values.put(DataStorage.Media.SYNCHRONIZED, true);

                cursor = getContentResolver().query(DataStorage.Media.CONTENT_URI,
                        new String[] {"count(*) AS count"},
                        DataStorage.Media.ID_ITEM+"="+cursor.getString(cursor.getColumnIndex(Items._ID)), null, null);
                cursor.moveToFirst();
                int count = cursor.getInt(0);

                if (count == 0)
                    values.put(DataStorage.Media.AVATAR, true);
                else
                    values.put(DataStorage.Media.AVATAR, false);

                getContentResolver().insert(DataStorage.Media.CONTENT_URI, values);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                //TODO: throw error;
            } catch (IOException e) {
                e.printStackTrace();
                //TODO: throw error;
            }
        } else {
            //TODO: throw error;
        }
    }

    private void downloadCollections() {
        long startTime = System.currentTimeMillis();
        UserManager uM = UserManager.getInstance();

        CRUDEngine<Collection> collectionCrud = new CRUDEngine<>(uM.getIp() + COLLECTION_ENDPOINT, Collection.class);
        List<Collection> collections = collectionCrud.getList(uM.getToken());

        String selection = String.format(Collections.ID_SERVER+" IN (");
        for (Collection collection : collections) {
            //listOfId.add(collection.getId());
            if (selection.charAt(selection.length() - 1)!='(') {
                selection += ",";
            }
            selection += collection.getId();
        }
        selection += ")";

        Cursor cursor = getContentResolver().query(Collections.CONTENT_URI, new String[]{Collections.ID_SERVER,Collections.MODIFIED_DATE}, selection, null, null);
        HashMap<String,Long> mapka = new HashMap<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            mapka.put(cursor.getString(cursor.getColumnIndex(Collections.ID_SERVER)),cursor.getLong(cursor.getColumnIndex(Collections.MODIFIED_DATE)));
            }

        for (Collection collection : collections) {
            if (mapka.get(collection.getId())==null) { //jezeli nie bylo takiego w bazie no to insert
                //insert
                insert(collection);

            }
            else { //jezeli data modyfikacji jest wieksza
                Long dataMod = mapka.get(collection.getId()); //TODO sprawdzenie czy data modyfikacji wieksza..
                //c.getString(c.getColumnIndex(Collections.MODIFIED_DATE));
                update(collection);
            }
        }

        try {
            getContentResolver().applyBatch(DataStorage.AUTHORITY,operations);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    private void uploadCollections() {
        UserManager uM = UserManager.getInstance();
        CRUDEngine<Collection> collectionCrud = new CRUDEngine<>(uM.getIp() + COLLECTION_ENDPOINT, Collection.class);
        Cursor cursor = getContentResolver().query(Collections.CONTENT_URI, null, null, null, null);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            //if (cursor.getInt(cursor.getColumnIndex(Collections.TYPE))!= TypeOfCollection.OFFLINE.getType()) {
                Collection collection = new Collection();
                collection.setName(cursor.getString(cursor.getColumnIndex(Collections.NAME)));
                collection.setDescription(cursor.getString(cursor.getColumnIndex(Collections.DESCRIPTION)));
                if (cursor.getInt(cursor.getColumnIndex(Collections.SYNCHRONIZED)) == 0
                        && cursor.getString(cursor.getColumnIndex(Collections.ID_SERVER)) != null) {
                    collectionCrud.update(collection, cursor.getString(cursor.getColumnIndex(Collections.ID_SERVER)), uM.getToken());
                    String where = String.format("%s = %s", Collections.ID_SERVER, cursor.getString(cursor.getColumnIndex(Collections.ID_SERVER)));
                    ContentValues values = new ContentValues();
                    values.put(Collections.SYNCHRONIZED, true);
                    int update = getContentResolver().update(Collections.CONTENT_URI, values, where, null);
                } else if (cursor.getInt(cursor.getColumnIndex(Collections.SYNCHRONIZED)) == 0) {
                    IModel imodel = collectionCrud.create(collection, uM.getToken());

                    String where = String.format("%s = %s", Collections._ID, cursor.getString(cursor.getColumnIndex(Collections._ID)));
                    ContentValues values = new ContentValues();
                    values.put(Collections.ID_SERVER, imodel.getId());
                    values.put(Collections.SYNCHRONIZED, true);
                    int update = getContentResolver().update(Collections.CONTENT_URI, values, where, null);
                }
            //}
        }
    }

    private void update(Collection collection) {
        ContentValues values = new ContentValues();
        values.put(Collections.ID_SERVER, collection.getId());
        values.put(Collections.NAME, collection.getName());
        if (collection.getDescription() != null)
            values.put(Collections.DESCRIPTION, collection.getDescription());
        values.put(Collections.TAGS, collection.getTags().toString());
        values.put(Collections.TYPE, TypeOfCollection.PUBLIC.toString());
        values.put(Collections.ITEMS_NUMBER, collection.getItems_number());
        values.put(Collections.SYNCHRONIZED, true);

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
        String where = String.format("%s = %s", Collections.ID_SERVER, collection.getId());
        operations.add(ContentProviderOperation.
                        newUpdate(Collections.CONTENT_URI)
                        .withSelection(where, null)
                        .withValues(values)
                        .build()
        );
    }


    private void insert(Collection collection) {
        ContentValues values = new ContentValues();
        values.put(Collections.ID_SERVER, collection.getId());
        values.put(Collections.NAME, collection.getName());
        if (collection.getDescription() != null)
            values.put(Collections.DESCRIPTION, collection.getDescription());
        values.put(Collections.TAGS, collection.getTags().toString());
        values.put(Collections.TYPE, TypeOfCollection.PUBLIC.toString());
        values.put(Collections.ITEMS_NUMBER, collection.getItems_number());
        values.put(Collections.SYNCHRONIZED, true);

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

        operations.add(ContentProviderOperation
                .newInsert(Collections.CONTENT_URI)
                .withValues(values)
                .build());
        //Uri insert = getContentResolver().insert(Collections.CONTENT_URI, values);
    }


    private void update(Item item) {
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

            values.put(Items.MODIFIED_DATE, modifiedDate);
            values.put(Items.CREATED_DATE, createdDate);

        } catch (ParseException e) {
            values.put(Items.MODIFIED_DATE, Calendar.getInstance().getTimeInMillis());
            values.put(Items.CREATED_DATE, Calendar.getInstance().getTimeInMillis());
        }

        Uri uri = Collections.CONTENT_URI;
        String[] projection = new String[] { Collections._ID };
        String selection = String.format("%s = %s", Collections.ID_SERVER, item.collection);
        Cursor cursor = getContentResolver().query(uri, projection, selection, null, null);
        cursor.moveToFirst();
        values.put(DataStorage.Items.ID_COLLECTION, cursor.getString(0));

        String where = String.format("%s = %s", Items.ID_SERVER, item.getId());
        operations.add(ContentProviderOperation.
                        newUpdate(Items.CONTENT_URI)
                        .withSelection(where, null)
                        .withValues(values)
                        .build()
        );
    }

    private void insert(Item item) {
        ContentValues values = new ContentValues();

        values.put(DataStorage.Items.ID_SERVER, item.id);
        if (item.name != null) values.put(DataStorage.Items.NAME, item.name);
        if (item.description != null) values.put(DataStorage.Items.DESCRIPTION, item.description);
        if (item.location != null) {
            values.put(DataStorage.Items.LOCATION_LAT, item.location.lat);
            values.put(DataStorage.Items.LOCATION_LNG, item.location.lng);
        }
        values.put(Collections.SYNCHRONIZED, true);
        try {
            java.util.Date modDate = new SimpleDateFormat(DATE_FORMAT).parse(item.modifiedDate);
            java.util.Date creDate = new SimpleDateFormat(DATE_FORMAT).parse(item.createdDate);

            long modifiedDate = modDate.getTime();
            long createdDate = creDate.getTime();

            values.put(Items.MODIFIED_DATE, modifiedDate);
            values.put(Items.CREATED_DATE, createdDate);

        } catch (ParseException e) {
            values.put(Items.MODIFIED_DATE, Calendar.getInstance().getTimeInMillis());
            values.put(Items.CREATED_DATE, Calendar.getInstance().getTimeInMillis());
        }

        Uri uri = Collections.CONTENT_URI;
        String[] projection = new String[] { Collections._ID };
        String selection = String.format("%s = %s", Collections.ID_SERVER, item.collection);
        Cursor cursor = getContentResolver().query(uri, projection, selection, null, null);
        cursor.moveToFirst();
        values.put(DataStorage.Items.ID_COLLECTION, cursor.getString(0));

        operations.add(ContentProviderOperation
                .newInsert(Items.CONTENT_URI)
                .withValues(values)
                .build());
    }

    /*private void insert(Media media, Long idItem, String name) throws IOException {
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
    }*/
}
