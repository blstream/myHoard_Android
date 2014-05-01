package com.myhoard.app.views;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.google.android.gms.maps.model.LatLng;
import com.myhoard.app.element.ElementPhotoFragment;
import com.myhoard.app.element.ElementStaticMapFragment;
import com.myhoard.app.provider.DataStorage;

/**
 * Created by Sebastian Peryt on 27.04.14.
 */
public class ElementFragmentPager extends FragmentPagerAdapter {

    private Cursor cursor;
    private LatLng position;

    public ElementFragmentPager(FragmentManager fm, Cursor cursor, LatLng position) {
        super(fm);
        this.cursor = cursor;
        this.position = position;
    }

    public void swapPosition(LatLng position) {
        this.position = position;
    }

    @Override
    public Fragment getItem(int position) {
        if(cursor == null) {
            return null;
        }

        Fragment fragment;
        if(position == cursor.getCount()) {
            fragment = new ElementStaticMapFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable("position",this.position);
            fragment.setArguments(bundle);
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
