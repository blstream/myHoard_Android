package com.myhoard.app.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

import com.myhoard.app.model.Collection;
import com.myhoard.app.provider.DataStorage.Items;
import com.myhoard.app.provider.DataStorage.Collections;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String DB_NAME = "mh.db";
	private static final int DB_VERSION = 4;

	private List<DatabaseTable> tables;

	public DatabaseHelper(Context context, List<DatabaseTable> tables) {
		super(context, DB_NAME, null, DB_VERSION);
		this.tables = tables;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.beginTransaction();
		try {
			for (DatabaseTable tb : tables) {
				tb.createTable(db);
			}
            //create trigger, where is his place?
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TRIGGER update_items_number AFTER INSERT ON " + Items.TABLE_NAME)
                    .append(" BEGIN ")
                    .append("UPDATE " + Collections.TABLE_NAME + " set " + Collections.ITEMS_NUMBER + " = ")
                    .append("(SELECT COUNT(*) from " + Items.TABLE_NAME)
                    .append(" WHERE " + Items.TABLE_NAME + "." + Items.ID_COLLECTION)
                    .append(" = " + Collections.TABLE_NAME + "." + Collections._ID + "); ")
                    .append("END");
            db.execSQL(sql.toString());
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.beginTransaction();
		try {
			for (DatabaseTable tb : tables) {
				tb.upgradeTable(db, oldVersion, newVersion);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if (!db.isReadOnly()) {
			// Enable foreign key constraints
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}
}
