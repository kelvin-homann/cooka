package app.cooka.cookapp.model;

import android.content.Context;
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
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A class that represents a user and that is an app model representation of the corresponding
 * database entity called Users. Used to serialize between app and database and to cache its
 * state without needing to repeatedly query the same unchanged object from the database.
 */
@JsonAdapter(User.JsonAdapter.class)
public class User extends java.util.Observable {

    public static final String LOGTAG = "COOKALOG";

    public static final int CHANGED_USERNAME = 0x00000001;
    public static final int CHANGED_NAME = 0x00000002;
    public static final int CHANGED_EMAILADDRESS = 0x00000004;
    public static final int CHANGED_VERIFIEDSTATE = 0x00000008;
    public static final int CHANGED_USERRIGHTS = 0x00000010;
    public static final int CHANGED_LINKEDPROFILE = 0x00000020;
    public static final int CHANGED_LASTDATETIME = 0x00000040;
    public static final int CHANGED_PROFILEIMAGE = 0x00000080;
    public static final int CHANGED_FORCE_UPDATE = 0xffffffff;

    private int changeState = 0;
    private boolean committed = false;

    private final long userId;
    private String userName;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private int confirmedEmailAddress = 0;
    private int verifiedState = 0;
    private long userRights;

    private ELinkedProfileType linkedProfileType = ELinkedProfileType.unlinked;
    private String linkedProfileUserId = null;

    private long profileImageId = 0;
    private String profileImageFileName = null;
    private Bitmap profileImage = null;

    private Date joinedDateTime = null;
    private Date lastActiveDateTime = null;
    private Date lastRecipeCreatedDateTime = null;
    private Date lastCollectionEditedDateTime = null;
    private Date lastCookModeUsedDateTime = null;

    private int viewedCount = 0;
    private int followerCount = 0;
    private int followeeCount = 0;

    private User(final long userId, String userName, String emailAddress, long userRights) {

        this.userId = userId;
        this.userName = userName;
        this.emailAddress = emailAddress;
        this.userRights = userRights;
    }

    private User(final long userId, String userName, String firstName, String lastName,
        String emailAddress, ELinkedProfileType linkedProfileType, String linkedProfileUserId,
        long userRights)
    {
        this.userId = userId;
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.linkedProfileType = linkedProfileType;
        this.linkedProfileUserId = linkedProfileUserId;
        this.userRights = userRights;
    }

    /**
     * This constructor is solely used by the user factory to initialize a user previously
     * deserialized from the database.
     */
    private User(final long userId, String userName, String firstName, String lastName,
        String emailAddress, int confirmedEmailAddress, String linkedProfileType,
        String linkedProfileUserId, long profileImageId, String profileImageFileName,
        String joinedDateTime, String lastActiveDateTime, String lastRecipeCreatedDateTime,
        String lastCollectionEditedDateTime, String lastCookModeUsedDateTime, int viewedCount,
        int followerCount, int followeeCount, int verifiedState, long userRights)
    {
        this.userId = userId;
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.confirmedEmailAddress = confirmedEmailAddress;

        try {
            if(linkedProfileType.length() > 0)
                this.linkedProfileType = ELinkedProfileType.valueOf(linkedProfileType);
        }
        catch(IllegalArgumentException e) {
            e.printStackTrace();
            Log.e(LOGTAG, String.format("could not parse linkedProfileType while creating user %d: %s. Set to null instead.", userId, userName));
        }

        this.linkedProfileUserId = linkedProfileUserId;
        this.profileImageId = profileImageId;

        setProfileImageFileName(profileImageFileName);

        try {
            if(joinedDateTime.length() > 0)
                this.joinedDateTime = DatabaseClient.databaseDateFormat.parse(joinedDateTime);
        }
        catch(ParseException e) {
            e.printStackTrace();
            Log.e(LOGTAG, String.format("could not parse joinedDateTime while creating user %d: %s. Set to null instead.", userId, userName));
        }

        try {
            if(lastActiveDateTime.length() > 0)
                this.lastActiveDateTime = DatabaseClient.databaseDateFormat.parse(lastActiveDateTime);
        }
        catch(ParseException e) {
            e.printStackTrace();
            Log.e(LOGTAG, String.format("could not parse lastActiveDateTime while creating user %d: %s. Set to null instead.", userId, userName));
        }

        try {
            if(lastRecipeCreatedDateTime.length() > 0)
                this.lastRecipeCreatedDateTime = DatabaseClient.databaseDateFormat.parse(lastRecipeCreatedDateTime);
        }
        catch(ParseException e) {
            e.printStackTrace();
            Log.e(LOGTAG, String.format("could not parse lastRecipeCreatedDateTime while creating user %d: %s. Set to null instead.", userId, userName));
        }

        try {
            if(lastCollectionEditedDateTime.length() > 0)
                this.lastCollectionEditedDateTime = DatabaseClient.databaseDateFormat.parse(lastCollectionEditedDateTime);
        }
        catch(ParseException e) {
            e.printStackTrace();
            Log.e(LOGTAG, String.format("could not parse lastCollectionEditedDateTime while creating user %d: %s. Set to null instead.", userId, userName));
        }

        try {
            if(lastCookModeUsedDateTime.length() > 0)
                this.lastCookModeUsedDateTime = DatabaseClient.databaseDateFormat.parse(lastCookModeUsedDateTime);
        }
        catch(ParseException e) {
            e.printStackTrace();
            Log.e(LOGTAG, String.format("could not parse lastCookModeUsedDateTime while creating user %d: %s. Set to null instead.", userId, userName));
        }

        this.viewedCount = viewedCount;
        this.followerCount = followerCount;
        this.followeeCount = followeeCount;
        this.verifiedState = verifiedState;
        this.userRights = userRights;
    }

