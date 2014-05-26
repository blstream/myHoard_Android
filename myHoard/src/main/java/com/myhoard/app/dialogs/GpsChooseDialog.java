package com.myhoard.app.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import com.myhoard.app.R;

/**
 * Created by Piotr Brzozowski on 25.05.2014
 * GPS choose dialog used to choose concrete location for element photo
 */
public class GpsChooseDialog extends DialogFragment implements View.OnClickListener {

    private static final String GPS_INTENT_EXTRA_TEXT = "gps_choose";
    private static final int GPS_RESULT_CODE = 300;
    private static final int DEFAULT_GPS_INTENT_EXTRA_VALUE = -1;
    private static final int CHOOSE_LOCATION_MANUALLY_GPS_INTENT_EXTRA_VALUE = 2;
    private static final int GET_LOCATION_FROM_GPS_INTENT_EXTRA_VALUE = 1;
    RadioButton rbGpsEnable;
    RadioButton rbGpsManually;
    TextView tvCancelGps;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity(), R.style.TypeDialog);
        dialog.setContentView(R.layout.gps_dialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        rbGpsEnable = (RadioButton) dialog.findViewById(R.id.rbGpsEnable);
        rbGpsManually = (RadioButton) dialog.findViewById(R.id.rbGpsManually);
        tvCancelGps = (TextView) dialog.findViewById(R.id.tvCancelGps);

        rbGpsEnable.setOnClickListener(this);
        rbGpsManually.setOnClickListener(this);
        tvCancelGps.setOnClickListener(this);

        return dialog;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.rbGpsEnable:
                intent.putExtra(GPS_INTENT_EXTRA_TEXT,GET_LOCATION_FROM_GPS_INTENT_EXTRA_VALUE);
                getTargetFragment().onActivityResult(getTargetRequestCode(), GPS_RESULT_CODE, intent);
                dismiss();
                break;
            case R.id.rbGpsManually:
                intent.putExtra(GPS_INTENT_EXTRA_TEXT,CHOOSE_LOCATION_MANUALLY_GPS_INTENT_EXTRA_VALUE);
                getTargetFragment().onActivityResult(getTargetRequestCode(), GPS_RESULT_CODE, intent);
                dismiss();
                break;
            case R.id.tvCancelGps:
                intent.putExtra(GPS_INTENT_EXTRA_TEXT,DEFAULT_GPS_INTENT_EXTRA_VALUE);
                getTargetFragment().onActivityResult(getTargetRequestCode(), GPS_RESULT_CODE, intent);
                dismiss();
                break;
        }
    }
}
