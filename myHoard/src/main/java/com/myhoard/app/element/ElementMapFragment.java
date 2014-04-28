package com.myhoard.app.element;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.myhoard.app.R;

/**
 * Created by Sebastian Peryt on 27.04.14.
 */
public class ElementMapFragment extends Fragment {

    private GoogleMap mMap;

    private static View view;
    private Button mapBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_map, container, false);
            mapBtn = (Button) view.findViewById(R.id.mapBtn);
            mapBtn.setOnClickListener(listener);
        } catch (InflateException e) {
	        /* map is already there, just return view as it is */
        }
        checkGooglePlayServices();

        return view;
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Toast.makeText(getActivity(),"Klik",Toast.LENGTH_SHORT).show();
        }
    };

    private void checkGooglePlayServices() {
        int status;
        status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (status != ConnectionResult.SUCCESS) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, getActivity(),
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
        SupportMapFragment smf = (SupportMapFragment)getFragmentManager().findFragmentById(R.id.map);
        mMap = smf.getMap();
        if (mMap == null) {
            return;
        }
        // Initialize map options.
        LatLng startPoz = new LatLng(52.348763, 18.9);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoz, 6));

        Marker start = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(52.348763, 18.9)));
        start.setDraggable(true);
        mMap.setOnMarkerDragListener(dragListener);
    }

    private GoogleMap.OnMarkerDragListener dragListener = new GoogleMap.OnMarkerDragListener() {
        @Override
        public void onMarkerDragStart(Marker marker) {

        }

        @Override
        public void onMarkerDrag(Marker marker) {

        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            LatLng pos = marker.getPosition();
            Toast.makeText(getActivity(),pos.latitude + " " + pos.longitude,Toast.LENGTH_SHORT).show();
        }
    };
}
