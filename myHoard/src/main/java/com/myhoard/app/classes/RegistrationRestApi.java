package com.myhoard.app.classes;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.URL;

/**
 * Created by Czyz on 11.03.14.
 */
public class RegistrationRestApi extends AsyncTask<String,String,String>
{

    @Override
    protected String doInBackground(String... strings) {

        String urlString = strings[0];
        String email_to_server = strings[1];
        String password_to_server = strings[2];


        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
}
