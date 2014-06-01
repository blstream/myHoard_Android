package com.myhoard.app.crudengine;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.myhoard.app.model.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ConnectTimeoutException;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Description
 *
 * @author Tomasz Nosal & Marcin Łaszcz
 *         Date: 17.03.14
 */
 public class CRUDEngine<T> implements ICRUDEngine<T> {

    private final Class<T> clazz;
    private HttpGet httpGet;
    private HttpPost httpPost;
    private HttpPut httpPut;
    private HttpDelete httpDelete;

    public Class<T> getClazz() {
        return clazz;
    }

    protected String url;

    private static final String AUTHORIZATION = "Authorization";
    private static final String APPLICATION_JSON = "application/json; charset=utf-8";
    private static final String NO_INTERNET_CONNECTION ="YOU DON\'T HAVE INTERNET CONNECTION";
    private static final int STATUS_CREATED = 201;
    private static final int STATUS_OK = 200;
    private static final int STATUS_NO_CONTENT = 204;


    public CRUDEngine(String url,Class<T> clazz) {
        this.url = url;
        this.clazz = clazz;
    }

    @Override
    public List<T> getList(Token token) throws RuntimeException {
        if (token != null) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            httpGet = new HttpGet(url);
            httpGet.setHeader(AUTHORIZATION, token.getAccess_token());
            String stringResponse=null;
            try {
                HttpResponse response = httpClient.execute(httpGet, localContext);
                HttpEntity entity = response.getEntity();

                List<T> newItems = new ArrayList<>();
                stringResponse = getASCIIContentFromEntity(entity);

                JsonElement json = new JsonParser().parse(stringResponse);
                JsonArray array= json.getAsJsonArray();
                Iterator iterator = array.iterator();

                while(iterator.hasNext()){
                    JsonElement json2 = (JsonElement)iterator.next();
                    Gson gson = new Gson();
                    T item = gson.fromJson(json2, clazz);
                    newItems.add(item);
                }

                return newItems;
            } catch (Exception e) {
                throw new RuntimeException(handleError(stringResponse));
            }
        }
        return null;
    }

    @Override
    public T get(String id, Token token) throws RuntimeException  {
        if (token != null) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            httpGet = new HttpGet(url+id);
            httpGet.setHeader(AUTHORIZATION, token.getAccess_token());
            String stringResponse=null;
            try {
                HttpResponse response = httpClient.execute(httpGet, localContext);
                HttpEntity entity = response.getEntity();
                stringResponse = getASCIIContentFromEntity(entity);

                T iModel = new Gson().fromJson(stringResponse, clazz);

                return iModel;
            } catch (Exception e) {
                throw new RuntimeException(handleError(stringResponse));
            }
        }
        return null;
    }

    @Override
    public T searchByName(String url, Token token) {
        if (token != null) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            httpGet = new HttpGet(url);
            httpGet.setHeader(AUTHORIZATION, token.getAccess_token());
            String stringResponse=null;
            try {
                HttpResponse response = httpClient.execute(httpGet, localContext);
                HttpEntity entity = response.getEntity();


                List<T> newItems = new ArrayList<>();
                stringResponse = getASCIIContentFromEntity(entity);

                JsonElement json = new JsonParser().parse(stringResponse);
                JsonArray array= json.getAsJsonArray();
                Iterator iterator = array.iterator();

                while(iterator.hasNext()){
                    JsonElement json2 = (JsonElement)iterator.next();
                    Gson gson = new Gson();
                    T item = gson.fromJson(json2, clazz);
                    newItems.add(item);
                }

                //ponieważ ta metoda z serwera zwraca tablice jsonow, trzeba było pobrać tablice
                //jednak wiemy ze napewno przyjdzie tylko 1 obiekt, ponieważ nazwy są unikatowe
                if (newItems.size()>0)
                    return newItems.get(0);
            } catch (Exception e) {
                throw new RuntimeException(handleError(stringResponse));
            }
        }
        return null;
    }

    @Override
    public IModel create(IModel item, Token token) throws RuntimeException  {
        HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000); //Timeout Limit
        HttpResponse response;
        JSONObject json;
        String HTTP_response=null;

        try {
            httpPost = new HttpPost(url);
            if (token != null) {
                httpPost.setHeader(AUTHORIZATION, token.getAccess_token());
            }

            json = item.toJson();

            StringEntity se = new StringEntity(json.toString(),HTTP.UTF_8);
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON));
            httpPost.setEntity(se);
            response = httpClient.execute(httpPost);

            /*Checking response */
            if (response != null) {
                HttpEntity responseEntity = response.getEntity();
                HTTP_response = EntityUtils.toString(responseEntity, HTTP.UTF_8);
                if (response.getStatusLine().getStatusCode()==STATUS_CREATED) {
                    IModel imodel = new Gson().fromJson(HTTP_response, item.getClass());
                    Log.d("TAG", "Jsontext = " + HTTP_response);
                    String id = imodel.getId();
                    return imodel;
                }
                else {
                    Log.d("TAG", "Jsontext = " + HTTP_response);
                    throw new RuntimeException(HTTP_response);
                }
            }
        } catch (ConnectTimeoutException e) {
            throw new RuntimeException(NO_INTERNET_CONNECTION);
        } catch (Exception e) {
            Log.d("TAG", e.toString());
            throw new RuntimeException(handleError(HTTP_response));
        }
        return null;
    }

    @Override
    public T update(IModel item, String id, Token token) throws RuntimeException  {
        HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000); //Timeout Limit
        HttpResponse response;
        String HTTP_response = null;

        try {
            httpPut = new HttpPut(url + id + "/");
            //httpPut.setHeader("Accept", "application/json");
            httpPut.setHeader(AUTHORIZATION, token.getAccess_token());

            JSONObject json = item.toJson();

            StringEntity se = new StringEntity(json.toString(),HTTP.UTF_8);
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON));
            httpPut.setEntity(se);
            response = httpClient.execute(httpPut);

            /*Checking response */
            if (response != null) {
                HttpEntity responseEntity = response.getEntity();
                if (response.getStatusLine().getStatusCode()==STATUS_OK) {
                    HTTP_response = EntityUtils.toString(responseEntity, HTTP.UTF_8);
                    T model = (T) new Gson().fromJson(HTTP_response, item.getClass());
                    Log.d("TAG", "update Jsontext = " + HTTP_response);
                    return model;
                }
                else {
                    HTTP_response = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
                    Log.d("TAG", "update Jsontext = " + HTTP_response);
                    throw new RuntimeException(handleError(HTTP_response));
                }
            } else
                throw new RuntimeException("No response");
        } catch (Exception e) {
            Log.d("TAG", e.toString());
            throw new RuntimeException(handleError(HTTP_response));
        }
    }

    @Override
    public boolean remove(String id, Token token) throws RuntimeException  {
        HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000); //Timeout Limit
        httpDelete = new HttpDelete(url + id + "/");
        HttpResponse response;

        httpDelete.setHeader(AUTHORIZATION, token.getAccess_token());

        try {
            response = httpClient.execute(httpDelete);
            if (response != null) {
                if (response.getStatusLine().getStatusCode() == STATUS_NO_CONTENT) {
                    Log.d("TAG","usunieto");
                    return true;
                }
            }
        } catch (IOException e) {
            Log.d("TAG","NIEusunieto");
            throw new RuntimeException("Error: delete");
        }
        Log.d("TAG","NIEusunieto");
        return false;
    }

    @Override
    public void stopRequest() {
        if (httpGet != null)
        httpGet.abort();
        if (httpPost != null)
        httpPost.abort();
        if (httpPut != null)
        httpPut.abort();
        if (httpDelete != null)
        httpDelete.abort();
    }

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

    private String handleError(String http_response) {
        if (http_response.contains("errors")) {
            String myHoardError = http_response.split("\\}")[0].split("\\{")[2];
            return myHoardError.split("\"")[3];
        }
        else return "unrecognizable error";
    }
}