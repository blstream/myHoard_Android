package com.myhoard.app.element;

import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.myhoard.app.R;
import com.myhoard.app.model.Item;
import com.myhoard.app.model.ItemLocation;
import com.myhoard.app.provider.DataStorage;
import com.myhoard.app.views.ElementFragmentPager;

/**
 * Created by Sebastian Peryt on 28.04.14.
 * Modified by Piotr Brzozowski
 */
public class ElementReadFragment extends Fragment {

    private ElementFragmentPager pagerAdapter;
    private ViewPager pager;
    private long elementId;
    private TextView elementName, elementPosition, elementCollection, elementDescription;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        elementId = getArguments().getLong("elementId");
        mContext = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_element_read, container,
                false);
        setHasOptionsMenu(true);
        elementName = (TextView) v.findViewById(R.id.tvElementName);
        elementCollection = (TextView) v.findViewById(R.id.tvElementCategory);
        elementDescription = (TextView) v.findViewById(R.id.tvElementDescription);
        elementPosition = (TextView) v.findViewById(R.id.tvElementLocalisation);
        elementPosition.setOnClickListener(listener);
        pager = (ViewPager) v.findViewById(R.id.pager);
        getLoaderManager().initLoader(1, null, new LoaderImagesCallbacks());
        return v;
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            pager.setCurrentItem(pagerAdapter.getCount(), true);
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.edit_element,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_remove:
                getAlertDialogToDelete();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getAlertDialogToDelete(){
        new AlertDialog.Builder(getActivity())
                .setTitle(mContext.getString(R.string.edit_colection_dialog_title))
                .setMessage(mContext.getString(R.string.edit_colection_dialog_message))
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteElement(elementId);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();
    }

    private void deleteElement(long id){
        ContentValues values = new ContentValues();
        values.put(DataStorage.Items.DELETED,true);
        AsyncQueryHandler asyncHandler =
                new AsyncQueryHandler(getActivity().getContentResolver()) { };
        asyncHandler.startUpdate(0,null,DataStorage.Items.CONTENT_URI,values,DataStorage.Items._ID + " = ?",
                new String[]{String.valueOf(id)});
        getActivity().finish();
    }

    private class LoaderImagesCallbacks implements
            LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String[] projection = {DataStorage.Media.FILE_NAME,
                    DataStorage.Media.CREATED_DATE, DataStorage.Media._ID,
                    DataStorage.Media.ID_ITEM};
            return new CursorLoader(getActivity(),
                    DataStorage.Media.CONTENT_URI, projection,
                    DataStorage.Media.ID_ITEM + " =? " + " AND NOT " + DataStorage.Media.DELETED,
                    new String[]{String.valueOf(elementId)},
                    DataStorage.Media.CREATED_DATE + " ASC");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if(pagerAdapter == null) {
                pagerAdapter = new ElementFragmentPager(getChildFragmentManager(), data, null);
            }
            pagerAdapter.swapCursor(data);
            new AsyncElementRead().execute();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            pagerAdapter.swapCursor(null);
        }
    }

    private class AsyncElementRead extends AsyncTask<Void, Integer, Item> {

        @Override
        protected Item doInBackground(Void... params) {
            String[] projection = {DataStorage.Items.NAME,
                    DataStorage.Items.DESCRIPTION,
                    DataStorage.Items.ID_COLLECTION,
                    DataStorage.Items.LOCATION_LAT,
                    DataStorage.Items.LOCATION_LNG,
                    DataStorage.Items.LOCATION};
            String[] selection = {String.valueOf(elementId)};
            Cursor cursorItems = getActivity().getContentResolver().query(DataStorage.Items.CONTENT_URI, projection, DataStorage.Items.TABLE_NAME + "." + DataStorage.Items._ID + " =? ", selection, DataStorage.Items.TABLE_NAME + "." + DataStorage.Items._ID + " DESC");

            cursorItems.moveToFirst();
            String[] projection2 = {DataStorage.Collections.NAME};
            String[] selection2 = {cursorItems.getString(cursorItems.getColumnIndex(DataStorage.Items.ID_COLLECTION))};
            Cursor cursorCollections = getActivity().getContentResolver().query(DataStorage.Collections.CONTENT_URI, projection2, DataStorage.Collections._ID + " =? ", selection2, DataStorage.Collections._ID + " DESC");
            cursorCollections.moveToFirst();

            Item element = new Item();
            String name = cursorItems.getString(cursorItems.getColumnIndex(DataStorage.Items.NAME));
            String description = cursorItems.getString(cursorItems.getColumnIndex(DataStorage.Items.DESCRIPTION));
            String collection = cursorCollections.getString(cursorCollections.getColumnIndex(DataStorage.Collections.NAME));
            String locationTxt = cursorItems.getString(cursorItems.getColumnIndex(DataStorage.Items.LOCATION));
            float lat = cursorItems.getFloat(cursorItems.getColumnIndex(DataStorage.Items.LOCATION_LAT));
            float lon = cursorItems.getFloat(cursorItems.getColumnIndex(DataStorage.Items.LOCATION_LNG));
            element.setId(String.valueOf(elementId));
            element.setCollection(collection);
            element.setName(name);
            element.setDescription(description);
            element.setLocationTxt(locationTxt);
            element.setLocation(new ItemLocation(lat,lon));

            return element;
        }

        @Override
        protected void onPostExecute(Item item) {
            elementName.setText(item.getName());
            elementCollection.setText(item.getCollection());
            elementDescription.setText(item.getDescription());
            elementPosition.setText(item.getLocationTxt());
            if(item.getLocation()!=null) {
                pagerAdapter.swapPosition(new LatLng(item.getLocation().lat,item.getLocation().lng));
            }
//            // TODO change test data
//            pagerAdapter.swapPosition(new LatLng(53.42778,14.553384));
            pager.setAdapter(pagerAdapter);
        }
    }
}
