
package com.myhoard.app.fragments;

import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import com.myhoard.app.R;
import com.myhoard.app.activities.ElementActivity;
import com.myhoard.app.images.ImageAdapterList;
import com.myhoard.app.provider.DataStorage;

/**
 * Created by Piotr Brzozowski on 01.03.14.
 * SearchFragment class used to search concrete sentence in table of elements
 */
public class SearchFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TEXT_TO_SEARCH = "TextSearch";
    private static final String SEARCH_BY_NAME_TAB = "name";
    private static final String SEARCH_ALL_TAB = "all";
    private static final String SEARCH_BY_DESCRIPTION_TAB = "description";
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int TEXT_TO_SEARCH_MIN_LENGTH = 2;
    private static final int SEARCH_ALL = 0;
    private static final int SEARCH_BY_NAME = 1;
    private static final int SEARCH_BY_DESCRIPTION = 2;
    private static int sSelectedTypeOfSearch = SEARCH_ALL;
    private String mTextToSearch = "";
    private EditText mSearchText;
    private Context mContext;
    private ImageAdapterList mImageAdapterList;
    private TextView mSearchByName;
    private TextView mSearchAll;
    private TextView mSearchByDescription;
    private GridView mGridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_search, container, false);
        setHasOptionsMenu(true);
        mContext = getActivity();
        mGridView = (GridView) v.findViewById(R.id.gridViewSearch);
        //Create adapter to adapt data to individual list row
        mImageAdapterList = new ImageAdapterList(mContext, null, 0);
        //Set adapter for ListView
        mGridView.setAdapter(mImageAdapterList);
        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setCustomView(R.layout.action_bar_search);
        mSearchText = (EditText)actionBar.getCustomView().findViewById(R.id.editTextSearcher);
        createSearchAllTab(actionBar);
        createSearchByNameTab(actionBar);
        createSearchByDescriptionTab(actionBar);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
        //Use text changed listener by mSearchTest EditText object to find elements in real time of search
        textListener();
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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

    public void createSearchByNameTab(ActionBar actionBar){
        ActionBar.Tab actionBarTabSearchByName = actionBar.newTab();
        actionBarTabSearchByName.setCustomView(R.layout.search_tab);
        mSearchByName = (TextView)actionBarTabSearchByName.getCustomView().findViewById(R.id.tab_text_search);
        setUnselectedTabSearchByName(SEARCH_BY_NAME_TAB);
        ActionBar.TabListener searchByNameTabListener = new ActionBar.TabListener() {

            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                setSelectedTabSearchByName(SEARCH_BY_NAME_TAB);
                sSelectedTypeOfSearch = SEARCH_BY_NAME;
                checkText(mTextToSearch);
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                setUnselectedTabSearchByName(SEARCH_BY_NAME_TAB);
            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                setSelectedTabSearchByName(SEARCH_BY_NAME_TAB);
                sSelectedTypeOfSearch = SEARCH_BY_NAME;
                checkText(mTextToSearch);
            }
        };
        actionBarTabSearchByName.setTabListener(searchByNameTabListener);
        actionBar.addTab(actionBarTabSearchByName);
    }

    public void createSearchAllTab(ActionBar actionBar){
        ActionBar.Tab actionBarTabSearchAll = actionBar.newTab();
        actionBarTabSearchAll.setCustomView(R.layout.search_tab);
        mSearchAll = (TextView)actionBarTabSearchAll.getCustomView().findViewById(R.id.tab_text_search);
        setSelectedTabSearchAll(SEARCH_ALL_TAB);
        ActionBar.TabListener searchAllTabListener = new ActionBar.TabListener() {

            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                setSelectedTabSearchAll(SEARCH_ALL_TAB);
                sSelectedTypeOfSearch = SEARCH_ALL;
                checkText(mTextToSearch);
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                setUnselectedTabSearchAll(SEARCH_ALL_TAB);
            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                setSelectedTabSearchAll(SEARCH_ALL_TAB);
                sSelectedTypeOfSearch = SEARCH_ALL;
                checkText(mTextToSearch);
            }
        };
        actionBarTabSearchAll.setTabListener(searchAllTabListener);
        actionBar.addTab(actionBarTabSearchAll);
    }

    public void createSearchByDescriptionTab(ActionBar actionBar){
        ActionBar.Tab actionBarTabSearchByDescription = actionBar.newTab();
        actionBarTabSearchByDescription.setCustomView(R.layout.search_tab);
        mSearchByDescription = (TextView)actionBarTabSearchByDescription.getCustomView().findViewById(R.id.tab_text_search);
        setUnselectedTabSearchByDescription(SEARCH_BY_DESCRIPTION_TAB);
        ActionBar.TabListener searchByDescriptionTabListener = new ActionBar.TabListener() {

            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                setSelectedTabSearchByDescription(SEARCH_BY_DESCRIPTION_TAB);
                sSelectedTypeOfSearch = SEARCH_BY_DESCRIPTION;
                checkText(mTextToSearch);
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                setUnselectedTabSearchByDescription(SEARCH_BY_DESCRIPTION_TAB);
            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                setSelectedTabSearchByDescription(SEARCH_BY_DESCRIPTION_TAB);
                sSelectedTypeOfSearch = SEARCH_BY_DESCRIPTION;
                checkText(mTextToSearch);
            }
        };
        actionBarTabSearchByDescription.setTabListener(searchByDescriptionTabListener);
        actionBar.addTab(actionBarTabSearchByDescription);
    }

    public void textListener(){
        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                assert mSearchText.getText() != null;
                mTextToSearch = mSearchText.getText().toString();
                mTextToSearch = mTextToSearch.trim();
                mTextToSearch = mTextToSearch.toLowerCase();
                //Search element when text to search have more than two characters
                checkText(mTextToSearch);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void checkText(String collectionElementText){
        if (collectionElementText.length() >= TEXT_TO_SEARCH_MIN_LENGTH) {
            Bundle args = new Bundle();
            //Put text to search to Bundle object
            args.putString(TEXT_TO_SEARCH, collectionElementText);
            //Restart to load data when user query is changed
            getLoaderManager().restartLoader(sSelectedTypeOfSearch, args, SearchFragment.this);
        } else {
            //Clear screen
            mImageAdapterList.swapCursor(null);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        int groupId = 0;
        menu.add(groupId, DELETE_ID, DELETE_ID, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            // Sharing item from list
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Get text to search from args object
        String collectionElementText = args.getString(TEXT_TO_SEARCH);
        String selection = null;
        switch(id){
            case SEARCH_ALL:
                selection = String.format("%s!=%d AND (%s=%d OR %s is null) AND (%s LIKE '%%%s%%' OR %s LIKE '%%%s%%')",
                        DataStorage.Items.TABLE_NAME + "." +DataStorage.Items.DELETED,1,
                        DataStorage.Media.AVATAR,1,DataStorage.Media.AVATAR,
                        DataStorage.Items.DESCRIPTION,collectionElementText,DataStorage.Items.NAME,collectionElementText);
                break;
            case SEARCH_BY_NAME:
                selection = String.format("%s!=%d AND (%s=%d OR %s is null) AND %s LIKE '%%%s%%'",
                        DataStorage.Items.TABLE_NAME + "." +DataStorage.Items.DELETED,1,
                        DataStorage.Media.AVATAR,1,DataStorage.Media.AVATAR,
                        DataStorage.Items.NAME,collectionElementText);
                break;
            case SEARCH_BY_DESCRIPTION:
                selection = String.format("%s!=%d AND (%s=%d OR %s is null) AND %s LIKE '%%%s%%'",
                        DataStorage.Items.TABLE_NAME + "." +DataStorage.Items.DELETED,1,
                        DataStorage.Media.AVATAR,1,DataStorage.Media.AVATAR,
                        DataStorage.Items.DESCRIPTION,collectionElementText);
                break;
        }
        //CursorLoader used to get data from user query
        return new CursorLoader(mContext, DataStorage.Items.CONTENT_URI,
                new String[]{DataStorage.Items.NAME, DataStorage.Media.FILE_NAME, DataStorage.Items.TABLE_NAME + "." + DataStorage.Items._ID},
                selection, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Change cursor with data from database
        mImageAdapterList.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mImageAdapterList.swapCursor(null);
    }

    private void setSelectedTabSearchByName(String text) {
        mSearchByName.setTextColor(getResources().getColor(R.color.selected_tab_text_color));
        mSearchByName.setText(text);
    }

    public void setUnselectedTabSearchByName(String text) {
        mSearchByName.setTextColor(getResources().getColor(R.color.black));
        mSearchByName.setText(text);
    }

    private void setSelectedTabSearchAll(String text) {
        mSearchAll.setTextColor(getResources().getColor(R.color.selected_tab_text_color));
        mSearchAll.setText(text);
    }

    public void setUnselectedTabSearchAll(String text) {
        mSearchAll.setTextColor(getResources().getColor(R.color.black));
        mSearchAll.setText(text);
    }

    private void setSelectedTabSearchByDescription(String text) {
        mSearchByDescription.setTextColor(getResources().getColor(R.color.selected_tab_text_color));
        mSearchByDescription.setText(text);
    }

    public void setUnselectedTabSearchByDescription(String text) {
        mSearchByDescription.setTextColor(getResources().getColor(R.color.black));
        mSearchByDescription.setText(text);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        resetActionBarNavigationMode();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSearchText.setText(mTextToSearch);
        checkText(mTextToSearch);
    }

    private void resetActionBarNavigationMode() {
        //getting the action bar from the MainActivity
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.removeAllTabs();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayShowTitleEnabled(true);
    }

    private void deleteElement(long id){
        ContentValues values = new ContentValues();
        values.put(DataStorage.Items.DELETED,true);
        AsyncQueryHandler asyncHandler =
                new AsyncQueryHandler(getActivity().getContentResolver()) { };
        asyncHandler.startUpdate(0,null,DataStorage.Items.CONTENT_URI,values,DataStorage.Items._ID + " = ?",
                new String[]{String.valueOf(id)});
        checkText(mTextToSearch);
    }
}