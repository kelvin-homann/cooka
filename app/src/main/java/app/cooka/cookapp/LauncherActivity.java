package app.cooka.cookapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManagerFix;
import android.util.Log;

import app.cooka.cookapp.login.LoginManager;

public class LauncherActivity extends AppCompatActivity {
    // Settings
    SharedPreferences settings;

    // LoginManager
    private LoginManager loginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        Log.d("COOKALOG", "LauncherActivity started");

        // Get SharedPreferences
        settings = PreferenceManagerFix.getDefaultSharedPreferences(this);

        // Login Manager
        loginManager = LoginManager.Factory.getInstance(getApplicationContext());

        // If no user is logged in the registerFragment is started
        if (!isLoggedIn()){
            Log.d("COOKALOG", "User is not logged in");
            Intent intent_register = new Intent(this, RegisterActivity.class);
            startActivity(intent_register);
        }

        // if user is logged
        else{
            // if tutorial is enabled
            if (tutorialIsEnabled()){
                Intent intent_tutorial = new Intent(this, TutorialActivity.class);
                startActivity(intent_tutorial);
            }
            // if user is already logged in and the tutorial is disabled
            else{
                Intent intent_main = new Intent(this, MainActivity.class);
                startActivity(intent_main);
            }
        }

        finish();
    }

    /**
     * if no user is logged in it returns false, if someone is logged in it returns true
     * @return boolean
     */
    public boolean isLoggedIn(){
        Log.d("COOKALOG", "isLoggedIn(): " + String.valueOf(loginManager.getUserId() != 0L));
        return loginManager.getUserId() != 0L;
    }

    /**
     * If the tutorial is enabled in the settings it returns true, when its disable it returns false
     * @return boolean
     */
    public boolean tutorialIsEnabled(){
        return settings.getBoolean("tutorialCheckBox", true);
    }
}
