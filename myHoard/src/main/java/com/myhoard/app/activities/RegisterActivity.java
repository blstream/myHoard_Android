package com.myhoard.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.myhoard.app.R;
import com.myhoard.app.model.User;
import com.myhoard.app.model.UserManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO add username text field
    /*
    Created by Mateusz Czyszkiewicz , modified by Tomasz Nosal
    */
public class RegisterActivity extends ActionBarActivity {


    private static final String TAG = "REGISTRATION";
    private EditText emailRegistry;
    private EditText passwordRegistry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailRegistry = (EditText) findViewById(R.id.email_register);
        passwordRegistry = (EditText) findViewById(R.id.password_register);
        Button registryButton = (Button) findViewById(R.id.reg_button);

        registryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    public void registerUser() {
        boolean passwordFound = validatePassword();
        boolean emailFound = validateEmail();
        if (!emailFound) {

            emailRegistry.setError(getString(R.string.wrong_email_format));
        }
        if (!passwordFound) {

            passwordRegistry.setError(getString(R.string.password_information));
        }


        if (passwordFound && emailFound) {
            User user = new User();
            user.setEmail(String.valueOf(emailRegistry.getText()));
            user.setUsername(String.valueOf(emailRegistry.getText())); //TODO replace with value from username text field that will be added in near future
            user.setPassword(String.valueOf(passwordRegistry.getText()));


            /* AWA:FIXME: Niebezpieczne używanie wątku
        Brak anulowania tej operacji.
        Wyjście z Activity nie kończy wątku,
        należy o to zadbać.
        */
            RegisterUser register  = new RegisterUser();
            register.user = user;
            register.activity = this;
            register.execute();
        }
    }

    public boolean validatePassword() {
        String password = String.valueOf(passwordRegistry.getText());


        Pattern pattern = Pattern.compile(getString(R.string.password_pattern));
        Matcher matcher = pattern.matcher(password);


        if (!matcher.matches()) Log.d(TAG,getString(R.string.invalid_mail));
        return matcher.matches();
    }

    public boolean validateEmail() {
        String email = String.valueOf(emailRegistry.getText());
        Pattern p = Pattern.compile((getString(R.string.email_pattern)));
        Matcher m = p.matcher(email);

        if (!m.matches()) Log.d(TAG,getString(R.string.invalid_password));
        return m.matches();
    }

    private class RegisterUser extends AsyncTask<Void, Void, Boolean> {

        public User user;
        public Activity activity;

        @Override
        protected Boolean doInBackground(Void... params) {
            return UserManager.getInstance().register(user);
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(getBaseContext(), getString(R.string.registration_succesfully), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(activity, MainActivity.class);
                startActivity(intent);
                activity.finish();

                Log.d(TAG,"finished");
                return;
            }

            Log.d(TAG,"error");
        }
    }
}
