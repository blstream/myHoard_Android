package com.myhoard.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.myhoard.app.R;
import com.myhoard.app.model.RowItem;

import java.util.List;

/*
* Created by Mateusz Czyszkiewicz on 2014-03-26.
*/

public class NavDrawerListAdapter extends ArrayAdapter<RowItem> {

    private final Context context;

    public NavDrawerListAdapter(Context context,int resourceId, List<RowItem> items) {
        super(context,resourceId,items);
        this.context = context;
    }



    public View getView(int position, View convertView, ViewGroup parent) {

        RowItem row = getItem(position);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.drawer_menu_row, parent, false);
        TextView txt = (TextView) rowView.findViewById(R.id.textViewRow);
        ImageView img = (ImageView) rowView.findViewById(R.id.icon);
        txt.setText(row.getTitle());
        img.setImageResource(row.getImageId());
        return rowView;
    }
}





