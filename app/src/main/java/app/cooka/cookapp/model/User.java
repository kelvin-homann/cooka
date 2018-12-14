package app.cooka.cookapp.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;

/**
 * A class that represents a user and that is an app model representation of the corresponding
 * database entity called Users. Used to serialize between app and database and to cache its
 * state without needing to repeatedly query the same unchanged object from the database.
 */
@JsonAdapter(User.JsonAdapter.class)
public class User extends Observable {

    public static final String LOGTAG = "COOKALOG";

    public static final int CHANGED_USERNAME = 1;
    public static final int CHANGED_NAME = 1 << 1;
    public static final int CHANGED_EMAILADDRESS = 1 << 2;
    public static final int CHANGED_VERIFIEDSTATE = 1 << 3;
    public static final int CHANGED_USERRIGHTS = 1 << 4;
    public static final int CHANGED_LINKEDPROFILE = 1 << 5;
    public static final int CHANGED_LASTDATETIME = 1 << 6;
    public static final int CHANGED_PROFILEIMAGE = 1 << 7;
    public static final int CHANGED_FORCE_UPDATE = 0xffffffff;

    private int changeState = 0;

    private final long userId;
    private String userName;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private int confirmedEmailAddress;
    private int verifiedState;
    private long userRights;

    private ELinkedProfileType linkedProfileType;
    private String linkedProfileUserId;

    private long profileImageId;
    private String profileImageFileName;
    private Bitmap profileImage;

    private Date joinedDateTime;
    private Date lastActiveDateTime;
    private Date lastRecipeCreatedDateTime;
    private Date lastCollectionEditedDateTime;
    private Date lastCookModeUsedDateTime;

    private int viewedCount;
    private int followedCount;
    private int followingCount;

    public User(final long userId, String userName, String emailAddress, int userRights) {

        this.userId = userId;
        this.userName = userName;
        this.emailAddress = emailAddress;
        this.userRights = userRights;
    }

    public User(final long userId, String userName, String firstName, String lastName,
        String emailAddress, int confirmedEmailAddress, String linkedProfileType,
        String linkedProfileUserId, long profileImageId, String profileImageFileName,
        String joinedDateTime, String lastActiveDateTime, String lastRecipeCreatedDateTime,
        String lastCollectionEditedDateTime, String lastCookModeUsedDateTime, int viewedCount,
        int followedCount, int followingCount, int verifiedState, long userRights)
    {
        this.userId = userId;
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.confirmedEmailAddress = confirmedEmailAddress;
        try {
            this.linkedProfileType = ELinkedProfileType.valueOf(linkedProfileType);
        }
        catch(IllegalArgumentException e) {
            e.printStackTrace();
            Log.e(LOGTAG, String.format("could not parse linkedProfileType while creating user %d: %s. Set to null instead.", userId, userName));
        }
        this.linkedProfileUserId = linkedProfileUserId;
        this.profileImageId = profileImageId;
        this.profileImageFileName = profileImageFileName;
        try {
            this.joinedDateTime = DatabaseClient.databaseDateFormat.parse(joinedDateTime);
        }
        catch(ParseException e) {
            e.printStackTrace();
            Log.e(LOGTAG, String.format("could not parse joinedDateTime while creating user %d: %s. Set to null instead.", userId, userName));
        }
        try {
            this.lastActiveDateTime = DatabaseClient.databaseDateFormat.parse(lastActiveDateTime);
        }
        catch(ParseException e) {
            e.printStackTrace();
            Log.e(LOGTAG, String.format("could not parse lastActiveDateTime while creating user %d: %s. Set to null instead.", userId, userName));
        }
        try {
            this.lastRecipeCreatedDateTime = DatabaseClient.databaseDateFormat.parse(lastRecipeCreatedDateTime);
        }
        catch(ParseException e) {
            e.printStackTrace();
            Log.e(LOGTAG, String.format("could not parse lastRecipeCreatedDateTime while creating user %d: %s. Set to null instead.", userId, userName));
        }
        try {
            this.lastCollectionEditedDateTime = DatabaseClient.databaseDateFormat.parse(lastCollectionEditedDateTime);
        }
        catch(ParseException e) {
            e.printStackTrace();
            Log.e(LOGTAG, String.format("could not parse lastCollectionEditedDateTime while creating user %d: %s. Set to null instead.", userId, userName));
        }
        try {
            this.lastCookModeUsedDateTime = DatabaseClient.databaseDateFormat.parse(lastCookModeUsedDateTime);
        }
        catch(ParseException e) {
            e.printStackTrace();
            Log.e(LOGTAG, String.format("could not parse lastCookModeUsedDateTime while creating user %d: %s. Set to null instead.", userId, userName));
        }
        this.viewedCount = viewedCount;
        this.followedCount = followedCount;
        this.followingCount = followingCount;
        this.verifiedState = verifiedState;
        this.userRights = userRights;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        changeState |= CHANGED_USERNAME;
        setChanged();
        notifyObservers();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        changeState |= CHANGED_NAME;
        setChanged();
        notifyObservers();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        changeState |= CHANGED_NAME;
        setChanged();
        notifyObservers();
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
        changeState |= CHANGED_EMAILADDRESS;
        setChanged();
        notifyObservers();
    }

