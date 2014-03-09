package com.myhoard.app.application;

import android.app.Application;

import com.myhoard.app.R;

import org.acra.*;
import org.acra.annotation.*;

/**
 * Created by Piotr Brzozowski on 08.03.14.
 * Class use to set ACRA settings
 */
@ReportsCrashes(formKey = "",
        mailTo = "pat2014-szn-android-list@blstream.com",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.action_acra)

public class AcraApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }
}
