package app.cooka.cookapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import app.cooka.cookapp.login.ILogoutCallback;
import app.cooka.cookapp.login.LoginManager;
import app.cooka.cookapp.model.InvalidateLoginResult;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void prefLogout(View view) {
        LoginManager.Factory.getInstance(getApplicationContext()).logout(true,
                new ILogoutCallback() {
                    @Override
                    public void onSucceeded(InvalidateLoginResult result) {
                        Log.d("COOKALOG", "the stored login information has been invalidated");
                        Log.d("COOKALOG", "the user has been logged out");
                        Toast.makeText(getApplicationContext(), "You have been logged out", Toast.LENGTH_SHORT).show();
                    }

                    @Override public void onFailed(int errorCode, String errorMessage, Throwable t) {
                        Log.d("COOKALOG", "Couldnt logout");
                    }
                });
    }
}
