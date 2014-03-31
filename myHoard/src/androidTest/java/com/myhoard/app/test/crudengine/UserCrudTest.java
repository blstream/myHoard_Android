package com.myhoard.app.test.crudengine;

import com.myhoard.app.Managers.UserManager;
import com.myhoard.app.model.User;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Description
 *
 * @author Tomasz Nosal
 *         Date: 31.03.14
 */
public class UserCrudTest extends TestCase {

    private String email;
    private String password;
    public static final List<String> URLS = Arrays.asList("http://78.133.154.39:2080/",
            "http://78.133.154.39:1080/");

    public UserCrudTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        email = "android"+Calendar.getInstance().getTimeInMillis()+"@op.pl";
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
        for (String url: URLS) {
            uM.setIp(url);
            assertTrue(uM.register(new User(email, password)));
            uM.login(new User(email, password));
            assertNotNull(uM.getToken());
        }
    }
}
