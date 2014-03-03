package com.myhoard.app.test.provider;

import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import android.util.Log;
import com.myhoard.app.provider.DataProvider;

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
		} catch(Exception e) {}
	}

	// TODO cała reszta
}
