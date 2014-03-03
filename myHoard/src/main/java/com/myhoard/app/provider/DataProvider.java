package com.myhoard.app.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import com.myhoard.app.provider.DataStorage.Collections;
import com.myhoard.app.provider.DataStorage.Items;
import com.myhoard.app.provider.DataStorage.Media;

import java.util.LinkedList;
import java.util.List;

public class DataProvider extends ContentProvider {

	// Constants used by the Uri matcher to choose an action based on the pattern of the incoming URI
	public static final int COLLECTIONS = 1;
	public static final int ELEMENTS = 2;
    public static final int MEDIA = 3;

	private List<DatabaseTable> tables;

	/**
	 * A UriMatcher instance
	 */
	private static final UriMatcher uriMatcher;

	// Handle to a new DatabaseHelper.
	private DatabaseHelper helper;

	static {
		// Creates and initializes the URI matcher
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(DataStorage.AUTHORITY, Collections.TABLE_NAME, COLLECTIONS);
		uriMatcher.addURI(DataStorage.AUTHORITY, Items.TABLE_NAME, ELEMENTS);
        uriMatcher.addURI(DataStorage.AUTHORITY, Media.TABLE_NAME, MEDIA);
	}

	@Override
	public boolean onCreate() {
		tables = new LinkedList<>();
		tables.add(new CollectionsTable(getContext(), COLLECTIONS));
		tables.add(new ItemsTable(getContext(), ELEMENTS));
        tables.add(new MediaTable(getContext(), MEDIA));

		// Creates a new helper object. Note that the database itself isn't opened until
		// something tries to access it, and it's only created if it doesn't already exist.
		helper = new DatabaseHelper(getContext(), tables);

		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		DatabaseTable table = findTable(uri);
		return table.query(helper.getWritableDatabase(), projection, selection, selectionArgs, sortOrder);
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
