package app.cooka.cookapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

/**
 * This activity exists to hold the different profile related fragments
 */
public class ProfileActivity extends AppCompatActivity {

    public static final String USERID = "userid";

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

    public static void startProfile(final Context context, final long userid){
        Bundle b = new Bundle();
        b.putLong(USERID, userid);
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtras(b);
        context.startActivity(intent);
    }
}
