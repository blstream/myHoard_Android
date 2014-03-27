package com.myhoard.app.test.activites;

import android.app.Activity;
import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ViewAsserts;
import android.widget.Button;
import android.widget.EditText;

import com.myhoard.app.R;
import com.myhoard.app.activities.MainActivity;
import com.myhoard.app.activities.RegisterActivity;

/*
Created by Mateusz Czyszkiewicz
 */
public class RegisterActivityTest extends ActivityInstrumentationTestCase2<RegisterActivity> {


    private EditText emailRegistry;
    private EditText passwordRegistry;
    private Activity mActivity;
    private Button button;

    public RegisterActivityTest()
    {
        super(RegisterActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        mActivity = getActivity();
        emailRegistry = (EditText)mActivity.findViewById(R.id.email_register);
        passwordRegistry = (EditText)mActivity.findViewById(R.id.password_register);
        button = (Button)mActivity.findViewById(R.id.reg_button);


    }
    public void testPreConditions()
    {
        assertNotNull(mActivity);
        assertNotNull(emailRegistry);
        assertNotNull(passwordRegistry);
        assertNotNull(button);

    }

    public void testViewVisible()
    {
        ViewAsserts.assertOnScreen(emailRegistry.getRootView(), emailRegistry);
        ViewAsserts.assertOnScreen(passwordRegistry.getRootView(),passwordRegistry);
        ViewAsserts.assertOnScreen(button.getRootView(),button);

    }

    public void testButton() throws Throwable {
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(MainActivity.class.getName(),null,false);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {

                button.performClick();
            }
        });
        MainActivity mainActivity = (MainActivity) getInstrumentation().waitForMonitorWithTimeout(activityMonitor,3000);
        assertNull(mainActivity);

    }

    public void testEdittextEmpty()
    {
        assertTrue("emailRegistry field is empty","".equals(String.valueOf(emailRegistry.getText())));
        assertTrue("passwordRegistry field is empty","".equals(String.valueOf(passwordRegistry.getText())));
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }



    public void testButtonfillText() throws Throwable {
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(MainActivity.class.getName(),null,false);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                emailRegistry.setText("a@b.pl");
                passwordRegistry.setText("ataaa");
                button.performClick();
            }
        });
        MainActivity mainActivity = (MainActivity) getInstrumentation().waitForMonitorWithTimeout(activityMonitor,3000);
        assertNull(mainActivity);
        //mainActivity.finish();




    }
    public void testButtonfillTextWrong() throws Throwable {
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(MainActivity.class.getName(),null,false);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                emailRegistry.setText("ab.pl");
                passwordRegistry.setText("aaa");
                button.performClick();
            }
        });
        MainActivity mainActivity = (MainActivity) getInstrumentation().waitForMonitorWithTimeout(activityMonitor,5000);
        assertNull(mainActivity);

    }

}
