package app.cooka.cookapp.model;

import java.util.List;

import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public interface IDatabase {

    @POST("database.php?action=listCategories")
    Observable<List<Category>> getCategories(
        @Query("userId") long userId,
        @Query("userAccessToken") String userAccessToken,
        @Query("languageId") long languageId);


    @POST("database.php?action=updateCategory")
    Observable<Integer> setCategory(
        @Query("userId") long userId,
        @Query("userAccessToken") String userAccessToken,
        @Query("languageId") long languageId,
        @Query("categoryId") long categoryId,
        @Query("parentCategoryId") long parentCategoryId,
        @Query("name") String name,
        @Query("description") String description,
        @Query("imageId") long imageId,
        @Query("imageFileName") String imageFileName,
        @Query("sortPrefix") String sortPrefix,
        @Query("browsable") int browsable);


    // todo: create all kinds of additional database functions for reading from and writing to database through our web service
}
