package com.myhoard.app.images;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import com.myhoard.app.R;

import java.util.ArrayList;


/**
 * Created by Dawid Graczyk on 18.05.14.
  */

public class FacebookImageAdapterList extends CursorAdapter {

    private static final int ELEMENT_NO_PHOTO = 2;
    private static final int ELEMENT_FILE_NAME_FROM_LOADER = 0;
    private final LayoutInflater mInflater;
    public ArrayList<Integer> mSelectedItems = new ArrayList<>();

    private static class ViewHolder {
        CheckBox box;
        ImageView img;
        String path;
    }
    public final ImageLoader mImageLoader;
	public FacebookImageAdapterList(Context context, Cursor c, int flags) {
	    super(context, c, flags);
	    mImageLoader = new ImageLoader(context,ELEMENT_NO_PHOTO);
        mInflater = LayoutInflater.from(context);
    }

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.item_facebook, parent, false);
        //Create ViewHolder to ListView scrolling smooth
        ViewHolder viewHolder = new ViewHolder();
        assert v != null;
        viewHolder.box = (CheckBox)v.findViewById(R.id.chbItemToShare);
        viewHolder.img = (ImageView)v.findViewById(R.id.ivSquareAvatarItem);
        v.setTag(viewHolder);
		bindView(v, context, cursor);
		return v;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
		//Set image for ImageView in search_listitem
		if (cursor.getString(ELEMENT_FILE_NAME_FROM_LOADER) == null) {
			viewHolder.img.setImageResource(R.drawable.element_empty);
		} else {
            viewHolder.path = cursor.getString(ELEMENT_FILE_NAME_FROM_LOADER);
            //Use LazyLoading for elements list
		    mImageLoader.DisplayImage(viewHolder.path,viewHolder.img);
        }
	}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView==null){
            convertView = mInflater.inflate(R.layout.item_facebook, null);
            holder = new ViewHolder();
            if(convertView !=null) {
                holder.box = (CheckBox) convertView.findViewById(R.id.chbItemToShare);
                holder.img = (ImageView) convertView.findViewById(R.id.ivSquareAvatarItem);
            }
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        setSelectOnCheckbox(holder,position);
        convertView.setTag(holder);
        bindView(convertView,mContext,mCursor);
        return convertView;
    }

    private void setSelectOnCheckbox(ViewHolder holder,int position) {
        mCursor.moveToPosition(position);
        if(isSelected(position))
            holder.box.setChecked(true);
        else holder.box.setChecked(false);
    }

    private boolean isSelected(int pos) {
        for(int i=0;i < mSelectedItems.size();i++) {
            if(mSelectedItems.get(i) == pos)
                return true;
        }
        return false;
    }
}

