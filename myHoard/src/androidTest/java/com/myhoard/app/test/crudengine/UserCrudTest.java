package com.myhoard.app.test.crudengine;

import com.google.gson.Gson;
import com.myhoard.app.Managers.UserManager;
import com.myhoard.app.crudengine.CRUDEngine;
import com.myhoard.app.model.User;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Description
 *
 * @author Tomasz Nosal
 *         Date: 31.03.14
 */
public class UserCrudTest extends TestCase {

    public static final List<String> URLS = Arrays.asList("http://78.133.154.39:2080/",
            "http://78.133.154.39:1080/");
    public String email = "android"+Calendar.getInstance().getTimeInMillis()+"@op.pl";
    public String password = "haselko";
    public String updatedEmail = "android"+Calendar.getInstance().getTimeInMillis()+"@op.pl";

    public UserCrudTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();

    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Tests: registration and getting token
     */
    /*public void testRegistrationUser() {
        UserManager uM = UserManager.getInstance();
        for (String url: URLS) {
            uM.setIp(url);
            assertTrue(uM.register(new User(email, password)));
        }
        email = "android"+Calendar.getInstance().getTimeInMillis()+"@op.se";
    }

    public void testLogin() {
        UserManager uM = UserManager.getInstance();
        for (String url: URLS) {
            uM.setIp(url);
            assertTrue(uM.register(new User(email, password)));
            uM.login(new User(email, password));
            assertNotNull(uM.getToken());
            uM.logout();
        }
        email = "android"+Calendar.getInstance().getTimeInMillis()+"@op.de";
    }

    public void testGetUsers() {
        UserManager uM = UserManager.getInstance();
        for (String url: URLS) {
            uM.setIp(url);
            uM.register(new User(email, password));
            uM.login(new User(email, password));
            CRUDEngine<User> usersCRUD = new CRUDEngine<User>(url+"users/",User.class);
            List<User> users = usersCRUD.getList(uM.getToken());
            assertNotNull(users);
        }
        email = "android"+Calendar.getInstance().getTimeInMillis()+"@op.be";
    }*/

    public void testUpdateUser() {
        UserManager uM = UserManager.getInstance();
        for (String url : URLS) {
            uM.setIp(url);
            uM.register(new User(email, password));
            uM.login(new User(email, password));
            CRUDEngine<User> usersCRUD = new CRUDEngine<User>(url+"users/",User.class);
            List<User> users = usersCRUD.getList(uM.getToken());
            List<User> usersy = new ArrayList<User>();

            Iterator iter = users.iterator();
            while(iter.hasNext()) {
                String stringResponse = iter.next().toString();
                User u = new Gson().fromJson(stringResponse, User.class);
                usersy.add(u);
            }

            String idUser = null;
            for (User us: usersy) {
                if(us.getEmail().startsWith("android")){
                    idUser = us.getId();
                }
            }

            usersCRUD.update(new User(updatedEmail,password),idUser,uM.getToken());
        }
        email = "android"+Calendar.getInstance().getTimeInMillis()+"@op.uk";
    }
}
