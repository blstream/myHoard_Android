package com.myhoard.app.views;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.myhoard.app.element.ElementPhotoFragment;
import com.myhoard.app.element.ElementMapFragment;
import com.myhoard.app.provider.DataStorage;

/**
 * Created by Sebastian Peryt on 27.04.14.
 */
public class ElementFragmentPager extends FragmentPagerAdapter {

    private Cursor cursor;

    public ElementFragmentPager(FragmentManager fm, Cursor cursor) {
        super(fm);
        this.cursor = cursor;
    }

    @Override
    public Fragment getItem(int position) {
        if(cursor == null) {
            return null;
        }

        Fragment fragment;
        if(position == cursor.getCount()) {
            // TODO może być zaminione na statyczny obrazek
            // http://stackoverflow.com/questions/5324004/how-to-display-static-google-map-on-android-imageview
            fragment = new ElementMapFragment();
        } else {
            cursor.moveToPosition(position);
            fragment = new ElementPhotoFragment();
            Bundle args = new Bundle();
            String uriString = cursor.getString(cursor.getColumnIndex(DataStorage.Media.FILE_NAME));
            args.putString("uri", uriString);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public int getCount() {
        if(cursor == null) {
            return 0;
        } else {
            return cursor.getCount() + 1;
        }
    }

    public void swapCursor(Cursor c) {
        if (cursor == c)
            return;

        this.cursor = c;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return cursor;
    }
}
