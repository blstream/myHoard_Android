package com.myhoard.app.fragments;


import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;

import com.myhoard.app.R;
import com.myhoard.app.images.FacebookImageAdapterList;
import com.myhoard.app.provider.DataStorage;

import java.util.ArrayList;

/**
 * Created by Dawid Graczyk on 2014-05-18.
 */
public class FacebookItemsToShare extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOAD_ITEMS = 0;
    public static final String ITEM_ID = "itemId";
    private GridView mGridView;
    private FacebookImageAdapterList mFacebookImageAdapterList;
    private Context mContext;
    private long mElementId;
    private ArrayList<Long> mSelectedItems = new ArrayList<>();
    int mCount;

    private TextView tvSelectedItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.fragment__facebook_items_list, container, false);
        mContext = getActivity();
        mFacebookImageAdapterList = new FacebookImageAdapterList(mContext,null,0);
        return v;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGridView = (GridView)view.findViewById(R.id.gvItemsList);
        tvSelectedItems = (TextView)view.findViewById(R.id.tvItemsSelected);
        Bundle bundle = this.getArguments();
        mElementId = bundle.getLong(ITEM_ID);
        setOnClickActionOnGridView();
        getLoaderManager().initLoader(LOAD_ITEMS,null,this);
        bindData();
    }

    private void bindData() {
        mGridView.setAdapter(mFacebookImageAdapterList);
    }

    public void setOnClickActionOnGridView() {
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox box =(CheckBox) view.findViewById(R.id.chbItemToShare);
                if(box.isChecked()) box.setChecked(false);
                else box.setChecked(true);
                setSelectedItems(position, id);
            }
        });

    }

    private void setSelectedItems(int index,long id) {
        if(isSelected(id)) {
            mCount--;
            setCountOnView();
        }
        else {
            mSelectedItems.add(id);
            mCount++;
            setCountOnView();
        }
    }

    private boolean isSelected(long id) {
        for(long i : mSelectedItems) {
            if(id == i){
                mSelectedItems.remove(id);
                return true;
            }
        }
        return false;
    }

    private void setCountOnView() {
        String s = String.format("%s %d",getString(R.string.selected_items),mCount);
        tvSelectedItems.setText(s);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(LOAD_ITEMS,null,this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mFacebookImageAdapterList.mImageLoader.clearCache();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = null;
        String selection;
        if(id == LOAD_ITEMS) {
            selection = String.format("%s = %s",mElementId,DataStorage.Media.ID_ITEM);
            cursorLoader =  new CursorLoader(mContext, DataStorage.Media.CONTENT_URI,
                    new String[]{DataStorage.Media.FILE_NAME,
                            DataStorage.Media.TABLE_NAME + "." + DataStorage.Media._ID},
                    selection, null, null);
        }

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mFacebookImageAdapterList.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFacebookImageAdapterList.swapCursor(null);
    }

}
