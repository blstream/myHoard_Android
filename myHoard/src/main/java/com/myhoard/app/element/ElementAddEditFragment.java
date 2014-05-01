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
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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
import com.myhoard.app.images.PhotoManager;
import com.myhoard.app.model.Item;
import com.myhoard.app.provider.DataStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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
    private SimpleCursorAdapter adapterCategories, adapterItems;
    private ImageElementAdapterCursor adapterImages;
    private GridView gvPhotosList;
    private ArrayList<Uri> imagesUriList;
    private ImageElementAdapterList imageListAdapter;
    private int imageId;
    private Item element;

    private PhotoManager photoManager;

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

        tvElementPosition.setText("ustalam");
        tvElementPosition.setTextColor(Color.YELLOW);

        elementId = -1;
        iCollectionId = -1;
        imageId = -1;
//        editionMode = false;

        Bundle b = getArguments();
        if(b.getLong("categoryId",-1)!=-1) {
            iCollectionId = (int) b.getLong("categoryId");
        } else if(b.getParcelable("element")!=null) {
            element = b.getParcelable("element");
            elementId = Integer.parseInt(element.getId());
            iCollectionId = Integer.parseInt(element.getCollection());

            etElementName.setText(element.getName());
        }
        gvPhotosList.setAdapter(getPhotosList());
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
//        if (!editionMode && elementId != -1) {
//            return;
//        }
        switch (view.getId()) {
            case R.id.tvElementLocalisation:
                if (String.valueOf(tvElementPosition.getText()).equals("brak")) {
//                    startActivity(new Intent(
//                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    startActivityForResult(new Intent(getActivity(),ElementMapFragment.class),9);
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
//        if (!editionMode && elementId != -1) {
//            return;
//        }
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
//            values.put(DataStorage.Media.ID_ITEM, id);
            values.put(DataStorage.Media.DELETED,true);
            AsyncImageQueryHandler asyncHandler = new AsyncImageQueryHandler(
                    getActivity().getContentResolver()) {
            };
            asyncHandler.startUpdate(0, null, DataStorage.Media.CONTENT_URI, values, DataStorage.Media._ID + " =? ",
                    new String[] { String.valueOf(id) });
//            asyncHandler.startDelete(0, null, DataStorage.Media.CONTENT_URI,
//                    DataStorage.Media._ID + " =? ",
//                    new String[] { String.valueOf(id) });
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
        Log.i("TAG","elementId: " + elementId + " " + imageId);
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
                    values.put(DataStorage.Media.AVATAR, first);
                    values.put(DataStorage.Media.CREATED_DATE, Calendar
                            .getInstance().getTime().getTime());
                    asyncHandler.startInsert(0, null,
                            DataStorage.Media.CONTENT_URI, values);
                }
                break;
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
                if (TextUtils.isEmpty(etElementName.getText())) {
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
                        getFragmentManager().popBackStackImmediate();
                        getFragmentManager().popBackStackImmediate();
                    } else {
                        values.put(DataStorage.Items.CREATED_DATE, Calendar
                                .getInstance().getTime().getTime());
                        asyncHandler.startInsert(0, null,
                                DataStorage.Items.CONTENT_URI, values);
                        getActivity().finish();
                    }
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
            // According to documentation moveToPosition range of values is -1 <= position <= count. This is why there is -1
            data.moveToPosition(iCollectionId - 1);
            String category = data.getString(data.getColumnIndex(DataStorage.Collections.NAME));
            tvElementCategory.setText(category);
            fillCategoriesData();
            adapterCategories.swapCursor(data);
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
                    DataStorage.Media.ID_ITEM };
            CursorLoader cursorLoader = new CursorLoader(getActivity(),
                    DataStorage.Media.CONTENT_URI, projection,
                    DataStorage.Media.ID_ITEM + " =? ",
                    // TODO working on image deleteting
//                    AND " + DataStorage.Media.DELETED +
//                    " != ? ",
                    new String[] { String.valueOf(elementId)/*, String.valueOf(true)*/ },
                    DataStorage.Media.CREATED_DATE + " ASC");
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if(data.getCount()!=0) {
                first = false;
            }
            String[] projection = { DataStorage.Media.FILE_NAME,
                    DataStorage.Media.CREATED_DATE, DataStorage.Media._ID,
                    DataStorage.Media.ID_ITEM };
            MatrixCursor extras = new MatrixCursor(projection);
            extras.addRow(new String[] { "", "", "-2", String.valueOf(elementId) });
            Cursor[] cursors = { extras, data };
            Cursor extendedCursor = new MergeCursor(cursors);
            adapterImages.swapCursor(extendedCursor);
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
}
