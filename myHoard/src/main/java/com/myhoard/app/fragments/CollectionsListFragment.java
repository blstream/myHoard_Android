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
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.myhoard.app.R;
import com.myhoard.app.images.ImageAdapter;
import com.myhoard.app.provider.DataStorage;

/**
 * Created by Rafał Soudani on 20/02/2014
 */
public class CollectionsListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private  GridView gridView;
    private  Context context;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int EDIT_ID = Menu.FIRST + 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();
        return inflater.inflate(R.layout.fragment_collections_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gridView = (GridView) view.findViewById(R.id.gridview);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO: show items in collection
                /*
                //Testing for collection element - author Sebastian Peryt
                // Create new fragment and transaction
                Fragment newFragment = new ElementFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                // DO NOT USE - Testing
                // Add arguments to opened fragment element
                Bundle b = new Bundle();
                // put name
                b.putString(ElementFragment.NAME,"NAME");
                // put description
                b.putString(ElementFragment.DESCRIPTION,"DESCRIPTION");
                newFragment.setArguments(b);
                // FROM THIS POINT -> USE

                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack
                transaction.replace(R.id.container, newFragment);
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
                */
            }
        });
        registerForContextMenu(gridView);
        gridView.setEmptyView(view.findViewById(R.id.tvEmpty));
        fillGridView();
    }

    public void fillGridView() {
        getLoaderManager().restartLoader(0, null, this);
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
                                            DataStorage.Collections._ID + " = " + info.id, null);
                                    fillGridView();
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
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new CollectionFragment(args), "EditCollection")
                        .addToBackStack("EditCollection")
                        .commit();
                return true;
        }
        return super.onContextItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String[] projection = DataStorage.Collections.TABLE_COLUMNS;
        return new CursorLoader(context, DataStorage.Collections.CONTENT_URI,
                projection, null, null, DataStorage.Collections.NAME);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        gridView.setAdapter(new ImageAdapter(context, cursor));
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}