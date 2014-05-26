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
 * Used to choose insert image option
 */
public class ImageInsertDialog extends DialogFragment implements View.OnClickListener {

    private static final String INSERT_IMAGE_INTENT_EXTRA_TEXT = "insert image";
    private static final int INSERT_IMAGE_RESULT_CODE = 100;
    private static final int DEFAULT_INSERT_IMAGE_INTENT_EXTRA_VALUE = -1;
    private static final int INSERT_IMAGE_PHOTO_INTENT_EXTRA_VALUE = 1;
    private static final int INSERT_IMAGE_GALLERY_INTENT_EXTRA_VALUE = 2;
    RadioGroup rgImage;
    RadioButton rbPhoto;
    RadioButton rbGallery;
    TextView tvCancel;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity(), R.style.TypeDialog);
        dialog.setContentView(R.layout.image_insert_dialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        rgImage = (RadioGroup) dialog.findViewById(R.id.rgImage);
        rbPhoto = (RadioButton) dialog.findViewById(R.id.rbPhoto);
        rbGallery = (RadioButton) dialog.findViewById(R.id.rbGallery);
        tvCancel = (TextView) dialog.findViewById(R.id.tvCancelImage);

        rbPhoto.setOnClickListener(this);
        rbGallery.setOnClickListener(this);
        tvCancel.setOnClickListener(this);

        return dialog;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.rbPhoto:
                intent.putExtra(INSERT_IMAGE_INTENT_EXTRA_TEXT, INSERT_IMAGE_PHOTO_INTENT_EXTRA_VALUE);
                getTargetFragment().onActivityResult(getTargetRequestCode(), INSERT_IMAGE_RESULT_CODE, intent);
                dismiss();
                break;
            case R.id.rbGallery:
                intent.putExtra(INSERT_IMAGE_INTENT_EXTRA_TEXT, INSERT_IMAGE_GALLERY_INTENT_EXTRA_VALUE);
                getTargetFragment().onActivityResult(getTargetRequestCode(), INSERT_IMAGE_RESULT_CODE, intent);
                dismiss();
                break;
            case R.id.tvCancelImage:
                intent.putExtra(INSERT_IMAGE_INTENT_EXTRA_TEXT, DEFAULT_INSERT_IMAGE_INTENT_EXTRA_VALUE);
                getTargetFragment().onActivityResult(getTargetRequestCode(), INSERT_IMAGE_RESULT_CODE, intent);
                dismiss();
                break;
        }
    }
}
