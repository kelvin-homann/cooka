package app.cooka.cookapp.model;

import android.content.SharedPreferences;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.List;

import app.cooka.cookapp.DatabaseTestActivity;
import app.cooka.cookapp.Settings;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

public class DatabaseClient {

    private static final String DATABASE_BASE_URL = "https://www.sebastianzander.de/cooka/";

    public static SimpleDateFormat databaseDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private IDatabase databaseInterface;
    private Category.JsonAdapter categoryJsonAdapter;
    SharedPreferences sharedPreferences;

    private DatabaseClient() {

        sharedPreferences = DatabaseTestActivity.sharedPreferences;
        categoryJsonAdapter = new Category.JsonAdapter(Settings.getInstance().getCurrentLanguageId());

        final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Category.class, categoryJsonAdapter)
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .create();

        final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(DATABASE_BASE_URL)
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

        databaseInterface = retrofit.create(IDatabase.class);
    }

    public static class Factory {

        private static DatabaseClient instance;
        public static DatabaseClient getInstance() {
            if(instance == null)
                instance = new DatabaseClient();
            return instance;
        }
    }

    /*  ************************************************************************************  *
     *  USER METHODS
     *  ************************************************************************************  */

    public Observable<User> selectUser(final long selectUserId)
    {
        return databaseInterface.selectUser(sharedPreferences.getLong(DatabaseTestActivity.SPK_USERID, 0L),
            sharedPreferences.getString(DatabaseTestActivity.SPK_ACCESSTOKEN, ""),
            sharedPreferences.getLong(DatabaseTestActivity.SPK_LANGUAGEID, 1031L), selectUserId);
    }

    public Observable<List<User>> selectUsers()
    {
        return databaseInterface.selectUsers(sharedPreferences.getLong(DatabaseTestActivity.SPK_USERID, 0L),
            sharedPreferences.getString(DatabaseTestActivity.SPK_ACCESSTOKEN, ""),
            sharedPreferences.getLong(DatabaseTestActivity.SPK_LANGUAGEID, 1031L));
    }

    public Observable<List<User>> selectUsers(final SelectModifier... modifiers)
    {
        return databaseInterface.selectUsers(sharedPreferences.getLong(DatabaseTestActivity.SPK_USERID, 0L),
            sharedPreferences.getString(DatabaseTestActivity.SPK_ACCESSTOKEN, ""),
            sharedPreferences.getLong(DatabaseTestActivity.SPK_LANGUAGEID, 1031L));
    }

    public Call<CreateUserResult> createUser(final String userName, final String firstName,
        final String lastName, final String emailAddress, final String hashedPassword,
        final String salt, final String accessToken, final ELinkedProfileType linkedProfileType,
        final String linkedProfileUserId, final long userRights, final String deviceId)
    {
        long languageId = sharedPreferences.getLong(DatabaseTestActivity.SPK_LANGUAGEID, 1031L);
        return databaseInterface.createUser(languageId, userName, firstName, lastName,
            emailAddress, hashedPassword, salt, accessToken,
            linkedProfileType != null ? linkedProfileType.toString() : null,
            linkedProfileUserId, userRights, deviceId);
    }

    /**
     * Checks if a user identified by either a login ID (handled as either user name or e-mail
     *      address), user name or e-mail address.
     * @param loginId the login ID to check against. Useful for login screens with only one input
     *      field that allows both user name and e-mail address. Either-or-check is performed on
     *      the database side.
     * @param userName the user name to check if exists.
     * @param emailAddress the e-mail address to check if exists.
     * @param pullSalt
     * @return
     */
    public Call<ExistsUserResult> existsUser(final String loginId, final String userName,
        final String emailAddress, boolean pullSalt)
    {
        return databaseInterface.existsUser(loginId, userName, emailAddress,
            pullSalt ? "true" : "false");
    }

    /**
     * Authenticates a user identified by either a user ID, user name or e-mail address and returns
     *      a AuthenticateUserResult that holds detailed information about the authentication
     *      result.
     * @param userId the user ID of the user to be authenticated (first priority
     * @param userName the user name of the user to be authenticated (second priority)
     * @param emailAddress the e-mail address of the user to be authenticated (third priority)
     * @param hashedPassword the hashed password to check against the hashed password stored in the
     *      database
     * @param accessToken the new access token that will be used to create a new login in case of a
     *      successful authentication
     * @param deviceId an identifier of the device the login takes place. This is used to
     *      distinguish between different logins and to remove obsolete ones
     * @return a AuthenticateUserResult object within a Call to it; null if an error occurred
     */
    public Call<AuthenticateUserResult> authenticateUser(final long userId, final String userName,
        final String emailAddress, final String hashedPassword, final String accessToken,
        final String deviceId)
    {
        return databaseInterface.authenticateUser(userId, userName, emailAddress, hashedPassword,
            accessToken, deviceId);
    }

    /**
     * Refreshes the current login that is associated with the stored user ID and access token
     * @return a RefreshLoginResult object within a Call to it; null if an error occurred or there
     *      is currently no user logged in (user ID or access token in shared preferences not set)
     */
    public Call<RefreshLoginResult> refreshLogin()  {

        final long userId = sharedPreferences.getLong(DatabaseTestActivity.SPK_USERID, 0L);
        final String accessToken = sharedPreferences.getString(DatabaseTestActivity.SPK_ACCESSTOKEN, "");
        return userId != 0 && accessToken != null && accessToken.length() > 0 ?
            databaseInterface.refreshLogin(userId, accessToken) : null;
    }

    /*  ************************************************************************************  *
     *  CATEGORY METHODS
     *  ************************************************************************************  */

    public Observable<List<Category>> selectCategories()
    {
        return databaseInterface.selectCategories(sharedPreferences.getLong(DatabaseTestActivity.SPK_USERID, 0L),
            sharedPreferences.getString(DatabaseTestActivity.SPK_ACCESSTOKEN, ""),
            sharedPreferences.getLong(DatabaseTestActivity.SPK_LANGUAGEID, 1031L));
    }

    /**
     * Writes a category back to the database. Does only update changed fields.
     * @param category
     * @return
     */
    public Observable<Integer> updateCategory(final Category category)
    {
        long changeState = 0;
        if(category == null || (changeState = category.getChangeState()) == 0)
            return null;
        long languageId = sharedPreferences.getLong(DatabaseTestActivity.SPK_LANGUAGEID, 1031L);
        return databaseInterface.updateCategory(sharedPreferences.getLong(DatabaseTestActivity.SPK_USERID, 0L),
            sharedPreferences.getString(DatabaseTestActivity.SPK_ACCESSTOKEN, ""), languageId, category.getCategoryId(),
            (changeState & Category.CHANGED_PARENTCATEGORYID) == Category.CHANGED_PARENTCATEGORYID ? category.getParentCategoryId() : 0,
            (changeState & Category.CHANGED_NAME) == Category.CHANGED_NAME ? category.getName(languageId) : null,
            (changeState & Category.CHANGED_DESCRIPTION) == Category.CHANGED_DESCRIPTION ? category.getDescription(languageId) : null,
            (changeState & Category.CHANGED_IMAGEID) == Category.CHANGED_IMAGEID ? category.getImageId() : 0,
            (changeState & Category.CHANGED_IMAGEFILENAME) == Category.CHANGED_IMAGEFILENAME ? category.getImageFileName() : null,
            (changeState & Category.CHANGED_SORTPREFIX) == Category.CHANGED_SORTPREFIX ? category.getSortPrefix() : null,
            (changeState & Category.CHANGED_BROWSABLE) == Category.CHANGED_BROWSABLE ? (category.isBrowsable() ? 1 : 0 ) : -1);
    }
}
