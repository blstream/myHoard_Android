package com.myhoard.app.test.crudengine;

import com.myhoard.app.Managers.UserManager;
import com.myhoard.app.model.User;

import junit.framework.TestCase;

import java.util.Calendar;

/**
 * Description
 *
 * @author Tomasz Nosal
 *         Date: 31.03.14
 */
public class UserCrudTest extends TestCase {

    public static final String URL = "http://78.133.154.39:2080/users/";

    public UserCrudTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRegistrationUser() {
        UserManager uM = UserManager.getInstance();
        assertTrue(uM.register(new User(UserExample.EMAIL, UserExample.PASSWORD)));
    }

    public static final class UserExample {
        public static final String EMAIL = "tom5@op.pl";
        public static final String PASSWORD = "haselko";
    }
}
