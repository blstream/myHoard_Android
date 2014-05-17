package com.myhoard.app.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

import com.myhoard.app.R;

/**
 * Description
 *
 * @author Tomasz Nosal
 *         Date: 2014-05-12
 */
public class SynchronizationDialog extends DialogFragment {

    private Dialog dialog;
    TextView stopSynchronization;
    Intent synchronizationIntent;
    Context context;

    public SynchronizationDialog (Intent synchronizationIntent, Context context) {
        this.synchronizationIntent = synchronizationIntent;
        this.context = context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new Dialog(getActivity(), R.style.GeneratorDialog);
        dialog.setContentView(R.layout.synchronization_dialog);
        stopSynchronization = (TextView) dialog.findViewById(R.id.textViewStopSynchronize);
        stopSynchronization.setOnClickListener(onClickStop);
        return dialog;
    }

    private View.OnClickListener onClickStop = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            context.stopService(synchronizationIntent);
            dialog.dismiss();
        }
    };
}
