package app.cooka.cookapp.model;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public interface IDatabase {

    /*  ************************************************************************************  *
     *  FEED MESSAGE METHODS
     *  ************************************************************************************  */

    @POST("database.php?action=selectFeedMessages")
    Observable<List<FeedMessage>> selectFeedMessages(
        @Query("userId") long userId,
        @Query("accessToken") String accessToken,
        @Query("languageId") long languageId,
        @Query("ofuserId") long ofuserId,
        @Query("selectedTypes") int selectedTypes,
        @Query("onlyOwnMessages") boolean onlyOwnMessages,
        @Query("startDate") String startDate,
        @Query("prettyPrint") boolean prettyPrint
    );

    /*  ************************************************************************************  *
     *  USER METHODS
     *  ************************************************************************************  */

    @POST("database.php?action=selectUser")
    Observable<User> selectUser(
        @Query("userId") long userId,
        @Query("accessToken") String accessToken,
        @Query("languageId") long languageId,
        @Query("selectUserId") long selectUserId
    );

    @POST("database.php?action=selectUsers")
    Observable<List<User>> selectUsers(
        @Query("userId") long userId,
        @Query("accessToken") String accessToken,
        @Query("languageId") long languageId
    );

    @POST("database.php?action=createUser")
    Call<CreateUserResult> createUser(
        @Query("languageId") long languageId,
        @Query("userName") String userName,
        @Query("firstName") String firstName,
        @Query("lastName") String lastName,
        @Query("emailAddress") String emailAddress,
        @Query("hashedPassword") String hashedPassword,
        @Query("salt") String salt,
        @Query("accessToken") String accessToken,
        @Query("linkedProfileType") String linkedProfileType,
        @Query("linkedProfileUserId") String linkedProfileUserId,
        @Query("profileImageFileName") String profileImageFileName,
        @Query("userRights") long userRights,
        @Query("deviceId") String deviceId
    );

    @POST("database.php?action=selectUserFollowers")
    Observable<List<Follower>> selectUserFollowers(
        @Query("userId") long userId,
        @Query("accessToken") String accessToken,
        @Query("ofuserId") long ofuserId
    );

    @POST("database.php?action=selectTagFollowers")
    Observable<List<Follower>> selectTagFollowers(
        @Query("userId") long userId,
        @Query("accessToken") String accessToken,
        @Query("oftagId") long oftagId
    );

    @POST("database.php?action=selectCollectionFollowers")
    Observable<List<Follower>> selectCollectionFollowers(
        @Query("userId") long userId,
        @Query("accessToken") String accessToken,
        @Query("ofcollectionId") long ofcollectionId
    );

    @POST("database.php?action=selectUserFollowees")
    Observable<List<Followee>> selectUserFollowees(
        @Query("userId") long userId,
        @Query("accessToken") String accessToken,
        @Query("languageId") long languageId,
        @Query("ofuserId") long ofuserId
    );

    @POST("database.php?action=existsUser")
    Call<ExistsUserResult> existsUser(
        @Query("loginId") String loginId,
        @Query("userName") String userName,
        @Query("emailAddress") String emailAddress,
        @Query("pullSalt") String pullSalt
    );

    @POST("database.php?action=authenticateUser")
    Call<AuthenticateUserResult> authenticateUser(
        @Query("userId") long userId,
        @Query("userName") String userName,
        @Query("emailAddress") String emailAddress,
        @Query("hashedPassword") String hashedPassword,
        @Query("accessToken") String accessToken,
        @Query("deviceId") String deviceId
    );

    @POST("database.php?action=refreshLogin")
    Call<RefreshLoginResult> refreshLogin(
        @Query("userId") long userId,
        @Query("accessToken") String accessToken
    );

    @POST("database.php?action=invalidateLogin")
    Call<InvalidateLoginResult> invalidateLogin(
        @Query("userId") long userId,
        @Query("accessToken") String accessToken
    );

    /*  ************************************************************************************  *
     *  RECIPE METHODS
     *  ************************************************************************************  */

    @POST("database.php?action=selectRecipes")
    Observable<List<Recipe>> selectRecipes(
        @Query("userId") long userId,
        @Query("accessToken") String accessToken,
        @Query("languageId") long languageId,
        @Query("filterKey") List<String> filterKeys,
        @Query("sortKey") List<String> sortKeys,
        @Query("limit") long limit,
        @Query("offset") long offset
    );

    /*  ************************************************************************************  *
     *  CATEGORY METHODS
     *  ************************************************************************************  */

    @POST("database.php?action=selectCategory")
    Observable<Category> selectCategory(
        @Query("userId") long userId,
        @Query("accessToken") String accessToken,
        @Query("languageId") long languageId,
        @Query("categoryId") long categoryId
    );

    @POST("database.php?action=selectCategories")
    Observable<List<Category>> selectCategories(
        @Query("userId") long userId,
        @Query("accessToken") String accessToken,
        @Query("languageId") long languageId
    );

    @POST("database.php?action=updateCategory")
    Observable<Integer> updateCategory(
        @Query("userId") long userId,
        @Query("accessToken") String accessToken,
        @Query("languageId") long languageId,
        @Query("categoryId") long categoryId,
        @Query("parentCategoryId") long parentCategoryId,
        @Query("name") String name,
        @Query("description") String description,
        @Query("imageId") long imageId,
        @Query("imageFileName") String imageFileName,
        @Query("sortPrefix") String sortPrefix,
        @Query("browsable") int browsable
    );


    // todo: create all kinds of additional database functions for reading from and writing to database through our web service
}
