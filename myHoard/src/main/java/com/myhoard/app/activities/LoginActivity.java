package com.myhoard.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.myhoard.app.R;
import com.myhoard.app.httpengine.ConnectionDetector;
import com.myhoard.app.httpengine.UserHttpEngine;
import com.myhoard.app.model.Token;
import com.myhoard.app.model.User;
import com.myhoard.app.model.UserSingleton;

public class LoginActivity extends ActionBarActivity {

	private EditText email;
	private EditText password;

	private String login_fix = "jan";
	private String password_fix = "aA12#";
	private CheckBox remember_check;
	private SharedPreferences.Editor editor;

    User user;

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

                ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
                if (cd.isConnectingToInternet()) {
                    String email_ch = String.valueOf(email.getText());
                    String password_ch = String.valueOf(password.getText());

                    user = new User();
                    user.setUsername(email_ch);
                    user.setPassword(password_ch);
                    new getUserSingleton().execute();


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
                        //new LoginApi().execute(email_ch,password_ch,getUsername(email_ch));
                        //Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        //startActivity(intent);


                    } else {
                        if (!email_ch.equals(login_fix)) {
                            //email.setError(getString(R.string.incoreect_login));
                        }
                        if (!password_ch.equals(password_fix)) {
                            //password.setError(getString(R.string.wrong_password));
                        }

                    }
                } else {
                    TextView incorrectData = (TextView) findViewById(R.id.incorrect_data);
                    incorrectData.setText("YOU DON'T HAVE INTERNET CONNECTION");
                }
            }
		});

	}

    private String getUsername(String email)
    {
        String[]tab = email.split("@");
        String username;
        return username = tab[0];
    }



    private class getUserSingleton extends AsyncTask<Void, Void, Token> {

        User u;

        @Override
        protected Token doInBackground(Void... params) {
            UserHttpEngine userHttpEngine = new UserHttpEngine("http://78.133.154.18:8080/oauth/token/");
            //return userHttpEngine.getAuthenticationCode(user);
            return userHttpEngine.getToken(user);
        }

        protected void onPostExecute(Token token) {
            if (token != null) {
                UserSingleton userS = UserSingleton.getInstance();
                userS.user = user;
                userS.token = token;

                Toast toast = Toast.makeText(getApplicationContext(), "Zalogowano",
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
                finish();
            }
            else {
                TextView incorrectData = (TextView) findViewById(R.id.incorrect_data);
                incorrectData.setText("E-MAIL OR PASSWORD IS INCORRECT");
            }
        }
    }
}
