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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
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

    public MediaCrudEngine(String url) {
        this.url = url;
    }

    @Override
    public List<T> getList(Token token) {
        return null;
    }

    @Override
    public T get(String id, Token token) {
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
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

        @Override
        public IModel create (IModel media, Token token){
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader(AUTHORIZATION, token.getAccess_token());
            MultipartEntity entity = new MultipartEntity(
                    HttpMultipartMode.BROWSER_COMPATIBLE);

            //Set Data and Content-type header for the image
            entity.addPart("image",
                    new ByteArrayBody(((Media) media).getFile(), "image/jpeg", "image"));
            httpPost.setEntity(entity);
            try {

                HttpResponse response = httpClient.execute(httpPost);
                //Read the response
                String jsonString = EntityUtils.toString(response.getEntity());
                IModel imodel = new Gson().fromJson(jsonString, Media.class);
                Log.d("TAG", "Jsontext = " + jsonString);
                String id = imodel.getId();
                return imodel;
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public T update (IModel t, String id, Token token){
            return null;
        }

        @Override
        public boolean remove (String id, Token token){
            return false;
        }
    }
