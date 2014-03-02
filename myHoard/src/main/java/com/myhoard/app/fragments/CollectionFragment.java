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

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.myhoard.app.R;
import com.myhoard.app.images.BitmapWorkerTask;
import com.myhoard.app.provider.DataStorage;

import java.util.ArrayList;

/**
 * Created by Rafał Soudani on 20.02.2014
 */
public class CollectionFragment extends Fragment implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private Context context;
    private static final int LOAD_IMAGE = 10;
    private static final int LOAD_DATA_FOR_EDIT = 20;
    private static final int LOAD_NAMES = 30;

    private Long mEditId;
    private String mImgPath, mName, mDescription;
    private ArrayList<String> mNamesList = new ArrayList<>();

    private EditText etCollectionName, etCollectionDescription;
    private ImageButton ibCollectionAvatar;
    private static int RESULT_LOAD_IMAGE = 1;
    OnFragmentClickListener mListener;

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

        if (v != null) {
            etCollectionName = (EditText) v.findViewById(R.id.etCollectionName);
            etCollectionDescription = (EditText) v.findViewById(R.id.etCollectionDescription);
            Button mBCollectionAdd = (Button) v.findViewById(R.id.bCollectionAdd);
            ibCollectionAvatar = (ImageButton) v.findViewById(R.id.ibCollectionAvatar);
            mBCollectionAdd.setOnClickListener(this);
            ibCollectionAvatar.setOnClickListener(this);

            if (this.getTag().equals("EditCollection")) {
                mBCollectionAdd.setText(context.getString(R.string.collection_edit));
            }
            if (savedInstanceState != null) {
                mEditId = savedInstanceState.getLong("editId");
                mImgPath = savedInstanceState.getString("imgPath");
                BitmapWorkerTask task = new BitmapWorkerTask(ibCollectionAvatar, context);
                task.execute(mImgPath);
            } else if (this.getTag().equals("EditCollection")) {
                getLoaderManager().restartLoader(LOAD_DATA_FOR_EDIT, null, this);
            }

        }

        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bCollectionAdd:
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
                    values.put(DataStorage.Collections.AVATAR_FILE_NAME, mImgPath);
                    if (this.getTag().equals("EditCollection")) {
                        Toast.makeText(getActivity(), context.getString(R.string
                                .collection_edited), Toast.LENGTH_LONG).show();
                        getActivity().getContentResolver()
                                .update(DataStorage.Collections.CONTENT_URI, values,
                                        DataStorage.Collections._ID + " = " + mEditId, null);
                    } else {
                        getActivity().getContentResolver()
                                .insert(DataStorage.Collections.CONTENT_URI, values);

                    }
                    mListener.OnFragmentClick();
                }
                break;

            case R.id.ibCollectionAvatar:
                Intent i = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
                break;

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            String[] projection = {MediaStore.Images.Media.DATA};

            Bundle args = new Bundle();
            if (uri != null) {
                args.putString("Uri", uri.toString());
            } else throw new NullPointerException("uri can't be null");
            args.putStringArray("Projection", projection);

            getLoaderManager().restartLoader(LOAD_IMAGE, args, this);

        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement listeners!");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("imgPath", mImgPath);
        if (mEditId != null) outState.putLong("editId", mEditId);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id) {
            case LOAD_IMAGE:
                Uri uri = Uri.parse(args.getString("Uri"));
                String[] projection = args.getStringArray("Projection");
                return new CursorLoader(context, uri, projection, null, null, null);
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
            case LOAD_IMAGE:
                int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                mImgPath = cursor.getString(columnIndex);

                BitmapWorkerTask task = new BitmapWorkerTask(ibCollectionAvatar, context);
                task.execute(mImgPath);
                break;
            case LOAD_DATA_FOR_EDIT:
                mName = cursor.getString(cursor.getColumnIndex(DataStorage.Collections.NAME));
                etCollectionName.setText(mName);
                mDescription = cursor.getString(cursor.getColumnIndex(DataStorage.Collections.DESCRIPTION));
                etCollectionDescription.setText(mDescription);
                mImgPath = cursor.getString(cursor.getColumnIndex(DataStorage.Collections.AVATAR_FILE_NAME));
                if (mImgPath != null) {
                    if (!mImgPath.isEmpty()) {
                        BitmapWorkerTask task2 = new BitmapWorkerTask(ibCollectionAvatar, context);
                        task2.execute(mImgPath);
                    }
                }
                break;
            case LOAD_NAMES:
                do {
                    if (mEditId != null) {
                        if (cursor.getLong(cursor.getColumnIndex(
                                DataStorage.Collections._ID)) == mEditId){
                            continue; //can edit if name not changed
                        }
                    }
                    mNamesList.add(cursor.getString(cursor.getColumnIndex(DataStorage.Collections.NAME)));
                }
                while (cursor.moveToNext());

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
