package com.myhoard.app.element;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.myhoard.app.R;
import com.myhoard.app.images.MapImageLoader;
import com.myhoard.app.model.StaticMap;
import com.myhoard.app.views.ScaleImageView;

/**
 * Created by Sebastian Peryt on 27.04.14.
 */
public class ElementStaticMapFragment extends Fragment {

    private MapImageLoader mapLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mapLoader = new MapImageLoader(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_element_photo, container,
                false);

        ScaleImageView img = (ScaleImageView) v.findViewById(R.id.image);
        LatLng position = getArguments().getParcelable("position");
        if(position != null) {
            double lati = position.latitude;
            double longi = position.longitude;
            String baseUrl = "http://maps.google.com/maps/api/staticmap?center=";
            String settings = "&zoom=16&size=400x400&";
            String marker = "markers=color:yellow|";
            String sensor = "&sensor=false";
            String latlng = lati + "," + longi;
            StringBuilder builder = new StringBuilder();
            builder.append(baseUrl);
            builder.append(latlng);
            builder.append(settings);
            builder.append(marker);
            builder.append(latlng);
            builder.append(sensor);
            // TODO display error about lack of Internet connection
            mapLoader.DisplayImage(builder.toString(), img);
        } else {
            img.setImageResource(R.drawable.nophoto);
        }
        return v;
    }
}