    public int getConfirmedEmailAddress() {
        return confirmedEmailAddress;
    }

    public void setConfirmedEmailAddress(int confirmedEmailAddress) {
        this.confirmedEmailAddress = confirmedEmailAddress;
    }

    public int getVerifiedState() {
        return verifiedState;
    }

    public void setVerifiedState(int verifiedState) {
        this.verifiedState = verifiedState;
        changeState |= CHANGED_VERIFIEDSTATE;
        setChanged();
        notifyObservers();
    }

    public long getUserRights() {
        return userRights;
    }

    public void setUserRights(long userRights) {
        this.userRights = userRights;
        changeState |= CHANGED_USERRIGHTS;
        setChanged();
        notifyObservers();
    }

    public void addUserRights(int rightsMask) {
        this.userRights |= rightsMask;
        changeState |= CHANGED_USERRIGHTS;
        setChanged();
        notifyObservers();
    }

    public void removeUserRights(int rightsMask) {
        this.userRights ^= rightsMask;
        changeState |= CHANGED_USERRIGHTS;
        setChanged();
        notifyObservers();
    }

    public ELinkedProfileType getLinkedProfileType() {
        return linkedProfileType;
    }

    public String getLinkedProfileUserId() {
        return linkedProfileUserId;
    }

    public void setLinkedProfile(ELinkedProfileType linkedProfileType, String linkedProfileUserId) {
        this.linkedProfileType = linkedProfileType;
        this.linkedProfileUserId = linkedProfileUserId;
        changeState |= CHANGED_LINKEDPROFILE;
        setChanged();
        notifyObservers();
    }

    public long getProfileImageId() {
        return profileImageId;
    }

    private void setProfileImageId(long profileImageId) {
        this.profileImageId = profileImageId;
        changeState |= CHANGED_PROFILEIMAGE;
        setChanged();
        notifyObservers();
    }

    public String getProfileImageFileName() {
        return profileImageFileName;
    }

    /**
     * Sets a new image file name. First looks in the image cache if the image is already cached
     * and chooses the cached version if up-to-date. Requests the image cache to download the image
     * if uncached or outdated.
     * @param profileImageFileName the relative image file name as it resides in the default image folder
     */
    private void setProfileImageFileName(String profileImageFileName) {

        if(this.profileImageFileName == null || this.profileImageFileName.compareTo(profileImageFileName) != 0) {
            this.profileImageFileName = profileImageFileName;
            changeState |= CHANGED_PROFILEIMAGE;

            if(profileImageFileName.length() > 0) {
                String imageUrl = "https://www.sebastianzander.de/cooka/img/" + profileImageFileName;
                Log.d(LOGTAG, String.format("downloading profile image from url %s for user %d: %s", imageUrl,
                    userId, userName));
                new DownloadImageTask(imageUrl, this).execute();
            }

            setChanged();
            notifyObservers();
        }
    }

    public Bitmap getProfileImage() {
        return profileImage;
    }

    /**
     * Sets the internal bitmap. Only used to set the actual bitmap after the image has been
     * requested. Is not used to change the actual image reference and does not cause database
     * synchronisation to happen.
     * @param profileImage
     */
    private void setProfileImage(Bitmap profileImage) {
        this.profileImage = profileImage;
        setChanged();
        notifyObservers();
    }

    public Date getJoinedDateTime() {
        return joinedDateTime;
    }

    public void setJoinedDateTime(Date joinedDateTime) {
        this.joinedDateTime = joinedDateTime;
    }

    public Date getLastActiveDateTime() {
        return lastActiveDateTime;
    }

    public void setLastActiveDateTime(Date lastActiveDateTime) {
        this.lastActiveDateTime = lastActiveDateTime;
        changeState |= CHANGED_LASTDATETIME;
        setChanged();
        notifyObservers();
    }

    public Date getLastRecipeCreatedDateTime() {
        return lastRecipeCreatedDateTime;
    }

    public void setLastRecipeCreatedDateTime(Date lastRecipeCreatedDateTime) {
        this.lastRecipeCreatedDateTime = lastRecipeCreatedDateTime;
        changeState |= CHANGED_LASTDATETIME;
        setChanged();
        notifyObservers();
    }

    public Date getLastCollectionEditedDateTime() {
        return lastCollectionEditedDateTime;
    }

    public void setLastCollectionEditedDateTime(Date lastCollectionEditedDateTime) {
        this.lastCollectionEditedDateTime = lastCollectionEditedDateTime;
        changeState |= CHANGED_LASTDATETIME;
        setChanged();
        notifyObservers();
    }

    public Date getLastCookModeUsedDateTime() {
        return lastCookModeUsedDateTime;
    }

    public void setLastCookModeUsedDateTime(Date lastCookModeUsedDateTime) {
        this.lastCookModeUsedDateTime = lastCookModeUsedDateTime;
        changeState |= CHANGED_LASTDATETIME;
        setChanged();
        notifyObservers();
    }

