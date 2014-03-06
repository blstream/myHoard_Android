package com.myhoard.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.myhoard.app.R;

public class LoginActivity extends ActionBarActivity {

    private EditText email;
    private EditText password;

    private String login_fix = "jan" ;
    private String password_fix = "aA12#";
    private CheckBox remember_check;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = (EditText) findViewById(R.id.login_email);
        password = (EditText) findViewById(R.id.login_password);
        Button login_button = (Button) findViewById(R.id.button_login);
        TextView txt = (TextView) findViewById(R.id.registration_text);
        remember_check = (CheckBox) findViewById(R.id.checkbox_remember);
        SharedPreferences loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        editor = loginPreferences.edit();
        Boolean saveLogin = loginPreferences.getBoolean("saveLogin", false);


        if (saveLogin) {
            email.setText(loginPreferences.getString("username", ""));
            password.setText(loginPreferences.getString("password", ""));
        }
        txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email_ch = String.valueOf(email.getText());
                String password_ch = String.valueOf(password.getText());
                if (email_ch.equals(login_fix) && password_ch.equals(password_fix)) {
                    if (remember_check.isChecked()) {

                        editor.putBoolean("saveLogin", true);
                        editor.putString("username", email_ch);
                        editor.putString("password", password_ch);
                        editor.commit();
                    } else {
                        editor.clear();
                        editor.commit();
                    }
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);

                } else {
                    if (!email_ch.equals(login_fix)) {
                        email.setError(getString(R.string.incoreect_login));
                    }
                    if (!password_ch.equals(password_fix)) {
                        password.setError(getString(R.string.wrong_password));
                    }

                }
            }
        });

    }


}
