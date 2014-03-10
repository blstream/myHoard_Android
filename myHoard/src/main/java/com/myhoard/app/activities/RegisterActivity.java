package com.myhoard.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.myhoard.app.R;
import com.myhoard.app.fragments.TermsFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends ActionBarActivity {


	private EditText email_registry;
	private EditText password_registry;
	private CheckBox checkBox_registry;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		TextView terms = (TextView) findViewById(R.id.text_term);
		email_registry = (EditText) findViewById(R.id.email_register);
		password_registry = (EditText) findViewById(R.id.password_register);
		Button registry_button = (Button) findViewById(R.id.reg_button);
		checkBox_registry = (CheckBox) findViewById(R.id.checkBox_reg);


		registry_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				validation();
			}
		});

		terms.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				FragmentManager fm = getSupportFragmentManager();
				TermsFragment tf = new TermsFragment();
				tf.show(fm, "TAG");
			}
		});

	}


	public void validation() {


		boolean password_found = password_check();
		boolean email_found = email_check();
		if (!email_found) {
			email_registry.setError(getString(R.string.wrong_email_format));
		}
		if (!password_found) {
			password_registry.setError(getString(R.string.password_information));
		}
		if (!checkBox_registry.isChecked()) {
			checkBox_registry.setError(getString(R.string.checkbox_not_check_registration));
		}

		Log.d("appka", "w" + password_found);
		Log.d("appka", "k" + email_found);


		if (password_found && email_found && checkBox_registry.isChecked()) {

			Toast.makeText(getBaseContext(), getString(R.string.registration_succesfully), Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(this, LoginActivity.class);

			startActivity(intent);
		}
	}

	/*
	      *password check with regexp
	     */
	public boolean password_check() {
		String password_ch = String.valueOf(password_registry.getText());
		Pattern pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{5,}$");
		Matcher matcher = pattern.matcher(password_ch);
		return matcher.matches();
	}

	/*
	      *Email check with regexp
	      */
	public boolean email_check() {


		String email_ch = String.valueOf(email_registry.getText());
		Pattern p = Pattern.compile((".+@.+\\.[a-z]+"));
		Matcher m = p.matcher(email_ch);
		return m.matches();
	}

}