    public int getViewedCount() {
        return viewedCount;
    }

    public void setViewedCount(int viewedCount) {
        this.viewedCount = viewedCount;
        setChanged();
        notifyObservers();
    }

    public int getFollowedCount() {
        return followedCount;
    }

    public void setFollowedCount(int followedCount) {
        this.followedCount = followedCount;
        setChanged();
        notifyObservers();
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
        setChanged();
        notifyObservers();
    }

    /**
     * A simple asynchronous image download task.
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        String imageUrl;
        User user;

        /**
         * @param imageUrl the URL of the image to be downloaded
         * @param user the user to assign the resulting profile image Bitmap to
         */
        public DownloadImageTask(String imageUrl, User user) {
            this.imageUrl = imageUrl;
            this.user = user;
        }

        protected Bitmap doInBackground(String... urls) {
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(imageUrl).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            }
            catch(Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bitmap;
        }

        /**
         * Assigns the downloaded image in form of a bitmap to the referenced user object.
         * @param bitmap the downloaded image returned by doInBackground()
         */
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap != null && user != null) {
                user.setProfileImage(bitmap);
                Log.d(LOGTAG, String.format("image bitmap set for user %d: %s",
                    user.userId, user.userName));
            }
        }
    }

    /**
     * A JSON type adapter that is responsible for User object serialization
     */
    public static class JsonAdapter extends TypeAdapter<User> {

        @Override
        public void write(JsonWriter out, User user) throws IOException {
            out.beginObject();

            out.name("userId");
            out.value(user.getUserId());

            out.name("userName");
            out.value(user.getUserName());

            out.name("firstName");
            out.value(user.getFirstName());

            out.name("lastName");
            out.value(user.getLastName());

            out.name("emailAddress");
            out.value(user.getEmailAddress());

            out.name("confirmedEmailAddress");
            out.value(user.getConfirmedEmailAddress());

            out.name("linkedProfileType");
            out.value(user.getLinkedProfileType().toString());

            out.name("linkedProfileUserId");
            out.value(user.getLinkedProfileUserId());

            out.name("profileImageId");
            out.value(user.getProfileImageId());

            out.name("profileImageFileName");
            out.value(user.getProfileImageFileName());

            out.name("joinedDateTime");
            out.value(DatabaseClient.databaseDateFormat.format(user.getJoinedDateTime()));

            out.name("lastActiveDateTime");
            out.value(DatabaseClient.databaseDateFormat.format(user.getLastActiveDateTime()));

            out.name("lastRecipeCreatedDateTime");
            out.value(DatabaseClient.databaseDateFormat.format(user.getLastRecipeCreatedDateTime()));

            out.name("lastCollectionEditedDateTime");
            out.value(DatabaseClient.databaseDateFormat.format(user.getLastCollectionEditedDateTime()));

            out.name("lastCookModeUsedDateTime");
            out.value(DatabaseClient.databaseDateFormat.format(user.getLastCookModeUsedDateTime()));

            out.name("viewedCount");
            out.value(user.getViewedCount());

            out.name("followedCount");
            out.value(user.getFollowedCount());

            out.name("followingCount");
            out.value(user.getFollowingCount());

            out.name("verifiedState");
            out.value(user.getVerifiedState());

            out.name("userRights");
            out.value(user.getUserRights());

            out.endObject();
        }

        @Override
        public User read(JsonReader in) throws IOException {
            in.beginObject();

            in.nextName();
            long userId = in.nextLong();

            in.nextName();
            String userName = in.nextString();

            in.nextName();
            String firstName = in.nextString();

            in.nextName();
            String lastName = in.nextString();

            in.nextName();
            String emailAddress = in.nextString();

            in.nextName();
            int confirmedEmailAddress = in.nextInt();

            in.nextName();
            String linkedProfileType = in.nextString();

            in.nextName();
            String linkedProfileUserId = in.nextString();

            in.nextName();
            long profileImageId = in.nextLong();

            in.nextName();
            String profileImageFileName = in.nextString();

            in.nextName();
            String joinedDateTime = in.nextString();

            in.nextName();
            String lastActiveDateTime = in.nextString();

            in.nextName();
            String lastRecipeCreatedDateTime = in.nextString();

            in.nextName();
            String lastCollectionEditedDateTime = in.nextString();

            in.nextName();
            String lastCookModeUsedDateTime = in.nextString();

            in.nextName();
            int viewedCount= in.nextInt();

            in.nextName();
            int followedCount = in.nextInt();

            in.nextName();
            int followingCount = in.nextInt();

            in.nextName();
            int verifiedState = in.nextInt();

            in.nextName();
            long userRights = in.nextLong();

            in.nextInt();

            return new User(userId, userName, firstName, lastName, emailAddress, confirmedEmailAddress,
                linkedProfileType, linkedProfileUserId, profileImageId, profileImageFileName,
                joinedDateTime, lastActiveDateTime, lastRecipeCreatedDateTime,
                lastCollectionEditedDateTime, lastCookModeUsedDateTime, viewedCount,
                followedCount, followingCount, verifiedState, userRights);
        }
    }
}
