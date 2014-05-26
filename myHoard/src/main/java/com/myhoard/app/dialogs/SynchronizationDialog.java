package com.myhoard.app.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

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
            cancelSynchronization.putExtra(SynchronizationService.CANCEL_COMMAND_KEY, "pusto");
            cancelSynchronization.putExtra("option", "pusto");
            getActivity().startService(cancelSynchronization);
            dialog.dismiss();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Intent askIfServiceEnded = new Intent(getActivity().getBaseContext(),SynchronizationService.class);
        askIfServiceEnded.putExtra(SynchronizationService.ASK_IF_SERVICE_ENDED, "pusto");
        askIfServiceEnded.putExtra("option", "pusto");
        getActivity().startService(askIfServiceEnded);
    }
}
