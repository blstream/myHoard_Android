package com.myhoard.app.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.myhoard.app.Managers.UserManager;
import com.myhoard.app.R;

/**
 * Created by Rafał Soudani on 14.03.2014
 */
public class TypeDialog extends DialogFragment implements View.OnClickListener {

    private static final int TYPE_RESULT_CODE = 100;
    RadioButton rbPrivate;
    RadioButton rbPublic;
    RadioButton rbOffline;
    RadioGroup rgType;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity(), R.style.TypeDialog);
        dialog.setContentView(R.layout.type_dialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        rgType = (RadioGroup) dialog.findViewById(R.id.rgType);
        rbOffline = (RadioButton) dialog.findViewById(R.id.rbOffline);
        rbPrivate = (RadioButton) dialog.findViewById(R.id.rbPrivate);
        rbPublic = (RadioButton) dialog.findViewById(R.id.rbPublic);

        checkCorrectType();

        rbOffline.setOnClickListener(this);
        rbPrivate.setOnClickListener(this);
        rbPublic.setOnClickListener(this);

        return dialog;
    }

    private void checkCorrectType() {
        String typeName = getArguments().getString("type");
        if (typeName != null) {
            if (typeName.equals(getString(R.string.privates))) {
                rgType.check(R.id.rbPrivate);
            } else if (typeName.equals(getString(R.string.publics))) {
                rgType.check(R.id.rbPublic);
            } else {
                rgType.check(R.id.rbOffline);
            }
        }
    }


    @Override
    public void onClick(View view) {
        UserManager uM = UserManager.getInstance();
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.rbOffline:
                intent.putExtra("type", getString(R.string.offline));
                getTargetFragment().onActivityResult(getTargetRequestCode(), TYPE_RESULT_CODE, intent);
                dismiss();
                break;
            case R.id.rbPublic:
                if (uM.isLoggedIn()) {
                    intent.putExtra("type", getString(R.string.publics));
                    getTargetFragment().onActivityResult(getTargetRequestCode(), TYPE_RESULT_CODE, intent);
                } else {
                    NoAccountDialog noAccountDialog = new NoAccountDialog();
                    noAccountDialog.show(getFragmentManager(), "");
                }
                dismiss();
                break;
            case R.id.rbPrivate:
                if (uM.isLoggedIn()) {
                    intent.putExtra("type", getString(R.string.privates));
                    getTargetFragment().onActivityResult(getTargetRequestCode(), TYPE_RESULT_CODE, intent);
                } else {
                    NoAccountDialog noAccountDialog = new NoAccountDialog();
                    noAccountDialog.show(getFragmentManager(), "");
                }
                dismiss();
                break;
        }
    }
}
