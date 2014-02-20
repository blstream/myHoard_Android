package com.myhoard.app.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Interface for handling with creating and upgrading table.
 */
public abstract class DatabaseTable {

	protected String tableName;
	private int code;
	private Context context;

	public DatabaseTable(Context context, int code, String tableName) {
		this();
		this.code = code;
		this.context = context;
		this.tableName = tableName;
	}

	private DatabaseTable() {}

	public boolean containsCode(int code) {

		if (this.code == code)
			return true;

		return false;
	}

	protected Context getContext() {
		return context;
	}

	public abstract void createTable(SQLiteDatabase db);
	public abstract void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion);
	public abstract Cursor query(SQLiteDatabase db, String[] projection, String selection, String[] selectionArgs, String sortOrder);
	public abstract Uri insert(SQLiteDatabase db, ContentValues values);

	public int delete(SQLiteDatabase db, String selection, String[] selectionArgs) {
		return db.delete(tableName, selection, selectionArgs);
	}

	public int update(SQLiteDatabase db, ContentValues values, String selection, String[] selectionArgs) {
		return db.update(
				tableName,      // The database table name.
				values,         // A map of column names and new values to use.
				selection,      // The where clause column names.
				selectionArgs   // The where clause column values to select on.
		);
	}
}
