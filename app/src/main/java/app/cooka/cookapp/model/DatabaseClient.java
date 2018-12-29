package app.cooka.cookapp.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.List;

import app.cooka.cookapp.DatabaseTestActivity;
import app.cooka.cookapp.Settings;
import app.cooka.cookapp.login.LoginManager;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

public class DatabaseClient {

    private static final String PREFERENCES_NAME = "userdata";
    private static final String DATABASE_BASE_URL = "https://www.sebastianzander.de/cooka/";
    public static final SimpleDateFormat databaseDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private SharedPreferences sharedPreferences;
    private IDatabase databaseInterface;
    private boolean jsonPrettyPrint = false;

    private DatabaseClient(Context context) {

        sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

        final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Category.class, new Category.JsonAdapter())
            .registerTypeAdapter(Recipe.class, new Recipe.JsonAdapter())
            .registerTypeAdapter(FeedMessage.class, new FeedMessage.JsonAdapter())
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
        public static DatabaseClient getInstance(Context context) {
            if(instance == null)
                instance = new DatabaseClient(context);
            return instance;
        }
    }

    /*  ************************************************************************************  *
     *  FEED MESSAGE METHODS
     *  ************************************************************************************  */

    /**
     * Selects all {@linkplain FeedMessage}s of the user specified by the user id and allows for
     * filtering by specifying what message types shall be selected.
     * @param ofuserId the identifier of the user to create the feed for (i.e. the user whose own
     *      actions and whose followee's actions shall be selected).
     * @param selectedTypes a bit mask used to combine different feed message types; uses the
     *      integer constants {@linkplain FeedMessage}.ST_* that can be combined with bitwise-or.
     *      For example: {@code FeedMessage.ST_CREATED_RECIPE | FeedMessage.ST_COOKED_RECIPE}
     * @param onlyOwnMessages whether or not only messages where ofuserId is the performer of the
     *      described action shall be selected (i.e. omits messages of users that ofuserId is
     *      following).
     * @return a list of {@linkplain FeedMessage}s within an {@linkplain Observable};
     *      null if an error occurred.
     */
    public Observable<List<FeedMessage>> selectFeedMessages(final long ofuserId,
        final int selectedTypes, final boolean onlyOwnMessages)
    {
        return databaseInterface.selectFeedMessages(
            sharedPreferences.getLong(LoginManager.SPK_USERID, 0L),
            sharedPreferences.getString(LoginManager.SPK_ACCESSTOKEN, ""),
            sharedPreferences.getLong(LoginManager.SPK_LANGUAGEID, 1031L), ofuserId,
            selectedTypes, onlyOwnMessages, null, jsonPrettyPrint);
    }

    /**
     * Selects all {@linkplain FeedMessage}s of the user specified by the user id and allows for
     * filtering by specifying what message types shall be selected.
     * @param ofuserId the identifier of the user to create the feed for (i.e. the user whose own
     *      actions and whose followee's actions shall be selected).
     * @param selectedTypes a bit mask used to combine different feed message types; uses the
     *      integer constants {@linkplain FeedMessage}.ST_* that can be combined with bitwise-or.
     *      For example: {@code FeedMessage.ST_CREATED_RECIPE | FeedMessage.ST_COOKED_RECIPE}
     * @param onlyOwnMessages whether or not only messages where ofuserId is the performer of the
     *      described action shall be selected (i.e. omits messages of users that ofuserId is
     *      following).
     * @param startDate the latest date of which to select feed messages from (used for pagination
     *      and as a natural offset to the result set).
     * @return a list of {@linkplain FeedMessage}s within an {@linkplain Observable};
     *      null if an error occurred.
     */
    public Observable<List<FeedMessage>> selectFeedMessages(final long ofuserId,
        final int selectedTypes, final boolean onlyOwnMessages, final String startDate)
    {
        return databaseInterface.selectFeedMessages(
            sharedPreferences.getLong(LoginManager.SPK_USERID, 0L),
            sharedPreferences.getString(LoginManager.SPK_ACCESSTOKEN, ""),
            sharedPreferences.getLong(LoginManager.SPK_LANGUAGEID, 1031L), ofuserId,
            selectedTypes, onlyOwnMessages, startDate, jsonPrettyPrint);
    }

    /*  ************************************************************************************  *
     *  USER METHODS
     *  ************************************************************************************  */

    public Observable<User> selectUser(final long selectUserId) {

        return databaseInterface.selectUser(
            sharedPreferences.getLong(LoginManager.SPK_USERID, 0L),
            sharedPreferences.getString(LoginManager.SPK_ACCESSTOKEN, ""),
            sharedPreferences.getLong(LoginManager.SPK_LANGUAGEID, 1031L), selectUserId);
    }

    public Observable<List<User>> selectUsers() {

        return databaseInterface.selectUsers(
            sharedPreferences.getLong(LoginManager.SPK_USERID, 0L),
            sharedPreferences.getString(LoginManager.SPK_ACCESSTOKEN, ""),
            sharedPreferences.getLong(LoginManager.SPK_LANGUAGEID, 1031L));
    }

    public Observable<List<User>> selectUsers(final SelectModifier... modifiers) {

        return databaseInterface.selectUsers(
            sharedPreferences.getLong(LoginManager.SPK_USERID, 0L),
            sharedPreferences.getString(LoginManager.SPK_ACCESSTOKEN, ""),
            sharedPreferences.getLong(LoginManager.SPK_LANGUAGEID, 1031L));
    }

    public Observable<List<Follower>> selectUserFollowers(final long ofuserId) {

        return databaseInterface.selectUserFollowers(
            sharedPreferences.getLong(LoginManager.SPK_USERID, 0L),
            sharedPreferences.getString(LoginManager.SPK_ACCESSTOKEN, ""), ofuserId);
    }

    public Observable<List<Follower>> selectTagFollowers(final long oftagId) {

        return databaseInterface.selectTagFollowers(
            sharedPreferences.getLong(LoginManager.SPK_USERID, 0L),
            sharedPreferences.getString(LoginManager.SPK_ACCESSTOKEN, ""), oftagId);
    }

    public Observable<List<Follower>> selectCollectionFollowers(final long ofcollectionId) {

        return databaseInterface.selectCollectionFollowers(
            sharedPreferences.getLong(LoginManager.SPK_USERID, 0L),
            sharedPreferences.getString(LoginManager.SPK_ACCESSTOKEN, ""), ofcollectionId);
    }

    public Observable<List<Followee>> selectUserFollowees(final long ofuserId) {

        return databaseInterface.selectUserFollowees(
            sharedPreferences.getLong(LoginManager.SPK_USERID, 0L),
            sharedPreferences.getString(LoginManager.SPK_ACCESSTOKEN, ""),
            sharedPreferences.getLong(LoginManager.SPK_LANGUAGEID, 1031L), ofuserId);
    }

    public Call<CreateUserResult> createUser(final String userName, final String firstName,
        final String lastName, final String emailAddress, final String hashedPassword,
        final String salt, final String accessToken, final ELinkedProfileType linkedProfileType,
        final String linkedProfileUserId, final long userRights, final String deviceId)
    {
        long languageId = sharedPreferences.getLong(LoginManager.SPK_LANGUAGEID, 1031L);
        return databaseInterface.createUser(languageId, userName, firstName, lastName,
            emailAddress, hashedPassword, salt, accessToken,
            linkedProfileType != null ? linkedProfileType.toString() : null,
            linkedProfileUserId, null, userRights, deviceId);
    }

    public Call<CreateUserResult> createUser(final String userName, final String firstName,
        final String lastName, final String emailAddress, final String hashedPassword,
        final String salt, final String accessToken, final ELinkedProfileType linkedProfileType,
        final String linkedProfileUserId, final String profileImageFileName, final long
        userRights, final String deviceId)
    {
        long languageId = sharedPreferences.getLong(LoginManager.SPK_LANGUAGEID, 1031L);
        return databaseInterface.createUser(languageId, userName, firstName, lastName,
            emailAddress, hashedPassword, salt, accessToken,
            linkedProfileType != null ? linkedProfileType.toString() : null,
            linkedProfileUserId, profileImageFileName, userRights, deviceId);
    }

    /**
     * Checks if a user - identified by either a login ID (handled as either user name or e-mail
     *      address), user name or e-mail address - exists.
     * @param loginId the login ID to check against. Useful for login screens with only one input
     *      field that allows both user name and e-mail address. Either-or-check is performed on
     *      the database side.
     * @param userName the user name to check if exists.
     * @param emailAddress the e-mail address to check if exists.
     * @param pullSalt pulls the salt and the user ID if set to true.
     * @return a {@linkplain ExistsUserResult} object within a Call to it; null if an error occurred
     */
    public Call<ExistsUserResult> existsUser(final String loginId, final String userName,
        final String emailAddress, boolean pullSalt)
    {
        return databaseInterface.existsUser(loginId, userName, emailAddress,
            pullSalt ? "true" : "false");
    }

    /**
     * Authenticates a user identified by either a user ID, user name or e-mail address and returns
     *      a {@linkplain AuthenticateUserResult} that holds detailed information about the
     *      authentication result.
     * @param userId the user ID of the user to be authenticated (first priority).
     * @param userName the user name of the user to be authenticated (second priority).
     * @param emailAddress the e-mail address of the user to be authenticated (third priority).
     * @param hashedPassword the hashed password to check against the hashed password stored in the
     *      database.
     * @param accessToken the new access token that will be used to create a new login in case of a
     *      successful authentication.
     * @param deviceId an identifier of the device the login takes place. This is used to
     *      distinguish between different logins and to remove obsolete ones.
     * @return a {@linkplain AuthenticateUserResult} object within a Call to it; null if an error
     *      occurred.
     */
    public Call<AuthenticateUserResult> authenticateUser(final long userId, final String userName,
        final String emailAddress, final String hashedPassword, final String accessToken,
        final String deviceId)
    {
        return databaseInterface.authenticateUser(userId, userName, emailAddress, hashedPassword,
            accessToken, deviceId);
    }

    /**
     * Refreshes the current login that is associated with the stored user ID and access token.
     * @return a {@linkplain RefreshLoginResult} object within a Call to it; null if an error
     *      occurred or there is currently no user logged in (user ID or access token in shared
     *      preferences not set).
     */
    public Call<RefreshLoginResult> refreshLogin() {

        final long userId = sharedPreferences.getLong(LoginManager.SPK_USERID, 0L);
        final String accessToken = sharedPreferences.getString(LoginManager.SPK_ACCESSTOKEN, "");
        return userId != 0 && accessToken != null && accessToken.length() > 0 ?
            databaseInterface.refreshLogin(userId, accessToken) : null;
    }

    public Call<RefreshLoginResult> refreshLogin(final long userId, final String accessToken) {

        return userId != 0 && accessToken != null && accessToken.length() > 0 ?
            databaseInterface.refreshLogin(userId, accessToken) : null;
    }

    /**
     * Invalidates the current login that is associated with the stored user ID and access token.
     * @return a {@linkplain InvalidateLoginResult} object within a Call to it; null if an error
     *      occurred or there is currently no user logged in (user ID or access token in shared
     *      preferences not set).
     */
    public Call<InvalidateLoginResult> invalidateLogin() {

        final long userId = sharedPreferences.getLong(LoginManager.SPK_USERID, 0L);
        final String accessToken = sharedPreferences.getString(LoginManager.SPK_ACCESSTOKEN, "");
        return userId != 0 && accessToken != null && accessToken.length() > 0 ?
            databaseInterface.invalidateLogin(userId, accessToken) : null;
    }

    public Call<InvalidateLoginResult> invalidateLogin(final long userId, final String
        accessToken)
    {
        return userId != 0 && accessToken != null && accessToken.length() > 0 ?
            databaseInterface.invalidateLogin(userId, accessToken) : null;
    }

    /*  ************************************************************************************  *
     *  RECIPE METHODS
     *  ************************************************************************************  */

    /**
     * Inserts a new {@linkplain Recipe} from a given recipe draft object into the database.
     * @param recipe the {@linkplain Recipe} draft object to be inserted into the database.
     * @param ignoreDuplicate whether or not to ignore duplicate recipes when there is already a
     *      recipe with the exact same title and description (default: false).
     * @return a {@linkplain CreateRecipeResult} within a {@linkplain Call} to it; null if an
     *      error occurred.
     */
    public Call<CreateRecipeResult> createRecipe(final Recipe recipe, boolean ignoreDuplicate) {

        final long userId = sharedPreferences.getLong(LoginManager.SPK_USERID, 0L);
        if(userId == 0L)
            return null;
        final String accessToken = sharedPreferences.getString(LoginManager.SPK_ACCESSTOKEN, "");
        final long languageId = sharedPreferences.getLong(LoginManager.SPK_LANGUAGEID, 1031L);
        return databaseInterface.createRecipe(userId, accessToken, languageId,
            ignoreDuplicate ? 1 : 0, recipe);
    }

    /**
     * Updates a {@linkplain Recipe} from a given recipe object in the database.
     * @param recipe the {@linkplain Recipe} object to be updated in the database.
     * @return a {@linkplain UpdateRecipeResult} within a {@linkplain Call} to it; null if an
     *      error occurred.
     */
    public Call<UpdateRecipeResult> updateRecipe(final Recipe recipe) {

        final long userId = sharedPreferences.getLong(LoginManager.SPK_USERID, 0L);
        if(userId == 0L)
            return null;
        final String accessToken = sharedPreferences.getString(LoginManager.SPK_ACCESSTOKEN, "");
        final long languageId = sharedPreferences.getLong(LoginManager.SPK_LANGUAGEID, 1031L);
        return databaseInterface.updateRecipe(userId, accessToken, languageId, recipe);
    }

    public Observable<Recipe> selectRecipe(final long recipeId) {

        final long userId = sharedPreferences.getLong(LoginManager.SPK_USERID, 0L);
        final String accessToken = sharedPreferences.getString(LoginManager.SPK_ACCESSTOKEN, "");
        final long languageId = sharedPreferences.getLong(LoginManager.SPK_LANGUAGEID, 1031L);
        return databaseInterface.selectRecipe(userId, accessToken, languageId, recipeId);
    }

    public Observable<Recipe> selectRecipe(final long recipeId, final int numCategoriesRequested,
        final int numTagsRequested, final int numRecipeStepsRequested, final int
        numRecipeRatingsRequested)
    {
        final long userId = sharedPreferences.getLong(LoginManager.SPK_USERID, 0L);
        final String accessToken = sharedPreferences.getString(LoginManager.SPK_ACCESSTOKEN, "");
        final long languageId = sharedPreferences.getLong(LoginManager.SPK_LANGUAGEID, 1031L);
        return databaseInterface.selectRecipe(userId, accessToken, languageId, recipeId,
            numCategoriesRequested, numTagsRequested, numRecipeStepsRequested,
            numRecipeRatingsRequested);
    }

    public Observable<List<Recipe>> selectRecipes(final List<String> filterKeys,
        final List<String> sortKeys, final long limit, final long offset)
    {
        final long userId = sharedPreferences.getLong(LoginManager.SPK_USERID, 0L);
        final String accessToken = sharedPreferences.getString(LoginManager.SPK_ACCESSTOKEN, "");
        final long languageId = sharedPreferences.getLong(LoginManager.SPK_LANGUAGEID, 1031L);
        return databaseInterface.selectRecipes(userId, accessToken, languageId, filterKeys,
            sortKeys, limit, offset);
    }

    /*  ************************************************************************************  *
     *  CATEGORY METHODS
     *  ************************************************************************************  */

    public Observable<List<Category>> selectCategories()
    {
        return databaseInterface.selectCategories(
            sharedPreferences.getLong(LoginManager.SPK_USERID, 0L),
            sharedPreferences.getString(LoginManager.SPK_ACCESSTOKEN, ""),
            sharedPreferences.getLong(LoginManager.SPK_LANGUAGEID, 1031L));
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
        long languageId = sharedPreferences.getLong(LoginManager.SPK_LANGUAGEID, 1031L);
        return databaseInterface.updateCategory(
            sharedPreferences.getLong(LoginManager.SPK_USERID, 0L),
            sharedPreferences.getString(LoginManager.SPK_ACCESSTOKEN, ""), languageId, category.getCategoryId(),
            (changeState & Category.CHANGED_PARENTCATEGORYID) == Category.CHANGED_PARENTCATEGORYID ? category.getParentCategoryId() : 0,
            (changeState & Category.CHANGED_NAME) == Category.CHANGED_NAME ? category.getName() : null,
            (changeState & Category.CHANGED_DESCRIPTION) == Category.CHANGED_DESCRIPTION ? category.getDescription() : null,
            (changeState & Category.CHANGED_IMAGEID) == Category.CHANGED_IMAGEID ? category.getImageId() : 0,
            (changeState & Category.CHANGED_IMAGEFILENAME) == Category.CHANGED_IMAGEFILENAME ? category.getImageFileName() : null,
            (changeState & Category.CHANGED_SORTPREFIX) == Category.CHANGED_SORTPREFIX ? category.getSortPrefix() : null,
            (changeState & Category.CHANGED_BROWSABLE) == Category.CHANGED_BROWSABLE ? (category.isBrowsable() ? 1 : 0 ) : -1);
    }
}
