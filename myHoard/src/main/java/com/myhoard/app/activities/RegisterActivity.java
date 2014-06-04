package com.myhoard.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.myhoard.app.Managers.UserManager;
import com.myhoard.app.R;
import com.myhoard.app.crudengine.ConnectionDetector;
import com.myhoard.app.model.PasswordStrenghtMetter;
import com.myhoard.app.model.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
Created by Mateusz Czyszkiewicz , modified by Marcin Laszcz, Tomasz Nosal
*/
public class RegisterActivity extends BaseActivity {


    private static final String TAG = "REGISTRATION";
    private EditText emailRegistry;
    private EditText passwordRegistry;
    private TextView password_strength;
    private TextView txt;
    private TextView textViewEmailAlreadyExists;
    private RegisterUser register;

    private static final int LOW = 1;
    private static final int NICE = 2;
    private static final int OK = 3;
    private static final int EPIC = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailRegistry = (EditText) findViewById(R.id.email_register);
        emailRegistry.addTextChangedListener(emailWatcher);
        passwordRegistry = (EditText) findViewById(R.id.password_register);
        Button registryButton = (Button) findViewById(R.id.reg_button);
        passwordRegistry.addTextChangedListener(PasswordEditorMatcher);
        password_strength = (TextView) findViewById(R.id.passwordStrenghtText);
        textViewEmailAlreadyExists = (TextView) findViewById(R.id.textViewEmailAlreadyExists);
        txt = (TextView)findViewById(R.id.NoInternetTextView);

        registryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (register!=null)
            register.cancel(true);
    }

    public void registerUser() {

        ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
        if(!cd.isConnectingToInternet())
        {
            txt.setText(getString(R.string.no_internet_connection));
        }
        else {
            boolean passwordFound = validatePassword();
            boolean emailFound = validateEmail();

            if (!emailFound ) {

                emailRegistry.setError(getString(R.string.wrong_email_format));
            }
            if (!passwordFound) {

                passwordRegistry.setError(getString(R.string.password_information));
            }

            if (passwordFound && emailFound) {
                User user = new User();
                user.setEmail(String.valueOf(emailRegistry.getText()).toLowerCase());
                user.setPassword(String.valueOf(passwordRegistry.getText()));

                register = new RegisterUser();
                register.user = user;
                register.activity = this;
                register.execute();
            }
        }
    }

    TextWatcher emailWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            CharSequence warningText = textViewEmailAlreadyExists.getText();
            if (warningText != null) {
                String warning = textViewEmailAlreadyExists.getText().toString();
                if (warning.length() > 0) {
                    textViewEmailAlreadyExists.setText("");
                }
            }
        }
    };


    public boolean validatePassword() {
        String password = String.valueOf(passwordRegistry.getText());


        Pattern pattern = Pattern.compile(getString(R.string.password_pattern));
        Matcher matcher = pattern.matcher(password);


        if (!matcher.matches()) Log.d(TAG,getString(R.string.invalid_mail));
        return matcher.matches();
    }

    public boolean validateEmail() {
        String email = String.valueOf(emailRegistry.getText()).toLowerCase();
        Pattern p = Pattern.compile((getString(R.string.email_pattern)));
        Matcher m = p.matcher(email);

        if (!m.matches()) Log.d(TAG,getString(R.string.invalid_password));
        return m.matches();
    }

    private class RegisterUser extends AsyncTask<Void, Void, Boolean> {

        public User user;
        public Activity activity;
        private String warning;

        @Override
        protected Boolean doInBackground(Void... params) {
            if (UserManager.getInstance().getIp() == null)
            UserManager.getInstance().setIp(getString(R.string.serverJava2));
            try {
                UserManager.getInstance().register(user);
                return UserManager.getInstance().login(user);
            } catch (RuntimeException e) {
                warning = e.getMessage();
                return false;
            }
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(getBaseContext(), getString(R.string.registration_successfully), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(activity, MainActivity.class);
                startActivity(intent);
                activity.finish();
            } else if (warning.equals(getString(R.string.no_internet_connection))) {
                txt.setText(getString(R.string.no_internet_connection));
            } else {
                textViewEmailAlreadyExists.setText(getString(R.string.email_already_exists));
            }
        }
    }

        private final TextWatcher PasswordEditorMatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                password_strength.setText("Empty");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                PasswordStrenghtMetter psm = new PasswordStrenghtMetter();
                int passwordStrenght = psm.passwordRanking(s);

                switch (passwordStrenght) {

                    case LOW: {

                        password_strength.setTextColor(getResources().getColor(R.color.orange));
                        password_strength.setText("low");

                        break;
                    }
                    case NICE:
                    {
                        password_strength.setTextColor(getResources().getColor(R.color.yellow_text));
                        password_strength.setText("nice");
                        break;
                    }
                    case OK:
                    {
                        password_strength.setTextColor(getResources().getColor(R.color.green));
                        password_strength.setText("ok");
                        break;
                    }
                    case EPIC:
                    {
                        password_strength.setTextColor(getResources().getColor(R.color.green));
                        password_strength.setText("Epic");
                        break;
                    }

                    default:
                        password_strength.setTextColor(getResources().getColor(R.color.red));
                        password_strength.setText("too short");

                        break;
                }
            }

        };


    }
