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
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.myhoard.app.R;
import com.myhoard.app.fragments.CollectionFragment;
import com.myhoard.app.fragments.CollectionsListFragment;
import com.myhoard.app.fragments.OnFragmentClickListener;

import java.util.List;

public class MainActivity extends ActionBarActivity implements OnFragmentClickListener {
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int EDIT_ID = Menu.FIRST + 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        if (savedInstanceState == null) {
            fm.beginTransaction()
                    .add(R.id.container, new CollectionsListFragment(), "Main")
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_new_collection:
                if (!getVisibleFragmentTag().equals("NewCollection")){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new CollectionFragment(), "NewCollection")
                        .addToBackStack("NewCollection")
                        .commit();
                }
            break;
            case R.id.action_login:
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
            break;
        default:
        return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, EDIT_ID, 0, R.string.menu_edit);
        menu.add(0, DELETE_ID, 1, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case DELETE_ID:
                //TODO: Delete collection
                return true;
            case EDIT_ID:
                //TODO: Edit collection
                return true;
        }
        return super.onContextItemSelected(item);
    }


    @Override
    public void OnFragmentClick() {
        CollectionsListFragment.fillGridView();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public String getVisibleFragmentTag(){
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for(Fragment fragment : fragments){
            if(fragment != null && fragment.isVisible())
                return fragment.getTag();
        }
        throw new IllegalStateException("There is no active fragment");
    }
}
