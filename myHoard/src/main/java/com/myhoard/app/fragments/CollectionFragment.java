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

/**
 * Created by Rafał Soudani on 20.02.2014
 */
public class CollectionFragment extends Fragment implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor>{

    private Context context;

    private String mImgPath;

    private EditText etCollectionName, etCollectionDescription;
    private ImageButton ibCollectionAvatar;
    private static int RESULT_LOAD_IMAGE = 1;
    OnFragmentClickListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        context = getActivity();
        final View v = inflater.inflate(R.layout.fragment_new_collection, container, false);

        etCollectionName = (EditText) v.findViewById(R.id.etCollectionName);
        etCollectionDescription = (EditText) v.findViewById(R.id.etCollectionDescription);
        Button mBCollectionAdd = (Button) v.findViewById(R.id.bCollectionAdd);
        ibCollectionAvatar = (ImageButton) v.findViewById(R.id.ibCollectionAvatar);
        mBCollectionAdd.setOnClickListener(this);
        ibCollectionAvatar.setOnClickListener(this);

        if (savedInstanceState != null) {
            mImgPath = savedInstanceState.getString("imgPath");
            BitmapWorkerTask task = new BitmapWorkerTask(ibCollectionAvatar, context);
            task.execute(mImgPath);
        }

        //noinspection StatementWithEmptyBody
        if (this.getTag().equals("EditCollection")) {
            //TODO: edit collection
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
                } else {
                    String mName;
                    mName = etCollectionName.getText().toString();
                    String mDescription;
                    mDescription = etCollectionDescription.getText().toString();
                    ContentValues values = new ContentValues();
                    values.put(DataStorage.Collections.NAME, mName);
                    values.put(DataStorage.Collections.DESCRIPTION, mDescription);
                    values.put(DataStorage.Collections.AVATAR_FILE_NAME, mImgPath);
                    getActivity().getContentResolver()
                            .insert(DataStorage.Collections.CONTENT_URI, values);

                    mListener.OnFragmentClick();
                    getFragmentManager().popBackStackImmediate();
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
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == getActivity().RESULT_OK && data != null) {
            Uri uri = data.getData();
            String[] projection = {MediaStore.Images.Media.DATA};

            Bundle args = new Bundle();
            args.putString("Uri", uri.toString());
            args.putStringArray("Projection", projection);

            getLoaderManager().restartLoader(0, args, this);

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
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri uri = Uri.parse(args.getString("Uri"));
        String[] projection = args.getStringArray("Projection");

        return new CursorLoader(context, uri, projection, null, null, null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
        mImgPath = cursor.getString(columnIndex);

        BitmapWorkerTask task = new BitmapWorkerTask(ibCollectionAvatar, context);
        task.execute(mImgPath);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
