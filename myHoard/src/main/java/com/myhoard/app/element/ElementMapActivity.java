package com.myhoard.app.element;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.myhoard.app.R;

/**
 * Created by Sebastian Peryt on 27.04.14.
 */
public class ElementMapActivity extends ActionBarActivity {

    private GoogleMap mMap;
    private LatLng localisation;
    private boolean isMarkerSet = false;
    private Marker start;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_map);

        localisation = getIntent().getParcelableExtra("localisation");

        checkGooglePlayServices();
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
                Intent out = new Intent();
                if(start!=null) {
                    out.putExtra("localisation", start.getPosition());
                }
                setResult(Activity.RESULT_OK,out);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkGooglePlayServices() {
        int status;
        status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,
                    69);
            dialog.show();
        } else {
            setUpMapIfNeeded();
        }
    }

    private void setUpMapIfNeeded() {
        if (mMap != null) {
            return;
        }
        mMap = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMap();
        if (mMap == null) {
            return;
        }
        // Initialize map options.
        LatLng startPoz = new LatLng(52.348763, 18.9);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoz, 6));
        mMap.setOnMapClickListener(mapClickListener);

        if(localisation != null) {
            setMarker(localisation);
        }
    }

    private void setMarker(LatLng latlng) {
            start = mMap.addMarker(new MarkerOptions()
                    .position(latlng));
            start.setDraggable(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            isMarkerSet = true;
    }

    private GoogleMap.OnMapClickListener mapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            if(!isMarkerSet) {
                setMarker(latLng);
            } else {
                start.setPosition(latLng);
            }
        }
    };
}
