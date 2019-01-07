package app.cooka.cookapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

/**
 * This activity exists to hold the different profile related fragments
 */
public class ProfileActivity extends AppCompatActivity {

    private long mUserid;

    public ProfileActivity(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Bundle b = getIntent().getExtras();
        if(b != null)
            mUserid = b.getLong("userid");

        Log.d("COOKALOG", "ProfileActivity userid: " + String.valueOf(mUserid));
    }
}
