package com.myhoard.app.crudengine;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.myhoard.app.model.Collection;
import com.myhoard.app.model.IModel;
import com.myhoard.app.model.Token;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Description
 *
 * @author Tomasz Nosal
 *         Date: 17.03.14
 */
 public class CRUDEngine<T> implements ICRUDEngine<T> {

    protected String url;

    private static final String AUTHORIZATION = "Authorization";
    private static final String APPLICATIONJSON = "application/json";
    private static final String ERROR_CODE = "error_code";

    public CRUDEngine(String url) {
        this.url = url;
    }

    @Override
    public List<T> getList(Token token) {
        if (token != null) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader(AUTHORIZATION, token.getAccess_token());
            String stringResponse;
            try {
                HttpResponse response = httpClient.execute(httpGet, localContext);
                HttpEntity entity = response.getEntity();
                stringResponse = getASCIIContentFromEntity(entity);

                Type collectionType = new TypeToken<List<Collection>>() {
                }.getType();
                List<T> items = (List<T>) new Gson().fromJson(stringResponse, collectionType);

                return items;
            } catch (Exception e) {
                //return e.getLocalizedMessage();
                return null;
            }
        }
        return null;
    }

    @Override
    public T get(int id) {
        return null;
    }

    @Override
    public boolean create(IModel item, Token token) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000); //Timeout Limit
        HttpResponse response;
        JSONObject json;

        try {
            HttpPost httpPost = new HttpPost(url);
            if (token != null) {
                httpPost.setHeader(AUTHORIZATION, token.getAccess_token());
            }

            json = item.toJson();

            StringEntity se = new StringEntity(json.toString());
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, APPLICATIONJSON));
            httpPost.setEntity(se);
            response = httpClient.execute(httpPost);

            /*Checking response */
            if (response != null) {
                HttpEntity responseEntity = response.getEntity();
                String HTTP_response = EntityUtils.toString(responseEntity, HTTP.UTF_8);
                Log.d("TAG", "Jsontext = " + HTTP_response);
                return !HTTP_response.contains(ERROR_CODE);
            }
        } catch (Exception e) {
            /* AWA:FIXME: Obsługa błędów
Wypychanie błędów do UI
*/
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void update(IModel item, String id, Token token) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000); //Timeout Limit
        HttpResponse response;


        try {
            HttpPut httpPut = new HttpPut(url + id);
            //httpPut.setHeader("Accept", "application/json");
            httpPut.setHeader(AUTHORIZATION, token.getAccess_token());

            JSONObject json = item.toJson();

            StringEntity se = new StringEntity(json.toString());
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, APPLICATIONJSON));
            httpPut.setEntity(se);
            response = httpClient.execute(httpPut);

            /*Checking response */
            if (response != null) {
                HttpEntity responseEntity = response.getEntity();
                String HTTP_response = EntityUtils.toString(responseEntity, HTTP.UTF_8);
                Log.d("TAG", "Jsontext = " + HTTP_response);
                //jezeli odpowiedz zawiera kod Created
                if (HTTP_response.contains("error_code")) {
                    Log.d("TAG", "blad");
                } else {
                    Log.d("TAG", "zupdatowano id=" + id);
                }
            }
        } catch (Exception e) {
            /* AWA:FIXME: Obsługa błędów
                Wypychanie błędów do UI
            */
            e.printStackTrace();
        }
    }

    @Override
    public void remove(String id, Token token) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000); //Timeout Limit
        HttpDelete httpDelete = new HttpDelete(url + id);

        //httpDelete.setHeader("Accept", "application/json");
        httpDelete.setHeader(AUTHORIZATION, token.getAccess_token());

        try {
            httpClient.execute(httpDelete);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /* AWA:FIXME: Magic numbers
*/
    protected String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException {
        Integer ONE = 1;
        Integer ZERO = 0;
        Integer MAXBYTES = 4096;

        InputStream in = entity.getContent();
        StringBuilder out = new StringBuilder();
        int n = ONE;
        while (n > ZERO) {
            byte[] b = new byte[MAXBYTES];
            n = in.read(b);
            if (n > ZERO) out.append(new String(b, ZERO, n));
        }
        return out.toString();
    }
}