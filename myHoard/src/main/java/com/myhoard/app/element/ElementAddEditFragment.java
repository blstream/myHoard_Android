package com.myhoard.app.element;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.myhoard.app.R;
import com.myhoard.app.adapters.ImageElementAdapterList;
import com.myhoard.app.dialogs.CategoryDialog;
import com.myhoard.app.dialogs.GpsChooseDialog;
import com.myhoard.app.dialogs.ImageDeleteDialog;
import com.myhoard.app.dialogs.ImageEditDialog;
import com.myhoard.app.dialogs.ImageInsertDialog;
import com.myhoard.app.images.PhotoManager;
import com.myhoard.app.model.Item;
import com.myhoard.app.provider.DataStorage;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/*
 * Created by Sebastian Peryt on 27.02.14.
 * Modified by Piotr Brzozowski on 15.05.14.
 */
public class ElementAddEditFragment extends Fragment implements View.OnClickListener,
        AdapterView.OnItemClickListener {

    public static final String ID = "elementId";
    public static final String NAME = "elementName";
    public static final String DESCRIPTION = "elementDescription";
    public static final String COLLECTION_ID = "elementCollectionId";
    public static final String CREATED_DATE = "elementCreatedDate";
    public static final String MODIFIED_DATE = "elementModifiedDate";
    public static final String TAGS = "elementTags";
    public static final String EDITION = "edition";
    private static final int INSERT_IMAGE_REQUEST_CODE = 100;
    private static final int EDIT_IMAGE_REQUEST_CODE = 101;
    private static final int DELETE_IMAGE_REQUEST_CODE = 102;
    private static final int CATEGORY_RESULT_CODE = 200;
    private static final int GPS_RESULT_CODE = 300;
    private static final String PHOTO_MANAGER_KEY = "photoManagerKey";
    private static final int REQUEST_GET_PHOTO = 1;

    private static final int LOADER_CATEGORIES = 1;
    private static final int LOADER_IMAGES = 2;
    private static final int NO_FLAGS = 0;

    private static final int LOCATION_FROM_ACTIVITY = 1;
    private static final int LOCATION_FROM_FRAGMENT = 0;

    private boolean first = true;

    private TextView mElementPosition,
            tvElementCategory;
    private EditText mElementName, mElementDescription;
    private int mCollectionId;
    private int mElementId;
    private Context mContext;
    private SimpleCursorAdapter mAdapterCategories;
    private GridView mPhotosList;
    private ArrayList<Uri> mImagesUriList;
    private ArrayList<Integer> mImagesUriDeleteList;
    private HashMap<Integer,Uri> mImagesUriInsertList;
    private HashMap<Integer,Uri> mImagesUriInsertListTmp;
    private HashMap<Integer,Integer> mImagesPositionListTmp;
    private HashMap<Integer,Integer> mImagesPositionList;
    private HashMap<Integer,String> mImagesIDServerList;
    private HashMap<Integer,Integer> mImagesTmp;
    private ImageElementAdapterList mImageListAdapter;
    private int mImageId;
    private boolean mLocationUserSet = false;
    private boolean mGpsEnabled = false;
    private String mActualElementName;
    private PhotoManager mPhotoManager;
    private int mActualCollectionId;
    private LatLng mElementLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_element_addedit, container,
                false);
        final RelativeLayout rlEmptyView = (RelativeLayout) v.findViewById(R.id.element_emptyview);
        final LinearLayout lnEmptyViewClickable = (LinearLayout) v.findViewById(R.id.emptyview_inside);
        mContext = getActivity();
        mImagesUriList = new ArrayList<>();
        mElementId = -1;
        mCollectionId = -1;
        mImageId = -1;
        Bundle b = getArguments();
        b.getParcelable("location");
        if (savedInstanceState != null) {
            mPhotoManager = savedInstanceState.getParcelable(PHOTO_MANAGER_KEY);
        } else {
            mPhotoManager = new PhotoManager(this,REQUEST_GET_PHOTO);
        }
        setHasOptionsMenu(true);
        initVariablesFromLayout(v,rlEmptyView);
        setGpsFormInitStyle(b);
        setFormInEditElementMode(b);
        setOnClickListenerForLayoutElement(lnEmptyViewClickable);
        getLoaderManager().initLoader(LOADER_CATEGORIES, null,
                new LoaderCategoriesCallbacks());
        return v;
    }

    private void initVariablesFromLayout(View v,View rlEmptyView){
        mElementPosition = (TextView) v.findViewById(R.id.tvElementLocalisation);
        mElementName = (EditText) v.findViewById(R.id.etElementName);
        mElementDescription = (EditText) v.findViewById(R.id.etElementDescription);
        tvElementCategory = (TextView) v.findViewById(R.id.tvElementCategory);
        mPhotosList = (GridView) v.findViewById(R.id.gvPhotosList);
        mPhotosList.setEmptyView(rlEmptyView);
        mElementPosition.setOnClickListener(this);
    }

    private void setGpsFormInitStyle(Bundle b){
        mGpsEnabled = b.getBoolean("gps");
        if(!mGpsEnabled) {
            mElementPosition.setText(R.string.gps_no_signal);
            mElementPosition.setTextColor(Color.RED);
        } else {
            mElementPosition.setText(R.string.gps_finding_location);
            mElementPosition.setTextColor(Color.YELLOW);
        }
    }

    private void setFormInEditElementMode(Bundle b){
        if(b.getLong("categoryId",-1)!=-1) {
            mCollectionId = (int) b.getLong("categoryId");
        } else if(b.getParcelable("element")!=null) {
            Item element = b.getParcelable("element");
            mElementId = Integer.parseInt(element.getId());
            mCollectionId = Integer.parseInt(element.getCollection());
            mActualCollectionId = mCollectionId;
            if(element.getLocation().lat!=0 && element.getLocation().lng!=0) {
                mElementPosition.setText(element.getLocationTxt());
                mLocationUserSet = true;
                mElementPosition.setTextColor(Color.GREEN);
                mElementLocation = new LatLng(element.getLocation().lat, element.getLocation().lng);
            }
            mElementName.setText(element.getName());
            mActualElementName = mElementName.getText().toString().trim();
            mElementDescription.setText(element.getDescription());
            mImagesUriInsertList = new HashMap<>();
            mImagesPositionListTmp = new HashMap<>();
            mImagesPositionList = new HashMap<>();
            mImagesUriDeleteList = new ArrayList<>();
            mImagesIDServerList = new HashMap<>();
            mImagesUriInsertListTmp = new HashMap<>();
            mImagesTmp = new HashMap<>();
        }
    }

    private void setOnClickListenerForLayoutElement(LinearLayout lnEmptyViewClickable){
        mPhotosList.setAdapter(getPhotosList());
        mPhotosList.setOnItemClickListener(this);
        tvElementCategory.setOnClickListener(this);
        lnEmptyViewClickable.setOnClickListener(this);
    }

    private ListAdapter getPhotosList() {
        if (mElementId != -1) {
            fillPhotosData();
        } else{
            mImageListAdapter = new ImageElementAdapterList(getActivity(),NO_FLAGS, mImagesUriList);
        }
        return mImageListAdapter;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvElementLocalisation:
                if(!mGpsEnabled){
                    showGpsPickerDialog();
                } else {
                    Intent intent = new Intent(getActivity(), ElementMapActivity.class);
                    intent.putExtra("localisation",mElementLocation);
                    startActivityForResult(intent, 9);
                }
                break;
            case R.id.tvElementCategory:
                showCategoryPickerDialog();
                break;
            case R.id.emptyview_inside:
                showImagePickerDialog();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(i == 0) {
            showImagePickerDialog();
        } else {
            mImageId = (int) l;
            showImageEditPickerDialog();
        }
    }

    /**
     * Method shows category picker with categories from whole database.
     */
    private void categoryPicker(int collectionId_position) {
        if(collectionId_position!=-1){
            mAdapterCategories.getCursor().moveToPosition(collectionId_position);
            int columnIndex = mAdapterCategories.getCursor()
                    .getColumnIndex(DataStorage.Collections.NAME);
            tvElementCategory.setText(mAdapterCategories.getCursor()
                    .getString(columnIndex));
            mCollectionId = (int) mAdapterCategories.getItemId(collectionId_position);
        }
    }

    private void localisationPicker(int location_choose) {
        if(location_choose==1){
            startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 10);
        }else if(location_choose==2){
            Intent intent = new Intent(getActivity(), ElementMapActivity.class);
            if(mElementLocation!=null){
                intent.putExtra("localisation",mElementLocation);
            }
            startActivityForResult(intent, 9);
        }
    }

    /**
     * Fill adapter data with proper cursor values
     */
    private void fillCategoriesData() {
        // Fields from databse from which data will be taken. Has to include _id
        // column.
        String[] from = new String[] { DataStorage.Collections.NAME,
                DataStorage.Collections._ID };
        // UI fields in given layout into which elemnts have to be put.
        int[] to = new int[] { android.R.id.text1 };

        mAdapterCategories = new SimpleCursorAdapter(mContext,
                android.R.layout.simple_list_item_1, null, from, to, NO_FLAGS);
    }

    /**
     * Fill adapter data with proper cursor values
     */
    private void fillPhotosData() {
        getLoaderManager().initLoader(LOADER_IMAGES, null,
                new LoaderImagesCallbacks());
    }

    private void showImagePickerDialog(){
        ImageInsertDialog insertDialog = new ImageInsertDialog();
        insertDialog.setTargetFragment(this, INSERT_IMAGE_REQUEST_CODE);
        insertDialog.show(getFragmentManager(), "");
    }

    private void showGpsPickerDialog(){
        GpsChooseDialog gpsDialog = new GpsChooseDialog();
        gpsDialog.setTargetFragment(this, GPS_RESULT_CODE);
        gpsDialog.show(getFragmentManager(), "");
    }


    private void showCategoryPickerDialog(){
        CategoryDialog categoryDialog = new CategoryDialog();
        categoryDialog.setTargetFragment(this, CATEGORY_RESULT_CODE);
        categoryDialog.show(getFragmentManager(), "");
    }

    private void showImageEditPickerDialog(){
        if(mElementId!=-1){
            ImageDeleteDialog removeDialog = new ImageDeleteDialog();
            removeDialog.setTargetFragment(this, DELETE_IMAGE_REQUEST_CODE);
            removeDialog.show(getFragmentManager(), "");
        }else{
            ImageEditDialog editDialog = new ImageEditDialog();
            editDialog.setTargetFragment(this, EDIT_IMAGE_REQUEST_CODE);
            editDialog.show(getFragmentManager(), "");
        }
    }

    /**
     * Method shows source picker, where user chose source of element image.
     */
    private void imagePicker(int which) {
        if(which==1){
            try {
                mPhotoManager.takePicture(PhotoManager.MODE_CAMERA);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(which==2){
            try {
                mPhotoManager.takePicture(PhotoManager.MODE_GALLERY);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            mImageId = -1;
        }
    }

    private void imagePickerDel(int which) {
        if(mElementId!=-1){
            if (which == 1) {
                deleteImage(mImageId);
            }else{
                mImageId = -1;
            }
        }else {
            if (which == 0) {
                try {
                    mPhotoManager.takePicture(PhotoManager.MODE_CAMERA);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (which == 1) {
                try {
                    mPhotoManager.takePicture(PhotoManager.MODE_GALLERY);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (which == 2) {
                deleteImage(mImageId);
            } else {
                mImageId = -1;
            }
        }
    }

    private void deleteImage(int id) {
        deleteElementInEditMode(id);
        mImagesUriList.remove(id);
        if(mImagesUriList.size()==1) {
            mImagesUriList.clear();
        }
        if(mImagesUriList.size() == 2) {
            mPhotosList.setNumColumns(2);
        } else {
            mPhotosList.setNumColumns(3);
        }
        mImageId = -1;
        mImageListAdapter.notifyDataSetChanged();
    }

    private void deleteElementInEditMode(int id){
        if (mElementId != -1) {
            if(mImagesUriInsertList.containsKey(id)){
                mImagesUriInsertList.remove(id);
            }else{
                mImagesUriDeleteList.add(mImagesPositionListTmp.get(id));
                mImagesPositionListTmp.remove(id);
                updateImagesPositionAfterRemove();
                updateRecentlyInsertImagesAfterRemove();
            }
        }
    }

    private void updateImagesPositionAfterRemove(){
        Integer key;
        int i=1;
        Map<Integer,Integer> map = new TreeMap<>(mImagesPositionListTmp);
        Iterator<Integer> keySetIterator = map.keySet().iterator();
        while(keySetIterator.hasNext()){
            key = keySetIterator.next();
            mImagesTmp.put(i,map.get(key));
            i++;
        }
        mImagesPositionListTmp.clear();
        keySetIterator = mImagesTmp.keySet().iterator();
        while(keySetIterator.hasNext()){
            key = keySetIterator.next();
            mImagesPositionListTmp.put(key,mImagesTmp.get(key));
        }
        mImagesTmp.clear();
    }

    private void updateRecentlyInsertImagesAfterRemove(){
        Integer key;
        Iterator<Integer> keySetIterator;
        if(!mImagesUriInsertList.isEmpty()){
            keySetIterator = mImagesUriInsertList.keySet().iterator();
            while(keySetIterator.hasNext()){
                key = keySetIterator.next();
                mImagesUriInsertListTmp.put(key-1,mImagesUriInsertList.get(key));
            }
            mImagesUriInsertList.clear();
            keySetIterator = mImagesUriInsertListTmp.keySet().iterator();
            while(keySetIterator.hasNext()){
                key = keySetIterator.next();
                mImagesUriInsertList.put(key,mImagesUriInsertListTmp.get(key));
            }
            mImagesUriInsertListTmp.clear();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case INSERT_IMAGE_REQUEST_CODE:
                imagePicker(data.getIntExtra("insert image", -1));
                break;
            case EDIT_IMAGE_REQUEST_CODE:
                imagePickerDel(data.getIntExtra("insert image", -1));
                break;
            case DELETE_IMAGE_REQUEST_CODE:
                imagePickerDel(data.getIntExtra("insert image", -1));
                break;
            case CATEGORY_RESULT_CODE:
                categoryPicker(data.getIntExtra("collectionId",-1));
                break;
            case GPS_RESULT_CODE:
                localisationPicker(data.getIntExtra("gps_choose",-1));
                break;
            case REQUEST_GET_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    Uri imgUri = mPhotoManager.proceedResultPicture(this, data);
                    setImage(imgUri);
                }
                break;
            case 9:
                if (resultCode == Activity.RESULT_OK) {
                    LatLng location = data.getParcelableExtra("localisation");
                    if (location != null) {
                        mLocationUserSet = true;
                        updateLocationData(location, LOCATION_FROM_FRAGMENT);
                    } else {
                        mLocationUserSet = false;
                    }
                }
            default:
                break;
        }
    }

    private void setImage(Uri uri) {
        if(mImageListAdapter.getCount()==0) {
            mImagesUriList.add(null);
        }
        if (mImageId != -1) {
            removeNewAddedImage(uri);
        } else {
            setNewImagePosition(uri);
        }
        if(mImageListAdapter.getCount()==2){
            mPhotosList.setNumColumns(2);
        } else {
            mPhotosList.setNumColumns(3);
        }
    }

    private void removeNewAddedImage(Uri uri){
        if(mElementId!=-1){
            if(mImagesUriInsertList.containsKey(mImageId)){
                mImagesUriInsertList.remove(mImageId);
            }
            mImagesUriInsertList.put(mImageId,uri);
            mImagesPositionListTmp.put(mImageId,-1);
        }
        mImagesUriList.set(mImageId, uri);
        mImageListAdapter.notifyDataSetChanged();
        mImageId = -1;
    }

    private void setNewImagePosition(Uri uri){
        if(mElementId!=-1){
            mImagesUriInsertList.put(mImagesUriList.size(), uri);
            mImagesPositionListTmp.put(mImagesUriList.size(),-1);
        }
        mImagesUriList.add(uri);
        mImageListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(PHOTO_MANAGER_KEY,mPhotoManager);
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
                updateOrInsertElement();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void updateOrInsertElement(){
        String sName = setElementNameFromForm();
        String sDescription = setElementDescriptionFromForm();
        if(sName.isEmpty()){
            Toast.makeText(getActivity(),
                    getString(R.string.required_name_element),
                    Toast.LENGTH_SHORT).show();
        } else if(sName.length()<2) {
            Toast.makeText(getActivity(),
                    getString(R.string.required_name_element_minimum_length),
                    Toast.LENGTH_SHORT).show();
        }else {
            ContentValues values = setContentValues(sName,sDescription);
            AsyncElementQueryHandler asyncHandler = new AsyncElementQueryHandler(
                    getActivity().getContentResolver()) {
            };
            if (mElementId != -1) {
                updateEditElement(sName,values,asyncHandler);
            } else {
                insertNewElement(sName,values,asyncHandler);
            }
        }
    }

    private ContentValues setContentValues(String sName, String sDescription){
        ContentValues values = new ContentValues();
        values.put(DataStorage.Items.NAME, sName);
        values.put(DataStorage.Items.DESCRIPTION, sDescription);
        values.put(DataStorage.Items.ID_COLLECTION, mCollectionId);
        if(mElementLocation!=null) {
            values.put(DataStorage.Items.LOCATION_LAT, mElementLocation.latitude);
            values.put(DataStorage.Items.LOCATION_LNG, mElementLocation.longitude);
            values.put(DataStorage.Items.LOCATION, mElementPosition.getText().toString());
        } else {
            values.put(DataStorage.Items.LOCATION, getResources().getString(R.string.gps_no_location));
        }
        return values;
    }

    private String setElementNameFromForm(){
        String sName;
        sName = mElementName.getText().toString();
        sName = sName.trim();
        return sName;
    }

    private String setElementDescriptionFromForm(){
        String sDescription = "";
        if (mElementDescription.getText() != null) {
            sDescription = mElementDescription.getText().toString();
            sDescription = sDescription.trim();
        }
        return sDescription;
    }

    private void updateEditElement(String sName,ContentValues values,AsyncQueryHandler asyncHandler){
        if(sName.equals(mActualElementName)&&(mCollectionId==mActualCollectionId)){
            values.put(DataStorage.Items.MODIFIED_DATE, Calendar
                    .getInstance().getTime().getTime());
            values.put(DataStorage.Items.SYNCHRONIZED, false);
            asyncHandler.startUpdate(0, null,
                    DataStorage.Items.CONTENT_URI, values,
                    DataStorage.Items._ID + " = ?",
                    new String[] { String.valueOf(mElementId) });
            getActivity().finish();
        }
        else{
            if(checkUniquenessElementName(sName)){
                values.put(DataStorage.Items.MODIFIED_DATE, Calendar
                        .getInstance().getTime().getTime());
                values.put(DataStorage.Items.SYNCHRONIZED, false);
                asyncHandler.startUpdate(0, null,
                        DataStorage.Items.CONTENT_URI, values,
                        DataStorage.Items._ID + " = ?",
                        new String[] { String.valueOf(mElementId) });
                getActivity().finish();
            }
            else{
                Toast.makeText(getActivity(),R.string.element_exist,Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void insertNewElement(String sName,ContentValues values, AsyncQueryHandler asyncHandler){
        if(checkUniquenessElementName(sName)) {
            values.put(DataStorage.Items.CREATED_DATE, Calendar
                    .getInstance().getTime().getTime());
            asyncHandler.startInsert(0, null,
                    DataStorage.Items.CONTENT_URI, values);
            getActivity().finish();
        } else{
            Toast.makeText(getActivity(),R.string.element_exist,Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkUniquenessElementName(String sName){
        Cursor cursor = getActivity().getContentResolver().query(DataStorage.Items.CONTENT_URI,
                new String[] {DataStorage.Items.NAME},DataStorage.Items.NAME + " = '" + sName + "' AND " +
                        DataStorage.Items.ID_COLLECTION + " = '" + mCollectionId +"' AND (" + DataStorage.Items.TABLE_NAME + "."
                        + DataStorage.Items.DELETED + " != '" + 1 + "')",null,null);
        return cursor == null || cursor.isAfterLast();
    }

    private class LoaderCategoriesCallbacks implements
            LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String[] projection = { DataStorage.Collections.NAME,
                    DataStorage.Collections._ID };
            String selection = String.format("%s!=%d",DataStorage.Collections.DELETED,1);
            return new CursorLoader(getActivity(),
                    DataStorage.Collections.CONTENT_URI, projection, selection,
                    null, DataStorage.Collections.NAME + " ASC");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            fillCategoriesData();
            mAdapterCategories.swapCursor(data);
            mAdapterCategories.getCursor().moveToFirst();
            for(int i=0;i<mAdapterCategories.getCount();i++){
                int column_index_id = mAdapterCategories.getCursor().getColumnIndex(DataStorage.Collections._ID);
                if(mAdapterCategories.getCursor().getInt(column_index_id)!=mCollectionId){
                    mAdapterCategories.getCursor().moveToNext();
                }else{
                    int column_index_name = mAdapterCategories.getCursor().getColumnIndex(DataStorage.Collections.NAME);
                    tvElementCategory.setText(mAdapterCategories.getCursor().getString(column_index_name));
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapterCategories.swapCursor(null);
        }
    }

    private class LoaderImagesCallbacks implements
            LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String[] projection = { DataStorage.Media.FILE_NAME,
                    DataStorage.Media.CREATED_DATE, DataStorage.Media._ID,
                    DataStorage.Media.ID_ITEM,DataStorage.Media.ID_SERVER };
            return new CursorLoader(getActivity(),
                    DataStorage.Media.CONTENT_URI, projection,
                    DataStorage.Media.ID_ITEM + " =? AND NOT "+ DataStorage.Media.DELETED,
                    new String[] { String.valueOf(mElementId)/*, String.valueOf(true)*/ },
                    DataStorage.Media.CREATED_DATE + " ASC");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if(data.getCount()!=0) {
                first = false;
            }
            if(data.getCount()!=0){
                mImagesUriList.add(null);
            }
            data.moveToFirst();
            int position = 1;
            while(!data.isAfterLast()){
                mImagesIDServerList.put(data.getInt(data.getColumnIndexOrThrow(DataStorage.Media._ID)),
                        data.getString(data.getColumnIndexOrThrow(DataStorage.Media.ID_SERVER)));
                mImagesPositionList.put(position,data.getInt(data.getColumnIndexOrThrow(DataStorage.Media._ID)));
                mImagesPositionListTmp.put(position,data.getInt(data.getColumnIndexOrThrow(DataStorage.Media._ID)));
                mImagesUriList.add(Uri.parse(data.getString(data.getColumnIndexOrThrow(DataStorage.Media.FILE_NAME))));
                data.moveToNext();
                position++;
            }
            mImageListAdapter = new ImageElementAdapterList(getActivity(),NO_FLAGS, mImagesUriList);
            mPhotosList.setAdapter(mImageListAdapter);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mImagesUriList.clear();
        }
    }

    private class AsyncImageQueryHandler extends AsyncQueryHandler {
        public AsyncImageQueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {

            super.onDeleteComplete(token, cookie, result);
        }

        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            super.onUpdateComplete(token, cookie, result);
        }

        @Override
        protected void onInsertComplete(int token, Object cookie, Uri uri) {
            super.onInsertComplete(token, cookie, uri);
        }

    }

    private class AsyncElementQueryHandler extends AsyncQueryHandler {
        private ContentResolver cr;

        public AsyncElementQueryHandler(ContentResolver cr) {
            super(cr);
            this.cr = cr;
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {

            super.onDeleteComplete(token, cookie, result);
        }

        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            super.onUpdateComplete(token, cookie, result);
            Map<Integer,Integer> map = new TreeMap<>(mImagesPositionListTmp);
            if(!map.isEmpty()){
                Iterator<Integer> keySetIterator = map.keySet().iterator();
                if(keySetIterator.hasNext()){
                    Integer key = keySetIterator.next();
                    if(map.get(key)==-1){
                        first = true;
                    }else{
                        updateElementAvatar(map.get(key));
                    }
                }
            }
            if(mImagesUriInsertList.size()!=0){
                for (Integer key : mImagesUriInsertList.keySet()) {
                    insertImage(mElementId, mImagesUriInsertList.get(key));
                }
            }
            if(mImagesUriDeleteList.size()!=0){
                for (Integer anImagesUriDeleteList : mImagesUriDeleteList) {
                    deleteImage(anImagesUriDeleteList);
                }
            }
        }

        @Override
        protected void onInsertComplete(int token, Object cookie, Uri uri) {
            super.onInsertComplete(token, cookie, uri);
            int id = Integer.parseInt(uri.getLastPathSegment());
            if (mImagesUriList.size()!=0)
                mImagesUriList.remove(0);
            for (Uri imageUri : mImagesUriList) {
                insertImage(id, imageUri);
            }
        }

        private void updateElementAvatar(int id){
            ContentValues values = new ContentValues();
            values.put(DataStorage.Media.AVATAR,true);
            AsyncImageQueryHandler asyncHandler = new AsyncImageQueryHandler(cr) {};
            asyncHandler.startUpdate(0,null,DataStorage.Media.CONTENT_URI,values,
                    DataStorage.Media._ID + " =? ",new String[] {String.valueOf(id)});
        }

        private void deleteImage(int id){
            ContentValues values = new ContentValues();
            values.put(DataStorage.Media.ID_SERVER,mImagesIDServerList.get(id));
            AsyncImageQueryHandler asyncHandler = new AsyncImageQueryHandler(cr) {};
            asyncHandler.startDelete(0,null,DataStorage.Media.CONTENT_URI,
                    DataStorage.Media._ID + " =? ",new String[] {String.valueOf(id)});
            asyncHandler.startInsert(0,null,DataStorage.DeletedMedia.CONTENT_URI,values);
        }

        private void insertImage(int id, Uri uri) {
            ContentValues values = new ContentValues();
            values.put(DataStorage.Media.ID_ITEM, id);
            values.put(DataStorage.Media.FILE_NAME, uri.toString());
            values.put(DataStorage.Media.SYNCHRONIZED, false);
            if (first) {
                values.put(DataStorage.Media.AVATAR, true);
                first = false;
            } else {
                values.put(DataStorage.Media.AVATAR, false);
            }
            AsyncImageQueryHandler asyncHandler = new AsyncImageQueryHandler(cr) {};
            values.put(DataStorage.Media.CREATED_DATE, Calendar.getInstance()
                    .getTime().getTime());
            asyncHandler.startInsert(0, null, DataStorage.Media.CONTENT_URI,
                    values);
        }
    }

    private class AsyncLocationName extends AsyncTask<LatLng,Integer,JSONObject> {
        @Override
        protected JSONObject doInBackground(LatLng... params) {
            return getLocationInfo(params[0].latitude, params[0].longitude);
        }

        @Override
        protected void onPostExecute(JSONObject object) {
            JSONObject location, geometryLocation, geometry;
            String location_string;
            try {
                //Get JSON Array called "results" and then get the 0th complete object as JSON
                location = object.getJSONArray("results").getJSONObject(0);
                geometry = object.getJSONArray("results").getJSONObject(0).getJSONObject("geometry");
                geometryLocation = geometry.getJSONObject("location");
                // Get the value of the attribute whose name is "formatted_string"
                location_string = new String(location.getString("formatted_address").getBytes("ISO-8859-1"),"UTF-8");
                double lat = Double.parseDouble(geometryLocation.getString("lat"));
                double lon = Double.parseDouble(geometryLocation.getString("lng"));
                mElementLocation = new LatLng(lat,lon);
                mElementPosition.setText(location_string);
                mElementPosition.setTextColor(Color.GREEN);
            } catch (JSONException | UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
        }

        public JSONObject getLocationInfo( double lat, double lng) {
            HttpGet httpGet = new HttpGet("http://maps.google.com/maps/api/geocode/json?latlng="+lat+","+lng+"&language=pl&sensor=false");
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            StringBuilder stringBuilder = new StringBuilder();
            InputStream stream = null;

            try {
                response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                stream = entity.getContent();
                int b;
                while ((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }
            } catch (IOException e) {
                Log.d("NO CONNECTION", "No internet connection");
            } finally {
                try{
                    if(stream != null) {
                        stream.close();
                    }
                } catch(IOException e) {
                    Log.d("IOException stream", "Stream IOException");
                }
            }

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject = new JSONObject(stringBuilder.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                return null;
            }
            return jsonObject;
        }
    }

    public void gpsEnabled(boolean gpsEnabled) {
        this.mGpsEnabled = gpsEnabled;
        if(mElementPosition!=null && mElementLocation==null) {
            if(!gpsEnabled) {
                mElementPosition.setText(R.string.gps_no_signal);
                mElementPosition.setTextColor(Color.RED);
            } else {
                mElementPosition.setText(R.string.gps_finding_location);
                mElementPosition.setTextColor(Color.YELLOW);
            }
        } else {
            // TODO loading
            // TODO after return when wifi off with no location change don't change text
            if(mElementPosition!=null) {
                mElementPosition.setText(R.string.gps_finding_location);
                mElementPosition.setTextColor(Color.YELLOW);
            }
        }
    }

    public void putLocation(LatLng location) {
        if(mElementPosition != null) {
            updateLocationData(location,LOCATION_FROM_ACTIVITY);
        }
    }

    private void updateLocationData(LatLng location, int source) {
        if(location==null || (location.latitude == 0 && location.longitude == 0)) {
            return;
        }

        if(location == mElementLocation) {
            return;
        }

        switch(source) {
            case LOCATION_FROM_FRAGMENT:
                new AsyncLocationName().execute(location);
//                elementLocation = location;
                break;
            case LOCATION_FROM_ACTIVITY:
                if(mLocationUserSet) {
                    break;
                }
                if(mImagesUriList != null && mImagesUriList.size()>1) {
                    new AsyncLocationName().execute(location);
                }
                mElementLocation = location;
                break;
            default:
                break;
        }
    }
}