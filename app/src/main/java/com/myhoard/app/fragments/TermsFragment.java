package com.myhoard.app.fragments;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.myhoard.app.R;

/**
 * Created by Czyz on 24.02.14.
 */
public class TermsFragment extends DialogFragment {

    private TextView text ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {



        getDialog().setTitle("terms of use");
        View v = inflater.inflate(R.layout.terms_fragment_activity,container,false);
        text = (TextView)v.findViewById(R.id.text_term_fragment);

       return v;

    }
}
