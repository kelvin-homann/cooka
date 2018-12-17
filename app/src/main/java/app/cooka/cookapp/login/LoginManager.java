package app.cooka.cookapp.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import app.cooka.cookapp.Settings;
import app.cooka.cookapp.model.AuthenticateUserResult;
import app.cooka.cookapp.model.CreateUserResult;
import app.cooka.cookapp.model.DatabaseClient;
import app.cooka.cookapp.model.ExistsUserResult;
import app.cooka.cookapp.model.ICreateUserCallback;
import app.cooka.cookapp.model.InvalidateLoginResult;
import app.cooka.cookapp.model.RefreshLoginResult;
import app.cooka.cookapp.model.User;
import app.cooka.cookapp.utils.SecurityUtils;
import app.cooka.cookapp.utils.SystemUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginManager {

    public static final String LOGTAG = "COOKALOG";

    private static final String PREFERENCES_NAME = "userdata";

    // shared preference keys
    public static final String SPK_USERID = "userId";
    public static final String SPK_USERNAME = "userName";
    public static final String SPK_FIRSTNAME = "firstName";
    public static final String SPK_LASTNAME = "lastName";
    public static final String SPK_ACCESSTOKEN = "accessToken";
    public static final String SPK_USERRIGHTS = "userRights";
    public static final String SPK_LANGUAGEID = "languageId";
    public static final String SPK_INVALID = "invalid";

    private SharedPreferences sharedPreferences;
    private Context context;

    private LoginManager(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static class Factory {

        private static LoginManager instance;
        public static LoginManager getInstance(Context context) {
            if(instance == null)
                instance = new LoginManager(context);
            return instance;
        }
    }

    public long getUserId() {
        return sharedPreferences.contains(SPK_USERID) ? sharedPreferences.getLong(SPK_USERID, 0L) : 0L;
    }

    public String getUserName() {
        return sharedPreferences.contains(SPK_USERNAME) ? sharedPreferences.getString(SPK_USERNAME, null) : null;
    }

    public String getFirstName() {
        return sharedPreferences.contains(SPK_FIRSTNAME) ? sharedPreferences.getString(SPK_FIRSTNAME, null) : null;
    }

    public String getLastName() {
        return sharedPreferences.contains(SPK_LASTNAME) ? sharedPreferences.getString(SPK_LASTNAME, null) : null;
    }

    public String getAccessToken() {
        return sharedPreferences.contains(SPK_ACCESSTOKEN) ? sharedPreferences.getString(SPK_ACCESSTOKEN, null) : null;
    }

    public long getUserRights() {
        return sharedPreferences.contains(SPK_USERRIGHTS) ? sharedPreferences.getLong(SPK_USERRIGHTS, 0L) : 0L;
    }

    public long getLanguageId() {
        return sharedPreferences.contains(SPK_LANGUAGEID) ? sharedPreferences.getLong(SPK_LANGUAGEID, 0L) : 0L;
    }

    public boolean isLoginInvalid() {
        return sharedPreferences.contains(SPK_INVALID) ? sharedPreferences.getBoolean(SPK_INVALID, false) : false;
    }

    /**
     * Logs in the user associated with the specified login ID and password. Calls the provided
     *      ILoginCallback on success or failure.
     * @param loginId the login ID in form of a user name or e-mail address.
     * @param password the user password.
     * @param loginResultCallback a login result callback that performs certain tasks on success or
     *      on failure.
     */
    public void login(final String loginId, final String password, final ILoginCallback
        loginResultCallback)
    {
        // run user exists request
        DatabaseClient.Factory.getInstance(context)
            .existsUser(loginId, null, null, true)
            .enqueue(new Callback<ExistsUserResult>() {
                @Override
                public void onResponse(Call<ExistsUserResult> call,
                    Response<ExistsUserResult> response)
                {
                    // do the authentication using the received salt
                    ExistsUserResult existsUserResult = response.body();
                    // if the user name/e-mail address exists
                    if(existsUserResult.result == 1) {
                        final long userId = existsUserResult.userId;
                        final String salt = existsUserResult.salt;
                        if(userId != 0 && salt.length() != 0) {
                            // do the authentication
                            authenticate(userId, password, salt, loginResultCallback);
                        }
                        // this should really not happen (database inconsistency)
                        else {
                            if(loginResultCallback != null)
                                loginResultCallback.onFailed(13, "Please contact the app developer.", null);
                        }
                    }
                    // if the user name/e-mail address does not exist
                    else {
                        if(loginResultCallback != null)
                            loginResultCallback.onFailed(11, "Invalid user name/e-mail address or password", null);
                    }
                }

                @Override
                public void onFailure(Call<ExistsUserResult> call, Throwable t) {
                    if(loginResultCallback != null)
                        loginResultCallback.onFailed(21, "Oops, that didn't work. Please try again in a minute.", t);
                    t.printStackTrace();
                }
            });
    }

    /**
     * Authenticates a user identified by userId with the provided password that is going to be
     * hashed using the provided salt.
     * @param userId the user ID of the user to be authenticated.
     * @param password the cleartext password to be hashed and checked against the one stored in
     *      the database.
     * @param salt the salt associated with the user previously gathered from the database and
     *      to be used to generate the hashed password.
     * @param loginResultCallback a login result callback that performs certain tasks on success or
     *      on failure.
     */
    public void authenticate(final long userId, final String password, final String salt,
        final ILoginCallback loginResultCallback)
    {
        if(sharedPreferences == null) {
            if(loginResultCallback != null)
                loginResultCallback.onFailed(15, "The login information store is not accessible.", null);
            return;
        }

        final String hashedPassword = SecurityUtils.generateHashedPassword(password, salt);
        final String accessToken = SecurityUtils.generateAccessToken();
        final String deviceId = SystemUtils.getAndroidId(context.getContentResolver());

        // run authentication request
        DatabaseClient.Factory.getInstance(context)
            .authenticateUser(userId, null, null, hashedPassword, accessToken, deviceId)
            .enqueue(new Callback<AuthenticateUserResult>() {
                @Override
                public void onResponse(Call<AuthenticateUserResult> call,
                    Response<AuthenticateUserResult> response)
                {
                    AuthenticateUserResult authenticateUserResult = response.body();
                    // if login successful
                    if(authenticateUserResult.result == 1) {
                        // set shared preferences (session variables)
                        if(sharedPreferences != null) {
                            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                            sharedPreferencesEditor.putLong(SPK_USERID, authenticateUserResult.userId);
                            sharedPreferencesEditor.putString(SPK_USERNAME, authenticateUserResult.userName);
                            sharedPreferencesEditor.putString(SPK_FIRSTNAME, authenticateUserResult.firstName);
                            sharedPreferencesEditor.putString(SPK_LASTNAME, authenticateUserResult.lastName);
                            sharedPreferencesEditor.putString(SPK_ACCESSTOKEN, accessToken);
                            sharedPreferencesEditor.putLong(SPK_USERRIGHTS, authenticateUserResult.userRights);
                            sharedPreferencesEditor.putLong(SPK_LANGUAGEID, Settings.Factory.
                                getInstance().getCurrentLanguageId());
                            sharedPreferencesEditor.apply();
                        }

                        if(loginResultCallback != null)
                            loginResultCallback.onSucceeded(authenticateUserResult);
                    }
                    // if login failed (probably wrong password)
                    else {
                        if(loginResultCallback != null)
                            loginResultCallback.onFailed(12, "Invalid user name/e-mail address or password", null);
                    }
                }

                @Override
                public void onFailure(Call<AuthenticateUserResult> call, Throwable t) {
                    if(loginResultCallback != null)
                        loginResultCallback.onFailed(22, "Oops, that didn't work. Please try again in a minute.", t);
                    t.printStackTrace();
                }
            });
    }

    /**
     * Logs the user out and removes the login associated with the stored access token. Hides any
     * authentication-required content panels and shows the login panel again.
     * @param manual true: deletes the login from the database (used when the user manually
     *      chooses to log out); false: does not delete the login from the database and assumes it
     *      was already removed if set false (used in effect of automatic login invalidation).
     */
    public void logout(final boolean manual, final ILogoutCallback logoutResultCallback) {

        if(sharedPreferences == null) {
            if(logoutResultCallback != null)
                logoutResultCallback.onFailed(15, "The login information store is not accessible.", null);
            return;
        }

        // run login delete request on the database
        if(sharedPreferences != null && manual) {
            final long userId = sharedPreferences.getLong(SPK_USERID, 0);
            final String accessToken = sharedPreferences.getString(SPK_ACCESSTOKEN, null);
            DatabaseClient.Factory.getInstance(context)
                .invalidateLogin(userId, accessToken)
                .enqueue(new Callback<InvalidateLoginResult>() {
                    @Override
                    public void onResponse(Call<InvalidateLoginResult> call, Response<InvalidateLoginResult> response) {
                        InvalidateLoginResult invalidateLoginResult = response.body();
                        if(invalidateLoginResult.result == 1) {
                            if(logoutResultCallback != null)
                                logoutResultCallback.onSucceeded(invalidateLoginResult);
                        }
                        else {
                            if(logoutResultCallback != null)
                                logoutResultCallback.onFailed(14, "Login could not be invalidated.", null);
                        }
                    }

                    @Override
                    public void onFailure(Call<InvalidateLoginResult> call, Throwable t) {
                        if(logoutResultCallback != null)
                            logoutResultCallback.onFailed(23, "Oops, that didn't work. Please try again in a minute.", t);
                        t.printStackTrace();
                    }
                });
        }

        // clear the shared preferences and remove any stored login information
        if(sharedPreferences != null) {
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.clear();
            sharedPreferencesEditor.apply();
        }
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
    public void refreshLogin(final boolean forceLogout, final IRefreshLoginCallback
        refreshLoginCallback)
    {
        if(sharedPreferences == null) {
            if(refreshLoginCallback != null)
                refreshLoginCallback.onFailed(15, "The login information store is not accessible.", null);
            return;
        }

        boolean invalid = sharedPreferences.getBoolean(SPK_INVALID, false);

        // if the stored login information was marked invalid, log the user out immediately
        if(invalid) {
            logout(false, null);
            return;
        }

        final long userId = sharedPreferences.getLong(SPK_USERID, 0);
        final String userName = sharedPreferences.getString(SPK_USERNAME, null);
        final String accessToken = sharedPreferences.getString(SPK_ACCESSTOKEN, null);

        if(userId == 0 || userName == null || accessToken == null)
            return;

        // run the refresh login request
        DatabaseClient.Factory.getInstance(context)
            .refreshLogin(userId, accessToken)
            .enqueue(new Callback<RefreshLoginResult>() {
                @Override
                public void onResponse(Call<RefreshLoginResult> call, Response<RefreshLoginResult> response) {
                    RefreshLoginResult refreshLoginResult = response.body();
                    // if the login has been refreshed and the access token is still valid
                    if(refreshLoginResult.result == 1) {
                        // nothing to do here; the login has been refreshed on the database
                        if(refreshLoginCallback != null)
                            refreshLoginCallback.onLoginRefreshed(refreshLoginResult);
                    }
                    // if the login expired or the access token has been declared invalid
                    else {
                        // todo: check for other result codes that indicate more specific errors
                        if(refreshLoginCallback != null)
                            refreshLoginCallback.onLoginInvalidated();
                        if(forceLogout)
                            logout(false, null);
                        else
                            invalidateLogin();
                    }
                }

                @Override
                public void onFailure(Call<RefreshLoginResult> call, Throwable t) {
                    if(refreshLoginCallback != null)
                        refreshLoginCallback.onFailed(24, "Oops, that didn't work. Please try again in a minute.", t);
                    t.printStackTrace();
                }
            });
    }

    /**
     * Invalidates the login information stored in the shared preferences but does not log out the
     * user immediately. The logout is postponed until the next user authentication-required
     * action or until the next login refresh at the latest to avoid unwanted usage interference.
     */
    public void invalidateLogin() {

        if(sharedPreferences != null) {
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putBoolean(SPK_INVALID, true);
            sharedPreferencesEditor.apply();
        }
    }

    /**
     * Creates a new user account with the provided information.
     */
    public void createAccount(final String userName, final String emailAddress,
        final String password, final ICreateAccountCallback createAccountCallback)
    {
        if(sharedPreferences == null) {
            if(createAccountCallback != null)
                createAccountCallback.onFailed(15, "The login information store is not accessible.", null);
            return;
        }
        
        final String salt = SecurityUtils.generateSalt(SecurityUtils.DEFAULT_SALT_LENGTH);
        final String hashedPassword = SecurityUtils.generateHashedPassword(password, salt);
        final String accessToken = SecurityUtils.generateAccessToken();
        final String deviceId = SystemUtils.getAndroidId(context.getContentResolver());
        final int userRights = 1;

//        Log.d(LOGTAG, String.format("registering user %s (%s)", userName, emailAddress));
//        Log.d(LOGTAG, String.format("hashed password = %s", hashedPassword));
//        Log.d(LOGTAG, String.format("generated salt = %s", salt));
//        Log.d(LOGTAG, String.format("generated access token = %s", accessToken));

        User.Factory.createUser(context, userName, null, null, emailAddress, hashedPassword, salt,
            accessToken, null, null, userRights, deviceId, new ICreateUserCallback() {
            @Override public void onSucceeded(CreateUserResult createUserResult, User createdUser) {
                Log.d(LOGTAG, String.format("successfully created user %s", createdUser.getUserName()));
                Log.d(LOGTAG, String.format("user id = %d", createdUser.getUserId()));
                Log.d(LOGTAG, String.format("main collection id = %d", createUserResult.mainCollectionId));
                Log.d(LOGTAG, String.format("login id = %d", createUserResult.loginId));

                // set shared preferences (session variables)
                if(sharedPreferences != null) {
                    SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                    sharedPreferencesEditor.putLong(SPK_USERID, createdUser.getUserId());
                    sharedPreferencesEditor.putString(SPK_USERNAME, createdUser.getUserName());
                    sharedPreferencesEditor.putString(SPK_ACCESSTOKEN, accessToken);
                    sharedPreferencesEditor.putLong(SPK_USERRIGHTS, userRights);
                    sharedPreferencesEditor.putLong(SPK_LANGUAGEID, Settings.Factory.getInstance().getCurrentLanguageId());
                    sharedPreferencesEditor.apply();
                }

                if(createAccountCallback != null)
                    createAccountCallback.onSucceeded(createUserResult, createdUser);
            }

            @Override public void onFailed() {
                if(createAccountCallback != null)
                    createAccountCallback.onFailed(24, "Oops, that didn't work. Please try again in a minute.", null);
            }
        });
    }
}
