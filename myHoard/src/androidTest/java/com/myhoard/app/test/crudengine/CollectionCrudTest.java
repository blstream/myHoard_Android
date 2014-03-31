package com.myhoard.app.test.crudengine;

import com.myhoard.app.crudengine.CRUDEngine;
import com.myhoard.app.model.Collection;
import com.myhoard.app.model.IModel;
import com.myhoard.app.model.Token;
import com.myhoard.app.model.User;
import com.myhoard.app.Managers.UserManager;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Description
 *
 * @author Tomasz Nosal
 *         Date: 30.03.14
 */
public class CollectionCrudTest extends TestCase {

    private String email;
    private String password;
    private Token token;
    private CRUDEngine<Collection> collectionEngine;
    TestCollections testCollections;
    public static final List<String> URLS = Arrays.asList("http://78.133.154.39:2080/",
            "http://78.133.154.39:1080/");

    public CollectionCrudTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        testCollections = new TestCollections();
        email = "android" + Calendar.getInstance().getTimeInMillis() + "@op.pl";
        password = "haselko";
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Tests
     */
    public final void testCrudForCollections() {
        for (String url : URLS) {
            registerAndGetToken(url);
            /**
             * Create
             */
            Collection collection = new Collection(testCollections.NAME,
                    testCollections.DESCRIPTION,
                    testCollections.TAGS,
                    null,
                    null,
                    null,
                    null);

            IModel imodel = collectionEngine.create(collection, token);
            String returnedId = imodel.getId();
            assertNotNull(returnedId);

            /**
             * Read
             */
            Collection c = collectionEngine.get(returnedId, token);
            assertEquals(c.getName(), testCollections.NAME);
            assertEquals(c.getDescription(), testCollections.DESCRIPTION);
            for (String s : testCollections.TAGS) {
                assertTrue(c.getTags().contains(s));
            }

            /**
             * Update
             */
            Collection collectionToUpdate = new Collection(testCollections.UPDATED_NAME,
                    testCollections.UPDATED_DESCRIPTION,
                    testCollections.UPDATED_TAGS,
                    null,
                    null,
                    null,
                    null);
            collectionToUpdate.setId(returnedId);
            c = collectionEngine.update(collectionToUpdate, returnedId, token);
            assertEquals(c.getName(), testCollections.UPDATED_NAME);
            assertEquals(c.getDescription(), testCollections.UPDATED_DESCRIPTION);
            for (String s : testCollections.UPDATED_TAGS) {
                assertTrue(c.getTags().contains(s));
            }

            /**
             * Delete
             */
            assertTrue(collectionEngine.remove(returnedId, token));
        }
    }

    private void registerAndGetToken(String url) {
        UserManager uM = UserManager.getInstance();
        //rejestracja
        uM.setIp(url);
        uM.register(new User(email, password));
        //pobranie tokena = logowanie
        uM.login(new User(email, password));
        token = uM.getToken();
        collectionEngine = new CRUDEngine<Collection>(url+"collections/", Collection.class);
    }


    private static final class TestCollections {
        public final String NAME;
        public final String DESCRIPTION;
        public final List<String> TAGS;

        public final String UPDATED_NAME;
        public final String UPDATED_DESCRIPTION;
        public final List<String> UPDATED_TAGS;

        public TestCollections() {
            NAME = "testName" + Calendar.getInstance().getTime().toString();
            DESCRIPTION = "testDescription" + Calendar.getInstance().getTime().toString();
            TAGS = Arrays.asList("Buenos Aires", "Cordoba", "La Plata");

            UPDATED_NAME = "testNameUpdated" + Calendar.getInstance().getTimeInMillis();
            UPDATED_DESCRIPTION = "testDescriptionUpdated" + Calendar.getInstance().getTimeInMillis();
            UPDATED_TAGS = Arrays.asList("Barcelona", "Valencia");
        }
    }
}
