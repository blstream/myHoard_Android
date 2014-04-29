package com.myhoard.app.element;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myhoard.app.R;
import com.myhoard.app.model.Item;
import com.myhoard.app.provider.DataProvider;
import com.myhoard.app.provider.DataStorage;
import com.myhoard.app.views.ElementFragmentPager;

/**
 * Created by Sebastian Peryt on 28.04.14.
 */
public class ElementReadFragment extends Fragment {

    private ElementFragmentPager pagerAdapter;
    private ViewPager pager;
    private long elementId;
    private AsyncElementRead asyncElementRead;
    private TextView elementName, elementPosition, elementCollection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        element = getArguments().getParcelable("element");
        elementId = getArguments().getLong("elementId");
        getLoaderManager().initLoader(1, null, new LoaderImagesCallbacks());
//        asyncElementRead =
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_element_read, container,
                false);

        elementName = (TextView) v.findViewById(R.id.tvElementName);
        elementCollection = (TextView) v.findViewById(R.id.tvElementCategory);
        elementPosition = (TextView) v.findViewById(R.id.tvElementLocalisation);
        elementPosition.setOnClickListener(listener);

        new AsyncElementRead().execute((long) 1);

        pager = (ViewPager) v.findViewById(R.id.pager);

        return v;
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            pager.setCurrentItem(pagerAdapter.getCount(), true);
        }
    };

    private class LoaderImagesCallbacks implements
            LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String[] projection = {DataStorage.Media.FILE_NAME,
                    DataStorage.Media.CREATED_DATE, DataStorage.Media._ID,
                    DataStorage.Media.ID_ITEM};
            CursorLoader cursorLoader = new CursorLoader(getActivity(),
                    DataStorage.Media.CONTENT_URI, projection,
                    DataStorage.Media.ID_ITEM + " =? ",
                    new String[]{String.valueOf(elementId)},
                    DataStorage.Media.CREATED_DATE + " ASC");
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if(pagerAdapter == null) {
                pagerAdapter = new ElementFragmentPager(getChildFragmentManager(), data);
            }
            pagerAdapter.swapCursor(data);
            pager.setAdapter(pagerAdapter);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            pagerAdapter.swapCursor(null);
        }
    }

    private class AsyncElementRead extends AsyncTask<Long, Integer, Item> {

        @Override
        protected Item doInBackground(Long... params) {
            String[] projection = {DataStorage.Items.NAME,
                    DataStorage.Items.DESCRIPTION,
                    DataStorage.Items.ID_COLLECTION};
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
            element.setId(String.valueOf(elementId));
            element.setCollection(collection);
            element.setName(name);
            element.setDescription(description);
            return element;
        }

        @Override
        protected void onPostExecute(Item item) {
            elementName.setText(item.getName());
            elementCollection.setText(item.getCollection());
        }
    }
}
