package com.myhoard.app.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.myhoard.app.model.Collection;
import com.myhoard.app.model.Item;
import com.myhoard.app.provider.DataStorage.Collections;
import com.myhoard.app.provider.DataStorage.Items;
import com.myhoard.app.provider.DataStorage.Media;
import com.myhoard.app.provider.DataStorage.DeletedMedia;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DataProvider extends ContentProvider {

	// Constants used by the Uri matcher to choose an action based on the pattern of the incoming URI
	public static final int COLLECTIONS = 1;
	public static final int ITEMS = 2;
	public static final int MEDIA = 3;
	public static final int DELETED_MEDIA = 4;
	public static final int COLLECTIONS_ITEMS_MEDIA = 5;


	/**
	 * A UriMatcher instance
	 */
	private static final UriMatcher uriMatcher;
	private List<DatabaseTable> tables;
	// Handle to a new DatabaseHelper.
	private DatabaseHelper helper;

	static {
		// Creates and initializes the URI matcher
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(DataStorage.AUTHORITY, Collections.TABLE_NAME, COLLECTIONS);
		uriMatcher.addURI(DataStorage.AUTHORITY, Items.TABLE_NAME, ITEMS);
		uriMatcher.addURI(DataStorage.AUTHORITY, Media.TABLE_NAME, MEDIA);
		uriMatcher.addURI(DataStorage.AUTHORITY, DeletedMedia.TABLE_NAME, DELETED_MEDIA);
		uriMatcher.addURI(DataStorage.AUTHORITY, Collections.JOIN_NAME, COLLECTIONS_ITEMS_MEDIA);
	}

	@Override
	public boolean onCreate() {
		tables = new LinkedList<>();
		tables.add(new CollectionsTable(getContext(), COLLECTIONS));
		tables.add(new ItemsTable(getContext(), ITEMS));
		tables.add(new MediaTable(getContext(), MEDIA));
		tables.add(new DeletedMediaTable(getContext(), DELETED_MEDIA));

		// Creates a new helper object. Note that the database itself isn't opened until
		// something tries to access it, and it's only created if it doesn't already exist.
		helper = new DatabaseHelper(getContext(), tables);

		return false;
	}

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        ContentProviderResult[] result;
        try {
            result = super.applyBatch(operations);
            db.setTransactionSuccessful();
        } catch (OperationApplicationException e) {
            db.endTransaction();
            throw e;
        }
        db.endTransaction();
        return result;
    }

    @Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {


        /* JOIN dodany przez Rafała Soudani, aby wyswietlac miniatury w liscie kolekcji */
        SQLiteDatabase db = helper.getWritableDatabase();
        if (uriMatcher.match(uri) == COLLECTIONS_ITEMS_MEDIA){
            String tables = Collections.TABLE_NAME + " LEFT JOIN " + Items.TABLE_NAME + " ON " +
                    Collections.TABLE_NAME+"."+Collections._ID + " = " +Items.ID_COLLECTION+" LEFT JOIN " +
                    Media.TABLE_NAME + " ON " + Items.TABLE_NAME+"."+Items._ID + " = " + Media.ID_ITEM
                    ;

            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
            queryBuilder.setTables(tables);
            Cursor cursor = queryBuilder.query(db, projection, selection,
                    selectionArgs, DataStorage.Collections.TABLE_NAME+"."+DataStorage.Collections._ID, null, sortOrder);
            return cursor;
        }

		DatabaseTable table = findTable(uri);
		return table.query(db, projection, selection, selectionArgs, sortOrder);
	}

	private DatabaseTable findTable(Uri uri) {
		int code = uriMatcher.match(uri);
		for (DatabaseTable table : tables) {
			if (table.containsCode(code))
				return table;
		}
		throw new IllegalArgumentException("Unknown URI " + uri);
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		DatabaseTable table = findTable(uri);
		return table.insert(helper.getWritableDatabase(), values);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		DatabaseTable table = findTable(uri);
		return table.delete(helper.getWritableDatabase(), selection, selectionArgs);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		DatabaseTable table = findTable(uri);
		return table.update(helper.getWritableDatabase(), values, selection, selectionArgs);
	}

}
