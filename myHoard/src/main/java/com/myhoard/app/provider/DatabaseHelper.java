package com.myhoard.app.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

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
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TRIGGER update_items_number AFTER INSERT ON items ")
                    .append(" BEGIN ")
                    .append(" UPDATE collections set itemsNumber = ")
                    .append(" (SELECT COUNT(*) from items WHERE items.idCollection = collections._id); ")
                    .append(" END");
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
