/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.myhoard.app.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.myhoard.app.Managers.UserManager;
import com.myhoard.app.R;
import com.myhoard.app.activities.MainActivity;
import com.myhoard.app.images.ImageAdapter;
import com.myhoard.app.provider.DataStorage;

/**
 * Created by Rafał Soudani on 20/02/2014
 * Modified by Maciej Plewko
 */
public class CollectionsListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int EDIT_ID = Menu.FIRST + 2;
    private static final String ItemsList = "ItemsList";
    private static final String NEWCOLLECTION = "NewCollection";
    private static final String EDITCOLLECTION = "EditCollection";
    public static final java.lang.String QUERY = "Query";
    private static final int SEARCH = 1;
    private GridView gridView;
    RelativeLayout tvEmpty, spinner;
    private Context context;
    private ImageAdapter adapter;

    private static TextView sortByNameTabText;
    private static TextView sortByDateTabText;
    private static final String LABEL_BY_NAME_ASC = "A-Z";
    private static final String LABEL_BY_NAME_DESC = "Z-A";
    private static final String LABEL_BY_DATE_ASC = "< DATE";
    private static final String LABEL_BY_DATE_DESC = "> DATE";
    private static final String DEFAULT_SORT = DataStorage.Collections.NAME;
    private static String sortByNameAscending = DataStorage.Collections.NAME + " ASC";
    private static String sortByDateAscending = DataStorage.Collections.TABLE_NAME + "." +
            DataStorage.Collections.CREATED_DATE + " ASC";
    private static String sortByNameDescending = DataStorage.Collections.NAME + " DESC";
    private static String sortByDateDescending = DataStorage.Collections.TABLE_NAME + "." +
            DataStorage.Collections.CREATED_DATE + " DESC";
    private static String sortOrder = DEFAULT_SORT;

    boolean gridViewWasFilled=false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_collections_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //set sortTabs currently in onResume()
        //setSortTabs();
        gridView = (GridView) view.findViewById(R.id.gridview);
        gridView.setEmptyView(view.findViewById(R.id.tvEmpty));

        tvEmpty = (RelativeLayout) view.findViewById(R.id.tvEmpty);
        spinner = (RelativeLayout) view.findViewById(R.id.spinner);

        ImageView ivFirstCollectionButton = (ImageView) view.findViewById(R.id.ivFirstCollectionButton);
        ivFirstCollectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new CollectionFragment(), NEWCOLLECTION)
                        .addToBackStack(NEWCOLLECTION)
                        .commit();
            }
        });

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        UserManager userManager = UserManager.getInstance();
        if (userManager.isLoggedIn()) {
            menu.findItem(R.id.action_login).setTitle("Logout");
            menu.findItem(R.id.action_synchronize).setVisible(true);
        } else {
            menu.findItem(R.id.action_login).setTitle("Login");
            menu.findItem(R.id.action_synchronize).setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //launching ItemsListFragment - author Maciej Plewko
                //TODO passing the clicked collection ID
                Fragment newFragment = new ItemsListFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Bundle b = new Bundle();
                b.putLong(ItemsListFragment.Selected_Collection_ID, l);
                newFragment.setArguments(b);
                MainActivity.collectionSelected = l;

                transaction.replace(R.id.container, newFragment, ItemsList);
                transaction.addToBackStack(ItemsList);

                transaction.commit();
            }
        });
        registerForContextMenu(gridView);
        adapter = new ImageAdapter(context);
        gridView.setAdapter(adapter);
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
                if(!gridViewWasFilled) {
                    sortByName();
                    gridViewWasFilled=false;
                }
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
        fillGridView(null);
        setSortTabs();
    }

    @Override
    public void onStop() {
        super.onStop();
        resetActionBarNavigationMode();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, EDIT_ID, 0, R.string.menu_edit);
        menu.add(0, DELETE_ID, 1, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case DELETE_ID:
                new AlertDialog.Builder(getActivity())
                        .setTitle(context.getString(R.string.delete_colection_dialog_title))
                        .setMessage(context.getString(R.string.delete_colection_dialog_message))
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (info != null) {
                                    getActivity().getContentResolver().delete(
                                            DataStorage.Collections.CONTENT_URI,
                                            String.format("%s = %s", DataStorage.Collections._ID, info.id), null);
                                    fillGridView(null);
                                }
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
                return true;
            case EDIT_ID:
                Bundle args = new Bundle();
                if (info != null) {
                    args.putLong("id", info.id);
                }
                CollectionFragment collectionFragment = new CollectionFragment();
                collectionFragment.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, collectionFragment, EDITCOLLECTION)
                        .addToBackStack(EDITCOLLECTION)
                        .commit();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.main, menu);
        getActivity().setTitle(context.getString(R.string.app_name));
    }

    public void fillGridView(Bundle args) {
        tvEmpty.setVisibility(View.GONE);
        spinner.setVisibility(View.VISIBLE);
        if (args != null) {
            getLoaderManager().restartLoader(SEARCH, args, this);
        }else{
            gridViewWasFilled = true;
            getLoaderManager().restartLoader(0, args, this);
        }
    }

    private void sortByName() {
        if (sortOrder == sortByNameAscending) {
            sortOrder = sortByNameDescending;
            setSelectedTabByNameText(LABEL_BY_NAME_DESC);
        } else {
            sortOrder = sortByNameAscending;
            setSelectedTabByNameText(LABEL_BY_NAME_ASC);
        }
        getLoaderManager().restartLoader(0, null, this);
    }

    private void sortByDate() {
        if (sortOrder == sortByDateAscending) {
            sortOrder = sortByDateDescending;
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String[] projection = DataStorage.Collections.TABLE_COLUMNS;
        String selection = null;
        if (args != null) {
            selection = String.format("%s LIKE '%%%s%%'", DataStorage.Collections.NAME, args.getString(QUERY));
        }

        CursorLoader cL = new CursorLoader(context, DataStorage.Collections.CONTENT_URI,
                projection, selection, null, sortOrder);
        return cL;
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        adapter.swapCursor(cursor);
        spinner.setVisibility(View.GONE);
        if (loader.getId() != SEARCH) {
            gridView.setEmptyView(tvEmpty);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }

    }

    @Override
    public void onLoaderReset(Loader loader) {
        adapter.swapCursor(null);
    }
}