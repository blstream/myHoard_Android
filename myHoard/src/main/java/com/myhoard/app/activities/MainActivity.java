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
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.myhoard.app.R;
import com.myhoard.app.adapters.NavDrawerListAdapter;
import com.myhoard.app.dialogs.GeneratorDialog;
import com.myhoard.app.fragments.CollectionFragment;
import com.myhoard.app.fragments.CollectionsListFragment;
import com.myhoard.app.fragments.ElementFragment;
import com.myhoard.app.fragments.ItemsListFragment;
import com.myhoard.app.model.RowItem;
import com.myhoard.app.model.UserManager;
import com.myhoard.app.services.SynchronizeService;
import java.util.ArrayList;
import java.util.List;

/*
Created by Rafał Soudani, modified by Tomasz Nosal, Mateusz Czyszkiewicz
*/
public class MainActivity extends ActionBarActivity implements FragmentManager.OnBackStackChangedListener {
    CollectionsListFragment collectionsListFragment;
    UserManager userManager;


    //to receive information from service SynchronizeService
    private ResponseReceiver receiver;
    private ActionBarDrawerToggle actionBarDrawerToggle;


    //variables to progressBar
    ProgressDialog progressBar;
    private Double progressBarStatusIn = 0.;
    private Double progressBarStatusOut = 0.;

    private double maxProgressBarStatus = Double.POSITIVE_INFINITY;
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 100;
    private static final int XOFFSET = 0;
    private static final int YOFFSET = 0;
    private static final String NEWCOLLECTION = "NewCollection";
    private static final String MAIN = "Main";
    private static final String WYLOGOWANO = "Wylogowano";
    private static final String NEWELEMNT = "NewElement";
    private static final String ITEMSLIST = "ItemsList";
    private static final String FRAGMENT = "fragment";

    private Handler handler = new Handler();
    Thread progressBarThread;
    NavDrawerListAdapter navDrawerListAdapter;

    @Override
    /* AWA:FIXME: Ciało metody jest za dlugie.
    Mozna je podzielic na "krótsze" metody
    Patrz:Ksiazka:Czysty kod:Rozdział 3:Funkcje
    */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Listen for changes in the back stack
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        shouldDisplayHomeUp();

        collectionsListFragment = new CollectionsListFragment();
        FragmentManager fm = getSupportFragmentManager();
        if (savedInstanceState == null) {


            /* AWA:FIXME: Hardcoded value
            String "" powinien być jako stała np.
            private final static String NAZWA_STALEJ="Main"
                    */
            fm.beginTransaction()
                    .add(R.id.container, collectionsListFragment, MAIN)
                    .commit();
        } else {
            fm.beginTransaction()
                    .replace(R.id.container,
                            fm.findFragmentByTag(savedInstanceState.getString(FRAGMENT)))
                    .commit();

        }
        List<RowItem> list = preparing_navigationDrawer();
        navDrawerListAdapter = new NavDrawerListAdapter(this,R.layout.drawer_menu_row,list);

