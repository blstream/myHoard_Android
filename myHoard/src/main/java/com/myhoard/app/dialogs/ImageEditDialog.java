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
public class ImageEditDialog extends DialogFragment implements View.OnClickListener {

    private static final int EDIT_IMAGE_RESULT_CODE = 101;
    RadioGroup rgImage;
    RadioButton rbPhoto;
    RadioButton rbGallery;
    RadioButton rbDelete;
    TextView tvCancel;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity(), R.style.TypeDialog);
        dialog.setContentView(R.layout.image_insert_edit_dialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        rgImage = (RadioGroup) dialog.findViewById(R.id.rgImage);
        rbPhoto = (RadioButton) dialog.findViewById(R.id.rbPhoto);
        rbGallery = (RadioButton) dialog.findViewById(R.id.rbGallery);
        rbDelete = (RadioButton) dialog.findViewById(R.id.rbDelete);
        tvCancel = (TextView) dialog.findViewById(R.id.tvCancelImage);

        rbPhoto.setOnClickListener(this);
        rbGallery.setOnClickListener(this);
        rbDelete.setOnClickListener(this);
        tvCancel.setOnClickListener(this);

        return dialog;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.rbPhoto:
                intent.putExtra("insert image", 0);
                getTargetFragment().onActivityResult(getTargetRequestCode(), EDIT_IMAGE_RESULT_CODE, intent);
                dismiss();
                break;
            case R.id.rbGallery:
                intent.putExtra("insert image", 1);
                getTargetFragment().onActivityResult(getTargetRequestCode(), EDIT_IMAGE_RESULT_CODE, intent);
                dismiss();
                break;
            case R.id.rbDelete:
                intent.putExtra("insert image", 2);
                getTargetFragment().onActivityResult(getTargetRequestCode(), EDIT_IMAGE_RESULT_CODE, intent);
                dismiss();
                break;
            case R.id.tvCancelImage:
                intent.putExtra("insert image", -1);
                getTargetFragment().onActivityResult(getTargetRequestCode(), EDIT_IMAGE_RESULT_CODE, intent);
                dismiss();
                break;
        }
    }
}