    /**
     * A user factory class that does user creation, selection and database serialization.
     */
    public static class Factory {

        /**
         * Creates a new user in the database and a new connected user object.
         * @return a subscription object to the create request; null if en error occurred.
         */
        public static Subscription createUser(final Context context, final String userName,
            final String emailAddress, final long userRights)
        {
            // execute database insert and receive a valid user id
            // create and return user object
            return null;
        }

        public static void createUser(final Context context, final String userName, final String firstName,
            final String lastName, final String emailAddress, final String hashedPassword,
            final String salt, final String accessToken, final ELinkedProfileType linkedProfileType,
            final String linkedProfileUserId, final long userRights, final String deviceId,
            final ICreateUserCallback createUserCallback)
        {
            if(createUserCallback == null) {
                throw new NullPointerException("create user callback is null");
            }
            DatabaseClient.Factory.getInstance(context)
                .createUser(userName, firstName, lastName, emailAddress, hashedPassword,
                    salt, accessToken, linkedProfileType, linkedProfileUserId, userRights,
                    deviceId)
                .enqueue(new Callback<CreateUserResult>() {
                    @Override
                    public void onResponse(Call<CreateUserResult> call, Response<CreateUserResult> response) {
                        CreateUserResult createUserResult = response.body();
                        User createdUser = new User(createUserResult.userId, userName, firstName, lastName,
                            emailAddress, linkedProfileType, linkedProfileUserId, userRights);
                        // todo: register new user at the update observer
                        createUserCallback.onSucceeded(createUserResult, createdUser);
                    }

                    @Override
                    public void onFailure(Call<CreateUserResult> call, Throwable t) {
                        Log.e(LOGTAG, "User.Factory.createUser() failed");
                        Log.e(LOGTAG, t.getMessage());
                        createUserCallback.onFailed();
                    }
                });
        }

        /**
         * Selects an existing user from the database and creates a connected user object.
         * @param context the Android context to the run the method on.
         * @param selectUserId the identifier of the user to get the user object of.
         * @param selectUserCallback
         * @return a subscription object to the select request; null if en error occurred.
         */
        public static Subscription selectUser(final Context context, final long selectUserId,
            final IResultCallback<User> selectUserCallback)
        {
            if(selectUserCallback == null) {
                throw new NullPointerException("select user callback is null");
            }
            return DatabaseClient.Factory.getInstance(context)
                .selectUser(selectUserId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<User>() {
                    @Override public void onCompleted() {}
                    @Override public void onError(Throwable e) {
                        Log.e(LOGTAG, "User.Factory.selectUser() failed");
                        Log.e(LOGTAG, e.getMessage());
                    }
                    @Override public void onNext(User user) {
                        // todo: register user at the update observer
                        selectUserCallback.onSucceeded(user);
                    }
                });
        }

