package app.cooka.cookapp.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A class that represents a feed or activity log message to be displayed in recipe feeds and on
 * user profiles.
 */
@JsonAdapter(FeedMessage.JsonAdapter.class)
public class FeedMessage {

    public static final String LOGTAG = "COOKALOG";

    // selected types
    public static final int ST_FOLLOWED_USER = 1;
    public static final int ST_FOLLOWED_TAG = 1 << 1;
    public static final int ST_FOLLOWED_COLLECTION = 1 << 2;
    public static final int ST_CREATED_RECIPE = 1 << 3;
    public static final int ST_MODIFIED_RECIPE = 1 << 4;
    public static final int ST_COOKED_RECIPE = 1 << 5;
    public static final int ST_CREATED_COLLECTION = 1 << 6;
    public static final int ST_ADDED_RECIPE_TO_COLLECTION = 1 << 7;
    public static final int ST_ADDED_IMAGE_TO_RECIPE = 1 << 8;
    public static final int ST_ALL = 0xffffffff;

    private EFeedMessageType type;
    private String message;

    private long userId;
    private String userName;
    private String userImageFileName;

    private long object1Id;
    private String object1Name;
    private String object1ImageFileName;

    private long object2Id;

    private Date performedDateTime;

    private FeedMessage(String type, String message, long userId, String userName,
        String userImageFileName, long object1Id, String object1Name,
        String object1ImageFileName, long object2Id, String performedDateTime)
    {
        try {
            if(type.length() > 0)
                this.type = EFeedMessageType.valueOf(type);
        }
        catch(IllegalArgumentException e) {
            e.printStackTrace();
            Log.e(LOGTAG, "could not parse type while creating feed message.");
        }

        this.message = message;
        this.userId = userId;
        this.userName = userName;
        this.userImageFileName = userImageFileName;
        this.object1Id = object1Id;
        this.object1Name = object1Name;
        this.object1ImageFileName = object1ImageFileName;
        this.object2Id = object2Id;

        try {
            if(performedDateTime.length() > 0)
                this.performedDateTime = DatabaseClient.databaseDateFormat.parse(performedDateTime);
        }
        catch(ParseException e) {
            e.printStackTrace();
            Log.e(LOGTAG, "could not parse performedDateTime while creating feed message.");
        }
    }

    public static class Factory {

        public static Subscription selectFeedMessages(final Context context, final long ofuserId,
            final int selectedTypes, final boolean onlyOwnMessages,
            final IResultCallback<List<FeedMessage>> selectFeedMessagesCallback)
        {
            if(selectFeedMessagesCallback == null) {
                throw new NullPointerException("select feed messages callback is null");
            }
            return DatabaseClient.Factory.getInstance(context)
                .selectFeedMessages(ofuserId, selectedTypes, onlyOwnMessages)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<FeedMessage>>() {
                    @Override public void onCompleted() {}
                    @Override public void onError(Throwable e) {
                        Log.e(LOGTAG, "FeedMessage.Factory.selectFeedMessages() failed");
                        Log.e(LOGTAG, e.getMessage());
                    }
                    @Override public void onNext(List<FeedMessage> feedMessages) {
                        selectFeedMessagesCallback.onSucceeded(feedMessages);
                    }
                });
        }
    }

    public EFeedMessageType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserImageFileName() {
        return userImageFileName;
    }

    public long getObject1Id() {
        return object1Id;
    }

    public String getObject1Name() {
        return object1Name;
    }

    public String getObject1ImageFileName() {
        return object1ImageFileName;
    }

    public long getObject2Id() {
        return object2Id;
    }

    public Date getPerformedDateTime() {
        return performedDateTime;
    }

    /**
     * A JSON type adapter that is responsible for FeedMessage object serialization
     */
    public static class JsonAdapter extends TypeAdapter<FeedMessage> {

        @Override
        public void write(JsonWriter out, FeedMessage feedMessage) throws IOException {}

        @Override
        public FeedMessage read(JsonReader in) throws IOException {
            in.beginObject();

            in.nextName();
            String type = in.nextString();

            in.nextName();
            String message = in.nextString();

            in.nextName();
            long userId = in.nextLong();

            in.nextName();
            String userName = in.nextString();

            in.nextName();
            String userImageFileName = in.nextString();

            in.nextName();
            long object1Id = in.nextLong();

            in.nextName();
            String object1Name = in.nextString();

            in.nextName();
            String object1ImageFileName = in.nextString();

            in.nextName();
            long object2Id = in.nextLong();

            in.nextName();
            String performedDateTime = in.nextString();

            in.endObject();

            return new FeedMessage(type, message, userId, userName, userImageFileName, object1Id,
                object1Name, object1ImageFileName, object2Id, performedDateTime);
        }
    }
}