package com.myhoard.app.Managers;

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

import com.myhoard.app.crudengine.CRUDEngine;
import com.myhoard.app.crudengine.ICRUDEngine;
import com.myhoard.app.model.Token;
import com.myhoard.app.model.User;


/**
 * Singleton class for managing currently logged user and new user registration
 *
 * @author Tomasz Nosal & Marcin Łasszcz
 *         Date: 18.03.14
 */
public class UserManager {
    private static UserManager instance = null;
    private static Object mutex = new Object();

    private User user;
    private Token token;
    private UserHttpEngine userHttpEngine;

    //TODO add support for more servers and removed fixed ip address from code
    private static final String IP = "http://78.133.154.39:2080";
    private static final String USER_PATH = "/users/";
    private static final String TOKEN_PATH = "/oauth/token/";
    private static final String API_USERNAME = "username";
    private static final String API_EMAIL = "email";
    private static final String API_PASSWORD = "password";
    private static final String API_GRANT_TYPE = "grant_type";
    private static final String ERROR_STRING = "error_code";
    private static final String CONTENT_TYPE = "application/json";

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

    public Token getToken() {
        return token;
    }
    public User getUser() {
        return user;
    }

    /**
     * Login to a server
     * @param user login information
     * @return true if logged successfully otherwise returns false
     */
    public boolean login(User user) {
        userHttpEngine = new UserHttpEngine(IP + TOKEN_PATH);
        token = userHttpEngine.getToken(user);

        if (token == null) {
            this.user = null;
            return false;
        }

        this.user = user;
        return true;
    }

    public boolean register(User user) {
        userHttpEngine = new UserHttpEngine(IP + USER_PATH);
        return userHttpEngine.create(user, null) == null ? false : true;
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
   public class UserHttpEngine extends CRUDEngine<User> {

        private static final int TIMEOUT_LIMIT = 10000;

        public UserHttpEngine(String url) {
            super(url,User.class);
        }

        /**
         * Gets token that is used for authorization
         * @param user login information
         * @return Token containing data that is used to communicate with a server
         */
        public Token getToken(User user) {
            HttpClient httpClient = new DefaultHttpClient();

            HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), TIMEOUT_LIMIT);
            HttpResponse response;
            JSONObject json = new JSONObject();

            try {
                HttpPost post = new HttpPost(url);

                json.put(API_EMAIL, user.getEmail());
                json.put(API_PASSWORD, user.getPassword());
                json.put(API_GRANT_TYPE, API_PASSWORD);
                StringEntity se = new StringEntity( json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, CONTENT_TYPE));
                post.setEntity(se);
                response = httpClient.execute(post);

                //Checking response
                if(response!=null){
                    HttpEntity responseEntity = response.getEntity();
                    String HTTP_response = null;
                    HTTP_response = EntityUtils.toString(responseEntity, HTTP.UTF_8);

                    if (HTTP_response.contains(ERROR_STRING)){
                        return null;
                    } else {
                        Type tokenType = new TypeToken<Token>(){}.getType();
                        Token token = new Gson().fromJson( HTTP_response , tokenType);
                        return token;
                    }
                }
            } catch(Exception e) {
                return null;
            }
            return null;
        }
    }
}
