package com.myhoard.app.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.myhoard.app.R;
import com.myhoard.app.adapters.ImageElementAdapterCursor;
import com.myhoard.app.adapters.ImageElementAdapterList;
import com.myhoard.app.gps.GPSProvider;
import com.myhoard.app.images.PhotoManager;
import com.myhoard.app.provider.DataStorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/*
 * Created by Sebastian Peryt on 27.02.14.
 */
public class ElementFragment extends Fragment implements View.OnClickListener,
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

    /*
     * AWA:FIXME: Niepotrzebne prefiksy określające typ Patrz:Ksiazka:Czysty
     * kod:Rozdział 2:Nazwy klas, metod….
     */
    private TextView tvElementName, tvElementDescription, tvElementPosition,
            tvElementCategory;
    private EditText etElementName, etElementDescription;
    private String sCurrentPhotoPath;
    private String sImagePath;
    private int iCollectionId;
    private int elementId;
    private Context context;
    // private ScaleImageView ivElementPhoto;
    private SimpleCursorAdapter adapterCategories, adapterItems;
    private ImageElementAdapterCursor adapterImages;
    private GridView gvPhotosList;
    private ArrayList<Uri> imagesUriList;
    private ImageElementAdapterList imageListAdapter;
    private int imageId;
    private boolean editionMode;

    private PhotoManager photoManager;

    GPSProvider mService;
    boolean mBound = false;
    /*
     * Część kodu odpowiedzialna za binder
     * (http://developer.android.com/guide/components/bound-services.html)
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Przywiazano do uslugi i rzutowano IBinder
            GPSProvider.LocalGPSBinder binder = (GPSProvider.LocalGPSBinder) service;
            mService = binder.getService();
            mBound = true;
            if (D)
                Log.d(TAG, "Binder");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    /*
     * broadcast reciver dzięki któremu istnieje połączenie z uslugą GPS
     */
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updatePosition(intent);
            Bundle b = intent.getExtras();
			/*
			 * AWA:FIXME: Hardcoded value Umiesc w private final static String,
			 * int, etc.... lub w strings.xml lub innym *.xml
			 */
            if (b != null && !b.getBoolean("GPS")) {
                tvElementPosition.setText("brak");
                tvElementPosition.setTextColor(Color.RED);
            }
        }
    };

	/*
	 * KONIEC - Część kodu odpowiedzialna za binder
	 */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_element, container,
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

        tvElementName = (TextView) v.findViewById(R.id.tvElementName);
        tvElementDescription = (TextView) v.findViewById(R.id.tvElementDescription);
        tvElementPosition = (TextView) v.findViewById(R.id.tvElementLocalisation);
        etElementName = (EditText) v.findViewById(R.id.etElementName);
        etElementDescription = (EditText) v.findViewById(R.id.etElementDescription);
        tvElementCategory = (TextView) v.findViewById(R.id.tvElementCategory);
        gvPhotosList = (GridView) v.findViewById(R.id.gvPhotosList);
        gvPhotosList.setEmptyView(rlEmptyView);

        tvElementPosition.setOnClickListener(this);

        tvElementPosition.setText("ustalam");
        tvElementPosition.setTextColor(Color.YELLOW);

        elementId = -1;
        iCollectionId = -1;
        imageId = -1;
        editionMode = false;

        Bundle b = getArguments();
        if (b != null) {
            iCollectionId = (int) b.getLong(ElementFragment.COLLECTION_ID);
            if (b.getInt(ElementFragment.ID) != -1) {
                elementId = (int) b.getLong(ElementFragment.ID);
                gvPhotosList.setAdapter(getPhotosList());
                editionMode = b.getBoolean(ElementFragment.EDITION);
                if (!editionMode) {
                    etElementName.setVisibility(View.GONE);
                    etElementDescription.setVisibility(View.GONE);
                    tvElementName.setVisibility(View.VISIBLE);
                    tvElementDescription.setVisibility(View.VISIBLE);
                    tvElementName.setText(b.getString(ElementFragment.NAME));
                    tvElementDescription.setText(b
                            .getString(ElementFragment.DESCRIPTION));
                } else {
                    etElementName.setVisibility(View.VISIBLE);
                    etElementDescription.setVisibility(View.VISIBLE);
                    tvElementName.setVisibility(View.GONE);
                    tvElementDescription.setVisibility(View.GONE);
                    etElementName.setText(b.getString(ElementFragment.NAME));
                    etElementDescription.setText(b
                            .getString(ElementFragment.DESCRIPTION));
                }
            } else {
                gvPhotosList.setAdapter(getPhotosList());
            }
        }
        gvPhotosList.setOnItemClickListener(this);
        tvElementCategory.setOnClickListener(this);
        lnEmptyViewClickable.setOnClickListener(this);

        getLoaderManager().initLoader(LOADER_CATEGORIES, null,
                new LoaderCategoriesCallbacks());
        return v;
    }

    private ListAdapter getPhotosList() {
        if (elementId != -1) {
            fillPhotosData();
            return adapterImages;
        } else {
            imageListAdapter = new ImageElementAdapterList(getActivity(),
                    NO_FLAGS, imagesUriList);
            return imageListAdapter;
        }
    }

    @Override
    public void onClick(View view) {
        if (!editionMode && elementId != -1) {
            return;
        }
        switch (view.getId()) {
            case R.id.tvElementLocalisation:
                if (String.valueOf(tvElementPosition.getText()).equals("brak")) {
                    startActivity(new Intent(
                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
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
        if (!editionMode && elementId != -1) {
            return;
        }
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

//        getLoaderManager().initLoader(LOADER_CATEGORIES, null,
//                new LoaderCategoriesCallbacks());
//        getLoaderManager().initLoader(3, null, new LoaderItemsCallbacks());

        adapterCategories = new SimpleCursorAdapter(context,
                android.R.layout.simple_list_item_1, null, from, to, NO_FLAGS);
    }

    /**
     * Fill adapter data with proper cursor values
     */
    private void fillPhotosData() {
        getLoaderManager().initLoader(LOADER_IMAGES, null,
                new LoaderImagesCallbacks());

        adapterImages = new ImageElementAdapterCursor(getActivity(), null,
                NO_FLAGS);
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

        AlertDialog choseDialog = pickerDialogBuilder.create();
        choseDialog.show();

    }

    private void deleteImage(int id) {
        if (elementId != -1) {
            ContentValues values = new ContentValues();
            values.put(DataStorage.Media.ID_ITEM, id);
            AsyncImageQueryHandler asyncHandler = new AsyncImageQueryHandler(
                    getActivity().getContentResolver()) {
            };
            asyncHandler.startDelete(0, null, DataStorage.Media.CONTENT_URI,
                    DataStorage.Media._ID + " =? ",
                    new String[] { String.valueOf(id) });
        } else {
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_GET_PHOTO:
                        Uri imgUri = photoManager.proceedResultPicture(this,data);
                        setImage(imgUri);
                    break;
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
        switch (elementId) {
            case -1:
                if(imageListAdapter.getCount()==0) {
                    imagesUriList.add(null);
                }
                if (imageId != -1) {
                    imagesUriList.set(imageId, uri);
                    imageListAdapter.notifyDataSetChanged();
                    imageId = -1;
                } else {
                    imagesUriList.add(uri);
                    imageListAdapter.notifyDataSetChanged();
                }
                if(imageListAdapter.getCount()==2){
                    gvPhotosList.setNumColumns(2);
                } else {
                    gvPhotosList.setNumColumns(3);
                }
                break;
            default:
                ContentValues values = new ContentValues();
                values.put(DataStorage.Media.ID_ITEM, elementId);
                values.put(DataStorage.Media.FILE_NAME, uri.toString());
                AsyncImageQueryHandler asyncHandler = new AsyncImageQueryHandler(
                        getActivity().getContentResolver()) {
                };
                if (imageId != -1) {
                    values.put(DataStorage.Media.SYNCHRONIZED, false);
                    values.put(DataStorage.Media.AVATAR, false);
                    asyncHandler.startUpdate(0, null,
                            DataStorage.Media.CONTENT_URI, values,
                            DataStorage.Media._ID + " = ?",
                            new String[] { String.valueOf(imageId) });
                    imageId = -1;
                } else {
                    values.put(DataStorage.Media.AVATAR, true);
                    values.put(DataStorage.Media.CREATED_DATE, Calendar
                            .getInstance().getTime().getTime());
                    asyncHandler.startInsert(0, null,
                            DataStorage.Media.CONTENT_URI, values);
                }
                break;
        }
    }

    /*
     * Część kodu odpowiedzialna za GPS
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getBaseContext().bindService(
                new Intent(getActivity(), GPSProvider.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        context.registerReceiver(broadcastReceiver, new IntentFilter(
                GPSProvider.BROADCAST_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        context.unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onDestroy() {
        getActivity().getBaseContext().unbindService(mConnection);
        super.onDestroy();
    }

    private void updatePosition(Intent intent) {
        Log.e(TAG, "In");
        if (intent == null) {
            Log.e(TAG, "error");
            return;
        }
        Log.e(TAG, "OK");
        Bundle b = intent.getExtras();
        double lat = b.getDouble(GPSProvider.POS_LAT);
        double lon = b.getDouble(GPSProvider.POS_LON);

        tvElementPosition.setText(pos2str(lat, lon));
        tvElementPosition.setTextColor(Color.GREEN);
    }

    private String pos2str(double lat, double lon) {
        return lat + ":" + lon;
    }
	/*
	 * KONIEC - Część kodu odpowiedzialna za GPS
	 */

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
                if (!editionMode && elementId != -1) {
                    getFragmentManager().popBackStackImmediate();
                    break;
                }
                if (tvElementName.getText() == null
                        || TextUtils.isEmpty(etElementName.getText())) {
                    Toast.makeText(getActivity(),
                            getString(R.string.required_name_element),
                            Toast.LENGTH_SHORT).show();
                } else {
                    String sName = "", sDescription = "";
                    if (etElementName.getText() != null) {
                        sName = etElementName.getText().toString();
                    }
                    if (etElementDescription.getText() != null) {
                        sDescription = etElementDescription.getText().toString();
                    }
                    ContentValues values = new ContentValues();
                    values.put(DataStorage.Items.NAME, sName);
                    values.put(DataStorage.Items.DESCRIPTION, sDescription);
                    values.put(DataStorage.Items.ID_COLLECTION, iCollectionId);
                    Log.d("TAG", "collection id: " + iCollectionId);
                    AsyncElementQueryHandler asyncHandler = new AsyncElementQueryHandler(
                            getActivity().getContentResolver()) {
                    };
                    if (elementId != -1) {
                        values.put(DataStorage.Items.MODIFIED_DATE, Calendar
                                .getInstance().getTime().getTime());
                        values.put(DataStorage.Items.SYNCHRONIZED, false);
                        asyncHandler.startUpdate(0, null,
                                DataStorage.Items.CONTENT_URI, values,
                                DataStorage.Items._ID + " = ?",
                                new String[] { String.valueOf(elementId) });
                        fillPhotosData();
                    } else {
                        values.put(DataStorage.Items.CREATED_DATE, Calendar
                                .getInstance().getTime().getTime());
                        asyncHandler.startInsert(0, null,
                                DataStorage.Items.CONTENT_URI, values);
                    }
                    getFragmentManager().popBackStackImmediate();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
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
            if (adapterCategories == null) {
                // According to documentation moveToPosition range of values is -1 <= position <= count. This is why there is -1
                data.moveToPosition(iCollectionId - 1);
                String category = data.getString(data.getColumnIndex(DataStorage.Collections.NAME));
                tvElementCategory.setText(category);
                fillCategoriesData();
            }
            adapterCategories.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            adapterCategories.swapCursor(null);
        }
    }

    private class LoaderItemsCallbacks implements
            LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String[] projection = { DataStorage.Items.TABLE_NAME + "."
                    + DataStorage.Items._ID };
            CursorLoader cursorLoader = new CursorLoader(getActivity(),
                    DataStorage.Items.CONTENT_URI, projection, null, null, null);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            // Toast.makeText(context,"Ilosc: " +
            // data.getCount(),Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    }

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
                    new String[] { String.valueOf(elementId) },
                    DataStorage.Media.CREATED_DATE + " ASC");
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            adapterImages.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            adapterImages.swapCursor(null);
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
            fillPhotosData();
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

        private void insertImage(int id, Uri uri) {
            ContentValues values = new ContentValues();
            values.put(DataStorage.Media.ID_ITEM, id);
            values.put(DataStorage.Media.FILE_NAME, uri.toString());
            values.put(DataStorage.Media.SYNCHRONIZED, false);
            if (first) {
                values.put(DataStorage.Media.AVATAR, true);
                first = false;
            } else
                values.put(DataStorage.Media.AVATAR, false);
            AsyncImageQueryHandler asyncHandler = new AsyncImageQueryHandler(cr) {
            };
            values.put(DataStorage.Media.CREATED_DATE, Calendar.getInstance()
                    .getTime().getTime());
            asyncHandler.startInsert(0, null, DataStorage.Media.CONTENT_URI,
                    values);
        }
    }
}
