package com.myhoard.app.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.myhoard.app.Managers.UserManager;
import com.myhoard.app.R;
import com.myhoard.app.activities.ElementActivity;
import com.myhoard.app.dialogs.FacebookShareDialog;
import com.myhoard.app.images.ImageAdapterList;
import com.myhoard.app.images.ImageLoader;
import com.myhoard.app.provider.DataStorage;

/**
 * Created by Maciej Plewko on 04.03.14.
 * Modified by Piotr Brzozowski, Dawid Graczyk, Rafal Soudani, Sebastian Peryt
 * List of items of collection
 */
public class ItemsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

	public static final String Selected_Collection_ID = "id";
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int SHARE_ID = Menu.FIRST + 3; // Facebook
    private static final String[] PERMISSIONS = {"publish_actions"}; // Facebook
    private static final String PUBLISH_PHOTOS = "me/photos";
    private static final int LOAD_COLLECTION_NAME_AND_DESCRIPTION = 2;
    private static final int LOAD_COLLECTION_ELEMENTS = 0;
    private static final String NEW_SEARCH_FRAGMENT_NAME = "SearchFragment";
    private static final String SEARCH_COLLECTION_ID = "SearchCollection";
    private static final String NEW_FACEBOOK_FRAGMENT_NAME = "FacebookFragment";

    private Session.StatusCallback statusCallback = new SessionStatusCallback(); //Facebook
    private ProgressDialog mProgressDialog; //Facebook
    private int mItemPositionOnList;
    private String mMessageOnFb;

    private Context mContext;
    private GridView mGridView;
    private ImageView mEmptyView;
	private Long mCollectionID;
    private ImageAdapterList mImageAdapterList;
    private TextView mItemsDescription;
    private TextView mItemsTags;

    private static TextView sortByNameTabText;
    private static TextView sortByDateTabText;
    private static final String LABEL_BY_NAME_ASC = "A-Z";
    private static final String LABEL_BY_NAME_DESC = "Z-A";
    private static final String LABEL_BY_DATE_ASC = "< DATE";
    private static final String LABEL_BY_DATE_DESC = "> DATE";
    private static final String DEFAULT_SORT = DataStorage.Items.NAME;
    private static String sortOrder = DEFAULT_SORT;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_items_list, container, false);
        mContext = getActivity();
        // call the method setHasOptionsMenu, to have access to the menu from the fragment
        setHasOptionsMenu(true);
        //Create adapter to adapt data to individual list row
        mImageAdapterList = new ImageAdapterList(mContext, null, 0);
        assert v != null;
        mItemsDescription = (TextView) v.findViewById(R.id.tvItemsListDescription);
        mItemsTags = (TextView)v.findViewById(R.id.tvItemsListTags);
        mEmptyView = (ImageView)v.findViewById(R.id.imageViewEmptyList);
        mItemsDescription.setVisibility(View.INVISIBLE);
        mItemsTags.setVisibility(View.INVISIBLE);
        mEmptyView.setVisibility(View.INVISIBLE);
        return v;
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

        // currently moved to onResume()
        //setSortTabs();

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

		mGridView = (GridView) view.findViewById(R.id.gvItemsList);
        getLoaderManager().initLoader(LOAD_COLLECTION_ELEMENTS, null, this);
        bindData();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        // retrieving data from CollectionsListFragment
        Bundle bundle = this.getArguments();
        mCollectionID = bundle.getLong(Selected_Collection_ID);
        getLoaderManager().initLoader(LOAD_COLLECTION_NAME_AND_DESCRIPTION, null, this);
		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
			                        int position, long id) {
                Intent in = new Intent(getActivity(), ElementActivity.class);
                in.putExtra("elementId",id);
                startActivity(in);
            }
        });
        registerForContextMenu(mGridView);
	}

    private void setSortTabs() {
        //getting the action bar from the MainActivity
        final ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        //adding tabs to the action bar
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        //tab sort by name
        ActionBar.Tab tabSortByName = actionBar.newTab();
        tabSortByName.setCustomView(R.layout.sort_tab);
        sortByNameTabText = (TextView) tabSortByName.getCustomView().findViewById(R.id.tab_text);
        setSelectedTabByNameText(LABEL_BY_NAME_ASC);
        ActionBar.TabListener sortByNameTabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                sortByName();
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                sortByNameTabUnselected();
            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                sortByName();
            }
        };
        tabSortByName.setTabListener(sortByNameTabListener);
        actionBar.addTab(tabSortByName);

        //tab sort by date
        ActionBar.Tab tabSortByDate = actionBar.newTab();
        tabSortByDate.setCustomView(R.layout.sort_tab);
        sortByDateTabText = (TextView) tabSortByDate.getCustomView().findViewById(R.id.tab_text);
        setUnselectedTabByDateText(LABEL_BY_DATE_ASC);
        ActionBar.TabListener sortByDateTabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                sortByDate();
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                sortByDateTabUnselected();

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                sortByDate();
            }
        };
        tabSortByDate.setTabListener(sortByDateTabListener);
        actionBar.addTab(tabSortByDate);
    }

    private void resetActionBarNavigationMode() {
        //getting the action bar from the MainActivity
        final ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.removeAllTabs();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }

	@Override
	public void onResume() {
		super.onResume();
        getLoaderManager().restartLoader(LOAD_COLLECTION_ELEMENTS, null, this);
        setSortTabs();
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
        resetActionBarNavigationMode();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mImageAdapterList.mImageLoader.clearCache();
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
            if(item!=null) item.setTitle(R.string.logout);
        }
        else {
            item = menu.findItem(R.id.action_login);
            if(item!=null) item.setTitle(R.string.Login);
        }
        super.onPrepareOptionsMenu(menu);
    }

    //create options menu with a MenuInflater to have all needed options visible in this fragment
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.item_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Bundle b = new Bundle();
        Fragment newFragment;
        FragmentTransaction transaction;

        switch(item.getItemId()) {
            case R.id.action_search:
            newFragment = new SearchFragment();
            transaction = getFragmentManager().beginTransaction();

            // Add arguments to opened fragment element
            b.putLong(SEARCH_COLLECTION_ID,mCollectionID);
            newFragment.setArguments(b);

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack
            transaction.replace(R.id.container, newFragment, NEW_SEARCH_FRAGMENT_NAME);
            transaction.addToBackStack(NEW_SEARCH_FRAGMENT_NAME);

            // Commit the transaction
            transaction.commit();
                break;

            case R.id.action_new_element:
                Intent in = new Intent(getActivity(),ElementActivity.class);
                in.putExtra("categoryId",mCollectionID);
                startActivity(in);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        int groupId = 0;
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
                    /*FacebookShareDialog facebookShareDialog = new FacebookShareDialog();
                    facebookShareDialog.setDefaultPostOnFb(setDefaultPostOnFb());
                    facebookShareDialog.setTargetFragment(this,FacebookShareDialog.DIALOG_ID);
                    facebookShareDialog.show(getFragmentManager(),null);*/
                    newFacebookShareFragment(info.id);
                }
                return true;
            case DELETE_ID:
                new AlertDialog.Builder(getActivity())
                        .setTitle(mContext.getString(R.string.edit_colection_dialog_title))
                        .setMessage(mContext.getString(R.string.edit_colection_dialog_message))
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (info != null) {
                                    deleteElement(info.id);
                                }
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
                return true;
        }

        return super.onContextItemSelected(item);
    }

    private void deleteElement(long id){
        ContentValues values = new ContentValues();
        values.put(DataStorage.Items.DELETED,true);
        AsyncQueryHandler asyncHandler =
                new AsyncQueryHandler(getActivity().getContentResolver()) { };
        asyncHandler.startUpdate(0,null,DataStorage.Items.CONTENT_URI,values,DataStorage.Items._ID + " = ?",
                new String[]{String.valueOf(id)});
        getLoaderManager().restartLoader(LOAD_COLLECTION_ELEMENTS, null, this);
        bindData();
    }

    private void bindData() {
        mGridView.setAdapter(mImageAdapterList);
    }

    // creates a new loader after initLoader() call
	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        CursorLoader cursorLoader = null;
        String selection;
        switch(i){
            //Get all elements from collection
            case LOAD_COLLECTION_ELEMENTS:
                selection = String.format("%s = %s and %s!=%d and (%s=%d or %s is null)",mCollectionID,DataStorage.Items.ID_COLLECTION,
                        DataStorage.Items.TABLE_NAME+"."+DataStorage.Items.DELETED,1,
                        DataStorage.Media.AVATAR,1,DataStorage.Media.AVATAR);
                cursorLoader =  new CursorLoader(mContext, DataStorage.Items.CONTENT_URI,
                        new String[]{DataStorage.Items.NAME, DataStorage.Media.FILE_NAME,
                                DataStorage.Items.TABLE_NAME + "." + DataStorage.Items._ID,DataStorage.Items.DESCRIPTION},
                        selection, null, sortOrder);
                break;
            //Get name and description of elements collection
            case LOAD_COLLECTION_NAME_AND_DESCRIPTION:
                selection = String.format("%s = %s",mCollectionID,DataStorage.Collections._ID);
                cursorLoader = new CursorLoader(mContext, DataStorage.Collections.CONTENT_URI,
                        new String[]{DataStorage.Collections.DESCRIPTION,DataStorage.Collections.TAGS,DataStorage.Collections.NAME},
                        selection, null, null);
                break;
        }
        return cursorLoader;

    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
		if(loader.getId()==LOAD_COLLECTION_NAME_AND_DESCRIPTION){
            data.moveToFirst();
            mItemsDescription.setText(data.getString(0));
            mItemsTags.setText(data.getString(1));
            getActivity().setTitle(data.getString(2));
        }
        else {
            if(data.getCount()==0){
                mItemsDescription.setVisibility(View.INVISIBLE);
                mItemsTags.setVisibility(View.INVISIBLE);
                mEmptyView.setVisibility(View.VISIBLE);
            } else{
                mItemsDescription.setVisibility(View.VISIBLE);
                mItemsTags.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.INVISIBLE);
            }
            mImageAdapterList.swapCursor(data);
        }
    }

	@Override
	public void onLoaderReset(Loader loader) {
		if(loader.getId()!=LOAD_COLLECTION_NAME_AND_DESCRIPTION) {
            mImageAdapterList.swapCursor(null);
        }
	}

    private void sortByName() {
        String sortByNameAscending = DataStorage.Items.NAME + " ASC";
        if (sortOrder.equals(sortByNameAscending)) {
            sortOrder = DataStorage.Items.NAME + " DESC";
            setSelectedTabByNameText(LABEL_BY_NAME_DESC);
        } else {
            sortOrder = sortByNameAscending;
            setSelectedTabByNameText(LABEL_BY_NAME_ASC);
        }
        getLoaderManager().restartLoader(0, null, this);
    }

    private void sortByDate() {
        String sortByDateAscending = DataStorage.Items.TABLE_NAME + "." +
                DataStorage.Items.CREATED_DATE + " ASC";
        if (sortOrder.equals(sortByDateAscending)) {
            sortOrder = DataStorage.Items.TABLE_NAME + "." +
                    DataStorage.Items.CREATED_DATE + " DESC";
            setSelectedTabByDateText(LABEL_BY_DATE_DESC);
        } else {
            sortOrder = sortByDateAscending;
            setSelectedTabByDateText(LABEL_BY_DATE_ASC);
        }
        getLoaderManager().restartLoader(0, null, this);
    }

    private void sortByNameTabUnselected() {
        setUnselectedTabByNameText(LABEL_BY_NAME_ASC);
        sortOrder = DEFAULT_SORT;
    }

    private void sortByDateTabUnselected() {
        setUnselectedTabByDateText(LABEL_BY_DATE_ASC);
        sortOrder = DEFAULT_SORT;
    }

    private void setSelectedTabByNameText(String text) {
        sortByNameTabText.setTextColor(getResources().getColor(R.color.selected_tab_text_color));
        sortByNameTabText.setText(text);
    }

    private void setSelectedTabByDateText(String text) {
        sortByDateTabText.setTextColor(getResources().getColor(R.color.selected_tab_text_color));
        sortByDateTabText.setText(text);
    }

    private void setUnselectedTabByNameText(String text) {
        sortByNameTabText.setTextColor(getResources().getColor(R.color.black));
        sortByNameTabText.setText(text);
    }

    private void setUnselectedTabByDateText(String text) {
        sortByDateTabText.setTextColor(getResources().getColor(R.color.black));
        sortByDateTabText.setText(text);
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
            mProgressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.progress), true);
            Bundle postParams = prepareDataToShare(mMessageOnFb);
            if (postParams != null) {
                Request.Callback callback = new Request.Callback() {
                    public void onCompleted(Response response) {

                        FacebookRequestError error = response.getError();
                        if (error != null) {
                            if (getActivity().getApplicationContext() != null) {
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
                RequestAsyncTask mFacebookTask = new RequestAsyncTask(postRequest);
                mFacebookTask.execute();

            } else {
                mProgressDialog.dismiss();
                makeAndShowToast(getString(R.string.no_photo));
            }
        }
    }

    public void makeAndShowToast(String message) {
        if(getActivity().getApplicationContext()!=null) {
            Toast.makeText(
                    getActivity().getApplicationContext(),
                    message,
                    Toast.LENGTH_SHORT
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
        request.setLoginBehavior(SessionLoginBehavior.SUPPRESS_SSO);
        if (!session.isOpened() && !session.isClosed()) {
            session.openForPublish(request);
        } else if (session.getState().equals(SessionState.CLOSED_LOGIN_FAILED) || session.isClosed()) {
            session.close();

            session = new Session.Builder(getActivity()).build();
            session.addCallback(statusCallback);
            Session.setActiveSession(session);
            session.openForPublish(request);
        }
        else {
            Session.openActiveSession(getActivity(), this, true, statusCallback);
        }
    }
    /*
     Retrieving data that will be sharing on FB
     */
    private Bundle prepareDataToShare(String message) {

        int photoSizeX = 800;
        int photoSizeY = 600;
        Bundle bundle = new Bundle();

        Cursor cursor = mImageAdapterList.getCursor();
        cursor.moveToPosition(mItemPositionOnList);
        // getting data form cursor
        String data = cursor.getString(cursor.getColumnIndex(DataStorage.Media.FILE_NAME));
        if(data != null) {
            // Decoding image
            Bitmap image = ImageLoader.decodeSampledBitmapFromResource(data, photoSizeX, photoSizeY);

            bundle.putParcelable("source", image);
            bundle.putString("message", message);
            return bundle;
        } else return null;

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

    private void newFacebookShareFragment(long id) {
        Fragment newFragment = new FacebookItemsToShare();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putLong(FacebookItemsToShare.ITEM_ID,id);
        newFragment.setArguments(bundle);
        transaction.replace(R.id.container,newFragment,NEW_FACEBOOK_FRAGMENT_NAME);
        transaction.addToBackStack(NEW_FACEBOOK_FRAGMENT_NAME);
        transaction.commit();
    }
}
