package com.myhoard.app.images;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;

/**
 * Created by Piotr Brzozowski on 2014-04-26.
 * Second cache level
 */
public class ImageCacheDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "image_cache";
    private static final String TABLE_NAME = "cache";

    private static final String KEY_URL = "url";
    private static final String KEY_BITMAP = "image_bitmap";

    private static final String CREATE_IMAGE_CACHE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
            "(" + KEY_BITMAP + " BLOB, " + KEY_URL + " TEXT" + ")";

    public ImageCacheDatabase(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_IMAGE_CACHE_TABLE);
        Log.d("CREATE DATABASE","CREATE DATABASE FOR CACHE");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addBitmap(String url,Bitmap bmp){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG,100,blob);
        byte[] byteArray = blob.toByteArray();
        values.put(KEY_BITMAP,byteArray);
        values.put(KEY_URL,url);
        if (db != null) {
            db.insert(TABLE_NAME,null,values);
        }
    }

    public Bitmap getBitmap(String url){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if (db != null) {
            cursor = db.query(TABLE_NAME,new String[]{
                KEY_BITMAP},KEY_URL + "='" + url + "'",null,null,null,null);
        }
        if (cursor != null) {
            if(cursor.moveToFirst()){
                return BitmapFactory.decodeByteArray(cursor.getBlob(0),0,cursor.getBlob(0).length);
            }
        }
        return null;
    }

    public void clearDatabase(){
        SQLiteDatabase db = this.getWritableDatabase();
        if (db != null) {
            db.delete(TABLE_NAME,null,null);
        }
    }
}
