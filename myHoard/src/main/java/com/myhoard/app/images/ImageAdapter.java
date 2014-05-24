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

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.myhoard.app.R;
import com.myhoard.app.provider.DataStorage;

/**
 * Created by Rafał Soudani on 20.02.2014
 * Modified by Tomasz Nosal on 25.03.2014
 */

public class ImageAdapter extends CursorAdapter {
    private static final String EMPTY_COLLECTION = "0";
    private static final int COLLECTION_NO_PHOTO = 1;
    public final ImageLoader mImageLoader;

    public ImageAdapter(Context context) {
        super(context, null, 0);
        mImageLoader = new ImageLoader(context, COLLECTION_NO_PHOTO);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.collection_griditem, viewGroup, false);

        ViewHolder holder = new ViewHolder();
        if (v != null) {
            holder.name = (TextView) v.findViewById(R.id.tvSquareName);
            holder.tags = (TextView) v.findViewById(R.id.tvSquareTags);
            holder.img = (ImageView) v.findViewById(R.id.ivSquareAvatar);
            holder.count = (TextView) v.findViewById(R.id.tvSquareCount);
            holder.nameIndex = cursor.getColumnIndexOrThrow(DataStorage.Collections.NAME);
            holder.tagsIndex = cursor.getColumnIndexOrThrow(DataStorage.Collections.TAGS);
            holder.imgIndex = cursor.getColumnIndexOrThrow(DataStorage.Media.FILE_NAME);
            holder.countIndex = cursor.getColumnIndexOrThrow(DataStorage.Collections.ITEMS_NUMBER);

            v.setTag(holder);
        }

        bindView(v, context, cursor);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (cursor.isClosed()) return;

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.name.setText(cursor.getString(holder.nameIndex));
        holder.tags.setText(cursor.getString(holder.tagsIndex));

        if (cursor.getString(holder.countIndex) == null) {
            holder.count.setText(EMPTY_COLLECTION);
        } else {
            holder.count.setText(cursor.getString(holder.countIndex));
        }

        mImageLoader.DisplayImage(cursor.getString(holder.imgIndex), holder.img);


    }

    private static class ViewHolder {
        int nameIndex;
        int imgIndex;
        int countIndex;
        int tagsIndex;
        TextView name;
        TextView tags;
        TextView count;
        ImageView img;
    }


}
