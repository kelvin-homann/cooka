package app.cooka.cookapp.model;

import android.content.Context;
import android.util.Log;

import java.util.List;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A class that represents a followee of a specified user. The field set is generic in that it
 * may represent different types of followees: either another user, a tag or a recipe collection.
 * It contains fields that change their meaning depending on the context type that the data query
 * shall serve. For specific meanings please see the documentation of the fields and their getters
 * and setters.
 */
public class Followee {

    public static final String LOGTAG = "COOKALOG";

    private EFolloweeType type;
    private long ofuserId;
    private long id;

    /**
     * Context depending meaning of displayName:
     * 1) the user name in case of type user;
     * 2) the tag name in case of type tag;
     * 3) the collection title in case of type collection.
     */
    private String displayName;

    /**
     * Context depending meaning of detail1:
     * 1) the first name in case of type user;
     * 2) the number of tagged recipes in case of type tag;
     * 3) the number of contained recipes in case of type collection.
     */
    private String detail1;

    /**
     * Context depending meaning of detail2:
     * 1) the last name in case of type user;
     * 2) null (unused);
     * 3) the collection creator (user) identifier in case of type collection.
     */
    private String detail2;

    /**
     * Context depending meaning of imageId:
     * 1) the user profile image identifier in case of type user;
     * 2) the image identifier of either a statically selected or dynamically (based on rating)
     *      selected image of one of the recipes tagged with the corresponding tag in case of
     *      type tag;
     * 3) the image identifier of either a statically selected or dynamically (based on rating)
     *      selected image of one of the recipes contained in the corresponding collection in
     *      case of type collection.
     */
    private long imageId;

    /**
     * 1) the user profile image file name in case of type user;
     * 2) the image file name of either a statically selected or dynamically (based on rating)
     *      selected image of one of the recipes tagged with the corresponding tag in case of
     *      type tag;
     * 3) the image file name of either a statically selected or dynamically (based on rating)
     *      selected image of one of the recipes contained in the corresponding collection in
     *      case of type collection.
     */
    private String imageFileName;

    /**
     * Context depending meaning of followerCount:
     * the number of users following the context depending type.
     */
    private int followerCount;

    /**
     * Context depending meaning of followeeCount:
     * the number of users, tags and collections followed by the given context depending type
     * (only valid/reasonable for users since only a user can actively follow a certain context
     * type).
     */
    private int followeeCount;

    public static class Factory {

        /**
         * Selects the user, tag and collection followees of the given user id and calls the
         * specified callback method on success or failure.
         * @param context the Android context to the run the method on.
         * @param ofuserId the identifier of the user to get the followees of.
         * @param selectUserFolloweesCallback the callback interface to be called on success or
         *      failure.
         * @return a subscription object to the select request; null if en error occurred.
         */
        public static Subscription selectUserFollowees(Context context, final long ofuserId,
            final IResultCallback<List<Followee>> selectUserFolloweesCallback)
        {
            if(selectUserFolloweesCallback == null) {
                throw new NullPointerException("select user followees callback is null");
            }
            return DatabaseClient.Factory.getInstance(context)
                .selectUserFollowees(ofuserId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Followee>>() {
                    @Override public void onCompleted() {}
                    @Override public void onError(Throwable e) {
                        Log.e(LOGTAG, "Followee.Factory.selectUserFollowees() failed");
                        Log.e(LOGTAG, e.getMessage());
                    }
                    @Override public void onNext(List<Followee> followees) {
                        selectUserFolloweesCallback.onSucceeded(followees);
                    }
                });
        }
    }

    public EFolloweeType getType() {
        return type;
    }

    public long getOfUserId() {
        return ofuserId;
    }

    public long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDetail1() {
        return detail1;
    }

    public String getDetail2() {
        return detail2;
    }

    public long getImageId() {
        return imageId;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public int getFolloweeCount() {
        return followeeCount;
    }
}
