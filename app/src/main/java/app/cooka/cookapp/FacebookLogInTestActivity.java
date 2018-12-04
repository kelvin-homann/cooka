package app.cooka.cookapp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class FacebookLogInTestActivity extends AppCompatActivity {

    public static final String LOGTAG = "COOKALOG";
    private static final String READ_PERMISSIONS[] = {
        "email"
    };

    private UserProfile userProfile;
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private TextView loginStatus;
    private AccessTokenTracker accessTokenTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_login_test);

        String systemKeyHash = getSystemKeyHash(this);
        Log.d(LOGTAG, String.format("system key hash = %s", systemKeyHash));

        userProfile = UserProfile.getInstance();
        callbackManager = CallbackManager.Factory.create();

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

        loginStatus = (TextView)findViewById(R.id.tvLoginStatus);
        if(isLoggedIn)
            executePostLogin(accessToken);
        else
            loginStatus.setText("Please log in");

        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("email"));

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {}
            @Override
            public void onCancel() {}
            @Override
            public void onError(FacebookException exception) {
                Log.e(LOGTAG, "Error during login");
            }
        });

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                // if just logged in
                if(oldAccessToken == null && currentAccessToken != null) {
                    executePostLogin(currentAccessToken);
                }
                // if just logged out
                else if(oldAccessToken != null && currentAccessToken == null) {
                    executePostLogout(oldAccessToken);
                }
            }
        };

        accessTokenTracker.startTracking();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * execute post login steps like polling remote profile parameters, setting up the user profile and so on
     * @param currentAccessToken
     */
    private void executePostLogin(AccessToken currentAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(currentAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse response) {
                if(response.getError() != null) {
                    // handle error
                }
                else {
                    // get email and id of the user
                    String name = jsonObject.optString("name");
                    String email = jsonObject.optString("email");
                    String id = jsonObject.optString("id");

                    userProfile.setUsername(name);
                    userProfile.setEmail(email);
                    userProfile.setProfileLocation(EProfileLocation.RemoteFacebook);
                    userProfile.setLoggedIn(true);

                    Log.d(LOGTAG, String.format("you have been logged in as %s", name));
                    String loginMessage = String.format("You have been logged in as %s\n\nYour e-mail address is %s", name, email);
                    loginStatus.setText(loginMessage);
                }
            }
        });

        Bundle requestParameters = new Bundle();
        requestParameters.putString("fields", "id,name,email");
        request.setParameters(requestParameters);
        request.executeAsync();
    }

    /**
     * executes post logout steps
     * @param oldAccessToken
     */
    private void executePostLogout(AccessToken oldAccessToken) {
        userProfile.setLoggedIn(false);
        Log.d(LOGTAG, "you have been logged out");
        loginStatus.setText("You have been logged out");
    }

    /**
     * gets a Android Studio instance specific key hash that during development is required for social sign in services such as Facebook
     * @param context
     * @return the Android Studio instance specific key hash to be registered with the social sign in service for developmental access; returns null if an error occurred
     */
    public static String getSystemKeyHash(Activity context) {
        PackageInfo packageInfo;
        String key = null;
        try {
            String packageName = context.getApplicationContext().getPackageName();
            packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);

            for(Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                key = new String(Base64.encode(md.digest(), 0));
            }
        }
        catch(PackageManager.NameNotFoundException e) {
            Log.e("Name not found", e.toString());
        }
        catch(NoSuchAlgorithmException e) {
            Log.e("No such algorithm", e.toString());
        }
        catch(Exception e) {
            Log.e("Exception", e.toString());
        }

        return key;
    }
}
