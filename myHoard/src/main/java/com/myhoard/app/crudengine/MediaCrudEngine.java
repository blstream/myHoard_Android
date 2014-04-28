package com.myhoard.app.crudengine;

import android.util.Log;

import com.google.gson.Gson;
import com.myhoard.app.model.IModel;
import com.myhoard.app.model.Media;
import com.myhoard.app.model.Token;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.mime.MultipartEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Description
 *
 * @author gohilukk
 *         Date: 03.04.14
 */
public class MediaCrudEngine<T> implements ICRUDEngine<T> {

    protected String url;
    private static final String AUTHORIZATION = "Authorization";
    private static final int STATUS_NO_CONTENT = 204;

    public MediaCrudEngine(String url) {
        this.url = url;
    }

    @Override
    public List<T> getList(Token token) {
        return null;
    }

    @Override
    public T get(String id, Token token) throws RuntimeException {
        if (token != null) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpGet httpGet = new HttpGet(url + id);
            httpGet.setHeader(AUTHORIZATION, token.getAccess_token());
            try {
                HttpResponse response = httpClient.execute(httpGet, localContext);
                HttpEntity entity = response.getEntity();
                InputStream inputStream = entity.getContent();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte data[] = new byte[1024];
                int count;
                while ((count = inputStream.read(data)) != -1) {
                    bos.write(data, 0, count);
                }
                return (T) new Media(bos.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException("Error: get media");
            }
        }
        return null;
    }

        @Override
        public IModel create (IModel media, Token token)  throws RuntimeException {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader(AUTHORIZATION, token.getAccess_token());
            MultipartEntity entity = new MultipartEntity(
                    HttpMultipartMode.BROWSER_COMPATIBLE);

            entity.addPart("image",
                    new ByteArrayBody(((Media) media).getFile(), "image/jpeg", "image"));
            httpPost.setEntity(entity);
            String jsonString = null;
            try {
                HttpResponse response = httpClient.execute(httpPost);
                //Read the response
                jsonString = EntityUtils.toString(response.getEntity());
                IModel imodel = new Gson().fromJson(jsonString, Media.class);
                Log.d("TAG", "Jsontext = " + jsonString);
                String id = imodel.getId();
                return imodel;
            } catch (IOException e) {
                throw new RuntimeException(jsonString);
            }
        }

        @Override
        public T update (IModel media, String id, Token token) throws RuntimeException {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPut httpPut = new HttpPut(url+id+"/");
            httpPut.setHeader(AUTHORIZATION, token.getAccess_token());
            MultipartEntity entity = new MultipartEntity(
                    HttpMultipartMode.BROWSER_COMPATIBLE);
            entity.addPart("image",
                    new ByteArrayBody(((Media) media).getFile(), "image/jpeg", "image"));
            httpPut.setEntity(entity);
            String jsonString = null;
            try {
                HttpResponse response = httpClient.execute(httpPut);
                //Read the response
                jsonString = EntityUtils.toString(response.getEntity());
                IModel imodel = new Gson().fromJson(jsonString, Media.class);
                Log.d("TAG", "Jsontext = " + jsonString);
                String returedId = imodel.getId();
                return (T)imodel;
            } catch (IOException e) {
                throw new RuntimeException(jsonString);
            }
        }

        @Override
        public boolean remove (String id, Token token) throws RuntimeException {
            HttpClient httpClient = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000); //Timeout Limit
            HttpDelete httpDelete = new HttpDelete(url + id + "/");
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
                throw new RuntimeException("Error: delete media");
            }
            Log.d("TAG","NIEusunieto");
            throw new RuntimeException("Error: delete media");
        }
    }
