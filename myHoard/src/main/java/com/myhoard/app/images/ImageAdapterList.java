package com.myhoard.app.images;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.myhoard.app.R;


/**
 * Created by Piotr Brzozowski on 01.03.14.
 * ImageAdapterList class used to get list of element from data in SearchFragment
 */

public class ImageAdapterList extends CursorAdapter {

    static class ViewHolder {
        TextView name;
        ImageView img;
        String path;
    }

	public ImageAdapterList(Context context, Cursor c, int flags) {
		super(context, c, flags);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.item_griditem, parent, false);
        //Create ViewHolder to ListView scrolling smooth
        ViewHolder viewHolder = new ViewHolder();
        assert v != null;
        viewHolder.name = (TextView)v.findViewById(R.id.tvSquareNameItem);
        viewHolder.img = (ImageView)v.findViewById(R.id.ivSquareAvatarItem);
        v.setTag(viewHolder);
		bindView(v, context, cursor);
		return v;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
		//Set text for TextView in search_listitem
		viewHolder.name.setText(cursor.getString(0));
		//Set image for ImageView in search_listitem
		if (cursor.getString(1) == null) {
			viewHolder.img.setImageResource(R.drawable.nophoto);
		} else {
			//imageViewCollectionElementAvatar.setImageBitmap(decodeSampledBitmapFromResource(cursor.getString(1), 100, 100));
            //Load bitmap asynchronously
            viewHolder.path = cursor.getString(1);
		    loadBitmap(viewHolder);
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
    public void loadBitmap(ViewHolder viewHolder) {
        BitmapWorkerTask task = new BitmapWorkerTask();
        task.execute(viewHolder);
    }
    class BitmapWorkerTask extends AsyncTask<ViewHolder, Void, Bitmap> {
        //private final WeakReference<ImageView> imageViewReference;
        private ViewHolder data;

        /*public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<>(imageView);
        }*/

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(ViewHolder... params) {
            data = params[0];
            return decodeSampledBitmapFromResource(data.path, 100, 100);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                if (data.img != null) {
                    data.img.setImageBitmap(bitmap);
                }
            }
        }
    }
}

