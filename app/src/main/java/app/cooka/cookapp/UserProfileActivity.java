package app.cooka.cookapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class UserProfileActivity extends AppCompatActivity {

    public long mUserid = 0;

    public UserProfileActivity(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UserProfileFragment userProfileFragment = new UserProfileFragment();
        Bundle b = getIntent().getExtras();
        if(b != null)
            mUserid = b.getLong("userid");

        Bundle bundle = new Bundle();
        bundle.putLong("userid", mUserid);

        Log.d("COOKALOG", "UserProfileActivity userid: " + String.valueOf(mUserid));
        Log.d("COOKALOG", "UserProfileActivity bundle: " + String.valueOf(bundle));
        userProfileFragment.setArguments(bundle);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_userprofile, userProfileFragment).commit();
    }


}
