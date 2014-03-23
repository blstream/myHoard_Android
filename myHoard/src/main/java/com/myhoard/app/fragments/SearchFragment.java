
package com.myhoard.app.fragments;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;
import com.myhoard.app.R;
import com.myhoard.app.images.ImageAdapterList;
import com.myhoard.app.provider.DataStorage;

/**
 * Created by Piotr Brzozowski on 01.03.14.
 * SearchFragment class used to search concrete sentence in table of elements
 */
public class SearchFragment extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private EditText mSearchText;
    private Context mContext;
    private ImageAdapterList mImageAdapterList;
    private Long mCollectionId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_search, container, false);
        Bundle b = this.getArguments();
        mCollectionId = b.getLong("SearchFragment");
        mContext = getActivity();
        //Create adapter to adapt data to individual list row
        mImageAdapterList = new ImageAdapterList(mContext, null, 0);
        assert v != null;
        GridView mSearchList = (GridView) v.findViewById(R.id.gridViewSearch);
        //Set adapter for ListView
        mSearchList.setAdapter(mImageAdapterList);
        ImageButton imButtonSearch = (ImageButton) v.findViewById(R.id.imageButtonSearch);
        imButtonSearch.setOnClickListener(this);
        mSearchText = (EditText) v.findViewById(R.id.editTextSearch);
        //Use text changed listener by mSearchTest EditText object to find elements in real time of search
        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                assert mSearchText.getText() != null;
                String collectionElementText = mSearchText.getText().toString();
                collectionElementText = collectionElementText.trim();
                collectionElementText = collectionElementText.toLowerCase();
                //Search element when text to search have more than two characters
                if (collectionElementText.length() >= 2) {
                    Bundle args = new Bundle();
                    //Put text to search to Bundle object
                    args.putString("fragmentElement", collectionElementText);
                    //Restart to load data when user query is changed
                    getLoaderManager().restartLoader(0, args, SearchFragment.this);
                } else {
                    //Clear screen
                    mImageAdapterList.swapCursor(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return v;
    }

    @Override
    public void onClick(View v) {
        //Get interact with one of button in layout by id
        switch (v.getId()) {
            case R.id.imageButtonSearch:
                assert mSearchText.getText() != null;
                String collectionElementText = mSearchText.getText().toString();
                collectionElementText = collectionElementText.trim();
                collectionElementText = collectionElementText.toLowerCase();
                //Search element when text to search have more than two characters
                if (collectionElementText.length() >= 2) {
                    Bundle args = new Bundle();
                    //Put text to search to Bundle object
                    args.putString("fragmentElement", collectionElementText);
                    //Restart to load data when user query is changed
                    getLoaderManager().restartLoader(0, args, this);
                } else {
                    Toast.makeText(getActivity(), "Please type 2 characters to start search!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Get text to search from args object
        String collectionElementText = args.getString("fragmentElement");
        //CursorLoader used to get data from user query
        return new CursorLoader(mContext, DataStorage.Items.CONTENT_URI,
                new String[]{DataStorage.Items.NAME, DataStorage.Media.AVATAR, DataStorage.Items.TABLE_NAME + "." + DataStorage.Items._ID},
                mCollectionId + " = " + DataStorage.Items.ID_COLLECTION + " AND ("+ DataStorage.Items.DESCRIPTION + " LIKE '%" + collectionElementText + "%' OR " + DataStorage.Items.NAME + " = '" + collectionElementText + "')", null, null);
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
}