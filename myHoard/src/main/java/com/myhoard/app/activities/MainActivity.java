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


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.myhoard.app.R;
import com.myhoard.app.dialogs.GeneratorDialog;
import com.myhoard.app.fragments.CollectionFragment;
import com.myhoard.app.fragments.CollectionsListFragment;
import com.myhoard.app.fragments.ElementFragment;
import com.myhoard.app.fragments.ItemsListFragment;
import com.myhoard.app.model.UserManager;
import com.myhoard.app.services.SynchronizeService;

import java.util.List;

public class MainActivity extends ActionBarActivity implements FragmentManager.OnBackStackChangedListener {
    CollectionsListFragment collectionsListFragment;
    UserManager userManager;


    //to receive information from service SynchronizeService
    private ResponseReceiver receiver;

    //variables to progressBar
    ProgressDialog progressBar;
    private Double progressBarStatusIn = 0.;
    private Double progressBarStatusOut = 0.;
    private double maxProgressBarStatus = 10000;
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 100;
    private Handler progressBarHandler = new Handler();

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
        String[]  drawerListItems = getResources().getStringArray(R.array.drawer_menu);



        ArrayAdapter adapter = new ArrayAdapter<>(getBaseContext(),R.layout.drawer_menu_row,R.id.textViewRow,drawerListItems);
        final DrawerLayout drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        final  ListView navigationList = (ListView)findViewById(R.id.drawer_list);
        navigationList.setAdapter(adapter);

        navigationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i,final long l) {
                drawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener(){

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        super.onDrawerClosed(drawerView);
                        int w = i;

                        switch(w){
                            case 0:

                                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                break;
                            case 1:
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.container, new CollectionFragment(),"NewCollection")
                                        .addToBackStack("NewCollection")
                                        .commit();
                                break;
                            case 2:
                                GeneratorDialog generatorDialog = new GeneratorDialog();
                                generatorDialog.show(getSupportFragmentManager(), "");
                                break;
                            case 3:
                                break;

                        }


                    }
                });
                drawerLayout.closeDrawer(navigationList);
            }
        });

    }

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);
        super.onStart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("fragment", getVisibleFragmentTag());
    }

    @Override
    protected void onStop() {
        Log.d("TAG", "onSTop");
        if (receiver == null){
            Log.d("TAG","onSTop Receiver == null");
        } else {
            Log.d("TAG","onSTop Receiver not null");
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("TAG", "onDestroy");
        if (receiver == null){
            Log.d("TAG","onDestroy Receiver == null");
        } else {
            Log.d("TAG","onDestroy Receiver not null");
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
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
        //set search option unvisible
        menu.findItem(R.id.action_search).setVisible(false);
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
                    b.putInt(ElementFragment.ID, -1);
                    elementFragment.setArguments(b);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, elementFragment, "NewElement")
                            .addToBackStack("NewElement")
                            .commit();
                }

                break;
            case R.id.action_login:
                userManager = UserManager.getInstance();
                if (!userManager.isLoggedIn()) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    userManager.logout();
                    Toast toast = Toast.makeText(getApplicationContext(), "Wylogowano",
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                }
                break;
            case R.id.action_generate:
                GeneratorDialog generatorDialog = new GeneratorDialog();
                generatorDialog.show(getSupportFragmentManager(), "");
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
            case R.id.action_synchronize:
                startProgressBar();
                Intent synchronize = new Intent(this, SynchronizeService.class);
                startService(synchronize);
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

    public void shouldDisplayHomeUp() {
        //Enable Up button only  if there are entries in the back stack
        boolean canBack = getSupportFragmentManager().getBackStackEntryCount() > 0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(canBack);
    }

    @Override
    public boolean onSupportNavigateUp() {
        //This method is called when the up button is pressed. Just the pop back stack.
        getSupportFragmentManager().popBackStack();
        return true;
    }

    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP = "MESSAGE_SYNCHRONIZE";

        @Override
        public void onReceive(Context context, Intent intent) {
            String status = intent.getStringExtra(SynchronizeService.currentStatus);
            progressBarStatusOut = Double.parseDouble(status);
            status = intent.getStringExtra(SynchronizeService.maxStatus);
            maxProgressBarStatus = Double.parseDouble(status);
        }

    }


    private void startProgressBar() {
        // prepare for a progress bar dialog
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(true);
        progressBar.setMessage("File synchronizing ...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setProgress(MIN_VALUE);
        progressBar.setMax(MAX_VALUE);

        progressBar.setCancelable(false);
        progressBar.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        progressBar.show();
        //reset progress bar status
        progressBarStatusIn = 0.;
        progressBarStatusOut = 0.;

        new Thread(new Runnable() {
            public void run() {
                while (progressBarStatusIn < MAX_VALUE) {

                    // calculate progress bar status
                    progressBarStatusIn = progressBarStatusOut/maxProgressBarStatus*MAX_VALUE;

                    // Update the progress bar
                    progressBarHandler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress((int) progressBarStatusIn.doubleValue());
                        }
                    });
                }

                // ok, file is downloaded,
                if (progressBarStatusIn >= MAX_VALUE) {

                    // sleep 1 seconds, so that you can see the 100%
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    maxProgressBarStatus = 100000;
                    // close the progress bar dialog
                    progressBar.dismiss();
                }
            }
        }).start();
    }
}
