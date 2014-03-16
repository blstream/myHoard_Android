package com.myhoard.app.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.myhoard.app.R;
import com.myhoard.app.gps.GPSProvider;
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

    //TODO Clean and refactor code

	public static final String ID = "elementId";
	public static final String NAME = "elementName";
	public static final String DESCRIPTION = "elementDescription";
	public static final String COLLECTION_ID = "elementCollectionId";
	public static final String CREATED_DATE = "elementCreatedDate";
	public static final String MODIFIED_DATE = "elementModifiedDate";
	public static final String TAGS = "elementTags";

	private static final String TAG = "ElementFragment";
	private static final boolean D = false;
	private static final int REQUEST_IMAGE_CAPTURE = 2;
	private static final int SELECT_PICTURE = 1;

    private TextView tvElementName, tvElementDescription, tvElementPosition;
    private EditText etElementName, etElementDescription;
    private String sCurrentPhotoPath;
    private String sImagePath;
    private int iCollectionId;
    private GridView gvImages;
    private Button btSave, btCancel, btAdd;
    private int elementId;
    private int modeOn;
    private Context context;

	private static final int MODE_ADD = 3;
	private static final int MODE_EDIT = 4;
	GPSProvider mService;
	boolean mBound = false;
	/*
		 * Część kodu odpowiedzialna za binder
		 * (http://developer.android.com/guide/components/bound-services.html)
		 */
	private ServiceConnection mConnection = new ServiceConnection() {

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
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updatePosition(intent);
			Bundle b = intent.getExtras();
			if (!b.getBoolean("GPS")) {
                tvElementPosition.setText("brak");
                tvElementPosition.setTextColor(Color.RED);
			}
		}
	};
	/*
	 * KONIEC - Część kodu odpowiedzialna za binder
	 */

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
        case R.id.addBtn:
            imagePicker();
            break;
		case R.id.saveBtn:
			// TODO in some production
			if (modeOn == MODE_EDIT) {
				etElementName.setVisibility(View.VISIBLE);
				etElementDescription.setVisibility(View.VISIBLE);
				tvElementName.setVisibility(View.INVISIBLE);
				tvElementDescription.setVisibility(View.INVISIBLE);
				etElementName.setText(tvElementName.getText().toString());
				etElementDescription.setText(tvElementDescription.getText().toString());
				//ivElementPhoto.setOnClickListener(this);
				btSave.setText(context.getResources().getString(R.string.save));
				modeOn = MODE_ADD;
				break;
			}

			if (tvElementName.getText() == null || TextUtils.isEmpty(etElementName.getText())) {
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
				values.put(DataStorage.Items.NAME, sName);
				values.put(DataStorage.Items.DESCRIPTION, sDescription);
                values.put(DataStorage.Items.ID_COLLECTION, iCollectionId);
				//values.put(DataStorage.Items.AVATAR_FILE_NAME, sImagePath);
				// TODO fix after explanation
				//values.put(DataStorage.Elements.TAGS);
				AsyncQueryHandler asyncHandler =
						new AsyncQueryHandler(getActivity().getContentResolver()) {};
				if (elementId != -1) {
                    values.put(DataStorage.Items.MODIFIED_DATE, getCurrentDate());
					asyncHandler.startUpdate(MODE_EDIT, null, DataStorage.Items.CONTENT_URI, values,
							DataStorage.Items._ID + " = " + elementId, null);
				} else {
					values.put(DataStorage.Items.CREATED_DATE, getCurrentDate());
					asyncHandler.startInsert(MODE_ADD, null, DataStorage
							.Items.CONTENT_URI, values);
				}
				getFragmentManager().popBackStackImmediate();
			}
			break;
		case R.id.cancelBtn:
			getFragmentManager().popBackStackImmediate();
			break;
        case R.id.positionTextView:
            //todo
            break;
		}
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
				}
		);

		AlertDialog choseDialog = pickerDialogBuilder.create();
		choseDialog.show();

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
				Log.e(TAG, "Error occurred while creating the File");
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
	 *
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			Bitmap bitmap = null;
			Uri imgUri = null;
			switch (requestCode) {
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
				Log.e(TAG, e.toString());
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(TAG, e.toString());
				e.printStackTrace();
			}
			//ivElementPhoto.setImageBitmap(bitmap);
		} else {
			// Response is wrong - visible only in debug mode
			if (D) Log.d(TAG, "Response != " + Activity.RESULT_OK);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.fragment_element, container, false);

		context = getActivity();

		tvElementName = (TextView) v.findViewById(R.id.nameTextView);
		tvElementDescription = (TextView) v.findViewById(R.id.descriptionTextView);
        tvElementPosition = (TextView) v.findViewById(R.id.positionTextView);
		etElementName = (EditText) v.findViewById(R.id.nameEditText);
		etElementDescription = (EditText) v.findViewById(R.id.descriptionEditText);
		//gvImages = (GridView) v.findViewById(R.id.gridview);

        btAdd = (Button) v.findViewById(R.id.addBtn);
        btAdd.setOnClickListener(this);

		btSave = (Button) v.findViewById(R.id.saveBtn);
		btCancel = (Button) v.findViewById(R.id.cancelBtn);
		btSave.setOnClickListener(this);
		btCancel.setOnClickListener(this);

        tvElementPosition.setOnClickListener(this);

        tvElementPosition.setText("ustalam");
        tvElementPosition.setTextColor(Color.YELLOW);

		elementId = -1;
        iCollectionId = 0;

		Bundle b = getArguments();
		if (b != null && (b.getInt(ElementFragment.ID)!=-1)) {
			etElementName.setVisibility(View.INVISIBLE);
			etElementDescription.setVisibility(View.INVISIBLE);
			tvElementName.setVisibility(View.VISIBLE);
			tvElementDescription.setVisibility(View.VISIBLE);
			tvElementName.setText(b.getString(ElementFragment.NAME));
			tvElementDescription.setText(b.getString(ElementFragment.DESCRIPTION));
			elementId = b.getInt(ElementFragment.ID);
            iCollectionId = (int)b.getLong(ElementFragment.COLLECTION_ID);
			modeOn = MODE_EDIT;
			btSave.setText(context.getResources().getString(R.string.edit));
		} else {
			btSave.setText(context.getResources().getString(R.string.save));
			//ivElementPhoto.setOnClickListener(this);
			modeOn = MODE_ADD;
		}


		return v;
	}

	/*
	     * Część kodu odpowiedzialna za GPS
	     */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getActivity().getApplicationContext()
				.bindService(new Intent(getActivity(),
								GPSProvider.class), mConnection,
						Context.BIND_AUTO_CREATE
				);
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
		getActivity().getApplicationContext().unbindService(mConnection);
		super.onDestroy();
	}

    private void updatePosition(Intent intent) {
        Log.e(TAG,"In");
        if(intent==null)
        {
            Log.e(TAG,"error");
            return;
        }
        Log.e(TAG,"OK");
        Bundle b = intent.getExtras();
        double lat = b.getDouble(GPSProvider.POS_LAT);
        double lon = b.getDouble(GPSProvider.POS_LON);

        tvElementPosition.setText(pos2str(lat,lon));
        tvElementPosition.setTextColor(Color.GREEN);
    }

    private String pos2str(double lat, double lon) {
        String pos = lat + ":" + lon;
        return pos;
    }
    /*
	 * KONIEC - Część kodu odpowiedzialna za GPS
	 */

	/**
	 * Method refreshes gallery view with added photo, so
	 * it can be seen by users in gallery.
	 *
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

    private int getCurrentDate() {
        Date d = new Date();
        CharSequence s  = DateFormat.format("dMMyyyy", d.getTime());
        return Integer.getInteger(s.toString());
    }
}
