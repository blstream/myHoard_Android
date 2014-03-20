package com.myhoard.app.model;

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
import com.myhoard.app.httpengine.HttpEngine;


/**
 * Singleton class for managing currently logged user and new user registration
 *
 * @author gohilukk & Marcin Łasszcz
 *         Date: 18.03.14
 */
public class UserManager {
    private static UserManager instance = null;
    private static Object mutex = new Object();

    private User user;
    private Token token;
    private UserHttpEngine userHttpEngine;

    //TODO add support for more servers and removed fixed ip address from code
    private static final String IP = "http://78.133.154.18:8080";

    private UserManager() {

    }

    /**
     * Instance accessor
     * @return user manager instance
     */
    public static UserManager getInstance() {
        if (instance == null) {
            synchronized (mutex) {
                if (instance == null) instance = new UserManager();
            }
        }
        return instance;
    }

    //TODO create more advanced interface for user's token e.g. refreshing
    public Token getToken() {
        return token;
    }

    //Should be removed in the final version of the class
    public User getUser() {
        return user;
    }

    /**
     * Login to a server
     * @param user login information
     * @return true if logged successfully otherwise returns false
     */
    public boolean login(User user) {
        userHttpEngine = new UserHttpEngine(IP + "/oauth/token/");
        token = userHttpEngine.getToken(user);

        if (token == null) {
            this.user = null;
            return false;
        }

        this.user = user;
        return true;
    }

    public boolean register(User user) {
        userHttpEngine = new UserHttpEngine(IP + "/users/");
        return userHttpEngine.create(user, null);
    }

    /**
     * Checks if any user is currently logged in
     * @return true if there is user currently logged in otherwise returns false
     */
    public boolean isLoggedIn() {
        if (user == null) {
            return false;
        }

        return true;
    }

    /**
     * logs out currently logged user
     */
    public void logout() {
        user = null;
        token = null;
    }

    /**
     * Inner class that handles communication with a server using REST
     */
   public class UserHttpEngine extends HttpEngine<User> {

        private static final int TIMEOUT_LIMIT = 10000;

        public UserHttpEngine(String url) {
            super(url);
        }

        /**
         * Gets token that is used for authorization
         * @param user login information
         * @return Token containing data that is used to communicate with a server
         */
        public Token getToken(User user) {
            HttpClient httpClient = new DefaultHttpClient();

            HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), TIMEOUT_LIMIT); //Timeout Limit
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

                //Checking response
                if(response!=null){
                    HttpEntity responseEntity = response.getEntity();
                    String HTTP_response = null;
                    HTTP_response = EntityUtils.toString(responseEntity, HTTP.UTF_8);
                    Log.d("TAG", "Jsontext = " + HTTP_response);
                    //jezeli odpowiedz zawiera kod OK
                    if (HTTP_response.contains("error_code")){
                        return null;
                    } else {
                        Type tokenType = new TypeToken<Token>(){}.getType();
                        Token token = new Gson().fromJson( HTTP_response , tokenType);
                        Log.d("TAG",token.getAccess_token());
                        return token;
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
