package com.myhoard.app.httpengine;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.lang.reflect.Type;

import com.myhoard.app.model.Token;
import com.myhoard.app.model.User;

/**
 * Description
 *
 * @author gohilukk
 *         Date: 18.03.14
 */
 public class UserHttpEngine extends HttpEngine {

    public UserHttpEngine(String url) {
        super(url);
    }

    public String getAuthenticationCode(User user) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000); //Timeout Limit
        HttpResponse response;
        JSONObject json = new JSONObject();

        try {
            HttpPost post = new HttpPost(url);
            json.put("username", user.getUsername());
            json.put("password", user.getPassword());
            json.put("grant_type", "password");
            StringEntity se = new StringEntity( json.toString());
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            post.setEntity(se);
            response = httpClient.execute(post);

            /*Checking response */
            if(response!=null){
                HttpEntity responseEntity = response.getEntity();
                String HTTP_response = null;
                HTTP_response = EntityUtils.toString(responseEntity, HTTP.UTF_8);
                Log.d("TAG", "Jsontext = " + HTTP_response);
                //jezeli odpowiedz zawiera kod OK
                if (HTTP_response.contains("error_code")){
                    return "bład";
                } else {
                    Type tokenType = new TypeToken<Token>(){}.getType();
                    Token token = new Gson().fromJson( HTTP_response , tokenType);
                    Log.d("TAG",token.getAccess_token());
                    return token.getAccess_token();
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
