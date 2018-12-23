package app.cooka.cookapp.model;

import android.content.Context;
import android.util.Log;

import java.util.List;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A class that represents a follower either another user, a tag or a recipe collection. It
 * is a generic class in that it fields need to be laid out in a known context. The the field
 * descriptions for further information. This class is an abstract app model representation of the
 * corresponding database entities called UserUserFollows, UserTagFollows and UserCollectionFollows.
 */
public class Follower {

    public static final String LOGTAG = "COOKALOG";

    /**
     * Context dependent identifier, either representing a user, tag or collection
     */
    private long oftypeId;

    /**
     * The identifier of the user that is following the context type that is either another user,
     * a tag or a recipe collection.
     */
    private long userId;
    private String userName;
    private String firstName;
    private String lastName;
    private long profileImageId;
    private String profileImageFileName;

    /**
     * The number of users that follow this context dependent type identified by oftypeId.
     */
    private int followerCount = 0;

    /**
     * The number of users, tags and collections that are being followed by the context dependent
     * type (only valid/reasonable for users since only a user can actively follow a certain
     * context type).
     */
    private int followeeCount = 0;
    private int verifiedState = 0;

    public static class Factory {

        /**
         * Selects user followers of the given user id and calls the specified callback method on
         * success or failure.
         * @param context the Android context to the run the method on.
         * @param ofuserId the identifier of the user to get the user followers of.
         * @param selectUserFollowersCallback the callback interface to be called on success or
         *      failure.
         * @return a subscription object to the select request; null if en error occurred.
         */
        public static Subscription selectUserFollowers(Context context, final long ofuserId,
            final IResultCallback<List<Follower>> selectUserFollowersCallback)
        {
            if(selectUserFollowersCallback == null) {
                throw new NullPointerException("select user followers callback is null");
            }
            return DatabaseClient.Factory.getInstance(context)
                .selectUserFollowers(ofuserId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Follower>>() {
                    @Override public void onCompleted() {}
                    @Override public void onError(Throwable e) {
                        Log.e(LOGTAG, "Follower.Factory.selectUserFollowers() failed");
                        Log.e(LOGTAG, e.getMessage());
                    }
                    @Override public void onNext(List<Follower> followers) {
                        selectUserFollowersCallback.onSucceeded(followers);
                    }
                });
        }

        /**
         * Selects user followers of the given tag id and calls the specified callback method on
         * success or failure.
         * @param context the Android context to the run the method on.
         * @param oftagId the identifier of the tag to get the user followers of.
         * @param selectTagFollowersCallback the callback interface to be called on success or
         *      failure.
         * @return a subscription object to the select request; null if en error occurred.
         */
        public static Subscription selectTagFollowers(Context context, final long oftagId,
            final IResultCallback<List<Follower>> selectTagFollowersCallback)
        {
            if(selectTagFollowersCallback == null) {
                throw new NullPointerException("select tag followers callback is null");
            }
            return DatabaseClient.Factory.getInstance(context)
                .selectTagFollowers(oftagId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Follower>>() {
                    @Override public void onCompleted() {}
                    @Override public void onError(Throwable e) {
                        Log.e(LOGTAG, "Follower.Factory.selectTagFollowers() failed");
                        Log.e(LOGTAG, e.getMessage());
                    }
                    @Override public void onNext(List<Follower> followers) {
                        selectTagFollowersCallback.onSucceeded(followers);
                    }
                });
        }

        /**
         * Selects user followers of the given collection id and calls the specified callback
         * method on success or failure.
         * @param context the Android context to the run the method on.
         * @param ofcollectionId the identifier of the collection to get the user followers of.
         * @param selectCollectionFollowersCallback the callback interface to be called on success
         *      or failure.
         * @return a subscription object to the select request; null if en error occurred.
         */
        public static Subscription selectCollectionFollowers(Context context, final long
            ofcollectionId, final IResultCallback<List<Follower>> selectCollectionFollowersCallback)
        {
            if(selectCollectionFollowersCallback == null) {
                throw new NullPointerException("select collection followers callback is null");
            }
            return DatabaseClient.Factory.getInstance(context)
                .selectCollectionFollowers(ofcollectionId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Follower>>() {
                    @Override public void onCompleted() {}
                    @Override public void onError(Throwable e) {
                        Log.e(LOGTAG, "Follower.Factory.selectCollectionFollowers() failed");
                        Log.e(LOGTAG, e.getMessage());
                    }
                    @Override public void onNext(List<Follower> followers) {
                        selectCollectionFollowersCallback.onSucceeded(followers);
                    }
                });
        }
    }

    public long getOftypeId() {
        return oftypeId;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public long getProfileImageId() {
        return profileImageId;
    }

    public String getProfileImageFileName() {
        return profileImageFileName;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public int getFolloweeCount() {
        return followeeCount;
    }

    public int getVerifiedState() {
        return verifiedState;
    }
}
