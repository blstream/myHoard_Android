package com.myhoard.app.test.activites;

import android.test.ActivityInstrumentationTestCase2;

import com.myhoard.app.activities.LoginActivity;
import com.robotium.solo.Solo;

/**
 * Created by Maciej Plewko on 2014-04-13.
 */
public class LoginActivityRobotiumTest extends ActivityInstrumentationTestCase2<LoginActivity> {
    private Solo solo;

    public LoginActivityRobotiumTest() {
        super(LoginActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    public void testClickLoginButtonEmptyForm() throws Exception {
        solo.clickOnText("Log in");
        solo.sleep(1000);
        solo.assertCurrentActivity("Expected LoginActivity", "LoginActivity");
    }

    public void testClickLoginButtonEmptyEmail() throws Exception {
        solo.enterText(0, "    ");
        solo.enterText(1, "lollol");
        solo.clickOnText("Log in");
        solo.sleep(1000);
        solo.assertCurrentActivity("Expected LoginActivity", "LoginActivity");
    }

    public void testClickLoginButtonEmptyPassword() throws Exception {
        solo.enterText(0, "lol@wp.pl");
        solo.enterText(1, "    ");
        solo.clickOnText("Log in");
        solo.sleep(1000);
        solo.assertCurrentActivity("Expected LoginActivity", "LoginActivity");
    }

    public void testClickLoginButtonCorrectLogin() throws Exception {
        solo.enterText(0, "lol@wp.pl");
        solo.enterText(1, "lollol");
        solo.clickOnText("Log in");
        solo.sleep(1000);
        solo.assertCurrentActivity("Expected MainActivity", "MainActivity");
    }

}
