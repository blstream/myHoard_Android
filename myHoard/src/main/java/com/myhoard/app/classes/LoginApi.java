package com.myhoard.app.classes;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;


public class LoginApi extends AsyncTask<String,String,String>
{

    @Override
    protected String doInBackground(String... strings) {


        String server_address = "http://78.133.154.18:8080/users/";
        String email_to_server = strings[0];
        String password_to_server = strings[1];
        String username = strings[2];
        String result = "";
        String txt = ParseToJSON(email_to_server,password_to_server,username);
        Log.d("appka",txt);


        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(server_address);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            StringEntity en = new StringEntity(txt);
            httpPost.setEntity(en);
            HttpResponse httpResponse = httpclient.execute(httpPost);
            InputStream inputStream = httpResponse.getEntity().getContent();
            int code = httpResponse.getStatusLine().getStatusCode();
            Log.d("appka","a" + code);
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

            Log.d("appka",result);
            return result;


        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    public static String ParseToJSON(String email, String password, String username) {
        try {


            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username",username);
            jsonObject.put("password",password);
            jsonObject.put("email", email);


            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }



}
