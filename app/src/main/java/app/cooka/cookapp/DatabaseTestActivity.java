package app.cooka.cookapp;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

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
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import app.cooka.cookapp.firebase.MessagingService;
import app.cooka.cookapp.login.ICreateAccountCallback;
import app.cooka.cookapp.login.ILoginCallback;
import app.cooka.cookapp.login.IRefreshLoginCallback;
import app.cooka.cookapp.login.LoginManager;
import app.cooka.cookapp.login.ILogoutCallback;
import app.cooka.cookapp.model.ArrayListObserver;
import app.cooka.cookapp.model.AuthenticateUserResult;
import app.cooka.cookapp.model.Category;
import app.cooka.cookapp.model.CreateRecipeResult;
import app.cooka.cookapp.model.EDifficultyType;
import app.cooka.cookapp.model.EPublicationType;
import app.cooka.cookapp.model.FeedMessage;
import app.cooka.cookapp.model.FollowUserResult;
import app.cooka.cookapp.model.ICreateRecipeCallback;
import app.cooka.cookapp.model.IUpdateRecipeCallback;
import app.cooka.cookapp.model.Recipe;
import app.cooka.cookapp.model.RecipeStep;
import app.cooka.cookapp.model.RecipeStepIngredient;
import app.cooka.cookapp.model.Tag;
import app.cooka.cookapp.model.UpdateRecipeResult;
import app.cooka.cookapp.utils.NotificationUtils;
import app.cooka.cookapp.view.CategoryGridViewAdapter;
import app.cooka.cookapp.view.CategoryListViewAdapter;
import app.cooka.cookapp.model.CreateUserResult;
import app.cooka.cookapp.model.DatabaseClient;
import app.cooka.cookapp.model.IResultCallback;
import app.cooka.cookapp.model.InvalidateLoginResult;
import app.cooka.cookapp.model.RefreshLoginResult;
import app.cooka.cookapp.model.User;
import app.cooka.cookapp.utils.SystemUtils;
import app.cooka.cookapp.view.FeedMessageRecyclerViewAdapter;
import app.cooka.cookapp.view.RecipeFeedCardItemAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@TargetApi(26)
public class DatabaseTestActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String LOGTAG = "COOKALOG";
    private static final String SSKH = "IxscGsjAYrkVPy2BF4EzhflkqZc=";
    private static final String PREFERENCES_NAME = "userdata";

    private LinearLayout lltLoginPanel;
    private LinearLayout lltCreateAccountPanel;
    private LinearLayout lltWelcomePanel;
    private LinearLayout lltPollCategoriesPanel;
    private LinearLayout lltPollRecipesPanel;
    private LinearLayout lltPollFeedMessagesPanel;

    private LoginManager loginManager;
    private SSLContext sslContext;
    private DatabaseClient databaseClient;
    private CategoryListViewAdapter categoryListViewAdapter = new CategoryListViewAdapter();
    private CategoryGridViewAdapter categoryGridViewAdapter = new CategoryGridViewAdapter();
    private ArrayListObserver categoryListObserver;
    private ArrayListObserver recipeListObserver;
    private ArrayListObserver feedMessageListObserver;
    private Subscription categorySubscription;
    private Subscription recipeSubscription;
    private Subscription feedMessageSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_test);
        setTitle("Database Tests");

        //if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            List<NotificationChannel> notificationChannels = new ArrayList<>();
            notificationChannels.add(NotificationUtils.createAppNotificationChanel("usage",
                "Usage recommendations", "Makes recommendations about how to use this app and how to improve your usage experience",
                NotificationManager.IMPORTANCE_DEFAULT));
            notificationChannels.add(NotificationUtils.createAppNotificationChanel("recipe",
                "Recipe recommendations", "Makes recommendations about recipes that you may like",
                NotificationManager.IMPORTANCE_DEFAULT));
            notificationChannels.add(NotificationUtils.createAppNotificationChanel("followee",
                "Followee notifications", "Gives you notifications about the people and topics that you follow",
                NotificationManager.IMPORTANCE_DEFAULT));

            NotificationUtils.registerNotificationChannels(this, notificationChannels);
        //}

        loginManager = LoginManager.Factory.getInstance(getApplicationContext());

        lltLoginPanel = findViewById(R.id.lltLoginPanel);
        lltCreateAccountPanel = findViewById(R.id.lltCreateAccountPanel);
        lltWelcomePanel = findViewById(R.id.lltWelcomePanel);
        lltPollCategoriesPanel = findViewById(R.id.lltPollCategoriesPanel);
        lltPollRecipesPanel = findViewById(R.id.lltPollRecipesPanel);
        lltPollFeedMessagesPanel = findViewById(R.id.lltPollFeedMessagesPanel);

        // hide all panels until the login state is known
        hideAllPanels();

        // if the stored login information was declared invalid
        if(loginManager.isLoginInvalid()) {
            Log.d(LOGTAG, String.format("the login associated with the access token %s is now invalid",
                loginManager.getAccessToken() != null ? loginManager.getAccessToken() : "n/a"));
            logout(false);
            Log.d(LOGTAG, "the user has been logged out");
        }
        else
            refreshLogin(true);

        // if there is currently a user logged in
        if(loginManager.getUserId() != 0L) {
            // set welcome message
            TextView tvwWelcomeMessage = findViewById(R.id.tvwWelcomeMessage);
            String firstName = loginManager.getFirstName();
            String userName = loginManager.getUserName();
            if(firstName != null)
                tvwWelcomeMessage.setText(String.format("Welcome back %s", firstName));
            else if(userName != null)
                tvwWelcomeMessage.setText(String.format("Welcome back @%s", userName));
            else
                tvwWelcomeMessage.setText("Welcome back!");

            // make create account button un-clickable just to be sure
            Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
            btnCreateAccount.setClickable(false);

            showAuthenticationRequiredPanels();

            Log.d(LOGTAG, String.format("SPK_USERID = %d", loginManager.getUserId()));
            Log.d(LOGTAG, String.format("SPK_USERRIGHTS = 0x%08x", loginManager.getUserRights()));
            Log.d(LOGTAG, String.format("SPK_LANGUAGEID = %d", loginManager.getLanguageId()));
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
        findViewById(R.id.btnPollRecipes).setOnClickListener(this);
        findViewById(R.id.btnPollFeedMessages).setOnClickListener(this);
        findViewById(R.id.btnNotify).setOnClickListener(this);

        databaseClient = DatabaseClient.Factory.getInstance(this);

        //selectAndUpdateTestRecipe();
        testFirebase();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if(categorySubscription != null && categorySubscription.isUnsubscribed()) {
            categorySubscription.unsubscribe();
            categorySubscription = null;
        }
        if(recipeSubscription != null && recipeSubscription.isUnsubscribed()) {
            recipeSubscription.unsubscribe();
            recipeSubscription = null;
        }
        if(feedMessageSubscription != null && feedMessageSubscription.isUnsubscribed()) {
            feedMessageSubscription.unsubscribe();
            feedMessageSubscription = null;
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

            case R.id.btnPollRecipes:
                pollRecipes();
                break;

            case R.id.btnPollFeedMessages:
                pollFeedMessages();
                break;

            case R.id.btnNotify:
                testNotification();
        }
    }

    private void hideAllPanels() {

        lltLoginPanel.setVisibility(View.GONE);
        lltCreateAccountPanel.setVisibility(View.GONE);
        lltWelcomePanel.setVisibility(View.GONE);
        lltPollRecipesPanel.setVisibility(View.GONE);
        //lltPollCategoriesPanel.setVisibility(View.GONE);
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
        lltPollFeedMessagesPanel.setVisibility(View.VISIBLE);
        //lltPollRecipesPanel.setVisibility(View.VISIBLE);
        //lltPollCategoriesPanel.setVisibility(View.VISIBLE);
    }

    /**
     * Hides all authentication-required panels and shows the login panel.
     */
    private void hideAuthenticationRequiredPanels() {

        lltWelcomePanel.setVisibility(View.GONE);
        lltPollCategoriesPanel.setVisibility(View.GONE);
        lltPollRecipesPanel.setVisibility(View.GONE);
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

        LoginManager.Factory.getInstance(getApplicationContext())
            .login(loginId, password, new ILoginCallback() {
                @Override
                public void onSucceeded(AuthenticateUserResult result) {
                    // set welcome message
                    TextView tvwWelcomeMessage = findViewById(R.id.tvwWelcomeMessage);
                    if(result.firstName != null)
                        tvwWelcomeMessage.setText(String.format("Welcome back %s", result.firstName));
                    else if(result.userName != null)
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
                        categoryListObserver = new ArrayListObserver(categoryGridViewAdapter, null);
                        for(Category category : categories) {
                            category.addObserver(categoryListObserver);
                        }

                        int numCategoriesPolled = categories.size();
                        Toast.makeText(getApplicationContext(), String.format("%d %s polled", numCategoriesPolled,
                            numCategoriesPolled == 1 ? "category" : "categories"), Toast.LENGTH_SHORT).show();

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

    private void pollRecipes() {

        if(databaseClient == null)
            return;

        final Button btnPollRecipes = findViewById(R.id.btnPollRecipes);
        btnPollRecipes.setClickable(false);

        try {
            recipeSubscription = Recipe.Factory
                .selectRecipes(this, null, null, 0, 0,
                    new IResultCallback<List<Recipe>>() {
                        @Override
                        public void onSucceeded(List<Recipe> recipes) {

                            int numRecipesPolled = recipes.size();
                            Toast.makeText(getApplicationContext(), String.format("%d %s polled", numRecipesPolled,
                                numRecipesPolled == 1 ? "recipe" : "recipes"), Toast.LENGTH_SHORT).show();

                            RecyclerView rvwRecipesList = findViewById(R.id.rvwRecipesList);
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                            rvwRecipesList.setLayoutManager(layoutManager);

                            RecyclerView.Adapter adapter = new RecipeFeedCardItemAdapter(recipes);
                            rvwRecipesList.setAdapter(adapter);

                            recipeListObserver = new ArrayListObserver(null, adapter);
                            for(Recipe recipe : recipes) {
                                recipe.addObserver(recipeListObserver);
                            }

                            btnPollRecipes.setClickable(true);
                        }
                    });
        }
        catch(Exception e) {
            e.printStackTrace();
            btnPollRecipes.setClickable(true);
        }
    }

    private void pollFeedMessages() {

        if(databaseClient == null)
            return;

        final Button btnPollFeedMessages = findViewById(R.id.btnPollFeedMessages);
        btnPollFeedMessages.setClickable(false);

        try {
            recipeSubscription = FeedMessage.Factory
                .selectFeedMessages(this, 4, 0, false,
                    new IResultCallback<List<FeedMessage>>() {
                        @Override
                        public void onSucceeded(List<FeedMessage> feedMessages) {

                            int numFeedMessagesPolled = feedMessages.size();
                            Toast.makeText(getApplicationContext(), String.format("%d %s polled", numFeedMessagesPolled,
                                numFeedMessagesPolled == 1 ? "feed message" : "feed messages"), Toast.LENGTH_SHORT).show();

                            RecyclerView rvwFeedMessages = findViewById(R.id.rvwFeedMessages);
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                            rvwFeedMessages.setLayoutManager(layoutManager);

                            RecyclerView.Adapter adapter = new FeedMessageRecyclerViewAdapter(feedMessages);
                            rvwFeedMessages.setAdapter(adapter);

//                            feedMessageListObserver = new ArrayListObserver(null, adapter);
//                            for(FeedMessage feedMessage : feedMessages) {
//                                feedMessage.addObserver(recipeListObserver);
//                            }

                            btnPollFeedMessages.setClickable(true);
                        }
                    });
        }
        catch(Exception e) {
            e.printStackTrace();
            btnPollFeedMessages.setClickable(true);
        }
    }

    private void createTestRecipe() {

        Recipe newRecipe = Recipe.Factory.createRecipeDraft(Settings.getInstance().getCurrentLanguageId());
        newRecipe.setTitle("Kürbiscremesuppe mit Ingwer");
        newRecipe.setDescription("Kürbissuppe ist eine meist gebundene Suppe mit Kürbis als Hauptzutat. In Varianten ist sie in vielen europäischen Ländern sowie den USA, anderen Teilen Amerikas und in Australien bekannt. Obwohl Kürbis als „Gesinde-Kost“ und auch als Schweinefutter verwendet wurde, gehörte Kürbissuppe mindestens seit dem 18. Jahrhundert auch zur bürgerlichen und herrschaftlichen Küche.");
        newRecipe.setCreatorId(4);
        newRecipe.setMainImageFileName("fca6fc21192a1e59d1322746291da79b.jpg");
        newRecipe.setMainCategoryId(11);
        newRecipe.setPublicationType(EPublicationType.PUBLIC);
        newRecipe.setDifficultyType(EDifficultyType.SIMPLE);
        newRecipe.setPreparationTime(30);

        List<Tag> tags = new ArrayList<>();
        tags.add(Tag.fromTagId(1));
        tags.add(Tag.fromTagId(2));
        newRecipe.setTags(tags);

        List<RecipeStep> recipeSteps = new ArrayList<>();

        // step 1
        List<RecipeStepIngredient> recipeStep1Ingredients = new ArrayList<>();
        recipeStep1Ingredients.add(RecipeStepIngredient.Factory.createRecipeStepIngredientDraft("Kürbis", null, 120f, null, "g", null));
        RecipeStep step1 = RecipeStep.Factory.createRecipeStepDraft(1, "Kürbis vorbereiten", "Den Kürbis gut abspülen und halbieren. Kerne und das \"Stroh\" im Inneren entfernen und das Fruchtfleisch in grobe Stücke schneiden (Hokkaido-Kürbis braucht nicht geschält zu werden, andere Sorten besser schälen)",
            recipeStep1Ingredients);
        recipeSteps.add(step1);

        // step 2
        List<RecipeStepIngredient> recipeStep2Ingredients = new ArrayList<>();
        recipeStep2Ingredients.add(RecipeStepIngredient.Factory.createRecipeStepIngredientDraft("Kartoffeln", null, 35f, null, "g", null));
        recipeStep2Ingredients.add(RecipeStepIngredient.Factory.createRecipeStepIngredientDraft("Zwiebeln", null, 0.1666666666666f, null, "Stck", null));
        recipeStep2Ingredients.add(RecipeStepIngredient.Factory.createRecipeStepIngredientDraft("Ingwer", null, 0.1666666666666f, null, "Stck", null));
        RecipeStep step2 = RecipeStep.Factory.createRecipeStepDraft(2, "Gemüse in Würfel schneiden", "Kartoffeln schälen, abspülen und in Stücke schneiden. Zwiebel abziehen, Ingwer schälen und beides in kleine Würfel schneiden.",
            recipeStep2Ingredients);
        recipeSteps.add(step2);

        // step 3
        List<RecipeStepIngredient> recipeStep3Ingredients = new ArrayList<>();
        recipeStep3Ingredients.add(RecipeStepIngredient.Factory.createRecipeStepIngredientDraft("Öl", null, 1, null, "EL", null));
        recipeStep3Ingredients.add(RecipeStepIngredient.Factory.createRecipeStepIngredientDraft("Gemüsebrühe", null, 130, "Milliliter", "ml", null));
        RecipeStep step3 = RecipeStep.Factory.createRecipeStepDraft(3, "Gemüse kochen", "Das Öl in einem großen Topf erhitzen und Kürbis, Kartoffeln, Zwiebel und Ingwer darin etwa 3 Minuten andünsten. Dann die Brühe dazugießen und etwa 20 Minuten kochen lassen, bis das Gemüse weich ist.",
            recipeStep3Ingredients);
        recipeSteps.add(step3);

        // step 4
        List<RecipeStepIngredient> recipeStep4Ingredients = new ArrayList<>();
        recipeStep4Ingredients.add(RecipeStepIngredient.Factory.createRecipeStepIngredientDraft("Apfelkompott", null, 50f, null, "g", null));
        recipeStep4Ingredients.add(RecipeStepIngredient.Factory.createRecipeStepIngredientDraft("Salz und Pfeffer", null, 0f, null, null, null));
        RecipeStep step4 = RecipeStep.Factory.createRecipeStepDraft(4, "Pürieren und abschmecken", "Das Gemüse in der Brühe mit dem Stabmixer fein pürieren. Das Apfelkompott unterrühren und die Suppe mit Salz und Pfeffer abschmecken.",
            recipeStep4Ingredients);
        recipeSteps.add(step4);

        // step 5
        List<RecipeStepIngredient> recipeStep5Ingredients = new ArrayList<>();
        recipeStep5Ingredients.add(RecipeStepIngredient.Factory.createRecipeStepIngredientDraft("Schlagsahne", null, 0.5f, null, "EL", null));
        recipeStep5Ingredients.add(RecipeStepIngredient.Factory.createRecipeStepIngredientDraft("Pfeffer", null, 0f, null, null, null));
        RecipeStep step5 = RecipeStep.Factory.createRecipeStepDraft(5, "Suppe servieren", "Zum Servieren die Suppe in Teller geben und etwas flüssige Sahne mit einem Löffel kreisförmig darauf verteilen. Etwas frisch gemahlenen Pfeffer darüber streuen.",
            recipeStep5Ingredients);
        recipeSteps.add(step5);

        newRecipe.setRecipeSteps(recipeSteps);
        newRecipe.setFlags(Recipe.FLAG_MOCKUP | Recipe.FLAG_UNCHANGED_COPY);

        try {
            Recipe.Factory.submitRecipe(getApplicationContext(), newRecipe, false,
                new ICreateRecipeCallback() {
                @Override
                public void onSucceeded(CreateRecipeResult createRecipeResult,
                    Recipe createdRecipe)
                {
                    Log.d(LOGTAG, String.format("submitRecipe succeeded and returned recipe id %d",
                        createRecipeResult.recipeId));
                }

                @Override
                public void onFailed(CreateRecipeResult createRecipeResult) {
                    String errorMessage = "submitRecipe failed";
                    if(createRecipeResult != null) {
                        errorMessage += String.format(": error %d: %s",
                            createRecipeResult.resultCode, createRecipeResult.resultMessage);
                    }
                    Log.e(LOGTAG, errorMessage);
                }
            });
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void selectAndUpdateTestRecipe() {

        // select recipe before we can update one
        Recipe.Factory.selectRecipe(getApplicationContext(), 11, new IResultCallback<Recipe>() {
            @Override
            public void onSucceeded(Recipe recipe) {
                // then perform an update
                updateTestRecipe(recipe);
            }
        });
    }

    private void updateTestRecipe(Recipe recipe) {

        recipe.setTitle("Kürbissuppe mit Ingwer");
        recipe.setDescription(null);
        recipe.getTags().add(Tag.fromTagId(3));
        recipe.setDifficultyType(EDifficultyType.DEMANDING);
        recipe.setPreparationTime(40);
        recipe.addFlags(Recipe.FLAG_HIGHLIGHTED);

        // update recipe in database
        Recipe.Factory.updateRecipe(getApplicationContext(), recipe, new IUpdateRecipeCallback() {
            @Override
            public void onSucceeded(UpdateRecipeResult updateRecipeResult, Recipe updatedRecipe) {
                Log.d(LOGTAG, String.format("updateRecipe succeeded; number of rows affected = %d, " +
                    "inserted = %d, deleted = %d", updateRecipeResult.numAffectedRows,
                    updateRecipeResult.numInsertedRows, updateRecipeResult.numDeletedRows));
            }

            @Override
            public void onFailed(UpdateRecipeResult updateRecipeResult) {
                String errorMessage = "updateRecipe failed";
                if(updateRecipeResult != null) {
                    errorMessage += String.format(": error %d: %s",
                        updateRecipeResult.resultCode, updateRecipeResult.resultMessage);
                }
                Log.e(LOGTAG, errorMessage);
            }
        });
    }

    private void testFirebase() {

        FirebaseInstanceId.getInstance().getInstanceId()
            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if (!task.isSuccessful()) {
                        Log.w(LOGTAG, "getInstanceId failed", task.getException());
                        return;
                    }

                    String token = task.getResult().getToken();
                    Log.d(LOGTAG, "Refreshed Firebase Cloud Messaging token: " + token);
                }
            });
    }

    private void testNotification() {

        String channelId = "usage";
        String title = "Welcome to Cooka";
        String message = "Thank you for choosing Cooka. Please tell us what food you would like to cook and what food hashtags, collections and people you would like to follow.";

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT);

        PendingIntent explorePendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT);
        PendingIntent hashtagsIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT);
        PendingIntent peopleIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(this, channelId);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText
            .setBigContentTitle(title)
            .setSummaryText(title)
            .bigText(message);

        Bitmap cookaLogo = BitmapFactory.decodeResource(getResources(),
            R.drawable.ic_cooka_icon);

        final NotificationCompat.Action exploreAction =
            new NotificationCompat.Action(R.drawable.ic_explore_24px, getString(R.string.notif_action_explore),
                explorePendingIntent);
        final NotificationCompat.Action hashtagsAction =
            new NotificationCompat.Action(R.drawable.ic_hashtag_24px, getString(R.string.notif_action_hashtags),
                explorePendingIntent);
        final NotificationCompat.Action peoplesAction =
            new NotificationCompat.Action(R.drawable.ic_people_24dp, getString(R.string.notif_action_people),
                explorePendingIntent);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        notificationBuilder
            .setContentIntent(pendingIntent)
            .setLargeIcon(cookaLogo)
            .setSmallIcon(R.drawable.ic_cooka_icon)
            .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark))
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(Notification.PRIORITY_MAX)
            .setStyle(bigText)
            .setLights(Color.GREEN, 500, 500)
            .setSound(defaultSoundUri)
            .addAction(exploreAction)
            .addAction(hashtagsAction)
            .addAction(peoplesAction)
            .setAutoCancel(true)
            .setChannelId(channelId);

        NotificationManager notificationManager = (NotificationManager)getSystemService(
            Context.NOTIFICATION_SERVICE);
        Notification notification = notificationBuilder.build();
        notificationManager.notify(MessagingService.getNextNotificationId(), notification);
    }

    private void testDatabaseRequest() {

        databaseClient.followUser(46, 42)
            .enqueue(new Callback<FollowUserResult>() {
                @Override
                public void onResponse(Call<FollowUserResult> call, Response<FollowUserResult>
                    response)
                {
                    FollowUserResult result = response.body();
                    if(result == null) {
                        Log.e(LOGTAG, "follow user failed on response");
                        return;
                    }
                    if(result.resultCode != 0) {
                        Log.e(LOGTAG, String.format("follow user failed with code %d: %s",
                            result.resultCode, result.resultMessage));
                        return;
                    }
                    Log.d(LOGTAG, "follow user succeeded");
                }

                @Override
                public void onFailure(Call<FollowUserResult> call, Throwable t) {
                    Log.e(LOGTAG, "followUser: the web service did not respond");
                    t.printStackTrace();
                }
            });

        List<Long> followUserIds = new ArrayList<>();
        followUserIds.add(39L);
        followUserIds.add(44L);
        followUserIds.add(33L);

        databaseClient.followUsers(followUserIds, 42)
            .enqueue(new Callback<FollowUserResult>() {
                @Override
                public void onResponse(Call<FollowUserResult> call, Response<FollowUserResult>
                    response)
                {
                    FollowUserResult result = response.body();
                    if(result == null) {
                        Log.e(LOGTAG, "follow users failed on response");
                        return;
                    }
                    if(result.resultCode != 0) {
                        Log.e(LOGTAG, String.format("follow users failed with code %d: %s",
                            result.resultCode, result.resultMessage));
                        return;
                    }
                    Log.d(LOGTAG, "follow users succeeded");
                }

                @Override
                public void onFailure(Call<FollowUserResult> call, Throwable t) {
                    Log.e(LOGTAG, "followUsers: the web service did not respond");
                    t.printStackTrace();
                }
            });
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
