package com.myhoard.app.test.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import android.util.Log;
import com.myhoard.app.provider.DataProvider;
import com.myhoard.app.provider.DataStorage.Collections;

public class ContentProviderTest extends ProviderTestCase2<DataProvider> {
	private static MockContentResolver resolve;

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

		Uri uri = resolve.insert(Collections.CONTENT_URI, values);
		assertEquals(uri, Collections.CONTENT_URI);
	}

	public void testQueryCollection() {
		Cursor c = resolve.query(Collections.CONTENT_URI, null, null, null, null);
		assertEquals(c.getCount(), 1);
	}

	private static final class TestCollections {
		public static final String NAME = "testName";
		public static final String DESCRIPTION = "testDescription";
		public static final String AVATAR_FILE_NAME = "testAvatarFileName";
		public static final String TAGS = "testTags";
		public static final Long CREATED_DATE = 666l;
		public static final Long MODIFIED_DATE = 777l;
		public static final String SERVERS = "testServers";

	}
}
