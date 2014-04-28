package com.myhoard.app.element;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myhoard.app.R;
import com.myhoard.app.model.Item;
import com.myhoard.app.provider.DataStorage;
import com.myhoard.app.views.ElementFragmentPager;

/**
 * Created by Sebastian Peryt on 28.04.14.
 */
public class ElementReadFragment extends Fragment {

    private ElementFragmentPager pagerAdapter;
    private ViewPager pager;
    private Item element;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        element = getArguments().getParcelable("element");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_element_read, container,
                false);

        final TextView elementName = (TextView) v.findViewById(R.id.tvElementName);
        final TextView elementPosition = (TextView) v.findViewById(R.id.tvElementLocalisation);
        elementPosition.setOnClickListener(listener);

        elementName.setText(element.getName());

        getActivity().getSupportLoaderManager().initLoader(1, null, new LoaderImagesCallbacks());

        return v;
    }

    private void initViewPager(Cursor data) {
        pagerAdapter = new ElementFragmentPager(getActivity().getSupportFragmentManager(),data);
        pager = (ViewPager) getActivity().findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            pager.setCurrentItem(pagerAdapter.getCount(),true);
        }
    };

    private class LoaderImagesCallbacks implements
            LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String[] projection = { DataStorage.Media.FILE_NAME,
                    DataStorage.Media.CREATED_DATE, DataStorage.Media._ID,
                    DataStorage.Media.ID_ITEM };
            CursorLoader cursorLoader = new CursorLoader(getActivity(),
                    DataStorage.Media.CONTENT_URI, projection,
                    DataStorage.Media.ID_ITEM + " =? ",
                    new String[] { element.getId() },
                    DataStorage.Media.CREATED_DATE + " ASC");
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if(pagerAdapter == null) {
                initViewPager(data);
            } else {
                pagerAdapter.swapCursor(data);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            pagerAdapter.swapCursor(null);
        }
    }
}
