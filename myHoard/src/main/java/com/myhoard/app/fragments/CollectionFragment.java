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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.Gravity;
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

/**
 * Created by Rafał Soudani on 20.02.2014
 */
public class CollectionFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnFocusChangeListener{

	private static final int LOAD_DATA_FOR_EDIT = 20;
	private static final int LOAD_NAMES = 30;
    private static final int MIN_NUMBER_OF_CHARAKCTERS = 2;
    private Context context;
	private Long mEditId;
	private String mName, mDescription;
	private ArrayList<String> mNamesList = new ArrayList<>();
	private String mTags = "";
	private EditText etCollectionName, etCollectionDescription, etCollectionTags, etCollectionType;

	public CollectionFragment() {
		super();
	}

	public CollectionFragment(Bundle args) {
		super();
		mEditId = args.getLong("id");

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		context = getActivity();
		final View v = inflater.inflate(R.layout.fragment_new_collection, container, false);
		getLoaderManager().initLoader(LOAD_NAMES, null, this);
        setHasOptionsMenu(true);

        if (v != null) {
			etCollectionName = (EditText) v.findViewById(R.id.etCollectionName);
            etCollectionName.setOnFocusChangeListener(this);
			etCollectionDescription = (EditText) v.findViewById(R.id.etCollectionDescription);
            etCollectionDescription.setOnFocusChangeListener(this);
            etCollectionTags = (EditText) v.findViewById(R.id.etCollectionTags);
            etCollectionTags.setOnFocusChangeListener(this);
            etCollectionType = (EditText) v.findViewById(R.id.etCollectionType);
            etCollectionType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TypeDialog typeDialog = new TypeDialog();
                    typeDialog.show(getFragmentManager(),"");
                }
            });

			if (!this.getTag().equals("EditCollection")) {
                getActivity().setTitle(context.getString(R.string.new_collection));
			}

			if (savedInstanceState != null) {
				mEditId = savedInstanceState.getLong("editId");
				mTags = savedInstanceState.getString("tags");
				etCollectionTags.setText(mTags);
			} else if (this.getTag().equals("EditCollection")) {
				getLoaderManager().restartLoader(LOAD_DATA_FOR_EDIT, null, this);
			}

		}

		return v;
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.new_collection, menu);
    }

    /* AWA:FIXME: Ciało metody jest za dlugie.
    Mozna je podzielic na "krótsze" metody
    Patrz:Ksiazka:Czysty kod:Rozdział 3:Funkcje
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_accept:
                if (etCollectionName.getText() != null) {
                    if (TextUtils.isEmpty(etCollectionName.getText())) {
                        Toast.makeText(getActivity(), getString(R.string.required_name_collection),
                                Toast.LENGTH_SHORT).show();
                    } else if (isWhiteSpaces(etCollectionName.getText().toString())) {
                        Toast.makeText(getActivity(), getString(R.string.required_name_collection),
                                Toast.LENGTH_SHORT).show();
                    } else if (etCollectionName.getText().toString().length()<MIN_NUMBER_OF_CHARAKCTERS) {
                        Toast.makeText(getActivity(), getString(R.string.name_too_short),
                                Toast.LENGTH_SHORT).show();
                    } else if (mNamesList.contains(etCollectionName.getText().toString())) {
                        Toast toast = Toast.makeText(getActivity(), getString(R.string.name_already_exist),
                                Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                        toast.show();
                    } else {
                        if (etCollectionName.getText() != null) {
                            mName = etCollectionName.getText().toString();
                        }
                        if (etCollectionDescription.getText() != null) {
                            mDescription = etCollectionDescription.getText().toString();
                        }
                        if (etCollectionTags.getText() != null) {
                            mTags = etCollectionTags.getText().toString();
                        }
                        ContentValues values = new ContentValues();
                        values.put(DataStorage.Collections.NAME, mName);
                        values.put(DataStorage.Collections.DESCRIPTION, mDescription);
                        values.put(DataStorage.Collections.TAGS, mTags);
                        values.put(DataStorage.Collections.MODIFIED_DATE, Calendar.getInstance()
                                .getTime().getTime());
                        values.put(DataStorage.Collections.TYPE, getTypeOfCollection());
                        if (this.getTag().equals("EditCollection")) {
                            Toast.makeText(getActivity(), context.getString(R.string
                                    .collection_edited), Toast.LENGTH_LONG).show();

                            AsyncQueryHandler handler =
                                    new AsyncQueryHandler(getActivity().getContentResolver()) {};
                            handler.startUpdate(-1, null, DataStorage.Collections.CONTENT_URI, values,
                                    DataStorage.Collections._ID + " = " + mEditId, null);
                        } else {
                            values.put(DataStorage.Collections.CREATED_DATE, Calendar.getInstance()
                                    .getTime().getTime());

                            AsyncQueryHandler handler =
                                    new AsyncQueryHandler(getActivity().getContentResolver()) {};
                            handler.startInsert(-1, null, DataStorage
                                    .Collections.CONTENT_URI, values);

                        }
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mEditId != null) outState.putLong("editId", mEditId);
		//TODO: delete outState.putString("tags", mTags);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		switch (id) {
		case LOAD_DATA_FOR_EDIT:
			Uri uri2 = DataStorage.Collections.CONTENT_URI;
			String[] projection2 = DataStorage.Collections.TABLE_COLUMNS;
            /* AWA:FIXME: Używaj String.format*/
			return new CursorLoader(context, uri2, projection2, DataStorage.Collections._ID + " = " + mEditId, null, null);
		case LOAD_NAMES:
			Uri uri3 = DataStorage.Collections.CONTENT_URI;
			String[] projection3 = {DataStorage.Collections._ID, DataStorage.Collections.NAME};
			return new CursorLoader(context, uri3, projection3, null, null, null);

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
			break;
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

    boolean isWhiteSpaces( String s ) {
        return s != null && s.matches("\\s+");
    }

     /* AWA:FIXME: Hardcoded value
                    Umiesc w private final static String ....
                    lub w strings.xml
                    */
    private int getTypeOfCollection() {
        if (etCollectionType.getText().toString().equals("offline")) {
            return DataStorage.TypeOfCollection.OFFLINE.getType();
        }
        else if (etCollectionType.getText().toString().equals("public")) {
            return DataStorage.TypeOfCollection.PUBLIC.getType();
        }
        else if (etCollectionType.getText().toString().equals("private")) {
            return DataStorage.TypeOfCollection.PRIVATE.getType();
        }
        return DataStorage.TypeOfCollection.ERROR.getType();
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        EditText et = (EditText) view;
        if (hasFocus) {
            et.setTextColor(getResources().getColor(R.color.white));
        }
        else
            et.setTextColor(getResources().getColor(R.color.yellow_main));
    }
}
