package com.myhoard.app.test.crudengine;

import com.google.gson.Gson;
import com.myhoard.app.Managers.UserManager;
import com.myhoard.app.crudengine.CRUDEngine;
import com.myhoard.app.model.Token;
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

    public static final List<String> URLS = Arrays.asList("http://78.133.154.39:2080/");//,
            //"http://78.133.154.39:1080/");
    public String password = "haselko";

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
    public void testRegistrationUser() {
        String email = "regandroid" + Calendar.getInstance().getTimeInMillis()+"@op.pl";
        UserManager uM = UserManager.getInstance();
        for (String url: URLS) {
            uM.setIp(url);
            assertTrue(uM.register(new User(email, password)));
        }
    }

    public void testLogin() {
        UserManager uM = UserManager.getInstance();
        String email = "logandroid" + Calendar.getInstance().getTimeInMillis()+"@op.pl";
        for (String url: URLS) {
            uM.setIp(url);
            boolean tmp = uM.register(new User(email, password));
            assertTrue(tmp);
            uM.login(new User(email, password));
            Token token = uM.getToken();
            assertNotNull(token);
            uM.logout();
        }
    }

    public void testGetUsers() {
        UserManager uM = UserManager.getInstance();
        String email = "getandroid" + Calendar.getInstance().getTimeInMillis()+"@op.pl";
        for (String url: URLS) {
            uM.setIp(url);
            uM.register(new User(email, password));
            uM.login(new User(email, password));
            CRUDEngine<User> usersCRUD = new CRUDEngine<User>(url+"users/",User.class);
            List<User> users = usersCRUD.getList(uM.getToken());
            assertNotNull(users);
        }
    }

    public void testUpdateUser() {
        UserManager uM = UserManager.getInstance();
        String email = "updateandroid" + Calendar.getInstance().getTimeInMillis()+"@op.pl";
        String emailUpdate = "androidUpdate" + Calendar.getInstance().getTimeInMillis()+"@op.pl";
        for (String url : URLS) {
            uM.setIp(url);
            uM.register(new User(email, password));
            uM.login(new User(email, password));
            CRUDEngine<User> usersCRUD = new CRUDEngine<User>(url+"users/",User.class);
            List<User> users = usersCRUD.getList(uM.getToken());


            String idUser = null;
            for (User us: users) {
                if(us.getEmail().equals(email)){
                    idUser = us.getId();
                }
            }

            User u = usersCRUD.update(new User(emailUpdate,password),idUser,uM.getToken());
            //assertEquals(u.getEmail(), emailUpdate);
        }
    }

    public void testDeleteUser() {
        UserManager uM = UserManager.getInstance();
        String email = "delete" + Calendar.getInstance().getTimeInMillis()+"@op.pl";
        for (String url : URLS) {
            uM.setIp(url);
            uM.register(new User(email, password));
            uM.login(new User(email, password));
            CRUDEngine<User> usersCRUD = new CRUDEngine<User>(url+"users/",User.class);
            List<User> users = usersCRUD.getList(uM.getToken());


            String idUser = null;
            for (User us: users) {
                if(us.getEmail().equals(email)){
                    idUser = us.getId();
                }
            }
            assertTrue(usersCRUD.remove(idUser,uM.getToken()));
        }
    }
}
