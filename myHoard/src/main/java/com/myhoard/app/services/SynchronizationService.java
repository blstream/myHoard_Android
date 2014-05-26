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
import com.myhoard.app.provider.DataStorage.Items;
import com.myhoard.app.provider.DataStorage.Collections;
import com.myhoard.app.provider.DataStorage.TypeOfCollection;
import com.myhoard.app.model.Media;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Description
 *
 * @author Tomasz Nosal
 *         Date: 08.04.14
 */
public class SynchronizationService extends IntentService {

    private static final String COLLECTIONS_ENDPOINT = "collections/";
    private static final String USERS_ENDPOINT = "users/";
    private static final String ITEM_ENDPOINT = "items/";
    private static final String MEDIA_ENDPOINT = "media/";
    private static final String SEPARATOR = "#";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final int INDEX_OF_FIRST_ELEMENT = 0;
    private static final int ERROR = -1;
    private static final String ERROR_CREATING_FOLDER = "Error creating folder myHoardFiles";

    public static final String CANCEL_COMMAND_KEY = "cancelCommand";
    public static final String ASK_IF_SERVICE_ENDED = "ifEnd";

    private UserManager userManager = UserManager.getInstance();
    ArrayList<ContentProviderOperation> operations;
    private static Boolean mutex=false;
    private static Boolean cancel=false;

