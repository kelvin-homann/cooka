package app.cooka.cookapp;


import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

import app.cooka.cookapp.login.ILoginCallback;
import app.cooka.cookapp.login.LoginManager;
import app.cooka.cookapp.model.AuthenticateUserResult;
import app.cooka.cookapp.utils.StringUtils;
import de.hdodenhof.circleimageview.CircleImageView;


public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etvEmail;
    private TextInputEditText etvPassword;
    private Button loginButton;
    private LoginButton fbLoginButton;
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    private boolean etvEmailReset = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etvEmail = (TextInputEditText)findViewById(R.id.etvEmailLogin);
        etvPassword = (TextInputEditText)findViewById(R.id.etvPasswordLogin);
        loginButton = (Button)findViewById(R.id.btnUserLogin);
        fbLoginButton = (LoginButton)findViewById(R.id.btnFacebookLogin);
        final TextInputLayout tilEmail = (TextInputLayout) findViewById(R.id.tilEmailLogin);
        final TextInputLayout tilPassword = (TextInputLayout) findViewById(R.id.tilPasswordLogin);

        TextWatcher textWatchEmail = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,int count) {
                if (!StringUtils.isValidEmailAddress(etvEmail.getText().toString()) && !StringUtils.isValidUserName(etvEmail.getText().toString())){
                    tilEmail.setErrorEnabled(true);
                    tilEmail.setError(getString(R.string.error_email_login));
                }
                else {
                    tilEmail.setErrorEnabled(false);
                }
            }

            public void afterTextChanged(Editable s) {
            }
        };
        etvEmail.addTextChangedListener(textWatchEmail);

        TextWatcher textWatchPassword = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,int count) {
                if (!StringUtils.isValidPassword(etvPassword.getText().toString())){
                    tilPassword.setErrorEnabled(true);
                    tilPassword.setError(getString(R.string.error_password_login));
                }
                else {
                    tilPassword.setErrorEnabled(false);
                }
            }

            public void afterTextChanged(Editable s) {
            }
        };
        etvPassword.addTextChangedListener(textWatchPassword);

        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (etvEmail.getText().toString().isEmpty()){
                    tilEmail.setErrorEnabled(true);
                    tilEmail.setError(getString(R.string.error_field_required));
                }
                else {
                    tilEmail.setErrorEnabled(false);
                }
                if (etvPassword.getText().toString().isEmpty()){
                    tilPassword.setErrorEnabled(true);
                    tilPassword.setError(getString(R.string.error_field_required));
                }
                else {
                    tilPassword.setErrorEnabled(false);
                }

                LoginManager.Factory.getInstance(getApplicationContext())
                        .login(etvEmail.getText().toString(), etvPassword.getText().toString(), new ILoginCallback() {
                            @Override
                            public void onSucceeded(AuthenticateUserResult result) {
                                Toast.makeText(getApplicationContext(), "Successfully logged in", Toast.LENGTH_SHORT).show();
                                tilEmail.setErrorEnabled(false);
                                tilPassword.setErrorEnabled(false);
                            }

                            @Override
                            public void onFailed(int errorCode, String errorMessage, Throwable t) {
                                if (errorCode == 11){ // Invalid Password and Username
                                    if (!etvEmail.getText().toString().isEmpty()){
                                        tilEmail.setErrorEnabled(true);
                                        tilEmail.setError(getString(R.string.login_invalid));
                                    }
                                    else if(!etvPassword.getText().toString().isEmpty()){
                                        tilPassword.setErrorEnabled(true);
                                        tilPassword.setError(getString(R.string.login_invalid));
                                    }

                                }
                                else if (errorCode == 12){ // Invalid Password
                                    if (!etvEmail.getText().toString().isEmpty()){
                                        tilEmail.setErrorEnabled(true);
                                        tilEmail.setError(getString(R.string.login_invalid));
                                    }
                                    else if(!etvPassword.getText().toString().isEmpty()){
                                        tilPassword.setErrorEnabled(true);
                                        tilPassword.setError(getString(R.string.login_invalid));
                                    }
                                }
                            }
                        });
            }
        });

        // Facebook stuff

        callbackManager = CallbackManager.Factory.create();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

        if(isLoggedIn)
            executePostLogin(accessToken);
        else
            Log.e("COOKALOG", "Please log in");

        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {}
            @Override
            public void onCancel() {}
            @Override
            public void onError(FacebookException exception) {
                Log.e("COOKALOG", "Error during login");
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

                    Log.d("COOKALOG", "you have been logged in with your facebook account");
                    Log.d("COOKALOG", String.format("facebook user id = %s", id));
                    Log.d("COOKALOG", String.format("facebook username = %s", name));
                    Log.d("COOKALOG", String.format("facebook email address = %s", email));
                    String loginMessage = String.format("You have been logged in as %s\n\nYour e-mail address is %s", name, email);
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
        Log.d("COOKALOG", "you have been logged out");
    }
}
