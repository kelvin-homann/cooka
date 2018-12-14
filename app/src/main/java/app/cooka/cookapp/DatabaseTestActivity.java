package app.cooka.cookapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
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
import app.cooka.cookapp.model.Category;
import app.cooka.cookapp.model.CategoryGridViewAdapter;
import app.cooka.cookapp.model.CategoryListViewAdapter;
import app.cooka.cookapp.model.DatabaseClient;
import app.cooka.cookapp.utils.SystemUtils;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DatabaseTestActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String LOGTAG = "COOKALOG";
    private static final String SSKH = "IxscGsjAYrkVPy2BF4EzhflkqZc=";

    public static final int LPT_LOCAL = 0;
    public static final int LPT_REMOTE_COOKA = 1;
    public static final int LPT_REMOTE_FACEBOOK = 2;
    public static final int LPT_REMOTE_GOOGLE = 3;

    private EditText etUserName;
    private EditText etEmailAddress;
    private EditText etPassword;
    private Button btnSignUp;
    private Button btnPollCategories;

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

        Settings.getInstance().setCurrentLanguageId(1031);
        String systemKeyHash = SystemUtils.getSystemKeyHash(this);

        // if this is Sebastian's machine trust its local ssl certificate
        if(systemKeyHash.compareTo(SSKH) == 0)
            trustLocalSslCertificate();

        etUserName = findViewById(R.id.etUserName);
        etEmailAddress = findViewById(R.id.etEmailAddress);
        etPassword = findViewById(R.id.etPassword);
        btnSignUp = findViewById(R.id.btnSignUpButton);
        btnSignUp.setOnClickListener(this);

        btnPollCategories = findViewById(R.id.btnPollCategories);
        btnPollCategories.setOnClickListener(this);

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
            case R.id.btnSignUpButton:
                signUp();
                break;

            case R.id.btnPollCategories:
                // todo: use dynamic user identification data and current app language for database polls
                pollCategoriesAsync(4, "d13830b53e59c8e771264e58936f88d2", 1031);
                break;
        }
    }

    /**
     * makes a retrofit database request to receive all unhidden categories in the specified language
     * @param userId the user identifier of the user who makes the request
     * @param userAccessToken the user access token of the user who makes the request (authentication check)
     * @param languageId the language identifier of the language to get the result in
     */
    private void pollCategoriesAsync(final long userId, final String userAccessToken, final long languageId) {
        if(databaseClient == null)
            return;

        categorySubscription = databaseClient
            .getCategories(userId, userAccessToken, languageId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<List<Category>>() {
                @Override
                public void onCompleted() {
                    Log.d(LOGTAG, "pollCategoriesAsync().onCompleted()");
                }

                @Override
                public void onError(Throwable e) {
                    Log.d(LOGTAG, "pollCategoriesAsync().onError()");
                    Log.d(LOGTAG, e.getMessage());
                }

                @Override
                public void onNext(List<Category> categories) {
                    categoryListObserver = new ArrayListObserver(categoryGridViewAdapter);
                    for(Category category : categories) {
                        category.addObserver(categoryListObserver);
                    }

                    int numCategoriesPolled = categories.size();
                    Log.d(LOGTAG, "pollCategoriesAsync().onNext()");
                    Log.d(LOGTAG, String.format("categories received = %d", numCategoriesPolled));

                    Toast.makeText(getApplicationContext(), String.format("%d %s polled", numCategoriesPolled,
                        numCategoriesPolled == 1 ? "category" : "categories"), Toast.LENGTH_LONG).show();

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
                }
            });
    }

    /**
     * makes a new retrofit database request to sign up a new user
     */
    private void signUp() {
        Toast.makeText(getApplicationContext(), "Sign up is not yet implemented!", Toast.LENGTH_LONG).show();

        /*btnSignUp.setClickable(false);

        if(etUserName.getText().length() == 0)
            Toast.makeText(getApplicationContext(), "Please enter a user name", Toast.LENGTH_LONG).show();
        else if(etEmailAddress.getText().length() == 0)
            Toast.makeText(getApplicationContext(), "Please enter a e-mail address", Toast.LENGTH_LONG).show();
        else if(etPassword.getText().length() == 0)
            Toast.makeText(getApplicationContext(), "Please enter a password", Toast.LENGTH_LONG).show();

        String userName = etUserName.getText().toString();
        String emailAddress = etEmailAddress.getText().toString();
        String password = etPassword.getText().toString();*/

        // todo: implement retrofit database request for user sign up
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
            Log.d(LOGTAG, String.format("generating certificate from file \"%s\"", certificateFileName));
            Log.d(LOGTAG, String.format("certificate ca = %s", x509Certificate.getSubjectDN()));

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
            Log.e(LOGTAG, "onCreate(): CertificateException");
            e.printStackTrace();
        }
        catch(FileNotFoundException e) {
            Log.e(LOGTAG, "onCreate(): FileNotFoundException");
            e.printStackTrace();
        }
        catch(IOException e) {
            Log.e(LOGTAG, "onCreate(): IOException");
            e.printStackTrace();
        }
        catch(KeyStoreException e) {
            Log.e(LOGTAG, "onCreate(): KeyStoreException");
            e.printStackTrace();
        }
        catch(NoSuchAlgorithmException e) {
            Log.e(LOGTAG, "onCreate(): NoSuchAlgorithmException");
            e.printStackTrace();
        }
        catch(KeyManagementException e) {
            Log.e(LOGTAG, "onCreate(): KeyManagementException");
            e.printStackTrace();
        }
    }
}
