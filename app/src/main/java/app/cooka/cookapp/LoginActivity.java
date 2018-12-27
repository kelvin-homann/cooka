package app.cooka.cookapp;

import android.os.Bundle;
import android.app.Activity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;

import de.hdodenhof.circleimageview.CircleImageView;

public class LoginActivity extends Activity {

    private EditText etvEmail;
    private EditText etvPassword;
    private CircleImageView ivwFacebook;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etvEmail.findViewById(R.id.etvEmailLogin);
        etvPassword.findViewById(R.id.etvPasswordLogin);
        ivwFacebook.findViewById(R.id.ivwFacebookLogin);
    }

}
