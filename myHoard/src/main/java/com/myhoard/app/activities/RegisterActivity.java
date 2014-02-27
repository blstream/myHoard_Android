package com.myhoard.app.activities;

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

    private TextView terms;
    private EditText email_registry;
    private EditText password_registry;
    private Pattern pattern;
    private Button registry_button;
    private Matcher matcher;
    private CheckBox checkBox_registry;

    private static final String password_pattern = "( (?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{5,10} )";

            //


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        terms = (TextView)findViewById(R.id.text_term);
        email_registry = (EditText)findViewById(R.id.email_register);
        password_registry = (EditText)findViewById(R.id.password_register);
        registry_button = (Button)findViewById(R.id.reg_button);
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



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.register, menu);
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

    public void validation()
    {
        matcher = pattern.matcher(password_registry.getText().toString());

        if (matcher.matches() == true && email_registry.length() > 0 && checkBox_registry.isChecked() )
        {

            Toast.makeText(getApplicationContext(),"z" + matcher.matches() ,Toast.LENGTH_SHORT).show();
        }
        else if (email_registry.length() == 0 && password_registry.length() == 0 )
        {
            Toast.makeText(getApplicationContext(), "You cannot registry without login and password" ,Toast.LENGTH_SHORT).show();
        }
        else if (email_registry.length() == 0 )
        {
            Toast.makeText(getApplicationContext(), "Please write your email" ,Toast.LENGTH_SHORT).show();
        }
        else if (password_registry.length() == 0)
        {
            Toast.makeText(getApplicationContext(), "Please write your password" ,Toast.LENGTH_SHORT).show();
        }
        else if (!checkBox_registry.isChecked())
        {
            Toast.makeText(getApplicationContext(), "You have to accept the terms of use",Toast.LENGTH_SHORT).show();
        }



    }

}
