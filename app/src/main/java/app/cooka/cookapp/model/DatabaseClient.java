package app.cooka.cookapp.model;

import android.support.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import app.cooka.cookapp.Settings;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

public class DatabaseClient {

    private static final String DATABASE_BASE_URL = "https://www.sebastianzander.de/cooka/";

    public static SimpleDateFormat databaseDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private IDatabase databaseInterface;
    private Category.JsonAdapter categoryJsonAdapter;

    private DatabaseClient() {

        categoryJsonAdapter = new Category.JsonAdapter(Settings.getInstance().getCurrentLanguageId());

        final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Category.class, categoryJsonAdapter)
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .create();

        final Retrofit retrofit = new Retrofit.Builder().baseUrl(DATABASE_BASE_URL)
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


    public Observable<List<Category>> getCategories(@NonNull long userId, @NonNull String userAccessToken,
        @NonNull long languageId)
    {
        return databaseInterface.getCategories(userId, userAccessToken, languageId);
    }

    /**
     * Writes a category back to the database. Does only update changed fields.
     * @param userId
     * @param userAccessToken
     * @param languageId
     * @param category
     * @return
     */
    public Observable<Integer> setCategory(@NonNull long userId, @NonNull String userAccessToken,
        @NonNull long languageId, final Category category)
    {
        long changeState = 0;
        if(category == null || (changeState = category.getChangeState()) == 0)
            return null;
        return databaseInterface.setCategory(userId, userAccessToken, languageId, category.getCategoryId(),
            (changeState & Category.CHANGED_PARENTCATEGORYID) == Category.CHANGED_PARENTCATEGORYID ? category.getParentCategoryId() : 0,
            (changeState & Category.CHANGED_NAME) == Category.CHANGED_NAME ? category.getName(languageId) : null,
            (changeState & Category.CHANGED_DESCRIPTION) == Category.CHANGED_DESCRIPTION ? category.getDescription(languageId) : null,
            (changeState & Category.CHANGED_IMAGEID) == Category.CHANGED_IMAGEID ? category.getImageId() : 0,
            (changeState & Category.CHANGED_IMAGEFILENAME) == Category.CHANGED_IMAGEFILENAME ? category.getImageFileName() : null,
            (changeState & Category.CHANGED_SORTPREFIX) == Category.CHANGED_SORTPREFIX ? category.getSortPrefix() : null,
            (changeState & Category.CHANGED_BROWSABLE) == Category.CHANGED_BROWSABLE ? (category.isBrowsable() ? 1 : 0 ) : -1);
    }
}
