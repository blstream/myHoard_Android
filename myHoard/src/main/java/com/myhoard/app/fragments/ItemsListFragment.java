package com.myhoard.app.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.myhoard.app.R;
import com.myhoard.app.dialogs.FacebookShareDialog;
import com.myhoard.app.images.ImageAdapterList;
import com.myhoard.app.Managers.UserManager;
import com.myhoard.app.provider.DataStorage;

/**
 * Created by Maciej Plewko on 04.03.14.
 * List of items of collection
 */
public class ItemsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

	public static final String Selected_Collection_ID = "id";
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int EDIT_ID = Menu.FIRST + 2;
    private static final int SHARE_ID = Menu.FIRST + 3; // Facebook
    private static final String[] PERMISSIONS = {"publish_actions"}; // Facebook
    private static final String PUBLISH_PHOTOS = "me/photos";

    private Session.StatusCallback statusCallback = new SessionStatusCallback(); //Facebook
    private ProgressDialog mProgressDialog; //Facebook
    private int mItemPositionOnList;
    private String mMessageOnFb;

    private Context context;
    private GridView gridView;
	private Long collectionID;

    private static String sortByName = DataStorage.Items.NAME + " ASC";
    private static String sortByDate = DataStorage.Items.TABLE_NAME + "." +
            DataStorage.Items.CREATED_DATE + " ASC";
    private static String sortOrder = sortByName;
    private ImageAdapterList mImageAdapterList;
    private TextView mItemsDescription;
    private TextView mItemsTags;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_items_list, container, false);
        context = getActivity();
        // call the method setHasOptionsMenu, to have access to the menu from the fragment
        setHasOptionsMenu(true);
        //Create adapter to adapt data to individual list row
        mImageAdapterList = new ImageAdapterList(context, null, 0);
        assert v != null;
        mItemsDescription = (TextView) v.findViewById(R.id.tvItemsListDescription);
        mItemsTags = (TextView)v.findViewById(R.id.tvItemsListTags);
        return v;
	}


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

		gridView = (GridView) view.findViewById(R.id.gvItemsList);
		//gridView.setEmptyView(view.findViewById(R.id.tvNoItems));
        getLoaderManager().initLoader(0, null, this);
        bindData();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        // retrieving data from CollectionsListFragment
        Bundle bundle = this.getArguments();
        collectionID = bundle.getLong(Selected_Collection_ID);
        /* AWA:FIXME: Magic number 2
*/
        getLoaderManager().initLoader(2, null, this);
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                b.putString(ElementFragment.NAME,"Name");
                b.putString(ElementFragment.DESCRIPTION,"Description");
                newFragment.setArguments(b);

                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack
                /* AWA:FIXME: Hardcoded value
                    Umiesc w private final static String, int, etc....
                    lub w strings.xml
                    lub innym *.xml
                */
                transaction.replace(R.id.container, newFragment, "NewElement");
                transaction.addToBackStack("NewElement");

                // Commit the transaction
                transaction.commit();

            }
        });

		registerForContextMenu(gridView);
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
        if (requestCode == FacebookShareDialog.DIALOG_ID) {
            mMessageOnFb = data.getStringExtra(FacebookShareDialog.GET_RESULT);
            openFbSessionForShare();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        UserManager userManager = UserManager.getInstance();
        MenuItem item;
        if (userManager.isLoggedIn()) {
            item = menu.findItem(R.id.action_login);

            /* AWA:FIXME: Hardcoded value
                    Umiesc w private final static String, int, etc....
                    lub w strings.xml
                    lub innym *.xml
                */

            if(item!=null) item.setTitle("Logout");
            item = menu.findItem(R.id.action_synchronize);
            if(item!=null) item.setVisible(true);
        }
        else {
            item = menu.findItem(R.id.action_login);
            if(item!=null) item.setTitle("Login");
            item = menu.findItem(R.id.action_synchronize);
            if(item!=null) item.setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    //create options menu with a MenuInflater to have all needed options visible in this fragment
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item;
        //set sort option visible in the ItemsListFragment
        item = menu.findItem(R.id.action_sort);
        if(item!=null) item.setVisible(true);
        //Set search option visible in the ItemsListFragment
        item = menu.findItem(R.id.action_search);
        if(item!=null) item.setVisible(true);
        //Set new_collection option invisible in the ItemsListFragment
        item = menu.findItem(R.id.action_new_collection);
        if(item!=null) item.setVisible(false);
        //Set generator option invisible in the ItemsListFragment
        item = menu.findItem(R.id.action_generate);
        if(item!=null) item.setVisible(false);
        //Set login option invisible in the ItemsListFragment
        item = menu.findItem(R.id.action_login);
        if(item!=null) item.setVisible(false);
        //set proper menu option title depending on the sort order
        if (sortOrder.equals(sortByDate)) {

           item = menu.findItem(R.id.action_sort);
           if(item!=null) item.setTitle(R.string.action_sort_by_name);

            item = menu.findItem(R.id.action_sort);
            if(item!=null) item.setTitle(R.string.action_sort_by_name);

        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_search){
            Fragment newFragment = new SearchFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            // Add arguments to opened fragment element
            Bundle b = new Bundle();
            b.putLong("SearchFragment",collectionID);
            newFragment.setArguments(b);

            /* AWA:FIXME: Hardcoded value
                    Umiesc w private final static String, int, etc....
                    lub w strings.xml
                    lub innym *.xml
                */
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack
            transaction.replace(R.id.container, newFragment, "Search");
            transaction.addToBackStack("Search");

            // Commit the transaction
            transaction.commit();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        int groupId = 0;
        menu.add(groupId, EDIT_ID, EDIT_ID, R.string.menu_edit);
        menu.add(groupId, DELETE_ID, DELETE_ID, R.string.menu_delete);
        menu.add(groupId, SHARE_ID, SHARE_ID, R.string.menu_share); // Sharing on FB
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            // Sharing item from list
            case SHARE_ID:
                if(info!=null) {
                    mItemPositionOnList = info.position;
                    FacebookShareDialog facebookShareDialog = new FacebookShareDialog(setDefaultPostOnFb());
                    facebookShareDialog.setTargetFragment(this,FacebookShareDialog.DIALOG_ID);
                    facebookShareDialog.show(getFragmentManager(),null);
                }
                return true;
        }

        return super.onContextItemSelected(item);
    }

    private void bindData() {
        gridView.setAdapter(mImageAdapterList);
    }

    // creates a new loader after initLoader() call
	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        CursorLoader cursorLoader = null;
        switch(i){
            //Get all elements from collection
            case 0:
                // columns to be selected from the table
                final String[] projection = new String[]{DataStorage.Items.NAME, DataStorage.Media.AVATAR,
                        DataStorage.Items.TABLE_NAME + "." + DataStorage.Items._ID,DataStorage.Items.DESCRIPTION};

/* AWA:FIXME: Używaj String.format
*/
                String selection = collectionID + " = " + DataStorage.Items.ID_COLLECTION;
                cursorLoader =  new CursorLoader(context, DataStorage.Items.CONTENT_URI,
                        projection, selection, null, sortOrder);
                break;
            //Get concrete element form user editText
            case 1:
                //Get text to search from args object
                /* AWA:FIXME: Hardcoded value
                    Umiesc w private final static String, int, etc....
                    lub w strings.xml
                    lub innym *.xml
                    */
                String collectionElementText = bundle.getString("fragmentElement");
                //CursorLoader used to get data from user query
                cursorLoader = new CursorLoader(context, DataStorage.Items.CONTENT_URI,
                        new String[]{DataStorage.Items.NAME, DataStorage.Media.AVATAR, DataStorage.Items.TABLE_NAME + "." + DataStorage.Items._ID},
                        collectionID + " = " + DataStorage.Items.ID_COLLECTION + " AND ("+ DataStorage.Items.DESCRIPTION + " LIKE '%" + collectionElementText + "%' OR " + DataStorage.Items.NAME + " = '" + collectionElementText + "')", null, null);
                break;
            case 2:
                //CursorLoader used to get data from user query
                cursorLoader = new CursorLoader(context, DataStorage.Collections.CONTENT_URI,
                        new String[]{DataStorage.Collections.DESCRIPTION,DataStorage.Collections.TAGS,DataStorage.Collections.NAME},
                        collectionID + " = " + DataStorage.Collections._ID, null, null);
                break;
        }
        return cursorLoader;

    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        /* AWA:FIXME: Magic numbers
*/
		if(loader.getId()==2){
            data.moveToFirst();
            mItemsDescription.setText(data.getString(0));
            mItemsTags.setText(data.getString(1));
            getActivity().setTitle(data.getString(2));
        }
        else {
            mImageAdapterList.swapCursor(data);
        }
    }

	@Override
	public void onLoaderReset(Loader loader) {
		if(loader.getId()!=2) {
            mImageAdapterList.swapCursor(null);
        }
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
    * TODO Sharing group of items
    */
    private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            shareOnFacebook(session);
        }
    }

    public void shareOnFacebook(Session session) {
        if (session != null && session.isOpened()) {
            mProgressDialog = ProgressDialog.show(getActivity(),"",getString(R.string.progress),true);
            // Temporary sharing only element name
            Bundle postParams = prepareDataToShare(mMessageOnFb);

            Request.Callback callback = new Request.Callback() {
                public void onCompleted(Response response) {

                    FacebookRequestError error = response.getError();
                    if (error != null) {
                        if(getActivity().getApplicationContext()!=null) {
                            makeAndShowToast(error.getErrorMessage());
                        }
                    } else {
                       makeAndShowToast(getString(R.string.sharing_succeeded));
                    }
                    mProgressDialog.dismiss();
                }
            };

            Request postRequest = new Request(session, PUBLISH_PHOTOS, postParams, HttpMethod.POST, callback);
                /* AWA:FIXME: Niebezpieczne używanie wątku
        Brak anulowania tej operacji.
        Wyjście z Activity nie kończy wątku,
        należy o to zadbać.
        */
            RequestAsyncTask task = new RequestAsyncTask(postRequest);
            task.execute();

        }

    }

    public void makeAndShowToast(String message) {
        if(getActivity().getApplicationContext()!=null) {
            Toast.makeText(
                    getActivity().getApplicationContext(),
                    message,
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    /*
    Opening Facebook session for publish
     */
    private void openFbSessionForShare() {
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
    private Bundle prepareDataToShare(String message) {

        int photoSizeX = 1000;
        int photoSizeY = 1000;
        Bundle bundle = new Bundle();

        Cursor cursor = mImageAdapterList.getCursor();
        cursor.moveToPosition(mItemPositionOnList);
        // getting data form cursor
        String data = cursor.getString(cursor.getColumnIndex(DataStorage.Media.AVATAR));
        // Decoding image
        Bitmap image  = ImageAdapterList.decodeSampledBitmapFromResource(data,photoSizeX,photoSizeY);

        bundle.putParcelable("source",image);
        bundle.putString("message",message);
        return  bundle;
    }

    private String setDefaultPostOnFb() {
        String messageOnFB;
        Cursor cursor = mImageAdapterList.getCursor();
        cursor.moveToPosition(mItemPositionOnList);

        String[] data = {cursor.getString(cursor.getColumnIndex(DataStorage.Items.NAME)),
                         cursor.getString(cursor.getColumnIndex(DataStorage.Items.DESCRIPTION))};
        messageOnFB = String.format("%s \n %s",data[0],data[1]);
        return messageOnFB;
    }
}
