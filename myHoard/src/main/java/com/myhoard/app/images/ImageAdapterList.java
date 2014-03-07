package com.myhoard.app.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.myhoard.app.R;


import java.util.ArrayList;

/**
 * Created by Piotr Brzozowski on 01.03.14.
 * ImageAdapterList class used to get list of element from data in SearchFragment
 */

// FIXME Adapter w tym przypadku powinien być dziedziczony po CUrsorAdapter
public class ImageAdapterList extends BaseAdapter {
    //Context object to get context of application
    private Context mContext;
    //ArrayList for collection element name from database
    private ArrayList<String> mCollectionElementName;
    //ArrayList for collection element avatar from database
    private ArrayList<String> mCollectionElementAvatar;
    //Constructor get context of application, ArrayList for collection element name, ArrayList for collection element avatar
    public ImageAdapterList(Context context, ArrayList<String> collectionElementName, ArrayList<String> collectionElementAvatar){
        //set private variables of class
        mContext = context;
        mCollectionElementName = collectionElementName;
        mCollectionElementAvatar = collectionElementAvatar;
    }

    @Override
    public int getCount() {
        return mCollectionElementName.size();
    }

    @Override
    public Object getItem(int position) {
        return mCollectionElementName.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Get LayoutInflater object to create view
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //New view for each of list elements matched to class
        View listView = inflater.inflate(R.layout.search_listitem,null);
        //Get TextView object from ListView
        assert listView != null;
        TextView textViewCollectionElementName = (TextView)listView.findViewById(R.id.textViewListRowSearch);
        //Get ImageView object from ListView
        ImageView imageViewCollectionElementAvatar = (ImageView)listView.findViewById(R.id.imageViewListRowSearch);
        //Set text for TextView in search_listitem
        textViewCollectionElementName.setText(mCollectionElementName.get(position));
        //Set image for ImageView in search_listitem
        if(mCollectionElementAvatar.get(position)==null){
            imageViewCollectionElementAvatar.setImageResource(R.drawable.ic_launcher);
        }
        else{
            imageViewCollectionElementAvatar.setImageBitmap(decodeSampledBitmapFromResource(mCollectionElementAvatar.get(position),100,100));
        }
        return listView;
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

    public static Bitmap decodeSampledBitmapFromResource(String path,
                                                         int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path,options);
    }
}

