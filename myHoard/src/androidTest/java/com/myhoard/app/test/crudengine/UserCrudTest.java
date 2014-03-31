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

    private String email;
    private String password;
    public static final String URL = "http://78.133.154.39:2080/users/";

    public UserCrudTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        email = "tom"+Calendar.getInstance().getTimeInMillis()+"@op.pl";
        password = "haselko";
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Tests: registration and getting token
     */
    public void testRegistrationUser() {
        UserManager uM = UserManager.getInstance();
        assertTrue(uM.register(new User(email, password)));
        uM.login(new User(email,password));
        assertNotNull(uM.getToken());
    }
}
