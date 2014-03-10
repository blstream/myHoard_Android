package com.myhoard.app.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.myhoard.app.R;
import com.myhoard.app.provider.DataStorage;

/**
 * Created by Maciej Plewko on 04.03.14.
 */
public class ItemsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter adapter;
    private Context context;
    private ListView listView;
    private Long collectionID;

    public static final String Selected_Collection_ID = "id";


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        return inflater.inflate(R.layout.fragment_items_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = (ListView) view.findViewById(R.id.itemsList);
        listView.setEmptyView(view.findViewById(R.id.tvNoItems));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //TODO item clicked action
            }
        });

        // retrieving data from CollectionsListFragment
        Bundle bundle = this.getArguments();
        collectionID = bundle.getLong(Selected_Collection_ID);

        registerForContextMenu(listView);
    }

    // creates a new loader after initLoader() call
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // TODO  !custom sort!
        final String[] projection = {DataStorage.Items.TABLE_NAME + '.' + DataStorage.Items._ID,
                DataStorage.Items.TABLE_NAME + '.' + DataStorage.Items.NAME,
                DataStorage.Items.TABLE_NAME + '.' + DataStorage.Items.CREATED_DATE};
        //select items from selected collection
        //String selection = collectionID + " = " + DataStorage.Items.ID_COLLECTION;

        return new CursorLoader(context, DataStorage.Items.CONTENT_URI,
                projection, null, null, null);

    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        fillData();
    }

    private void fillData() {
        //Fields from the database
        String[] from = new String[]{DataStorage.Items._ID, DataStorage.Items.NAME,
                DataStorage.Items.CREATED_DATE};
        //UI fields to which the data is mapped
        int[] to = new int[]{R.id.item_name, R.id.item_creation_date};

        getLoaderManager().initLoader(0, null, this);
        adapter = new SimpleCursorAdapter(context, R.layout.item_row, null, from, to, 0);
        listView.setAdapter(adapter);
    }
}
