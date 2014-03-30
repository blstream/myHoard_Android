package com.myhoard.app.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.myhoard.app.R;

/**
 * Created by Rafał Soudani on 14.03.2014
 */
public class TypeDialog extends DialogFragment implements View.OnClickListener {
    private RadioGroup rgType;
    private RadioButton rbPrivate;
    private RadioButton rbPublic;
    private RadioButton rbOffline;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity());
        /* AWA:FIXME:Uzycie styli zamiast ustawiania w kodzie wygladu Dialogu
        Proponuję sprawdzić czy nie da się tego ustawić w pliku stylu
        i ewentualnei w kodzie zroibć tylko ustawianie tego stylu
        Np.
        http://stackoverflow.com/questions/19167185/cant-get-my-dialogfragment-background-to-be-transparent
        */
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.setContentView(R.layout.type_dialog);
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        rgType = (RadioGroup) dialog.findViewById(R.id.rgType);

        rbOffline = (RadioButton) dialog.findViewById(R.id.rbOffline);
        rbPrivate = (RadioButton) dialog.findViewById(R.id.rbPrivate);
        rbPublic = (RadioButton) dialog.findViewById(R.id.rbPublic);

        rbOffline.setOnClickListener(this);
        rbPrivate.setOnClickListener(this);
        rbPublic.setOnClickListener(this);

        return dialog;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rbOffline:
                //TODO: Collection type offline
                dismiss();
                break;
            case R.id.rbPublic:
                //TODO: Collection type Public
                //dismiss();
                break;
            case R.id.rbPrivate:
                //TODO: Collection type private
                //dismiss();
                break;
        }
    }
}
