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

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.myhoard.app.R;
import com.myhoard.app.dialogs.TypeDialog;
import com.myhoard.app.provider.DataStorage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Rafał Soudani on 20.02.2014
 */
public class CollectionFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnFocusChangeListener, View.OnClickListener {

    private static final int LOAD_DATA_FOR_EDIT = 20;
    private static final int LOAD_NAMES = 30;
    private static final int MIN_NUMBER_OF_CHARAKCTERS = 2;
    private static final int TYPE_REQUEST_CODE = 100;

    private Context context;
    private Long mEditId;
    private String mName, mDescription;
    private final ArrayList<String> mNamesList = new ArrayList<>();
    private String mTags = "";
    private EditText etCollectionName, etCollectionDescription, etCollectionTags, etCollectionType;
    private final HashMap<Integer, String> typesMap = new HashMap<>();
    Toast toast;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        context = getActivity();
        final View view = inflater.inflate(R.layout.fragment_new_collection, container, false);
        getLoaderManager().initLoader(LOAD_NAMES, null, this);
        setHasOptionsMenu(true);

        setTypesMap();
        setEditId();

        if (view != null) {

            findViewsByIds(view);
            setListeners();

            if (this.getTag().equals("EditCollection")) {
                if (savedInstanceState != null) {
                    mEditId = savedInstanceState.getLong("editId");
                    mTags = savedInstanceState.getString("tags");
                    etCollectionTags.setText(mTags);
                } else {
                    getLoaderManager().restartLoader(LOAD_DATA_FOR_EDIT, null, this);
                }
            } else {
                getActivity().setTitle(context.getString(R.string.new_collection));
            }
        }

