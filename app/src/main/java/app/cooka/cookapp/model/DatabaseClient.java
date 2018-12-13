package app.cooka.cookapp.model;

import android.support.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

public class DatabaseClient {

    private static final String DATABASE_BASE_URL = "https://www.sebastianzander.de/cooka/";

    private IDatabase databaseInterface;

    private DatabaseClient() {

        final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Category.class, new CategoryJsonAdapter(1031))
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

    public Observable<List<Category>> getCategories(@NonNull long userId, @NonNull String userAccessToken, @NonNull long languageId) {
        return databaseInterface.getCategories(userId, userAccessToken, languageId);
    }
}
