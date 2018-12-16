package app.cooka.cookapp.model;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public interface IDatabase {

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
        @Query("userRights") long userRights,
        @Query("deviceId") String deviceId
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

    @POST("database.php?action=listCategories")
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
