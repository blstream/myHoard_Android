package com.myhoard.app.provider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Description
 *
 * @author gohilukk
 *         Date: 2014-05-17
 */
public class DeletedMediaTable extends DatabaseTable {

    private static final String DEFAULT_SORT_ORDER = DataStorage.Media._ID + " ASC";

    public DeletedMediaTable(Context context, int code) {
        super(context, code, DataStorage.DeletedMedia.TABLE_NAME);
    }

    @Override
    public void createTable(SQLiteDatabase db) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE " + tableName + " (")
                .append(DataStorage.DeletedMedia._ID + " INTEGER PRIMARY KEY, ")
                .append(DataStorage.DeletedMedia.ID_SERVER + " TEXT)");
        db.execSQL(sql.toString());
    }

    @Override
    public void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public Cursor query(SQLiteDatabase db, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(tableName);

        String orderBy;
        // If no sort order is specified, uses the default
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = DEFAULT_SORT_ORDER;
        } else {
            // otherwise, uses the incoming sort order
            orderBy = sortOrder;
        }

		/*
		 * Performs the query. If no problems occur trying to read the database, then a Cursor
                 * object is returned; otherwise, the cursor variable contains null. If no records were
                 * selected, then the Cursor object is empty, and Cursor.getCount() returns 0.
                 */
        Cursor c = qb.query(
                db,            // The database to query
                projection,    // The columns to return from the query
                selection,     // The columns for the where clause
                selectionArgs, // The values for the where clause
                null,          // don't group the rows
                null,          // don't filter by row groups
                orderBy        // The sort order
        );

        // Tells the Cursor what URI to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), DataStorage.DeletedMedia.CONTENT_URI);
        return c;

    }

    @Override
    public Uri insert(SQLiteDatabase db, ContentValues initialValues) {
        // A map to hold the new record's values.
        ContentValues values;

        // If the incoming values map is not null, uses it for the new values.
        if (initialValues != null) {
            values = new ContentValues(initialValues);

        } else {
            // Otherwise, create a new value map
            values = new ContentValues();
        }

        long rowId = db.insert(
                tableName,      // The table to insert into.
                null,           // A hack, SQLite sets this column value to null if values is empty.
                values          // A map of column names, and the values to insert into the columns.
        );

        // If the insert succeeded, the row ID exists.
        if (rowId > 0) {
            // Creates a URI with the note ID pattern and the new row ID appended to it.
            Uri uri = ContentUris.withAppendedId(DataStorage.DeletedMedia.CONTENT_URI, rowId);

            // Notifies observers registered against this provider that the data changed.
            getContext().getContentResolver().notifyChange(uri, null);
            return uri;
        }

        // If the insert didn't succeed, then the rowID is <= 0. Throws an exception.
        throw new SQLException("Failed to insert row into " + DataStorage.DeletedMedia.CONTENT_URI);
    }
}
