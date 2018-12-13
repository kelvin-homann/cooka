package app.cooka.cookapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.Arrays;

import app.cooka.cookapp.utils.SystemUtils;

public class FacebookLogInTestActivity extends AppCompatActivity {

    public static final String LOGTAG = "COOKALOG";
    private static final String READ_PERMISSIONS[] = {
        "email"
    };

    //private UserProfile userProfile;
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private TextView loginStatus;
    private AccessTokenTracker accessTokenTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_login_test);

        String systemKeyHash = SystemUtils.getSystemKeyHash(this);
        Log.d(LOGTAG, String.format("system key hash = %s", systemKeyHash));

        //userProfile = UserProfile.getInstance();
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

//                    userProfile.setUsername(name);
//                    userProfile.setEmail(email);
//                    userProfile.setProfileLocation(ELinkedProfileType.RemoteFacebook);
//                    userProfile.setLoggedIn(true);

                    Log.d(LOGTAG, "you have been logged in with your facebook account");
                    Log.d(LOGTAG, String.format("facebook user id = %s", id));
                    Log.d(LOGTAG, String.format("facebook username = %s", name));
                    Log.d(LOGTAG, String.format("facebook email address = %s", email));
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
        //userProfile.setLoggedIn(false);
        Log.d(LOGTAG, "you have been logged out");
        loginStatus.setText("You have been logged out");
    }
}
