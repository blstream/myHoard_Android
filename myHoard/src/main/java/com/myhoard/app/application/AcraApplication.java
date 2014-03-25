package com.myhoard.app.application;

import android.app.Application;
import com.myhoard.app.R;
import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

/**
 * Created by Piotr Brzozowski on 08.03.14.
 * Class use to set ACRA settings
 */
@ReportsCrashes(formKey = "",
		mailTo = "pat2014-szn-android-list@blstream.com",
        customReportContent = {ReportField.APP_VERSION_CODE,ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,ReportField.BRAND,ReportField.PHONE_MODEL,
                ReportField.CUSTOM_DATA,ReportField.USER_COMMENT,ReportField.STACK_TRACE},
		mode = ReportingInteractionMode.DIALOG,
        resDialogText = R.string.action_acra)

/* AWA:FIXME:Nazwa klasy Application
Nazwa klasy powinna byc powiazana z MyHoard
np. MyHoardApplication
*/
public class AcraApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		ACRA.init(this);
	}
}
