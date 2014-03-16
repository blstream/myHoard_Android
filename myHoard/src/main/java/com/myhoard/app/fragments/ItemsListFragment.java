package com.myhoard.app.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.myhoard.app.R;
import com.myhoard.app.provider.DataStorage;

/**
 * Created by Maciej Plewko on 04.03.14.
 */
public class ItemsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	public static final String Selected_Collection_ID = "id";
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int EDIT_ID = Menu.FIRST + 2;
    private static final int SHARE_ID = Menu.FIRST + 3; // Facebook
    private static final String[] PERMISSIONS = {"publish_actions"}; // Facebook

    private SimpleCursorAdapter adapter;
    private Context context;
    private ListView listView;
	private Long collectionID;

    private static String sortByName = DataStorage.Items.NAME + " ASC";
    private static String sortByDate = DataStorage.Items.TABLE_NAME + "." +
            DataStorage.Items.CREATED_DATE + " ASC";
    private static String sortOrder = sortByName;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        // call the method setHasOptionsMenu, to have access to the menu from the fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_items_list, container, false);
	}

    private Session.StatusCallback statusCallback = new SessionStatusCallback(); //Facebook
    private ProgressDialog mProgressDialog; //Facebook
    private int mItemPosition;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

        // Lifecycle Facebook session
        Session session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(getActivity(), null, statusCallback, savedInstanceState);
            }
            if (session == null) {
                session = new Session(getActivity());

            }
            Session.setActiveSession(session);
        }

		listView = (ListView) view.findViewById(R.id.itemsList);
		listView.setEmptyView(view.findViewById(R.id.tvNoItems));
        getLoaderManager().initLoader(0, null, this);
        bindData();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        // retrieving data from CollectionsListFragment
        Bundle bundle = this.getArguments();
        collectionID = bundle.getLong(Selected_Collection_ID);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
			                        int position, long id) {
				//TODO item clicked action
                // Create new fragment and transaction

                Fragment newFragment = new ElementFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                // Add arguments to opened fragment element
                Bundle b = new Bundle();
                b.putLong(ElementFragment.ID,id);
                b.putLong(ElementFragment.COLLECTION_ID,collectionID);
                newFragment.setArguments(b);

                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack
                transaction.replace(R.id.container, newFragment, "NewElement");
                transaction.addToBackStack("NewElement");

                // Commit the transaction
                transaction.commit();

            }
        });

		registerForContextMenu(listView);
	}

	@Override
	public void onResume() {
		super.onResume();
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Session.getActiveSession().addCallback(statusCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
        Session.getActiveSession().removeCallback(statusCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(getActivity(), requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
    }

    //create options menu with a MenuInflater to have all needed options visible in this fragment
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //set sort option visible in the ItemsListFragment
        menu.findItem(R.id.action_sort).setVisible(true);
        //set proper menu option title depending on the sort order
        if (sortOrder == sortByDate) {
            menu.findItem(R.id.action_sort).setTitle(R.string.action_sort_by_name);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, EDIT_ID, 0, R.string.menu_edit);
        menu.add(0, DELETE_ID, 1, R.string.menu_delete);
        menu.add(0, SHARE_ID, 2, R.string.menu_share); // Sharing on FB
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            // Sharing item from list
            case SHARE_ID:
                mItemPosition = info.position;
                share();
                return true;
        }

        return super.onContextItemSelected(item);
    }

    private void bindData() {
        //Fields from the database
        String[] from = new String[]{DataStorage.Items.NAME,
                DataStorage.Items.CREATED_DATE};
        //UI fields to which the data is mapped
        int[] to = new int[]{R.id.item_name, R.id.item_creation_date};
        adapter = new SimpleCursorAdapter(context, R.layout.item_row, null, from, to, 0);
        listView.setAdapter(adapter);
    }

    // creates a new loader after initLoader() call
	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // columns to be selected from the table
        final String[] projection = {DataStorage.Items.TABLE_NAME + '.' + DataStorage.Items._ID,
                DataStorage.Items.TABLE_NAME + '.' + DataStorage.Items.NAME,
                DataStorage.Items.TABLE_NAME + '.' + DataStorage.Items.CREATED_DATE};
        //TODO select items from selected collection
        //String selection = collectionID + " = " + DataStorage.Items.ID_COLLECTION;

        return new CursorLoader(context, DataStorage.Items.CONTENT_URI,
                projection, null, null, sortOrder);

    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
		adapter.changeCursor(data);
	}

	@Override
	public void onLoaderReset(Loader loader) {
		adapter.changeCursor(null);
	}

    public void itemsSortOrderChange(MenuItem item) {
        if (sortOrder.equals(sortByName)) {
            sortOrder = sortByDate;
            Toast.makeText(context, "ITEMS SORTED BY DATE", Toast.LENGTH_SHORT).show();
            item.setTitle(R.string.action_sort_by_name);
        } else if (sortOrder.equals(sortByDate)) {
            sortOrder = sortByName;
            Toast.makeText(context, "ITEMS SORTED BY NAME", Toast.LENGTH_SHORT).show();
            item.setTitle(R.string.action_sort_by_date);
        }
        getLoaderManager().restartLoader(0, null, this);
    }

    /*
    * Sharing on Facebook name/description/photos/location
    * TODO: sharing description/photos/location
    */
    private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {

            if (session != null && session.isOpened()) {
                mProgressDialog = ProgressDialog.show(getActivity(),"","In progress...",true);
                // Temporary sharing only element name
                Bundle postParams = prepareDataToShare();

                Request.Callback callback = new Request.Callback() {
                    public void onCompleted(Response response) {

                        FacebookRequestError error = response.getError();
                        if (error != null) {
                            Toast.makeText(getActivity()
                                            .getApplicationContext(),
                                    error.getErrorMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        } else {

                            Toast.makeText(getActivity()
                                            .getApplicationContext(),
                                    "Sharing succeeded",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                        mProgressDialog.dismiss();
                    }
                };

                Request postRequest = new Request(session, "me/feed", postParams, HttpMethod.POST, callback);
                RequestAsyncTask task = new RequestAsyncTask(postRequest);
                task.execute();

            }
        }
    }

    /*
    Opening Facebook session for publish
     */
    private void share() {
        Session session = Session.getActiveSession();
        Session.OpenRequest request = new Session.OpenRequest(this).setCallback(statusCallback);
        request.setPermissions(PERMISSIONS);

        if (!session.isOpened() && !session.isClosed()) {
            session.openForPublish(request);
        } else {
            Session.openActiveSession(getActivity(), this, true, statusCallback);
        }
    }
    /*
     Retrieving data that will be sharing on FB
     */
    private Bundle prepareDataToShare() {
        Cursor cursor = adapter.getCursor();
        cursor.moveToPosition(mItemPosition); // position on list
        String[] data = {cursor.getString(cursor.getColumnIndex(DataStorage.Items.NAME))};
        Bundle bundle = new Bundle();
        bundle.putString("message",data[0]); // Temporary post on fb only message with name
        return  bundle;
    }
}
