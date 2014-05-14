package com.myhoard.app.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.net.Uri;
import com.myhoard.app.R;
import com.myhoard.app.images.ImageLoader;
import com.myhoard.app.provider.DataStorage;


/**
 * Created by Sebastian Peryt on 06.04.14.
 * Modified by Piotr Brzozowski on 14.05.14.
 */

public class ImageElementAdapterCursor extends CursorAdapter {
    private static final int NO_PHOTO_RESOURCES = 2;
    private static class ViewHolder {
        ImageView img;
        Uri path;
    }
    private final ImageLoader mImageLoader;
	public ImageElementAdapterCursor(Context context, Cursor c, int flags) {
	    super(context, c, flags);
	    mImageLoader = new ImageLoader(context,NO_PHOTO_RESOURCES);
    }

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.element_image_griditem, parent, false);
        //Create ViewHolder to ListView scrolling smooth
        ViewHolder viewHolder = new ViewHolder();
        assert v != null;
        viewHolder.img = (ImageView)v.findViewById(R.id.ivSquareAvatarItem);
        v.setTag(viewHolder);
		bindView(v, context, cursor);
		return v;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
		//Set image for ImageView in edit element view
        String path = cursor.getString(cursor.getColumnIndex(DataStorage.Media.FILE_NAME));
		if (path == null) {
			viewHolder.img.setImageResource(R.drawable.nophoto);
		} else {
            viewHolder.path = Uri.parse(path);
            mImageLoader.DisplayImage(viewHolder.path.toString(),viewHolder.img);
        }
    }
}

