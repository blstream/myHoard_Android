/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.myhoard.app.images;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.myhoard.app.R;
import com.myhoard.app.provider.DataStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rafał Soudani on 20.02.2014
 */
public class ImageAdapter extends BaseAdapter {

    Context context;
    private List<Item> items = new ArrayList<>();
    private LayoutInflater inflater;

    public ImageAdapter(Context c) {
        context = c;
        inflater = LayoutInflater.from(c);

        final ContentResolver resolver = c.getContentResolver();
        final String[] projection = DataStorage.Collections.TABLE_COLUMNS;
        Cursor cursor = resolver.query(DataStorage.Collections.CONTENT_URI, projection, null, null,
                DataStorage.Collections.NAME);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Drawable d;
                if (cursor.getString(cursor.getColumnIndex(DataStorage.Collections.AVATAR_FILE_NAME)) == null) {
                    d = c.getResources().getDrawable(R.drawable.nophoto);
                } else {
                    d = new BitmapDrawable(c.getResources(), cursor.getString(cursor.getColumnIndex(DataStorage.Collections.AVATAR_FILE_NAME)));
                    d = resizeImage(d);
                }
                String name = cursor.getString(cursor.getColumnIndex(DataStorage.Collections.NAME));
                cursor.getString(cursor.getColumnIndex(DataStorage.Collections.AVATAR_FILE_NAME));
                items.add(new Item(name, d));

            } while (cursor.moveToNext());
        }

    }

    //TODO: resizeImage() correctly
    private Drawable resizeImage(Drawable d) {

        Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
        return new BitmapDrawable(context.getResources(), bitmapResized);

    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        ImageView picture;
        TextView name;

        if (v == null) {
            v = inflater.inflate(R.layout.collection_griditem, viewGroup, false);
            if (v != null) {
                v.setTag(R.id.picture, v.findViewById(R.id.picture));
                v.setTag(R.id.text, v.findViewById(R.id.text));
            }
        }

        if (v != null) {
            picture = (ImageView) v.getTag(R.id.picture);
            name = (TextView) v.getTag(R.id.text);

            Item item = (Item) getItem(i);

            if (picture != null) {
                picture.setImageDrawable(item.drawableId);
            }
            if (name != null) {
                name.setText(item.name);
            }
        }


        return v;
    }

    private class Item {
        final String name;
        final Drawable drawableId;

        Item(String name, Drawable drawableId) {
            this.name = name;
            this.drawableId = drawableId;
        }
    }
}
