package com.myhoard.app.test.crudengine;

import com.myhoard.app.crudengine.CrudEngine;
import com.myhoard.app.model.Collection;
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
 * @author gohilukk
 *         Date: 30.03.14
 */
public class CollectionCrudTest extends TestCase {

    private static final String EMAIL = "tomek";
    private static final String PASSWORD = "tomek";
    private Token token;
    private CrudEngine<Collection> collectionEngine;
    public static final String URL = "http://78.133.154.18:8080/collections/";


    public CollectionCrudTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        //pobranie tokena = logowanie
        UserManager uM = UserManager.getInstance();
        uM.login(new User(EMAIL,PASSWORD));
        token = uM.getToken();
        collectionEngine = new CrudEngine<Collection>(URL);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Tests
     */
    public final void testCreate() {
        Collection collection = new Collection(TestCollections.NAME,
                TestCollections.DESCRIPTION,
                TestCollections.TAGS,
                null,
                null,
                null,
                null);

        assertFalse(collectionEngine.create(collection,token));
    }


    private static final class TestCollections {
        public static final String NAME = "testName" + Calendar.getInstance().getTime().toString();
        public static final String DESCRIPTION = "testDescription" + Calendar.getInstance().getTime().toString();
        public static final List<String> TAGS = Arrays.asList("Buenos Aires", "Córdoba", "La Plata");
    }
}
