package com.myhoard.app.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.myhoard.app.R;
import com.myhoard.app.provider.DataStorage;

/**
 * Created by Piotr Brzozowski on 25.05.2014
 */
public class CategoryDialog extends DialogFragment implements View.OnClickListener{

    private static final int CATEGORY_RESULT_CODE = 200;
    private static final int LOADER_CATEGORIES = 1;
    private static final int NO_FLAGS = 0;
    private ListView mCategoryListView;
    private SimpleCursorAdapter mAdapterCategories;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity(), R.style.TypeDialog);
        dialog.setContentView(R.layout.category_dialog);
        dialog.setCanceledOnTouchOutside(true);
        fillCategoriesData();
        mCategoryListView = (ListView) dialog.findViewById(R.id.listViewCategory);
        getLoaderManager().initLoader(LOADER_CATEGORIES, null,
                new LoaderCategoriesCallbacks());
        mCategoryListView.setAdapter(mAdapterCategories);
        mCategoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("collectionId",position);
                getTargetFragment().onActivityResult(getTargetRequestCode(), CATEGORY_RESULT_CODE, intent);
                dismiss();
            }
        });
        TextView cancelOption = (TextView)dialog.findViewById(R.id.tvCancelChooseCollection);
        cancelOption.setOnClickListener(this);
        dialog.show();
        return dialog;
    }

    private void fillCategoriesData() {
        String[] from = new String[] { DataStorage.Collections.NAME,
                DataStorage.Collections._ID };
        int[] to = new int[] { R.id.tvCategory };

        mAdapterCategories = new SimpleCursorAdapter(getActivity(),
                R.layout.category_dialog_row, null, from, to, NO_FLAGS);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch(v.getId()){
            case R.id.tvCancelChooseCollection:
                intent.putExtra("collectionId",-1);
                getTargetFragment().onActivityResult(getTargetRequestCode(), CATEGORY_RESULT_CODE, intent);
                dismiss();
                break;
        }
    }

    private class LoaderCategoriesCallbacks implements
            LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String[] projection = {DataStorage.Collections.NAME,
                    DataStorage.Collections._ID};
            String selection = String.format("%s!=%d", DataStorage.Collections.DELETED, 1);
            CursorLoader cursorLoader = new CursorLoader(getActivity(),
                    DataStorage.Collections.CONTENT_URI, projection, selection,
                    null, DataStorage.Collections.NAME + " ASC");
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mAdapterCategories.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapterCategories.swapCursor(null);
        }
    }
}
