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

import app.cooka.cookapp.login.ICreateAccountCallback;
import app.cooka.cookapp.login.ILoginCallback;
import app.cooka.cookapp.login.IRefreshLoginCallback;
import app.cooka.cookapp.login.LoginManager;
import app.cooka.cookapp.login.ILogoutCallback;
import app.cooka.cookapp.model.ArrayListObserver;
import app.cooka.cookapp.model.AuthenticateUserResult;
import app.cooka.cookapp.model.Category;
import app.cooka.cookapp.model.CategoryGridViewAdapter;
import app.cooka.cookapp.model.CategoryListViewAdapter;
import app.cooka.cookapp.model.CreateUserResult;
import app.cooka.cookapp.model.DatabaseClient;
import app.cooka.cookapp.model.ICreateUserCallback;
import app.cooka.cookapp.model.InvalidateLoginResult;
import app.cooka.cookapp.model.RefreshLoginResult;
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
    public static final String SPK_INVALID = "invalid";

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

        // hide all panels until the login state is known
        hideAllPanels();

        // if the stored login information was declared invalid
        if(sharedPreferences.contains(SPK_INVALID) && sharedPreferences.getBoolean(SPK_INVALID, false)) {
            logout(false);
            Log.d(LOGTAG, String.format("the login associated with the access token %s is now invalid",
                sharedPreferences.getString(SPK_ACCESSTOKEN, "n/a")));
            Log.d(LOGTAG, "the user has been logged out");
        }
        else
            refreshLogin(true);

        // if there is currently a user logged in
        if(sharedPreferences.contains(DatabaseTestActivity.SPK_USERID) &&
            sharedPreferences.getLong(DatabaseTestActivity.SPK_USERID, 0L) != 0L)
        {
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

            showAuthenticationRequiredPanels();

            Log.d(LOGTAG, String.format("SPK_USERID = %d", sharedPreferences.getLong(DatabaseTestActivity.SPK_USERID, 0L)));
            Log.d(LOGTAG, String.format("SPK_USERRIGHTS = 0x%08x", sharedPreferences.getLong(DatabaseTestActivity.SPK_USERRIGHTS, 0)));
            Log.d(LOGTAG, String.format("SPK_LANGUAGEID = %d", sharedPreferences.getLong(DatabaseTestActivity.SPK_LANGUAGEID, 0L)));
        }
        // if there is no user logged in
        else {
            hideAuthenticationRequiredPanels();
        }

        Settings.getInstance().setCurrentLanguageId(1031);
        String systemKeyHash = SystemUtils.getSystemKeyHash(this);

        // if this is Sebastian's machine trust its local ssl certificate
        if(systemKeyHash.compareTo(SSKH) == 0)
            trustLocalSslCertificate();

        findViewById(R.id.btnLogin).setOnClickListener(this);
        findViewById(R.id.btnLogout).setOnClickListener(this);
        findViewById(R.id.btnRegister).setOnClickListener(this);
        findViewById(R.id.btnIHaveAnAccount).setOnClickListener(this);
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
                logout(true);
                break;

            case R.id.btnRegister:
                showCreateAccountPanel();
                break;

            case R.id.btnIHaveAnAccount:
                showLoginPanel();
                break;

            case R.id.btnCreateAccount:
                createAccount();
                break;

            case R.id.btnPollCategories:
                pollCategoriesAsync();
                break;
        }
    }

    private void hideAllPanels() {

        lltLoginPanel.setVisibility(View.GONE);
        lltCreateAccountPanel.setVisibility(View.GONE);
        lltWelcomePanel.setVisibility(View.GONE);
        lltPollCategoriesPanel.setVisibility(View.GONE);
    }

    /**
     * Shows the create account panel and populates its fields with user name and password if were
     * previously entered into the login panel.
     */
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

    /**
     * Shows the login panel and hides all other authentication-required panels.
     */
    private void showLoginPanel() {

        lltCreateAccountPanel.setVisibility(View.GONE);
        hideAuthenticationRequiredPanels();
    }

    /**
     * Shows all authentication-required panels and hides login or create account panels.
     */
    private void showAuthenticationRequiredPanels() {

        lltLoginPanel.setVisibility(View.GONE);
        lltCreateAccountPanel.setVisibility(View.GONE);
        lltWelcomePanel.setVisibility(View.VISIBLE);
        lltPollCategoriesPanel.setVisibility(View.VISIBLE);
    }

    /**
     * Hides all authentication-required panels and shows the login panel.
     */
    private void hideAuthenticationRequiredPanels() {

        lltWelcomePanel.setVisibility(View.GONE);
        lltPollCategoriesPanel.setVisibility(View.GONE);
        lltLoginPanel.setVisibility(View.VISIBLE);
    }

    /**
     * Starts a login process with the login information provided in the login panel.
     */
    private void login() {

        final Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setClickable(false);

        final EditText etLoginId = findViewById(R.id.etLoginId);
        final EditText etLoginPassword = findViewById(R.id.etLoginPassword);

        if(etLoginId.getText().length() == 0)
            Toast.makeText(getApplicationContext(), "Please enter a user name or e-mail address", Toast.LENGTH_LONG).show();
        else if(etLoginPassword.getText().length() == 0)
            Toast.makeText(getApplicationContext(), "Please enter a password", Toast.LENGTH_LONG).show();

        final String loginId = etLoginId.getText().toString();
        final String password = etLoginPassword.getText().toString();

        LoginManager.Factory.getInstance(getApplicationContext()).
            login(loginId, password, new ILoginCallback() {
                @Override
                public void onSucceeded(AuthenticateUserResult result) {
                    // set welcome message
                    TextView tvwWelcomeMessage = findViewById(R.id.tvwWelcomeMessage);
                    if(result.userName != null)
                        tvwWelcomeMessage.setText(String.format("Welcome back @%s", result.userName));
                    else
                        tvwWelcomeMessage.setText("Welcome back!");

                    // show all authentication-required panels and hide login panel
                    showAuthenticationRequiredPanels();

                    etLoginId.setText("");
                    etLoginPassword.setText("");

                    Toast.makeText(getApplicationContext(), "Successfully logged in", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailed(int errorCode, String errorMessage, Throwable t) {
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                    Log.e(LOGTAG, String.format("error code %d: %s", errorCode, errorMessage));
                    btnLogin.setClickable(true);
                }
            });
    }

    /**
     * Logs the user out and removes the login associated with the stored access token. Hides any
     * authentication-required content panels and shows the login panel again.
     * @param manual deletes the login from the database if set true (used when the user manually
     *      chooses to log out); does not delete the login from the database and assumes it was
     *      already removed (used in effect of automatic login invalidation).
     */
    private void logout(final boolean manual) {

        LoginManager.Factory.getInstance(getApplicationContext()).logout(manual,
            new ILogoutCallback() {
            @Override
            public void onSucceeded(InvalidateLoginResult result) {
                Log.d(LOGTAG, "the stored login information has been invalidated");
                Log.d(LOGTAG, "the user has been logged out");
                Toast.makeText(getApplicationContext(), "You have been logged out", Toast.LENGTH_SHORT).show();
            }

            @Override public void onFailed(int errorCode, String errorMessage, Throwable t) {}
        });

        // clear the shared preferences and remove any stored login information
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

        hideAuthenticationRequiredPanels();
    }

    /**
     * Refreshes the login associated with the stored access token. If the refresh happens within
     * 30 days of the last user activity the login remains alive and will get refreshed. If the
     * refresh happens after these 30 days or after 4 month after the login was created the login
     * has expired or if the access token has been declared invalid or deleted for some other
     * reason, the user will get logged out. The user has to log in again and will receive a new
     * access token.
     * @param forceLogout forces a logout if the stored login information is invalid if set true;
     *      postpones the logout until the next authentication-required user action of until the
     *      next refresh login at the latest if set false.
     */
    private void refreshLogin(final boolean forceLogout) {

        LoginManager.Factory.getInstance(getApplicationContext()).refreshLogin(forceLogout,
            new IRefreshLoginCallback() {
                @Override
                public void onLoginRefreshed(RefreshLoginResult result) {
                    // nothing to do here; the login has been refreshed on the database
                    Log.d(LOGTAG, "the stored login information has been refreshed");
                }

                @Override
                public void onLoginInvalidated() {

                }

                @Override
                public void onFailed(int errorCode, String errorMessage, Throwable t) {

                }
            });
    }

    /**
     * Creates a new user account with the information provided in the create account panel.
     */
    private void createAccount() {

        final Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnCreateAccount.setClickable(false);

        final EditText etUserName = findViewById(R.id.etUserName);
        final EditText etEmailAddress = findViewById(R.id.etEmailAddress);
        final EditText etPassword = findViewById(R.id.etPassword);

        if(etUserName.getText().length() == 0)
            Toast.makeText(getApplicationContext(), "Please enter a user name", Toast.LENGTH_LONG).show();
        else if(etEmailAddress.getText().length() == 0)
            Toast.makeText(getApplicationContext(), "Please enter a e-mail address", Toast.LENGTH_LONG).show();
        else if(etPassword.getText().length() == 0)
            Toast.makeText(getApplicationContext(), "Please enter a password", Toast.LENGTH_LONG).show();

        final String userName = etUserName.getText().toString();
        final String emailAddress = etEmailAddress.getText().toString();
        final String password = etPassword.getText().toString();

        LoginManager.Factory.getInstance(getApplicationContext()).createAccount(userName,
            emailAddress, password, new ICreateAccountCallback() {
                @Override
                public void onSucceeded(CreateUserResult result, User createdUser) {
                    // hide the create account panel
                    lltCreateAccountPanel.setVisibility(View.GONE);

                    // set welcome message
                    TextView tvwWelcomeMessage = findViewById(R.id.tvwWelcomeMessage);
                    String userName = createdUser.getUserName();
                    if(userName != null)
                        tvwWelcomeMessage.setText(String.format("Welcome to Cooka @%s", userName));
                    else
                        tvwWelcomeMessage.setText("Welcome to Cooka!");

                    // show all authentication-required panels
                    showAuthenticationRequiredPanels();

                    etUserName.setText("");
                    etEmailAddress.setText("");
                    etPassword.setText("");

                    Toast.makeText(getApplicationContext(), "Account successfully created", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailed(int errorCode, String errorMessage, Throwable t) {
                    btnCreateAccount.setClickable(true);
                }
            });
    }

    /**
     * Polls all categories from the database and populates a grid view in the poll categories
     * panel with them.
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
     * {{DEBUG}}
     * Installs and trusts Sebastian's local self-signed SSL certificate used by the development
     * web server. Shall only be executed in debug builds.
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
