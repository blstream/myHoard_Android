package com.myhoard.app.fragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.myhoard.app.R;
import com.myhoard.app.activities.MainActivity;
import com.myhoard.app.dialogs.FacebookShareDialog;
import com.myhoard.app.images.FacebookImageAdapterList;
import com.myhoard.app.images.ImageLoader;
import com.myhoard.app.provider.DataStorage;

import java.util.ArrayList;

/**
 * Created by Dawid Graczyk on 2014-05-18.
 */
public class FacebookItemsToShare extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOAD_ITEMS = 0;
    public static final String ITEM_ID = "itemId";
    private static final String[] PERMISSIONS = {"publish_actions"}; // Facebook
    private static final String PUBLISH_PHOTOS = "me/photos";

    private Session.StatusCallback statusCallback = new SessionStatusCallback(); //Facebook
    private ProgressDialog mProgressDialog; //Facebook

    private GridView mGridView;
    private FacebookImageAdapterList mFacebookImageAdapterList;
    private Context mContext;
    private long mElementId;
    private ArrayList<Integer> mSelectedItems = new ArrayList<>();
    int mCount;

    private TextView tvSelectedItems;
    private Button mButtonShare;
    private String mMessageOnFb;

    private Notification facebookNotification;
    private NotificationManager notificationManager;
    private static final int SHARE_ID = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.fragment__facebook_items_list, container, false);
        mContext = getActivity();
        mFacebookImageAdapterList = new FacebookImageAdapterList(mContext,null,0);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGridView = (GridView)view.findViewById(R.id.gvItemsList);
        tvSelectedItems = (TextView)view.findViewById(R.id.tvItemsSelected);
        mButtonShare = (Button)view.findViewById(R.id.btShareOnFb);
        Bundle bundle = this.getArguments();
        mElementId = bundle.getLong(ITEM_ID);
        setOnClickActionOnGridView();
        setOnClickButton();

        // Lifecycle Facebook session
        Session session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(getActivity(), null, statusCallback, savedInstanceState);
            }
            if (session == null) {
                session = new Session(getActivity());

            }
            Session.setActiveSession(session);
        }

        getLoaderManager().initLoader(LOAD_ITEMS,null,this);
        bindData();
    }

    @Override
    public void onStart() {
        super.onStart();
        Session.getActiveSession().addCallback(statusCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
        Session.getActiveSession().removeCallback(statusCallback);
    }

    private void bindData() {
        mGridView.setAdapter(mFacebookImageAdapterList);
    }

    public void setOnClickActionOnGridView() {
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox box =(CheckBox) view.findViewById(R.id.chbItemToShare);
                if(box.isChecked()) box.setChecked(false);
                else box.setChecked(true);
                setSelectedItems(position);
            }
        });
    }

    private void setOnClickButton() {
        mButtonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookShareDialog();
            }
        });
    }

    private void setSelectedItems(int pos) {
        if(isSelected(pos)) {
            mCount--;
            setCountOnView();
        }
        else {
            mSelectedItems.add(pos);
            mCount++;
            setCountOnView();
        }
    }

    private boolean isSelected(long id) {
        for(int i : mSelectedItems) {
            if(id == i){
                mSelectedItems.remove(id);
                return true;
            }
        }
        return false;
    }

    private void setCountOnView() {
        String s = String.format("%s %d",getString(R.string.selected_items),mCount);
        tvSelectedItems.setText(s);
    }

    private void facebookShareDialog() {
         FacebookShareDialog facebookShareDialog = new FacebookShareDialog();
         facebookShareDialog.setTargetFragment(this,FacebookShareDialog.DIALOG_ID);
         facebookShareDialog.show(getFragmentManager(),null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(getActivity(), requestCode, resultCode, data);
        if (requestCode == FacebookShareDialog.DIALOG_ID) {
            mMessageOnFb = data.getStringExtra(FacebookShareDialog.GET_RESULT);
            openFbSessionForShare();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(LOAD_ITEMS,null,this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mFacebookImageAdapterList.mImageLoader.clearCache();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = null;
        String selection;
        if(id == LOAD_ITEMS) {
            selection = String.format("%s = %s",mElementId,DataStorage.Media.ID_ITEM);
            cursorLoader =  new CursorLoader(mContext, DataStorage.Media.CONTENT_URI,
                    new String[]{DataStorage.Media.FILE_NAME,
                            DataStorage.Media.TABLE_NAME + "." + DataStorage.Media._ID},
                    selection, null, null);
        }

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mFacebookImageAdapterList.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFacebookImageAdapterList.swapCursor(null);
    }

    private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            shareOnFacebook(session);
        }
    }

    public void shareOnFacebook(Session session) {
        if (session != null && session.isOpened()) {
            mProgressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.progress), true);
            buildNotification();
                Request.Callback callback = new Request.Callback() {
                    public void onCompleted(Response response) {

                        FacebookRequestError error = response.getError();
                        if (error != null) {
                            if (getActivity().getApplicationContext() != null) {
                                makeAndShowToast(error.getErrorMessage());
                            }
                        } else {
                            notificationManager.notify(SHARE_ID, facebookNotification);
                        }

                    }
                };


                /* AWA:FIXME: Niebezpieczne używanie wątku
        Brak anulowania tej operacji.
        Wyjście z Activity nie kończy wątku,
        należy o to zadbać.
        */
                RequestBatch batch = new RequestBatch();
                batch.add(prepareDataToShare(mMessageOnFb,0,session,callback));
                for(int pos=1;pos < mSelectedItems.size();pos++) {
                    batch.add(prepareDataToShare(null,pos,session,null));
                }
                RequestAsyncTask mFacebookTask = new RequestAsyncTask(batch);
                mFacebookTask.execute();
                mProgressDialog.dismiss();
                makeAndShowToast(getString(R.string.sharing_succeeded));
        }
    }

    public void makeAndShowToast(String message) {
        if(getActivity().getApplicationContext()!=null) {
            Toast.makeText(
                    getActivity().getApplicationContext(),
                    message,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /*
    Opening Facebook session for publish
     */
    private void openFbSessionForShare() {
        Session session = Session.getActiveSession();
        Session.OpenRequest request = new Session.OpenRequest(this).setCallback(statusCallback);
        request.setPermissions(PERMISSIONS);
        request.setLoginBehavior(SessionLoginBehavior.SUPPRESS_SSO);
        if (!session.isOpened() && !session.isClosed()) {
            session.openForPublish(request);
        } else if (session.getState().equals(SessionState.CLOSED_LOGIN_FAILED) || session.isClosed()) {
            session.close();

            session = new Session.Builder(getActivity()).build();
            session.addCallback(statusCallback);
            Session.setActiveSession(session);
            session.openForPublish(request);
        }
        else {
            Session.openActiveSession(getActivity(), this, true, statusCallback);
        }
    }
    /*
     Retrieving data that will be sharing on FB
     */
    private Request prepareDataToShare(String message,int position,Session session, Request.Callback callback) {

        int photoSizeX = 800;
        int photoSizeY = 600;
        Bundle bundle = new Bundle();

        Cursor cursor = mFacebookImageAdapterList.getCursor();
        cursor.moveToPosition(position);
        // getting data form cursor
        String data = cursor.getString(cursor.getColumnIndex(DataStorage.Media.FILE_NAME));
        if(data != null) {
            // Decoding image
            Bitmap image = ImageLoader.decodeSampledBitmapFromResource(data, photoSizeX, photoSizeY);
            bundle.putParcelable("source", image);
            bundle.putString("message", message);
            return new Request(session, PUBLISH_PHOTOS, bundle, HttpMethod.POST,callback);
        } else return null;

    }

    private void buildNotification() {
        Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(getActivity(), 0, intent, 0);
        facebookNotification = new NotificationCompat.Builder(getActivity().getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.sharing_succeeded))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .build();
        notificationManager =
                (NotificationManager) getActivity().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }
}
