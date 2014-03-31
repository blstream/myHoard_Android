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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.myhoard.app.R;
import com.myhoard.app.gps.GPSProvider;
import com.myhoard.app.provider.DataStorage;
import com.myhoard.app.views.ScaleImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/*
 * Created by Sebastian Peryt on 27.02.14.
 */
public class ElementFragment extends Fragment implements View.OnClickListener {

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

    /* AWA:FIXME: Niepotrzebne prefiksy określające typ
Patrz:Ksiazka:Czysty kod:Rozdział 2:Nazwy klas, metod….
*/
    private TextView tvElementName, tvElementDescription, tvElementPosition, tvElementCategory;
    private EditText etElementName, etElementDescription;
    private String sCurrentPhotoPath;
    private String sImagePath;
    private int iCollectionId;
    private int elementId;
    private Context context;
    private ScaleImageView ivElementPhoto;

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
             /* AWA:FIXME: Hardcoded value
                    Umiesc w private final static String, int, etc....
                    lub w strings.xml
                    lub innym *.xml
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_element, container, false);
        setHasOptionsMenu(true);

        context = getActivity();

        tvElementName = (TextView) v.findViewById(R.id.tvElementName);
        tvElementDescription = (TextView) v.findViewById(R.id.tvElementDescription);
        tvElementPosition = (TextView) v.findViewById(R.id.tvElementLocalisation);
        etElementName = (EditText) v.findViewById(R.id.etElementName);
        etElementDescription = (EditText) v.findViewById(R.id.etElementDescription);
        ivElementPhoto = (ScaleImageView) v.findViewById(R.id.ivElementPhoto);
        tvElementCategory = (TextView) v.findViewById(R.id.tvElementCategory);

        tvElementPosition.setOnClickListener(this);

        tvElementPosition.setText("ustalam");
        tvElementPosition.setTextColor(Color.YELLOW);

        tvElementCategory.setText("brak");//TODO add dialog to chose category from
        tvElementCategory.setOnClickListener(this);

        elementId = -1;
        iCollectionId = 0;

        Bundle b = getArguments();
        if (b != null) {
            iCollectionId = (int) b.getLong(ElementFragment.COLLECTION_ID);
            if (b.getInt(ElementFragment.ID) != -1) {
                etElementName.setVisibility(View.GONE);
                etElementDescription.setVisibility(View.GONE);
                tvElementName.setVisibility(View.VISIBLE);
                tvElementDescription.setVisibility(View.VISIBLE);
                tvElementName.setText(b.getString(ElementFragment.NAME));
                tvElementDescription.setText(b.getString(ElementFragment.DESCRIPTION));
                elementId = b.getInt(ElementFragment.ID);
            }
        }
        ivElementPhoto.setOnClickListener(this);

        return v;
    }

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
        case R.id.ivElementPhoto:
            imagePicker();
            break;
        case R.id.tvElementLocalisation:
            if(String.valueOf(tvElementPosition.getText()).equals("brak")) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
            break;
            case R.id.tvElementCategory:
                //TODO do
                Toast.makeText(context,"In development",Toast.LENGTH_SHORT).show();
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

/* AWA:FIXME: Obsługa błędów
Wypychanie błędów do UI
*/
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
        /* AWA:FIXME: Hardcoded value
                    Umiesc w private final static String, int, etc....
                    lub w strings.xml
                    lub innym *.xml
                    */

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
			Uri imgUri;
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
                /* AWA:FIXME: Obsługa błędów
                Wypychanie błędów do UI
                */
				Log.e(TAG, "FileNotFoundException error: " + e.toString());
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(TAG, "IOException error: " + e.toString());
				e.printStackTrace();
			}
			ivElementPhoto.setImageBitmap(bitmap);
		} else {
			// Response is wrong - visible only in debug mode
			if (D) Log.d(TAG, "Response != " + Activity.RESULT_OK);
		}
	}

	/*
	* Część kodu odpowiedzialna za GPS
	*/
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getActivity().getBaseContext()
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
		getActivity().getBaseContext().unbindService(mConnection);
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
        return lat + ":" + lon;
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.new_collection, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_accept:
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
                    Log.d("TAG","collection id: " + iCollectionId);
                    AsyncQueryHandler asyncHandler =
                            new AsyncQueryHandler(getActivity().getContentResolver()) {};
                    if (elementId != -1) {
                        values.put(DataStorage.Items.MODIFIED_DATE, Calendar.getInstance()
                                .getTime().getTime());
                        asyncHandler.startUpdate(0, null, DataStorage.Items.CONTENT_URI, values,
                                DataStorage.Items._ID + " = " + elementId, null);
                    } else {
                        values.put(DataStorage.Items.CREATED_DATE, Calendar.getInstance()
                                .getTime().getTime());
                        asyncHandler.startInsert(0, null, DataStorage
                                .Items.CONTENT_URI, values);
                    }
                    getFragmentManager().popBackStackImmediate();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
