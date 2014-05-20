package com.myhoard.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.myhoard.app.Managers.UserManager;
import com.myhoard.app.R;
import com.myhoard.app.crudengine.ConnectionDetector;
import com.myhoard.app.model.User;

/*
* Crreated by Mateusz Czyszkiewicz
*/
public class LoginActivity extends BaseActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;

    private final static String SAVELOGIN = "saveLogin";
    private final static String LOGINPREFS = "loginPrefs";
    private final static String USERNAMES = "username";
    private final static String PASSWORDS = "password";
    private final static String CHECK = "checked";
    private CheckBox remember_check;
    private SharedPreferences.Editor editor;
    private Button login_button;
    private TextView txt;
    private User user;

    GetUserSingleton getUserSingleton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setVariables();

        SharedPreferences loginPreferences = getSharedPreferences(LOGINPREFS, MODE_PRIVATE);
        editor = loginPreferences.edit();

        Boolean saveLogin = loginPreferences.getBoolean(SAVELOGIN, false);
        if (saveLogin) {
            editTextEmail.setText(loginPreferences.getString(USERNAMES, ""));
            editTextPassword.setText(loginPreferences.getString(PASSWORDS, ""));
            remember_check.setChecked(true);
        }


        txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registration_activity();
            }
        });

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registry();
            }
        });
    }


    public void setVariables() {
        editTextEmail = (EditText) findViewById(R.id.login_email);
        editTextPassword = (EditText) findViewById(R.id.login_password);
        login_button = (Button) findViewById(R.id.button_login);
        txt = (TextView) findViewById(R.id.registration_text);
        remember_check = (CheckBox) findViewById(R.id.checkbox_remember);
    }

    public void registration_activity() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    public void registry() {
        ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
        if (cd.isConnectingToInternet()) {

            String email = String.valueOf(editTextEmail.getText());
            String password = String.valueOf(editTextPassword.getText());

            user = new User();
            user.setEmail(email);
            user.setPassword(password);

            getUserSingleton = new GetUserSingleton();
            getUserSingleton.execute();


            if (remember_check.isChecked()) {

                editor.putBoolean(SAVELOGIN, true);
                editor.putString(USERNAMES, email);
                editor.putString(PASSWORDS, password);
                editor.putBoolean(CHECK, true);
                editor.commit();
            } else {
                editor.clear();
                editor.commit();
            }

        } else {
            TextView incorrectData = (TextView) findViewById(R.id.incorrect_data);
            incorrectData.setText(getString(R.string.no_internet_connection));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (getUserSingleton != null)
            getUserSingleton.cancel(true);
    }

    private class GetUserSingleton extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            UserManager.getInstance().setIp(getString(R.string.serverJava2));
            return UserManager.getInstance().login(user);
        }

        protected void onPostExecute(Boolean result) {
            if (result) {

                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                Toast toast = Toast.makeText(getBaseContext(), getString(R.string.Logged),
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
                finish();
            } else {
                TextView incorrectData = (TextView) findViewById(R.id.incorrect_data);
                incorrectData.setText(getString(R.string.email_or_password_incorrect));
            }
        }
    }
}
