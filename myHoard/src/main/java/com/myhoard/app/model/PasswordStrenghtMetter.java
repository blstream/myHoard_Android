package com.myhoard.app.model;

import android.text.Editable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created by Mateusz Czyszkiewicz on 2014-05-04.
 */
public class PasswordStrenghtMetter {



    public int passwordRanking(Editable password)
    {

        Pattern numPatt = Pattern.compile("\\d+");
        Pattern specialPattern = Pattern.compile("[@#$%^&+=]");
        Pattern bigLetterPattern = Pattern.compile("[A-Z]");
        Matcher matcher = numPatt.matcher(password);
        Matcher matcher1 = specialPattern.matcher(password);
        Matcher matcher2 = bigLetterPattern.matcher(password);
        int passwordStrenght = 0 ;

        if(password.length() >= 4) {
            passwordStrenght++;
            if (matcher.find()) {
                passwordStrenght++;
            }
            if (matcher1.find()) {
                passwordStrenght++;
            }
            if (matcher2.find()) {
                passwordStrenght++;
            }
        }
        return passwordStrenght;
    }
}
