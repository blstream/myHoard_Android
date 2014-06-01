package com.myhoard.app.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;

import com.myhoard.app.R;

/**
 * Created by Rafal Soudani on 2014-05-28.
 */
public class TypeConfirmDialog extends DialogFragment implements View.OnClickListener {

    private static final int TYPE_RESULT_CODE = 110;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity(), R.style.TypeDialog);
        dialog.setContentView(R.layout.confirm_type_dialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        Button accept = (Button) dialog.findViewById(R.id.bAcceptType);
        accept.setOnClickListener(this);
        Button cancel = (Button) dialog.findViewById(R.id.bCancelType);
        cancel.setOnClickListener(this);

        return dialog;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.bAcceptType:
                intent.putExtra("accepted", true);
                getTargetFragment().onActivityResult(getTargetRequestCode(), TYPE_RESULT_CODE, intent);
                dismiss();
                break;
            case R.id.bCancelType:
                dismiss();
                break;
        }
    }
}
