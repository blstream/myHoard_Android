package com.myhoard.app.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.myhoard.app.R;
import com.myhoard.app.images.ImageLoader;
import java.util.ArrayList;

/**
 * Created by Sebastian on 06.04.14.
 */
public class ImageElementAdapterList extends ArrayAdapter<Uri> {

    private Context context;
    private ArrayList<Uri> data;

    public ImageElementAdapterList(Context context, int resource, ArrayList<Uri> objects) {
        super(context, resource, objects);
        this.context = context;
        this.data = objects;
    }

    public ImageElementAdapterList(Context context, int resource, int textViewResourceId, ArrayList<Uri> objects) {
        super(context, resource, textViewResourceId, objects);
        this.context = context;
        this.data = objects;
    }

    private static class ViewHolder {
        ImageView img;
        Bitmap bmp;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = LayoutInflater.from(context);
            row = inflater.inflate(R.layout.element_image_griditem, parent, false);

            holder = new ViewHolder();
            holder.img = (ImageView)row.findViewById(R.id.ivSquareAvatarItem);

            row.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)row.getTag();
        }
        Uri uri = null;
        if(position != 0) {
            uri = data.get(position);
        }

        if(uri==null)
        {
            holder.img.setImageResource(R.drawable.nophoto);
        } else {
            //holder.bmp = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            holder.bmp = ImageLoader.decodeSampledBitmapFromResource(uri.toString(),100,100);
            holder.img.setImageBitmap(holder.bmp);
        }
        return row;
    }
}
