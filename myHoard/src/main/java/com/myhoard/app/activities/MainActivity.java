/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.myhoard.app.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.myhoard.app.R;
import com.myhoard.app.dialogs.GeneratorDialog;
import com.myhoard.app.fragments.CollectionFragment;
import com.myhoard.app.fragments.CollectionsListFragment;
import com.myhoard.app.fragments.ElementFragment;
import com.myhoard.app.fragments.ItemsListFragment;
import com.myhoard.app.fragments.SearchFragment;
import com.myhoard.app.model.UserSingleton;

import java.util.List;

public class MainActivity extends ActionBarActivity implements FragmentManager.OnBackStackChangedListener {
	CollectionsListFragment collectionsListFragment;
    UserSingleton userSingleton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        //Listen for changes in the back stack
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        shouldDisplayHomeUp();

		collectionsListFragment = new CollectionsListFragment();
		FragmentManager fm = getSupportFragmentManager();
		if (savedInstanceState == null) {
			fm.beginTransaction()
					.add(R.id.container, collectionsListFragment, "Main")
					.commit();
		} else {
			fm.beginTransaction()
					.replace(R.id.container,
							fm.findFragmentByTag(savedInstanceState.getString("fragment")))
					.commit();
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("fragment", getVisibleFragmentTag());
	}

	public String getVisibleFragmentTag() {
		FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
		List<Fragment> fragments = fragmentManager.getFragments();
		for (Fragment fragment : fragments) {
			if (fragment != null && fragment.isVisible())
				return fragment.getTag();
		}
		throw new IllegalStateException("There is no active fragment");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
        //setting sort option unvisible
        menu.findItem(R.id.action_sort).setVisible(false);
        return super.onCreateOptionsMenu(menu);
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.action_new_collection:
			if (!getVisibleFragmentTag().equals("NewCollection") &&
                    !getVisibleFragmentTag().equals("ItemsList") &&
                    !getVisibleFragmentTag().equals("NewElement")) {
                //item.setTitle(R.string.action_new_collection);//TODO correct
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.container, new CollectionFragment(), "NewCollection")
						.addToBackStack("NewCollection")
						.commit();
			} else if (getVisibleFragmentTag().equals("ItemsList")) {
                //item.setTitle(R.string.action_new_element);//TODO correct
                Fragment elementFragment = new ElementFragment();
                Bundle b = new Bundle();
                //TODO Add collection id
                //b.putLong(ElementFragment.COLLECTION_ID,collectionID);
                b.putInt(ElementFragment.ID,-1);
                elementFragment.setArguments(b);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, elementFragment, "NewElement")
                        .addToBackStack("NewElement")
                        .commit();
            }

			break;
		case R.id.action_login:
            userSingleton = UserSingleton.getInstance();
            if (userSingleton.user == null) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
            else {
                userSingleton.user = null;
                userSingleton.token = null;
                Toast toast = Toast.makeText(getApplicationContext(), "Wylogowano",
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            }
			break;
        case R.id.action_generate:
                GeneratorDialog generatorDialog = new GeneratorDialog();
                generatorDialog.show(getSupportFragmentManager(),"");
                break;
		case R.id.action_search:
			if (!getVisibleFragmentTag().equals("Search")) {

				getSupportFragmentManager().beginTransaction()
						.replace(R.id.container, new SearchFragment(), "Search")
						.addToBackStack("Search")
						.commit();
			}
			break;
            case R.id.action_sort:
                if (!getVisibleFragmentTag().equals("NewCollection") &&
                        !getVisibleFragmentTag().equals("ItemsList") &&
                        !getVisibleFragmentTag().equals("NewElement")) {
                    //TODO collection list custom sort

                } else if (getVisibleFragmentTag().equals("ItemsList")) {
                    // items list sort order change
                    ItemsListFragment fragment = (ItemsListFragment) getSupportFragmentManager().
                            findFragmentByTag("ItemsList");
                    fragment.itemsSortOrderChange(item);
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
		return true;
	}

    @Override
    public void onBackStackChanged() {
        shouldDisplayHomeUp();
    }

    public void shouldDisplayHomeUp(){
        //Enable Up button only  if there are entries in the back stack
        boolean canBack = getSupportFragmentManager().getBackStackEntryCount()>0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(canBack);
    }

    @Override
    public boolean onSupportNavigateUp() {
        //This method is called when the up button is pressed. Just the pop back stack.
        getSupportFragmentManager().popBackStack();
        return true;
    }
}
