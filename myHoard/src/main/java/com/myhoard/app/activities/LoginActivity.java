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
import com.myhoard.app.crudengine.ConnectionDetector;
import com.myhoard.app.model.User;
import com.myhoard.app.Managers.UserManager;

/*
* Crreated by Mateusz Czyszkiewicz
*/
public class LoginActivity extends ActionBarActivity  {

	private EditText email;
	private EditText password;



    private final static String SAVELOGIN = "saveLogin";
    private final static String LOGINPREFS = "loginPrefs";
    private final static String USERNAMES = "username";
    private final static String PASSWORDS = "password";
	private CheckBox remember_check;
	private SharedPreferences.Editor editor;

    private User user;

	@Override
    /* AWA:FIXME: Ciało metody jest za dlugie.
    Mozna je podzielic na "krótsze" metody
    Patrz:Ksiazka:Czysty kod:Rozdział 3:Funkcje
    */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		email = (EditText) findViewById(R.id.login_email);
		password = (EditText) findViewById(R.id.login_password);
		Button login_button = (Button) findViewById(R.id.button_login);
		TextView txt = (TextView) findViewById(R.id.registration_text);
		remember_check = (CheckBox) findViewById(R.id.checkbox_remember);
		SharedPreferences loginPreferences = getSharedPreferences(LOGINPREFS, MODE_PRIVATE);
		editor = loginPreferences.edit();

		Boolean saveLogin = loginPreferences.getBoolean(SAVELOGIN, false);
    	if (saveLogin) {
			email.setText(loginPreferences.getString(USERNAMES, ""));
			password.setText(loginPreferences.getString(PASSWORDS, ""));
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

                    /* AWA:FIXME: AsyncTask canceling
                    Uruchamiany jest w tym miejscu AsyncTask
                    Jednak brak jest jego anulowania.
                    Co gdy uzytkownik opusci to acitivyt przez przycisk BACK?
                    AsyncTask dalej będzie w tle pracował.
                    */
                    new getUserSingleton().execute();



                        if (remember_check.isChecked()) {

                            editor.putBoolean(SAVELOGIN, true);
                            editor.putString(USERNAMES, email_ch);
                            editor.putString(PASSWORDS, password_ch);
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
		});

	}



    private class getUserSingleton extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            return UserManager.getInstance().login(user);
        }

        protected void onPostExecute(Boolean result) {
            if (result) {


                Toast toast = Toast.makeText(getBaseContext(), getString(R.string.Logged),
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
                finish();
            }
            else {
                TextView incorrectData = (TextView) findViewById(R.id.incorrect_data);
               incorrectData.setText(getString(R.string.email_or_password_incorrect));
            }
        }
    }
}