        final DrawerLayout drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        final  ListView navigationList = (ListView)findViewById(R.id.drawer_list);
        navigationList.setAdapter(navDrawerListAdapter);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,R.drawable.ic_drawer,R.string.drawer_open,R.string.drawer_close);

        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        navigationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i,final long l) {
                drawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener(){

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        super.onDrawerClosed(drawerView);

                        int w = i;

                        switch(w) {
                            case 0:
                                if (!UserManager.getInstance().isLoggedIn()) {
                                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                } else {
                                    UserManager.getInstance().logout();
                                    Toast toast = Toast.makeText(getBaseContext(), WYLOGOWANO,
                                            Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, XOFFSET, YOFFSET);
                                    toast.show();
                                }
                                break;
                            /* AWA:FIXME: Hardcoded value
                            Magiczne numerki co oznaczaja 1, 2, 3....
                            Musze z kodu wyczytac gdzie trafiłem ???
                            */
                            case 1:
                                if (!getVisibleFragmentTag().equals(NEWCOLLECTION) &&
                                        !getVisibleFragmentTag().equals(ITEMSLIST) &&
                                        !getVisibleFragmentTag().equals(NEWELEMNT)) {
                                    //item.setTitle(R.string.action_new_collection);//TODO correct
                                    getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.container, new CollectionFragment(), NEWCOLLECTION)
                                            .addToBackStack(NEWCOLLECTION)
                                            .commit();
                                } else if (getVisibleFragmentTag().equals(ITEMSLIST)) {
                                    //item.setTitle(R.string.action_new_element);//TODO correct
                                    Fragment elementFragment = new ElementFragment();
                                    Bundle b = new Bundle();
                                    //TODO Add collection id
                                    //b.putLong(ElementFragment.COLLECTION_ID,collectionID);
                                    b.putInt(ElementFragment.ID, -1);
                                    elementFragment.setArguments(b);
                                    getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.container, elementFragment, NEWELEMNT)
                                            .addToBackStack(NEWELEMNT)
                                            .commit();
                                }
                                break;
                            case 2:
                                GeneratorDialog generatorDialog = new GeneratorDialog();
                                generatorDialog.show(getSupportFragmentManager(), "");

                                break;
                            case 3:

                                break;
                            default:
                                break;

                        }
                    }
                });
                drawerLayout.closeDrawer(navigationList);

            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
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
        outState.putString(FRAGMENT, getVisibleFragmentTag());
    }

    @Override
    protected void onStop() {
        if (receiver != null){
        unregisterReceiver(receiver);
            receiver = null;
        }
        super.onStop();
        if (progressBarThread!=null) {
            progressBarThread.interrupt();
        }
    }

    @Override
    protected void onDestroy() {
        if (receiver != null){
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
        throw new IllegalStateException(getString(R.string.no_fragment));
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

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_new_collection:
                item.setTitle(R.string.action_new_collection);
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new CollectionFragment(), NEWCOLLECTION)
                    .addToBackStack(NEWCOLLECTION)
                    .commit();

                break;
            case R.id.action_login:
                userManager = UserManager.getInstance();
                if (!userManager.isLoggedIn()) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    userManager.logout();
                    Toast toast = Toast.makeText(getBaseContext(), WYLOGOWANO,
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, XOFFSET, YOFFSET);
                    toast.show();
                }
                break;
            case R.id.action_generate:
                GeneratorDialog generatorDialog = new GeneratorDialog();
                generatorDialog.show(getSupportFragmentManager(), "");
                break;
            case R.id.action_sort:

                if (!getVisibleFragmentTag().equals(NEWCOLLECTION) &&
                        !getVisibleFragmentTag().equals(ITEMSLIST) &&
                        !getVisibleFragmentTag().equals(NEWELEMNT)) {
                    //TODO collection list custom sort

                } else if (getVisibleFragmentTag().equals(ITEMSLIST)) {
                    // items list sort order change
                    ItemsListFragment fragment = (ItemsListFragment) getSupportFragmentManager().
                            findFragmentByTag(ITEMSLIST);
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
    //    shouldDisplayHomeUp();
    }

    public void shouldDisplayHomeUp() {
        //Enable Up button only  if there are entries in the back stack
      //  boolean canBack = getSupportFragmentManager().getBackStackEntryCount() > 0;
      //  getSupportActionBar().setDisplayHomeAsUpEnabled(canBack);
    }

    @Override
    public boolean onSupportNavigateUp() {
        //This method is called when the up button is pressed. Just the pop back stack.
     //   getSupportFragmentManager().popBackStack();
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
        progressBar.setMessage(getString(R.string.file_synchroniznig));
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setProgress(MIN_VALUE);
        progressBar.setMax(MAX_VALUE);

        progressBar.setCancelable(false);
        progressBar.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        progressBar.show();
        //reset progress bar status
        progressBarStatusIn = 0.;
        progressBarStatusOut = 0.;

        /* AWA:FIXME: Niebezpieczne używanie wątku
        Brak anulowania tej operacji.
        Wyjście z Activity nie kończy wątku,
        należy o to zadbać.
        */
        progressBarThread = new Thread() {
            public void run() {
                while (progressBarStatusIn < MAX_VALUE) {

                    // calculate progress bar status
                    progressBarStatusIn = progressBarStatusOut/maxProgressBarStatus*MAX_VALUE;

                    // Update the progress bar
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress((int) progressBarStatusIn.doubleValue());
                        }
                    });
                }

                // ok, file is downloaded,
                if (progressBarStatusIn >= MAX_VALUE) {
                    maxProgressBarStatus = Double.POSITIVE_INFINITY;
                    // close the progress bar dialog
                    progressBar.dismiss();
                }

            }
        };
        progressBarThread.start();
    }

    public List<RowItem> preparing_navigationDrawer()
    {
        UserManager uM = UserManager.getInstance();
        String[] drawerListItems = null;
        if(uM.isLoggedIn()) {
            drawerListItems = getResources().getStringArray(R.array.drawer_menu_with_logout);
        } else {
            drawerListItems = getResources().getStringArray(R.array.drawer_menu);
        }
        int[] images = {R.drawable.kolekcje,R.drawable.kolekcje,R.drawable.anuluj,R.drawable.znajomi,R.drawable.profilpng};
        //wiem ze to slabe, postaram sie niedlugo zrobic lepsze przekazywanie ikonek


        List<RowItem>list = new ArrayList<>();
        for(int i=0; i < drawerListItems.length ;i++)
        {
            RowItem item = new RowItem(drawerListItems[i],images[i]);
            list.add(item);
        }
        return list;
    }
}
