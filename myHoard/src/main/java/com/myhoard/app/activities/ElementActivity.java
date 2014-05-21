package com.myhoard.app.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.myhoard.app.R;
import com.myhoard.app.dialogs.GPSInfoDialog;
import com.myhoard.app.element.ElementReadFragment;
import com.myhoard.app.element.ElementAddEditFragment;
import com.myhoard.app.gps.GPSProvider;
import com.myhoard.app.model.Item;
import com.myhoard.app.model.ItemLocation;
import com.myhoard.app.provider.DataStorage;

/**
 * Created by Sebastian Peryt on 27.04.14.
 */

/**
 * FIXME: AWA: CLEAN CODE
 *
 * README
 * Ta klasa jak i wszystkie w tym pakiecie sa jeszcze przebudowywane, dlatego sa tam rózne hard coded wartości.
 * Zostanie to niedlugo naprawione.
 */
public class ElementActivity extends ActionBarActivity {

    private Item element;
    private long elementId;
    private long categoryId = -1;
    private AsyncElementRead asyncElementRead;
    private Intent intent;
    private GPSInfoDialog gpsInfoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_element);

        // API 14+ hack
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        intent = new Intent(this, GPSProvider.class);

        bindService(
                intent, mConnection,
                Context.BIND_AUTO_CREATE);

        asyncElementRead = new AsyncElementRead();

        if(getIntent().hasExtra("categoryId")) {
            categoryId = getIntent().getLongExtra("categoryId", 0);
            displayFragment(1);
        } else if(getIntent().hasExtra("elementId")) {
            elementId = getIntent().getLongExtra("elementId", 0);
            asyncElementRead.execute(elementId);
            displayFragment(0);
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_collection, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_accept:
                displayFragment(1);
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayFragment(int position) {
        Fragment fragment = null;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        Bundle bundle = new Bundle();
        switch (position) {
            case 0:
                fragment = fragmentManager.findFragmentByTag(String
                        .valueOf(position));
                if (fragment == null) {
                    fragment = new ElementReadFragment();
                    bundle.putLong("elementId", elementId);
                    fragment.setArguments(bundle);
                }
                break;
            case 1:
                fragment = fragmentManager.findFragmentByTag(String
                        .valueOf(position));
                if (fragment == null) {
                    fragment = new ElementAddEditFragment();
                    bundle.putParcelable("location",location);
                    if(categoryId == -1){
                        bundle.putParcelable("element",element);
                    } else {
                        bundle.putLong("categoryId", categoryId);
                    }
                    fragment.setArguments(bundle);
                }
                break;
            default:
                break;
        }

        if (fragment != null) {
            fragmentTransaction.replace(R.id.frame_container, fragment,
                    String.valueOf(position));
            fragmentTransaction.commit();
        }
    }

    private class AsyncElementRead extends AsyncTask<Long, Integer, Item> {

        @Override
        protected Item doInBackground(Long... params) {
            String[] projection = {DataStorage.Items.NAME,
                    DataStorage.Items.DESCRIPTION,
                    DataStorage.Items.ID_COLLECTION,
                    DataStorage.Items.LOCATION,
                    DataStorage.Items.LOCATION_LAT,
                    DataStorage.Items.LOCATION_LNG};
            String[] selection = {String.valueOf(elementId)};
            Cursor cursorItems = getContentResolver().query(DataStorage.Items.CONTENT_URI, projection, DataStorage.Items.TABLE_NAME + "." + DataStorage.Items._ID + " =? ", selection, DataStorage.Items.TABLE_NAME + "." + DataStorage.Items._ID + " DESC");

            cursorItems.moveToFirst();
            Item element = new Item();
            String name = cursorItems.getString(cursorItems.getColumnIndex(DataStorage.Items.NAME));
            String description = cursorItems.getString(cursorItems.getColumnIndex(DataStorage.Items.DESCRIPTION));
            int collection = cursorItems.getInt(cursorItems.getColumnIndex(DataStorage.Items.ID_COLLECTION));
            String locationTxt = cursorItems.getString(cursorItems.getColumnIndex(DataStorage.Items.LOCATION));
            float lat = cursorItems.getFloat(cursorItems.getColumnIndex(DataStorage.Items.LOCATION_LAT));
            float lon = cursorItems.getFloat(cursorItems.getColumnIndex(DataStorage.Items.LOCATION_LNG));
            element.setId(String.valueOf(elementId));
            element.setCollection(String.valueOf(collection));
            element.setName(name);
            element.setDescription(description);
            element.setLocationTxt(locationTxt);
            element.setLocation(new ItemLocation(lat,lon));
            return element;
        }

        @Override
        protected void onPostExecute(Item item) {
            element = item;
        }
    }

    /*
     * Część kodu odpowiedzialna za GPS
     */

    GPSProvider mService;
    boolean mBound = false;
    private LatLng location;
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
            Bundle bundle = intent.getExtras();
            boolean gpsEnabled = bundle.getBoolean("gps");

            ElementAddEditFragment elementAdd = (ElementAddEditFragment) getSupportFragmentManager().findFragmentByTag(String.valueOf(1));
            if(elementAdd != null) {
                elementAdd.gpsEnabled(gpsEnabled);
            }

            if(gpsInfoDialog == null) {
                gpsInfoDialog = new GPSInfoDialog();
            }
            if(gpsEnabled) {
                if(isDialogVisible(gpsInfoDialog)){
                    gpsInfoDialog.dismiss();
                }
                updatePosition(intent);
            } else {
                if(!isDialogVisible(gpsInfoDialog)) {
                    gpsInfoDialog.show(getSupportFragmentManager(), "gps_dialog");
                }
            }
        }
    };

    private boolean isDialogVisible(GPSInfoDialog dialog){
        if(dialog != null) {
            return dialog.getDialog() != null;
        }
        return false;
    }

	/*
	 * KONIEC - Część kodu odpowiedzialna za binder
	 */

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            try {
                unbindService(mConnection);
                Log.d("TAG", "unbind ok");
            } catch (Exception e) {
                Log.d("TAG", "nie unbind");
            }
            mBound = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBound) {
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(
                GPSProvider.BROADCAST_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onDestroy() {
        try{
            unbindService(mConnection);
        } catch (IllegalArgumentException iae) {
            // TODO
        }
        super.onDestroy();
    }

    private void updatePosition(Intent intent) {
        Log.e("TAG", "In");
        if (intent == null) {
            Log.e("TAG", "error");
            return;
        }
        Log.e("TAG", "OK");
        Bundle b = intent.getExtras();
        double lat = b.getDouble(GPSProvider.POS_LAT);
        double lon = b.getDouble(GPSProvider.POS_LON);

        location = new LatLng(lat, lon);

        ElementAddEditFragment elementAdd = (ElementAddEditFragment) getSupportFragmentManager().findFragmentByTag(String.valueOf(1));
        if(elementAdd != null) {
            elementAdd.putLocation(location);
        }
    }
	/*
	 * KONIEC - Część kodu odpowiedzialna za GPS
	 */
}
