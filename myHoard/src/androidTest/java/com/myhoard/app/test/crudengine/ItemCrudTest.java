package com.myhoard.app.test.crudengine;

import com.myhoard.app.Managers.UserManager;
import com.myhoard.app.crudengine.CRUDEngine;
import com.myhoard.app.model.Collection;
import com.myhoard.app.model.IModel;
import com.myhoard.app.model.Item;
import com.myhoard.app.model.ItemLocation;
import com.myhoard.app.model.ItemMedia;
import com.myhoard.app.model.Token;
import com.myhoard.app.model.User;

import junit.framework.Test;
import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author Marcin Łaszcz
 *         Date: 02.04.14
 */
public class ItemCrudTest extends TestCase {

    private String email;
    private String password;
    private Token token;
    private CRUDEngine<Item> itemsEngine;
    private TestItems testItems;
    private String collectionId;
    private CRUDEngine<Collection> collectionEngine;

    private static final List<String> URLS = Arrays.asList("http://78.133.154.39:2080/",
            "http://78.133.154.39:1080/");

    public ItemCrudTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        testItems = new TestItems();
        email = "android" + Calendar.getInstance().getTimeInMillis() + "@op.pl";
        password = "haselko";
    }

    /**
     * Tests
     */
    public final void testCrudForItems() {
        for (String url : URLS) {

            registerAndGetToken(url);

            //create
            Item item = new Item("0", testItems.NAME, "dsc", null, null, "", "", collectionId);

            IModel imodel = itemsEngine.create(item, token);
            String returnedId = imodel.getId();
            assertNotNull(returnedId);

            //read
            item = itemsEngine.get(returnedId, token);
            assertEquals(item.name, testItems.NAME);

            /**
             * Update
             */
            Item itemToUpdate = new Item(returnedId, testItems.UPDATED_NAME, "dsc", null, null, "", "", collectionId);
            itemToUpdate.setId(returnedId);
            item = itemsEngine.update(itemToUpdate, returnedId, token);
            assertEquals(item.name, testItems.UPDATED_NAME);

            /**
             * Delete
             */
            assertTrue(itemsEngine.remove(returnedId, token));
       }
    }

    private void registerAndGetToken(String url) {
        final String NAME = "dsc";
        final String DESCRIPTION = "dsc";

        UserManager uM = UserManager.getInstance();
        uM.setIp(url);
        uM.register(new User(email, password));
        uM.login(new User(email, password));
        token = uM.getToken();
        itemsEngine = new CRUDEngine<Item>(url+"items/", Item.class);

        collectionEngine = new CRUDEngine<Collection>(url+"collections/", Collection.class);
        Collection collection = new Collection(NAME,DESCRIPTION, null, null, null, null, null);
        IModel imodel = collectionEngine.create(collection, token);
        collectionId = imodel.getId();
    }


    private static final class TestItems {
        public final String NAME;
        public final String UPDATED_NAME;

        public TestItems() {
            NAME = "testName" + Calendar.getInstance().getTime().toString();

            UPDATED_NAME = "testNameUpdated" + Calendar.getInstance().getTimeInMillis();
        }
    }
}