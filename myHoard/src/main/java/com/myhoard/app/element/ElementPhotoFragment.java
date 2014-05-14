package com.myhoard.app.element;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myhoard.app.R;
import com.myhoard.app.images.ImageLoader;
import com.myhoard.app.views.ScaleImageView;

/**
 * Created by Sebastian Peryt on 27.04.14.
 */
public class ElementPhotoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_element_photo, container,
                false);

        Bundle extra = getArguments();
        Uri imgUri = Uri.parse(extra.getString("uri"));
        ScaleImageView img = (ScaleImageView)v.findViewById(R.id.image);
        Bitmap bmp = ImageLoader.decodeSampledBitmapFromResource(imgUri.toString(), 100, 100);
        //img.setImageURI(imgUri);
        img.setImageBitmap(bmp);

        return v;
    }
}
