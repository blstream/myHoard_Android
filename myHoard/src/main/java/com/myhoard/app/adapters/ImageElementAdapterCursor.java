package com.myhoard.app.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.net.Uri;

import com.myhoard.app.R;
import com.myhoard.app.images.ImageLoader;
import com.myhoard.app.provider.DataStorage;

import java.io.IOException;


/**
 * Created by Sebastian Peryt on 06.04.14.
 */

public class ImageElementAdapterCursor extends CursorAdapter {
    private static final int NO_PHOTO_RESOURCES = 2;
    private static class ViewHolder {
        ImageView img;
        Uri path;
        Bitmap bmp;
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
		//Set image for ImageView in search_listitem
        String path = cursor.getString(cursor.getColumnIndex(DataStorage.Media.FILE_NAME));
		if (path == null) {
			viewHolder.img.setImageResource(R.drawable.nophoto);
		} else {
            viewHolder.path = Uri.parse(path);
		    try{
                viewHolder.bmp = MediaStore.Images.Media.getBitmap(context.getContentResolver(), viewHolder.path);
                viewHolder.img.setImageBitmap(viewHolder.bmp);
            } catch (IOException io) {
                viewHolder.img.setImageResource(R.drawable.nophoto);
                io.printStackTrace();
            }
        }

	}

	public static Bitmap decodeSampledBitmapFromResource(String path,
	                                                     int reqWidth, int reqHeight) {
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}

	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;
			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
}