        return view;
    }

    private void setTypesMap() {
        typesMap.put(DataStorage.TypeOfCollection.OFFLINE.getType(), getString(R.string.offline));
        typesMap.put(DataStorage.TypeOfCollection.PUBLIC.getType(), getString(R.string.publics));
        typesMap.put(DataStorage.TypeOfCollection.PRIVATE.getType(), getString(R.string.privates));
    }

    private void setEditId() {
        Bundle args = getArguments();
        if (args != null) {
            mEditId = args.getLong("id");
        }
    }

    private void findViewsByIds(View view) {
        etCollectionName = (EditText) view.findViewById(R.id.etCollectionName);
        etCollectionDescription = (EditText) view.findViewById(R.id.etCollectionDescription);
        etCollectionTags = (EditText) view.findViewById(R.id.etCollectionTags);
        etCollectionType = (EditText) view.findViewById(R.id.etCollectionType);
    }

    private void setListeners() {
        etCollectionName.setOnFocusChangeListener(this);
        etCollectionDescription.setOnFocusChangeListener(this);
        etCollectionTags.setOnFocusChangeListener(this);
        etCollectionType.setOnClickListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.new_collection, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_accept:
                if (etCollectionName.getText() != null) {

                    if (validateCollectionName()) {
                        if (etCollectionName.getText() != null) {
                            mName = etCollectionName.getText().toString();
                        }
                        if (etCollectionDescription.getText() != null) {
                            mDescription = etCollectionDescription.getText().toString();
                        }
                        if (etCollectionTags.getText() != null) {
                            if (validateTags()) {
                                mTags = etCollectionTags.getText().toString();
                            }else{
                                return false;
                            }
                        }
                        saveDataInDataBase();
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private boolean validateTags() {
        String sentence = String.valueOf(etCollectionTags.getText());
        String[] tags = sentence.split(" ");
        List<String> result = new ArrayList<>();

        for (String tag : tags)
        {
            if(!result.contains(tag)){
                result.add(tag);
            }else{
                String toastText = String.format(getString(R.string.tag_exist), tag);
                showAToast(toastText, Toast.LENGTH_SHORT);
                return false;
            }
        }
        return true;
    }

    private boolean validateCollectionName() {
        String strCollectionName = "";
        if (etCollectionName.getText() != null) {
            strCollectionName = etCollectionName.getText().toString();
        }
        if (TextUtils.isEmpty(etCollectionName.getText())) {
            showAToast(getString(R.string.required_name_collection), Toast.LENGTH_SHORT);
        } else if (isWhiteSpaces(strCollectionName)) {
            showAToast(getString(R.string.required_name_collection), Toast.LENGTH_SHORT);
        } else if (strCollectionName.length() < MIN_NUMBER_OF_CHARAKCTERS) {
            showAToast(getString(R.string.name_too_short), Toast.LENGTH_SHORT);
        } else if (mNamesList.contains(strCollectionName)) {
            showAToast(getString(R.string.name_already_exist), Toast.LENGTH_SHORT);
        } else if (strCollectionName.charAt(strCollectionName.length() - 1) == ' ') {
            showAToast(getString(R.string.name_with_space_at_end), Toast.LENGTH_SHORT);
        } else if (strCollectionName.charAt(0) == ' ') {
            showAToast(getString(R.string.name_with_space_at_start), Toast.LENGTH_SHORT);
        } else {
            return true;
        }
        return false;

    }

    public void showAToast(String toastText, int toastDuration) {
        try {
            toast.getView().isShown();     // true if visible
            toast.setText(toastText);
        } catch (Exception e) {         // invisible if exception
            toast = Toast.makeText(getActivity(), toastText, toastDuration);
        }
        toast.show();  //finally display it
    }

    private void saveDataInDataBase() {
        ContentValues values = new ContentValues();
        values.put(DataStorage.Collections.NAME, mName);
        values.put(DataStorage.Collections.DESCRIPTION, mDescription);
        values.put(DataStorage.Collections.TAGS, mTags);
        values.put(DataStorage.Collections.MODIFIED_DATE, Calendar.getInstance()
                .getTime().getTime());
        values.put(DataStorage.Collections.TYPE, getTypeOfCollection(String.valueOf(etCollectionType.getText())));
        if (this.getTag().equals("EditCollection")) {
            values.put(DataStorage.Collections.SYNCHRONIZED,false);
            Toast.makeText(getActivity(), context.getString(R.string
                    .collection_edited), Toast.LENGTH_LONG).show();

            AsyncQueryHandler handler =
                    new AsyncQueryHandler(getActivity().getContentResolver()) {
                    };
            handler.startUpdate(-1, null, DataStorage.Collections.CONTENT_URI, values,
                    DataStorage.Collections._ID + " = " + mEditId, null);
        } else {
            showAToast(getString(R.string.collection_added), Toast.LENGTH_SHORT);
            values.put(DataStorage.Collections.CREATED_DATE, Calendar.getInstance()
                    .getTime().getTime());

            AsyncQueryHandler handler =
                    new AsyncQueryHandler(getActivity().getContentResolver()) {
                    };
            handler.startInsert(-1, null, DataStorage
                    .Collections.CONTENT_URI, values);

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mEditId != null) outState.putLong("editId", mEditId);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id) {
            case LOAD_DATA_FOR_EDIT:
                Uri uri2 = DataStorage.Collections.CONTENT_URI;
                String[] projection2 = DataStorage.Collections.TABLE_COLUMNS;
                return new CursorLoader(context, uri2, projection2, String.format("%s = %s", DataStorage.Collections._ID, mEditId), null, null);
            case LOAD_NAMES:
                Uri uri3 = DataStorage.Collections.CONTENT_URI;
                String[] projection3 = {DataStorage.Collections._ID, DataStorage.Collections.NAME};
                String selection3 = String.format("NOT %s ", DataStorage.Collections.DELETED);
                return new CursorLoader(context, uri3, projection3, selection3, null, null);
            default:
                throw new IllegalArgumentException("there is no action for id: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        cursor.moveToFirst();
        switch (loader.getId()) {
            case LOAD_DATA_FOR_EDIT:
                mName = cursor.getString(cursor.getColumnIndex(DataStorage.Collections.NAME));
                getActivity().setTitle(mName); //ActionBar title
                etCollectionName.setText(mName);
                mDescription = cursor.getString(cursor.getColumnIndex(DataStorage.Collections.DESCRIPTION));
                etCollectionDescription.setText(mDescription);
                mTags = cursor.getString(cursor.getColumnIndex(DataStorage.Collections.TAGS));
                etCollectionTags.setText(mTags);
                int type = cursor.getInt(cursor.getColumnIndex(DataStorage.Collections.TYPE));
                etCollectionType.setText(getNameOfCollectionType(type));
            case LOAD_NAMES:
                while (!cursor.isAfterLast()) {
                    if (mEditId != null) {
                        if (cursor.getLong(cursor.getColumnIndex(
                                DataStorage.Collections._ID)) == mEditId) {
                            cursor.moveToNext();
                            continue; //can edit if name not changed
                        }
                    }
                    mNamesList.add(cursor.getString(cursor.getColumnIndex(DataStorage.Collections.NAME)));
                    cursor.moveToNext();
                }

        }
    }



    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    boolean isWhiteSpaces(String s) {
        return s != null && s.matches("\\s+");
    }

    private int getTypeOfCollection(String name) {

        for (Integer i : typesMap.keySet()) {
            if (typesMap.get(i).equals(name)) return i;
        }
        return DataStorage.TypeOfCollection.ERROR.getType();
    }

    private String getNameOfCollectionType(int type) {
        if (typesMap.containsKey(type)) return typesMap.get(type);
        return typesMap.get(DataStorage.TypeOfCollection.OFFLINE.getType());
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        EditText et = (EditText) view;
        if (hasFocus) {
            et.setTextColor(getResources().getColor(R.color.white));
        } else
            et.setTextColor(getResources().getColor(R.color.yellow_main));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TYPE_REQUEST_CODE:
                etCollectionType.setText(data.getStringExtra("type"));
        }
    }

    @Override
    public void onClick(View view) {
        Bundle args = new Bundle();
        args.putString("type", String.valueOf(etCollectionType.getText()));
        TypeDialog typeDialog = new TypeDialog();
        typeDialog.setArguments(args);
        typeDialog.setTargetFragment(this, TYPE_REQUEST_CODE);
        typeDialog.show(getFragmentManager(), "");
    }
}
