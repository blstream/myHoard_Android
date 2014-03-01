package com.myhoard.app.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.myhoard.app.R;
import com.myhoard.app.provider.DataStorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Sebastian Peryt on 27.02.14.
 */
public class ElementFragment extends Fragment implements View.OnClickListener {

    // Debug mode variables
    private static final String TAG = "ElementFragment";
    private static final boolean D = false;
    // END - Debug mode variables

    public static final String ID = "elementId";
    public static final String NAME = "elementName";
    public static final String DESCRIPTION = "elementDescription";
    public static final String COLLECTION_ID = "elementCollectionId";
    public static final String CREATED_DATE = "elementCreatedDate";
    public static final String MODIFIED_DATE = "elementModifiedDate";
    public static final String TAGS = "elementTags";

    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int SELECT_PICTURE = 1;

    private TextView tvElementName, tvElementDescription;
    private EditText etElementName, etElementDescription;
    private String sCurrentPhotoPath;
    private String sImagePath;
    private ImageView ivElementPhoto;
    private Button btSave, btCancel;
    private int elementId;

    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_element, container, false);

        context = getActivity();

        tvElementName = (TextView) v.findViewById(R.id.nameTextView);
        tvElementDescription = (TextView) v.findViewById(R.id.descriptionTextView);
        etElementName = (EditText) v.findViewById(R.id.nameEditText);
        etElementDescription = (EditText) v.findViewById(R.id.descriptionEditText);
        ivElementPhoto = (ImageView) v.findViewById(R.id.ivThumbnailPhoto);

        btSave = (Button) v.findViewById(R.id.saveBtn);
        btCancel = (Button) v.findViewById(R.id.cancelBtn);
        btSave.setOnClickListener(this);
        btCancel.setOnClickListener(this);

        elementId = -1;

        Bundle b = getArguments();
        if(b!=null) {
            etElementName.setVisibility(View.INVISIBLE);
            etElementDescription.setVisibility(View.INVISIBLE);
            tvElementName.setVisibility(View.VISIBLE);
            tvElementDescription.setVisibility(View.VISIBLE);
            tvElementName.setText(b.getString(ElementFragment.NAME));
            tvElementDescription.setText(b.getString(ElementFragment.DESCRIPTION));
            //elementId = b.getInt(ElementFragment.ID);
            ivElementPhoto.setOnClickListener(this);
        } else {
            ivElementPhoto.setOnClickListener(this);
        }

        return v;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.saveBtn:
                // TODO in some production
                if (TextUtils.isEmpty(etElementName.getText()) && tvElementName.getText() == null) {
                    Toast.makeText(getActivity(), getString(R.string.required_name_element),
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
                    values.put(DataStorage.Elements.NAME, sName);
                    values.put(DataStorage.Elements.DESCRIPTION, sDescription);
                    values.put(DataStorage.Elements.AVATAR_FILE_NAME, sImagePath);
                    // TODO fix after explanation
                    //values.put(DataStorage.Elements.COLLECTION_ID);
                    //values.put(DataStorage.Elements.CREATED_DATE);
                    //values.put(DataStorage.Elements.MODIFIED_DATE);
                    //values.put(DataStorage.Elements.TAGS);
                    if (elementId!=-1) {
                        Toast.makeText(getActivity(),context.getString(R.string
                                .element_edited), Toast.LENGTH_SHORT).show();
                        getActivity().getContentResolver()
                                .update(DataStorage.Elements.CONTENT_URI, values,
                                        DataStorage.Elements._ID + " = " + elementId, null);
                    } else {
                        getActivity().getContentResolver()
                                .insert(DataStorage.Elements.CONTENT_URI, values);

                    }
                    getFragmentManager().popBackStackImmediate();
                }
                break;
            case R.id.cancelBtn:
                getFragmentManager().popBackStackImmediate();
                break;
            case R.id.ivThumbnailPhoto:
                imagePicker();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK)
        {
            Bitmap bitmap = null;
            Uri imgUri = null;
            switch(requestCode)
            {
                case REQUEST_IMAGE_CAPTURE:
                    // if image was added by photo
                    imgUri = galleryAddPic();
                    sImagePath = imgUri.getPath();
                    break;
                case SELECT_PICTURE:
                    // if image was added from gallery
                    imgUri = data.getData();
                    sImagePath = imgUri.getPath();
                    break;
                default:
                    return;
            }
            try {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imgUri);
            } catch (FileNotFoundException e) {
                Log.e(TAG,e.toString());
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
            ivElementPhoto.setImageBitmap(bitmap);
        } else {
            // Response is wrong - visible only in debug mode
            if(D) Log.d(TAG,"Response != " + Activity.RESULT_OK);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG,"Error occurred while creating the File");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /**
     * Method creates file into which the taken photo will be saved.
     * Without this mathod saving big photo is impossible/very difficult
     * @return the file in which photo will be saved
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir     /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        sCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }

    /**
     * Method refreshes gallery view with added photo, so
     * it can be seen by users in gallery.
     * @return uri with info about added image
     */
    private Uri galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(sCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
        return contentUri;
    }

    /**
     * Method shows source picker, where user chose source of element image.
     */
    private void imagePicker() {
        AlertDialog.Builder pickerDialogBuilder = new AlertDialog.Builder(context);

        pickerDialogBuilder.setItems(R.array.actions_on_picker,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        if (which == 0) // add from camera
                        {
                            dispatchTakePictureIntent();
                        } else if (which == 1) // add from gallery
                        {
                            Intent i = new Intent(
                                    Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(i, SELECT_PICTURE);
                        }
                    }
                });

        AlertDialog choseDialog = pickerDialogBuilder.create();
        choseDialog.show();

    }
}
