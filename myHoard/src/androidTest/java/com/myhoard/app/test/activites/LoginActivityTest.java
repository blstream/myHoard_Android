package com.myhoard.app.test.activites;


import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ViewAsserts;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.myhoard.app.R;
import com.myhoard.app.activities.LoginActivity;
import com.myhoard.app.activities.MainActivity;
import com.myhoard.app.activities.RegisterActivity;

/*
* Created by Mateusz Czyszkiewicz on 2014-03-25.
*/
public class LoginActivityTest extends ActivityInstrumentationTestCase2<LoginActivity>{

    private TextView txt;
    private Button button;
    private CheckBox checkBox;
    private EditText editText1,editText2;
    LoginActivity mActivity;
    boolean checkbox_test = true;
    private TextView auth_text;



    public LoginActivityTest()
    {
        super(LoginActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        mActivity = getActivity();
        txt = (TextView) mActivity.findViewById(R.id.registration_text);
        button = (Button) mActivity.findViewById(R.id.button_login);
        checkBox = (CheckBox)mActivity.findViewById(R.id.checkbox_remember);
        editText1 = (EditText)mActivity.findViewById(R.id.login_email);
        editText2 = (EditText)mActivity.findViewById(R.id.login_password);
        auth_text = (TextView)mActivity.findViewById(R.id.incorrect_data);

    }


    public void testButtonRegister() throws Throwable {
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(RegisterActivity.class.getName(),null,false);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                txt.performClick();
            }
        });
        RegisterActivity registerActivity = (RegisterActivity) getInstrumentation().waitForMonitorWithTimeout(activityMonitor,5000);
        assertNotNull(registerActivity);
        registerActivity.finish();

    }


    public void testButtonMain() throws Throwable {
        Instrumentation.ActivityMonitor activityMonitor1 = getInstrumentation().addMonitor(MainActivity.class.getName(),null,false);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {

                button.performClick();
            }
        });
        MainActivity mainActivity = (MainActivity) getInstrumentation().waitForMonitorWithTimeout(activityMonitor1,3000);
        assertNull(mainActivity);

    }

    public void testButtonMainfillText() throws Throwable {
        Instrumentation.ActivityMonitor activityMonitor1 = getInstrumentation().addMonitor(MainActivity.class.getName(),null,false);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                editText1.setText("tomek");
                editText2.setText("tomek");

                button.performClick();
            }
        });
        MainActivity mainActivity = (MainActivity) getInstrumentation().waitForMonitorWithTimeout(activityMonitor1,3000);
        assertNotNull(mainActivity);
       // mainActivity.finish();



    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testEdittextEmpty()
    {
        assertTrue("edittex1 field is empty","".equals(String.valueOf(editText1.getText())));
        assertTrue("edittex2 field is empty","".equals(String.valueOf(editText2.getText())));
    }

    public void testZCheckboxUI()
    {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                checkBox.requestFocus();
                checkBox.setChecked(checkbox_test);

            }
        });

    }

    public void testViewVisible()
    {
        ViewAsserts.assertOnScreen(editText1.getRootView(), editText1);
        ViewAsserts.assertOnScreen(editText2.getRootView(),editText2);
        ViewAsserts.assertOnScreen(txt.getRootView(),txt);
        ViewAsserts.assertOnScreen(checkBox.getRootView(),checkBox);
        ViewAsserts.assertOnScreen(button.getRootView(),button);
        ViewAsserts.assertOnScreen(auth_text.getRootView(),auth_text);
    }

    public void testPreConditions()
    {
        assertNotNull(mActivity);
        assertNotNull(checkBox);
        assertNotNull(editText1);
        assertNotNull(editText2);
        assertNotNull(txt);
        assertNotNull(auth_text);
        assertNotNull(button);

    }

    public void testButtonMainfillTextWrong() throws Throwable {
        Instrumentation.ActivityMonitor activityMonitor1 = getInstrumentation().addMonitor(MainActivity.class.getName(),null,false);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                editText1.setText("tomasz");
                editText2.setText("tomasz");

                button.performClick();
            }
        });
        MainActivity mainActivity = (MainActivity) getInstrumentation().waitForMonitorWithTimeout(activityMonitor1,3000);
        assertNull(mainActivity);
        // mainActivity.finish();



    }

}




