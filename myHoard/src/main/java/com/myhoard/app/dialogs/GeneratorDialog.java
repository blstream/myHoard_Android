package com.myhoard.app.dialogs;

import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    private EditText collectionNumber;
    private EditText elementNumber;
    private String collection_id;
    private String element_id;
    private Context mContext;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Create dialog to generate data
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.generator_dialog);
        //Set mContext
        mContext = getActivity().getApplicationContext();
        //Set EditText for dialog
        collectionNumber = (EditText)dialog.findViewById(R.id.editTextGeneratorDialog);
        elementNumber = (EditText)dialog.findViewById(R.id.editText2GeneratorDialog);
        //Set Button for dialog
        Button cancelButton = (Button) dialog.findViewById(R.id.buttonGeneratorDialog);
        Button generateButton = (Button) dialog.findViewById(R.id.button2GeneratorDialog);
        //Set interact with cancel and generate button
        cancelButton.setOnClickListener(this);
        generateButton.setOnClickListener(this);
        return dialog;
    }

    @Override
    public void onClick(View v) {
        //Get id of clicked button
        switch(v.getId()){
            case R.id.buttonGeneratorDialog:
                //Cancel button
                dismiss();
                break;
            case R.id.button2GeneratorDialog:
                //Generate button
                generateData();
                dismiss();
                break;
        }
    }

    public void generateData() {
        assert collectionNumber.getText() != null;
        //Get text from Collection number label
        int collection_number = Integer.parseInt(collectionNumber.getText().toString());
        assert elementNumber.getText() != null;
        //Get text from Element number label
        int element_number = Integer.parseInt(elementNumber.getText().toString());
        InputStream is = null;
        //List of jpg images in assets folder
        String[] list = new String[]{"flower1.jpg","flower2.jpg","beer1.jpg","snack1.jpg",
                "ball1.jpg","coins1.jpg","pizza1.jpg","pizza2.jpg","snack2.jpg"};
        OutputStream file_stream = null;
        //asyncHandler use to do insert, asynchronous
        AsyncQueryHandler asyncHandler =
                new AsyncQueryHandler(getActivity().getContentResolver()) {
                };
        //Generate collection and element of this collection
        for (int i = 0; i < collection_number; i++) {
            ContentValues valuesCollection = new ContentValues();
            String collection_name;
            //Put value about collection to valuesCollection
            valuesCollection.put(DataStorage.Collections.NAME, collection_name = generateString(new Random(), "abcdefgijklmnouprstwuxyz", 10));
            valuesCollection.put(DataStorage.Collections.DESCRIPTION, generateString(new Random(), "abcdefgijklmnouprstwuxyz", 20));
            //AssetManager used to get data from image in assets folder
            AssetManager am = getActivity().getAssets();
            Random r = new Random();
            int random_number = r.nextInt(list.length);
            //Get random image from assets folder
            try {
                is = am.open(list[random_number]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Create new file in mContext.getFilesDir() path
            File file = new File(mContext.getFilesDir(), list[random_number]);
            try {
                //New stream used to write data in file
                file_stream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            byte[] b = new byte[2048];
            int length;
            try {
                assert is != null;
                while ((length = is.read(b)) != -1) {
                    assert file_stream != null;
                    //Write byte structure to file
                    file_stream.write(b, 0, length);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert getActivity().getFilesDir() != null;
            //Put avatar file path to contentValues object
            valuesCollection.put(DataStorage.Collections.AVATAR_FILE_NAME, getActivity().getFilesDir().getPath() + "/" + list[random_number]);
            //Insert data of collection to database asynchronously
            asyncHandler.startInsert(-1, null, DataStorage.Collections.CONTENT_URI, valuesCollection);
            for (int j = 0; j < element_number; j++) {
                ContentValues valuesElement = new ContentValues();
                //Get id of last collection added to database
                Cursor cursor = mContext.getContentResolver().query(DataStorage.Collections.CONTENT_URI,
                        new String[]{DataStorage.Collections._ID},
                        DataStorage.Collections.NAME+" = '"+ collection_name +"'",null,null);
                assert cursor != null;
                cursor.moveToFirst();
                while(!cursor.isAfterLast()){
                    collection_id=cursor.getString(0);
                    cursor.moveToNext();
                }
                String element_name;
                //Put values about element to valuesElement object
                valuesElement.put(DataStorage.Items.NAME, element_name = generateString(new Random(), "abcdefgijklmnouprstwuxyz", 6));
                valuesElement.put(DataStorage.Items.DESCRIPTION, generateString(new Random(), "abcdefgijklmnouprstwuxyz", 15));
                valuesElement.put(DataStorage.Items.ID_COLLECTION, collection_id);
                //Insert data of collection element to database asynchronously
                asyncHandler.startInsert(1, null, DataStorage.Items.CONTENT_URI, valuesElement);
                //Get id of added element to database
                Cursor cursor1 = mContext.getContentResolver().query(DataStorage.Items.CONTENT_URI,
                        new String[]{DataStorage.Items.TABLE_NAME+"."+DataStorage.Items._ID,DataStorage.Media.AVATAR},
                        DataStorage.Items.NAME+" = '"+ element_name +"'",null,null);
                assert cursor1 != null;
                cursor1.moveToFirst();
                while(!cursor1.isAfterLast()){
                    element_id=cursor1.getString(0);
                    cursor1.moveToNext();
                }
                am = mContext.getAssets();
                r = new Random();
                //Get random image from assets folder
                random_number = r.nextInt(list.length);
                try {
                    //Open image
                    is = am.open(list[random_number]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //Create new file in mContext.getFilesDir() path
                file = new File(mContext.getFilesDir(), list[random_number]);
                try {
                    //New stream used to write data in file
                    file_stream = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    while ((length = is.read(b)) != -1) {
                        assert file_stream != null;
                        //Write byte structure to file
                        file_stream.write(b, 0, length);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ContentValues valuesMedia = new ContentValues();
                //Put data of media to valuesMedia object
                valuesMedia.put(DataStorage.Media.ID_ITEM,element_id);
                assert mContext.getFilesDir() !=null;
                valuesMedia.put(DataStorage.Media.AVATAR,mContext.getFilesDir().getPath() + "/" + list[random_number]);
                //Insert data of element media to database asynchronously
                asyncHandler.startInsert(-1,null,DataStorage.Media.CONTENT_URI,valuesMedia);
            }
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
}
