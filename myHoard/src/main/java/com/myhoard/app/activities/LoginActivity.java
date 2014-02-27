package com.myhoard.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.myhoard.app.R;

public class LoginActivity extends ActionBarActivity {

    private EditText email;
    private EditText password;
    private Button login_button;
    private TextView txt;
    private String login_fix = "janek";
    private String password_fix = "1234a";
    private CheckBox remember_check;
    private String username, user_password;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor editor;
    private Boolean saveLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        email = (EditText) findViewById(R.id.login_email);
        password = (EditText) findViewById(R.id.login_password);
        login_button = (Button) findViewById(R.id.button_login);
        txt = (TextView) findViewById(R.id.registration_text);
        remember_check = (CheckBox) findViewById(R.id.checkbox_remember);
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        editor = loginPreferences.edit();
        saveLogin = loginPreferences.getBoolean("saveLogin", false);


        if (saveLogin == true) {
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
                if (email.getText().toString().equals(login_fix) && password.getText().toString().equals(password_fix)) {
                    if (remember_check.isChecked()) {
                        username = email.getText().toString();
                        user_password = password.getText().toString();
                        editor.putBoolean("saveLogin", true);
                        editor.putString("username", username);
                        editor.putString("password", user_password);
                        editor.commit();
                    }
                    else
                    {
                        editor.clear();
                        editor.commit();
                    }
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Wrong login or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
/*
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String tmp_email = email.getText().toString();
        outState.putString("email",tmp_email);
    }*/
}
