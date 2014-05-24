package com.myhoard.app.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.myhoard.app.R;

/**
 * Created by Piotr Brzozowski on 24.05.2014
 */
public class ImageDeleteDialog extends DialogFragment implements View.OnClickListener {

    private static final int DELETE_IMAGE_RESULT_CODE = 102;
    RadioGroup rgImage;
    RadioButton rbDelete;
    TextView tvCancel;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity(), R.style.TypeDialog);
        dialog.setContentView(R.layout.image_delete_dialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        rgImage = (RadioGroup) dialog.findViewById(R.id.rgImage);
        rbDelete = (RadioButton) dialog.findViewById(R.id.rbDelete);
        tvCancel = (TextView) dialog.findViewById(R.id.tvCancelImage);

        rbDelete.setOnClickListener(this);
        tvCancel.setOnClickListener(this);

        return dialog;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.rbDelete:
                intent.putExtra("insert image", 1);
                getTargetFragment().onActivityResult(getTargetRequestCode(), DELETE_IMAGE_RESULT_CODE, intent);
                dismiss();
                break;
            case R.id.tvCancelImage:
                intent.putExtra("insert image", -1);
                getTargetFragment().onActivityResult(getTargetRequestCode(), DELETE_IMAGE_RESULT_CODE, intent);
                dismiss();
                break;
        }
    }
}
