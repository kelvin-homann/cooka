package app.cooka.cookapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import app.cooka.cookapp.model.ArrayListObserver;
import app.cooka.cookapp.model.AuthenticateUserResult;
import app.cooka.cookapp.model.Category;
import app.cooka.cookapp.model.CategoryGridViewAdapter;
import app.cooka.cookapp.model.CategoryListViewAdapter;
import app.cooka.cookapp.model.CreateUserResult;
import app.cooka.cookapp.model.DatabaseClient;
import app.cooka.cookapp.model.ExistsUserResult;
import app.cooka.cookapp.model.ICreateUserCallback;
import app.cooka.cookapp.model.User;
import app.cooka.cookapp.utils.SecurityUtils;
import app.cooka.cookapp.utils.SystemUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DatabaseTestActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String LOGTAG = "COOKALOG";
    private static final String SSKH = "IxscGsjAYrkVPy2BF4EzhflkqZc=";
    private static final String PREFERENCES_NAME = "userdata";

    // shared preference keys
    public static final String SPK_USERID = "userId";
    public static final String SPK_USERNAME = "userName";
    public static final String SPK_ACCESSTOKEN = "accessToken";
    public static final String SPK_USERRIGHTS = "userRights";
    public static final String SPK_LANGUAGEID = "languageId";

    public static SharedPreferences sharedPreferences;

    private LinearLayout lltLoginPanel;
    private LinearLayout lltCreateAccountPanel;
    private LinearLayout lltWelcomePanel;
    private LinearLayout lltPollCategoriesPanel;

    private SSLContext sslContext;
    private DatabaseClient databaseClient;
    private CategoryListViewAdapter categoryListViewAdapter = new CategoryListViewAdapter();
    private CategoryGridViewAdapter categoryGridViewAdapter = new CategoryGridViewAdapter();
    private ArrayListObserver categoryListObserver;
    private Subscription categorySubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_test);
        setTitle("Database Tests");
        findViewById(R.id.lvwCategories).setVisibility(View.GONE);

        if(sharedPreferences == null) {
            sharedPreferences = getApplicationContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        }

        lltLoginPanel = findViewById(R.id.lltLoginPanel);
        lltCreateAccountPanel = findViewById(R.id.lltCreateAccountPanel);
        lltWelcomePanel = findViewById(R.id.lltWelcomePanel);
        lltPollCategoriesPanel = findViewById(R.id.lltPollCategoriesPanel);

        // hide the create account panel
        lltCreateAccountPanel.setVisibility(View.GONE);

        // if there is currently a user logged in
        if(sharedPreferences.contains(DatabaseTestActivity.SPK_USERID) &&
            sharedPreferences.getLong(DatabaseTestActivity.SPK_USERID, 0L) != 0L)
        {
            // hide login panel
            lltLoginPanel.setVisibility(View.GONE);

            // set welcome message
            TextView tvwWelcomeMessage = findViewById(R.id.tvwWelcomeMessage);
            String userName = sharedPreferences.getString(SPK_USERNAME, null);
            if(userName != null)
                tvwWelcomeMessage.setText(String.format("Welcome back @%s", userName));
            else
                tvwWelcomeMessage.setText("Welcome back!");

            // make create account button un-clickable just to be sure
            Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
            btnCreateAccount.setClickable(false);

            Log.d(LOGTAG, String.format("SPK_USERID = %d", sharedPreferences.getLong(DatabaseTestActivity.SPK_USERID, 0L)));
            Log.d(LOGTAG, String.format("SPK_USERRIGHTS = 0x%08x", sharedPreferences.getLong(DatabaseTestActivity.SPK_USERRIGHTS, 0)));
            Log.d(LOGTAG, String.format("SPK_LANGUAGEID = %d", sharedPreferences.getLong(DatabaseTestActivity.SPK_LANGUAGEID, 0L)));
        }
        // if there is no user logged in
        else {
            // hide welcome panel
            lltWelcomePanel.setVisibility(View.GONE);
            // show login panel
            lltLoginPanel.setVisibility(View.VISIBLE);
        }

        Settings.getInstance().setCurrentLanguageId(1031);
        String systemKeyHash = SystemUtils.getSystemKeyHash(this);

        // if this is Sebastian's machine trust its local ssl certificate
        if(systemKeyHash.compareTo(SSKH) == 0)
            trustLocalSslCertificate();

        findViewById(R.id.btnLogin).setOnClickListener(this);
        findViewById(R.id.btnLogout).setOnClickListener(this);
        findViewById(R.id.btnRegister).setOnClickListener(this);
        findViewById(R.id.btnCreateAccount).setOnClickListener(this);
        findViewById(R.id.btnPollCategories).setOnClickListener(this);

        databaseClient = DatabaseClient.Factory.getInstance();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if(categorySubscription != null && categorySubscription.isUnsubscribed()) {
            categorySubscription.unsubscribe();
            categorySubscription = null;
        }
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()) {
            case R.id.btnLogin:
                login();
                break;

            case R.id.btnLogout:
                logout();
                break;

            case R.id.btnRegister:
                showCreateAccountPanel();
                break;

            case R.id.btnCreateAccount:
                createAccount();
                break;

            case R.id.btnPollCategories:
                pollCategoriesAsync();
                break;
        }
    }

    private void showCreateAccountPanel() {

        EditText etUserName = findViewById(R.id.etUserName);
        EditText etPassword = findViewById(R.id.etPassword);
        final String userName = etUserName.getText().toString();
        final String password = etPassword.getText().toString();

        // populate the create account panel with values from the login panel if there are any
        EditText etLoginId = findViewById(R.id.etLoginId);
        EditText etLoginPassword = findViewById(R.id.etLoginPassword);
        etLoginId.setText(userName);
        etLoginPassword.setText(password);

        // hide other panels
        lltLoginPanel.setVisibility(View.GONE);
        lltWelcomePanel.setVisibility(View.GONE);

        // show create account panel
        lltCreateAccountPanel.setVisibility(View.VISIBLE);
    }

    private void showWelcomePanel() {

        lltLoginPanel.setVisibility(View.GONE);
        lltCreateAccountPanel.setVisibility(View.GONE);
        lltWelcomePanel.setVisibility(View.VISIBLE);
        lltPollCategoriesPanel.setVisibility(View.VISIBLE);
    }

    private void login() {

        final Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setClickable(false);

        EditText etLoginId = findViewById(R.id.etLoginId);
        EditText etLoginPassword = findViewById(R.id.etLoginPassword);

        if(etLoginId.getText().length() == 0)
            Toast.makeText(getApplicationContext(), "Please enter a user name or e-mail address", Toast.LENGTH_LONG).show();
        else if(etLoginPassword.getText().length() == 0)
            Toast.makeText(getApplicationContext(), "Please enter a password", Toast.LENGTH_LONG).show();

        final String loginId = etLoginId.getText().toString();
        final String password = etLoginPassword.getText().toString();

        // run user exists request
        DatabaseClient.Factory.getInstance()
            .existsUser(loginId, null, null, true)
            .enqueue(new Callback<ExistsUserResult>() {
                @Override
                public void onResponse(Call<ExistsUserResult> call, Response<ExistsUserResult> response) {
                    // do the authentication using the received salt
                    ExistsUserResult existsUserResult = response.body();
                    // if the user name/e-mail address exists
                    if(existsUserResult.result == 1) {
                        final long userId = existsUserResult.userId;
                        final String salt = existsUserResult.salt;
                        if(userId != 0 && salt.length() != 0) {
                            // do the authentication
                            authenticate(userId, password, salt);
                        }
                        // this should really not happen (database inconsistency)
                        else {
                            Toast.makeText(getApplicationContext(), "Error code 13. Please contact the app developer.", Toast.LENGTH_LONG).show();
                            btnLogin.setClickable(true);
                        }
                    }
                    // if the user name/e-mail address does not exist
                    else {
                        Toast.makeText(getApplicationContext(), "Invalid user name/e-mail address or password", Toast.LENGTH_LONG).show();
                        btnLogin.setClickable(true);
                    }
                }

                @Override
                public void onFailure(Call<ExistsUserResult> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Oops, that didn't work. Please try again in a minute.", Toast.LENGTH_LONG).show();
                    t.printStackTrace();
                    btnLogin.setClickable(true);
                }
            });
    }

    private void authenticate(final long userId, final String password, final String salt) {

        final Button btnLogin = findViewById(R.id.btnLogin);
        final String hashedPassword = SecurityUtils.generateHashedPassword(password, salt);
        final String accessToken = SecurityUtils.generateAccessToken();
        final String deviceId = SystemUtils.getAndroidId(getContentResolver());

        // run authentication request
        DatabaseClient.Factory.getInstance()
            .authenticateUser(userId, null, null, hashedPassword, accessToken, deviceId)
            .enqueue(new Callback<AuthenticateUserResult>() {
                @Override
                public void onResponse(Call<AuthenticateUserResult> call, Response<AuthenticateUserResult> response) {
                    AuthenticateUserResult authenticateUserResult = response.body();
                    // if login successful
                    if(authenticateUserResult.result == 1) {
                        // set shared preferences (session variables)
                        if(sharedPreferences == null) {
                            sharedPreferences = getApplicationContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
                        }
                        if(sharedPreferences != null) {
                            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                            sharedPreferencesEditor.putLong(SPK_USERID, authenticateUserResult.userId);
                            sharedPreferencesEditor.putString(SPK_USERNAME, authenticateUserResult.userName);
                            sharedPreferencesEditor.putString(SPK_ACCESSTOKEN, accessToken);
                            sharedPreferencesEditor.putLong(SPK_USERRIGHTS, authenticateUserResult.userRights);
                            sharedPreferencesEditor.putLong(SPK_LANGUAGEID, Settings.Factory.getInstance().getCurrentLanguageId());
                            sharedPreferencesEditor.apply();
                        }

                        // hide login panel
                        lltLoginPanel.setVisibility(View.GONE);

                        // set welcome message
                        TextView tvwWelcomeMessage = findViewById(R.id.tvwWelcomeMessage);
                        if(authenticateUserResult.userName != null)
                            tvwWelcomeMessage.setText(String.format("Welcome back @%s", authenticateUserResult.userName));
                        else
                            tvwWelcomeMessage.setText("Welcome back!");

                        // show welcome panel
                        lltWelcomePanel.setVisibility(View.VISIBLE);

                        Toast.makeText(getApplicationContext(), "Successfully logged in", Toast.LENGTH_SHORT).show();
                    }
                    // if login failed (probably wrong password)
                    else {
                        Toast.makeText(getApplicationContext(), "Invalid user name/e-mail address or password", Toast.LENGTH_LONG).show();
                        btnLogin.setClickable(true);
                    }
                }

                @Override
                public void onFailure(Call<AuthenticateUserResult> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Oops, that didn't work. Please try again in a minute.", Toast.LENGTH_LONG).show();
                    t.printStackTrace();
                    btnLogin.setClickable(true);
                }
            });
    }

    private void logout() {

        // remove shared preferences (session variables)
        if(sharedPreferences == null) {
            sharedPreferences = getApplicationContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        }
        if(sharedPreferences != null) {
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.clear();
            sharedPreferencesEditor.apply();
        }

        // make the create account button clickable again
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnCreateAccount.setClickable(true);

        // make login button clickable again
        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setClickable(true);

        // hide welcome panel
        lltWelcomePanel.setVisibility(View.GONE);

        // show login panel
        lltLoginPanel.setVisibility(View.VISIBLE);
    }

    /**
     * makes a new retrofit database request to create a new user account
     */
    private void createAccount() {

        final Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnCreateAccount.setClickable(false);

        EditText etUserName = findViewById(R.id.etUserName);
        EditText etEmailAddress = findViewById(R.id.etEmailAddress);
        EditText etPassword = findViewById(R.id.etPassword);

        if(etUserName.getText().length() == 0)
            Toast.makeText(getApplicationContext(), "Please enter a user name", Toast.LENGTH_LONG).show();
        else if(etEmailAddress.getText().length() == 0)
            Toast.makeText(getApplicationContext(), "Please enter a e-mail address", Toast.LENGTH_LONG).show();
        else if(etPassword.getText().length() == 0)
            Toast.makeText(getApplicationContext(), "Please enter a password", Toast.LENGTH_LONG).show();

        final String userName = etUserName.getText().toString();
        final String emailAddress = etEmailAddress.getText().toString();
        final String password = etPassword.getText().toString();
        final String salt = SecurityUtils.generateSalt(SecurityUtils.DEFAULT_SALT_LENGTH);
        final String hashedPassword = SecurityUtils.generateHashedPassword(password, salt);
        final String accessToken = SecurityUtils.generateAccessToken();
        final String deviceId = SystemUtils.getAndroidId(getContentResolver());
        final int userRights = 1;

//        Log.d(LOGTAG, String.format("registering user %s (%s)", userName, emailAddress));
//        Log.d(LOGTAG, String.format("hashed password = %s", hashedPassword));
//        Log.d(LOGTAG, String.format("generated salt = %s", salt));
//        Log.d(LOGTAG, String.format("generated access token = %s", accessToken));

        User.Factory.createUser(userName, null, null, emailAddress, hashedPassword, salt,
            accessToken, null, null, userRights, deviceId, new ICreateUserCallback() {
            @Override public void onSucceeded(CreateUserResult createUserResult, User createdUser) {
                Log.d(LOGTAG, String.format("successfully created user %s", createdUser.getUserName()));
                Log.d(LOGTAG, String.format("user id = %d", createdUser.getUserId()));
                Log.d(LOGTAG, String.format("main collection id = %d", createUserResult.mainCollectionId));
                Log.d(LOGTAG, String.format("login id = %d", createUserResult.loginId));

                // set shared preferences (session variables)
                if(sharedPreferences == null) {
                    sharedPreferences = getApplicationContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
                }
                if(sharedPreferences != null) {
                    SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                    sharedPreferencesEditor.putLong(SPK_USERID, createdUser.getUserId());
                    sharedPreferencesEditor.putString(SPK_USERNAME, createdUser.getUserName());
                    sharedPreferencesEditor.putString(SPK_ACCESSTOKEN, accessToken);
                    sharedPreferencesEditor.putLong(SPK_USERRIGHTS, userRights);
                    sharedPreferencesEditor.putLong(SPK_LANGUAGEID, Settings.Factory.getInstance().getCurrentLanguageId());
                    sharedPreferencesEditor.apply();
                }

                // hide the create account panel
                lltCreateAccountPanel.setVisibility(View.GONE);

                // set welcome message
                TextView tvwWelcomeMessage = findViewById(R.id.tvwWelcomeMessage);
                String userName = createdUser.getUserName();
                if(userName != null)
                    tvwWelcomeMessage.setText(String.format("Welcome to Cooka @%s", userName));
                else
                    tvwWelcomeMessage.setText("Welcome to Cooka!");

                // show welcome panel
                lltWelcomePanel.setVisibility(View.VISIBLE);

                Toast.makeText(getApplicationContext(), "Account successfully created", Toast.LENGTH_SHORT).show();
            }

            @Override public void onFailed() {
                btnCreateAccount.setClickable(true);
            }
        });
    }

    /**
     * makes a retrofit database request to receive all unhidden categories in the specified language
     */
    private void pollCategoriesAsync() {

        if(databaseClient == null)
            return;

        final Button btnPollCategories = findViewById(R.id.btnPollCategories);
        btnPollCategories.setClickable(false);

        try {
            categorySubscription = databaseClient
                .selectCategories()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Category>>() {
                    @Override
                    public void onCompleted() {
                        btnPollCategories.setClickable(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(LOGTAG, "pollCategoriesAsync().onError()");
                        Log.d(LOGTAG, e.getMessage());
                        btnPollCategories.setClickable(true);
                    }

                    @Override
                    public void onNext(List<Category> categories) {
                        categoryListObserver = new ArrayListObserver(categoryGridViewAdapter);
                        for(Category category : categories) {
                            category.addObserver(categoryListObserver);
                        }

                        int numCategoriesPolled = categories.size();
                        Toast.makeText(getApplicationContext(), String.format("%d %s polled", numCategoriesPolled,
                            numCategoriesPolled == 1 ? "category" : "categories"), Toast.LENGTH_SHORT).show();

                        ListView lvwCategories = findViewById(R.id.lvwCategories);
                        lvwCategories.setAdapter(categoryListViewAdapter);
                        categoryListViewAdapter.setCategories(categories);

                        lvwCategories.setOnTouchListener(new View.OnTouchListener() {
                            // Setting on Touch Listener for handling the touch inside ScrollView
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                // Disallow the touch request for parent scroll on touch of child view
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                return false;
                            }
                        });

                        GridView gvwCategories = findViewById(R.id.gvwCategories);
                        gvwCategories.setAdapter(categoryGridViewAdapter);
                        categoryGridViewAdapter.setCategories(categories);

                        gvwCategories.setOnTouchListener(new View.OnTouchListener() {
                            // Setting on Touch Listener for handling the touch inside ScrollView
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                // Disallow the touch request for parent scroll on touch of child view
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                                return false;
                            }
                        });

                        btnPollCategories.setClickable(true);
                    }
                });
        }
        catch(Exception e) {
            e.printStackTrace();
            btnPollCategories.setClickable(true);
        }
    }

    /**
     * installs and trusts Sebastian's local self-signed SSL certificate used by the development web server
     * shall only be executed in debug builds
     */
    private void trustLocalSslCertificate() {

        Log.d(LOGTAG, "trusting self-signed certificate for https connections to localhost development web server \"cooka.local\"");
        // trust self-signed certificate for https connections to localhost development web server

        // import certificate from x509 formatted file
        Certificate certificate = null;
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            String certificateFileName = "cookalocal.crt";
            InputStream certificateInputStream = new BufferedInputStream(getResources().openRawResource(R.raw.cookalocal));

            certificate = certificateFactory.generateCertificate(certificateInputStream);
            X509Certificate x509Certificate = (X509Certificate)certificate;
            //Log.d(LOGTAG, String.format("generating certificate from file \"%s\"", certificateFileName));
            //Log.d(LOGTAG, String.format("certificate ca = %s", x509Certificate.getSubjectDN()));

            certificateInputStream.close();

            // add certificate to a new keystore
            if(certificate != null) {
                // create new keystore and add certificate to it
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", certificate);

                // create trust manager that trusts the certificate authorities in the new keystore
                String trustManagerAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(trustManagerAlgorithm);
                trustManagerFactory.init(keyStore);

                // create ssl context that uses the new trust manager
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            }
        }
        catch(CertificateException e) {
            Log.e(LOGTAG, "trustLocalSslCertificate(): CertificateException");
            e.printStackTrace();
        }
        catch(FileNotFoundException e) {
            Log.e(LOGTAG, "trustLocalSslCertificate(): FileNotFoundException");
            e.printStackTrace();
        }
        catch(IOException e) {
            Log.e(LOGTAG, "trustLocalSslCertificate(): IOException");
            e.printStackTrace();
        }
        catch(KeyStoreException e) {
            Log.e(LOGTAG, "trustLocalSslCertificate(): KeyStoreException");
            e.printStackTrace();
        }
        catch(NoSuchAlgorithmException e) {
            Log.e(LOGTAG, "trustLocalSslCertificate(): NoSuchAlgorithmException");
            e.printStackTrace();
        }
        catch(KeyManagementException e) {
            Log.e(LOGTAG, "trustLocalSslCertificate(): KeyManagementException");
            e.printStackTrace();
        }
    }
}
