package app.cooka.cookapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.preference.PreferenceManagerFix;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import app.cooka.cookapp.login.ICreateAccountCallback;
import app.cooka.cookapp.login.ILoginCallback;
import app.cooka.cookapp.login.LoginManager;
import app.cooka.cookapp.model.AuthenticateUserResult;
import app.cooka.cookapp.model.CreateUserResult;
import app.cooka.cookapp.model.User;
import app.cooka.cookapp.utils.StringUtils;
import app.cooka.cookapp.utils.SystemUtils;

import static android.support.v4.content.ContextCompat.getSystemService;

public class RegisterFragment extends Fragment {

    private TextInputEditText etvEmail;
    private TextInputEditText etvUsername;
    private TextInputEditText etvPassword1;
    private TextInputEditText etvPassword2;
    private TextView tvwRegistertoLogin;
    private LoginButton btnfacebookRegister;
    private Button btnRegister;

    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    SharedPreferences settings;
    MediaPlayer mp;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Get Preferences
        settings = PreferenceManagerFix.getDefaultSharedPreferences(getContext());

        // Error Sound
        mp = MediaPlayer.create(getActivity(), R.raw.errorsound);

        etvEmail = view.findViewById(R.id.etvEmailRegister);
        etvUsername = view.findViewById(R.id.etvUsernameRegister);
        etvPassword1 = view.findViewById(R.id.etvPasswordRegister);
        etvPassword2 = view.findViewById(R.id.etvPasswordRegister2);
        tvwRegistertoLogin = view.findViewById(R.id.tvwRegistertoLogin);
        btnfacebookRegister = view.findViewById(R.id.btnFacebookRegister);
        btnRegister = view.findViewById(R.id.btnRegister);

        final TextInputLayout tilEmail = view.findViewById(R.id.tilEmailRegister);
        final TextInputLayout tilUsername = view.findViewById(R.id.tilUsernameRegister);
        final TextInputLayout tilPassword = view.findViewById(R.id.tilPasswordRegister1);
        final TextInputLayout tilPassword2 = view.findViewById(R.id.tilPasswordRegister2);

