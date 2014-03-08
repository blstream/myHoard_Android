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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.CursorAdapter;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.myhoard.app.R;
import com.myhoard.app.provider.DataStorage;

/**
 * Created by Rafał Soudani on 20.02.2014
 */

public class ImageAdapter extends CursorAdapter {
    private int width = 200;

	public ImageAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        //noinspection deprecation
        width = display.getWidth()/2;  // deprecated from api 13
	}


	private Drawable resizeImage(Context c, Drawable d, int width) {

		Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
        //noinspection SuspiciousNameCombination
        Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap, width, width, false);
		return new BitmapDrawable(c.getResources(), bitmapResized);

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.collection_griditem, viewGroup, false);
		bindView(v, context, cursor);
		return v;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (cursor.isClosed()) return;

        ImageView ivPicture = (ImageView) view.findViewById(R.id.picture);
        TextView tvName = (TextView) view.findViewById(R.id.text);

		Drawable d;
		if (cursor.getString(cursor.getColumnIndex(DataStorage.Collections.AVATAR_FILE_NAME)) == null) {
			d = context.getResources().getDrawable(R.drawable.nophoto);
		} else {
			d = new BitmapDrawable(context.getResources(), cursor.getString(cursor.getColumnIndex(DataStorage.Collections.AVATAR_FILE_NAME)));
            //noinspection SuspiciousNameCombination
            d = resizeImage(context, d, width);
		}
		String name = cursor.getString(cursor.getColumnIndex(DataStorage.Collections.NAME));

		ivPicture.setImageDrawable(d);
		tvName.setText(name);

	}


}
