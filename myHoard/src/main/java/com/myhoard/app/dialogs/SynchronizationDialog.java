package com.myhoard.app.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.myhoard.app.R;
import com.myhoard.app.services.SynchronizationService;

/**
 * Description
 *
 * @author Tomasz Nosal
 *         Date: 2014-05-12
 */
public class SynchronizationDialog extends DialogFragment {

    private Dialog dialog;
    TextView stopSynchronization;
    private static final String SYNCHRONIZATION_STOPPED = "Synchronization stopped";
    private static final String INVALID_VALUE = "invalid";

    public SynchronizationDialog() {}

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
            Intent cancelSynchronization = new Intent(getActivity().getBaseContext(),SynchronizationService.class);
            cancelSynchronization.putExtra(SynchronizationService.CANCEL_COMMAND_KEY, INVALID_VALUE);
            cancelSynchronization.putExtra(SynchronizationService.OPTION_KEY, INVALID_VALUE);
            getActivity().startService(cancelSynchronization);
            dialog.dismiss();
            Toast.makeText(getActivity().getBaseContext(), SYNCHRONIZATION_STOPPED, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Intent askIfServiceEnded = new Intent(getActivity().getBaseContext(),SynchronizationService.class);
        askIfServiceEnded.putExtra(SynchronizationService.ASK_IF_SERVICE_ENDED, INVALID_VALUE);
        askIfServiceEnded.putExtra(SynchronizationService.OPTION_KEY, INVALID_VALUE);
        getActivity().startService(askIfServiceEnded);
    }
}