        TextWatcher textWatchEmail = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,int count) {
                if (!etvEmail.getText().toString().isEmpty()){
                    if (!StringUtils.isValidEmailAddress(etvEmail.getText().toString())){
                        tilEmail.setErrorEnabled(true);
                        tilEmail.setError(getString(R.string.error_email_login));
                        vibrate(getActivity(),250, 10);
                    }
                    else {
                        tilEmail.setErrorEnabled(false);
                    }
                }
            }

            public void afterTextChanged(Editable s) {
            }
        };

        TextWatcher textWatchUsername = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,int count) {
                if (!etvUsername.getText().toString().isEmpty()){
                    if (!StringUtils.isValidUserName(etvUsername.getText().toString())){
                        tilUsername.setErrorEnabled(true);
                        tilUsername.setError(getString(R.string.error_invalid_username));
                    }
                    else {
                        tilUsername.setErrorEnabled(false);
                    }
                }
            }

            public void afterTextChanged(Editable s) {

            }
        };

        TextWatcher textWatchPassword = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,int count) {
                if (!etvPassword1.getText().toString().isEmpty()){

                    if(etvPassword1.getText().toString().length() <= 6){
                        tilPassword.setErrorEnabled(true);
                        tilPassword.setError(getString(R.string.error_invalid_password));
                    }

                    else if (!StringUtils.isValidPassword(etvPassword1.getText().toString())){
                        tilPassword.setErrorEnabled(true);
                        tilPassword.setError(getString(R.string.error_password_login));
                    }
                    else {
                        tilPassword.setErrorEnabled(false);
                    }
                }
            }

            public void afterTextChanged(Editable s) {
            }
        };

        TextWatcher textWatchPassword2 = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,int count) {
                if (!etvPassword2.getText().toString().isEmpty()){
                    if (!etvPassword1.getText().toString().equals(etvPassword2.getText().toString())){
                        tilPassword2.setErrorEnabled(true);
                        tilPassword2.setError(getString(R.string.error_notsame_password));
                    }
                    else{
                        tilPassword2.setErrorEnabled(false);
                        if (!StringUtils.isValidPassword(etvPassword2.getText().toString())){
                            tilPassword2.setErrorEnabled(true);
                            tilPassword2.setError(getString(R.string.error_password_login));
                        }
                        else {
                            tilPassword2.setErrorEnabled(false);
                        }
                    }
                }
            }

            public void afterTextChanged(Editable s) {
            }
        };

        // Adding the TextChangeListener
        etvEmail.addTextChangedListener(textWatchEmail);
        etvUsername.addTextChangedListener(textWatchUsername);
        etvPassword1.addTextChangedListener(textWatchPassword);
        etvPassword2.addTextChangedListener(textWatchPassword2);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // If EditText Empty display "Field Required" Error
                if (etvEmail.getText().toString().isEmpty()){
                    tilEmail.setErrorEnabled(true);
                    tilEmail.setError(getString(R.string.error_field_required));
                    if(soundIsEnabled())
                        mp.start();
                    vibrate(getActivity(),250, 10);
                }
                else {
                    tilEmail.setErrorEnabled(false);
                }
                // If EditText Empty display "Field Required" Error
                if (etvUsername.getText().toString().isEmpty()){
                    tilUsername.setErrorEnabled(true);
                    tilUsername.setError(getString(R.string.error_field_required));
                    if(soundIsEnabled())
                        mp.start();
                    vibrate(getActivity(),250, 10);
                }
                else {
                    tilUsername.setErrorEnabled(false);
                }
                // If EditText Empty display "Field Required" Error
                if (etvPassword1.getText().toString().isEmpty()){
                    tilPassword.setErrorEnabled(true);
                    tilPassword.setError(getString(R.string.error_field_required));
                    if(soundIsEnabled())
                        mp.start();
                    vibrate(getActivity(),250, 10);
                }
                else {
                    tilPassword.setErrorEnabled(false);
                }
                // If EditText Empty display "Field Required" Error
                if (etvPassword2.getText().toString().isEmpty()){
                    tilPassword2.setErrorEnabled(true);
                    tilPassword2.setError(getString(R.string.error_field_required));
                    if(soundIsEnabled())
                        mp.start();
                    vibrate(getActivity(),250, 10);
                }
                else {
                    tilPassword2.setErrorEnabled(false);
                }

                if (StringUtils.isValidEmailAddress(etvEmail.getText().toString()) && // If Email is valid
                        StringUtils.isValidUserName(etvUsername.getText().toString()) && // If Username is valid
                            (etvPassword1.getText().toString().equals(etvPassword2.getText().toString())) && // If password1 and password2 are equal
                                StringUtils.isValidPassword(etvPassword1.getText().toString())){ // If Password is valid

                                    final String userName = etvUsername.getText().toString();
                                    final String emailAddress = etvEmail.getText().toString();
                                    final String password = etvPassword2.getText().toString();
                                    createAccount(userName, emailAddress, password);
                }
            }
        });

        // Start LoginActivity in case someone already has an Account
        tvwRegistertoLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });

        // Facebook Login Button
        callbackManager = CallbackManager.Factory.create();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

        if(isLoggedIn)
            executePostLogin(accessToken);

        btnfacebookRegister.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
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

        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        accessTokenTracker.stopTracking();
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


    /**
     * Login Method, is called after successful registration
     * @param userName Username
     * @param password password
     */
    private void login(final String userName, final String password) {
        LoginManager.Factory.getInstance(getContext())
                .login(userName, password, new ILoginCallback() {
                    @Override
                    public void onSucceeded(AuthenticateUserResult result) {

                    }

                    @Override
                    public void onFailed(int errorCode, String errorMessage, Throwable t) {

                    }
                });
    }

    /**
     * Registers an Account
     * @param userName Username from an EditText View
     * @param emailAddress E-Mail address from an EditText View
     * @param password Password from an EditText View
     */
    private void createAccount(final String userName,final  String emailAddress,final  String password){
        LoginManager.Factory.getInstance(getContext()).createAccount(
                userName, emailAddress, password, new ICreateAccountCallback() {
                    @Override
                    public void onSucceeded(CreateUserResult result, User createdUser) {
                        login(userName, password);
                        getActivity().finish();
                    }

                    @Override
                    public void onFailed(int errorCode, String errorMessage, Throwable t) {
                        if(soundIsEnabled())
                            mp.start();
                    }
                });
    }

    // TODO Doesn't work yet, couldn't find the reason yet, should work

    /** Vibration
     * @param activity Passing Activity
     * @param duration duration of the vibration
     * @param amplitude strength of vibration
     */
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


}
