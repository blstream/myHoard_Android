package com.myhoard.app.element;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
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
import android.text.format.DateFormat;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/*
 * Created by Sebastian Peryt on 27.02.14.
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

    private static final String PHOTO_MANAGER_KEY = "photoManagerKey";
    private static final int REQUEST_GET_PHOTO = 1;

    private static final String TAG = "ElementFragment";
    private static final boolean D = false;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int SELECT_PICTURE = 1;

    private static final int LOADER_CATEGORIES = 1;
    private static final int LOADER_IMAGES = 2;
    private static final int NO_FLAGS = 0;

    private boolean first = true;
    private static int sLastImageIndex;

    /*
     * AWA:FIXME: Niepotrzebne prefiksy określające typ Patrz:Ksiazka:Czysty
     * kod:Rozdział 2:Nazwy klas, metod….
     */
    private TextView tvElementPosition,
            tvElementCategory;
    private EditText etElementName, etElementDescription;
    private String sCurrentPhotoPath;
    private String sImagePath;
    private int iCollectionId;
    private int elementId;
    private Context context;
    // private ScaleImageView ivElementPhoto;
    private SimpleCursorAdapter adapterCategories;
    private GridView gvPhotosList;
    private ArrayList<Uri> imagesUriList;
    private ArrayList<Integer> imagesUriDeleteList;
    private HashMap<Integer,Uri> imagesUriInsertList;
    private HashMap<Integer,Integer> imagesPositionListTmp;
    private HashMap<Integer,Integer> imagesPositionList;
    private HashMap<Integer,String> imagesIDServerList;
    private ImageElementAdapterList imageListAdapter;
    private int imageId;
    private Item element;
    private boolean locationUserSet = false;
    private boolean gpsEnabled = false;
    private String mActualElementName;
    private PhotoManager photoManager;

    private LatLng elementLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_element_addedit, container,
                false);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            photoManager = savedInstanceState.getParcelable(PHOTO_MANAGER_KEY);
        } else {
            photoManager = new PhotoManager(this,REQUEST_GET_PHOTO);
        }

        final RelativeLayout rlEmptyView = (RelativeLayout) v.findViewById(R.id.element_emptyview);
        final LinearLayout lnEmptyViewClickable = (LinearLayout) v.findViewById(R.id.emptyview_inside);
        context = getActivity();
        imagesUriList = new ArrayList<Uri>();

        tvElementPosition = (TextView) v.findViewById(R.id.tvElementLocalisation);
        etElementName = (EditText) v.findViewById(R.id.etElementName);
        etElementDescription = (EditText) v.findViewById(R.id.etElementDescription);
        tvElementCategory = (TextView) v.findViewById(R.id.tvElementCategory);
        gvPhotosList = (GridView) v.findViewById(R.id.gvPhotosList);
        gvPhotosList.setEmptyView(rlEmptyView);

        tvElementPosition.setOnClickListener(this);

        if(!gpsEnabled) {
            tvElementPosition.setText("brak sygnału gps");
            tvElementPosition.setTextColor(Color.RED);
        } else {
            tvElementPosition.setText("ustalam");
            tvElementPosition.setTextColor(Color.YELLOW);
        }

        elementId = -1;
        iCollectionId = -1;
        imageId = -1;
