package com.myhoard.app.images;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by Sebastian on 27.04.14.
 */
public class PhotoManager implements Parcelable {

	public static final int MODE_GALLERY = 1;
	public static final int MODE_CAMERA = 2;

    private static final String SIMPLE_FORMAT_FORMATTING = "yyyyMMdd_HHmmss";
    private static final String FILE_NAME_PREFIX = "MYH_";
    private static final String FILE_EXTENSION = ".jpg";

	private Fragment fragment;
	private int requestCode;

	public String sCurrentPhotoPath;

	public PhotoManager(Fragment fragment, int code) {
		this.fragment = fragment;
		this.requestCode = code;
	}
	
	public PhotoManager(Parcel in) {
		readFromParce(in);
	}

    /**
     * Method allows to chose from where picture should be added
     * @param mode of picture: MODE_GALLERY or MODE_CAMERA
     * @throws IOException
     */
    public void takePicture(int mode) throws IOException {
		switch (mode) {
		case MODE_GALLERY:
			Intent i = new Intent(Intent.ACTION_PICK,
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			fragment.startActivityForResult(i, requestCode);
			break;
		case MODE_CAMERA:
			dispatchTakePictureIntent();
			break;
		default:
			break;
		}
	}

    /**
     * Method responsible for preparing everything for photo taking
     * @throws IOException
     */
    private void dispatchTakePictureIntent() throws IOException {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(fragment.getActivity().getPackageManager()) != null) {
			// Create the File where the photo should go
			File photoFile = createImageFile();
			// Continue only if the File was successfully created
			if (photoFile != null) {
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(photoFile));
				fragment.startActivityForResult(takePictureIntent, requestCode);
			}
		}
	}

    /**
     * Methods update gallery view after new photo has been taken
     * @return Uri of the new photo
     */
	private Uri galleryAddPic() {
		Intent mediaScanIntent = new Intent(
				Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		File f = new File(sCurrentPhotoPath);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		fragment.getActivity().sendBroadcast(mediaScanIntent);
		return contentUri;
	}

	/**
	 * Method creates file into which the taken photo will be saved. Without
	 * this mathod saving big photo is impossible/very difficult
	 * 
	 * @return the file in which photo will be saved
	 * @throws java.io.IOException
	 */
	private File createImageFile() throws IOException {
		String timeStamp = new SimpleDateFormat(SIMPLE_FORMAT_FORMATTING)
				.format(new Date());
		String imageFileName = FILE_NAME_PREFIX + timeStamp;
		File storageDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(imageFileName, /* prefix */
				FILE_EXTENSION, /* suffix */
				storageDir /* directory */
		);

		// Save a file: path for use with ACTION_VIEW intents
		sCurrentPhotoPath = image.getAbsolutePath();

		return image;
	}

    /**
     * Method used in onActivityResult to proceed data returned. It is aware of what intent has been called.
     * @param fragment calling fragment
     * @param data received by onActivityResult
     * @return
     */
    public Uri proceedResultPicture(Fragment fragment, Intent data) {
		this.fragment = fragment;
		Uri imgUri;
		if (data != null) {
			imgUri = data.getData();
		} else {
			imgUri = galleryAddPic();
		}
		return imgUri;
	}
	
	// Parcelable stuff
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(sCurrentPhotoPath);
	}
	
	private void readFromParce(Parcel in) {
		this.sCurrentPhotoPath = in.readString();
	}
	
	public static final Creator CREATOR = new Creator() {
        public PhotoManager createFromParcel(Parcel in) {
            return new PhotoManager(in);
        }

        public PhotoManager[] newArray(int size) {
            return new PhotoManager[size];
        }
    };
}