        /**
         * Selects multiple existing users from the database, creates connected user objects and
         * puts them into a list.
         * @param context the Android context to the run the method on.
         * @param selectUsersCallback the select callback that will be called when the selection
         * succeeded and that will be given the list of user objects
         * @return a subscription object to the select request; null if an error occurred.
         */
        public static Subscription selectUsers(final Context context,
            final IResultCallback<List<User>> selectUsersCallback)
        {
            if(selectUsersCallback == null) {
                throw new NullPointerException("select users callback is null");
            }
            return DatabaseClient.Factory.getInstance(context)
                .selectUsers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<User>>() {
                    @Override public void onCompleted() {}
                    @Override public void onError(Throwable e) {
                        Log.e(LOGTAG, "User.Factory.selectUsers() failed");
                        Log.e(LOGTAG, e.getMessage());
                    }
                    @Override public void onNext(List<User> users) {
                        // todo: register all users at the update observer
                        selectUsersCallback.onSucceeded(users);
                    }
                });
        }

        public static Subscription selectUsers(final Context context, final IResultCallback<List<User>>
            selectUsersCallback, final SelectModifier... modifiers)
        {
            if(selectUsersCallback == null) {
                throw new NullPointerException("select users callback is null");
            }
            return DatabaseClient.Factory.getInstance(context)
                .selectUsers(modifiers)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<User>>() {
                    @Override public void onCompleted() {}
                    @Override public void onError(Throwable e) {
                        Log.e(LOGTAG, "User.Factory.selectUsers() failed");
                        Log.e(LOGTAG, e.getMessage());
                    }
                    @Override public void onNext(List<User> users) {
                        // todo: register all users at the update observer
                        selectUsersCallback.onSucceeded(users);
                    }
                });
        }

        /**
         * Selects multiple existing users from the database, creates connected user objects and
         * adds them to the specified list.
         * @param usersReceivingList the list that will receive the selected user objects
         * @return a subscription object to the select request; null if an error occurred.
         */
        public static Subscription selectUsers(final Context context, final List<User>
            usersReceivingList)
        {
            if(usersReceivingList == null) {
                throw new NullPointerException("users receiving list is null");
            }
            return DatabaseClient.Factory.getInstance(context)
                .selectUsers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<User>>() {
                    @Override public void onCompleted() {}
                    @Override public void onError(Throwable e) {
                        Log.e(LOGTAG, "User.Factory.selectUsers() failed");
                        Log.e(LOGTAG, e.getMessage());
                    }
                    @Override public void onNext(List<User> users) {
                        // todo: register all users at the update observer
                        usersReceivingList.addAll(users);
                    }
                });
        }

        /**
         * Selects user followers of the given user id and calls the specified callback method on
         * success or failure.
         * @param context the Android context to the run the method on.
         * @param ofuserId the identifier of the user to get the user followers of.
         * @param selectUserFollowersCallback the callback interface to be called on success or
         *      failure.
         * @return a subscription object to the select request; null if en error occurred.
         */
        public static Subscription selectUserFollowers(final Context context,
            final long ofuserId, final IResultCallback<List<Follower>> selectUserFollowersCallback)
        {
            return Follower.Factory.selectUserFollowers(context, ofuserId,
                selectUserFollowersCallback);
        }

        /**
         * Selects the user, tag and collection followees of the given user id and calls the
         * specified callback method on success or failure.
         * @param context the Android context to the run the method on.
         * @param ofuserId the identifier of the user to get the followees of.
         * @param selectUserFolloweesCallback the callback interface to be called on success or
         *      failure.
         * @return a subscription object to the select request; null if en error occurred.
         */
        public static Subscription selectUserFollowees(final Context context,
            final long ofuserId, final IResultCallback<List<Followee>> selectUserFolloweesCallback)
        {
            return Followee.Factory.selectUserFollowees(context, ofuserId,
                selectUserFolloweesCallback);
        }

        /**
         * Follows the user identified by followUserId and calls the corresponding callback methods
         * if provided.
         * @param context the Android context to the run the method on.
         * @param followUserId the identifier of the user to be followed.
         * @param followUserCallback the callback interface to be called on success or on failure
         *      (may be null).
         */
        public static void followUser(final Context context, final long followUserId, final
            IFollowUserCallback followUserCallback)
        {
            DatabaseClient.Factory.getInstance(context)
                .followUser(followUserId)
                .enqueue(new Callback<FollowUserResult>() {
                    @Override
                    public void onResponse(Call<FollowUserResult> call, Response<FollowUserResult>
                        response)
                    {
                        final FollowUserResult result = response.body();
                        if(result == null) {
                            Log.e(LOGTAG, "User.Factory.followUser() failed on response");
                            if(followUserCallback != null)
                                followUserCallback.onFailed(null);
                            return;
                        }
                        if(result.resultCode != 0) {
                            Log.e(LOGTAG, String.format("User.Factory.followUser() failed with code %d: %s",
                                result.resultCode, result.resultMessage));
                            if(followUserCallback != null)
                                followUserCallback.onFailed(result);
                            return;
                        }
                        if(followUserCallback != null)
                            followUserCallback.onSucceeded(result);
                    }

                    @Override
                    public void onFailure(Call<FollowUserResult> call, Throwable t) {
                        Log.e(LOGTAG, "User.Factory.followUser() failed: the web service did not respond");
                        t.printStackTrace();
                        if(followUserCallback != null)
                                followUserCallback.onFailed(null);
                    }
                });
        }

        /**
         * Follows the users identified by the ids given in the followUserIds list and calls the
         * corresponding callback methods if provided.
         * @param context the Android context to the run the method on.
         * @param followUserIds list of identifiers of the users to be followed.
         * @param followUserCallback the callback interface to be called on success or on failure
         *      (may be null).
         */
        public static void followUsers(final Context context, final List<Long> followUserIds, final
            IFollowUserCallback followUserCallback)
        {
            DatabaseClient.Factory.getInstance(context)
                .followUsers(followUserIds)
                .enqueue(new Callback<FollowUserResult>() {
                    @Override
                    public void onResponse(Call<FollowUserResult> call, Response<FollowUserResult>
                        response)
                    {
                        final FollowUserResult result = response.body();
                        if(result == null) {
                            Log.e(LOGTAG, "User.Factory.followUsers() failed on response");
                            if(followUserCallback != null)
                                followUserCallback.onFailed(null);
                            return;
                        }
                        if(result.resultCode != 0) {
                            Log.e(LOGTAG, String.format("User.Factory.followUsers() failed with code %d: %s",
                                result.resultCode, result.resultMessage));
                            if(followUserCallback != null)
                                followUserCallback.onFailed(result);
                            return;
                        }
                        if(followUserCallback != null)
                            followUserCallback.onSucceeded(result);
                    }

                    @Override
                    public void onFailure(Call<FollowUserResult> call, Throwable t) {
                        Log.e(LOGTAG, "User.Factory.followUsers() failed: the web service did not respond");
                        t.printStackTrace();
                        if(followUserCallback != null)
                                followUserCallback.onFailed(null);
                    }
                });
        }
    }

    /**
     * Gets the change state of this category instance that reflects what fields have changed
     * since the last synchronization. Basically an extended dirty flag.
     * @return the change state bit field
     */
    public long getChangeState() {
        return changeState;
    }

    public void resetChangeState() {
        changeState = 0;
    }

    public void setForceUpdate() {
        changeState = CHANGED_FORCE_UPDATE;
    }

    public boolean getCommited() {
        return committed;
    }

    public void resetCommitted() {
        this.committed = false;
    }

    public void commit() {
        this.committed = true;
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

    /**
     * Gets a full name from first name and last name if at least one of them is set. Gets the
     * user name otherwise.
     * @return a full name from first name and last name if at least one of them is set; the user
     *      name otherwise.
     */
    public String getFullName() {
        if(firstName != null && firstName.length() > 0 && lastName != null && lastName.length() > 0)
            return firstName + " " + lastName;
        else if(firstName != null && firstName.length() > 0)
            return firstName;
        if(lastName != null && lastName.length() > 0)
            return lastName;
        else
            return userName;
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

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
        setChanged();
        notifyObservers();
    }

    public int getFolloweeCount() {
        return followeeCount;
    }

    public void setFolloweeCount(int followeeCount) {
        this.followeeCount = followeeCount;
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

            out.name("followerCount");
            out.value(user.getFollowerCount());

            out.name("followeeCount");
            out.value(user.getFolloweeCount());

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
            int followerCount = in.nextInt();

            in.nextName();
            int followeeCount = in.nextInt();

            in.nextName();
            int verifiedState = in.nextInt();

            in.nextName();
            long userRights = in.nextLong();

            in.endObject();

            return new User(userId, userName, firstName, lastName, emailAddress, confirmedEmailAddress,
                linkedProfileType, linkedProfileUserId, profileImageId, profileImageFileName,
                joinedDateTime, lastActiveDateTime, lastRecipeCreatedDateTime,
                lastCollectionEditedDateTime, lastCookModeUsedDateTime, viewedCount,
                followerCount, followeeCount, verifiedState, userRights);
        }
    }
}
