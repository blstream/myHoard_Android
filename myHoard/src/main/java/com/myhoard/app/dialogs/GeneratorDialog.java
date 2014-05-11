package com.myhoard.app.dialogs;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.myhoard.app.R;
import com.myhoard.app.provider.DataStorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

/**
 * Created by Piotr Brzozowski on 15.03.14.
 * Dialog use choose number of collections and elements to generate
 */
public class GeneratorDialog extends DialogFragment implements View.OnClickListener {

    private final static String[] ASSETS_LIST = new String[]{"flower1.jpg","flower2.jpg","beer1.jpg","snack1.jpg",
            "ball1.jpg","coins1.jpg","pizza1.jpg","pizza2.jpg","snack2.jpg"};
    private final static String SET_OF_CHARACTERS = "abcdefgijklmnouprstwuxyz";
    private final static int MAX_BYTE_TABLE_LENGTH = 2048;
    private final static int START_PROGRESS = 0;
    private final static int STOP_PROGRESS = 100;
    private EditText collectionNumber;
    private EditText elementNumber;
    private ProgressBar mProgressBar;
    private Button mGenerateButton;
    private String collection_id;
    private String element_id;
    private int mNumberOfCollection;
    private int mNumberOfElements;
    private Context mAppContext;
    private Uri mUriAddress;
    private AssetManager mAssetManager;
    private Dialog mDialog;
    private GenerateAsyncTask generateAsyncTask;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Create dialog to generate data
        mDialog = new Dialog(getActivity(),R.style.GeneratorDialog);
        mDialog.setContentView(R.layout.generator_dialog);
        //Set mContext
        mAppContext = getActivity().getApplicationContext();
        //Set EditText for dialog
        collectionNumber = (EditText)mDialog.findViewById(R.id.editTextGeneratorDialog);
        elementNumber = (EditText)mDialog.findViewById(R.id.editText2GeneratorDialog);
        mProgressBar = (ProgressBar)mDialog.findViewById(R.id.progressBarGeneratorDialog);
        //Set Button for dialog
        Button mCancelButton = (Button) mDialog.findViewById(R.id.buttonGeneratorDialog);
        mGenerateButton = (Button)mDialog.findViewById(R.id.button2GeneratorDialog);
        //Set interact with cancel and generate button
        mCancelButton.setOnClickListener(this);
        mGenerateButton.setOnClickListener(this);
        generateAsyncTask = new GenerateAsyncTask();
        return mDialog;
    }

    @Override
    public void onClick(View v) {
        //Get id of clicked button
        switch(v.getId()){
            case R.id.buttonGeneratorDialog:
                generateAsyncTask.cancel(true);
                break;
            case R.id.button2GeneratorDialog:
                generateAsyncTask.execute();
                break;
        }
    }
    
    public void getDataToGenerate() {
        mAssetManager = mAppContext.getAssets();
        if(collectionNumber.getText() != null){
            //Get text from Collection number label
            mNumberOfCollection = Integer.parseInt(collectionNumber.getText().toString());
            //Set ProgressBar scale
            mProgressBar.setMax(mNumberOfCollection);
        }
        if(elementNumber.getText() != null){
            //Get text from Element number label
            mNumberOfElements = Integer.parseInt(elementNumber.getText().toString());
        }
    }

    public String generateString(Random rng, String characters, int length)
    {
        char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            //Generate random string for element/collection name, description
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }

    public void createFile(AssetManager am,int random_number){
        InputStream is = null;
        OutputStream file_stream = null;
        try {
            is = am.open(ASSETS_LIST[random_number]);
        } catch (IOException e) {
                /* AWA:FIXME: Obsługa błędów
                Wypychanie błędów do UI
                Patrz:Ksiazka:Czysty kod:Rozdział 7:Obsługa błędów
                */
            e.printStackTrace();
        }
        //Create new file in mContext.getFilesDir() path
        File file = new File(mAppContext.getFilesDir(), ASSETS_LIST[random_number]);
        mUriAddress = Uri.fromFile(file);
        try {
            //New stream used to write data in file
            file_stream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        byte[] b = new byte[MAX_BYTE_TABLE_LENGTH];
        int length;
        try {
            if(is != null){
                while ((length = is.read(b)) != -1) {
                    if(file_stream != null){
                        //Write byte structure to file
                        file_stream.write(b, 0, length);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try{
                if(file_stream!=null) file_stream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void createCollections(AssetManager am){
        int random_number;
        Random r = new Random();
        //Generate collection and element of this collection
        ContentValues valuesCollection = new ContentValues();
        //Put value about collection to valuesCollection
        String mCollectionName;
        valuesCollection.put(DataStorage.Collections.NAME, mCollectionName = generateString(new Random(), SET_OF_CHARACTERS, 10));
        valuesCollection.put(DataStorage.Collections.DESCRIPTION, generateString(new Random(), SET_OF_CHARACTERS, 20));
        random_number = r.nextInt(ASSETS_LIST.length);
        //Get random image from assets folder
        createFile(am,random_number);
        if(mAppContext.getFilesDir() != null) {
            //Put avatar file path to contentValues object
            valuesCollection.put(DataStorage.Collections.AVATAR_FILE_NAME, mUriAddress.toString());
            //Insert data of collection to database
            mAppContext.getContentResolver().insert(DataStorage.Collections.CONTENT_URI, valuesCollection);
        }
        createElements(mCollectionName,am);
    }

    public void createElements(String mCollectionName,AssetManager am){
        int random_number;
        Random r;
        for (int j = 0; j < mNumberOfElements; j++) {
            ContentValues valuesElement = new ContentValues();
            //Get id of last collection added to database
            Cursor cursor = mAppContext.getContentResolver().query(DataStorage.Collections.CONTENT_URI,
                    new String[]{DataStorage.Collections._ID},
                    DataStorage.Collections.NAME+" = '"+ mCollectionName +"'",null,null);
            if(cursor!=null){
                cursor.moveToFirst();
                while(!cursor.isAfterLast()){
                    collection_id=cursor.getString(0);
                    cursor.moveToNext();
                }
            }
            String element_name;
            //Put values about element to valuesElement object
            valuesElement.put(DataStorage.Items.NAME, element_name = generateString(new Random(), SET_OF_CHARACTERS, 6));
            valuesElement.put(DataStorage.Items.DESCRIPTION, generateString(new Random(), SET_OF_CHARACTERS, 15));
            valuesElement.put(DataStorage.Items.ID_COLLECTION, collection_id);
            //Insert data of collection element to database
            mAppContext.getContentResolver().insert(DataStorage.Items.CONTENT_URI, valuesElement);
            //Get id of added element to database
            Cursor cursor1 = mAppContext.getContentResolver().query(DataStorage.Items.CONTENT_URI,
                    new String[]{DataStorage.Items.TABLE_NAME+"."+DataStorage.Items._ID,DataStorage.Media.AVATAR},
                    DataStorage.Items.NAME+" = '"+ element_name +"'",null,null);
            if(cursor1 != null){
                cursor1.moveToFirst();
                while(!cursor1.isAfterLast()){
                    element_id=cursor1.getString(0);
                    cursor1.moveToNext();
                }
            }
            r = new Random();
            //Get random image from assets folder
            random_number = r.nextInt(ASSETS_LIST.length);
            createFile(am,random_number);
            ContentValues valuesMedia = new ContentValues();
            //Put data of media to valuesMedia object
            valuesMedia.put(DataStorage.Media.ID_ITEM,element_id);
            if(mAppContext.getFilesDir() !=null){
                valuesMedia.put(DataStorage.Media.FILE_NAME,mUriAddress.toString());
                //Insert data of element media to database asynchronously
                mAppContext.getContentResolver().insert(DataStorage.Media.CONTENT_URI, valuesMedia);
            }
        }
    }

    private class GenerateAsyncTask extends AsyncTask<Void,Integer,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            getDataToGenerate();
            for (int i = 0;(i < mNumberOfCollection) && !isCancelled(); i++) {
                createCollections(mAssetManager);
                publishProgress(i+1);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mProgressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void s) {
            mProgressBar.setProgress(STOP_PROGRESS);
            mGenerateButton.setEnabled(true);
            mDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            mProgressBar.setProgress(START_PROGRESS);
            mGenerateButton.setEnabled(false);
        }

        @Override
        protected void onCancelled() {
            mDialog.dismiss();
        }
    }
}
