package com.myhoard.app.fragments;

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
import android.view.LayoutInflater;
import android.view.Menu;
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

import java.io.ByteArrayOutputStream;

/**
 * Created by Dawid Graczyk on 2014-05-18.
 */
public class FacebookItemsToShare extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static boolean isShareRun = false;
    private static final int LOAD_ITEMS = 0;
    public static final String ITEM_ID = "itemId";
    private static final String[] PERMISSIONS = {"publish_actions"}; // Facebook
    private static final String ALBUM_NAME = "myHoard";
    private static final String CREATE_ALBUM = "/me/albums";
    private static final String SEND_PHOTO = "/me/photos";

    private Session.StatusCallback statusCallback = new SessionStatusCallback(); //Facebook

    private GridView mGridView;
    private FacebookImageAdapterList mFacebookImageAdapterList;
    private Context mContext;
    private long mElementId;
    int mCount;

    private TextView tvSelectedItems;
    private Button mButtonShare;
    private String mMessageOnFb;

    private NotificationCompat.Builder facebookNotification;
    private NotificationManager notificationManager;
    private static final int SHARE_ID = 0;
    private String mAlbumId;
    private Bitmap photoToSend;
    private int mElementToSend;
    private Request.Callback mCallbackPhoto;
    private String[] mPhotosPath;
    private ProgressDialog mProgressDialog;
    private String[] mTextOnNotification;
    public static final String POST_LOCATION = "location";
    private String mPostLocation;

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
        mPostLocation = bundle.getString(POST_LOCATION);
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
                if(box.isChecked()){
                    box.setChecked(false);
                    mCount--;
                    setCountOnView();
                    isSelected(position);

                }
                else {
                    box.setChecked(true);
                    mFacebookImageAdapterList.mSelectedItems.add(position);
                    mCount++;
                    setCountOnView();
                }

            }
        });
    }

    private void setOnClickButton() {
        mButtonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mFacebookImageAdapterList.mSelectedItems.size() > 0)
                    facebookShareDialog();
                else makeAndShowToast(getString(R.string.required_photo));
            }
        });
    }

    private void isSelected(int pos) {
        for(int i=0;i<mFacebookImageAdapterList.mSelectedItems.size();i++) {
            if(mFacebookImageAdapterList.mSelectedItems.get(i) == pos)
                mFacebookImageAdapterList.mSelectedItems.remove(i);
        }
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
            if(mFacebookImageAdapterList.mSelectedItems.size() == 1) shareOnFacebookSinglePhoto(session);
            else createAlbumOnFacebook(session, mMessageOnFb, ALBUM_NAME);
        }
    }

    private void prepareForShare() {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle(getString(R.string.please_wait));
        mProgressDialog.setMessage(getString(R.string.preparing_data));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        mTextOnNotification = new String[2];
        mTextOnNotification[0] = getString(R.string.sent_photo);
        mTextOnNotification[1] = getString(R.string.sharing_succeeded);
        buildNotification();
        setNumberOfPhotosToSend();
        setPhotosPathToSend();
        FacebookItemsToShare.isShareRun = true;

    }

    public void shareOnFacebookMultiPhotos(final Session session) {
        if (session != null && session.isOpened()) {
            mCallbackPhoto = new Request.Callback() {
                public void onCompleted(Response response) {
                    FacebookRequestError error = response.getError();
                    if (error != null) {
                        errorNotification(error.getErrorMessage());
                        mProgressDialog.dismiss();
                    } else {
                        recycleBitmap();
                        if (mElementToSend == -1) successNotification();
                        else {
                            Request request = sendPhotosToAlbum(mAlbumId, mPhotosPath[mElementToSend], session, mCallbackPhoto);
                            RequestAsyncTask task = new RequestAsyncTask(request);
                            updateNotification();
                            task.execute();
                        }
                    }
                }
            };
            RequestAsyncTask mFacebookTask = new RequestAsyncTask(
                    sendPhotosToAlbum(mAlbumId, mPhotosPath[mElementToSend], session, mCallbackPhoto)
            );
            mFacebookTask.execute();
            mProgressDialog.dismiss();
            updateNotification();
            makeAndShowToast(getString(R.string.sharing_in_progress));
            getFragmentManager().popBackStackImmediate();
        }
    }

    public void shareOnFacebookSinglePhoto(final Session session) {
        if (session != null && session.isOpened()) {
            prepareForShare();
            mCallbackPhoto = new Request.Callback() {
                public void onCompleted(Response response) {
                    FacebookRequestError error = response.getError();
                    if (error != null) {
                        errorNotification(error.getErrorMessage());
                        mProgressDialog.dismiss();
                    } else {
                        recycleBitmap();
                        successNotification();
                    }
                }
            };
            RequestAsyncTask mFacebookTask = new RequestAsyncTask(
                    sendPhoto(mPhotosPath[mElementToSend], session, mCallbackPhoto)
            );
            mFacebookTask.execute();
            mProgressDialog.dismiss();
            updateNotification();
            makeAndShowToast(getString(R.string.sharing_in_progress));
            getFragmentManager().popBackStackImmediate();
        }
    }

    private void recycleBitmap() {
        if(photoToSend != null) photoToSend.recycle();
    }
    private void setNumberOfPhotosToSend() {
        mElementToSend =mFacebookImageAdapterList.mSelectedItems.size() - 1;
    }

    private void setPhotosPathToSend() {
        mPhotosPath = new String[mFacebookImageAdapterList.mSelectedItems.size()];
        for(int i =0; i < mFacebookImageAdapterList.mSelectedItems.size(); i++) {
            mPhotosPath[i] = getPhotoPath(i);
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

     private void buildNotification() {
        Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(getActivity(), 0, intent, 0);
        facebookNotification = new NotificationCompat.Builder(getActivity().getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.sharing_succeeded))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .setAutoCancel(true);
        notificationManager =
                (NotificationManager) getActivity().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void updateNotification() {
        String text = String.format(mTextOnNotification[0],(mCount-mElementToSend),mCount);
        facebookNotification.setContentText(text);
        mElementToSend--;
        notificationManager.notify(SHARE_ID, facebookNotification.build());
    }

    private void errorNotification(String error) {
        FacebookItemsToShare.isShareRun = false;
        facebookNotification.setContentText(error);
        notificationManager.notify(SHARE_ID, facebookNotification.build());
    }

    private void successNotification() {
        FacebookItemsToShare.isShareRun = false;
        facebookNotification.setContentText(mTextOnNotification[1]);
        notificationManager.notify(SHARE_ID, facebookNotification.build());
    }

    private Request sendPhotosToAlbum(String album_id,String path,Session session,Request.Callback callback) {
        Bundle bundle = new Bundle();
        if(path != null) {
            bundle.putByteArray("source", prepareBitmapToSend(path));
            String publish = String.format("%s/photos",album_id);
            return new Request(session, publish, bundle, HttpMethod.POST,callback);
        } else return null;
    }

    private Request sendPhoto(String path,Session session,Request.Callback callback) {
        Bundle bundle = new Bundle();
        if(path != null) {
            bundle.putByteArray("source", prepareBitmapToSend(path));
            bundle.putString("message", setMessage(mMessageOnFb));
            return new Request(session, SEND_PHOTO, bundle, HttpMethod.POST,callback);
        } else return null;
    }

    private String getPhotoPath(int position) {
        Cursor cursor = mFacebookImageAdapterList.getCursor();
        cursor.moveToPosition(position);
        // getting data form cursor
        return cursor.getString(cursor.getColumnIndex(DataStorage.Media.FILE_NAME));
    }

    private byte[] prepareBitmapToSend(String path) {
        int photoSizeX = 800;
        int photoSizeY = 600;
        // Decoding image
        photoToSend = ImageLoader.decodeSampledBitmapFromResource(path, photoSizeX, photoSizeY);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        photoToSend.compress(Bitmap.CompressFormat.JPEG, 100, out);
        return out.toByteArray();
    }

    private void createAlbumOnFacebook(final Session session,String message,String albumName) {
        if (session != null && session.isOpened()) {
            prepareForShare();
            Bundle params = new Bundle();
            params.putString("name", albumName);
            params.putString("message", setMessage(message));
        /* make the API call */
            new Request(
                    session,
                    CREATE_ALBUM,
                    params,
                    HttpMethod.POST,
                    new Request.Callback() {
                        public void onCompleted(Response response) {
                            FacebookRequestError error = response.getError();
                            if (error != null) {
                                if (getActivity().getApplicationContext() != null) {
                                    errorNotification(error.getErrorMessage());
                                    mProgressDialog.dismiss();
                                }
                            }
                            mAlbumId = (String)response.getGraphObject().getProperty("id");
                            shareOnFacebookMultiPhotos(session);
                        }
                    }
            ).executeAsync();
        }
    }

    private String setMessage(String msg) {
        if((mPostLocation.compareTo(getString(R.string.gps_no_location)) != 0) &&
                mPostLocation.compareTo(getString(R.string.gps_finding_location))!=0) {
            msg = String.format("%s \n \n %s %s",msg,getString(R.string.location),mPostLocation);
            return msg;
        }
        return msg;
    }
}
