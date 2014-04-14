package com.myhoard.app.test.activites;

import android.support.v7.internal.view.menu.MenuView;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;

import com.myhoard.app.R;
import com.myhoard.app.activities.MainActivity;
import com.robotium.solo.Solo;

import java.util.List;

/**
 * Created by Dawid Graczyk on 2014-04-13.
 */
public class CollectionsTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private static final String MAIN = "Main";
    private static final String COLLECTION_NAME = "Test";
    private static final String COLLECTION_DESC = "Testing";
    private static final String COLLECTION_TAG = "Tag";
    private final int TIME = 1000;

    GridView gridView;
    private Solo solo;

    public CollectionsTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
        gridView = (GridView) getActivity().findViewById(R.id.gridview);
    }

    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    public void clickOnActionBarItem(int resourceId) {

        List<View> views = solo.getCurrentViews();
        for (View v : views) {
            if (v instanceof MenuView.ItemView) {
                MenuView.ItemView itemView = (MenuView.ItemView) v;
                if (itemView.getItemData().getItemId() != resourceId) {
                    continue;
                }
                solo.clickOnView(v);
                return;
            }
        }
    }

    public void addNewCollection(String name, String desc, String tag) {
        assertTrue("Expected four edit text", solo.waitForView(EditText.class, 4, 1000));
        solo.enterText(0, name);
        solo.enterText(2, desc);
        solo.enterText(3, tag);
        assertTrue("Not found name", solo.searchText(name));
        assertTrue("Not found description", solo.searchText(desc));
        assertTrue("Not found tag", solo.searchText(tag));
        clickOnActionBarItem(R.id.action_accept);

    }

    public void testAddNewCollections() {
        assertTrue("Expected CollectionsListFragment", solo.waitForFragmentByTag(MAIN));
        clickOnActionBarItem(R.id.action_new_collection);
        addNewCollection(COLLECTION_NAME,COLLECTION_DESC,COLLECTION_TAG);
        solo.sleep(TIME);
        clickOnActionBarItem(R.id.action_new_collection);
        addNewCollection(COLLECTION_NAME,COLLECTION_DESC,COLLECTION_TAG);
        assertTrue("Expected Toast", solo.waitForText(getActivity().getString(R.string.name_already_exist)));
        solo.sleep(TIME);
        solo.goBack();

        int collectionToAdd = 5;
        for(int i=0;i<collectionToAdd;i++) {
            clickOnActionBarItem(R.id.action_new_collection);
            addNewCollection(COLLECTION_NAME+i,COLLECTION_DESC,COLLECTION_TAG);
            assertTrue("Expected Toast", solo.waitForText(getActivity().getString(R.string.collection_added)));
            solo.sleep(TIME);
        }
        gridView = (GridView) getActivity().findViewById(R.id.gridview);
        assertEquals(collectionToAdd+1, gridView.getAdapter().getCount());
    }

    public void testDeleteAllCollection() {
        gridView = (GridView) getActivity().findViewById(R.id.gridview);
        int count = gridView.getAdapter().getCount();
        for (int i = 0; i < count; i++) {
            solo.clickLongInList(0);
            solo.clickOnMenuItem("Delete");
            assertTrue("Expected AlertDialog", solo.waitForText("DELETE COLLECTION"));
            solo.setActivityOrientation(Solo.LANDSCAPE);
            solo.setActivityOrientation(Solo.PORTRAIT);
            solo.clickOnButton("Ok");
        }
    }

    public void testEmptyCollectionList() {
        assertTrue("Expected CollectionsListFragment", solo.waitForFragmentByTag(MAIN));
        if(gridView.getAdapter().getCount() !=0) testDeleteAllCollection();
        if (gridView.getAdapter().getCount() == 0) {
            assertTrue("Expected ImageView of empty list", solo.waitForView(ImageView.class, 2, 1000));
            View view = solo.getView(R.id.ivFirstCollectionButton);
            solo.clickOnView(view);
            addNewCollection(COLLECTION_NAME,COLLECTION_DESC,COLLECTION_TAG);
            assertTrue("Expected Toast", solo.waitForText(getActivity().getString(R.string.collection_added)));
            gridView = (GridView) getActivity().findViewById(R.id.gridview);
            assertEquals(1, gridView.getAdapter().getCount());
        }
    }

    public void testFirstCollectionEdit() {
        gridView = (GridView) getActivity().findViewById(R.id.gridview);
        if (gridView.getAdapter().getCount() != 0) {
            solo.clickLongInList(0);
            solo.clickOnMenuItem("Edit");
            assertTrue("Expected 4 edit text", solo.waitForView(EditText.class, 4, 1000));
            assertTrue("Not found name", solo.searchText(COLLECTION_NAME));
            assertTrue("Not found description", solo.searchText(COLLECTION_DESC));
            assertTrue("Not found tag", solo.searchText(COLLECTION_TAG));
            isNameCollection();
            solo.sleep(TIME);
            isNameLongEnough();
            solo.sleep(TIME);
            clickOnActionBarItem(R.id.action_accept);
            assertTrue("Expected Toast", solo.waitForText(getActivity().getString(R.string.collection_edited)));
        }
    }

    public void isNameCollection() {
        solo.clearEditText(0);
        clickOnActionBarItem(R.id.action_accept);
        assertTrue("Expected Toast", solo.waitForText(getActivity().getString(R.string.required_name_collection)));
        solo.enterText(0, "     ");
        solo.sleep(TIME);
        clickOnActionBarItem(R.id.action_accept);
        assertTrue("Expected Toast", solo.waitForText(getActivity().getString(R.string.required_name_collection)));
        solo.enterText(0,COLLECTION_NAME);
    }

    public void isNameLongEnough() {
        solo.clearEditText(0);
        solo.enterText(0,"p");
        clickOnActionBarItem(R.id.action_accept);
        assertTrue("Expected Toast", solo.waitForText(getActivity().getString(R.string.name_too_short)));
        solo.clearEditText(0);
        solo.enterText(0,COLLECTION_NAME);
    }
}
