package app.cooka.cookapp.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
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
    public static final int ST_FOLLOWED_USER = 0x00000001;
    public static final int ST_FOLLOWED_TAG = 0x00000002;
    public static final int ST_FOLLOWED_COLLECTION = 0x00000004;
    public static final int ST_CREATED_RECIPE = 0x00000008;
    public static final int ST_MODIFIED_RECIPE = 0x00000010;
    public static final int ST_COOKED_RECIPE = 0x00000020;
    public static final int ST_CREATED_COLLECTION = 0x00000040;
    public static final int ST_ADDED_RECIPE_TO_COLLECTION = 0x00000080;
    public static final int ST_ADDED_IMAGE_TO_RECIPE = 0x00000100;
    public static final int ST_ALL = 0xffffffff;

    private EFeedMessageType type;
    private int count;
    private String message;

    private long userId;
    private String userName;
    private String userImageFileName;

    private List<Long> object1Ids;
    private List<String> object1Names;
    private List<String> object1ImageFileNames;

    private long object2Id;

    private List<Date> performedDateTimes;

    private FeedMessage(String type, int count, String message, long userId, String userName,
        String userImageFileName, List<Long> object1Ids, List<String> object1Names,
        List<String> object1ImageFileNames, long object2Id, List<String> performedDateTimes)
    {
        try {
            if(type.length() > 0)
                this.type = EFeedMessageType.valueOf(type);
        }
        catch(IllegalArgumentException e) {
            e.printStackTrace();
            Log.e(LOGTAG, "could not parse type while creating feed message.");
        }

        this.count = count;
        this.message = message;
        this.userId = userId;
        this.userName = userName;
        this.userImageFileName = userImageFileName;
        this.object1Ids = object1Ids;
        this.object1Names = object1Names;
        this.object1ImageFileNames = object1ImageFileNames;
        this.object2Id = object2Id;

        this.performedDateTimes = new ArrayList<>();
        for(String date : performedDateTimes) {
            try {
                if(date.length() > 0)
                    this.performedDateTimes.add(DatabaseClient.databaseDateFormat.parse(date));
                else
                    this.performedDateTimes.add(new Date());
            }
            catch(ParseException e) {
                Log.e(LOGTAG, "could not parse performedDateTimes while creating feed message.");
                this.performedDateTimes.add(new Date());
            }
        }
    }

    public static class Factory {

        /**
         * Selects all feed messages of the user specified by the user id and allows for filtering
         * by specifying what message types shall be selected.
         * @param context the Android context to run this method in.
         * @param ofuserId the identifier of the user to create the feed for (i.e. the user whose
         *      own actions and whose followee's actions shall be selected).
         * @param selectedTypes a bit mask used to combine different feed message types; uses the
         *      integer constants FeedMessage.ST_* that can be combined with bitwise-or.
         *      For example: FeedMessage.ST_CREATED_RECIPE | ST_COOKED_RECIPE
         * @param onlyOwnMessages whether or not only messages where ofuserId is the performer of
         *      the described action shall be selected (i.e. omits messages of users that ofuserId
         *      is following).
         * @param selectFeedMessagesCallback the select callback that will be called when the
         *      selection succeeded and that will be given the list of feed messages.
         * @return a subscription object to the select request; null if an error occurred.
         */
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

    public int getCount() {
        return count;
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
        return object1Ids != null && object1Ids.size() > 0 ? object1Ids.get(0) : -1L;
    }

    public long getObject1Id(int index) {
        return object1Ids != null && object1Ids.size() > 0 && index < object1Ids.size() ?
            object1Ids.get(index) : -1L;
    }

    public List<Long> getObject1Ids() {
        return object1Ids;
    }

    public String getObject1Name() {
        return object1Names != null && object1Names.size() > 0 ? object1Names.get(0) : "";
    }

    public String getObject1Name(int index) {
        return object1Names != null && object1Names.size() > 0 && index < object1Names.size() ?
            object1Names.get(index) : "";
    }

    public List<String> getObject1Names() {
        return object1Names;
    }

    public String getObject1ImageFileName() {
        return object1ImageFileNames != null && object1ImageFileNames.size() > 0 ?
            object1ImageFileNames.get(0) : "";
    }

    public String getObject1ImageFileName(int index) {
        return object1ImageFileNames != null && object1ImageFileNames.size() > 0 && index <
            object1ImageFileNames.size() ? object1ImageFileNames.get(index) : "";
    }

    public List<String> getObject1ImageFileNames() {
        return object1ImageFileNames;
    }

    public long getObject2Id() {
        return object2Id;
    }

    public Date getPerformedDateTime() {
        return performedDateTimes != null && performedDateTimes.size() > 0 ? performedDateTimes.get(0) : null;
    }

    public Date getPerformedDateTime(int index) {
        return performedDateTimes != null && performedDateTimes.size() > 0 && index <
            performedDateTimes.size() ? performedDateTimes.get(index) : null;
    }

    public List<Date> getPerformedDateTimes() {
        return performedDateTimes;
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
            int count = in.nextInt();

            in.nextName();
            String message = in.nextString();

            in.nextName();
            long userId = in.nextLong();

            in.nextName();
            String userName = in.nextString();

            in.nextName();
            String userImageFileName = in.nextString();

            in.nextName();
            List<Long> object1Ids = new ArrayList<>();
            // deserialize a |-separated list of object 1 ids
            if(count > 1) {
                String ids = in.nextString();
                String[] object1IdStrings = ids.split("\\|");
                for(String object1IdString : object1IdStrings) {
                    long object1Id = Long.parseLong(object1IdString);
                    object1Ids.add(object1Id);
                }
            }
            // deserialize a single object 1 id
            else {
                long object1Id = in.nextLong();
                object1Ids.add(object1Id);
            }

            in.nextName();
            List<String> object1Names = new ArrayList<>();
            String name = in.nextString();
            if(count > 1) {
                String[] object1NameStrings = name.split("\\|");
                for(String object1NameString : object1NameStrings) {
                    object1Names.add(object1NameString);
                }
            }
            else {
                object1Names.add(name);
            }

            in.nextName();
            List<String> object1ImageFileNames = new ArrayList<>();
            String imageFileName = in.nextString();
            if(count > 1) {
                String[] object1ImageFileNameStrings = imageFileName.split("\\|");
                for(String object1ImageFileNameString : object1ImageFileNameStrings) {
                    object1ImageFileNames.add(object1ImageFileNameString);
                }
            }
            else {
                object1ImageFileNames.add(imageFileName);
            }

            in.nextName();
            long object2Id = in.nextLong();

            in.nextName();
            List<String> performedDateTimes = new ArrayList<>();
            String dateTime = in.nextString();
            if(count > 1) {
                String[] performedDateTimeStrings = dateTime.split("\\|");
                for(String performedDateTimeString : performedDateTimeStrings) {
                    performedDateTimes.add(performedDateTimeString);
                }
            }
            else {
                performedDateTimes.add(dateTime);
            }

            in.endObject();

            return new FeedMessage(type, count, message, userId, userName, userImageFileName, object1Ids,
                object1Names, object1ImageFileNames, object2Id, performedDateTimes);
        }
    }
}