    CRUDEngine<Collection> collectionCrud;
    CRUDEngine<Item> itemCrud;
    MediaCrudEngine<Media> mediaCrud;

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
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.hasExtra(CANCEL_COMMAND_KEY)) {
                cancel();
            }
            if (intent.hasExtra(ASK_IF_SERVICE_ENDED)) {
                if(!mutex){
                    Intent intenttt = new Intent("notification");
                    intenttt.putExtra("result", "synchronized");
                    intenttt.putExtra("result2", "downloaded");
                    sendBroadcast(intenttt);
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void cancel(){
        cancel=true;
        if (collectionCrud != null)
            collectionCrud.stopRequest();
        if (itemCrud != null)
            itemCrud.stopRequest();
        if (mediaCrud != null)
            mediaCrud.stopRequest();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String option = intent.getStringExtra("option");

        switch (option) {
            case "synchronization":
                mutex = true;
                uploadCollections();
                uploadItems();
                downloadCollections();
                downloadItems();

                Intent intenttt = new Intent("notification");
                //2 notification: 1 to MainActivity, 2 to CollectionListFragment
                intenttt.putExtra("result", "synchronized");
                intenttt.putExtra("result2", "downloaded");
                sendBroadcast(intenttt);
                mutex = false;
                cancel = false;
                break;
        }
    }

    private void uploadCollections() {
        collectionCrud = new CRUDEngine<>(userManager.getIp() + COLLECTIONS_ENDPOINT, Collection.class);
        Cursor cursor = getContentResolver().query(Collections.CONTENT_URI, null, null, null, null);
        if (cursor != null)
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                if (!cancel)
                uploadCollection(collectionCrud, cursor);
            }
    }

    private void uploadCollection(CRUDEngine<Collection> collectionCrud, Cursor cursor) {
        if (cursor.getInt(cursor.getColumnIndex(Collections.DELETED)) == 1) {
            deleteCollectionOnServerAndInDatabase(collectionCrud, cursor);
        } else if (cursor.getInt(cursor.getColumnIndex(Collections.TYPE)) != TypeOfCollection.OFFLINE.getType()) {
            Collection collection = getCollectionFromDatabase(cursor);
            if (cursor.getInt(cursor.getColumnIndex(Collections.SYNCHRONIZED)) == 0
                    && cursor.getString(cursor.getColumnIndex(Collections.ID_SERVER)) != null) {
                updateCollectionOnServerAndUpdateInDatabase(collectionCrud, cursor, collection);
            } else if (cursor.getInt(cursor.getColumnIndex(Collections.SYNCHRONIZED)) == 0) {
                createCollectionOnServerAndUpdateInDatabase(collectionCrud, cursor, collection);
            }
        } else if (cursor.getString(cursor.getColumnIndex(Collections.ID_SERVER)) != null) {
            collectionCrud.remove(cursor.getString(cursor.getColumnIndex(Collections.ID_SERVER)), userManager.getToken());
            String where = String.format("%s = %s", Collections.ID_SERVER, cursor.getString(cursor.getColumnIndex(Collections.ID_SERVER)));
            ContentValues values = new ContentValues();
            values.putNull(Collections.ID_SERVER);
            getContentResolver().update(Collections.CONTENT_URI, values, where, null);
        }
    }

    private void deleteCollectionOnServerAndInDatabase(CRUDEngine<Collection> collectionCrud, Cursor cursor) {
        String idCollectionOnServer = cursor.getString(cursor.getColumnIndex(Collections.ID_SERVER));
        String id = cursor.getString(cursor.getColumnIndex(Collections._ID));
        String where = Collections._ID + "=?";
        String[] args = new String[]{id};
        if (idCollectionOnServer == null) {
            getContentResolver().delete(Collections.CONTENT_URI, where, args);
        } else {
            collectionCrud.remove(idCollectionOnServer, userManager.getToken());
            getContentResolver().delete(Collections.CONTENT_URI, where, args);
        }
    }

    private Collection getCollectionFromDatabase(Cursor cursor) {
        Collection collection = new Collection();
        collection.setId(cursor.getString(cursor.getColumnIndex(Collections.ID_SERVER)));
        collection.setName(cursor.getString(cursor.getColumnIndex(Collections.NAME)));
        collection.setDescription(cursor.getString(cursor.getColumnIndex(Collections.DESCRIPTION)));
        if (cursor.getInt(cursor.getColumnIndex(Collections.TYPE)) == TypeOfCollection.PUBLIC.getType())
            collection.setIfPublic(true);
        String tags = cursor.getString(cursor.getColumnIndex(Collections.TAGS));
        if (tags != null) {
            String[] splitedTags = tags.split(SEPARATOR);
            List<String> list = new LinkedList<>(Arrays.asList(splitedTags));
            list.remove(0); //zero element is always empty, remove it
            for (int i = 0; i < list.size(); i++) {
                list.set(i, list.get(i).trim());
            }
            collection.setTags(list);
        }
        return collection;
    }

    private void updateCollectionOnServerAndUpdateInDatabase(CRUDEngine<Collection> collectionCrud, Cursor cursor, Collection collection) {
        try {
            collectionCrud.update(collection, collection.getId(), userManager.getToken());
            String where = String.format("%s = %s", Collections.ID_SERVER, collection.getId());
            ContentValues values = new ContentValues();
            values.put(Collections.SYNCHRONIZED, true);
            getContentResolver().update(Collections.CONTENT_URI, values, where, null);
        } catch (RuntimeException re) { // mozliwe, ze ktos skasowal kolekjce z innego urzadzenia i nie mozna jej zudpatowac, trzeba sprobowac create
            try {
                createCollectionOnServerAndUpdateInDatabase(collectionCrud, cursor, collection);
            } catch (RuntimeException e) {
                sendError(e.getMessage());
            }
        }
    }

    private void createCollectionOnServerAndUpdateInDatabase(CRUDEngine<Collection> collectionCrud, Cursor cursor, Collection collection) {
        try {
            IModel imodel = collectionCrud.create(collection, userManager.getToken());
            String where = String.format("%s = %s", Collections._ID, cursor.getString(cursor.getColumnIndex(Collections._ID)));
            ContentValues values = new ContentValues();
            values.put(Collections.ID_SERVER, imodel.getId());
            values.put(Collections.SYNCHRONIZED, true);
            getContentResolver().update(Collections.CONTENT_URI, values, where, null);
        } catch (RuntimeException re) { //moze sie zdarzyć, że już jest taka kolekcja na serwerze, wtedy search i updatujemy
            try {
                String url = userManager.getIp() + USERS_ENDPOINT + UserManager.getInstance().getToken().getUserId() + "/" + COLLECTIONS_ENDPOINT + "?name=" + collection.getName();
                IModel imodel = collectionCrud.searchByName(url, userManager.getToken());
                collection.setId(imodel.getId());
                collectionCrud.update(collection, collection.getId(), userManager.getToken());

                String where = String.format("%s = %s", Collections._ID, cursor.getString(cursor.getColumnIndex(Collections._ID)));
                ContentValues values = new ContentValues();
                values.put(Collections.ID_SERVER, imodel.getId());
                values.put(Collections.SYNCHRONIZED, true);
                getContentResolver().update(Collections.CONTENT_URI, values, where, null);
            } catch (RuntimeException e) {
                sendError(re.getMessage());
            }
        }
    }

    private void uploadItems() {
        itemCrud = new CRUDEngine<>(userManager.getIp() + ITEM_ENDPOINT, Item.class);

        String[] projection = new String[]{Items.TABLE_NAME + "." + Items.ID_SERVER,
                Items.TABLE_NAME + "." + Items._ID,
                Items.TABLE_NAME + "." + Items.ID_COLLECTION,
                Items.TABLE_NAME + "." + Items.SYNCHRONIZED,
                Items.TABLE_NAME + "." + Items.DESCRIPTION,
                Items.TABLE_NAME + "." + Items.NAME,
                Items.TABLE_NAME + "." + Items.DELETED
        };
        Cursor cursor = getContentResolver().query(Items.CONTENT_URI, projection, null, null, null);

        if (cursor != null)
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                if (!cancel)
                uploadItem(itemCrud, cursor);
            }
    }

    private void uploadItem(CRUDEngine<Item> itemCrud, Cursor cursor) {
        if (cursor.getInt(cursor.getColumnIndex(Items.DELETED)) == 1) {
            deleteItemOnServerAndInDatabase(itemCrud, cursor);
        } else if (cursor.getInt(cursor.getColumnIndex(Items.SYNCHRONIZED)) == 0) {
            String where = String.format("%s = %s", Collections._ID, cursor.getString(cursor.getColumnIndex(Items.ID_COLLECTION)));
            Cursor c = getContentResolver().query(Collections.CONTENT_URI, new String[]{Collections.ID_SERVER, Collections.TYPE}, where, null, null);
            if (c != null) {
                c.moveToFirst();
                if (c.getInt(c.getColumnIndex(Collections.TYPE)) != TypeOfCollection.OFFLINE.getType()) {
                    //createOrUpdateItemOnServerAndUpdateInDatabase(itemCrud, cursor, c);
                    if (cursor.getString(cursor.getColumnIndex(Items.ID_SERVER)) != null)
                        updateItemOnServerAndUpdateInDatabase(itemCrud, cursor, c);
                    else
                        createItemOnServerAndUpdateInDatabase(itemCrud, cursor, c);
                }
            }
        }
    }

    private void deleteItemOnServerAndInDatabase(CRUDEngine<Item> itemCrud, Cursor cursor) {
        String idItemOnServer = cursor.getString(cursor.getColumnIndex(Items.ID_SERVER));
        String id = cursor.getString(cursor.getColumnIndex(Items._ID));
        String where = Items._ID + "=?";
        String[] args = new String[]{id};
        if (idItemOnServer == null) {
            getContentResolver().delete(Collections.CONTENT_URI, where, args);
        } else {
            itemCrud.remove(idItemOnServer, userManager.getToken());
            getContentResolver().delete(Collections.CONTENT_URI, where, args);
        }
    }

    private void updateItemOnServerAndUpdateInDatabase(CRUDEngine<Item> itemCrud, Cursor cursor, Cursor cursorCollection) {
        ContentValues values = new ContentValues();
        Item item = getItemFromDatabase(cursor, cursorCollection);
        String where;
        try {
            values.put(Items.SYNCHRONIZED, true);
            itemCrud.update(item, cursor.getString(cursor.getColumnIndex(Items.ID_SERVER)), userManager.getToken());
            where = String.format("%s = %s", Items.ID_SERVER, cursor.getString(cursor.getColumnIndex(Items.ID_SERVER)));
            getContentResolver().update(Items.CONTENT_URI, values, where, null);
        } catch (RuntimeException re) {
            //TODO: trzeba wysłac wszystkie obrazki od nowa, mimo że mogą być synchronized, ktoś skasował nasza kolekcje z serwera
            //try {
            //    IModel imodel = itemCrud.create(item, userManager.getToken());
            //    where = String.format("%s = %s", Items._ID, cursor.getString(cursor.getColumnIndex(Items._ID)));
            //    values.put(Items.ID_SERVER, imodel.getId());
            //    getContentResolver().update(Items.CONTENT_URI, values, where, null);
            //} catch (RuntimeException e) {
            sendError(re.getMessage());
            //}
        }
    }

    private void createItemOnServerAndUpdateInDatabase(CRUDEngine<Item> itemCrud, Cursor cursor, Cursor c) {
        Item item = getItemFromDatabase(cursor, c);
        try {
            ContentValues values = new ContentValues();
            String where;
            IModel imodel = itemCrud.create(item, userManager.getToken());
            where = String.format("%s = %s", Items._ID, cursor.getString(cursor.getColumnIndex(Items._ID)));
            values.put(Items.ID_SERVER, imodel.getId());

            values.put(Items.SYNCHRONIZED, true);
            getContentResolver().update(Items.CONTENT_URI, values, where, null);
        } catch (RuntimeException re) {
            //mozliwe ze nie mozna stowrzyc bo jest na serwerze item o takiej nazwie
            try {
                String url = userManager.getIp() + ITEM_ENDPOINT + "?name=" + item.getName() + "&collection=" + item.getCollection();
                IModel imodel = itemCrud.searchByName(url, userManager.getToken());

                //TODO: pobrac zdjecia?
                HashMap<String, String> mapka = new HashMap<>();
                if (item.getMedia().size() > 0) {
                    Cursor cursorMedia = getContentResolver().query(DataStorage.Media.CONTENT_URI,
                            null,
                            DataStorage.Media.ID_SERVER + createSelectionForMedia(item.getMedia()),
                            null,
                            null);
                    if (cursorMedia != null)
                        for (cursorMedia.moveToFirst(); !cursorMedia.isAfterLast(); cursorMedia.moveToNext()) {
                            mapka.put(cursorMedia.getString(cursorMedia.getColumnIndex(DataStorage.Media.ID_SERVER)), cursorMedia.getString(cursorMedia.getColumnIndex(DataStorage.Media.ID_ITEM)));
                        }
                }
                List<ItemMedia> tmp = item.getMedia();
                for (ItemMedia med : ((Item) imodel).getMedia()) {
                    if (!mapka.containsKey(med.id)) { //jezeli serwer id nie znajduje sie w bazie
                        insert(med, item.getId(), item.getName());
                        tmp.add(new ItemMedia(med.getId()));
                    }
                }

                item.setId(imodel.getId());
                item.setMedia(tmp);

                itemCrud.update(item, imodel.getId(), userManager.getToken());

                String where = String.format("%s = %s", Items._ID, cursor.getString(cursor.getColumnIndex(Items._ID)));
                ContentValues values = new ContentValues();
                values.put(Items.ID_SERVER, imodel.getId());
                values.put(Items.SYNCHRONIZED, true);
                getContentResolver().update(Items.CONTENT_URI, values, where, null);
            } catch (RuntimeException e) {
                sendError(e.getMessage());
            }
        }
    }

    private Item getItemFromDatabase(Cursor cursor, Cursor cursorCollection) {
        Item item = new Item();
        item.setId(cursor.getString(cursor.getColumnIndex(Items._ID)));
        item.setName(cursor.getString(cursor.getColumnIndex(Items.NAME)));
        item.setDescription(cursor.getString(cursor.getColumnIndex(Items.DESCRIPTION)));
        item.setCollection(cursorCollection.getString(cursorCollection.getColumnIndex(Collections.ID_SERVER)));

        List<ItemMedia> mediaId = uploadMedia(cursor.getString(cursor.getColumnIndex(Items._ID)));
        if (mediaId.size() > 0)
            item.setMedia(mediaId);
        return item;
    }

    private List<ItemMedia> uploadMedia(String id) {
        List<ItemMedia> listId = new ArrayList<>();
        Cursor cursor = getContentResolver().query(DataStorage.Media.CONTENT_URI,
                new String[]{DataStorage.Media._ID, DataStorage.Media.FILE_NAME, DataStorage.Media.SYNCHRONIZED, DataStorage.Media.ID_SERVER},
                DataStorage.Media.ID_ITEM + "=" + id, null, null);
        if (cursor != null)
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                if (cursor.getInt(cursor.getColumnIndex(DataStorage.Media.SYNCHRONIZED)) == 0) {
                    createMediaOnServerAndUpdateInDatabase(cursor, listId);
                } else
                    listId.add(new ItemMedia(cursor.getString(cursor.getColumnIndex(DataStorage.Media.ID_SERVER))));
            }
        return listId;
    }

    private void createMediaOnServerAndUpdateInDatabase(Cursor cursor, List<ItemMedia> listId) {
        try {
            mediaCrud = new MediaCrudEngine<>(userManager.getIp() + MEDIA_ENDPOINT);
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(cursor.getString(cursor.getColumnIndex(DataStorage.Media.FILE_NAME))));
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            IModel imodel = mediaCrud.create(new Media(byteArray), userManager.getToken());
            if (imodel != null) {
                listId.add(new ItemMedia(imodel.getId()));

                ContentValues values = new ContentValues();
                values.put(DataStorage.Media.SYNCHRONIZED, true);
                values.put(DataStorage.Media.ID_SERVER, imodel.getId());
                String where = String.format("%s = %s", DataStorage.Media._ID, cursor.getString(cursor.getColumnIndex(DataStorage.Media._ID)));
                getContentResolver().update(DataStorage.Media.CONTENT_URI, values, where, null);
            }
        } catch (RuntimeException re) {
            sendError(re.getMessage());
        } catch (IOException e) {
            sendError(e.toString());
        }
    }

    private void downloadItems() {
        operations = new ArrayList<>();
        itemCrud = new CRUDEngine<>(userManager.getIp() + ITEM_ENDPOINT, Item.class);
        List<Item> items;
        try {
            items = itemCrud.getList(userManager.getToken());

            HashMap<String, Long> idServerToModifiedDate = new HashMap<>();
            if (items.size() > 0) {
                addItemsIdServerAndModifiedDateToHashMap(items, idServerToModifiedDate);
            }
            for (Item item : items) {
                if (!cancel) {
                    if (idServerToModifiedDate.get(item.getId()) == null) { //jezeli nie bylo takiego w bazie no to insert
                        insert(item);
                    } else if (modifiedDateOnServerIsGraterThanInDatabase(item.getModifiedDate(), idServerToModifiedDate.get(item.getId()))) {
                        update(item);
                    }
                }
            }
            executeOperations();

            if (!cancel)
            deleteNotFoundItems(items);

            for (Item item : items) {
                if (!cancel)
                if (item.getMedia() != null)
                    downloadMedia(item.getMedia(), item.getId());
            }
        } catch (RuntimeException re) {
            sendError(re.getMessage());
        }
    }

    private void sendError(String message) {
        Intent intentt = new Intent("notification");
        intentt.putExtra("error", message);
        sendBroadcast(intentt);
    }

    private String createSelection(List<Item> items) {
        String selection = String.format(" IN (");
        for (Item item : items) {
            if (selection.charAt(selection.length() - 1) != '(') {
                selection += ",";
            }
            selection += item.getId();
        }
        selection += ")";
        return selection;
    }

    private void addItemsIdServerAndModifiedDateToHashMap(List<Item> items, HashMap<String, Long> idServerToModifiedDate) {
        Cursor cursor = getContentResolver().query(Items.CONTENT_URI,
                new String[]{Items.TABLE_NAME + "." + Items.ID_SERVER, Items.TABLE_NAME + "." + Items.MODIFIED_DATE},
                Items.TABLE_NAME + "." + Items.ID_SERVER + createSelection(items),
                null,
                null
        );
        if (cursor != null)
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                idServerToModifiedDate.put(cursor.getString(cursor.getColumnIndex(Items.ID_SERVER)), cursor.getLong(cursor.getColumnIndex(Items.MODIFIED_DATE)));
            }
    }

    private void executeOperations() {
        try {
            getContentResolver().applyBatch(DataStorage.AUTHORITY, operations);
        } catch (RemoteException | OperationApplicationException e) {
            sendError(e.toString());
        }
    }

    private void deleteNotFoundItems(List<Item> items) {
        Cursor cursor = getContentResolver().query(Items.CONTENT_URI,
                new String[]{Items.TABLE_NAME + "." + Items.ID_SERVER},
                Items.TABLE_NAME + "." + Items.ID_SERVER + " NOT" + createSelection(items),
                null,
                null
        );
        if (cursor != null)
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String idServer = cursor.getString(cursor.getColumnIndex(Items.ID_SERVER));
                String where = Items.ID_SERVER + "=?";
                String[] args = new String[]{idServer};
                getContentResolver().delete(Items.CONTENT_URI, where, args);
            }
    }

    private void downloadMedia(List<ItemMedia> media, String itemId) {
        HashMap<String, String> mapka = new HashMap<>();
        if (media.size() > 0) {
            Cursor cursor = getContentResolver().query(DataStorage.Media.CONTENT_URI,
                    null,
                    DataStorage.Media.ID_SERVER + createSelectionForMedia(media),
                    null,
                    null);
            if (cursor != null)
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    mapka.put(cursor.getString(cursor.getColumnIndex(DataStorage.Media.ID_SERVER)), cursor.getString(cursor.getColumnIndex(DataStorage.Media.ID_ITEM)));
                }
        }
        for (ItemMedia med : media) {
            if (!cancel)
            if (!mapka.containsKey(med.id)) { //jezeli serwer id nie znajduje sie w bazie
                insert(med, itemId, null);
            }
        }
        if (!cancel)
        deleteNotFoundMedia(media, itemId);
    }

    private String createSelectionForMedia(List<ItemMedia> media) {
        String selection = String.format(" IN (");
        for (ItemMedia med : media) {
            if (selection.charAt(selection.length() - 1) != '(') {
                selection += ",";
            }
            selection += med.id;
        }
        selection += ")";
        return selection;
    }

    private void insert(ItemMedia med, String itemId, String name) {
        ContentValues values = new ContentValues();
        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream fOut;

        boolean success = createFolderMyHoard(path);
        if (success) {
            mediaCrud = new MediaCrudEngine<>(userManager.getIp() + MEDIA_ENDPOINT);
            Media media = null;
            try {
                media = mediaCrud.get(med.id, userManager.getToken());
            } catch (Exception e) {
                e.getMessage();
            }
            Cursor cursor = getContentResolver().query(Items.CONTENT_URI,
                    new String[]{Items.TABLE_NAME + "." + Items._ID, Items.TABLE_NAME + "." + Items.NAME},
                    Items.TABLE_NAME + "." + Items.ID_SERVER + "=" + itemId, null, null);
            File file;
            if (cursor != null)
                cursor.moveToFirst();
            String filename = "";
            String id = "";
            if (cursor != null)
                if (cursor.isAfterLast()) {
                    filename = name;
                    id = itemId;
                } else {
                    filename = cursor.getString(cursor.getColumnIndex(Items.NAME));
                    id = cursor.getString(cursor.getColumnIndex(Items._ID));
                }

            file = new File(path + "/myHoardFiles", filename + med.id + ".jpg");

            if (media!=null)
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
                values.put(DataStorage.Media.ID_SERVER, med.getId());
                values.put(DataStorage.Media.CREATED_DATE, Calendar.getInstance()
                        .getTime().getTime());
                values.put(DataStorage.Media.ID_ITEM, id);
                values.put(DataStorage.Media.SYNCHRONIZED, true);

                cursor = getContentResolver().query(DataStorage.Media.CONTENT_URI,
                        new String[]{"count(*) AS count"},
                        DataStorage.Media.ID_ITEM + "=" + id, null, null);
                int countOfMedia = ERROR;
                if (cursor != null) {
                    cursor.moveToFirst();
                    countOfMedia = cursor.getInt(INDEX_OF_FIRST_ELEMENT);
                }
                if (countOfMedia == 0)
                    values.put(DataStorage.Media.AVATAR, true);
                else
                    values.put(DataStorage.Media.AVATAR, false);

                getContentResolver().insert(DataStorage.Media.CONTENT_URI, values);
            } catch (IOException e) {
                sendError(e.toString());
            }
        } else {
            sendError(ERROR_CREATING_FOLDER);
        }
    }

    private boolean createFolderMyHoard(String path) {
        File folder = new File(path + "/myHoardFiles");
        return folder.exists() || folder.mkdir();
    }

    private void deleteNotFoundMedia(List<ItemMedia> media, String itemId) {
        Cursor cursor = getContentResolver().query(DataStorage.Media.CONTENT_URI,
                new String[]{DataStorage.Media.ID_SERVER},
                DataStorage.Media.ID_SERVER + " NOT" + createSelectionForMedia(media) + " AND " + DataStorage.Media.ID_ITEM + "="+itemId,
                null,
                null
        );
        if (cursor != null)
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String idServer = cursor.getString(cursor.getColumnIndex(DataStorage.Media.ID_SERVER));
                String where = DataStorage.Media.ID_SERVER + "=?";
                String[] args = new String[]{idServer};
                getContentResolver().delete(DataStorage.Media.CONTENT_URI, where, args);
            }
    }

    private void downloadCollections() {
        String url = userManager.getIp() + USERS_ENDPOINT + UserManager.getInstance().getToken().getUserId() + "/" + COLLECTIONS_ENDPOINT;
        Log.d("TAG", url);
        collectionCrud = new CRUDEngine<>(url, Collection.class);
        List<Collection> collections = new ArrayList<>();
        try {
            collections = collectionCrud.getList(userManager.getToken());
        } catch (RuntimeException re) {
            sendError(re.getMessage());
        }

        String selection = String.format(" IN (");
        for (Collection collection : collections) {
            if (selection.charAt(selection.length() - 1) != '(') {
                selection += ",";
            }
            selection += collection.getId();
        }
        selection += ")";

        Cursor cursor = getContentResolver().query(Collections.CONTENT_URI, new String[]{Collections.ID_SERVER, Collections.MODIFIED_DATE}, Collections.ID_SERVER + selection, null, null);
        HashMap<String, Long> idServerToModifiedDate = new HashMap<>();
        if (cursor != null)
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                idServerToModifiedDate.put(cursor.getString(cursor.getColumnIndex(Collections.ID_SERVER)), cursor.getLong(cursor.getColumnIndex(Collections.MODIFIED_DATE)));
            }

        for (Collection collection : collections) {
            if (!cancel) {
                if (idServerToModifiedDate.get(collection.getId()) == null) {
                    insert(collection);
                } else if (modifiedDateOnServerIsGraterThanInDatabase(collection.getModified_date(), idServerToModifiedDate.get(collection.getId()))) {
                    update(collection);
                }
            }
        }

        if (!cancel)
        deleteNotFoundCollections(selection);

        try {
            getContentResolver().applyBatch(DataStorage.AUTHORITY, operations);
        } catch (RemoteException | OperationApplicationException e) {
            sendError(e.toString());
        }
    }

    private void deleteNotFoundCollections(String selection) {
        Cursor cursor = getContentResolver().query(Collections.CONTENT_URI, new String[]{Collections.ID_SERVER}, Collections.ID_SERVER + " NOT" + selection, null, null);
        if (cursor != null)
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String idServer = cursor.getString(cursor.getColumnIndex(Collections.ID_SERVER));
                String where = Collections.ID_SERVER + "=?";
                String[] args = new String[]{idServer};
                getContentResolver().delete(Collections.CONTENT_URI, where, args);
            }
    }

    private boolean modifiedDateOnServerIsGraterThanInDatabase(String modifiedDate, Long modifiedDateDatabase) {
        try {
            long modifiedDateServer = new SimpleDateFormat(DATE_FORMAT).parse(modifiedDate).getTime();
            if (modifiedDateDatabase >= modifiedDateServer)
                return false;
        } catch (ParseException e) {
            return true;
        }
        return true;
    }


    private void update(Collection collection) {
        ContentValues values = getContentValuesForCollection(collection);

        String where = String.format("%s = %s", Collections.ID_SERVER, collection.getId());
        operations.add(ContentProviderOperation.
                        newUpdate(Collections.CONTENT_URI)
                        .withSelection(where, null)
                        .withValues(values)
                        .build()
        );
    }


    private void insert(Collection collection) {
        ContentValues values = getContentValuesForCollection(collection);

        operations.add(ContentProviderOperation
                .newInsert(Collections.CONTENT_URI)
                .withValues(values)
                .build());
    }

    private ContentValues getContentValuesForCollection(Collection collection) {
        ContentValues values = new ContentValues();
        values.put(Collections.ID_SERVER, collection.getId());
        values.put(Collections.NAME, collection.getName());
        if (collection.getDescription() != null)
            values.put(Collections.DESCRIPTION, collection.getDescription());
        String tags = "";
        if (collection.getTags() != null)
        for (String s : collection.getTags()) {
            tags = tags + "#" + s + " ";
        }
        values.put(Collections.TAGS, tags.trim());
        if (collection.getIfPublic())
            values.put(Collections.TYPE, TypeOfCollection.PUBLIC.getType());
        else
            values.put(Collections.TYPE, TypeOfCollection.PRIVATE.getType());
        values.put(Collections.ITEMS_NUMBER, collection.getItems_number());
        values.put(Collections.SYNCHRONIZED, true);

        try {
            if (collection.getModified_date() != null && collection.getCreated_date() != null) {
            java.util.Date modDate = new SimpleDateFormat(DATE_FORMAT).parse(collection.getModified_date());
            java.util.Date creDate = new SimpleDateFormat(DATE_FORMAT).parse(collection.getCreated_date());
            long modifiedDate = modDate.getTime();
            long createdDate = creDate.getTime();
            values.put(Collections.MODIFIED_DATE, modifiedDate);
            values.put(Collections.CREATED_DATE, createdDate);
            }
        } catch (ParseException e) {
            values.put(Collections.MODIFIED_DATE, Calendar.getInstance().getTimeInMillis());
            values.put(Collections.CREATED_DATE, Calendar.getInstance().getTimeInMillis());
            sendError(e.toString());
        }
        return values;
    }

    private void update(Item item) {
        ContentValues values = getContentValuesForItem(item);
        if (values != null) {
            String where = String.format("%s = %s", Items.ID_SERVER, item.getId());
            operations.add(ContentProviderOperation.
                            newUpdate(Items.CONTENT_URI)
                            .withSelection(where, null)
                            .withValues(values)
                            .build()
            );
        }
    }

    private void insert(Item item) {
        ContentValues values = getContentValuesForItem(item);
        if (values != null) {
            operations.add(ContentProviderOperation
                    .newInsert(Items.CONTENT_URI)
                    .withValues(values)
                    .build());
        }
    }

    private ContentValues getContentValuesForItem(Item item) {
        ContentValues values = new ContentValues();
        values.put(DataStorage.Items.ID_SERVER, item.id);
        values.put(Collections.SYNCHRONIZED, true);
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
        String[] projection = new String[]{Collections._ID, Collections.TYPE};
        String selection = String.format("%s = %s", Collections.ID_SERVER, item.collection);
        Cursor cursor = getContentResolver().query(uri, projection, selection, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getInt(1) != TypeOfCollection.OFFLINE.getType()) {
                values.put(DataStorage.Items.ID_COLLECTION, cursor.getString(0));
            } else return null;
        }
        return values;
    }
}
