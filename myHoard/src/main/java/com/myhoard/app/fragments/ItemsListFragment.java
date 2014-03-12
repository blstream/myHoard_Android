package com.myhoard.app.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
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

	public static final String Selected_Collection_ID = "id";
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int EDIT_ID = Menu.FIRST + 2;
	private SimpleCursorAdapter adapter;
	private Context context;
	private ListView listView;
	private Long collectionID;

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
                //Testing for collection element - author Sebastian Peryt
                // Create new fragment and transaction
                Fragment newFragment = new ElementFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                // DO NOT USE - Testing
                // Add arguments to opened fragment element
                //Bundle b = new Bundle();
                // put name
                //b.putString(ElementFragment.NAME,"NAME");
                // put description
                //b.putString(ElementFragment.DESCRIPTION,"DESCRIPTION");
                //newFragment.setArguments(b);
                // FROM THIS POINT -> USE

                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack
                transaction.replace(R.id.container, newFragment);
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
			}
		});

		// retrieving data from CollectionsListFragment
		Bundle bundle = this.getArguments();
		collectionID = bundle.getLong(Selected_Collection_ID);

		registerForContextMenu(listView);
	}

	@Override
	public void onResume() {
		super.onResume();
		fillData();
	}

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, EDIT_ID, 0, R.string.menu_edit);
        menu.add(0, DELETE_ID, 1, R.string.menu_delete);
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
}
