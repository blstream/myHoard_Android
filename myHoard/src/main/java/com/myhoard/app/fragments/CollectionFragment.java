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
import android.app.Dialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.myhoard.app.R;
import com.myhoard.app.provider.DataStorage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Rafał Soudani on 20.02.2014
 */
public class CollectionFragment extends Fragment implements View.OnClickListener,
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final int LOAD_DATA_FOR_EDIT = 20;
	private static final int LOAD_NAMES = 30;
    private Context context;
	private Long mEditId;
	private String mName, mDescription;
	private ArrayList<String> mNamesList = new ArrayList<>();
	private List<String> mTagsAvailable = new ArrayList<>();
	private String mTags = "";
	private EditText etCollectionName, etCollectionDescription, etCollectionTags;

	public CollectionFragment() {
		super();
	}

	public CollectionFragment(Bundle args) {
		super();
		mEditId = args.getLong("id");

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
            //TODO: Tags
		case R.id.etCollectionTags: {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(context.getString(R.string.select_tag_to_add));

			final ListView modeList = new ListView(context);
			builder.setView(modeList);
			final Dialog dialog = builder.create();
			ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(context,
					android.R.layout.simple_list_item_1, android.R.id.text1, mTagsAvailable);
			modeList.setAdapter(modeAdapter);
			modeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapterView, View v, int i, long l) {
					//noinspection ConstantConditions
					String item = ((TextView) v).getText().toString();

					mTagsAvailable.remove(item);
					mTags += item + ",";
					dialog.dismiss();
				}
			});

			dialog.show();
		}
		break;

        //Wait for UX
		/*case R.id.bCollectionTagDelete:
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(context.getString(R.string.select_tag_to_delete));

			final ListView modeList = new ListView(context);
			builder.setView(modeList);
			final Dialog dialog = builder.create();
			final List<String> tags = Arrays.asList(mTags.split("\\s*,\\s*"));
			ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(context,
					android.R.layout.simple_list_item_1, android.R.id.text1, tags);
			modeList.setAdapter(modeAdapter);
			modeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapterView, View v, int i, long l) {
					//noinspection ConstantConditions
					String item = ((TextView) v).getText().toString();
					mTagsAvailable.add(item);
					mTags = "";
					for (String s : tags) {
						if (!s.equals(item)) mTags += s + " , ";
					}
					if (mTags.isEmpty()) bDeleteTag.setVisibility(View.GONE);
					bAddTag.setVisibility(View.VISIBLE);
					tagsLayout.removeAllViews();
					getTags();
					dialog.dismiss();
				}
			});

			dialog.show();


		}*/
	}}



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		context = getActivity();
		final View v = inflater.inflate(R.layout.fragment_new_collection, container, false);
		getLoaderManager().initLoader(LOAD_NAMES, null, this);
        setHasOptionsMenu(true);

        if (v != null) {
			etCollectionName = (EditText) v.findViewById(R.id.etCollectionName);
			etCollectionDescription = (EditText) v.findViewById(R.id.etCollectionDescription);
            etCollectionTags = (EditText) v.findViewById(R.id.etCollectionTags);
            etCollectionTags.setOnClickListener(this);


			fillTags();


			if (!this.getTag().equals("EditCollection")) {
                getActivity().setTitle(context.getString(R.string.new_collection));
			}

			if (savedInstanceState != null) {
				mEditId = savedInstanceState.getLong("editId");
				mTags = savedInstanceState.getString("tags");
				//TODO: Tags
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_accept:
                if (etCollectionName.getText() != null) {
                    if (TextUtils.isEmpty(etCollectionName.getText())) {
                        Toast.makeText(getActivity(), getString(R.string.required_name_collection),
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
                        ContentValues values = new ContentValues();
                        values.put(DataStorage.Collections.NAME, mName);
                        values.put(DataStorage.Collections.DESCRIPTION, mDescription);
                        values.put(DataStorage.Collections.TAGS, mTags);
                        values.put(DataStorage.Collections.MODIFIED_DATE, Calendar.getInstance()
                                .getTime().getTime());
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

    private void fillTags() {
		mTagsAvailable.add("Zamki");
		mTagsAvailable.add("Kapsle");
		mTagsAvailable.add("Ludzie");
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mEditId != null) outState.putLong("editId", mEditId);
		outState.putString("tags", mTags);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		switch (id) {
		case LOAD_DATA_FOR_EDIT:
			Uri uri2 = DataStorage.Collections.CONTENT_URI;
			String[] projection2 = DataStorage.Collections.TABLE_COLUMNS;
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
			if (cursor.getString(cursor.getColumnIndex(DataStorage.Collections.TAGS)) != null) {
				mTags = cursor.getString(cursor.getColumnIndex(DataStorage.Collections.TAGS));
                //TODO collectionTags display here
			}
			etCollectionDescription.setText(mDescription);
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
}
