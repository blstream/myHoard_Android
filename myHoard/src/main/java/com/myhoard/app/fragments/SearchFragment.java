package com.myhoard.app.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import com.myhoard.app.R;
import com.myhoard.app.images.ImageAdapterList;
import com.myhoard.app.provider.DataStorage;
import java.util.ArrayList;

/**
 * Created by Piotr Brzozowski on 01.03.14.
 * SearchFragment class used to search concrete sentence in table of elements
 */
public class SearchFragment extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private ArrayList<String> mCollectionElementName = new ArrayList<>();
    private ArrayList<String> mCollectionElementAvatar = new ArrayList<>();
    private EditText mSearchText;
    private ListView mSearchList;
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_search,container,false);
        mContext = getActivity();
        assert v != null;
        mSearchList = (ListView) v.findViewById(R.id.listViewSearch);
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
                if(collectionElementText.length()>=2){
                    Bundle args = new Bundle();
                    //Put text to search to Bundle object
                    args.putString("fragmentElement",collectionElementText);
                    //Restart to load data when user query is changed
                    getLoaderManager().restartLoader(0,args,SearchFragment.this);
                }
                else{
                    mCollectionElementName.clear();
                    mCollectionElementAvatar.clear();
                    mSearchList.setAdapter(new ImageAdapterList(mContext,mCollectionElementName,mCollectionElementAvatar));
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
        switch(v.getId()){
            case R.id.imageButtonSearch:
                assert mSearchText.getText() != null;
                String collectionElementText = mSearchText.getText().toString();
                collectionElementText = collectionElementText.trim();
                collectionElementText = collectionElementText.toLowerCase();
                //Search element when text to search have more than two characters
                if(collectionElementText.length()>=2){
                    Bundle args = new Bundle();
                    //Put text to search to Bundle object
                    args.putString("fragmentElement",collectionElementText);
                    //Restart to load data when user query is changed
                    getLoaderManager().restartLoader(0,args,this);
                }
                else{
                    Toast.makeText(getActivity(),"Please type 2 characters to start search!",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Get text to search from args object
        String collectionElementText = args.getString("fragmentElement");
        //CursorLoader used to get data from user query
        return new CursorLoader(mContext,DataStorage.Items.CONTENT_URI,
                new String [] {DataStorage.Items.NAME,DataStorage.Media.AVATAR},
                DataStorage.Items.DESCRIPTION+" LIKE '%"+collectionElementText+"%' OR "+DataStorage.Items.NAME+" = '"+collectionElementText+"'",null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
	    // FIXME skoro mamy zwrócony cursor to adapter powinien być dziedziczony po CursorAdapter a nie po BaseAdapter. Tytaj powinno
	    // być w sytuacji idealnej tylko adapter.swapCursor(data)
        //Clear data when are not current
        mCollectionElementName.clear();
        mCollectionElementAvatar.clear();
        data.moveToFirst();
        if(!data.isAfterLast()){
            do{
                //Get data from cursor
                mCollectionElementName.add(data.getString(0));
                mCollectionElementAvatar.add(data.getString(1));
            }while(data.moveToNext());
        }
        data.close();
        //Set adapter for image and text in ListView
        mSearchList.setAdapter(new ImageAdapterList(mContext, mCollectionElementName, mCollectionElementAvatar));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
