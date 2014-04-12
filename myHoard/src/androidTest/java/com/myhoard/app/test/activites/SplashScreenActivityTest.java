package com.myhoard.app.test.activites;

import android.test.ActivityInstrumentationTestCase2;

import com.myhoard.app.activities.SplashScreenActivity;
import com.robotium.solo.Solo;

/**
 * Created by Dawid Graczyk on 2014-04-12.
 */
public class SplashScreenActivityTest extends ActivityInstrumentationTestCase2<SplashScreenActivity> {

    private Solo solo;

    public SplashScreenActivityTest() {
        super(SplashScreenActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testClickOnContinueOffline() throws Exception {
        solo.setActivityOrientation(Solo.LANDSCAPE);
        solo.setActivityOrientation(Solo.PORTRAIT);
        solo.clickOnText("Continue offline");
        solo.assertCurrentActivity("Expected MainActivity", "MainActivity");
        solo.goBack();
    }

    public void testClickOnLogIn() throws Exception {
        solo.setActivityOrientation(Solo.LANDSCAPE);
        solo.setActivityOrientation(Solo.PORTRAIT);
        solo.clickOnText("Log in");
        solo.assertCurrentActivity("Expected LoginActivity", "LoginActivity");
        solo.goBack();
    }

    public void testClickOnSignUp() throws Exception {
        solo.setActivityOrientation(Solo.LANDSCAPE);
        solo.setActivityOrientation(Solo.PORTRAIT);
        solo.clickOnText("Sign up");
        solo.assertCurrentActivity("Expected RegisterActivity", "RegisterActivity");
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}
