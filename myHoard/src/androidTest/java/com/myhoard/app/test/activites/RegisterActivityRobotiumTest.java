package com.myhoard.app.test.activites;

import android.test.ActivityInstrumentationTestCase2;

import com.myhoard.app.activities.RegisterActivity;
import com.robotium.solo.Solo;

/**
 * Created by Piotr Brzozowski on 2014-04-13.
 */
public class RegisterActivityRobotiumTest extends ActivityInstrumentationTestCase2<RegisterActivity> {
    private Solo solo;
    public RegisterActivityRobotiumTest() {
        super(RegisterActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    public void testClickLoginButton(){
        solo.clickOnText("Sign up");
        solo.sleep(1000);
        solo.assertCurrentActivity("Expected RegisterActivity", "RegisterActivity");
    }

    public void testValidateEmailEmpty() {
        solo.enterText(0,"    ");
        solo.clickOnText("Sign up");
        solo.sleep(1000);
        solo.assertCurrentActivity("Expected RegisterActivity", "RegisterActivity");
    }

    public void testValidateIncorrectEmail(){
        solo.enterText(0,"test@");
        solo.clickOnText("Sign up");
        solo.sleep(1000);
        solo.assertCurrentActivity("Expected RegisterActivity", "RegisterActivity");
    }

    public void testValidateEmailCorrectEmail(){
        solo.enterText(0,"test@wp");
        solo.clickOnText("Sign up");
        solo.sleep(1000);
        solo.assertCurrentActivity("Expected RegisterActivity", "RegisterActivity");
    }

    public void testValidatePasswordEmpty(){
        solo.enterText(1,"  ");
        solo.clickOnText("Sign up");
        solo.sleep(1000);
        solo.assertCurrentActivity("Expected RegisterActivity", "RegisterActivity");
    }

    public void testValidatePasswordIncorrect(){
        solo.enterText(1,"abc");
        solo.clickOnText("Sign up");
        solo.sleep(1000);
        solo.assertCurrentActivity("Expected RegisterActivity", "RegisterActivity");
    }

    public void testValidatePasswordCorrect(){
        solo.enterText(1,"abcd");
        solo.clickOnText("Sign up");
        solo.sleep(1000);
        solo.assertCurrentActivity("Expected RegisterActivity", "RegisterActivity");
    }

    public void testValidatePasswordAndEmailIncorrect(){
        solo.enterText(0,"test@");
        solo.enterText(1,"abc");
        solo.clickOnText("Sign up");
        solo.sleep(1000);
        solo.assertCurrentActivity("Expected RegisterActivity", "RegisterActivity");
    }

    public void testValidatePasswordAndEmailCorrect(){
        solo.enterText(0,"test@wp.pl");
        solo.enterText(1,"abcd");
        solo.clickOnText("Sign up");
        solo.sleep(1000);
        solo.assertCurrentActivity("Expected MainActivity", "MainActivity");
    }
}
