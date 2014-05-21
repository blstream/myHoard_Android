package com.myhoard.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.myhoard.app.R;

/**
 * Created by Sebastian Peryt on 20.05.14.
 */
public class GPSInfoDialog extends DialogFragment {

    // Some random number to decrease the possibility of interfering with other activity results
    private static final int GPS_INTENT = 87;

    public GPSInfoDialog() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.gps_info_dialog_title);
        builder.setMessage(R.string.gps_info_dialog_message);
        builder.setPositiveButton(R.string.gps_info_dialog_ok,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_INTENT);
            }
        });
        builder.setNegativeButton(R.string.gps_info_dialog_cancel,null);
        return builder.create();
    }
}
