package com.myhoard.app.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.RadioButton;

import com.myhoard.app.R;

/**
 * Created by Rafał Soudani on 14.03.2014
 */
public class TypeDialog extends DialogFragment implements View.OnClickListener {
    RadioButton rbPrivate;
    RadioButton rbPublic;
    RadioButton rbOffline;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity(), R.style.TypeDialog);
        dialog.setContentView(R.layout.type_dialog);
        dialog.show();

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
                //TODO: sprawdzić czy użytkownik jest zalogowany
                NoAccountDialog noAccountDialog = new NoAccountDialog();
                noAccountDialog.show(getFragmentManager(), "");
                dismiss();
                break;
            case R.id.rbPrivate:
                //TODO: sprawdzić czy użytkownik jest zalogowany
                NoAccountDialog noAccountDialog2 = new NoAccountDialog();
                noAccountDialog2.show(getFragmentManager(), "");
                dismiss();
                break;
        }
    }
}
