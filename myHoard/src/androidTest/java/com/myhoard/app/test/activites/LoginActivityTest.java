package com.myhoard.app.test.activites;


import android.app.Activity;
import android.content.Intent;
import android.test.ActivityUnitTestCase;

import com.myhoard.app.activities.LoginActivity;

/*
* Created by Mateusz Czyszkiewicz on 2014-03-25.
*/
public class LoginActivityTest extends ActivityUnitTestCase<LoginActivity> {

    private static final String TAG = "LoginActivityTest";

    public LoginActivityTest(Class<LoginActivity> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LoginActivity app = new LoginActivity();
        setApplication(app);

    }

    private void setApplication(LoginActivity app) {
    }

    public void testPreconditions()
    {
        startActivity(new Intent(getInstrumentation().getTargetContext(),LoginActivity.class),null,null);
        Activity activity = getActivity();
       // assertNotNull(activity.findViewById(R.id.));
    }
}