//        editionMode = false;

        Bundle b = getArguments();
        LatLng location = b.getParcelable("location");
        if(b.getLong("categoryId",-1)!=-1) {
            iCollectionId = (int) b.getLong("categoryId");
        } else if(b.getParcelable("element")!=null) {
            element = b.getParcelable("element");
            elementId = Integer.parseInt(element.getId());
            iCollectionId = Integer.parseInt(element.getCollection());

            tvElementPosition.setText(element.getLocationTxt());
            tvElementPosition.setTextColor(Color.GREEN);
            elementLocation = new LatLng(element.getLocation().lat,element.getLocation().lng);

            etElementName.setText(element.getName());
            mActualElementName = etElementName.getText().toString().trim();

            etElementDescription.setText(element.getDescription());
            imagesUriInsertList = new HashMap<>();
            imagesPositionListTmp = new HashMap<>();
            imagesPositionList = new HashMap<>();
            imagesUriDeleteList = new ArrayList<>();
            imagesIDServerList = new HashMap<>();
        }
        gvPhotosList.setAdapter(getPhotosList());
        gvPhotosList.setOnItemClickListener(this);
        tvElementCategory.setOnClickListener(this);
        lnEmptyViewClickable.setOnClickListener(this);

        getLoaderManager().initLoader(LOADER_CATEGORIES, null,
                new LoaderCategoriesCallbacks());
        updateLocationData(location,0);
        return v;
    }

    private ListAdapter getPhotosList() {
        if (elementId != -1) {
            fillPhotosData();
        } else{
            imageListAdapter = new ImageElementAdapterList(getActivity(),NO_FLAGS, imagesUriList);
        }
        return imageListAdapter;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvElementLocalisation:
                if(!gpsEnabled){
                    localisationPicker();
                } else {
                    Intent intent = new Intent(getActivity(), ElementMapActivity.class);
                    intent.putExtra("localisation",elementLocation);
                    startActivityForResult(intent, 9);
                }
                break;
            case R.id.tvElementCategory:
                categoryPicker();
                break;
            case R.id.emptyview_inside:
                imagePicker();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(i == 0) {
            imagePicker();
        } else {
            imageId = (int) l;
            imagePickerDel();
        }
    }

    /**
     * Method shows category picker with categories from whole database.
     */
    private void categoryPicker() {
        AlertDialog.Builder categoryDialogBuilder = new AlertDialog.Builder(
                context);

        categoryDialogBuilder.setAdapter(adapterCategories,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        adapterCategories.getCursor().moveToPosition(i);
                        int columnIndex = adapterCategories.getCursor()
                                .getColumnIndex(DataStorage.Collections.NAME);
                        tvElementCategory.setText(adapterCategories.getCursor()
                                .getString(columnIndex));
                        iCollectionId = (int) adapterCategories.getItemId(i);
                    }
                });

        AlertDialog choseDialog = categoryDialogBuilder.create();
        choseDialog.show();

    }

    private void localisationPicker() {
        AlertDialog.Builder localisationDialogBuilder = new AlertDialog.Builder(
                context);

        final String[] element = new String[]{"włącz gps", "ustal pozycje"};

        localisationDialogBuilder.setItems(element, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i) {
                    case 0:
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 10);
                        break;
                    case 1:
                        Intent intent = new Intent(getActivity(), ElementMapActivity.class);
                        if(elementLocation!=null){
                            intent.putExtra("localisation",elementLocation);
                        }
                        startActivityForResult(intent, 9);
                        break;
                }
            }
        });

        AlertDialog choseDialog = localisationDialogBuilder.create();
        choseDialog.show();
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

        adapterCategories = new SimpleCursorAdapter(context,
                android.R.layout.simple_list_item_1, null, from, to, NO_FLAGS);
    }

    /**
     * Fill adapter data with proper cursor values
     */
    private void fillPhotosData() {
        getLoaderManager().initLoader(LOADER_IMAGES, null,
                new LoaderImagesCallbacks());
    }

    /**
     * Method shows source picker, where user chose source of element image.
     */
    private void imagePicker() {
        AlertDialog.Builder pickerDialogBuilder = new AlertDialog.Builder(
                context);

        pickerDialogBuilder.setItems(R.array.actions_on_picker,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try{
                            if (which == 0) // add from camera
                            {
                                photoManager.takePicture(PhotoManager.MODE_CAMERA);
                            } else if (which == 1) // add from gallery
                            {
                                photoManager.takePicture(PhotoManager.MODE_GALLERY);
                            }
                        } catch (IOException io) {
                            //TODO show error
                        }
                    }
                });
        pickerDialogBuilder
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        imageId = -1;
                    }
                });

        AlertDialog choseDialog = pickerDialogBuilder.create();
        choseDialog.show();

    }

    private void imagePickerDel() {
        AlertDialog.Builder pickerDialogBuilder = new AlertDialog.Builder(
                context);
        if(elementId!=-1){
            pickerDialogBuilder.setItems(R.array.action_on_picker_edit,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            deleteImage(imageId);
                        }
                    });
            pickerDialogBuilder
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            imageId = -1;
                        }
                    });
        }else{
            pickerDialogBuilder.setItems(R.array.actions_on_picker_del,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            try {
                                if (which == 0) // add from camera
                                {
                                    photoManager.takePicture(PhotoManager.MODE_CAMERA);
                                } else if (which == 1) // add from gallery
                                {
                                    photoManager.takePicture(PhotoManager.MODE_GALLERY);
                                } else if (which == 2) {
                                    deleteImage(imageId);
                                }
                            } catch (IOException io) {
                                //TODO show error
                            }
                        }
                    });
            pickerDialogBuilder
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            imageId = -1;
                        }
                    });
        }

        AlertDialog choseDialog = pickerDialogBuilder.create();
        choseDialog.show();

    }

    private void deleteImage(int id) {
        if (elementId != -1) {
            if(imagesUriInsertList.containsKey(id)){
                imagesUriInsertList.remove(id);
            }else{
                if(imagesUriDeleteList.contains(id)){
                    imagesUriDeleteList.remove(id);
                }
                imagesPositionListTmp.remove(id);
                imagesUriDeleteList.add(id);
            }
        }
        imagesUriList.remove(id);
        if(imagesUriList.size()==1) {
            imagesUriList.clear();
        }
        if(imagesUriList.size() == 2) {
            gvPhotosList.setNumColumns(2);
        } else {
            gvPhotosList.setNumColumns(3);
        }
        imageId = -1;
        imageListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_GET_PHOTO:
                        Uri imgUri = photoManager.proceedResultPicture(this,data);
                        setImage(imgUri);
                    break;
                case 9:
                    LatLng location = data.getParcelableExtra("localisation");
                    if(location != null) {
                        locationUserSet = true;
                        updateLocationData(location,0);
                    } else {
                        locationUserSet = false;
                    }
                default:
                    break;
            }
        } else {
            // Response is wrong - visible only in debug mode
            if (D)
                Log.d(TAG, "Response != " + Activity.RESULT_OK);
        }
    }

    private void setImage(Uri uri) {
        if(imageListAdapter.getCount()==0) {
            imagesUriList.add(null);
        }
        if (imageId != -1) {
            if(elementId!=-1){
                if(imagesUriInsertList.containsKey(imageId)){
                    imagesUriInsertList.remove(imageId);
                }
                imagesUriInsertList.put(imageId,uri);
                imagesPositionListTmp.put(imageId,-1);
            }
            imagesUriList.set(imageId, uri);
            imageListAdapter.notifyDataSetChanged();
            imageId = -1;
        } else {
            if(elementId!=-1){
                imagesUriInsertList.put(imagesUriList.size(), uri);
                imagesPositionListTmp.put(imagesUriList.size(),-1);
            }
            imagesUriList.add(uri);
            imageListAdapter.notifyDataSetChanged();
        }
        if(imageListAdapter.getCount()==2){
            gvPhotosList.setNumColumns(2);
        } else {
            gvPhotosList.setNumColumns(3);
        }
    }

    private int getCurrentDate() {
        Date d = new Date();
        CharSequence s = DateFormat.format("dMMyyyy", d.getTime());
        return Integer.getInteger(s.toString());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(PHOTO_MANAGER_KEY,photoManager);
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
                String sName;
                sName = etElementName.getText().toString();
                sName = sName.trim();
                if(sName.isEmpty()){
                    Toast.makeText(getActivity(),
                            getString(R.string.required_name_element),
                            Toast.LENGTH_SHORT).show();
                } else if(sName.length()<2) {
                    Toast.makeText(getActivity(),
                            getString(R.string.required_name_element_minimum_length),
                            Toast.LENGTH_SHORT).show();
                }else {
                    String sDescription = "";
                    if (etElementDescription.getText() != null) {
                        sDescription = etElementDescription.getText().toString();
                        sDescription = sDescription.trim();
                    }
                    ContentValues values = new ContentValues();
                    values.put(DataStorage.Items.NAME, sName);
                    values.put(DataStorage.Items.DESCRIPTION, sDescription);
                    values.put(DataStorage.Items.ID_COLLECTION, iCollectionId);
                    if(elementLocation!=null) {
                        values.put(DataStorage.Items.LOCATION_LAT, elementLocation.latitude);
                        values.put(DataStorage.Items.LOCATION_LNG, elementLocation.longitude);
                        values.put(DataStorage.Items.LOCATION, tvElementPosition.getText().toString());
                    } else {
                        values.put(DataStorage.Items.LOCATION, "Brak");
                    }
                    AsyncElementQueryHandler asyncHandler = new AsyncElementQueryHandler(
                            getActivity().getContentResolver()) {
                    };
                    if (elementId != -1) {
                        if(sName.equals(mActualElementName)){
                            values.put(DataStorage.Items.MODIFIED_DATE, Calendar
                                    .getInstance().getTime().getTime());
                            values.put(DataStorage.Items.SYNCHRONIZED, false);
                            asyncHandler.startUpdate(0, null,
                                    DataStorage.Items.CONTENT_URI, values,
                                    DataStorage.Items._ID + " = ?",
                                    new String[] { String.valueOf(elementId) });
                            getActivity().finish();
                        } else{
                            if(checkUniquenessElementName(sName)){
                                values.put(DataStorage.Items.MODIFIED_DATE, Calendar
                                        .getInstance().getTime().getTime());
                                values.put(DataStorage.Items.SYNCHRONIZED, false);
                                asyncHandler.startUpdate(0, null,
                                        DataStorage.Items.CONTENT_URI, values,
                                        DataStorage.Items._ID + " = ?",
                                        new String[] { String.valueOf(elementId) });
                                getActivity().finish();
                            }
                            else{
                                Toast.makeText(getActivity(),R.string.element_exist,Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
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
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private boolean checkUniquenessElementName(String sName){
        Cursor cursor = getActivity().getContentResolver().query(DataStorage.Items.CONTENT_URI,
                new String[] {DataStorage.Items.NAME},DataStorage.Items.NAME + " = '" + sName + "' AND " +
                        DataStorage.Items.ID_COLLECTION + " = '" + iCollectionId +"'",null,null);
        if(cursor!=null){
            if(cursor.isAfterLast()){
                return true;
            }else{
                return false;
            }
        }
        return true;
    }

    private class LoaderCategoriesCallbacks implements
            LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String[] projection = { DataStorage.Collections.NAME,
                    DataStorage.Collections._ID };
            CursorLoader cursorLoader = new CursorLoader(getActivity(),
                    DataStorage.Collections.CONTENT_URI, projection, null,
                    null, DataStorage.Collections.NAME + " ASC");
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            // According to documentation moveToPosition range of values is -1 <= position <= count. This is why there is -1
            // FIXME Dziwny bład z rzutowaniem long na int. Do sprawdzenia na innych wersjach Androida
//            data.moveToPosition(iCollectionId-1);
            fillCategoriesData();
            adapterCategories.swapCursor(data);
            adapterCategories.getCursor().moveToFirst();
            for(int i=0;i<adapterCategories.getCount();i++){
                int column_index_id = adapterCategories.getCursor().getColumnIndex(DataStorage.Collections._ID);
                if(adapterCategories.getCursor().getInt(column_index_id)!=iCollectionId){
                    adapterCategories.getCursor().moveToNext();
                }else{
                    int column_index_name = adapterCategories.getCursor().getColumnIndex(DataStorage.Collections.NAME);
                    tvElementCategory.setText(adapterCategories.getCursor().getString(column_index_name));
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            adapterCategories.swapCursor(null);
        }
    }

    private class LoaderImagesCallbacks implements
            LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String[] projection = { DataStorage.Media.FILE_NAME,
                    DataStorage.Media.CREATED_DATE, DataStorage.Media._ID,
                    DataStorage.Media.ID_ITEM,DataStorage.Media.ID_SERVER };
            CursorLoader cursorLoader = new CursorLoader(getActivity(),
                    DataStorage.Media.CONTENT_URI, projection,
                    DataStorage.Media.ID_ITEM + " =? AND NOT "+ DataStorage.Media.DELETED,
                    new String[] { String.valueOf(elementId)/*, String.valueOf(true)*/ },
                    DataStorage.Media.CREATED_DATE + " ASC");
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if(data.getCount()!=0) {
                first = false;
            }
            if(data!=null){
                imagesUriList.add(null);
                data.moveToFirst();
                int position = 1;
                while(!data.isAfterLast()){
                    imagesIDServerList.put(data.getInt(data.getColumnIndexOrThrow(DataStorage.Media._ID)),
                            data.getString(data.getColumnIndexOrThrow(DataStorage.Media.ID_SERVER)));
                    imagesPositionList.put(position,data.getInt(data.getColumnIndexOrThrow(DataStorage.Media._ID)));
                    imagesPositionListTmp.put(position,data.getInt(data.getColumnIndexOrThrow(DataStorage.Media._ID)));
                    imagesUriList.add(Uri.parse(data.getString(data.getColumnIndexOrThrow(DataStorage.Media.FILE_NAME))));
                    data.moveToNext();
                    position++;
                }
                sLastImageIndex = position;
                imageListAdapter = new ImageElementAdapterList(getActivity(),NO_FLAGS, imagesUriList);
                gvPhotosList.setAdapter(imageListAdapter);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            imagesUriList.clear();
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
            /*if(!imagesPositionListTmp.isEmpty()){
                if(imagesPositionListTmp.get(1)==-1){
                    first = true;
                }else{
                    updateElementAvatar(imagesPositionListTmp.get(1));
                }
            }*/
            if(!imagesPositionListTmp.isEmpty()){
                Iterator<Integer> keySetIterator = imagesPositionListTmp.keySet().iterator();
                if(keySetIterator.hasNext()){
                    Integer key = keySetIterator.next();
                    if(imagesPositionListTmp.get(key)==-1){
                        first = true;
                    }else{
                        updateElementAvatar(imagesPositionListTmp.get(key));
                    }
                }
            }
            if(imagesUriInsertList.size()!=0){
                Iterator<Integer> keySetIterator = imagesUriInsertList.keySet().iterator();
                while(keySetIterator.hasNext()){
                    Integer key = keySetIterator.next();
                    insertImage(elementId, imagesUriInsertList.get(key));
                }
            }
            if(imagesUriDeleteList.size()!=0){
                for(int i=0;i<imagesUriDeleteList.size();i++){
                    deleteImage(imagesPositionList.get(imagesUriDeleteList.get(i)));
                }
            }
        }

        @Override
        protected void onInsertComplete(int token, Object cookie, Uri uri) {
            super.onInsertComplete(token, cookie, uri);
            int id = Integer.parseInt(uri.getLastPathSegment());
            if (imagesUriList.size()!=0)
                imagesUriList.remove(0);
            for (Uri imageUri : imagesUriList) {
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
            values.put(DataStorage.Media.ID_SERVER,imagesIDServerList.get(id));
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
            JSONObject json = getLocationInfo(params[0].latitude, params[0].longitude);
            return json;
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
                location_string = location.getString("formatted_address");
                double lat = Double.parseDouble(geometryLocation.getString("lat"));
                double lon = Double.parseDouble(geometryLocation.getString("lng"));
                LatLng latLng = new LatLng(lat,lon);
                elementLocation = latLng;
                tvElementPosition.setText(location_string);
                tvElementPosition.setTextColor(Color.GREEN);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }

        public JSONObject getLocationInfo( double lat, double lng) {
            Log.d("test", "start: " + lat + ":" + lng);
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
                //TODO push to UI
//                Toast.makeText(getActivity(),"Brak połaczenia internetowego",Toast.LENGTH_SHORT).show();
            } finally {
                try{
                    if(stream != null) {
                        stream.close();
                    }
                } catch(IOException e) {

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
        this.gpsEnabled = gpsEnabled;
        if(tvElementPosition!=null && elementLocation==null) {
            if(!gpsEnabled) {
                tvElementPosition.setText("brak sygnału gps");
                tvElementPosition.setTextColor(Color.RED);
            } else {
                tvElementPosition.setText("ustalam");
                tvElementPosition.setTextColor(Color.YELLOW);
            }
        } else {
            // TODO loading
            // TODO after return when wifi off with no location change don't change text
            tvElementPosition.setText("ustalam");
            tvElementPosition.setTextColor(Color.YELLOW);
        }
    }

    public void putLocation(LatLng location) {
        if(tvElementPosition != null) {
            updateLocationData(location,1);
        }
    }

    private void updateLocationData(LatLng location, int source) {
        if(location==null || (location.latitude == 0 && location.longitude == 0)) {
            return;
        }

        if(location == elementLocation) {
            return;
        }

        switch(source) {
            case 0:
                new AsyncLocationName().execute(location);
//                elementLocation = location;
                break;
            case 1:
                if(locationUserSet) {
                    break;
                }
                new AsyncLocationName().execute(location);
                elementLocation = location;
                break;
            default:
                break;
        }
    }
}
