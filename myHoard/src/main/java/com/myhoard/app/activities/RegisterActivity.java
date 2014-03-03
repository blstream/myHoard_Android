package com.myhoard.app.activities;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
    private Pattern pattern;
    private Matcher matcher;
    private CheckBox checkBox_registry;
    private static final String password_pattern = "^(?=.[a-z])(?=.[A-Z])(?=.\\d)(?=.[$@$!%?&])[A-Za-z\\d$@$!%?&]{5,}";

    // "( (?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{5,10} )";

            //


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView terms = (TextView)findViewById(R.id.text_term);
        email_registry = (EditText)findViewById(R.id.email_register);
        password_registry = (EditText)findViewById(R.id.password_register);
        Button registry_button = (Button)findViewById(R.id.reg_button);
        checkBox_registry = (CheckBox)findViewById(R.id.checkBox_reg);
        pattern = pattern.compile(password_pattern);

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

	// FIXME zamiast Toast popatrz na metodę EditText.setError()
    public void validation()
    {
        matcher = pattern.matcher(password_registry.getText().toString());

        if (matcher.matches() == true && email_registry.length() > 0 && checkBox_registry.isChecked() )
        {

            Toast.makeText(getApplicationContext(),getString(R.string.registration_succesfully),Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);
        }
        else if (email_registry.length() == 0 && password_registry.length() == 0 )
        {
            Toast.makeText(getApplicationContext(), getString(R.string.email_and_password_registration_empty) ,Toast.LENGTH_SHORT).show();
        }
        else if (email_registry.length() == 0 && !checkBox_registry.isChecked())
        {
            Toast.makeText(getApplicationContext(), getString(R.string.email_and_checkbox_registration_empty) ,Toast.LENGTH_SHORT).show();
        }
        else if (password_registry.length() == 0 && !checkBox_registry.isChecked())
        {
            Toast.makeText(getApplicationContext(), getString(R.string.password_and_checkbox_registration_empty) ,Toast.LENGTH_SHORT).show();
        }
        else if (email_registry.length() == 0 )
        {
            Toast.makeText(getApplicationContext(), getString(R.string.email_registration_empty) ,Toast.LENGTH_SHORT).show();
        }
        else if (password_registry.length() == 0)
        {
            Toast.makeText(getApplicationContext(), getString(R.string.password_registration_empty) ,Toast.LENGTH_SHORT).show();
        }
        else if (!checkBox_registry.isChecked())
        {
            Toast.makeText(getApplicationContext(), getString(R.string.checkbox_not_check_registration),Toast.LENGTH_SHORT).show();
        }
        if(!matcher.matches())
        {
            Toast.makeText(getApplicationContext(),getString(R.string.read_info_password),Toast.LENGTH_SHORT).show();
        }



    }

}
