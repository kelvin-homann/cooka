package app.cooka.cookapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceManagerFix;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etvUsername;
    private TextInputEditText etvPassword;
    private Button loginButton;
    private LoginButton fbLoginButton;
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private MediaPlayer mp; // To play errorsound
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Get SharedPreferences
        settings = PreferenceManagerFix.getDefaultSharedPreferences(this);

        // Init Objects from Layout
        etvUsername = findViewById(R.id.etvEmailLogin);
        etvPassword = findViewById(R.id.etvPasswordLogin);
        loginButton = findViewById(R.id.btnUserLogin);
        fbLoginButton = findViewById(R.id.btnFacebookLogin);
        final TextInputLayout tilEmail = findViewById(R.id.tilEmailLogin);
        final TextInputLayout tilPassword = findViewById(R.id.tilPasswordLogin);

        final Activity activity = this;

        mp = MediaPlayer.create(this, R.raw.errorsound);

        // TextWatcher for Email EditText
        TextWatcher textWatchEmail = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,int count) {
                if (!StringUtils.isValidUserName(etvUsername.getText().toString())){
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
        etvUsername.addTextChangedListener(textWatchEmail);

        // TextWatcher for Password EditText
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
                if (etvUsername.getText().toString().isEmpty()){
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
                        .login(etvUsername.getText().toString(), etvPassword.getText().toString(), new ILoginCallback() {
                            @Override
                            public void onSucceeded(AuthenticateUserResult result) {
                                tilEmail.setErrorEnabled(false);
                                tilPassword.setErrorEnabled(false);
                                // If Tutorial Enabled in the Settings tutorial is started at the start of the app
                                if (tutorialIsEnabled()){
                                    Intent intent_tutorial = new Intent(getApplicationContext(), TutorialActivity.class);
                                    startActivity(intent_tutorial);
                                }

                                else{
                                    Intent intent_main = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent_main);
                                }
                                finish();
                            }

                            @Override
                            public void onFailed(int errorCode, String errorMessage, Throwable t) {
                                if(soundIsEnabled())
                                    mp.start();
                                vibrate(activity,250, 10);
                                if (!etvUsername.getText().toString().isEmpty()){
                                    tilEmail.setErrorEnabled(true);
                                    tilEmail.setError(getString(R.string.login_invalid));
                                }
                                else if(!etvPassword.getText().toString().isEmpty()){
                                    tilPassword.setErrorEnabled(true);
                                    tilPassword.setError(getString(R.string.login_invalid));
                                }
                            }
                        });
            }
        });

        // Facebook Login Button
        callbackManager = CallbackManager.Factory.create();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

        if(isLoggedIn)
            executePostLogin(accessToken);

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

    public void vibrate(Activity activity, int duration, int amplitude){
        if (Build.VERSION.SDK_INT >= 26) {
            ((Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(duration, amplitude));
            Log.d("COOKALOG", "vibrate()");
        } else {
            ((Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(duration);
            Log.d("COOKALOG", "vibrate()");
        }
    }

    public boolean soundIsEnabled(){
        return settings.getBoolean("soundCheckBox", true);
    }

    public boolean tutorialIsEnabled(){
        return settings.getBoolean("tutorialCheckBox", true);
    }
}
