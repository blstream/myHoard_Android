package com.myhoard.app.test.provider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.IsolatedContext;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import android.util.Log;

import com.myhoard.app.provider.DataProvider;
import com.myhoard.app.provider.DataStorage.Collections;
import com.myhoard.app.provider.DataStorage.TypeOfCollection;

public class ContentProviderTest extends ProviderTestCase2<DataProvider> {
	private static MockContentResolver resolve;

    Uri uriFromInsert;
	/**
	 * Constructor.
	 */
	public ContentProviderTest() {
		super(DataProvider.class, "com.myhoard.app.provider.DataProvider");
	}

	@Override
	public void setUp() {
		try {
			Log.i("ContentProviderTest", "Entered Setup");
			super.setUp();
			resolve = this.getMockContentResolver();
            // Create the authority for the URI, by removing the 'content://' and any
            // '/' or path part after that.
            String authority = Collections.CONTENT_URI.toString().substring(10);
            int pos = authority.indexOf('/');
            if (pos > -1) {
                authority = authority.substring(0, pos);
            }
            DataProvider provider = new DataProvider();
            provider.attachInfo(getContext(), null);
            resolve.addProvider(authority, provider);
            this.setContext(new IsolatedContext(resolve, getContext()));

            assertTrue(this.getContext() instanceof IsolatedContext);
            assertTrue(this.getContext().getContentResolver() instanceof MockContentResolver);
		} catch (Exception e) {}
	}

	@Override
	public void tearDown() {
		try {
			super.tearDown();
		} catch (Exception e) {
		}
	}

	public void testInsertCollection() {
		ContentValues values = new ContentValues();
		values.put(Collections.NAME, TestCollections.NAME);
		values.put(Collections.DESCRIPTION, TestCollections.DESCRIPTION);
		values.put(Collections.AVATAR_FILE_NAME, TestCollections.AVATAR_FILE_NAME);
		values.put(Collections.TAGS, TestCollections.TAGS);
		values.put(Collections.CREATED_DATE, TestCollections.CREATED_DATE);
		values.put(Collections.MODIFIED_DATE, TestCollections.MODIFIED_DATE);
		values.put(Collections.SERVERS, TestCollections.SERVERS);
        values.put(Collections.TYPE, TestCollections.TYPE);

        uriFromInsert = resolve.insert(Collections.CONTENT_URI, values);

        Cursor c = resolve.query(Collections.CONTENT_URI,null,Collections._ID + "=?", new String[]{((Long)ContentUris.parseId(uriFromInsert)).toString()},null);
        c.moveToFirst();
        assertEquals(((Long) ContentUris.parseId(uriFromInsert)).toString(), c.getString(c.getColumnIndex(Collections._ID)));
        assertEquals(TestCollections.NAME, c.getString(c.getColumnIndex(Collections.NAME)));
        assertEquals(TestCollections.DESCRIPTION, c.getString(c.getColumnIndex(Collections.DESCRIPTION)));
        assertEquals(TestCollections.TAGS, c.getString(c.getColumnIndex(Collections.TAGS)));
        assertEquals(TestCollections.CREATED_DATE.toString(), c.getString(c.getColumnIndex(Collections.CREATED_DATE)));
        assertEquals(TestCollections.MODIFIED_DATE.toString(), c.getString(c.getColumnIndex(Collections.MODIFIED_DATE)));
        assertEquals(TestCollections.SERVERS, c.getString(c.getColumnIndex(Collections.SERVERS)));
        assertEquals(TestCollections.TYPE.toString(), c.getString(c.getColumnIndex(Collections.TYPE)));
	}



	private static final class TestCollections {
		public static final String NAME = "testName";
		public static final String DESCRIPTION = "testDescription";
		public static final String AVATAR_FILE_NAME = "testAvatarFileName";
		public static final String TAGS = "testTags";
		public static final Long CREATED_DATE = 666l;
		public static final Long MODIFIED_DATE = 777l;
		public static final String SERVERS = "testServers";
        public static final Integer TYPE = TypeOfCollection.OFFLINE.getType();
	}
}
