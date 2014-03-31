package com.myhoard.app.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.Session;
import com.myhoard.app.R;
import com.myhoard.app.crudengine.ConnectionDetector;

/**
 * Created by Dawid Graczyk on 2014-03-29.
 */
public class FacebookShareDialog extends DialogFragment implements View.OnClickListener {

    public static final String GET_RESULT = "result";
    public static final int DIALOG_ID = 1;

    private Button mFacebookButton;
    private EditText mPostOnWall;
    private Dialog dialog;
    private String mPostOnFB;

    public FacebookShareDialog(String message) {
        mPostOnFB = message;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        dialog = new Dialog(getActivity(),R.style.Facebook_Dialog);
        dialog.setContentView(R.layout.share_on_fb_dialog);

        mFacebookButton = (Button)dialog.findViewById(R.id.facebook_button);
        Button mShareOnFbButton = (Button)dialog.findViewById(R.id.share_button);
        mPostOnWall = (EditText)dialog.findViewById(R.id.post_on_wall);

        if(isUserLogIn()) {
            mFacebookButton.setText(R.string.log_out_facebook);
            mFacebookButton.setVisibility(View.VISIBLE);
        }
        else mFacebookButton.setVisibility(View.INVISIBLE);

        mFacebookButton.setOnClickListener(this);
        mShareOnFbButton.setOnClickListener(this);
        mPostOnWall.setText(mPostOnFB);

        return dialog;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.facebook_button) {
            if(isUserLogIn()) {
                mFacebookButton.setVisibility(View.INVISIBLE);
                Session session = Session.getActiveSession();
                if (!session.isClosed()) session.closeAndClearTokenInformation();
            }
        }
        if(v.getId() == R.id.share_button) {
            ConnectionDetector detector = new ConnectionDetector(getActivity());
            if(detector.isConnectingToInternet()) {
                String message = null;
                if (mPostOnWall.getText() != null) message = mPostOnWall.getText().toString();
                backToTheItemsListFragment(message);
            }
            else {
                Toast.makeText(
                        getActivity(),
                        R.string.no_internet_connection,
                        Toast.LENGTH_SHORT
                ).show();
                dialog.dismiss();
            }
        }

    }

    private void backToTheItemsListFragment(String value) {
        Intent intent = new Intent();
        intent.putExtra(GET_RESULT,value);
        getTargetFragment().onActivityResult(getTargetRequestCode(),DIALOG_ID,intent);
        dialog.dismiss();
    }

    private boolean isUserLogIn() {
        Session session = Session.getActiveSession();
        return  (session != null && session.isOpened());
    }


}
