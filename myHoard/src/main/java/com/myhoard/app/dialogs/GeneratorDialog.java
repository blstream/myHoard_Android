package com.myhoard.app.dialogs;

import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
public class GeneratorDialog extends DialogFragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private EditText collectionNumber;
    private EditText elementNumber;
    private Button cancelButton;
    private Button generateButton;
    private String collection_name;
    private String object_id;
    private String element_name;
    private Context mContext;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.generator_dialog);
        mContext = getActivity();
        collectionNumber = (EditText)dialog.findViewById(R.id.editTextGeneratorDialog);
        elementNumber = (EditText)dialog.findViewById(R.id.editText2GeneratorDialog);
        cancelButton = (Button)dialog.findViewById(R.id.buttonGeneratorDialog);
        generateButton = (Button)dialog.findViewById(R.id.button2GeneratorDialog);
        cancelButton.setOnClickListener(this);
        generateButton.setOnClickListener(this);
        return dialog;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.buttonGeneratorDialog:
                dismiss();
                break;
            case R.id.button2GeneratorDialog:
                generateData();
                dismiss();
                break;
        }
    }

    public void generateData() {
        assert collectionNumber.getText() != null;
        int collection_number = Integer.parseInt(collectionNumber.getText().toString());
        assert elementNumber.getText() != null;
        int element_number = Integer.parseInt(elementNumber.getText().toString());
        InputStream is = null;
        String[] list = new String[]{"flower1.jpg","flower2.jpg","beer1.jpg","snack1.jpg"};
        OutputStream file_stream = null;
        AsyncQueryHandler asyncHandler =
                new AsyncQueryHandler(getActivity().getContentResolver()) {
                };
        for (int i = 0; i < collection_number; i++) {
            ContentValues valuesCollection = new ContentValues();
            valuesCollection.put(DataStorage.Collections.NAME, collection_name = generateString(new Random(), "abcdefgijklmnouprstwuxyz", 10));
            valuesCollection.put(DataStorage.Collections.DESCRIPTION, generateString(new Random(), "abcdefgijklmnouprstwuxyz", 20));
            AssetManager am = getActivity().getAssets();
            Random r = new Random();
            int random_number = r.nextInt(list.length);
            try {
                is = am.open(list[random_number]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            File file = new File(getActivity().getFilesDir(), list[random_number]);
            try {
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
                    file_stream.write(b, 0, length);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert getActivity().getFilesDir() != null;
            valuesCollection.put(DataStorage.Collections.AVATAR_FILE_NAME, getActivity().getFilesDir().getPath() + "/" + list[random_number]);
            asyncHandler.startInsert(-1, null, DataStorage.Collections.CONTENT_URI, valuesCollection);
            for (int j = 0; j < element_number; j++) {
                ContentValues valuesElement = new ContentValues();
                getLoaderManager().initLoader(0, null, this);
                valuesElement.put(DataStorage.Items.NAME, element_name = generateString(new Random(), "abcdefgijklmnouprstwuxyz", 6));
                valuesElement.put(DataStorage.Items.DESCRIPTION, generateString(new Random(), "abcdefgijklmnouprstwuxyz", 15));
                valuesElement.put(DataStorage.Items.ID_COLLECTION, object_id);
                asyncHandler.startInsert(1, null, DataStorage.Items.CONTENT_URI, valuesElement);
                getLoaderManager().restartLoader(1,null,this);
                am = getActivity().getAssets();
                r = new Random();
                random_number = r.nextInt(list.length);
                try {
                    is = am.open(list[random_number]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                file = new File(getActivity().getFilesDir(), list[random_number]);
                try {
                    file_stream = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    while ((length = is.read(b)) != -1) {
                        assert file_stream != null;
                        file_stream.write(b, 0, length);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ContentValues valuesMedia = new ContentValues();
                valuesMedia.put(DataStorage.Media.ID_ITEM,object_id);
                valuesMedia.put(DataStorage.Media.AVATAR,getActivity().getFilesDir().getPath() + "/" + list[random_number]);
                asyncHandler.startInsert(-1,null,DataStorage.Media.CONTENT_URI,valuesMedia);
            }
        }
    }
    public String generateString(Random rng, String characters, int length)
    {
        char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursor_loader = null;
        switch(id){
            case 0:
                cursor_loader = new CursorLoader(mContext, DataStorage.Collections.CONTENT_URI,
                        new String[]{DataStorage.Collections._ID},
                        DataStorage.Collections.NAME+" = '"+collection_name+"'",null,null);
                break;
            case 1:
                cursor_loader = new CursorLoader(mContext, DataStorage.Items.CONTENT_URI,
                        new String[]{DataStorage.Items.TABLE_NAME+"."+DataStorage.Items._ID},
                        DataStorage.Items.NAME+" = '"+element_name+"'",null,null);
                break;
        }
        return cursor_loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursor.moveToFirst();
        object_id = cursor.getString(0);
        cursor.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
