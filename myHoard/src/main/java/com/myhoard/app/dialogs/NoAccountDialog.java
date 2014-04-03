package com.myhoard.app.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;

import com.myhoard.app.R;
import com.myhoard.app.activities.RegisterActivity;

/**
 * Created by Rafał Soudani on 03.04.2014
 */
public class NoAccountDialog extends DialogFragment implements View.OnClickListener {
    private Button bSignUp, bContinueOffline;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity(), R.style.TypeDialog);
        dialog.setContentView(R.layout.no_account_dialog);
        dialog.show();

        bSignUp = (Button) dialog.findViewById(R.id.bSignUp);
        bContinueOffline = (Button) dialog.findViewById(R.id.bContinueOffline);

        bSignUp.setOnClickListener(this);
        bContinueOffline.setOnClickListener(this);


        return dialog;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bSignUp:
                startActivity(new Intent(getActivity(),RegisterActivity.class));
                dismiss();
                break;
            case R.id.bContinueOffline:
                dismiss();
                break;
        }
    }
}
